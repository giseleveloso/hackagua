package br.unitins.topicos1.resource.ws;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

import org.jboss.logging.Logger;

import br.unitins.topicos1.dto.LeituraDTO;
import br.unitins.topicos1.service.LeituraService;
import br.unitins.topicos1.service.MedidorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/sensor/{uuid}")
@ApplicationScoped
public class SensorSocket {

    private static final Logger LOG = Logger.getLogger(SensorSocket.class);

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    LeituraService leituraService;

    @Inject
    MedidorService medidorService;

    

    @OnOpen
    public void onOpen(Session session, @PathParam("uuid") String medidorId) {
        sessions.put(medidorId, session);
        LOG.infof("Arduino conectado - Medidor ID: %s", medidorId);
    }

    @OnClose
    public void onClose(Session session, @PathParam("uuid") String medidorId) {
        sessions.remove(medidorId);
        LOG.infof("Arduino desconectado - Medidor ID: %s", medidorId);
    }

    @OnError
    public void onError(Session session, @PathParam("uuid") String medidorId, Throwable throwable) {
        sessions.remove(medidorId);
        LOG.errorf(throwable, "Erro na conexão WebSocket - Medidor ID: %s", medidorId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("uuid") String medidorId) {
        try {
            LOG.infof("Mensagem recebida do medidor %s: %s", medidorId, message);

            String[] parts = message.split(";");
            if (parts.length < 2) {
                LOG.warnf("Mensagem com formato inválido: %s", message);
                return;
            }

            switch (parts[0]) {
                case "01" -> {
                    if (parts.length >= 4) {
                        long id = Long.parseLong(parts[1]);
                        BigDecimal consumo = BigDecimal.valueOf(Double.parseDouble(parts[2]));
                        BigDecimal vazao = BigDecimal.valueOf(Double.parseDouble(parts[3]));

                        CompletableFuture.runAsync(() -> {
                            leituraService.registrarLeitura(new LeituraDTO(id, consumo, vazao));
                        }).whenComplete((v, t) -> {
                            if (t == null) {
                                LOG.infof("Leitura registrada - Medidor: %s, Consumo: %sL, Vazão: %sL/min", parts[1], parts[2], parts[3]);
                            } else {
                                LOG.errorf(t, "Erro ao processar mensagem: %s", message);
                            }
                        });
                    } else {
                        LOG.warnf("Mensagem tipo 01 com formato inválido: %s", message);
                    }
                }
                case "02" -> {
                    if (parts.length >= 3) {
                        long id = Long.parseLong(parts[1]);
                        boolean ligado = "ON".equalsIgnoreCase(parts[2]);

                        CompletableFuture.runAsync(() -> {
                            medidorService.updatePowerStatus(id, ligado);
                        }).whenComplete((v, t) -> {
                            if (t == null) {
                                LOG.infof("Status atualizado - Medidor: %s, Ligado: %s", parts[1], ligado);
                            } else {
                                LOG.errorf(t, "Erro ao processar mensagem: %s", message);
                            }
                        });
                    } else {
                        LOG.warnf("Mensagem tipo 02 com formato inválido: %s", message);
                    }
                }
                default -> LOG.warnf("Tipo de mensagem desconhecido: %s", parts[0]);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Erro ao processar mensagem: %s", message);
        }
    }

    public boolean powerUpdate(boolean power, String uuid) {
        Session session = sessions.get(uuid);
        if (session == null || !session.isOpen()) {
            LOG.warnf("Tentativa de enviar comando para sessão inválida - Medidor ID: %s", uuid);
            return false;
        }
        try {
            String command = power ? "03;ON" : "03;OFF";
            session.getAsyncRemote().sendText(command, result -> {
                if (result.getException() != null) {
                    LOG.errorf(result.getException(), "Erro ao enviar comando para medidor %s", uuid);
                }
            });
            LOG.infof("Comando enviado para medidor %s: %s", uuid, command);
            return true;
        } catch (Exception e) {
            LOG.errorf(e, "Erro ao enviar powerUpdate para medidor %s", uuid);
            return false;
        }
    }
}
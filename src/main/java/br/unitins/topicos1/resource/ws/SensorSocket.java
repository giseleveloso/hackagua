package br.unitins.topicos1.resource.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.unitins.topicos1.service.LeituraService;
import br.unitins.topicos1.service.MedidorService;
import br.unitins.topicos1.repository.MedidorRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.Session;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import br.unitins.topicos1.dto.LeituraDTO;
import java.math.BigDecimal;
import br.unitins.topicos1.model.Medidor;

@ServerEndpoint("/ws/sensor/{uuid}")
@ApplicationScoped
public class SensorSocket {

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    LeituraService leituraService;

    @Inject
    MedidorService medidorService;

    @Inject
    MedidorRepository medidorRepository;

    @OnOpen
    public void onOpen(Session session, @PathParam("uuid") String username) {
        broadcast("User " + username + " joined");
        sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("uuid") String username) {
        sessions.remove(username);
        broadcast("User " + username + " left");
    }

    @OnError
    public void onError(Session session, @PathParam("uuid") String username, Throwable throwable) {
        sessions.remove(username);
        broadcast("User " + username + " left on error: " + throwable);
    }

    @Transactional
    @OnMessage
    public void onMessage(String message, @PathParam("uuid") String username) {

        String[] parts = message.split(";");

        switch (parts[0]) {
            case "01" -> {
                leituraService.registrarLeitura(new LeituraDTO(
                    Long.parseLong(parts[1]), 
                    BigDecimal.valueOf(Double.parseDouble(parts[2])), 
                    BigDecimal.valueOf(Double.parseDouble((parts[3])))
                ));
            }
            case "02" -> {
                medidorService.setPower(Long.parseLong(parts[1]), parts[1].equals("ON"));
            }
        }
    }

    private void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendText(message, result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    public boolean powerUpdate(boolean power, String uuid) {
        Session session = sessions.get(uuid);
        if (session == null || !session.isOpen())
            return false;
        try {
            session.getAsyncRemote().sendText(power ? "03;ON" : "03;OFF", result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException()); // TODO: log
                }
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
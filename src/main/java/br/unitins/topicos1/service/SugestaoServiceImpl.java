package br.unitins.topicos1.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.unitins.topicos1.dto.SugestaoIaItemResponseDTO;
import br.unitins.topicos1.dto.SugestaoIaResponseDTO;
import br.unitins.topicos1.model.Medidor;
import br.unitins.topicos1.model.SugestaoIa;
import br.unitins.topicos1.model.SugestaoIaItem;
import br.unitins.topicos1.model.Usuario;
import br.unitins.topicos1.repository.MedidorRepository;
import br.unitins.topicos1.repository.SugestaoIaRepository;
import br.unitins.topicos1.repository.UsuarioRepository;
import br.unitins.topicos1.validation.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SugestaoServiceImpl implements SugestaoService {

    @Inject
    public UsuarioRepository usuarioRepository;

    @Inject
    public MedidorRepository medidorRepository;

    @Inject
    public EstatisticaService estatisticaService;

    @Inject
    public GeminiClient geminiClient;

    @Inject
    public SugestaoIaRepository sugestaoIaRepository;

    @ConfigProperty(name = "gemini.api.key")
    String apiKey;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    @Transactional
    public SugestaoIaResponseDTO gerarSugestoesIAParaMedidor(Long medidorId, String dataInicio, String dataFim) {
        Medidor medidor = medidorRepository.findById(medidorId);
        if (medidor == null)
            throw new ValidationException("medidorId", "Medidor não encontrado.");

        Long usuarioId = medidor.getUsuario() != null ? medidor.getUsuario().getId() : null;
        if (usuarioId == null)
            throw new ValidationException("usuarioId", "Usuário do medidor não encontrado.");

        var stats = estatisticaService.calcularEstatisticas(medidorId);

        String entrada = "{"
                + "\"cenario\":\"" + (medidor.getLocalizacao() != null ? medidor.getLocalizacao() : medidor.getNome())
                + "\","
                + "\"periodo\":{\"inicio\":\"" + java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay()
                + "\",\"fim\":\"" + java.time.LocalDateTime.now() + "\"},"
                + "\"usuario\":{\"id\":" + usuarioId + ",\"valorM3\":"
                + (medidor.getUsuario().getValorM() != null ? medidor.getUsuario().getValorM() : 0) + "},"
                + "\"medidores\":[{"
                + "\"id\":" + medidor.getId() + ","
                + "\"nome\":\"" + medidor.getNome() + "\","
                + "\"localizacao\":\"" + (medidor.getLocalizacao() != null ? medidor.getLocalizacao() : "") + "\","
                + "\"tipo\":\"" + inferTipo(medidor) + "\","
                + "\"ligado\":" + medidor.getLigado() + ","
                + "\"estatisticas\":{\"litrosTotal\":" + stats.totalLitros() + ",\"m3Total\":" + stats.totalM3()
                + ",\"custo\":" + stats.custoEstimado() + ",\"vazaoMediaLMin\":" + stats.vazaoMediaLMin() + "}"
                + "}]"
                + "}";

        String systemPrompt = "Você é um especialista em gestão de água e economia hídrica. Analise dados de medidores de água e forneça sugestões personalizadas, práticas específicas para cada localização.\n"
                + "REGRAS:\n"
                + "- Responda SOMENTE em JSON seguindo o schema fornecido.\n"
                + "- TODOS OS CAMPOS SÃO OBRIGATÓRIOS E NÃO PODEM SER VAZIOS: 'observacoes' deve ser uma string não vazia; cada item em 'sugestoes' deve conter 'titulo' e 'descricao' não vazios e 'economiaEstimadaReais' numérico.\n"
                + "- Se a situação estiver dentro do esperado, ainda assim forneça pelo menos UMA sugestão útil (ex: otimização, reuso, manutenção preventiva) com título/descrição claros.\n"
                + "- Foque em ações concretas, custo/benefício e impacto estimado.\n"
                + "- Não invente dados; use apenas valores/estatísticas recebidos.\n"
                + "- Se dados forem insuficientes, explique em 'observacoes' e forneça UMA sugestão conservadora.\n"
                + "HEURÍSTICAS:\n- Vazamento provável: vazão > 0.1 L/min por > 30 min contínuos fora de horários típicos OU fluxo contínuo prolongado.\n- Pico anômalo: consumo/hora > p95 do próprio medidor no período.\n- Gasto alto: m³ do período > média histórica + 20%.\n- Reuso: priorize sugestões de reaproveitamento quando fizer sentido (cozinha, lavanderia, jardim).\n"
                + "SCHEMA EXATO:\n"
                + "{\\n  \"observacoes\": \"string não vazia\",\\n  \"sugestoes\": [\\n    { \\n      \"titulo\": \"string não vazia\",\\n      \"descricao\": \"string não vazia\",\\n      \"economiaEstimadaReais\": number \\n    }\\n  ]\\n}\n"
                + "VALIDAÇÃO:\n- Caso um campo fique vazio, reescreva o conteúdo para cumprir as regras antes de responder.\n"
                + "SAÍDA:\n- Exatamente 5 sugestões;. Retorne APENAS o JSON final válido.";

        if (apiKey == null || apiKey.isBlank())
            throw new ValidationException("apiKey", "GEMINI_API_KEY não configurada");

        try {
            String response = geminiClient.generateJson(apiKey, systemPrompt, entrada);
            Usuario usuario = usuarioRepository.findById(usuarioId);

            SugestaoIa entidade = parseSugestaoIa(response, usuario, medidor);
            sugestaoIaRepository.persist(entidade);

            List<SugestaoIaItemResponseDTO> itens = entidade.getItens().stream()
                    .map(i -> new SugestaoIaItemResponseDTO(i.getTitulo(), i.getDescricao(),
                            i.getEconomiaEstimadaReais()))
                    .toList();

            return new SugestaoIaResponseDTO(medidor.getId(), entidade.getObservacoes(), itens);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao chamar Gemini", e);
        }
    }

    private String inferTipo(Medidor m) {
        String loc = m.getLocalizacao() != null ? m.getLocalizacao().toLowerCase() : "";
        if (loc.contains("cozinha"))
            return "cozinha";
        if (loc.contains("lavander"))
            return "lavanderia";
        if (loc.contains("jardim") || loc.contains("externa"))
            return "jardim";
        return "geral";
    }

    private SugestaoIa parseSugestaoIa(String json, Usuario usuario, Medidor medidor) {
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.has("candidates")) {
                JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (!textNode.isMissingNode()) {
                    root = MAPPER.readTree(textNode.asText());
                }
            }

            SugestaoIa s = new SugestaoIa();
            s.setUsuario(usuario);
            s.setMedidor(medidor);
            s.setDataHora(LocalDateTime.now());
            String observ = root.has("observacoes") ? root.get("observacoes").asText() : null;
            if (observ == null || observ.isBlank()) {
                observ = "Sem observações adicionais para o período analisado.";
            }
            s.setObservacoes(observ);

            JsonNode arr = root.has("sugestoes") ? root.get("sugestoes") : root.get("sugestoesEconomia");
            if (arr != null && arr.isArray() && arr.size() > 0) {
                for (JsonNode n : arr) {
                    SugestaoIaItem item = new SugestaoIaItem();
                    String titulo = n.has("titulo") ? n.get("titulo").asText() : "Sugestão";
                    if (titulo == null || titulo.isBlank()) {
                        titulo = "Sugestão";
                    }
                    String descricao = n.has("descricao") ? n.get("descricao").asText() : null;
                    if (descricao == null || descricao.isBlank()) {
                        descricao = "Aprimore o uso adotando práticas de reuso e manutenção preventiva.";
                    }
                    BigDecimal econ = BigDecimal.ZERO;
                    if (n.has("economiaEstimadaReais") && n.get("economiaEstimadaReais").isNumber()) {
                        econ = new BigDecimal(n.get("economiaEstimadaReais").asText());
                    }
                    item.setTitulo(titulo);
                    item.setDescricao(descricao);
                    item.setEconomiaEstimadaReais(econ);
                    s.addItem(item);
                }
            } else {
                SugestaoIaItem item = new SugestaoIaItem();
                item.setTitulo("Sem sugestões automáticas");
                item.setDescricao("Consumo dentro do esperado para o período analisado.");
                item.setEconomiaEstimadaReais(BigDecimal.ZERO);
                s.addItem(item);
            }
            return s;
        } catch (Exception e) {
            SugestaoIa s = new SugestaoIa();
            s.setUsuario(usuario);
            s.setMedidor(medidor);
            s.setDataHora(LocalDateTime.now());
            s.setObservacoes("Não foi possível estruturar a resposta da IA");
            SugestaoIaItem item = new SugestaoIaItem();
            item.setTitulo("Sugestão indisponível");
            item.setDescricao("Falha ao interpretar o retorno da IA. Tente novamente mais tarde.");
            item.setEconomiaEstimadaReais(BigDecimal.ZERO);
            s.addItem(item);
            return s;
        }
    }
}
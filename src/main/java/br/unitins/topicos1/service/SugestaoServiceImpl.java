package br.unitins.topicos1.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.dto.SugestaoDTO;
import br.unitins.topicos1.dto.SugestaoIaItemResponseDTO;
import br.unitins.topicos1.dto.SugestaoIaResponseDTO;
import br.unitins.topicos1.dto.SugestaoResponseDTO;
import br.unitins.topicos1.model.Medidor;
import br.unitins.topicos1.model.Sugestao;
import br.unitins.topicos1.model.SugestaoIa;
import br.unitins.topicos1.model.SugestaoIaItem;
import br.unitins.topicos1.model.Usuario;
import br.unitins.topicos1.repository.MedidorRepository;
import br.unitins.topicos1.repository.SugestaoIaRepository;
import br.unitins.topicos1.repository.SugestaoRepository;
import br.unitins.topicos1.repository.UsuarioRepository;
import br.unitins.topicos1.validation.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class SugestaoServiceImpl implements SugestaoService {

    @Inject
    public SugestaoRepository sugestaoRepository;

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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    @Transactional
    public SugestaoResponseDTO create(@Valid SugestaoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId());
        if (usuario == null)
            throw new ValidationException("usuarioId", "Usuário não encontrado.");

        Sugestao sugestao = new Sugestao();
        sugestao.setUsuario(usuario);
        sugestao.setMensagem(dto.mensagem());
        sugestao.setDataHora(LocalDateTime.now());

        sugestaoRepository.persist(sugestao);
        return SugestaoResponseDTO.valueOf(sugestao);
    }

    @Override
    public List<SugestaoResponseDTO> findByUsuarioId(Long usuarioId) {
        return sugestaoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(SugestaoResponseDTO::valueOf)
                .toList();
    }

    @Override
    @Transactional
    public SugestaoIaResponseDTO gerarSugestoesIAParaMedidor(Long medidorId, String dataInicio, String dataFim) {
        Medidor medidor = medidorRepository.findById(medidorId);
        if (medidor == null)
            throw new ValidationException("medidorId", "Medidor não encontrado.");

        Long usuarioId = medidor.getUsuario() != null ? medidor.getUsuario().getId() : null;
        if (usuarioId == null)
            throw new ValidationException("usuarioId", "Usuário do medidor não encontrado.");

        // Usa estatística mensal padrão (outra opção: aceitar período customizado)
        var stats = estatisticaService.calcularEstatisticas(medidorId);

        // Monta entrada JSON
        String entrada = "{"
            + "\"cenario\":\"" + (medidor.getLocalizacao() != null ? medidor.getLocalizacao() : medidor.getNome()) + "\","
            + "\"periodo\":{\"inicio\":\"" + java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay() + "\",\"fim\":\"" + java.time.LocalDateTime.now() + "\"},"
            + "\"usuario\":{\"id\":" + usuarioId + ",\"valorM3\":" + (medidor.getUsuario().getValorM() != null ? medidor.getUsuario().getValorM() : 0) + "},"
            + "\"medidores\":[{"
                + "\"id\":" + medidor.getId() + ","
                + "\"nome\":\"" + medidor.getNome() + "\","
                + "\"localizacao\":\"" + (medidor.getLocalizacao() != null ? medidor.getLocalizacao() : "") + "\","
                + "\"tipo\":\"" + inferTipo(medidor) + "\","
                + "\"ligado\":" + medidor.getLigado() + ","
                + "\"estatisticas\":{\"litrosTotal\":" + stats.totalLitros() + ",\"m3Total\":" + stats.totalM3() + ",\"custo\":" + stats.custoEstimado() + ",\"vazaoMediaLMin\":" + stats.vazaoMediaLMin() + "}"
            + "}]"
        + "}";

        String systemPrompt =
            "Você é um especialista em gestão de água e economia hídrica. Analise dados de medidores de água e forneça sugestões personalizadas, práticas específicas para cada localização.\n"
          + "REGRAS:\n- Responda SOMENTE em JSON seguindo o schema fornecido.\n- Foque em ações concretas, custo/benefício e impacto estimado.\n- Não invente dados; use apenas valores/estatísticas recebidos.\n- Se dados forem insuficientes, reduza recomendações e explique em observações.\n"
          + "HEURÍSTICAS:\n- Vazamento provável: vazão > 0.1 L/min por > 30 min contínuos fora de horários típicos OU fluxo contínuo prolongado.\n- Pico anômalo: consumo/hora > p95 do próprio medidor no período.\n- Gasto alto: m³ do período > média histórica + 20%.\n- Reuso: priorize sugestões de reaproveitamento quando fizer sentido (cozinha, lavanderia, jardim).\n"
          + "SAÍDA:\n- Máx. 5 sugestões; se consumo estiver ótimo, retorne menos, com baixa prioridade e foco em reuso.";

        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank())
            throw new ValidationException("apiKey", "GEMINI_API_KEY não configurada");

        try {
            String response = geminiClient.generateJson(apiKey, systemPrompt, entrada);
            Usuario usuario = usuarioRepository.findById(usuarioId);

            // Normaliza formato: mantém apenas campos necessários em cada item e mapeia para entidade
            SugestaoIa entidade = parseSugestaoIa(response, usuario, medidor);
            sugestaoIaRepository.persist(entidade);

            List<SugestaoIaItemResponseDTO> itens = entidade.getItens().stream()
                .map(i -> new SugestaoIaItemResponseDTO(i.getTitulo(), i.getDescricao(), i.getEconomiaEstimadaReais()))
                .toList();

            return new SugestaoIaResponseDTO(medidor.getId(), entidade.getObservacoes(), entidade.getDataHora(), itens);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao chamar Gemini", e);
        }
    }

    private String inferTipo(Medidor m) {
        String loc = m.getLocalizacao() != null ? m.getLocalizacao().toLowerCase() : "";
        if (loc.contains("cozinha")) return "cozinha";
        if (loc.contains("lavander")) return "lavanderia";
        if (loc.contains("jardim") || loc.contains("externa")) return "jardim";
        return "geral";
    }

    @Override
    @Transactional
    public List<SugestaoResponseDTO> gerarSugestoesIA(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null)
            throw new ValidationException("usuarioId", "Usuário não encontrado.");

        List<Medidor> medidores = medidorRepository.findByUsuarioId(usuarioId);
        List<SugestaoResponseDTO> sugestoes = new ArrayList<>();

        for (Medidor medidor : medidores) {
            EstatisticaResponseDTO stats = estatisticaService.calcularEstatisticas(medidor.getId());

            List<String> mensagens = analisarEGerarSugestoes(
                stats,
                medidor.getLocalizacao()
            );

            for (String mensagem : mensagens) {
                Sugestao sugestao = new Sugestao();
                sugestao.setUsuario(usuario);
                sugestao.setMensagem(mensagem);
                sugestao.setDataHora(LocalDateTime.now());

                sugestaoRepository.persist(sugestao);
                sugestoes.add(SugestaoResponseDTO.valueOf(sugestao));
            }
        }

        return sugestoes;
    }

    private List<String> analisarEGerarSugestoes(
            EstatisticaResponseDTO stats,
            String tipoLocalizacao) {

        List<String> sugestoes = new ArrayList<>();
        return sugestoes;
    }

    private SugestaoIa parseSugestaoIa(String json, Usuario usuario, Medidor medidor) {
        try {
            JsonNode root = MAPPER.readTree(json);
            // Se vier envelope "candidates" (falha no pretty extraction), tenta extrair texto
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
            if (root.has("observacoes")) s.setObservacoes(root.get("observacoes").asText());

            // itens: pode vir em "sugestoes" ou "sugestoesEconomia"
            JsonNode arr = root.has("sugestoes") ? root.get("sugestoes") : root.get("sugestoesEconomia");
            if (arr != null && arr.isArray()) {
                for (JsonNode n : arr) {
                    SugestaoIaItem item = new SugestaoIaItem();
                    if (n.has("titulo")) item.setTitulo(n.get("titulo").asText());
                    if (n.has("descricao")) item.setDescricao(n.get("descricao").asText());
                    if (n.has("economiaEstimadaReais") && n.get("economiaEstimadaReais").isNumber()) {
                        item.setEconomiaEstimadaReais(new BigDecimal(n.get("economiaEstimadaReais").asText()));
                    }
                    s.addItem(item);
                }
            }
            return s;
        } catch (Exception e) {
            // Fallback: salva bruto no modelo antigo para não perder
            Sugestao legado = new Sugestao();
            legado.setUsuario(usuario);
            legado.setMensagem(json);
            legado.setDataHora(LocalDateTime.now());
            sugestaoRepository.persist(legado);

            SugestaoIa s = new SugestaoIa();
            s.setUsuario(usuario);
            s.setMedidor(medidor);
            s.setDataHora(LocalDateTime.now());
            s.setObservacoes("Não foi possível estruturar a resposta da IA");
            return s;
        }
    }
}
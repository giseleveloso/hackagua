package br.unitins.topicos1.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.dto.SugestaoDTO;
import br.unitins.topicos1.dto.SugestaoResponseDTO;
import br.unitins.topicos1.model.Medidor;
import br.unitins.topicos1.model.Sugestao;
import br.unitins.topicos1.model.Usuario;
import br.unitins.topicos1.repository.MedidorRepository;
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
    public List<SugestaoResponseDTO> gerarSugestoesIA(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null)
            throw new ValidationException("usuarioId", "Usuário não encontrado.");

        List<Medidor> medidores = medidorRepository.findByUsuarioId(usuarioId);
        List<SugestaoResponseDTO> sugestoes = new ArrayList<>();

        for (Medidor medidor : medidores) {
            EstatisticaResponseDTO stats = estatisticaService.calcularEstatisticas(medidor.getId());
            
            // Gerar sugestões baseadas em estatísticas
            List<String> mensagens = analisarEGerarSugestoes(stats);
            
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

    private List<String> analisarEGerarSugestoes(EstatisticaResponseDTO stats) {
        List<String> sugestoes = new ArrayList<>();

        // Análise de consumo diário alto
        if (stats.consumoDiario().doubleValue() > 300) {
            sugestoes.add("💧 Seu consumo diário de " + stats.consumoDiario() + "L está acima da média. " +
                         "Considere reduzir o tempo no chuveiro em 2-3 minutos (economia de até 40L/dia).");
        }

        // Análise de vazão alta
        if (stats.vazaoMedia().doubleValue() > 15) {
            sugestoes.add("⚠️ Vazão média alta detectada (" + stats.vazaoMedia() + " L/min). " +
                         "Verifique possíveis vazamentos em torneiras e descargas.");
        }

        // Análise de consumo mensal
        if (stats.consumoMensal().doubleValue() > 10000) {
            sugestoes.add("📊 Consumo mensal elevado: " + stats.consumoMensal() + "L. " +
                         "Sugestões: instale arejadores nas torneiras (economia de 30-50%), " +
                         "use balde para lavar o carro, e aproveite água da chuva.");
        }

        // Sugestão de economia de água
        if (stats.totalLeituras() > 100) {
            sugestoes.add("🌱 Dica sustentável: Reutilize a água da máquina de lavar para limpeza de áreas externas. " +
                         "Economia estimada: 100L por lavagem.");
        }

        // Caso de uso eficiente
        if (stats.consumoDiario().doubleValue() < 150 && stats.consumoMensal().doubleValue() < 4000) {
            sugestoes.add("✅ Parabéns! Seu consumo está abaixo da média nacional. " +
                         "Continue com esses hábitos conscientes de uso da água.");
        }

        return sugestoes;
    }
}


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
            
            // Gerar sugestões baseadas em estatísticas, tipo de uso e localização
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

        if (stats.vazaoMedia().doubleValue() > 20) {
            sugestoes.add("⚠️ Vazão média muito alta (" + stats.vazaoMedia() + " L/min) detectada no + Verifique possíveis vazamentos imediatamente!");
        }

        return sugestoes;
    }
}


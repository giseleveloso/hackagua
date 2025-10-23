package br.unitins.topicos1.service;

import java.util.List;

import br.unitins.topicos1.dto.SugestaoDTO;
import br.unitins.topicos1.dto.SugestaoResponseDTO;
import jakarta.validation.Valid;

public interface SugestaoService {
    SugestaoResponseDTO create(@Valid SugestaoDTO dto);
    List<SugestaoResponseDTO> findByUsuarioId(Long usuarioId);
    List<SugestaoResponseDTO> gerarSugestoesIA(Long usuarioId);
    String gerarSugestoesIAParaMedidor(Long medidorId, String dataInicio, String dataFim);
}

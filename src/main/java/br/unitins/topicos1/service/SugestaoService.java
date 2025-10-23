package br.unitins.topicos1.service;

import br.unitins.topicos1.dto.SugestaoIaResponseDTO;

public interface SugestaoService {
    SugestaoIaResponseDTO gerarSugestoesIAParaMedidor(Long medidorId, String dataInicio, String dataFim);
}

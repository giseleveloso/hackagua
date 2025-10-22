package br.unitins.topicos1.service;

import br.unitins.topicos1.dto.EstatisticaResponseDTO;

public interface EstatisticaService {
    EstatisticaResponseDTO calcularEstatisticas(Long medidorId);
}
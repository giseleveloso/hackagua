package br.unitins.topicos1.service;

import br.unitins.topicos1.dto.GraficoResponseDTO;

public interface GraficoService {
    GraficoResponseDTO graficoMedidor(Long medidorId, String metric, String dataInicio, String dataFim);
    GraficoResponseDTO graficoUsuario(Long usuarioId, String metric, String dataInicio, String dataFim);
}



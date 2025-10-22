package br.unitins.topicos1.service;

import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.dto.UsuarioEstatisticaResponseDTO;

public interface EstatisticaService {
    EstatisticaResponseDTO calcularEstatisticas(Long medidorId);
    UsuarioEstatisticaResponseDTO calcularEstatisticasUsuario(Long usuarioId);
}
package br.unitins.topicos1.dto;

import java.math.BigDecimal;

public record UsuarioEstatisticaResponseDTO(
    Long usuarioId,
    BigDecimal litrosAcumulado,
    BigDecimal gastoMes,
    BigDecimal economiaMes
) {}



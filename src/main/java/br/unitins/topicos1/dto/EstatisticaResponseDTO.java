package br.unitins.topicos1.dto;

import java.math.BigDecimal;

public record EstatisticaResponseDTO(
    Long medidorId,
    String medidorNome,
    BigDecimal consumoDiario,
    BigDecimal consumoSemanal,
    BigDecimal consumoMensal,
    BigDecimal vazaoMedia,
    BigDecimal vazaoMaxima,
    Integer totalLeituras
) {}

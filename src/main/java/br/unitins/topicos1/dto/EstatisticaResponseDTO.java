package br.unitins.topicos1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EstatisticaResponseDTO(
    Long medidorId,
    String medidorNome,
    LocalDate dataInicio,
    LocalDate dataFim,
    BigDecimal totalLitros,
    BigDecimal totalM3,
    BigDecimal custoEstimado,
    BigDecimal vazaoMediaLMin,
    Integer numeroLeituras
) {}

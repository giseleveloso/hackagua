package br.unitins.topicos1.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GraficoConsumoDTO(
    LocalDateTime periodo,  
    BigDecimal totalLitros,
    BigDecimal vazaoMedia,
    BigDecimal custoEstimado
) {}

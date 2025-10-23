package br.unitins.topicos1.dto;

import java.math.BigDecimal;
import java.util.List;

public record GraficoResponseDTO(
    List<String> intervalos,
    List<BigDecimal> valores,
    String metric,
    String unidade,
    String granularidade
) {}



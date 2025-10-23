package br.unitins.topicos1.dto;

import java.math.BigDecimal;

public record SugestaoIaItemResponseDTO(
    String titulo,
    String descricao,
    BigDecimal economiaEstimadaReais
) {}



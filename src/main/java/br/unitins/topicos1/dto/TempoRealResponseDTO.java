package br.unitins.topicos1.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TempoRealResponseDTO(
    Long medidorId,
    String medidorNome,
    BigDecimal consumoLitros,
    BigDecimal vazaoLMin,
    LocalDateTime dataHora,
    boolean ligado
) {}



package br.unitins.topicos1.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LeituraDTO(
    @NotNull(message = "O ID do medidor é obrigatório")
    Long medidorId,
    
    @NotNull(message = "A vazão é obrigatória")
    @Positive(message = "A vazão deve ser positiva")
    BigDecimal vazao,
    
    @NotNull(message = "O consumo total é obrigatório")
    @Positive(message = "O consumo total deve ser positivo")
    BigDecimal consumoTotal
) {}

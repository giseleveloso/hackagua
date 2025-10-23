package br.unitins.topicos1.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LeituraDTO(
    @NotNull(message = "O ID do medidor é obrigatório")
    Long medidorId,
    
    @NotNull(message = "Litros consumidos é obrigatório")
    @Positive(message = "Litros deve ser positivo")
    BigDecimal litros,

    @Positive(message = "Vazão deve ser positiva")
    BigDecimal vazaoLMin
) {}

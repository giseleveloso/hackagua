package br.unitins.topicos1.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.unitins.topicos1.model.Leitura;

public record LeituraResponseDTO(
    Long id,
    Long medidorId,
    String medidorNome,
    BigDecimal litros,
    BigDecimal litrosAcumulado,
    BigDecimal vazaoLMin,
    LocalDateTime dataHora
) {
    public static LeituraResponseDTO valueOf(Leitura leitura) {
        BigDecimal vazao = leitura.getVazaoLMin() != null
                ? leitura.getVazaoLMin()
                : leitura.getLitros().divide(BigDecimal.valueOf(10.0 / 60.0), 3, java.math.RoundingMode.HALF_UP);
        
        return new LeituraResponseDTO(
            leitura.getId(),
            leitura.getMedidor().getId(),
            leitura.getMedidor().getNome(),
            leitura.getLitros(),
            leitura.getLitrosAcumulado(),
            vazao,
            leitura.getDataHora()
        );
    }
}


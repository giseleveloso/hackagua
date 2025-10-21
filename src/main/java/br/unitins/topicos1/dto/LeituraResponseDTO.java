package br.unitins.topicos1.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.unitins.topicos1.model.Leitura;

public record LeituraResponseDTO(
    Long id,
    Long medidorId,
    String medidorNome,
    BigDecimal vazao,
    BigDecimal consumoTotal,
    LocalDateTime dataHora
) {
    public static LeituraResponseDTO valueOf(Leitura leitura) {
        return new LeituraResponseDTO(
            leitura.getId(),
            leitura.getMedidor().getId(),
            leitura.getMedidor().getNome(),
            leitura.getVazao(),
            leitura.getConsumoTotal(),
            leitura.getDataHora()
        );
    }
}

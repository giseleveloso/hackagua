package br.unitins.topicos1.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SugestaoIaResponseDTO(
    Long medidorId,
    String observacoes,
    LocalDateTime dataHora,
    List<SugestaoIaItemResponseDTO> sugestoes
) {}



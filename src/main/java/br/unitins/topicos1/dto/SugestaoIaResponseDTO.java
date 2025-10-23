package br.unitins.topicos1.dto;

import java.util.List;

public record SugestaoIaResponseDTO(
    Long medidorId,
    String observacoes,
    List<SugestaoIaItemResponseDTO> sugestoes
) {}



package br.unitins.topicos1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SugestaoDTO(
    @NotNull(message = "O ID do usuário é obrigatório")
    Long usuarioId,
    
    @NotBlank(message = "A mensagem não pode ser nula ou vazia")
    String mensagem
) {}

package br.unitins.topicos1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MedidorDTO(
    @NotBlank(message = "O nome não pode ser nulo ou vazio")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    String nome,
    
    @NotBlank(message = "A localização não pode ser nula ou vazia")
    @Size(max = 150, message = "A localização deve ter no máximo 150 caracteres")
    String localizacao,
    
    @NotNull(message = "O ID do usuário é obrigatório")
    Long usuarioId,

    Double limite,

    boolean ligado,

    boolean interromper
) {}

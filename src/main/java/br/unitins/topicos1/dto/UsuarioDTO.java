package br.unitins.topicos1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioDTO(
    @NotBlank(message = "O nome não pode ser nulo ou vazio")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    String nome,
    
    @NotBlank(message = "O email não pode ser nulo ou vazio")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "A senha não pode ser nula ou vazia")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    String senha,
    
    @NotNull(message = "O tipo de uso é obrigatório")
    Integer tipoUso
) {}

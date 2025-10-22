package br.unitins.topicos1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
    @NotBlank(message = "O email não pode ser nulo ou vazio")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "A senha não pode ser nula ou vazia")
    String senha
) {}

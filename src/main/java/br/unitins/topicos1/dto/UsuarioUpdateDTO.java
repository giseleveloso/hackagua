package br.unitins.topicos1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
                @NotBlank(message = "O nome não pode ser nulo ou vazio") @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres") String nome,

                @NotBlank(message = "O email não pode ser nulo ou vazio") @Email(message = "Email inválido") String email,

                String senha,

                Double valorM) {
}

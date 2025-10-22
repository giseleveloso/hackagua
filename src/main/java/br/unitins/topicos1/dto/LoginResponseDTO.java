package br.unitins.topicos1.dto;

public record LoginResponseDTO(
    String token,
    UsuarioResponseDTO usuario
) {}

package br.unitins.topicos1.dto;

import br.unitins.topicos1.model.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    Integer tipoUso,
    String tipoUsoDescricao
) {
    public static UsuarioResponseDTO valueOf(Usuario usuario) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getTipoUso() != null ? usuario.getTipoUso().getId() : null,
            usuario.getTipoUso() != null ? usuario.getTipoUso().getDescricao() : null
        );
    }
}

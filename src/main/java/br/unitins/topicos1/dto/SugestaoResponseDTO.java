package br.unitins.topicos1.dto;

import java.time.LocalDateTime;

import br.unitins.topicos1.model.Sugestao;

public record SugestaoResponseDTO(
    Long id,
    Long usuarioId,
    String mensagem,
    LocalDateTime dataHora
) {
    public static SugestaoResponseDTO valueOf(Sugestao sugestao) {
        return new SugestaoResponseDTO(
            sugestao.getId(),
            sugestao.getUsuario().getId(),
            sugestao.getMensagem(),
            sugestao.getDataHora()
        );
    }
}

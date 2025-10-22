package br.unitins.topicos1.dto;

import br.unitins.topicos1.model.Medidor;

public record MedidorResponseDTO(
        Long id,
        String nome,
        String localizacao,
        Double limite,
        Long usuarioId,
        String usuarioNome) {
    public static MedidorResponseDTO valueOf(Medidor medidor) {
        return new MedidorResponseDTO(
                medidor.getId(),
                medidor.getNome(),
                medidor.getLocalizacao(),
                medidor.getLimite(),
                medidor.getUsuario().getId(),
                medidor.getUsuario().getNome());
    }
}

package br.unitins.topicos1.service;

import java.util.List;

import br.unitins.topicos1.dto.UsuarioDTO;
import br.unitins.topicos1.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;

public interface UsuarioService {
    UsuarioResponseDTO create(@Valid UsuarioDTO dto);
    void update(Long id, UsuarioDTO dto);
    void delete(Long id);
    UsuarioResponseDTO findById(Long id);
    List<UsuarioResponseDTO> findAll();
    UsuarioResponseDTO findByEmail(String email);
}

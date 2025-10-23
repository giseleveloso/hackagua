package br.unitins.topicos1.service;

import java.util.List;

import br.unitins.topicos1.dto.UsuarioDTO;
import br.unitins.topicos1.dto.UsuarioResponseDTO;
import br.unitins.topicos1.dto.UsuarioUpdateDTO;
import jakarta.validation.Valid;

public interface UsuarioService {
    UsuarioResponseDTO create(@Valid UsuarioDTO dto);
    UsuarioResponseDTO update(Long id, UsuarioUpdateDTO dto);
    void delete(Long id);
    UsuarioResponseDTO findById(Long id);
    List<UsuarioResponseDTO> findAll();
    UsuarioResponseDTO findByEmail(String email);
}

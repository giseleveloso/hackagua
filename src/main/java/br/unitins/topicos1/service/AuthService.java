package br.unitins.topicos1.service;

import br.unitins.topicos1.dto.LoginDTO;
import br.unitins.topicos1.dto.LoginResponseDTO;
import br.unitins.topicos1.dto.UsuarioDTO;
import br.unitins.topicos1.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;

public interface AuthService {
    LoginResponseDTO login(@Valid LoginDTO dto);
    UsuarioResponseDTO registro(@Valid UsuarioDTO dto);
}

package br.unitins.topicos1.service;

import br.unitins.topicos1.dto.LoginDTO;
import br.unitins.topicos1.dto.LoginResponseDTO;
import br.unitins.topicos1.dto.UsuarioDTO;
import br.unitins.topicos1.dto.UsuarioResponseDTO;
import br.unitins.topicos1.model.Usuario;
import br.unitins.topicos1.repository.UsuarioRepository;
import br.unitins.topicos1.util.HashUtil;
import br.unitins.topicos1.util.JwtUtil;
import br.unitins.topicos1.validation.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    JwtUtil jwtUtil;

    @Override
    public LoginResponseDTO login(@Valid LoginDTO dto) {
        Usuario usuario = usuarioRepository.findByEmailCompleto(dto.email());

        if (usuario == null || !HashUtil.verificarHash(dto.senha(), usuario.getSenha())) {
            throw new ValidationException("login", "Email ou senha inválidos");
        }

        UsuarioResponseDTO usuarioResponse = UsuarioResponseDTO.valueOf(usuario);
        String token = jwtUtil.generateJwt(usuarioResponse);

        return new LoginResponseDTO(token, usuarioResponse);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO registro(@Valid UsuarioDTO dto) {
        if (usuarioRepository.findByEmailCompleto(dto.email()) != null) {
            throw new ValidationException("email", "Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(HashUtil.hash(dto.senha()));
        usuario.setValorM(dto.valorM());

        usuarioRepository.persist(usuario);
        return UsuarioResponseDTO.valueOf(usuario);
    }
}

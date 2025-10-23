package br.unitins.topicos1.service;

import java.util.List;

import br.unitins.topicos1.dto.UsuarioDTO;
import br.unitins.topicos1.dto.UsuarioResponseDTO;
import br.unitins.topicos1.dto.UsuarioUpdateDTO;
import br.unitins.topicos1.model.Usuario;
import br.unitins.topicos1.repository.UsuarioRepository;
import br.unitins.topicos1.util.HashUtil;
import br.unitins.topicos1.validation.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {

    @Inject
    public UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UsuarioResponseDTO create(@Valid UsuarioDTO dto) {
        validarEmailUnico(dto.email());

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(HashUtil.hash(dto.senha()));
        usuario.setValorM(dto.valorM());

        usuarioRepository.persist(usuario);
        return UsuarioResponseDTO.valueOf(usuario);
    }

    public void validarEmailUnico(String email) {
        Usuario usuario = usuarioRepository.findByEmailCompleto(email);
        if (usuario != null)
            throw new ValidationException("email", "O email '" + email + "' já está em uso.");
    }

    @Override
    @Transactional
    public UsuarioResponseDTO update(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id);

        if (usuario == null)
            throw new ValidationException("id", "Usuário não encontrado.");

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setValorM(dto.valorM());

        if (dto.senha() != null && !dto.senha().isEmpty()) {
            usuario.setSenha(HashUtil.hash(dto.senha()));
        }
        return UsuarioResponseDTO.valueOf(usuario);

    }

    @Override
    @Transactional
    public void delete(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public UsuarioResponseDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id);
        if (usuario == null)
            throw new ValidationException("id", "Usuário não encontrado.");
        return UsuarioResponseDTO.valueOf(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.listAll()
                .stream()
                .map(UsuarioResponseDTO::valueOf)
                .toList();
    }

    @Override
    public UsuarioResponseDTO findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null)
            throw new ValidationException("email", "Usuário não encontrado.");
        return UsuarioResponseDTO.valueOf(usuario);
    }
}

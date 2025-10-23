package br.unitins.topicos1.service;

import java.util.List;

import org.jboss.logging.Logger;

import br.unitins.topicos1.dto.MedidorDTO;
import br.unitins.topicos1.dto.MedidorResponseDTO;
import br.unitins.topicos1.model.Medidor;
import br.unitins.topicos1.model.Usuario;
import br.unitins.topicos1.repository.MedidorRepository;
import br.unitins.topicos1.repository.UsuarioRepository;
import br.unitins.topicos1.validation.ValidationException;
import br.unitins.topicos1.resource.ws.SensorSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class MedidorServiceImpl implements MedidorService {

    private static final Logger LOG = Logger.getLogger(MedidorServiceImpl.class);

    @Inject
    public MedidorRepository medidorRepository;

    @Inject
    public UsuarioRepository usuarioRepository;

    @Inject
    public SensorSocket sensorSocket;

    @Override
    @Transactional
    public MedidorResponseDTO create(@Valid MedidorDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId());
        if (usuario == null)
            throw new ValidationException("usuarioId", "Usuário não encontrado.");

        Medidor medidor = new Medidor();
        medidor.setNome(dto.nome());
        medidor.setLocalizacao(dto.localizacao());
        medidor.setUsuario(usuario);
        medidor.setLimite(dto.limite());
        medidor.setLigado(dto.ligado());
        medidor.setInterromper(dto.interromper());

        medidorRepository.persist(medidor);
        return MedidorResponseDTO.valueOf(medidor);
    }

    @Override
    @Transactional
    public MedidorResponseDTO update(Long id, MedidorDTO dto) {
        Medidor medidor = medidorRepository.findById(id);

        if (medidor == null)
            throw new ValidationException("id", "Medidor não encontrado.");

        Usuario usuario = usuarioRepository.findById(dto.usuarioId());
        if (usuario == null)
            throw new ValidationException("usuarioId", "Usuário não encontrado.");

        medidor.setNome(dto.nome());
        medidor.setLocalizacao(dto.localizacao());
        medidor.setUsuario(usuario);
        medidor.setLimite(dto.limite());
        medidor.setLigado(dto.ligado());
        medidor.setInterromper(dto.interromper());
        return MedidorResponseDTO.valueOf(medidor);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        medidorRepository.deleteById(id);
    }

    @Override
    public MedidorResponseDTO findById(Long id) {
        Medidor medidor = medidorRepository.findById(id);
        if (medidor == null)
            throw new ValidationException("id", "Medidor não encontrado.");
        return MedidorResponseDTO.valueOf(medidor);
    }

    @Override
    public List<MedidorResponseDTO> findAll() {
        return medidorRepository.listAll()
                .stream()
                .map(MedidorResponseDTO::valueOf)
                .toList();
    }

    @Override
    public List<MedidorResponseDTO> findByUsuarioId(Long usuarioId) {
        return medidorRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(MedidorResponseDTO::valueOf)
                .toList();
    }

    @Override
    public List<MedidorResponseDTO> findByNome(String nome) {
        return medidorRepository.findByNome(nome)
                .stream()
                .map(MedidorResponseDTO::valueOf)
                .toList();
    }

    @Override
    @Transactional
    public MedidorResponseDTO setPower(Long id, boolean ligado) {
        Medidor medidor = medidorRepository.findById(id);
        if (medidor == null)
            throw new ValidationException("id", "Medidor não encontrado.");

        String uuid = String.valueOf(id);
        try {
            boolean sucesso = sensorSocket.powerUpdate(ligado, uuid);
            if (sucesso) {
                medidor.setLigado(ligado);
            } else {
                throw new Exception("Não foi possível desligar o medidor");
            }
        } catch (Exception e) {
            // apenas logar; manter estado no banco
        }
        return MedidorResponseDTO.valueOf(medidor);
    }

    @Override
    @Transactional
    public MedidorResponseDTO togglePower(Long id) {
        Medidor medidor = medidorRepository.findById(id);
        if (medidor == null)
            throw new ValidationException("id", "Medidor não encontrado.");

        boolean novoEstado = !medidor.getLigado();

        String uuid = String.valueOf(id);
        try {
            boolean sucesso = sensorSocket.powerUpdate(novoEstado, uuid);
            if (sucesso) {
                medidor.setLigado(novoEstado);
            } else {
                throw new Exception("Não foi possível desligar o medidor");
            }
        } catch (Exception e) {
            // apenas logar; manter estado no banco
        }
        return MedidorResponseDTO.valueOf(medidor);
    }

    @Override
    @Transactional
    public void updatePowerStatus(Long id, boolean ligado) {
        Medidor medidor = medidorRepository.findById(id);
        if (medidor != null) {
            medidor.setLigado(ligado);
            LOG.infof("Status do medidor atualizado no banco - ID: %d, Ligado: %s", id, ligado);
        }
    }
}

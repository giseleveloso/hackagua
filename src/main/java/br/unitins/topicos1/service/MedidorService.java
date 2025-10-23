package br.unitins.topicos1.service;

import java.util.List;

import br.unitins.topicos1.dto.MedidorDTO;
import br.unitins.topicos1.dto.MedidorResponseDTO;
import jakarta.validation.Valid;

public interface MedidorService {
    MedidorResponseDTO create(@Valid MedidorDTO dto);
    MedidorResponseDTO update(Long id, MedidorDTO dto);
    void delete(Long id);
    MedidorResponseDTO findById(Long id);
    List<MedidorResponseDTO> findAll();
    List<MedidorResponseDTO> findByUsuarioId(Long usuarioId);
    List<MedidorResponseDTO> findByNome(String nome);
    MedidorResponseDTO setPower(Long id, boolean ligado);
    MedidorResponseDTO togglePower(Long id);
    void updatePowerStatus(Long id, boolean ligado);
}

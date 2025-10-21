package br.unitins.topicos1.service;

import java.time.LocalDateTime;
import java.util.List;

import br.unitins.topicos1.dto.LeituraDTO;
import br.unitins.topicos1.dto.LeituraResponseDTO;
import jakarta.validation.Valid;

public interface LeituraService {
    LeituraResponseDTO create(@Valid LeituraDTO dto);
    LeituraResponseDTO findById(Long id);
    List<LeituraResponseDTO> findByMedidorId(Long medidorId);
    List<LeituraResponseDTO> findByMedidorIdAndPeriodo(Long medidorId, LocalDateTime inicio, LocalDateTime fim);
    List<LeituraResponseDTO> findLeiturasRecentes(Long medidorId, int limit);
}

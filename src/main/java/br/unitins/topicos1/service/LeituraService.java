package br.unitins.topicos1.service;

import java.time.LocalDate;
import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.dto.LeituraDTO;
import br.unitins.topicos1.dto.LeituraResponseDTO;
import jakarta.validation.Valid;

public interface LeituraService {
    LeituraResponseDTO registrarLeitura(@Valid LeituraDTO dto);
    EstatisticaResponseDTO calcularEstatisticas(Long medidorId, LocalDate dataInicio, LocalDate dataFim);
}
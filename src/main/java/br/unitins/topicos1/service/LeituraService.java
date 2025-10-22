package br.unitins.topicos1.service;

import java.time.LocalDate;
import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.dto.LeituraDTO;
import br.unitins.topicos1.dto.LeituraResponseDTO;
import br.unitins.topicos1.dto.TempoRealResponseDTO;
import jakarta.validation.Valid;
import java.util.List;

public interface LeituraService {
    LeituraResponseDTO registrarLeitura(@Valid LeituraDTO dto);
    EstatisticaResponseDTO calcularEstatisticas(Long medidorId, LocalDate dataInicio, LocalDate dataFim);
    TempoRealResponseDTO obterTempoReal(Long medidorId);
    List<TempoRealResponseDTO> obterTempoRealPorUsuario(Long usuarioId);
}
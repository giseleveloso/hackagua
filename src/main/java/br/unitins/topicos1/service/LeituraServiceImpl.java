package br.unitins.topicos1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.dto.LeituraDTO;
import br.unitins.topicos1.dto.LeituraResponseDTO;
import br.unitins.topicos1.model.Leitura;
import br.unitins.topicos1.model.Medidor;
import br.unitins.topicos1.repository.LeituraRepository;
import br.unitins.topicos1.repository.MedidorRepository;
import br.unitins.topicos1.validation.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class LeituraServiceImpl implements LeituraService {

    @Inject
    LeituraRepository leituraRepository;

    @Inject
    MedidorRepository medidorRepository;

    @Override
    @Transactional
    public LeituraResponseDTO registrarLeitura(@Valid LeituraDTO dto) {
        Medidor medidor = medidorRepository.findById(dto.medidorId());
        if (medidor == null)
            throw new ValidationException("medidorId", "Medidor não encontrado");

        // Buscar última leitura para calcular acumulado
        Leitura ultimaLeitura = leituraRepository.findUltimaLeitura(dto.medidorId());
        BigDecimal litrosAcumulado = ultimaLeitura != null 
            ? ultimaLeitura.getLitrosAcumulado().add(dto.litros())
            : dto.litros();

        Leitura leitura = new Leitura();
        leitura.setMedidor(medidor);
        leitura.setLitros(dto.litros());
        leitura.setLitrosAcumulado(litrosAcumulado);
        leitura.setDataHora(LocalDateTime.now());

        leituraRepository.persist(leitura);
        return LeituraResponseDTO.valueOf(leitura);
    }

    @Override
    public EstatisticaResponseDTO calcularEstatisticas(Long medidorId, LocalDate dataInicio, LocalDate dataFim) {
        Medidor medidor = medidorRepository.findById(medidorId);
        if (medidor == null)
            throw new ValidationException("medidorId", "Medidor não encontrado");

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.plusDays(1).atStartOfDay();

        List<Leitura> leituras = leituraRepository.findByMedidorIdAndPeriodo(medidorId, inicio, fim);

        if (leituras.isEmpty()) {
            return new EstatisticaResponseDTO(
                medidorId, medidor.getNome(), dataInicio, dataFim,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, 0
            );
        }

        // Somar litros
        BigDecimal totalLitros = leituras.stream()
                .map(Leitura::getLitros)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Converter para m³
        BigDecimal totalM3 = totalLitros.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        // Calcular vazão média (litros / (10 segundos / 60))
        BigDecimal vazaoMedia = leituras.stream()
                .map(l -> l.getLitros().divide(BigDecimal.valueOf(10.0 / 60.0), 3, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(leituras.size()), 3, RoundingMode.HALF_UP);

        // Calcular custo
        BigDecimal valorM3 = medidor.getUsuario().getValorM() != null 
            ? BigDecimal.valueOf(medidor.getUsuario().getValorM())
            : BigDecimal.ZERO;
        BigDecimal custoEstimado = totalM3.multiply(valorM3).setScale(2, RoundingMode.HALF_UP);

        return new EstatisticaResponseDTO(
            medidorId,
            medidor.getNome(),
            dataInicio,
            dataFim,
            totalLitros,
            totalM3,
            custoEstimado,
            vazaoMedia,
            leituras.size()
        );
    }
}

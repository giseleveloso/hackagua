package br.unitins.topicos1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.model.Leitura;
import br.unitins.topicos1.model.Medidor;
import br.unitins.topicos1.repository.LeituraRepository;
import br.unitins.topicos1.repository.MedidorRepository;
import br.unitins.topicos1.validation.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EstatisticaServiceImpl implements EstatisticaService {

    @Inject
    LeituraRepository leituraRepository;

    @Inject
    MedidorRepository medidorRepository;

    @Override
    @Transactional
    public EstatisticaResponseDTO calcularEstatisticas(Long medidorId) {
        Medidor medidor = medidorRepository.findById(medidorId);
        if (medidor == null) {
            throw new ValidationException("medidorId", "Medidor não encontrado.");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioMes = agora.minusDays(30);

        List<Leitura> leiturasMes = leituraRepository.findByMedidorIdAndPeriodo(medidorId, inicioMes, agora);

        BigDecimal totalLitros = calcularConsumoTotal(leiturasMes);
        BigDecimal totalM3 = totalLitros.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        BigDecimal custoEstimado = calcularCusto(totalM3, medidor);
        BigDecimal vazaoMedia = calcularVazaoMedia(leiturasMes);

        LocalDate dataInicio = inicioMes.toLocalDate();
        LocalDate dataFim = agora.toLocalDate();

        return new EstatisticaResponseDTO(
                medidorId,
                medidor.getNome(),
                dataInicio,
                dataFim,
                totalLitros,
                totalM3,
                custoEstimado,
                vazaoMedia,
                leiturasMes.size());
    }

    private BigDecimal calcularConsumoTotal(List<Leitura> leituras) {
        if (leituras.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Soma todos os litros das leituras
        return leituras.stream()
                .map(Leitura::getLitros)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularCusto(BigDecimal totalM3, Medidor medidor) {
        if (medidor.getUsuario() == null || medidor.getUsuario().getValorM() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal valorPorM3 = BigDecimal.valueOf(medidor.getUsuario().getValorM());
        return totalM3.multiply(valorPorM3).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularVazaoMedia(List<Leitura> leituras) {
        if (leituras.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Calcula a vazão média em litros por minuto
        // Assumindo que cada leitura tem um intervalo de tempo
        BigDecimal somaLitros = leituras.stream()
                .map(Leitura::getLitros)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcula o tempo total em minutos entre primeira e última leitura
        if (leituras.size() < 2) {
            return BigDecimal.ZERO;
        }

        LocalDateTime primeiraData = leituras.get(0).getDataHora();
        LocalDateTime ultimaData = leituras.get(leituras.size() - 1).getDataHora();

        long minutos = java.time.Duration.between(primeiraData, ultimaData).toMinutes();

        if (minutos == 0) {
            return BigDecimal.ZERO;
        }

        return somaLitros.divide(BigDecimal.valueOf(minutos), 2, RoundingMode.HALF_UP);
    }
}
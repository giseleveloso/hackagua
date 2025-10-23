package br.unitins.topicos1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.unitins.topicos1.dto.EstatisticaResponseDTO;
import br.unitins.topicos1.dto.UsuarioEstatisticaResponseDTO;
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

        BigDecimal somaLitros = leituras.stream()
                .map(Leitura::getLitros)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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

    @Override
    public UsuarioEstatisticaResponseDTO calcularEstatisticasUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new ValidationException("usuarioId", "Usuário inválido.");
        }

        List<Medidor> medidores = medidorRepository.findByUsuarioId(usuarioId);
        if (medidores.isEmpty()) {
            return new UsuarioEstatisticaResponseDTO(
                    usuarioId,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO);
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioMes = agora.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime inicioMesAnterior = inicioMes.minusMonths(1);
        LocalDateTime fimMesAnterior = inicioMes.minusSeconds(1);

        BigDecimal litrosMesAtual = BigDecimal.ZERO;
        BigDecimal litrosMesAnterior = BigDecimal.ZERO;
        BigDecimal litrosAcumulado = BigDecimal.ZERO;
        BigDecimal gastoMesAtual = BigDecimal.ZERO;

        for (Medidor medidor : medidores) {
            Long medidorId = medidor.getId();
            if (medidorId == null)
                continue;

            List<Leitura> leiturasMesAtual = leituraRepository.findByMedidorIdAndPeriodo(medidorId, inicioMes, agora);
            List<Leitura> leiturasMesAnterior = leituraRepository.findByMedidorIdAndPeriodo(medidorId,
                    inicioMesAnterior, fimMesAnterior);

            BigDecimal somaAtual = calcularConsumoTotal(leiturasMesAtual);
            BigDecimal somaAnterior = calcularConsumoTotal(leiturasMesAnterior);

            litrosMesAtual = litrosMesAtual.add(somaAtual);
            litrosMesAnterior = litrosMesAnterior.add(somaAnterior);

            Leitura ultima = leituraRepository.findUltimaLeitura(medidorId);
            if (ultima != null && ultima.getLitrosAcumulado() != null) {
                litrosAcumulado = litrosAcumulado.add(ultima.getLitrosAcumulado());
            }

            BigDecimal valorM3 = (medidor.getUsuario() != null && medidor.getUsuario().getValorM() != null)
                    ? BigDecimal.valueOf(medidor.getUsuario().getValorM())
                    : BigDecimal.ZERO;
            BigDecimal m3Atual = somaAtual.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            gastoMesAtual = gastoMesAtual.add(m3Atual.multiply(valorM3).setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal m3MesAnterior = litrosMesAnterior.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        BigDecimal economiaMes = BigDecimal.ZERO;
        if (m3MesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorM3Usuario = medidores.get(0).getUsuario() != null
                    && medidores.get(0).getUsuario().getValorM() != null
                            ? BigDecimal.valueOf(medidores.get(0).getUsuario().getValorM())
                            : BigDecimal.ZERO;
            BigDecimal gastoMesAnterior = m3MesAnterior.multiply(valorM3Usuario).setScale(2, RoundingMode.HALF_UP);
            economiaMes = gastoMesAnterior.subtract(gastoMesAtual).setScale(2, RoundingMode.HALF_UP);
        }

        return new UsuarioEstatisticaResponseDTO(
                usuarioId,
                litrosAcumulado.setScale(2, RoundingMode.HALF_UP),
                gastoMesAtual.setScale(2, RoundingMode.HALF_UP),
                economiaMes);
    }
}
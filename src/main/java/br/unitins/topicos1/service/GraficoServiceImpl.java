package br.unitins.topicos1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.unitins.topicos1.dto.GraficoResponseDTO;
import br.unitins.topicos1.model.Leitura;
import br.unitins.topicos1.model.Medidor;
import br.unitins.topicos1.repository.LeituraRepository;
import br.unitins.topicos1.repository.MedidorRepository;
import br.unitins.topicos1.validation.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GraficoServiceImpl implements GraficoService {

    private enum Granularidade { HORA, DIA, CADA3DIAS }

    @Inject
    LeituraRepository leituraRepository;

    @Inject
    MedidorRepository medidorRepository;

    @Override
    public GraficoResponseDTO graficoMedidor(Long medidorId, String metric, String dataInicio, String dataFim) {
        Medidor medidor = medidorRepository.findById(medidorId);
        if (medidor == null) throw new ValidationException("medidorId", "Medidor não encontrado.");
        var intervalo = parseIntervalo(dataInicio, dataFim);
        var granularidade = escolherGranularidade(intervalo.inicio, intervalo.fim);
        var buckets = gerarBuckets(intervalo.inicio, intervalo.fim, granularidade);
        List<String> intervalos = new ArrayList<>();
        List<BigDecimal> valores = new ArrayList<>();

        for (Bucket b : buckets) {
            List<Leitura> leituras = leituraRepository.findByMedidorIdAndPeriodo(medidorId, b.inicio, b.fim);
            BigDecimal v = agregadoBucket(leituras, metric, medidor);
            intervalos.add(formatLabel(b.inicio, granularidade));
            valores.add(v);
        }
        return new GraficoResponseDTO(intervalos, valores, metric, unidade(metric), granularidade.name());
    }

    @Override
    public GraficoResponseDTO graficoUsuario(Long usuarioId, String metric, String dataInicio, String dataFim) {
        var intervalo = parseIntervalo(dataInicio, dataFim);
        var granularidade = escolherGranularidade(intervalo.inicio, intervalo.fim);
        var buckets = gerarBuckets(intervalo.inicio, intervalo.fim, granularidade);

        List<Medidor> medidores = medidorRepository.findByUsuarioId(usuarioId);
        if (medidores.isEmpty()) {
            return new GraficoResponseDTO(List.of(), List.of(), metric, unidade(metric), granularidade.name());
        }

        List<String> intervalos = new ArrayList<>();
        List<BigDecimal> valores = new ArrayList<>();
        for (Bucket b : buckets) {
            BigDecimal soma = BigDecimal.ZERO;
            for (Medidor m : medidores) {
                List<Leitura> leituras = leituraRepository.findByMedidorIdAndPeriodo(m.getId(), b.inicio, b.fim);
                soma = soma.add(agregadoBucket(leituras, metric, m));
            }
            intervalos.add(formatLabel(b.inicio, granularidade));
            valores.add(soma);
        }
        return new GraficoResponseDTO(intervalos, valores, metric, unidade(metric), granularidade.name());
    }

    private String unidade(String metric) {
        return switch (metric) {
            case "consumo" -> "L";
            case "vazao" -> "L/min";
            case "gasto" -> "R$";
            default -> throw new ValidationException("metric", "Métrica inválida");
        };
    }

    private static class Intervalo { LocalDateTime inicio; LocalDateTime fim; }

    private Intervalo parseIntervalo(String dataInicio, String dataFim) {
        LocalDateTime inicio;
        LocalDateTime fim;
        if (dataInicio == null || dataFim == null) {
            LocalDate hoje = LocalDate.now();
            inicio = hoje.atStartOfDay();
            fim = hoje.plusDays(1).atStartOfDay();
        } else {
            if (dataInicio.length() == 10) inicio = LocalDate.parse(dataInicio).atStartOfDay();
            else inicio = LocalDateTime.parse(dataInicio);
            if (dataFim.length() == 10) fim = LocalDate.parse(dataFim).plusDays(1).atStartOfDay();
            else fim = LocalDateTime.parse(dataFim);
        }
        Intervalo i = new Intervalo();
        i.inicio = inicio;
        i.fim = fim;
        return i;
    }

    private Granularidade escolherGranularidade(LocalDateTime inicio, LocalDateTime fim) {
        long horas = Duration.between(inicio, fim).toHours();
        if (horas <= 24) return Granularidade.HORA;
        if (horas <= 24 * 30) return Granularidade.DIA;
        return Granularidade.CADA3DIAS; // até 90 dias
    }

    private static class Bucket { LocalDateTime inicio; LocalDateTime fim; }

    private List<Bucket> gerarBuckets(LocalDateTime inicio, LocalDateTime fim, Granularidade g) {
        List<Bucket> buckets = new ArrayList<>();
        LocalDateTime cursor = inicio;
        while (cursor.isBefore(fim)) {
            Bucket b = new Bucket();
            b.inicio = cursor;
            switch (g) {
                case HORA -> cursor = cursor.plusHours(1);
                case DIA -> cursor = cursor.plusDays(1);
                case CADA3DIAS -> cursor = cursor.plusDays(3);
            }
            b.fim = cursor.isAfter(fim) ? fim : cursor;
            buckets.add(b);
        }
        return buckets;
    }

    private BigDecimal agregadoBucket(List<Leitura> leituras, String metric, Medidor medidor) {
        return switch (metric) {
            case "consumo" -> somaLitros(leituras);
            case "vazao" -> mediaVazao(leituras);
            case "gasto" -> custo(leituras, medidor);
            default -> throw new ValidationException("metric", "Métrica inválida");
        };
    }

    private BigDecimal somaLitros(List<Leitura> leituras) {
        return leituras.stream()
            .map(Leitura::getLitros)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal mediaVazao(List<Leitura> leituras) {
        if (leituras.isEmpty()) return BigDecimal.ZERO;
        List<BigDecimal> valores = leituras.stream()
            .map(l -> l.getVazaoLMin() != null ? l.getVazaoLMin() : l.getLitros().divide(BigDecimal.valueOf(10.0 / 60.0), 3, RoundingMode.HALF_UP))
            .toList();
        BigDecimal soma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return valores.isEmpty() ? BigDecimal.ZERO : soma.divide(BigDecimal.valueOf(valores.size()), 3, RoundingMode.HALF_UP);
    }

    private BigDecimal custo(List<Leitura> leituras, Medidor medidor) {
        BigDecimal litros = somaLitros(leituras);
        BigDecimal m3 = litros.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        BigDecimal valorM3 = medidor.getUsuario() != null && medidor.getUsuario().getValorM() != null
            ? BigDecimal.valueOf(medidor.getUsuario().getValorM())
            : BigDecimal.ZERO;
        return m3.multiply(valorM3).setScale(2, RoundingMode.HALF_UP);
    }

    private String formatLabel(LocalDateTime inicio, Granularidade g) {
        return switch (g) {
            case HORA -> inicio.format(DateTimeFormatter.ofPattern("HH:00"));
            case DIA, CADA3DIAS -> inicio.toLocalDate().toString();
        };
    }
}



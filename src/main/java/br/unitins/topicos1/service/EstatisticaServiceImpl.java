package br.unitins.topicos1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

@ApplicationScoped
public class EstatisticaServiceImpl implements EstatisticaService {

    @Inject
    public LeituraRepository leituraRepository;

    @Inject
    public MedidorRepository medidorRepository;

    @Override
    public EstatisticaResponseDTO calcularEstatisticas(Long medidorId) {
        Medidor medidor = medidorRepository.findById(medidorId);
        if (medidor == null)
            throw new ValidationException("medidorId", "Medidor não encontrado.");

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioDia = agora.toLocalDate().atStartOfDay();
        LocalDateTime inicioSemana = agora.minusDays(7);
        LocalDateTime inicioMes = agora.minusDays(30);

        List<Leitura> leiturasDia = leituraRepository.findByMedidorIdAndPeriodo(medidorId, inicioDia, agora);
        List<Leitura> leiturasSemana = leituraRepository.findByMedidorIdAndPeriodo(medidorId, inicioSemana, agora);
        List<Leitura> leiturasMes = leituraRepository.findByMedidorIdAndPeriodo(medidorId, inicioMes, agora);

        BigDecimal consumoDiario = calcularConsumo(leiturasDia);
        BigDecimal consumoSemanal = calcularConsumo(leiturasSemana);
        BigDecimal consumoMensal = calcularConsumo(leiturasMes);
        BigDecimal vazaoMedia = calcularVazaoMedia(leiturasMes);
        BigDecimal vazaoMaxima = calcularVazaoMaxima(leiturasMes);

        return new EstatisticaResponseDTO(
            medidorId,
            medidor.getNome(),
            consumoDiario,
            consumoSemanal,
            consumoMensal,
            vazaoMedia,
            vazaoMaxima,
            leiturasMes.size()
        );
    }

    private BigDecimal calcularConsumo(List<Leitura> leituras) {
        if (leituras.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal primeiro = leituras.get(leituras.size() - 1).getConsumoTotal();
        BigDecimal ultimo = leituras.get(0).getConsumoTotal();
        
        return ultimo.subtract(primeiro);
    }

    private BigDecimal calcularVazaoMedia(List<Leitura> leituras) {
        if (leituras.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal soma = leituras.stream()
                .map(Leitura::getVazao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return soma.divide(BigDecimal.valueOf(leituras.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularVazaoMaxima(List<Leitura> leituras) {
        if (leituras.isEmpty()) return BigDecimal.ZERO;
        
        return leituras.stream()
                .map(Leitura::getVazao)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}

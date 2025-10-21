package br.unitins.topicos1.service;

import java.time.LocalDateTime;
import java.util.List;

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
    public LeituraRepository leituraRepository;

    @Inject
    public MedidorRepository medidorRepository;

    @Override
    @Transactional
    public LeituraResponseDTO create(@Valid LeituraDTO dto) {
        Medidor medidor = medidorRepository.findById(dto.medidorId());
        if (medidor == null)
            throw new ValidationException("medidorId", "Medidor não encontrado.");

        Leitura leitura = new Leitura();
        leitura.setMedidor(medidor);
        leitura.setVazao(dto.vazao());
        leitura.setConsumoTotal(dto.consumoTotal());
        leitura.setDataHora(LocalDateTime.now());

        leituraRepository.persist(leitura);
        return LeituraResponseDTO.valueOf(leitura);
    }

    @Override
    public LeituraResponseDTO findById(Long id) {
        Leitura leitura = leituraRepository.findById(id);
        if (leitura == null)
            throw new ValidationException("id", "Leitura não encontrada.");
        return LeituraResponseDTO.valueOf(leitura);
    }

    @Override
    public List<LeituraResponseDTO> findByMedidorId(Long medidorId) {
        return leituraRepository.findByMedidorId(medidorId)
                .stream()
                .map(LeituraResponseDTO::valueOf)
                .toList();
    }

    @Override
    public List<LeituraResponseDTO> findByMedidorIdAndPeriodo(Long medidorId, LocalDateTime inicio, LocalDateTime fim) {
        return leituraRepository.findByMedidorIdAndPeriodo(medidorId, inicio, fim)
                .stream()
                .map(LeituraResponseDTO::valueOf)
                .toList();
    }

    @Override
    public List<LeituraResponseDTO> findLeiturasRecentes(Long medidorId, int limit) {
        return leituraRepository.findLeiturasRecentes(medidorId, limit)
                .stream()
                .map(LeituraResponseDTO::valueOf)
                .toList();
    }
}

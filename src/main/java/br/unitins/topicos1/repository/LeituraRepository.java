package br.unitins.topicos1.repository;

import java.time.LocalDateTime;
import java.util.List;
import br.unitins.topicos1.model.Leitura;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LeituraRepository implements PanacheRepository<Leitura> {
    
    public List<Leitura> findByMedidorIdAndPeriodo(Long medidorId, LocalDateTime inicio, LocalDateTime fim) {
        return find("medidor.id = ?1 AND dataHora BETWEEN ?2 AND ?3 ORDER BY dataHora ASC", 
                    medidorId, inicio, fim).list();
    }
    
    public Leitura findUltimaLeitura(Long medidorId) {
        return find("medidor.id = ?1 ORDER BY dataHora DESC", medidorId).firstResult();
    }
}

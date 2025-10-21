package br.unitins.topicos1.repository;

import java.util.List;

import br.unitins.topicos1.model.Sugestao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SugestaoRepository implements PanacheRepository<Sugestao> {
    
    public List<Sugestao> findByUsuarioId(Long usuarioId) {
        return find("usuario.id = ?1 ORDER BY dataHora DESC", usuarioId).list();
    }
    
    public List<Sugestao> findSugestoesRecentes(Long usuarioId, int limit) {
        return find("usuario.id = ?1 ORDER BY dataHora DESC", usuarioId)
                .page(0, limit)
                .list();
    }
}

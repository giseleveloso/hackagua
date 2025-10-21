package br.unitins.topicos1.repository;

import java.util.List;

import br.unitins.topicos1.model.Medidor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MedidorRepository implements PanacheRepository<Medidor> {
    
    public List<Medidor> findByUsuarioId(Long usuarioId) {
        return find("usuario.id = ?1", usuarioId).list();
    }
    
    public List<Medidor> findByNome(String nome) {
        return find("UPPER(nome) LIKE ?1", "%" + nome.toUpperCase() + "%").list();
    }
}
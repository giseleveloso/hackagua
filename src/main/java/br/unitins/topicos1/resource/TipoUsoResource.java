package br.unitins.topicos1.resource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import br.unitins.topicos1.model.TipoUso;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tipos-uso")
@Produces(MediaType.APPLICATION_JSON)
public class TipoUsoResource {

    private static final Logger LOG = Logger.getLogger(TipoUsoResource.class);

    @GET
    @PermitAll
    public Response listarTiposUso() {
        LOG.info("Listando tipos de uso disponíveis");
        
        List<TipoUsoDTO> tipos = Arrays.stream(TipoUso.values())
            .map(tipo -> new TipoUsoDTO(tipo.getId(), tipo.getDescricao()))
            .collect(Collectors.toList());
        
        return Response.ok(tipos).build();
    }

    // DTO interno para resposta
    public static class TipoUsoDTO {
        public Integer id;
        public String descricao;

        public TipoUsoDTO(Integer id, String descricao) {
            this.id = id;
            this.descricao = descricao;
        }
    }
}

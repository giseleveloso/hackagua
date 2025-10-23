package br.unitins.topicos1.resource;

import org.jboss.logging.Logger;

import br.unitins.topicos1.service.SugestaoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/sugestoes")
public class SugestaoResource {
    
    @Inject
    public SugestaoService sugestaoService;

    private static final Logger LOG = Logger.getLogger(SugestaoResource.class);

    @GET
    @Path("/medidor/{medidorId}")
    public Response gerarParaMedidor(
        @PathParam("medidorId") Long medidorId,
        @QueryParam("dataInicio") String dataInicio,
        @QueryParam("dataFim") String dataFim
    ) {
        LOG.infof("Gerando sugestões IA para medidor: %d", medidorId);
        try {
            return Response.ok(sugestaoService.gerarSugestoesIAParaMedidor(medidorId, dataInicio, dataFim)).build();
        } catch (Exception e) {
            LOG.error("Erro ao gerar sugestões por medidor", e);
            return Response.status(Status.BAD_GATEWAY).entity("Resposta IA inválida ou indisponível").build();
        }
    }

}

package br.unitins.topicos1.resource;

import org.jboss.logging.Logger;

import br.unitins.topicos1.dto.SugestaoDTO;
import br.unitins.topicos1.service.SugestaoService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.POST;
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
    @Path("/usuario/{usuarioId}")
    public Response findByUsuarioId(@PathParam("usuarioId") Long usuarioId) {
        LOG.infof("Buscando sugestões do usuário: %d", usuarioId);
        return Response.ok(sugestaoService.findByUsuarioId(usuarioId)).build();
    }

    @POST
    @Path("/gerar/{usuarioId}")
    public Response gerarSugestoesIA(@PathParam("usuarioId") Long usuarioId) {
        LOG.infof("Gerando sugestões IA para usuário: %d", usuarioId);
        try {
            return Response.status(Status.CREATED)
                          .entity(sugestaoService.gerarSugestoesIA(usuarioId))
                          .build();
        } catch (Exception e) {
            LOG.error("Erro ao gerar sugestões", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    @POST
    public Response create(@Valid SugestaoDTO dto) {
        LOG.infof("Criando nova sugestão para usuário: %d", dto.usuarioId());
        try {
            return Response.status(Status.CREATED)
                          .entity(sugestaoService.create(dto))
                          .build();
        } catch (Exception e) {
            LOG.error("Erro ao criar sugestão", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

package br.unitins.topicos1.resource;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import br.unitins.topicos1.dto.LeituraDTO;
import br.unitins.topicos1.service.LeituraService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/leituras")
public class LeituraResource {
    
    @Inject
    public LeituraService leituraService;

    private static final Logger LOG = Logger.getLogger(LeituraResource.class);

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        LOG.infof("Buscando leitura com id: %d", id);
        return Response.ok(leituraService.findById(id)).build();
    }

    @GET
    @Path("/medidor/{medidorId}")
    public Response findByMedidorId(@PathParam("medidorId") Long medidorId) {
        LOG.infof("Buscando leituras do medidor: %d", medidorId);
        return Response.ok(leituraService.findByMedidorId(medidorId)).build();
    }

    @GET
    @Path("/medidor/{medidorId}/periodo")
    public Response findByPeriodo(
            @PathParam("medidorId") Long medidorId,
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim) {
        
        LOG.infof("Buscando leituras do medidor %d no período: %s a %s", medidorId, inicio, fim);
        
        LocalDateTime dataInicio = LocalDateTime.parse(inicio);
        LocalDateTime dataFim = LocalDateTime.parse(fim);
        
        return Response.ok(leituraService.findByMedidorIdAndPeriodo(medidorId, dataInicio, dataFim)).build();
    }

    @GET
    @Path("/medidor/{medidorId}/recentes")
    public Response findRecentes(
            @PathParam("medidorId") Long medidorId,
            @QueryParam("limit") Integer limit) {
        
        int limitValue = (limit != null && limit > 0) ? limit : 10;
        LOG.infof("Buscando %d leituras recentes do medidor: %d", limitValue, medidorId);
        
        return Response.ok(leituraService.findLeiturasRecentes(medidorId, limitValue)).build();
    }

    @POST
    public Response create(@Valid LeituraDTO dto) {
        LOG.infof("Registrando nova leitura para medidor: %d", dto.medidorId());
        try {
            return Response.status(Status.CREATED)
                          .entity(leituraService.create(dto))
                          .build();
        } catch (Exception e) {
            LOG.error("Erro ao registrar leitura", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

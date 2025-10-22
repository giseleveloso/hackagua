package br.unitins.topicos1.resource;

import org.jboss.logging.Logger;

import br.unitins.topicos1.service.EstatisticaService;
import br.unitins.topicos1.dto.UsuarioEstatisticaResponseDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/estatisticas")
public class EstatisticaResource {
    
    @Inject
    public EstatisticaService estatisticaService;

    private static final Logger LOG = Logger.getLogger(EstatisticaResource.class);

    @GET
    @Path("/medidor/{medidorId}")
    public Response calcularEstatisticas(@PathParam("medidorId") Long medidorId) {
        LOG.infof("Calculando estatísticas do medidor: %d", medidorId);
        return Response.ok(estatisticaService.calcularEstatisticas(medidorId)).build();
    }

    @GET
    @Path("/usuario/{usuarioId}")
    public Response calcularEstatisticasUsuario(@PathParam("usuarioId") Long usuarioId) {
        LOG.infof("Calculando estatísticas do usuário: %d", usuarioId);
        UsuarioEstatisticaResponseDTO dto = estatisticaService.calcularEstatisticasUsuario(usuarioId);
        return Response.ok(dto).build();
    }
}

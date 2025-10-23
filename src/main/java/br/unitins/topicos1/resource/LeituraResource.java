package br.unitins.topicos1.resource;

import java.time.LocalDate;
import org.jboss.logging.Logger;
import br.unitins.topicos1.service.LeituraService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/leituras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeituraResource {

    @Inject
    LeituraService leituraService;

    private static final Logger LOG = Logger.getLogger(LeituraResource.class);

    @GET
    @Path("/estatisticas/medidor/{medidorId}")
    public Response estatisticas(
            @PathParam("medidorId") Long medidorId,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {

        LocalDate inicio = dataInicio != null ? LocalDate.parse(dataInicio) : LocalDate.now();
        LocalDate fim = dataFim != null ? LocalDate.parse(dataFim) : LocalDate.now();

        LOG.infof("Estatísticas medidor %d: %s a %s", medidorId, inicio, fim);
        return Response.ok(leituraService.calcularEstatisticas(medidorId, inicio, fim)).build();
    }

    @GET
    @Path("/estatisticas/medidor/{medidorId}/hoje")
    public Response estatisticasHoje(@PathParam("medidorId") Long medidorId) {
        LocalDate hoje = LocalDate.now();
        return Response.ok(leituraService.calcularEstatisticas(medidorId, hoje, hoje)).build();
    }

    @GET
    @Path("/estatisticas/medidor/{medidorId}/semana")
    public Response estatisticasSemana(@PathParam("medidorId") Long medidorId) {
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusDays(6);
        return Response.ok(leituraService.calcularEstatisticas(medidorId, inicio, fim)).build();
    }

    @GET
    @Path("/estatisticas/medidor/{medidorId}/mes")
    public Response estatisticasMes(@PathParam("medidorId") Long medidorId) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        return Response.ok(leituraService.calcularEstatisticas(medidorId, inicioMes, hoje)).build();
    }

    @GET
    @Path("/tempo-real/medidor/{medidorId}")
    public Response tempoReal(@PathParam("medidorId") Long medidorId) {
        return Response.ok(leituraService.obterTempoReal(medidorId)).build();
    }

    @GET
    @Path("/tempo-real/usuario/{usuarioId}")
    public Response tempoRealPorUsuario(@PathParam("usuarioId") Long usuarioId) {
        return Response.ok(leituraService.obterTempoRealPorUsuario(usuarioId)).build();
    }
}

package br.unitins.topicos1.resource;

import org.jboss.logging.Logger;

import br.unitins.topicos1.dto.MedidorDTO;
import br.unitins.topicos1.service.MedidorService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/medidores")
public class MedidorResource {

    @Inject
    public MedidorService medidorService;

    private static final Logger LOG = Logger.getLogger(MedidorResource.class);

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        LOG.infof("Buscando medidor com id: %d", id);
        return Response.ok(medidorService.findById(id)).build();
    }

    @GET
    public Response findAll() {
        LOG.info("Listando todos os medidores");
        return Response.ok(medidorService.findAll()).build();
    }

    @GET
    @Path("/usuario/{usuarioId}")
    public Response findByUsuarioId(@PathParam("usuarioId") Long usuarioId) {
        LOG.infof("Buscando medidores do usuário: %d", usuarioId);
        return Response.ok(medidorService.findByUsuarioId(usuarioId)).build();
    }

    @GET
    @Path("/search/nome/{nome}")
    public Response findByNome(@PathParam("nome") String nome) {
        LOG.infof("Buscando medidores com nome: %s", nome);
        return Response.ok(medidorService.findByNome(nome)).build();
    }

    @POST
    public Response create(@Valid MedidorDTO dto) {
        LOG.infof("Criando novo medidor: %s", dto.nome());
        try {
            return Response.status(Status.CREATED)
                    .entity(medidorService.create(dto))
                    .build();
        } catch (Exception e) {
            LOG.error("Erro ao criar medidor", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid MedidorDTO dto) {
        LOG.infof("Atualizando medidor com id: %d", id);
        return Response.ok(medidorService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        LOG.infof("Deletando medidor com id: %d", id);
        medidorService.delete(id);
        return Response.status(Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/{id}/power")
    public Response setPower(@PathParam("id") Long id, @QueryParam("ligado") boolean ligado) {
        LOG.infof("Atualizando power do medidor %d para: %s", id, ligado);
        return Response.ok(medidorService.setPower(id, ligado)).build();
    }

    @PUT
    @Path("/{id}/power/toggle")
    public Response togglePower(@PathParam("id") Long id) {
        LOG.infof("Alternando power do medidor %d", id);
        return Response.ok(medidorService.togglePower(id)).build();
    }
}

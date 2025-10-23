package br.unitins.topicos1.resource;

import org.jboss.logging.Logger;

import br.unitins.topicos1.dto.UsuarioDTO;
import br.unitins.topicos1.dto.UsuarioUpdateDTO;
import br.unitins.topicos1.service.UsuarioService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/usuarios")
public class UsuarioResource {

    @Inject
    public UsuarioService usuarioService;

    private static final Logger LOG = Logger.getLogger(UsuarioResource.class);

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        LOG.infof("Buscando usuário com id: %d", id);
        return Response.ok(usuarioService.findById(id)).build();
    }

    @GET
    public Response findAll() {
        LOG.info("Listando todos os usuários");
        return Response.ok(usuarioService.findAll()).build();
    }

    @GET
    @Path("/email/{email}")
    public Response findByEmail(@PathParam("email") String email) {
        LOG.infof("Buscando usuário com email: %s", email);
        return Response.ok(usuarioService.findByEmail(email)).build();
    }

    @POST
    public Response create(@Valid UsuarioDTO dto) {
        LOG.infof("Criando novo usuário: %s", dto.nome());
        try {
            return Response.status(Status.CREATED)
                    .entity(usuarioService.create(dto))
                    .build();
        } catch (Exception e) {
            LOG.error("Erro ao criar usuário", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid UsuarioUpdateDTO dto) {
        LOG.infof("Atualizando usuário com id: %d", id);
        return Response.ok(usuarioService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        LOG.infof("Deletando usuário com id: %d", id);
        usuarioService.delete(id);
        return Response.status(Status.NO_CONTENT).build();
    }
}

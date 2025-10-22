package br.unitins.topicos1.resource;

import org.jboss.logging.Logger;

import br.unitins.topicos1.dto.LoginDTO;
import br.unitins.topicos1.dto.UsuarioDTO;
import br.unitins.topicos1.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    public AuthService authService;

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginDTO dto) {
        LOG.infof("Tentativa de login: %s", dto.email());
        try {
            return Response.ok(authService.login(dto)).build();
        } catch (Exception e) {
            LOG.errorf("Erro no login para %s: %s", dto.email(), e.getMessage());
            return Response.status(Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/registro")
    @PermitAll
    public Response registro(@Valid UsuarioDTO dto) {
        LOG.infof("Novo registro: %s", dto.email());
        try {
            return Response.status(Status.CREATED)
                    .entity(authService.registro(dto))
                    .build();
        } catch (Exception e) {
            LOG.errorf("Erro no registro: %s", e.getMessage());
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
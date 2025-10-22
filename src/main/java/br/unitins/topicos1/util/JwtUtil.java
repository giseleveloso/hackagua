package br.unitins.topicos1.util;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import br.unitins.topicos1.dto.UsuarioResponseDTO;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtUtil {
    
    private static final Duration JWT_DURATION = Duration.ofHours(24);
    
    public String generateJwt(UsuarioResponseDTO usuario) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(JWT_DURATION);
        
        Set<String> roles = new HashSet<>();
        roles.add("Usuario");
        
        return Jwt.issuer("hackagua-api")
                .subject(usuario.email())
                .groups(roles)
                .claim("id", usuario.id())
                .claim("nome", usuario.nome())
                .issuedAt(now)
                .expiresAt(expiryDate)
                .sign();
    }
}


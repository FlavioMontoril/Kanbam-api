package com.api.kanbam.infra.security;

import com.api.kanbam.domain.dtos.chat.TokenDataDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;

import java.util.List;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.issuer}")
    private String issuer;

    /**
     * Valida o token e extrai userId, role e subject em UMA ÚNICA verificação criptográfica.
     */
    public TokenDataDTO extractTokenData(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            String subject = jwt.getSubject();
            String userId = extractUserIdFromJwt(jwt);
            String role = extractRoleFromJwt(jwt);

            if (subject == null || userId == null) {
                log.warn("Token JWT válido, mas sem as claims essenciais (subject ou id).");
                return null;
            }

            // Retorna a ordem exata do seu TokenDataDTO(userId, role, subject)
            return new TokenDataDTO(userId, role, subject);

        } catch (JWTVerificationException exception) {
            log.debug("Falha na verificação do token JWT: {}", exception.getMessage());
            return null;
        }
    }

    private String extractUserIdFromJwt(DecodedJWT jwt) {
        Claim idClaim = jwt.getClaim("id");
        if (idClaim.isNull()) {
            return null;
        }
        // Aceita o ID tanto se o Auth-Server mandar como String ("123") quanto como Long/Número (123)
        return idClaim.asLong() != null ? String.valueOf(idClaim.asLong()) : idClaim.asString();
    }

    private String extractRoleFromJwt(DecodedJWT jwt) {
        Claim roleClaim = jwt.getClaim("role");
        if (!roleClaim.isNull() && roleClaim.asString() != null) {
            return roleClaim.asString();
        }

        Claim rolesClaim = jwt.getClaim("roles");
        if (!rolesClaim.isNull()) {
            List<String> rolesList = rolesClaim.asList(String.class);
            if (rolesList != null && !rolesList.isEmpty()) {
                return rolesList.get(0);
            }
            return rolesClaim.asString();
        }

        return null;
    }
}

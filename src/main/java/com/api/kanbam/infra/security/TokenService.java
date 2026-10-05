package com.api.kanbam.infra.security;

import com.api.kanbam.domain.dtos.chat.TokenDataDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.issuer}")
    private String issuer;

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public TokenDataDTO extractTokenData(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var jwt = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            // Extração flexível do ID (aceita Long ou String)
            Claim idClaim = jwt.getClaim("id");
            String userId = null;
            if (!idClaim.isNull()) {
                userId = idClaim.asLong() != null ? String.valueOf(idClaim.asLong()) : idClaim.asString();
            }

            // Extração da Role com suporte a "role" ou "roles"
            String role = null;
            Claim roleClaim = jwt.getClaim("role");
            if (!roleClaim.isNull() && roleClaim.asString() != null) {
                role = roleClaim.asString();
            } else {
                Claim rolesClaim = jwt.getClaim("roles");
                if (!rolesClaim.isNull()) {
                    List<String> rolesList = rolesClaim.asList(String.class);
                    role = (rolesList != null && !rolesList.isEmpty()) ? rolesList.get(0) : rolesClaim.asString();
                }
            }

            return new TokenDataDTO(userId, role, jwt.getSubject());
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
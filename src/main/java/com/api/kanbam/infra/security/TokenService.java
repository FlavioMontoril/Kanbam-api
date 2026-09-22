//package com.api.kanbam.infra.security;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.exceptions.JWTVerificationException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class TokenService {
//
//    @Value("${api.security.token.secret}")
//    private String secret;
//
//    public String validateToken(String token) {
//        try {
//            Algorithm algorithm = Algorithm.HMAC256(secret);
//            return JWT.require(algorithm)
//                    .withIssuer("auth-server-api") // Deve bater com o issuer da API de Auth
//                    .build()
//                    .verify(token)
//                    .getSubject();
//        } catch (JWTVerificationException exception) {
//            return null; // Token inválido ou expirado
//        }
//    }
//
//    public String extractRole(String token) {
//        try {
//            Algorithm algorithm = Algorithm.HMAC256(secret);
//            return JWT.require(algorithm)
//                    .withIssuer("auth-server-api")
//                    .build()
//                    .verify(token)
//                    .getClaim("role")
//                    .asString();
//        } catch (JWTVerificationException exception) {
//            return null;
//        }
//    }
//}

package com.api.kanbam.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public String extractRole(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var jwt = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            // Tenta ler como "role" (String)
            Claim roleClaim = jwt.getClaim("role");
            if (!roleClaim.isNull() && roleClaim.asString() != null) {
                return roleClaim.asString();
            }

            // Fallback: Tenta ler como "roles" (Lista ou String)
            Claim rolesClaim = jwt.getClaim("roles");
            if (!rolesClaim.isNull()) {
                List<String> rolesList = rolesClaim.asList(String.class);
                if (rolesList != null && !rolesList.isEmpty()) {
                    return rolesList.get(0);
                }
                return rolesClaim.asString();
            }

            return null;
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
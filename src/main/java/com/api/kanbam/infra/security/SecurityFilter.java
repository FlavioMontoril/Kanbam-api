package com.api.kanbam.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var token = recoverToken(request);

        if (token != null) {
            // Usa o novo método centralizado que extrai as claims em uma única verificação
            var tokenData = tokenService.extractTokenData(token);

            if (tokenData != null && tokenData.userId() != null && tokenData.role() != null) {
                String role = tokenData.role();
                // Formata para garantir o prefixo "ROLE_" exigido pelo Spring Security
                String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                var authorities = List.of(new SimpleGrantedAuthority(formattedRole));
                var authentication = new UsernamePasswordAuthenticationToken(tokenData.userId(), null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
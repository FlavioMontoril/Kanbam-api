package com.api.kanbam.config;

import com.api.kanbam.infra.security.JwtHandshakeInterceptor;
import com.api.kanbam.infra.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenService tokenService;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita o prefixo de tópico para o servidor enviar mensagens ao client
        // Habilita os dois prefixos: /topic (broadcast) e /queue (mensagens individuais/privadas)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefixo para mensagens enviadas do client para o servidor (se necessário)
        config.setApplicationDestinationPrefixes("/app");

        // Define o prefixo para destinos de usuários específicos (usado pelo convertAndSendToUser)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        registry.addEndpoint("/ws-kanban")
                .setAllowedOriginPatterns(origins)
                .addInterceptors(new JwtHandshakeInterceptor()) //Intercepta o Cookie HttpOnly no Handshake HTTP
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Intercepta a tentativa de conexão inicial via STOMP (Frame CONNECT)
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = extractToken(accessor);

                    if (token != null) {
                        var tokenData = tokenService.extractTokenData(token);

                        if (tokenData != null && tokenData.userId() != null && tokenData.role() != null) {
                            String formattedRole = tokenData.role().startsWith("ROLE_") ? tokenData.role() : "ROLE_" + tokenData.role();
                            var authorities = List.of(new SimpleGrantedAuthority(formattedRole));

                            var authentication = new UsernamePasswordAuthenticationToken(tokenData.userId(), null, authorities);

                            // Atribui o usuário autenticado à sessão do WebSocket
                            accessor.setUser(authentication);
                        } else {
                            throw new IllegalArgumentException("Token JWT inválido ou expirado no WebSocket.");
                        }
                    } else {
                        throw new IllegalArgumentException("Header Authorization ausente na conexão WebSocket.");
                    }
                }
                return message;
            }
        });
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // Tenta recuperar o token extraído do Cookie HttpOnly durante o Handshake
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("token")) {
            String token = (String) sessionAttributes.get("token");
            if (token != null && !token.isBlank()) {
                return token;
            }
        }

        // Fallback: Cabeçalho nativo "Authorization: Bearer <token>"
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Fallback: Cabeçalho nativo "token"
        String tokenParam = accessor.getFirstNativeHeader("token");
        if (tokenParam != null && !tokenParam.isBlank()) {
            return tokenParam;
        }

        return null;
    }
}

package org.chatapp.e2eechatserver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatapp.e2eechatserver.security.jwt.JwtTokenUtil;
import org.chatapp.e2eechatserver.user.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) return message;

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String auth = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
                    if (auth == null || !auth.startsWith("Bearer ")) {
                        throw new AccessDeniedException("Missing Authorization header");
                    }
                    String token = auth.substring("Bearer ".length());
                    String username = jwtTokenUtil.getUsername(token);
                    UserDetails userDetails = userService.loadUserByUsername(username);

                    accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));

                    log.info("WS CONNECT principal name = {}", accessor.getUser().getName());
                }
                return message;
            }
        });
    }
}


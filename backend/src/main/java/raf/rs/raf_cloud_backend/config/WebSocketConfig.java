package raf.rs.raf_cloud_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import raf.rs.raf_cloud_backend.handler.GeneralWebSocketHandler;

@Configuration
@Component
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private GeneralWebSocketHandler generalWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(generalWebSocketHandler, "/ws/events")
                .setAllowedOrigins("http://localhost:4200")
                .setAllowedOriginPatterns("http://localhost:*");

        registry
                .addHandler(generalWebSocketHandler, "/ws/machines")
                .setAllowedOrigins("http://localhost:4200")
                .setAllowedOriginPatterns("http://localhost:*");
    }
}



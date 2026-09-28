package raf.rs.raf_cloud_backend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final String jwtSecret;

    public JwtAuthenticationFilter(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        if (requestPath.startsWith("/ws/")) {
            logger.debug("WebSocket zahtev preskočen - JWT nije obavezan: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        if (requestPath.equals("/api/auth/login")) {
            logger.debug("Login zahtev preskočen - JWT nije obavezan");
            filterChain.doFilter(request, response);
            return;
        }
        if (requestPath.equals("/auth/register")) {
            logger.debug("Register zahtev preskočen - JWT nije obavezan");
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            try {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

                io.jsonwebtoken.Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                Object idObj = claims.get("id");
                Long userId = null;
                if (idObj instanceof Integer) {
                    userId = ((Integer) idObj).longValue();
                } else if (idObj instanceof Long) {
                    userId = (Long) idObj;
                } else if (idObj != null) {
                    userId = Long.valueOf(idObj.toString());
                }

                request.setAttribute("userId", userId);
                request.setAttribute("email", claims.getSubject());
                request.setAttribute("permissions", claims.get("permissions"));

                logger.debug("JWT Token parsed - userId: " + userId + ", email: " + claims.getSubject());

            } catch (Exception e) {
                logger.warn("JWT token validation failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}


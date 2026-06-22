package com.example.backend_tallerautomotriz.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Rutas públicas que no necesitan token
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/pagos/stripe/config",
            "/swagger-ui",
            "/api-docs"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Rutas públicas: pasar sin validar token
        for (String pub : PUBLIC_PATHS) {
            if (path.startsWith(pub)) {
                chain.doFilter(request, response);
                return;
            }
        }

        String header = request.getHeader("Authorization");

        // Sin header → pasar (Spring Security se encarga con .anyRequest().authenticated())
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // Token inválido o expirado → responder 401 directamente
        if (!tokenProvider.validateToken(token)) {
            log.warn("Token inválido o expirado: {}", path);
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
            return;
        }

        try {
            String email = tokenProvider.getEmailFromToken(token);

            if (email == null || email.isBlank()) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token sin datos de usuario");
                return;
            }

            UserDetails user = userDetailsService.loadUserByUsername(email);

            if (!user.isEnabled() || !user.isAccountNonLocked()) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Cuenta bloqueada o desactivada");
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Usuario autenticado: {}", email);

        } catch (UsernameNotFoundException ex) {
            log.warn("Usuario del token no encontrado: {}", ex.getMessage());
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Usuario no encontrado");
            return;
        } catch (Exception ex) {
            log.error("Error procesando token: {}", ex.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno al validar sesión");
            return;
        }

        chain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        if (response.isCommitted()) return;
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "status", status,
                "error", status == 401 ? "Unauthorized" : "Internal Server Error",
                "message", message
        ));
    }
}
package com.DAT250Project.PollApp.security;

/**
 * JWT authentication filter that intercepts each incoming HTTP request,
 * extracts the JWT token from the Authorization header, validates it,
 * loads the corresponding user, and sets authentication in the security context.
 *
 * <p>This filter runs once per request and supports stateless authentication.</p>
 */

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Extracts and validates JWT token from each incoming request.
     * If valid, sets authenticated user info into Spring Security context.
     *
     * @param request The incoming HTTP request
     * @param response The HTTP response being built
     * @param filterChain The next filter in the chain
     * @throws ServletException in case of errors
     * @throws IOException in case of I/O errors
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT validation for preflight (OPTIONS) requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Expecting "Authorization: Bearer <token>"
        final String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        // Extract token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                username = jwtService.getUsernameFromToken(token);
            } catch (Exception ex) {
                // Token extraction failed — leave username null
            }
        }

        // If username is valid and no authentication is set yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Validate token for this user
            if (jwtService.validateToken(token)) {

                // Build an authenticated token object and store it in security context
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}

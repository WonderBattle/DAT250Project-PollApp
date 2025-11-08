package com.DAT250Project.PollApp.security;

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

    // Called once per request
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Allow preflight (OPTIONS) to pass through (do not try to authenticate)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get Authorization header (expected: "Bearer <token>")
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // Check header format
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtService.getUsernameFromToken(token);
            } catch (Exception ex) {
                // If token parsing fails, username will remain null
            }
        }

        // If we got a username and there is no authentication in security context yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // load user details
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            // validate token
            if (jwtService.validateToken(token)) {
                // create authentication object and set it into the context
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // continue filter chain
        filterChain.doFilter(request, response);
    }
}

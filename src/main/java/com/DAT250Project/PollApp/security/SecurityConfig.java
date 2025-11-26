package com.DAT250Project.PollApp.security;

/**
 * Security configuration class for the PollApp backend.
 * Configures authentication, authorization, password encoding, JWT filters,
 * and CORS rules for the application.
 *
 * <p>This configuration uses Spring Security with JWT-based stateless authentication.
 * No sessions or CSRF protection are used.</p>
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Inject our custom JWT filter that will validate tokens on each request
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Inject our CustomUserDetailsService to load users from the database
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Creates a BCrypt password encoder bean.
     * Used for hashing and validating user passwords.
     *
     * @return PasswordEncoder (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt hashes passwords before storing in DB (very secure)
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the authentication provider used by Spring Security.
     * This provider uses the CustomUserDetailsService and the BCrypt password encoder.
     *
     * @return DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // This connects Spring Security's authentication system
        // with our custom user loading logic and password encoder
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Provides the authentication manager used for login operations.
     *
     * @param config Authentication configuration provided by Spring
     * @return AuthenticationManager
     * @throws Exception if authentication manager cannot be retrieved
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Provides a way to authenticate users manually (used in AuthController during login)
        return config.getAuthenticationManager();
    }

    /**
     * Main Spring Security filter chain configuration.
     * Defines authorization rules, disables CSRF, enables CORS, and registers the JWT filter.
     *
     * @param http HttpSecurity object provided by Spring
     * @return SecurityFilterChain configured filter chain
     * @throws Exception on configuration failure
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF since JWT is stateless
                .csrf(csrf -> csrf.disable())

                // Enable CORS rules defined in corsConfigurationSource()
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Allow the H2 console to load correctly
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))

                // Authorization rules for endpoints
                .authorizeHttpRequests(auth -> auth
                        // Allow anonymous access to these endpoints
                        .requestMatchers("/auth/**", "/polls/public", "/polls/*/options").permitAll()
                        .requestMatchers("/auth/login", "/auth/**").permitAll()
                        .requestMatchers("/polls/*/votes").permitAll()
                        .requestMatchers("/users", "/users/**").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // Public for everyone
                        .requestMatchers(HttpMethod.GET, "/polls/**").permitAll()
                        // Protect other endpoints
                        .requestMatchers("/polls", "/polls/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/polls/user/{userId}").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/polls/*/privacy").authenticated()
                        .anyRequest().authenticated()
                )

                // Connect custom authentication provider
                .authenticationProvider(authenticationProvider())

                // Register custom JWT filter before username/password authentication
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Finally, build and return the complete security configuration
        return http.build();
    }

    /**
     * CORS configuration bean for allowing the frontend (React) to communicate with backend.
     *
     * @return CorsConfigurationSource containing allowed origins, headers, and methods
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed frontend origin (React)
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // Allowed request methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed headers sent from frontend
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Headers exposed back to frontend
        configuration.setExposedHeaders(List.of("Authorization"));

        // No cookies required for JWT
        configuration.setAllowCredentials(false);

        // Register this configuration for all paths in the app
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

package com.DAT250Project.PollApp.security;

// ------------------ Imports ------------------
// These are all necessary Spring Security and Spring Framework classes.
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

// ------------------ Configuration Class ------------------
@Configuration  // Marks this class as a Spring configuration file.
@EnableMethodSecurity  // Enables method-level annotations like @PreAuthorize (optional, for future use)
public class SecurityConfig {

    // Inject our custom JWT filter that will validate tokens on each request
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Inject our CustomUserDetailsService to load users from the database
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // ------------------ Password Encoder Bean ------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt hashes passwords before storing in DB (very secure)
        return new BCryptPasswordEncoder();
    }

    // ------------------ Authentication Provider ------------------
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // This connects Spring Security's authentication system
        // with our custom user loading logic and password encoder
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService); // who loads users
        authProvider.setPasswordEncoder(passwordEncoder()); // how to verify passwords
        return authProvider;
    }

    // ------------------ Authentication Manager ------------------
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Provides a way to authenticate users manually (used in AuthController during login)
        return config.getAuthenticationManager();
    }

    // ------------------ Main Security Configuration ------------------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // This defines how Spring Security should secure HTTP requests.
        http
                // Disable CSRF since we use JWT (not cookies or sessions)
                .csrf(csrf -> csrf.disable())

                // Enable CORS with the custom configuration (defined below)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Allow the H2 database console to work (frames are blocked by default)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // Define which requests are allowed without authentication
                .authorizeHttpRequests(auth -> auth
                        // Allow anyone to access the authentication endpoints (login/register)
                        .requestMatchers("/auth/**").permitAll()

                        // Allow user registration (POST /users)
                        .requestMatchers("/users").permitAll()

                        .requestMatchers("/polls/public").permitAll()

                        .requestMatchers("/polls/private/**").authenticated()

                        //Allow anonymus votes
                        .requestMatchers(HttpMethod.POST, "/polls/**").permitAll()

                        // Allow Swagger and API docs without needing to log in
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Allow H2 console for development
                        .requestMatchers("/h2-console/**").permitAll()

                        // Any other endpoint (like /polls, /votes, etc.) requires authentication
                        .anyRequest().authenticated()
                )

                // Connect our authentication provider (user + password logic)
                .authenticationProvider(authenticationProvider())

                // Register our JWT filter before the built-in authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Finally, build and return the complete security configuration
        return http.build();
    }

    // ------------------ CORS Configuration ------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Create a configuration object describing what origins and headers are allowed
        CorsConfiguration configuration = new CorsConfiguration();

        // The URL of your React frontend (change if using another port)
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // The HTTP methods your app can use
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // The request headers the frontend is allowed to send
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Headers that the backend can expose to the frontend (optional)
        configuration.setExposedHeaders(List.of("Authorization"));

        // Credentials (cookies) are not needed for JWT, so we set false
        configuration.setAllowCredentials(false);

        // Register this configuration for all paths in the app
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        // Return the final CORS configuration
        return source;
    }
}

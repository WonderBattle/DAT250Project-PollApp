package com.DAT250Project.PollApp.security;

import com.DAT250Project.PollApp.model.User;
import com.DAT250Project.PollApp.repository.UserRepository;
import com.DAT250Project.PollApp.security.JwtService;
import com.DAT250Project.PollApp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // bean from SecurityConfig

    @Autowired
    private AuthenticationManager authenticationManager; // used for authentication during login

    @Autowired
    private JwtService jwtService;

    // -------------------------
    // REGISTER
    // -------------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        // expecting JSON with username, email, password
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        // basic validation
        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body("username, email and password are required");
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("email already in use");
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("username already in use");
        }

        // create user entity
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        // hash the password before saving
        user.setPassword(passwordEncoder.encode(password));
        // role left default ROLE_USER
        userRepository.save(user);

        // return created user (password will not be serialized thanks to @JsonProperty)
        return ResponseEntity.status(201).body(user);
    }

    // -------------------------
    // LOGIN
    // -------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        // expecting JSON with email and password
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("email and password are required");
        }

        // Authenticate - this will verify credentials using CustomUserDetailsService + PasswordEncoder
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, password);
        try {
            authenticationManager.authenticate(authToken);
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        // If authentication successful, generate token
        String jwt = jwtService.generateToken(email);

        // return token and optionally user info
        User user = userRepository.findByEmail(email).get();

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()
                )
        ));
    }
}

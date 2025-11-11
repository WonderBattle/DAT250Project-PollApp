package com.DAT250Project.PollApp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // read secret from application.properties
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // Create signing key from secret (HMAC SHA-256)
    private Key getSigningKey() {
        // IMPORTANT: In production the secret should be a strong random and safe (and stored outside
        // the code). JJWT can take a byte[] secret.
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate token for a username (email)
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)            // subject = user's email
                .setIssuedAt(now)                // token issued at
                .setExpiration(expiryDate)       // expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // sign
                .compact();
    }

    // Get username (email) from token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate token (signature + expiration)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            // JwtException covers expired, malformed, signature errors, etc.
            return false;
        }
    }
}


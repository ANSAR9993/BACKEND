package com.dgapr.demo.Security;

import com.dgapr.demo.Model.User;
import com.dgapr.demo.Repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    public static final String AUTH_HEADER   = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Value("${jwt.secret.v2}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    private SecretKey key;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** Create a Bearer-prefixed JWT with subject=username and claim "v"=tokenVersion */
    public String generateToken(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Instant now    = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationMs);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("v", user.getTokenVersion())
                .signWith(key)
                .compact();

        return TOKEN_PREFIX + token;
    }

    /** Returns true if signature+expiration are valid */
    public boolean validateToken(String rawToken) {
        try {
            parseClaims(rawToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            // In JJWT 0.12.x, various parsing/validation exceptions like SignatureException, MalformedJwtException etc.
            // are subclasses of JwtException.
            log.warn("JWT invalid: {}", e.getMessage());
        }
        return false;
    }

    /** Parse and return claims, stripping "Bearer " if present */
    public Claims parseClaims(String rawToken) {
        String token = rawToken.startsWith(TOKEN_PREFIX)
                ? rawToken.substring(TOKEN_PREFIX.length())
                : rawToken;

        // Updated parsing logic
        return Jwts.parser()
                .verifyWith(key) // Use verifyWith(SecretKey)
                .build()
                .parseSignedClaims(token) // Use parseSignedClaims()
                .getPayload(); // Use getPayload()
    }

    /** Extract the username (subject) */
    public String getUsernameFromJwt(String rawToken) {
        return parseClaims(rawToken).getSubject();
    }

    /** Extract numeric "v" claim as long, or –1 if missing/invalid */
    public long getTokenVersionFromJwt(String rawToken) {
        Object v = parseClaims(rawToken).get("v");
        return (v instanceof Number) ? ((Number) v).longValue() : -1L;
    }
}
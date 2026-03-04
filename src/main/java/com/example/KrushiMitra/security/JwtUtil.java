package com.example.KrushiMitra.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    // ✅ 0.12.x uses SecretKey directly
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)                          // ✅ was setSubject()
                .issuedAt(new Date())                    // ✅ was setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + expiration)) // ✅ was setExpiration()
                .signWith(getSigningKey())               // ✅ no algorithm needed separately
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()                             // ✅ was parserBuilder()
                .verifyWith(getSigningKey())             // ✅ was setSigningKey()
                .build()
                .parseSignedClaims(token)               // ✅ was parseClaimsJws()
                .getPayload()                            // ✅ was getBody()
                .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiry = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiry.before(new Date());
    }
}
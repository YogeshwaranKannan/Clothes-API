package Farme_rich.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    public static String generateToken(Map<String, Object> claims, String pin, String deviceId, String email) {
        try {
            String platform = (String) claims.get("platform");
            String rawSecret;
            String subject;


            if ("WEB".equalsIgnoreCase(platform)) {
                rawSecret = email;
                subject = "WEB|" + email;
                if (pin != null && !pin.isEmpty()) {
                    rawSecret += pin;
                    subject += "|" + pin;
                }


            } else {
                rawSecret = (pin == null || pin.isEmpty()) ? deviceId : pin + deviceId;

                subject = "APP|" + deviceId + "|" + (pin == null ? "" : pin);
            }

            if (rawSecret == null || rawSecret.isEmpty()) {
                throw new RuntimeException("JWT secret is null");
            }
            byte[] secretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);

            if (secretBytes.length < 32) {
                secretBytes = Arrays.copyOf(secretBytes, 32);
            } else if (secretBytes.length > 32) {
                secretBytes = Arrays.copyOf(secretBytes, 32);
            }

            SecretKey key = Keys.hmacShaKeyFor(secretBytes);

            logger.info("Generating token with:");
            logger.info("Subject: " + subject);
            logger.info("Claims: " + claims);
            logger.info("Key : " + key);
            logger.info("Key length: " + secretBytes.length + " bytes");

            long now = System.currentTimeMillis();

            long expiryTime = now + (60 * 60 * 1000); // 1 hour


            long refreshTime = expiryTime - (45 * 60 * 1000);
            claims.put("refreshAt", refreshTime);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(now))
                    .setExpiration(new Date(expiryTime))
                    .signWith(key, SignatureAlgorithm.HS256)  // mobile+pin or deviceId + pin
                    .compact();

        } catch (Exception e) {
            logger.error("JWT generation error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private static SecretKey buildKey(String secret) {
        byte[] keyBytes = Arrays.copyOf(
                secret.getBytes(StandardCharsets.UTF_8),
                32);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseClaimsAllowExpired(String token, String secret) {
        SecretKey key = buildKey(secret);
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)   // 🔑 verifies signature
                    .getBody();

        } catch (ExpiredJwtException e) {
            return e.getClaims();

        } catch (JwtException | IllegalArgumentException e) {
            throw e;
        }
    }

    public String extractSubjectWithoutValidation(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("Invalid JWT");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            Map<String, Object> payload = new ObjectMapper().readValue(payloadJson, Map.class);
            return (String) payload.get("sub");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to read JWT subject"
            );
        }
    }

    public String extractSubject(String token, String secret) {
        Claims claims = parseClaimsAllowExpired(token, secret);
        return claims.getSubject();
    }

    public String extractDeviceId(String token, String secret) {
        String subject = extractSubject(token, secret);
        String[] parts = subject.split("\\|");
        if (parts.length >= 2 &&
                "APP".equalsIgnoreCase(parts[0])) {
            return parts[1];
        }

        return null;
    }

    public String extractEmail(String token, String secret) {
        String subject = extractSubject(token, secret);

        String[] parts = subject.split("\\|");

        if (parts.length >= 2 &&
                "WEB".equalsIgnoreCase(parts[0])) {
            return parts[1];
        }

        return null;
    }

    public String extractPin(String token, String secret) {
        String subject = extractSubject(token, secret);

        String[] parts = subject.split("\\|");

        if (parts.length >= 3 &&
                "APP".equalsIgnoreCase(parts[0])) {

            return parts[2].isBlank()
                    ? null
                    : parts[2];
        }

        return null;
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public boolean shouldRefresh(Claims claims) {
        Long refreshAt = claims.get("refreshAt", Long.class);
        if (refreshAt == null) {
            return false;
        }
        return System.currentTimeMillis() >= refreshAt;
    }

    public Key getHmacKeyFromString(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // Ensure 256-bit key; pad or hash if needed
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32); // pad with 0s
        }
        return Keys.hmacShaKeyFor(keyBytes); // secure and recommended
    }

}

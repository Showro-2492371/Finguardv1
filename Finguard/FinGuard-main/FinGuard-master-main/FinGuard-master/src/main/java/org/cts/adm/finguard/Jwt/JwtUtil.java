package org.cts.adm.finguard.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-ms:3600000}")
    private long expirationMs;

    public String generateToken(Long customerId, String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("customerId", customerId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long extractCustomerId(String token) {
        return getClaims(token).get("customerId", Long.class);
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


//
//@Component
//public class JwtUtil {
//
//    private String SECRET;
//    private String TOKEN;
//    private final long EXPIRATION = 1000*60*60;
//
//
//    public String generateToken(String userName){
//        byte[] key = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
//        String base64Key = Base64.getEncoder().encodeToString(key);
//        SECRET = new String(base64Key);
//        TOKEN =  Jwts.builder()
//                .setSubject(userName)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(
//                        System.currentTimeMillis()+EXPIRATION
//                ))
//                .signWith(Keys.hmacShaKeyFor(base64Key.getBytes()),
//                        SignatureAlgorithm.HS256)
//                .compact();
//
//        return TOKEN;
//    }
//
//    public String extractUsername(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(SECRET.getBytes())
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    public boolean isTokenValid(String token) {
//        try {
//            extractUsername(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public String getSecretKey(){
//        return SECRET;
//    }
//
//}

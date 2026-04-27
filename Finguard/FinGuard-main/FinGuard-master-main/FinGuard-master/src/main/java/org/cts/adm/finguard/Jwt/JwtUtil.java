package org.cts.adm.finguard.Jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private String SECRET;
    private String TOKEN;
    private final long EXPIRATION = 1000*60*60;


    public String generateToken(String userName){
        byte[] key = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(key);
        SECRET = new String(base64Key);
        TOKEN =  Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(
                        System.currentTimeMillis()+EXPIRATION
                ))
                .signWith(Keys.hmacShaKeyFor(base64Key.getBytes()),
                        SignatureAlgorithm.HS256)
                .compact();

        return TOKEN;
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            extractUsername(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSecretKey(){
        return SECRET;
    }

}

package com.project.shopapp.components;

import com.project.shopapp.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    @Value("${jwt.expiration}")
    private int expiration; // able to save to environment variable

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String generateToken(User user){
        // properties -> claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", user.getPhoneNumber());
        try {
            String token = Jwts
                    .builder()
                    .setClaims(claims) // how to extract from this ? -> extractAllClaims method above
                    .setSubject(user.getPhoneNumber())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration*1000L))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        }catch (Exception e){
            // can use "Logger"
            System.err.println("Cannot create Jwt token, error: " + e.getMessage());
            return null;
        }
    }
    private Key getSignInKey(){
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    // extract Claims
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJwt(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = this.extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    // check expiration
    public boolean isTokenExpired(String token){
        Date expirationDate = this.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

}

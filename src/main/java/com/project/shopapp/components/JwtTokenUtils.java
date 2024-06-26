package com.project.shopapp.components;

import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    @Value("${jwt.expiration}")
    private int expiration; // able to save to environment variable

    @Value("${jwt.secretKey}")
    private String secretKey;

    private final TokenRepository tokenRepository;

    public String generateToken(User user) throws Exception{
        // properties -> claims
        Map<String, Object> claims = new HashMap<>();
//        this.generateSecretKey();
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("userId", user.getId());
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
            throw new InvalidParamException("Cannot create jwt token, error " + e.getMessage());
        }
    }
    private Key getSignInKey(){
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    private String generateSecretKey(){
        SecureRandom random =  new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        String secretKey = Encoders.BASE64.encode(keyBytes);
        return secretKey;
    }

    // extract Claims
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
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

    public String extractPhoneNumber(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, User userDetails){
        String phoneNumber = extractPhoneNumber(token);
        Token existingToken = tokenRepository.findByToken(token);
        if(existingToken == null || existingToken.isRevoked() == true || !userDetails.isActive()){
            return false;
        }
        return (phoneNumber.equals(userDetails.getUsername())) &&
                !isTokenExpired(token);
    }

}

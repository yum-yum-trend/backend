package com.udangtangtang.backend.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil  implements Serializable {

    private static final Long serialVersionUID = -2550185165626007488L;

    public static final Long ACCESS_TOKEN_EXP_TIME =  5 * 1000L;

    public static final Long REFRESH_TOKEN_EXP_TIME = 7 * 24  * 60 * 60 * 1000L;

    @Value("${jwt.secret}")
    private String secret;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
        } catch (IllegalArgumentException e) {
            System.out.println("an error occured during getting username from token");
            e.printStackTrace();
            return false;
        } catch(SignatureException e){
            System.out.println("Authentication Failed. Username or Password not valid.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder().setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setSubject(username).signWith(SignatureAlgorithm.HS512, secret)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP_TIME)).compact();
    }

    public String generateRefreshToken() {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder().setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS512, secret)
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP_TIME)).compact();
    }
}

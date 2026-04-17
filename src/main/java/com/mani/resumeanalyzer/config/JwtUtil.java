package com.mani.resumeanalyzer.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {

	private final SecretKey secretKey = Jwts.SIG.HS256.key().build();
	private final long EXPIRATION = 86400000; // 24 hours

	// Takes email → returns token string(Use Jwts.builder() to build and return token)
	public String generateToken(String email) {
		
		String token = Jwts.builder()
				.subject(email)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION))
				.signWith(secretKey)
				.compact();

		return token;
	}

	// Takes token → returns email stored inside(Use Jwts.parser() to parse and return subject)
	public String extractEmail(String token) {

		String email = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
		
		return email;
	}

	// Takes token → returns true if not expired
	public boolean isTokenValid(String token) {
	    try {
	        Date expiration = Jwts.parser()
	            .verifyWith(secretKey)
	            .build()
	            .parseSignedClaims(token)
	            .getPayload()
	            .getExpiration();
	        
	        return expiration.after(new Date());
	    } catch (Exception e) {
	        return false;
	    }
	}
}
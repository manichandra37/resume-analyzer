package com.mani.resumeanalyzer.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mani.resumeanalyzer.config.JwtUtil;
import com.mani.resumeanalyzer.dto.AuthResponse;
import com.mani.resumeanalyzer.dto.LoginRequest;
import com.mani.resumeanalyzer.dto.RegisterRequest;
import com.mani.resumeanalyzer.entity.Users;
import com.mani.resumeanalyzer.repository.UserRepository;

@Service
public class AuthService {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	UserRepository userRepository;

	public String Register(RegisterRequest request) {

		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("Email already Present");
		}

		Users user = new Users();
		user.setEmail(request.getEmail());
		user.setName(request.getName());
		user.setPhoneNumber(request.getPhoneNumber());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setCreatedAt(LocalDateTime.now());

		userRepository.save(user);

		return "User registered successfully";

	}

	public AuthResponse login(LoginRequest request) {

		AuthResponse response = new AuthResponse();

		Users user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new RuntimeException("Invalid Password");
		}

		String token = jwtUtil.generateToken(user.getEmail());

		response.setEmail(request.getEmail());
		response.setToken(token);

		return response;

	}

}

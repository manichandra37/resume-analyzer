package com.mani.resumeanalyzer.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mani.resumeanalyzer.dto.RegisterRequest;
import com.mani.resumeanalyzer.entity.User;
import com.mani.resumeanalyzer.repository.UserRepository;

@Service
public class AuthService {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	UserRepository userRepository;

	public String Register(RegisterRequest request) {

		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("Email already Present");
		}

		User user = new User();
		user.setEmail(request.getEmail());
		user.setName(request.getName());
		user.setPhoneNumber(request.getPhoneNumber());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setCreatedAt(LocalDateTime.now());

		userRepository.save(user);

		return "User registered successfully";

	}

}

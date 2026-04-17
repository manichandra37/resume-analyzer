package com.mani.resumeanalyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mani.resumeanalyzer.dto.AuthResponse;
import com.mani.resumeanalyzer.dto.LoginRequest;
import com.mani.resumeanalyzer.dto.RegisterRequest;
import com.mani.resumeanalyzer.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	AuthService authService;

	@PostMapping("/register")
	public String registerUser(@RequestBody RegisterRequest request) {

		return authService.Register(request);

	}

	@PostMapping("/login")
	public AuthResponse loginUser(@RequestBody LoginRequest request) {

		return authService.login(request);
	}

}

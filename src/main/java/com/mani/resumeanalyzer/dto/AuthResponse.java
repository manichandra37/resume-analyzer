package com.mani.resumeanalyzer.dto;

import lombok.Data;

@Data
public class AuthResponse {

	private String token;
	private String email;
}

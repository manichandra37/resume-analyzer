package com.mani.resumeanalyzer.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mani.resumeanalyzer.dto.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

	// Missing resume or no reports — HTTP 404 + body.
	@ExceptionHandler(ResumeNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResumeNotFound(ResumeNotFoundException ex) {
		ErrorResponse response = new ErrorResponse();
		response.setStatus(404);
		response.setMessage(ex.getMessage());
		response.setTimestamp(LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	// Explicit server errors from app code — HTTP 500.
	@ExceptionHandler(GenericException.class)
	public ResponseEntity<ErrorResponse> handleGenericException(GenericException ex) {
		ErrorResponse response = new ErrorResponse();

		response.setMessage(ex.getMessage());
		response.setStatus(500);
		response.setTimestamp(LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	// Fallback for anything not handled above — HTTP 500.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleExceptions(Exception ex) {
		ErrorResponse response = new ErrorResponse();

		response.setMessage("Something went Wrong" + ex.getMessage());
		response.setStatus(500);
		response.setTimestamp(LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}

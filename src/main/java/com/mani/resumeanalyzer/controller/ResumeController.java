package com.mani.resumeanalyzer.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mani.resumeanalyzer.service.ResumeService;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

	private final ResumeService resumeService;

	public ResumeController(ResumeService resumeService) {
		this.resumeService = resumeService;
	}

	@PostMapping("/upload")
	public ResponseEntity<String> uploadresume(@RequestParam("file") MultipartFile file){
		String result = resumeService.uploadAndProcessResume(file);
		return ResponseEntity.ok(result);
	}

}

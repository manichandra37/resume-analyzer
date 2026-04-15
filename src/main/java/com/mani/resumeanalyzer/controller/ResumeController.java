package com.mani.resumeanalyzer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mani.resumeanalyzer.service.ResumeGeneratorService;
import com.mani.resumeanalyzer.service.ResumeService;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

	private final ResumeService resumeService;
	private final ResumeGeneratorService resumeGeneratorService;

	public ResumeController(ResumeService resumeService, ResumeGeneratorService resumeGeneratorService) {
		this.resumeService = resumeService;
		this.resumeGeneratorService = resumeGeneratorService;
	}

	// Multipart "file": parse, save resume, return upload status string.
	@PostMapping("/upload")
	public ResponseEntity<String> uploadresume(@RequestParam("file") MultipartFile file) {
		String result = resumeService.uploadAndProcessResume(file);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/generate/{reportId}")
	public ResponseEntity<byte[]> generateResume(@PathVariable long reportId) {
		byte[] docx = resumeGeneratorService.generateDocx(reportId);
		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=resume.docx")
				.header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
				.body(docx);
	}

}

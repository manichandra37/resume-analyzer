package com.mani.resumeanalyzer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mani.resumeanalyzer.dto.AnalysisRequest;
import com.mani.resumeanalyzer.dto.ClaudeResponse;
import com.mani.resumeanalyzer.service.ResumeAnalysisService;

@RestController
@RequestMapping("/api/resumes")
public class AnalysisController {

	private final ResumeAnalysisService resumeAnalysisService;

	public AnalysisController(ResumeAnalysisService resumeAnalysisService) {
		this.resumeAnalysisService = resumeAnalysisService;
	}

	// POST body: jobDescription. Calls Claude, saves report, returns DTO.
	@PostMapping("/{resumeId}")
	public ResponseEntity<ClaudeResponse> analyzeResume(@PathVariable long resumeId,
			@RequestBody AnalysisRequest jobDescription) {

		if (jobDescription == null || jobDescription.getJobDescription() == null
				|| jobDescription.getJobDescription().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		ClaudeResponse result = resumeAnalysisService.claudeInteract(resumeId, jobDescription.getJobDescription());
		return ResponseEntity.ok(result);

	}

	// List reports for one resume.
	@GetMapping("/{resumeId}/reports")
	public ResponseEntity<List<ClaudeResponse>> getReport(@PathVariable long resumeId) {

		List<ClaudeResponse> reports = resumeAnalysisService.getReports(resumeId);
		return ResponseEntity.ok(reports);
	}

	// Get one report by id.
	@GetMapping("/reports/{reportId}")
	public ResponseEntity<ClaudeResponse> getReportById(@PathVariable long reportId) {

		ClaudeResponse report = resumeAnalysisService.getReportById(reportId);
		return ResponseEntity.ok(report);
	}

}

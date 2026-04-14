package com.mani.resumeanalyzer.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mani.resumeanalyzer.dto.ClaudeResponse;
import com.mani.resumeanalyzer.entity.AnalysisReport;
import com.mani.resumeanalyzer.entity.Resume;
import com.mani.resumeanalyzer.exception.ResumeNotFoundException;
import com.mani.resumeanalyzer.repository.AnalysisReportRepository;
import com.mani.resumeanalyzer.repository.ResumeRepository;

@Service
public class ResumeAnalysisService {

	@Autowired
	ResumeRepository resumeRepository;

	@Autowired
	AnthropicClient anthropicClient;

	@Autowired
	AnalysisReportRepository analysisReportRepository;

	// Claude analyze + persist; returns mapped DTO.
	public ClaudeResponse claudeInteract(long id, String jobDescription,String templateType) {

		Resume resume = resumeRepository.findById(id)
				.orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + id));

		String resumeText = resume.getExtractedText();

		String prompt = "Analyze this resume against the job description.\n\n" 
			    + "Resume:\n" + resumeText + "\n\n"
			    + "Job Description:\n" + jobDescription + "\n\n" 
			    + "Respond ONLY in JSON format with these fields:\n"
			    + "score: number from 0-100 representing how well the resume matches the job\n"
			    + "summary: brief analysis of the match\n"
			    + "matchedSkills: comma-separated list of skills from the resume that match the job description. If none, write 'None'\n"
			    + "missedSkills: comma-separated list of required skills from the job description that are missing in the resume. If none, write 'None'\n"
			    + "jobTitle: the job title from the job description\n"
			    + "The resume should be rewritten in " + templateType + " style:\n"
			    + "- service: emphasize client projects, team collaboration, delivery timelines, technology diversity\n"
			    + "- product: emphasize ownership, metrics, scale, system design, measurable impact\n"
			    + "- hybrid: balance both project delivery and measurable impact\n"
			    + "improvedResume: a JSON object with the resume rewritten for 95+ ATS score containing:\n"
			    + "  professionalSummary: a strong 3-4 sentence summary tailored to the job description\n"
			    + "  skills: array of skill strings, include matched skills and naturally add missed skills\n"
			    + "  experience: array of objects, each with title, company, duration, and bullets (array of strings rewritten with strong action verbs and metrics)\n"
			    + "  education: array of objects, each with degree, institution, year\n"
			    + "  certifications: array of certification name strings\n"
			    + "Do not include any text outside the JSON object. No markdown, no explanation.";
		
		
		MessageCreateParams params = MessageCreateParams.builder().model(Model.CLAUDE_HAIKU_4_5).maxTokens(4096L)
				.addUserMessage(prompt).build();

		Message message = anthropicClient.messages().create(params);

		String response = message.content().get(0).text().orElseThrow().text();

		response = response.replace("```json", "").replace("```", "").trim();

		ObjectMapper mapper = new ObjectMapper();

		try {
			JsonNode jsonNode = mapper.readTree(response);

			int score = jsonNode.get("score").asInt();
			String summary = jsonNode.get("summary").asText();
			String matchedSkills = jsonNode.get("matchedSkills").asText();
			String missedSkills = jsonNode.get("missedSkills").asText();
			String jobTitle = jsonNode.get("jobTitle").asText();

			String improvedContent = mapper.writeValueAsString(jsonNode.get("improvedResume"));

			AnalysisReport report = new AnalysisReport();

			report.setJobDescription(jobDescription);
			report.setScore(score);
			report.setSummary(summary);
			report.setMatchedSkills(matchedSkills);
			report.setMissedSkills(missedSkills);
			report.setAnalyzedAt(LocalDateTime.now());
			report.setResume(resume);
			report.setJobTitle(jobTitle);
			report.setImprovedContent(improvedContent);

			AnalysisReport savedReport = analysisReportRepository.save(report);

			return mapToResponse(savedReport);

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to parse Claude response", e);
		}
	}

	// Reports for resume id; 404 if empty.
	public List<ClaudeResponse> getReports(long id) {

		List<AnalysisReport> reports = analysisReportRepository.findByResumeId(id);
		if (reports.isEmpty()) {
			throw new ResumeNotFoundException("No reports found for resume id: " + id);
		}
		return reports.stream().map(this::mapToResponse).toList();
	}

	// Single report by pk; 404 if missing.
	public ClaudeResponse getReportById(long reportId) {
		AnalysisReport report = analysisReportRepository.findById(reportId)
				.orElseThrow(() -> new ResumeNotFoundException("Report not found for id:" + reportId));

		return mapToResponse(report);

	}

	// Report entity to response DTO.
	private ClaudeResponse mapToResponse(AnalysisReport report) {
		ClaudeResponse response = new ClaudeResponse();
		response.setId(report.getId());
		response.setJobTitle(report.getJobTitle());
		response.setMatchedSkills(report.getMatchedSkills());
		response.setMissedSkills(report.getMissedSkills());
		response.setScore(report.getScore());
		response.setSummary(report.getSummary());
		response.setAnalyzedAt(report.getAnalyzedAt());
		response.setImprovedContent(report.getImprovedContent());
		return response;
	}

}

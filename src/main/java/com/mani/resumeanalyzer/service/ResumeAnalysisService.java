package com.mani.resumeanalyzer.service;

import java.time.LocalDateTime;

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

	public ClaudeResponse claudeInteract(long id, String jobDescription) {

		Resume resume = resumeRepository.findById(id).orElseThrow(() -> new RuntimeException("Resume  not Found"));

		String resumeText = resume.getExtractedText();

		String prompt = "Analyze this resume against the job description.\n\n" + "Resume:\n" + resumeText + "\n\n"
				+ "Job Description:\n" + jobDescription + "\n\n" + "Respond ONLY in JSON format with these fields:\n"
				+ "score: number from 0-100 representing how well the resume matches the job\n"
				+ "summary: brief analysis of the match\n"
				+ "matchedSkills: comma-separated list of skills from the resume that match the job description. If none, write 'None'\n"
				+ "missedSkills: comma-separated list of required skills from the job description that are missing in the resume. If none, write 'None'\n"
				+ "jobTitle: the job title from the job description\n"
				+ "Do not include any text outside the JSON object. No markdown, no explanation.";

		MessageCreateParams params = MessageCreateParams.builder().model(Model.CLAUDE_SONNET_4_6).maxTokens(1024L)
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

			AnalysisReport report = new AnalysisReport();

			report.setJobDescription(jobDescription);
			report.setScore(score);
			report.setSummary(summary);
			report.setMatchedSkills(matchedSkills);
			report.setMissedSkills(missedSkills);
			report.setAnalyzedAt(LocalDateTime.now());
			report.setResume(resume);
			report.setJobTitle(jobTitle);

			AnalysisReport savedReport = analysisReportRepository.save(report);

			ClaudeResponse claudeResponse = new ClaudeResponse();

			claudeResponse.setAnalyzedAt(LocalDateTime.now());
			claudeResponse.setId(savedReport.getId());
			claudeResponse.setJobTitle(jobTitle);
			claudeResponse.setMatchedSkills(matchedSkills);
			claudeResponse.setMissedSkills(missedSkills);
			claudeResponse.setScore(score);
			claudeResponse.setSummary(summary);

			return claudeResponse;

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to parse Claude response", e);
		}
	}

}

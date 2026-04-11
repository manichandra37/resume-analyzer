package com.mani.resumeanalyzer.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ClaudeResponse {
	
	private long id;
	private String jobTitle;
	private String matchedSkills;
	private String missedSkills;
	private int score;
	private String summary;
	private LocalDateTime analyzedAt;

}

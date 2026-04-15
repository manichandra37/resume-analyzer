package com.mani.resumeanalyzer.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "analysis_report")
public class AnalysisReport {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String missedSkills;

	private int score;

	private String jobTitle;

	@Column(columnDefinition = "TEXT")
	private String jobDescription;

	@Column(columnDefinition = "TEXT")
	private String improvedContent;

	private String templateType;
	
	@Column(columnDefinition = "TEXT")
	private String contactInfo;
	
	@ManyToOne
	@JoinColumn(name = "resume_id")
	private Resume resume;

	@Column(columnDefinition = "TEXT")
	private String matchedSkills;

	@Column(columnDefinition = "TEXT")
	private String summary;

	private LocalDateTime analyzedAt;

}

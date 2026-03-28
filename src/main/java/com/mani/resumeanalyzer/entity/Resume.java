package com.mani.resumeanalyzer.entity;


import java.time.LocalDateTime;

import com.mani.resumeanalyzer.dto.UploadStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name ="resumes")
public class Resume {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String resumeName;
	
	@Column(columnDefinition = "TEXT")
	private String extractedText;
	
	private LocalDateTime uploadedTime;
	
	@Enumerated(EnumType.STRING)
	private UploadStatus uploadStatus;
	
}




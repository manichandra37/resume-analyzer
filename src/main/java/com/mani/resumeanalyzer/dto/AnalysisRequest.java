package com.mani.resumeanalyzer.dto;

import lombok.Data;

@Data
public class AnalysisRequest {
	
	private String templateType; // "service", "product", "hybrid"
    private String jobDescription;
}
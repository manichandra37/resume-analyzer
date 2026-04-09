package com.mani.resumeanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.mani.resumeanalyzer.repository.ResumeRepository;

public class ResumeAnalysisService {
	
	@Autowired
	ResumeRepository resumeRepository;
	
//	public String analyzeResume(Long Id) {
//		
//		Resume resume = new Resume();
//		
//		resume = resumeRepository.findById(Id);
//		
//		return resume;
//		
//	}

}

package com.mani.resumeanalyzer.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mani.resumeanalyzer.dto.UploadStatus;
import com.mani.resumeanalyzer.entity.Resume;
import com.mani.resumeanalyzer.repository.ResumeRepository;

@Service
public class ResumeService {

	@Autowired
	ResumeRepository resumeRepository;

	@Autowired
	ParserService parserService;
	
	
	public String uploadAndProcessResume(MultipartFile file) {
		
		Resume resume = new Resume();
		
		try {
			resume.setResumeName(file.getOriginalFilename());
			
			String text = parserService.parseResume(file);
			
			if(text != null && !text.isBlank()) {
				resume.setExtractedText(text);
				resume.setUploadStatus(UploadStatus.COMPLETED);
			}else {
				resume.setUploadStatus(UploadStatus.FAILED);
			}
		} catch(Exception e) {
			resume.setUploadStatus(UploadStatus.FAILED);
		}
		
		resume.setUploadedTime(LocalDateTime.now());
		
		resumeRepository.save(resume);
		
		return resume.getUploadStatus().name();
		
	}
	
	

}

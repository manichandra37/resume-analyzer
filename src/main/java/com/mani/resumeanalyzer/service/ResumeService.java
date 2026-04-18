package com.mani.resumeanalyzer.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mani.resumeanalyzer.dto.UploadStatus;
import com.mani.resumeanalyzer.entity.Resume;
import com.mani.resumeanalyzer.entity.Users;
import com.mani.resumeanalyzer.repository.ResumeRepository;
import com.mani.resumeanalyzer.repository.UserRepository;

@Service
public class ResumeService {

	@Autowired
	ResumeRepository resumeRepository;

	@Autowired
	ParserService parserService;

	@Autowired
	UserRepository userRepository;

	// Parse upload, save resume, return status enum name.
	public String uploadAndProcessResume(MultipartFile file) {

		Resume resume = new Resume();

		try {
			resume.setResumeName(file.getOriginalFilename());

			String text = parserService.parseResume(file);

			if (text != null && !text.isBlank()) {
				resume.setExtractedText(text);
				resume.setUploadStatus(UploadStatus.COMPLETED);
			} else {
				resume.setUploadStatus(UploadStatus.FAILED);
			}
		} catch (Exception e) {
			resume.setUploadStatus(UploadStatus.FAILED);
		}

		resume.setUploadedTime(LocalDateTime.now());

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		resume.setUser(user);

		resumeRepository.save(resume);

		return resume.getUploadStatus().name();

	}

	public List<Resume> getMyResumes() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User Not Found"));

		return resumeRepository.findByUserId(user.getId());
	}

}

package com.mani.resumeanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mani.resumeanalyzer.entity.Resume;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long>{
	
	

}

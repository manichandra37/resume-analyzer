package com.mani.resumeanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mani.resumeanalyzer.entity.Resume;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

	List<Resume> findByUserId(Long userId);
}

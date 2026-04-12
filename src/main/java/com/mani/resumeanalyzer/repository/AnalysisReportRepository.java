package com.mani.resumeanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mani.resumeanalyzer.entity.AnalysisReport;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long>{
	
	List<AnalysisReport> findByResumeId(Long resumeId);

}

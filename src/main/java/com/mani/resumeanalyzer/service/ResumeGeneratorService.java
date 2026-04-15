package com.mani.resumeanalyzer.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mani.resumeanalyzer.entity.AnalysisReport;
import com.mani.resumeanalyzer.exception.ResumeNotFoundException;
import com.mani.resumeanalyzer.repository.AnalysisReportRepository;

@Service
public class ResumeGeneratorService {

    @Autowired
    AnalysisReportRepository analysisReportRepository;

    // Main method: fetches report, parses JSON, builds DOCX based on template type
    public byte[] generateDocx(long reportId) {

        // Step 1: Fetch report from DB
        AnalysisReport report = analysisReportRepository.findById(reportId)
                .orElseThrow(() -> new ResumeNotFoundException("Report not found with id: " + reportId));

        String templateType = report.getTemplateType();
        String improvedContent = report.getImprovedContent();
        String contactInfoStr = report.getContactInfo();

        ObjectMapper mapper = new ObjectMapper();

        try {
            // Step 2: Parse both JSON strings
            JsonNode resume = mapper.readTree(improvedContent);
            JsonNode contact = mapper.readTree(contactInfoStr);

            // Step 3: Extract contact info
            String name = contact.get("name").asText();
            String email = contact.get("email").asText();
            String phone = contact.get("phone").asText();
            String location = contact.get("location").asText();
            String linkedin = contact.get("linkedin").asText();
            String github = contact.get("github").asText();
            String leetcode = contact.get("leetcode").asText();

            // Step 4: Extract resume sections
            String summary = resume.get("professionalSummary").asText();
            JsonNode skills = resume.get("skills");
            JsonNode experience = resume.get("experience");
            JsonNode education = resume.get("education");
            JsonNode certifications = resume.get("certifications");

            // Step 5: Build DOCX
            XWPFDocument document = new XWPFDocument();
            
            CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
            CTPageMar pageMar = sectPr.addNewPgMar();
            pageMar.setTop(BigInteger.valueOf(720));
            pageMar.setBottom(BigInteger.valueOf(720));
            pageMar.setLeft(BigInteger.valueOf(720));
            pageMar.setRight(BigInteger.valueOf(720));
            
            // Get template-specific styling
            TemplateStyle style = getTemplateStyle(templateType);

            // --- NAME | JOB TITLE (one line, centered) ---
            XWPFParagraph namePara = document.createParagraph();
            namePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun nameRun = namePara.createRun();
            nameRun.setText(name + " | " + report.getJobTitle());
            nameRun.setBold(true);
            nameRun.setFontSize(style.nameSize);
            nameRun.setFontFamily(style.font);
            if (style.headerColor != null) {
                nameRun.setColor(style.headerColor);
            }

            // --- CONTACT INFO (one line: location | phone | email | LinkedIn | GitHub | LeetCode) ---
            XWPFParagraph contactPara = document.createParagraph();
            contactPara.setAlignment(ParagraphAlignment.CENTER);
            contactPara.setSpacingAfter(200);
            XWPFRun contactRun = contactPara.createRun();
            StringBuilder contactLine = new StringBuilder();
            if (!"N/A".equals(location)) contactLine.append(location);
            if (!"N/A".equals(phone)) contactLine.append(" | ").append(phone);
            if (!"N/A".equals(email)) contactLine.append(" | ").append(email);
            if (!"N/A".equals(linkedin)) contactLine.append(" | LinkedIn");
            if (!"N/A".equals(github)) contactLine.append(" | GitHub");
            if (!"N/A".equals(leetcode)) contactLine.append(" | LeetCode");
            contactRun.setText(contactLine.toString());
            contactRun.setFontSize(9);
            contactRun.setFontFamily(style.font);
            contactRun.setColor("555555");

            // --- SUMMARY SECTION ---
            addSectionHeader(document, "SUMMARY", style);
            XWPFParagraph summaryPara = document.createParagraph();
            summaryPara.setSpacingAfter(200);
            XWPFRun summaryRun = summaryPara.createRun();
            summaryRun.setText(summary);
            summaryRun.setFontSize(style.bodySize);
            summaryRun.setFontFamily(style.font);

            // --- SKILLS SECTION (categorized) ---
            addSectionHeader(document, "SKILLS", style);
            for (JsonNode skillGroup : skills) {
                XWPFParagraph skillPara = document.createParagraph();
                skillPara.setSpacingAfter(40);

                // Category name (bold)
                XWPFRun categoryRun = skillPara.createRun();
                categoryRun.setText(skillGroup.get("category").asText() + ": ");
                categoryRun.setBold(true);
                categoryRun.setFontSize(style.bodySize);
                categoryRun.setFontFamily(style.font);

                // Skill items (not bold)
                XWPFRun itemsRun = skillPara.createRun();
                itemsRun.setText(skillGroup.get("items").asText());
                itemsRun.setFontSize(style.bodySize);
                itemsRun.setFontFamily(style.font);
            }

            // --- PROFESSIONAL EXPERIENCE SECTION ---
            addSectionHeader(document, "PROFESSIONAL EXPERIENCE", style);
            for (JsonNode job : experience) {

                // Job title | Company    Duration — all on one line
                XWPFParagraph jobPara = document.createParagraph();
                jobPara.setSpacingAfter(80);

                // Job title (bold)
                XWPFRun jobTitleRun = jobPara.createRun();
                jobTitleRun.setText(job.get("title").asText());
                jobTitleRun.setBold(true);
                jobTitleRun.setFontSize(style.bodySize);
                jobTitleRun.setFontFamily(style.font);
                if (style.headerColor != null) {
                    jobTitleRun.setColor(style.headerColor);
                }

                // | Company
                XWPFRun companyRun = jobPara.createRun();
                companyRun.setText(" | " + job.get("company").asText() + "    ");
                companyRun.setFontSize(style.bodySize);
                companyRun.setFontFamily(style.font);

                // Duration (italic)
                XWPFRun durationRun = jobPara.createRun();
                durationRun.setText(job.get("duration").asText());
                durationRun.setItalic(true);
                durationRun.setFontSize(style.bodySize);
                durationRun.setFontFamily(style.font);
                durationRun.setColor("666666");

                // Bullet points
                for (JsonNode bullet : job.get("bullets")) {
                    XWPFParagraph bulletPara = document.createParagraph();
                    bulletPara.setSpacingAfter(40);
                    bulletPara.setIndentationLeft(360);
                    XWPFRun bulletRun = bulletPara.createRun();
                    bulletRun.setText("\u2022 " + bullet.asText());
                    bulletRun.setFontSize(style.bodySize);
                    bulletRun.setFontFamily(style.font);
                }

                // Spacing after each job
                XWPFParagraph spacer = document.createParagraph();
                spacer.setSpacingAfter(80);
            }

            // --- EDUCATION SECTION ---
            addSectionHeader(document, "EDUCATION", style);
            for (JsonNode edu : education) {
                XWPFParagraph eduPara = document.createParagraph();
                eduPara.setSpacingAfter(80);

                // Degree (bold)
                XWPFRun degreeRun = eduPara.createRun();
                degreeRun.setText(edu.get("degree").asText());
                degreeRun.setBold(true);
                degreeRun.setFontSize(style.bodySize);
                degreeRun.setFontFamily(style.font);

                // | Institution | Year
                XWPFRun instRun = eduPara.createRun();
                instRun.setText(" | " + edu.get("institution").asText() + " | " + edu.get("year").asText());
                instRun.setFontSize(style.bodySize);
                instRun.setFontFamily(style.font);
            }

            // --- CERTIFICATIONS SECTION ---
            if (certifications != null && certifications.size() > 0) {
                addSectionHeader(document, "CERTIFICATIONS", style);
                StringBuilder certsText = new StringBuilder();
                for (int i = 0; i < certifications.size(); i++) {
                    if (i > 0) certsText.append(" | ");
                    certsText.append(certifications.get(i).asText());
                }
                XWPFParagraph certsPara = document.createParagraph();
                XWPFRun certsRun = certsPara.createRun();
                certsRun.setText(certsText.toString());
                certsRun.setFontSize(style.bodySize);
                certsRun.setFontFamily(style.font);
            }

            // Step 6: Write document to byte array and return
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            document.close();
            return out.toByteArray();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse improved content", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate DOCX", e);
        }
    }

    // Helper: adds a section header with bottom border
    private void addSectionHeader(XWPFDocument document, String title, TemplateStyle style) {
        XWPFParagraph header = document.createParagraph();
        header.setSpacingBefore(200);
        header.setSpacingAfter(100);
        header.setBorderBottom(Borders.SINGLE);

        XWPFRun run = header.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(style.sectionHeaderSize);
        run.setFontFamily(style.font);
        if (style.headerColor != null) {
            run.setColor(style.headerColor);
        }
    }

    // Returns styling config based on template type
    private TemplateStyle getTemplateStyle(String templateType) {
        switch (templateType.toLowerCase()) {
            case "service":
                return new TemplateStyle("Calibri", 14, 11, 11, 10, null);
            case "product":
                return new TemplateStyle("Georgia", 16, 11, 11, 10, "1B2A4A");
            case "hybrid":
                return new TemplateStyle("Arial", 14, 11, 11, 10, "333333");
            default:
                return new TemplateStyle("Arial", 14, 11, 11, 10, "333333");
        }
    }

    // Inner class to hold template-specific styling values
    private static class TemplateStyle {
        String font;
        int nameSize;
        int subHeaderSize;
        int sectionHeaderSize;
        int bodySize;
        String headerColor;

        TemplateStyle(String font, int nameSize, int subHeaderSize, int sectionHeaderSize, int bodySize, String headerColor) {
            this.font = font;
            this.nameSize = nameSize;
            this.subHeaderSize = subHeaderSize;
            this.sectionHeaderSize = sectionHeaderSize;
            this.bodySize = bodySize;
            this.headerColor = headerColor;
        }
    }
}
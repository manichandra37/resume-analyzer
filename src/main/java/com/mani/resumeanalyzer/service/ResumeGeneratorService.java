package com.mani.resumeanalyzer.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
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

    public byte[] generateDocx(long reportId) {

        // Step 1: Fetch report from DB
        AnalysisReport report = analysisReportRepository.findById(reportId)
                .orElseThrow(() -> new ResumeNotFoundException("Report not found with id: " + reportId));

        String templateType = report.getTemplateType();
        String improvedContent = report.getImprovedContent();
        String contactInfoStr = report.getContactInfo();

        ObjectMapper mapper = new ObjectMapper();

        try {
            // Step 2: Parse both JSON strings into navigable objects
            JsonNode resume = mapper.readTree(improvedContent);
            JsonNode contact = mapper.readTree(contactInfoStr);

            // Step 3: Extract all sections from the parsed JSON
            String name = contact.get("name").asText();
            String email = contact.get("email").asText();
            String phone = contact.get("phone").asText();
            String location = contact.get("location").asText();
            String linkedin = contact.get("linkedin").asText();
            String github = contact.get("github").asText();
            String leetcode = contact.get("leetcode").asText();

            String summary = resume.get("professionalSummary").asText();
            JsonNode skills = resume.get("skills");
            JsonNode experience = resume.get("experience");
            JsonNode education = resume.get("education");
            JsonNode certifications = resume.get("certifications");
            
            System.out.println("=== DEBUG ===");
            System.out.println("Template: " + report.getTemplateType());
            System.out.println("Contact Info: " + report.getContactInfo());
            System.out.println("Improved Content: " + report.getImprovedContent());
            System.out.println("=== END DEBUG ===");

            // Step 4: Build DOCX based on template type
            XWPFDocument document = new XWPFDocument();

            // Get template-specific styling
            TemplateStyle style = getTemplateStyle(templateType);

            // --- NAME HEADER ---
            XWPFParagraph namePara = document.createParagraph();
            namePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun nameRun = namePara.createRun();
            nameRun.setText(name);
            nameRun.setBold(true);
            nameRun.setFontSize(style.nameSize);
            nameRun.setFontFamily(style.font);
            if (style.headerColor != null) {
                nameRun.setColor(style.headerColor);
            }

            // --- JOB TITLE ---
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(report.getJobTitle());
            titleRun.setFontSize(style.subHeaderSize);
            titleRun.setFontFamily(style.font);
            titleRun.setColor("555555");

            // --- CONTACT INFO ---
            // Build contact line: location | phone | email
            XWPFParagraph contactPara = document.createParagraph();
            contactPara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun contactRun = contactPara.createRun();
            StringBuilder contactLine = new StringBuilder();
            if (!"N/A".equals(location)) contactLine.append(location);
            if (!"N/A".equals(phone)) contactLine.append(" | ").append(phone);
            if (!"N/A".equals(email)) contactLine.append(" | ").append(email);
            contactRun.setText(contactLine.toString());
            contactRun.setFontSize(9);
            contactRun.setFontFamily(style.font);
            contactRun.setColor("666666");

            // Links line: LinkedIn | GitHub | LeetCode
            XWPFParagraph linksPara = document.createParagraph();
            linksPara.setAlignment(ParagraphAlignment.CENTER);
            linksPara.setSpacingAfter(200);
            StringBuilder linksLine = new StringBuilder();
            if (!"N/A".equals(linkedin)) linksLine.append("LinkedIn: ").append(linkedin);
            if (!"N/A".equals(github)) {
                if (linksLine.length() > 0) linksLine.append(" | ");
                linksLine.append("GitHub: ").append(github);
            }
            if (!"N/A".equals(leetcode)) {
                if (linksLine.length() > 0) linksLine.append(" | ");
                linksLine.append("LeetCode: ").append(leetcode);
            }
            if (linksLine.length() > 0) {
                XWPFRun linksRun = linksPara.createRun();
                linksRun.setText(linksLine.toString());
                linksRun.setFontSize(9);
                linksRun.setFontFamily(style.font);
                linksRun.setColor("0563C1"); // hyperlink blue color
            }

            // --- SUMMARY SECTION ---
            addSectionHeader(document, "SUMMARY", style);
            XWPFParagraph summaryPara = document.createParagraph();
            summaryPara.setSpacingAfter(200);
            XWPFRun summaryRun = summaryPara.createRun();
            summaryRun.setText(summary);
            summaryRun.setFontSize(style.bodySize);
            summaryRun.setFontFamily(style.font);

         // --- SKILLS SECTION ---
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
            
            // --- EXPERIENCE SECTION ---
            addSectionHeader(document, "PROFESSIONAL EXPERIENCE", style);
            for (JsonNode job : experience) {
                // Job title | Company
                XWPFParagraph jobPara = document.createParagraph();
                jobPara.setSpacingAfter(0);
                XWPFRun jobTitleRun = jobPara.createRun();
                jobTitleRun.setText(job.get("title").asText());
                jobTitleRun.setBold(true);
                jobTitleRun.setFontSize(style.bodySize);
                jobTitleRun.setFontFamily(style.font);
                if (style.headerColor != null) {
                    jobTitleRun.setColor(style.headerColor);
                }
                XWPFRun jobCompanyRun = jobPara.createRun();
                jobCompanyRun.setText("  |  " + job.get("company").asText());
                jobCompanyRun.setFontSize(style.bodySize);
                jobCompanyRun.setFontFamily(style.font);

                // Duration
                XWPFParagraph durationPara = document.createParagraph();
                durationPara.setSpacingAfter(80);
                XWPFRun durationRun = durationPara.createRun();
                durationRun.setText(job.get("duration").asText());
                durationRun.setItalic(true);
                durationRun.setFontSize(style.bodySize - 1);
                durationRun.setFontFamily(style.font);
                durationRun.setColor("666666");

                // Bullet points
                for (JsonNode bullet : job.get("bullets")) {
                    XWPFParagraph bulletPara = document.createParagraph();
                    bulletPara.setSpacingAfter(40);
                    bulletPara.setIndentationLeft(360); // indent bullets
                    XWPFRun bulletRun = bulletPara.createRun();
                    bulletRun.setText("\u2022 " + bullet.asText());
                    bulletRun.setFontSize(style.bodySize);
                    bulletRun.setFontFamily(style.font);
                }

                // Add spacing after each job
                XWPFParagraph spacer = document.createParagraph();
                spacer.setSpacingAfter(120);
            }

            // --- EDUCATION SECTION ---
            addSectionHeader(document, "EDUCATION", style);
            for (JsonNode edu : education) {
                XWPFParagraph eduPara = document.createParagraph();
                eduPara.setSpacingAfter(80);

                XWPFRun degreeRun = eduPara.createRun();
                degreeRun.setText(edu.get("degree").asText());
                degreeRun.setBold(true);
                degreeRun.setFontSize(style.bodySize);
                degreeRun.setFontFamily(style.font);

                XWPFRun instRun = eduPara.createRun();
                instRun.setText("  |  " + edu.get("institution").asText() + "  |  " + edu.get("year").asText());
                instRun.setFontSize(style.bodySize);
                instRun.setFontFamily(style.font);
            }

            // --- CERTIFICATIONS SECTION ---
            if (certifications != null && certifications.size() > 0) {
                addSectionHeader(document, "CERTIFICATIONS", style);
                StringBuilder certsText = new StringBuilder();
                for (int i = 0; i < certifications.size(); i++) {
                    if (i > 0) certsText.append("  |  ");
                    certsText.append(certifications.get(i).asText());
                }
                XWPFParagraph certsPara = document.createParagraph();
                XWPFRun certsRun = certsPara.createRun();
                certsRun.setText(certsText.toString());
                certsRun.setFontSize(style.bodySize);
                certsRun.setFontFamily(style.font);
            }

            // Step 5: Write document to byte array and return
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

    // Helper: adds a section header with a bottom border line
    private void addSectionHeader(XWPFDocument document, String title, TemplateStyle style) {
        XWPFParagraph header = document.createParagraph();
        header.setSpacingBefore(200);
        header.setSpacingAfter(100);
        // Add bottom border to section headers
        header.setBorderBottom(org.apache.poi.xwpf.usermodel.Borders.SINGLE);

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
        int nameSize;        // font size for name header
        int subHeaderSize;   // font size for job title under name
        int sectionHeaderSize; // font size for section headers (SUMMARY, SKILLS, etc.)
        int bodySize;        // font size for body text
        String headerColor;  // color for headers (null = black)

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
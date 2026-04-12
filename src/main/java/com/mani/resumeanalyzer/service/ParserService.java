package com.mani.resumeanalyzer.service;

import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ParserService {
	
	// Branch on extension: .pdf vs .docx.
	public String parseResume(MultipartFile file) {

		String fileName = file.getOriginalFilename();

		// we are checking file format and sending to corresponding Function
		try {
			if (fileName != null && fileName.endsWith(".pdf")) {
				return parsePdf(file.getInputStream());
			} else if (fileName != null && fileName.endsWith(".docx")) {
				return parseDocx(file.getInputStream());
			} else {
				throw new RuntimeException("Unsupported file type");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error parsing file", e);
		}
	}

	// Function to Extract data from PDF
	private String parsePdf(InputStream inputStream) throws Exception {
		PDDocument document = PDDocument.load(inputStream);
		PDFTextStripper stripper = new PDFTextStripper();
		String text = stripper.getText(document);
		document.close();
		return text;
	}

	// Function to Extract data from DoCX
	private String parseDocx(InputStream inputStream) throws Exception {
		XWPFDocument document = new XWPFDocument(inputStream);
		XWPFWordExtractor extractor = new XWPFWordExtractor(document);
		String text = extractor.getText();
		return text;
	}

}

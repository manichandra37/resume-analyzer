# AI Resume Analyzer — Backend

AI-powered resume analysis and generation tool built with Spring Boot and Claude API.

## Features
- Resume upload and parsing (PDF/DOCX)
- AI-powered resume analysis against job descriptions
- Resume generation with 3 template styles (Service/Product/Hybrid)
- DOCX download
- JWT authentication
- User-scoped data access

## Tech Stack
- Java 21, Spring Boot 4.0.4
- PostgreSQL
- Anthropic Claude API (Haiku)
- Apache POI (DOCX generation)
- JWT (Authentication)

## API Endpoints
- POST /api/auth/register
- POST /api/auth/login
- POST /api/resumes/upload
- GET /api/resumes/my-resumes
- POST /api/resumes/{resumeId} (analyze)
- GET /api/resumes/{resumeId}/reports
- GET /api/resumes/generate/{reportId} (download DOCX)

## Setup
1. Clone the repo
2. Set environment variable: ANTHROPIC_API_KEY
3. Configure PostgreSQL in application.properties
4. Run: mvn spring-boot:run

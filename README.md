# Resume Analyzer

A backend service built with Spring Boot that allows users to upload resumes in PDF or DOCX format, extracts text content, and stores the parsed data in a PostgreSQL database for further analysis.

## Tech Stack

- **Java 21**
- **Spring Boot**
- **PostgreSQL** -- persistent storage
- **Apache PDFBox** -- PDF text extraction
- **Apache POI** -- DOCX text extraction
- **Maven** -- build and dependency management

## Architecture

```
Controller  -->  ResumeService  -->  ParserService  -->  Repository
   |                 |                    |                  |
 REST API      Business logic     PDF/DOCX parsing     Database
```

- **Controller** -- exposes REST endpoints for upload and retrieval.
- **ResumeService** -- orchestrates the workflow between parsing and persistence.
- **ParserService** -- delegates to PDFBox or POI based on file type and returns extracted text.
- **Repository** -- Spring Data JPA repository backed by PostgreSQL.

## Features

- Upload resumes in PDF or DOCX format
- Automatic text extraction from uploaded documents
- Persistent storage of resume metadata and extracted content
- Retrieve all stored resumes or a single resume by ID

## API Endpoints

| Method | Endpoint                  | Description                    |
|--------|---------------------------|--------------------------------|
| POST   | `/api/resumes/upload`     | Upload a resume (PDF or DOCX)  |
| GET    | `/api/resumes`            | List all stored resumes        |
| GET    | `/api/resumes/{id}`       | Retrieve a specific resume     |

### Upload Example

```bash
curl -X POST http://localhost:8080/api/resumes/upload \
  -F "file=@resume.pdf"
```

## Getting Started

### Prerequisites

- Java 21
- PostgreSQL
- Maven

### Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/manichandra37/resume-analyzer.git
   cd resume-analyzer
   ```

2. **Configure the database**

   Create a PostgreSQL database and update `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/resume_analyzer
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Build the project**

   ```bash
   mvn clean install
   ```

4. **Run the application**

   ```bash
   mvn spring-boot:run
   ```

   The server starts at `http://localhost:8080`.

## Roadmap

- **AI-powered resume analysis** -- Integrate the Anthropic API to provide intelligent analysis of resume content, including skill extraction, experience summarization, and actionable feedback.

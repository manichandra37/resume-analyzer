# AI Resume Analyzer — Backend

An AI-powered resume analysis and generation tool that helps job seekers optimize their resumes for specific job descriptions. Built with Spring Boot and powered by Anthropic's Claude AI.

## What It Does

1. **Upload** your resume (PDF or DOCX)
2. **Analyze** it against any job description — get a match score, matched skills, missing skills, and a detailed summary
3. **Generate** an improved resume tailored to the job with a 95+ ATS score
4. **Download** the improved resume as a DOCX file
5. Choose from **3 template styles** — Service-based, Product-based, or Hybrid

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.0.4 | Backend framework |
| PostgreSQL | Database |
| Anthropic Claude API (Haiku) | AI analysis and resume generation |
| Apache PDFBox | PDF text extraction |
| Apache POI | DOCX text extraction + resume generation |
| JWT (JJWT) | Authentication |
| BCrypt | Password encryption |
| Maven | Build tool |

## Project Structure

```
src/main/java/com/mani/resumeanalyzer/
├── ResumeAnalyzerApplication.java          # Main application entry point
│
├── config/
│   ├── ClaudeConfig.java                   # Anthropic client bean configuration
│   ├── SecurityConfig.java                 # Spring Security + JWT filter config
│   ├── JwtUtil.java                        # JWT token generation and validation
│   └── JwtFilter.java                      # JWT authentication filter
│
├── controller/
│   ├── ResumeController.java               # Upload, generate, download endpoints
│   ├── AnalysisController.java             # Analysis and report endpoints
│   └── AuthController.java                 # Register and login endpoints
│
├── service/
│   ├── ResumeService.java                  # Upload and parse resume
│   ├── ParserService.java                  # Extract text from PDF/DOCX
│   ├── ResumeAnalysisService.java          # AI analysis using Claude API
│   ├── ResumeGeneratorService.java         # Generate DOCX resume from AI output
│   └── AuthService.java                    # User registration and login
│
├── entity/
│   ├── Resume.java                         # Resume entity (id, name, text, user)
│   ├── AnalysisReport.java                 # Analysis results (score, skills, etc.)
│   └── Users.java                          # User entity (name, email, password)
│
├── repository/
│   ├── ResumeRepository.java               # Resume database operations
│   ├── AnalysisReportRepository.java       # Report database operations
│   └── UserRepository.java                 # User database operations
│
├── dto/
│   ├── ClaudeResponse.java                 # Analysis response DTO
│   ├── AnalysisRequest.java                # Analysis request (jobDescription, templateType)
│   ├── RegisterRequest.java                # Registration request (name, email, password, phone)
│   ├── LoginRequest.java                   # Login request (email, password)
│   ├── AuthResponse.java                   # Login response (token, email)
│   ├── ErrorResponse.java                  # Error response (status, message, timestamp)
│   └── UploadStatus.java                   # Enum (COMPLETED, FAILED)
│
└── exception/
    ├── GlobalExceptionHandler.java         # Catches all exceptions, returns clean JSON
    ├── ResumeNotFoundException.java        # Custom 404 exception
    └── GenericException.java               # Custom 500 exception
```

## API Endpoints

### Authentication (No token required)

| Method | Endpoint | Description | Request Body |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | `{ name, email, password, phoneNumber }` |
| POST | `/api/auth/login` | Login and get JWT token | `{ email, password }` |

### Resume (Token required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/resumes/upload` | Upload a resume (PDF/DOCX) |
| GET | `/api/resumes/my-resumes` | Get all resumes of logged-in user |

### Analysis (Token required)

| Method | Endpoint | Description | Request Body |
|---|---|---|---|
| POST | `/api/resumes/{resumeId}` | Analyze resume against job description | `{ jobDescription, templateType }` |
| GET | `/api/resumes/{resumeId}/reports` | Get all reports for a resume | — |
| GET | `/api/resumes/report/{reportId}` | Get a single report | — |

### Download (Token required)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/resumes/generate/{reportId}` | Download improved resume as DOCX |

## Template Types

| Type | Best For | Style |
|---|---|---|
| `service` | TCS, Infosys, Accenture, Wipro | Emphasizes client projects, team collaboration, delivery timelines |
| `product` | Google, Amazon, JPMorgan, Stripe | Emphasizes ownership, metrics, scale, measurable impact |
| `hybrid` | Deloitte, Cognizant, mid-size startups | Balanced — project delivery + impact metrics |

## How It Works

```
User uploads resume (PDF/DOCX)
        ↓
ParserService extracts text (PDFBox / Apache POI)
        ↓
Text saved to PostgreSQL database
        ↓
User provides job description + selects template type
        ↓
Claude AI analyzes resume vs job description
Returns: score, matched skills, missed skills, improved resume content
        ↓
Results saved to database
        ↓
User requests DOCX generation
        ↓
ResumeGeneratorService builds formatted DOCX using Apache POI
        ↓
User downloads the improved resume
```

## Setup Instructions

### Prerequisites

- Java 21
- Maven
- PostgreSQL
- Anthropic API key ([Get one here](https://console.anthropic.com))

### Step 1: Clone the repository

```bash
git clone https://github.com/manichandra37/resume-analyzer.git
cd resume-analyzer
```

### Step 2: Create PostgreSQL database

```bash
psql -U your_username
CREATE DATABASE resume_analyzer;
\q
```

### Step 3: Set environment variables

Add these to your shell profile (`~/.zshrc` for Mac, `~/.bashrc` for Linux):

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/resume_analyzer
export DB_USERNAME=your_postgres_username
export DB_PASSWORD=your_postgres_password
export ANTHROPIC_API_KEY=your_anthropic_api_key
export JWT_SECRET=any_long_random_secret_string
```

Then load them:

```bash
source ~/.zshrc
```

### Step 4: Run the application

```bash
mvn clean compile spring-boot:run
```

The app starts at `http://localhost:8080`

### Step 5: Test with Postman

1. **Register:** `POST http://localhost:8080/api/auth/register`
2. **Login:** `POST http://localhost:8080/api/auth/login` → copy the JWT token
3. **Upload resume:** `POST http://localhost:8080/api/resumes/upload` (add token in Authorization → Bearer Token)
4. **Analyze:** `POST http://localhost:8080/api/resumes/{resumeId}` with job description in body
5. **Download:** `GET http://localhost:8080/api/resumes/generate/{reportId}`

## Sample API Responses

### Login Response
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "mani@gmail.com"
}
```

### Analysis Response
```json
{
    "id": 252,
    "jobTitle": "Java Developer",
    "score": 72,
    "matchedSkills": "Java, Spring Boot, Microservices, REST APIs",
    "missedSkills": "Docker, AWS, System Design",
    "summary": "Strong match for backend development...",
    "improvedContent": "{ ... improved resume JSON ... }",
    "analyzedAt": "2026-04-13T03:09:38"
}
```

### Error Response
```json
{
    "status": 404,
    "message": "Resume not found with id: 999",
    "timestamp": "2026-04-11T19:51:58"
}
```

## Architecture

```
Controller Layer (REST endpoints)
        ↓
Service Layer (business logic)
    ├── ResumeService (upload + parse)
    ├── ResumeAnalysisService (AI analysis via Claude)
    ├── ResumeGeneratorService (DOCX generation)
    └── AuthService (register + login)
        ↓
Repository Layer (database operations)
        ↓
PostgreSQL Database
```

## Security

- All endpoints except `/api/auth/**` require a valid JWT token
- Passwords are encrypted using BCrypt
- JWT tokens expire after 24 hours
- Each user can only access their own resumes and reports
- API keys and secrets are stored as environment variables (never hardcoded)

## Future Enhancements

- [ ] PDF download support
- [ ] Frontend (React)
- [ ] Stripe payment integration
- [ ] Deploy to cloud (Render/AWS)
- [ ] Email notifications
- [ ] Resume version history

## Author

**Manichandra Maddi**
- GitHub: [@manichandra37](https://github.com/manichandra37)
- LinkedIn: [Manichandra Maddi](https://linkedin.com/in/manichandramaddi)

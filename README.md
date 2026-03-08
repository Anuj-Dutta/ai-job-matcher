# AI Job Matcher

An AI-powered system that analyzes a candidate's resume and automatically matches it with relevant job opportunities using semantic similarity and embeddings.

The platform allows users to upload their resume, extracts meaningful information, compares it with job descriptions from the Adzuna API, and ranks the most relevant jobs based on AI-driven similarity scores.

Users receive the job recommendations directly on the interface and via email.

---

# Features

### Resume Upload
Users can upload their resume (PDF format) through a clean web interface.

### AI Resume Analysis
The system extracts text from the resume and generates semantic embeddings.

### Smart Job Matching
Jobs are matched using **semantic similarity**, not just keyword matching.

### Job Aggregation
Jobs are automatically fetched from the **Adzuna Job API** and stored in the database.

### Ranking System
Jobs are ranked based on similarity score to show the most relevant opportunities first.

### Email Notification
Top matching jobs are automatically sent to the user's email.

### Modern User Interface
The frontend includes:

- Dark / Light mode toggle
- Animated loading indicators
- Job ranking visualization
- Match score progress bars
- Responsive centered layout

---

## System Architecture

```
                +----------------------+
                |      User (UI)       |
                |   React Frontend     |
                +----------+-----------+
                           |
                           v
                +----------------------+
                |   Resume Upload API  |
                |    Spring Boot       |
                +----------+-----------+
                           |
                           v
                +----------------------+
                | Resume Text Extractor|
                |   (PDF Parsing)      |
                +----------+-----------+
                           |
                           v
                +----------------------+
                | Embedding Generator  |
                |   Semantic Vectors   |
                +----------+-----------+
                           |
                           v
                +----------------------+
                |   Job Import System  |
                |    Adzuna API        |
                +----------+-----------+
                           |
                           v
                +----------------------+
                | Job Database (MySQL) |
                +----------+-----------+
                           |
                           v
                +----------------------+
                | Matching Engine      |
                | Cosine Similarity    |
                +----------+-----------+
                           |
                +----------+-----------+
                |                      |
                v                      v
        +---------------+      +---------------+
        |   Web UI      |      |  Email Alerts |
        |  Job Results  |      |  Notification |
        +---------------+      +---------------+
```

# Tech Stack

## Backend
- Java
- Spring Boot
- Spring Scheduler
- Spring Mail
- REST APIs

## AI / Matching
- Text Embeddings
- Cosine Similarity

## Database
- MySQL

## Frontend
- React
- Vite
- Modern CSS Animations

## External APIs
- Adzuna Job API

---

## Project Structure

```
ai-job-matcher
│
├── ai-job-matching-ui
│   ├── public
│   ├── src
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   │
│   ├── package.json
│   └── vite.config.js
│
├── ai-job-matching
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── anuj
│           │           └── resume_ai_backend
│           │               ├── controller
│           │               │   ├── MatchController.java
│           │               │   └── ResumeController.java
│           │               │
│           │               ├── service
│           │               │   ├── AdzunaService.java
│           │               │   ├── EmailService.java
│           │               │   ├── JobService.java
│           │               │   ├── MatchingService.java
│           │               │   └── ResumeService.java
│           │               │
│           │               ├── repository
│           │               │   ├── JobRepository.java
│           │               │   └── ResumeRepository.java
│           │               │
│           │               ├── entity
│           │               │   ├── Job.java
│           │               │   └── Resume.java
│           │               │
│           │               ├── scheduler
│           │               │   └── JobImportScheduler.java
│           │               │
│           │               └── ai
│           │                   └── EmbeddingService.java
│           │
│           └── resources
│               └── application.properties
│
└── .gitignore
```

# How It Works

1. The user uploads a resume through the web interface.
2. The backend extracts the resume text.
3. The text is converted into a semantic embedding.
4. Jobs are periodically fetched from the Adzuna API.
5. Each job description is converted into an embedding.
6. The system computes similarity between resume and job embeddings.
7. Jobs are ranked by similarity score.
8. Results are shown on the web interface and sent to the user's email.

---

# Installation & Setup

## Clone the repository
git clone https://github.com/YOUR_USERNAME/ai-job-matcher.git⁠
cd ai-job-matcher

---
# Backend Setup
Navigate to the backend directory: cd ai-job-matching

# Configure database connection in `application.properties`:
spring.datasource.url=jdbc:mysql://localhost:3306/resume_ai spring.datasource.username=your_username spring.datasource.password=your_password

Run the backend:
mvn spring-boot:run

Backend runs on:
http://localhost:8080

---

# Frontend Setup

Navigate to the frontend directory:
cd ai-job-matching-ui

Install dependencies:
npm install

Run the frontend:
npm run dev

Frontend runs on:
http://localhost:5173

---

# Example Workflow

1. Enter your email address
2. Upload your resume
3. Click **Find Jobs**
4. AI analyzes resume and matches relevant jobs
5. Jobs appear ranked by similarity score
6. Top matches are emailed to you

---

# Example Output
Backend Developer — Match Score: 82%
Software Engineer — Match Score: 79%
Full Stack Developer — Match Score: 75%

---

# Future Improvements

- Skill gap analysis
- Salary filtering
- Location filtering
- Save favorite jobs
- Deploy to cloud infrastructure
- Support for multiple job APIs

---

# Author

Anuj Dutta  
Email: dattaanuj1804@gmail.com

---

# License

This project is open source and available under the MIT License.

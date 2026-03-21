# AI Resume Screening and Job Matching

AI-powered resume screening and job matching application with a Spring Boot backend and a React/Vite frontend.

The system accepts a resume, extracts its content, compares it against imported job listings, ranks the best matches, and emails the results to the candidate.

## Features

- Resume upload and parsing
- AI-based semantic job matching
- Adzuna job import and persistence
- Ranked job recommendations
- Email delivery of matched jobs through the Gmail API
- React frontend for upload and result display

## Tech Stack

### Backend
- Java 21
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Apache Tika
- Gmail API over HTTPS

### Frontend
- React 19
- Vite 7

### External APIs
- Adzuna Job API
- Gmail API

## Repository Structure

```text
ai-resume-screening-and-job-matching/
|-- ai-job-matching/
|   |-- src/main/java/com/anuj/resume_ai_backend/
|   |-- src/main/resources/application.properties
|   `-- pom.xml
|-- ai-job-matching-ui/
|   |-- src/
|   |-- public/
|   `-- package.json
`-- render.yaml
```

## How It Works

1. The user uploads a resume from the frontend.
2. The backend extracts the resume text.
3. The backend computes similarity between the resume and stored jobs.
4. Matched jobs are ranked by score.
5. The ranked results are returned to the UI.
6. The same results are emailed to the candidate through the Gmail API.

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/ai-resume-screening-and-job-matching.git
cd ai-resume-screening-and-job-matching
```

### 2. Backend setup

```bash
cd ai-job-matching
```

Required environment variables:

```properties
DB_URL=jdbc:postgresql://localhost:5432/ai_job_matcher
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
ADZUNA_APP_ID=your_adzuna_app_id
ADZUNA_APP_KEY=your_adzuna_app_key
MAIL_ENABLED=true
MAIL_PROVIDER=gmail-api
MAIL_FROM=yourgmail@gmail.com
GMAIL_CLIENT_ID=your_google_client_id
GMAIL_CLIENT_SECRET=your_google_client_secret
GMAIL_REFRESH_TOKEN=your_google_refresh_token
```

Optional email-related defaults:

```properties
GMAIL_TOKEN_URL=https://oauth2.googleapis.com/token
GMAIL_SEND_URL=https://gmail.googleapis.com/gmail/v1/users/me/messages/send
MAIL_CONNECTION_TIMEOUT_MS=5000
MAIL_READ_TIMEOUT_MS=10000
```

Run the backend:

```bash
./mvnw spring-boot:run
```

Backend default URL:

```text
http://localhost:8080
```

### 3. Frontend setup

```bash
cd ../ai-job-matching-ui
npm install
npm run dev
```

Frontend default URL:

```text
http://localhost:5173
```

## Gmail API Setup

This project sends email through the Gmail API instead of SMTP. That avoids outbound SMTP restrictions on free hosting platforms.

### 1. Enable Gmail API
- Open Google Cloud Console
- Create or select a project
- Enable `Gmail API`

### 2. Configure OAuth consent
- Create an `External` app
- Add your Gmail account as a test user

### 3. Create OAuth credentials
- Go to `APIs & Services` -> `Credentials`
- Create an `OAuth client ID`
- Choose `Web application`
- Add this redirect URI exactly:

```text
https://developers.google.com/oauthplayground
```

### 4. Get a refresh token
- Open OAuth 2.0 Playground
- Enable `Use your own OAuth credentials`
- Paste the client ID and client secret
- Request this scope:

```text
https://www.googleapis.com/auth/gmail.send
```

- Authorize with the Gmail account that will send mail
- Exchange the authorization code for tokens
- Copy the refresh token into `GMAIL_REFRESH_TOKEN`

`MAIL_FROM` should match the Gmail account used during authorization.

## Deployment Notes

The included [render.yaml](./render.yaml) is configured for Render deployment.

Important points:
- Email uses the Gmail API over HTTPS
- SMTP credentials are not used
- Set the Google OAuth environment variables in Render before deploying
- The backend service can stay on Render free because the Gmail API uses HTTPS instead of blocked SMTP ports

## Example Flow

1. Enter an email address in the frontend
2. Upload a resume
3. Trigger job matching
4. Review ranked jobs in the UI
5. Receive the same job matches by email

## API Behavior

When `/match/{resumeId}` is called:
- the backend computes job matches
- it attempts to send the email
- it returns the jobs in the response body
- it also returns email delivery information in response headers

## Future Improvements

- Skill gap analysis
- Salary filters
- Location filters
- Saved job lists
- Additional job providers

## Author

Anuj Dutta

## License

MIT

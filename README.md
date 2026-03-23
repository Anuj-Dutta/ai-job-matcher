# JobHunt

JobHunt is a full-stack resume-to-jobs application with a Spring Boot backend and a React/Vite frontend.

The app lets a user upload a resume, extracts profile signals from it, pulls in relevant jobs, ranks the strongest matches, shows them in the UI, and emails the shortlist to the user.

## What It Does

- Uploads and parses resumes
- Extracts skills and resume keywords
- Generates embeddings for semantic matching
- Imports a broad set of jobs from Adzuna
- Fetches resume-specific jobs before matching
- Ranks jobs with a hybrid scoring pipeline
- Emails the final shortlist through the Gmail API

## Current Matching Approach

JobHunt no longer relies only on a generic job pool plus pure cosine similarity.

The backend now:

1. Parses the resume text with Apache Tika
2. Extracts skills and high-signal keywords
3. Generates an embedding for semantic comparison
4. Imports general jobs for baseline coverage
5. Fetches targeted jobs based on the uploaded resume
6. Shortlists candidate jobs using keyword and skill overlap
7. Re-ranks candidates with a hybrid score built from:
   - embedding similarity
   - skill overlap
   - keyword overlap
   - title overlap
   - exact phrase boosts

This produces stronger matches and broader non-developer coverage than the earlier developer-heavy flow.

## Tech Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- PostgreSQL
- Apache Tika
- Jackson
- Gmail API

### Frontend

- React 19
- Vite 7
- CSS

### External Services

- Adzuna Job API
- Hugging Face inference router
- Gmail API

## Repository Structure

```text
ai-resume-screening-and-job-matching/
|-- ai-job-matching/        # Spring Boot backend
|-- ai-job-matching-ui/     # React frontend
|-- render.yaml             # Render deployment config
`-- README.md
```

## Main API Flow

### Resume Upload

`POST /resume/upload`

- accepts `email` and `file`
- parses the resume
- extracts skills
- generates an embedding
- stores the resume in PostgreSQL
- returns the saved resume with its ID

### Matching

`GET /match/{resumeId}`

- ensures jobs are ready
- fetches resume-driven jobs from Adzuna when needed
- scores and ranks matching jobs
- sends email with the shortlist
- returns matched jobs in the response body
- returns email delivery info in:
  - `X-Email-Status`
  - `X-Email-Message`

## Local Setup

### 1. Clone

```bash
git clone https://github.com/YOUR_USERNAME/ai-resume-screening-and-job-matching.git
cd ai-resume-screening-and-job-matching
```

### 2. Backend Setup

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
HF_TOKEN=your_huggingface_token
MAIL_ENABLED=true
MAIL_PROVIDER=gmail-api
MAIL_FROM=yourgmail@gmail.com
GMAIL_CLIENT_ID=your_google_client_id
GMAIL_CLIENT_SECRET=your_google_client_secret
GMAIL_REFRESH_TOKEN=your_google_refresh_token
```

Optional backend variables:

```properties
HF_EMBEDDING_API=https://router.huggingface.co/hf-inference/models/sentence-transformers/all-MiniLM-L6-v2/pipeline/feature-extraction
GMAIL_TOKEN_URL=https://oauth2.googleapis.com/token
GMAIL_SEND_URL=https://gmail.googleapis.com/gmail/v1/users/me/messages/send
MAIL_CONNECTION_TIMEOUT_MS=5000
MAIL_READ_TIMEOUT_MS=10000
JOBS_IMPORT_SCHEDULER_ENABLED=false
JOBS_IMPORT_SCHEDULER_FIXED_RATE_MS=3600000
JOBS_IMPORT_SCHEDULER_INITIAL_DELAY_MS=60000
```

Run the backend:

```bash
./mvnw spring-boot:run
```

Default backend URL:

```text
http://localhost:8080
```

### 3. Frontend Setup

```bash
cd ../ai-job-matching-ui
npm install
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

## Gmail API Setup

This project sends mail through Gmail API over HTTPS instead of SMTP.

### Steps

1. Create or select a Google Cloud project
2. Enable `Gmail API`
3. Configure OAuth consent
4. Create OAuth client credentials
5. Add this redirect URI:

```text
https://developers.google.com/oauthplayground
```

6. Use OAuth Playground to request:

```text
https://www.googleapis.com/auth/gmail.send
```

7. Exchange the code for tokens and copy the refresh token into `GMAIL_REFRESH_TOKEN`

`MAIL_FROM` must match the Gmail account used during authorization.

## Deployment

The included [render.yaml](/C:/Users/datta/Projects/ai-resume-screening-and-job-matching/render.yaml) is set up for Render:

- backend deploys as a Docker web service
- frontend deploys as a static site
- Gmail API works over HTTPS, which avoids SMTP restrictions on free hosting

Before deployment, set the required environment variables in Render.

## Notes

- The frontend currently points to a deployed backend URL in `App.jsx`
- Replace the placeholder GitHub URL in docs/footer with your actual repo URL
- Matching is much stronger than the original baseline, but it is still a synchronous request flow and not yet built for high concurrency

## Future Work

- Vector-native retrieval
- Asynchronous email and import workers
- Better match explanations
- More job providers
- Role/category classification
- Recruiter-side dashboard

## Author

Anuj Dutta

## License

MIT

# JobHunt UI

This is the React frontend for JobHunt.

It provides the user-facing flow for:

- entering an email address
- uploading a resume
- triggering job matching
- viewing ranked job cards
- seeing email delivery feedback

## Stack

- React 19
- Vite 7
- ESLint
- Custom CSS

## UI Behavior

The frontend:

1. uploads the resume to the backend
2. stores the returned resume ID
3. calls the match endpoint
4. renders ranked jobs with score bars
5. shows email delivery status from response headers

The page also includes:

- a theme toggle
- a short onboarding/instructions section before jobs are loaded
- footer metadata for developer contact, GitHub, and branding

## Scripts

Install dependencies:

```bash
npm install
```

Start development server:

```bash
npm run dev
```

Build production bundle:

```bash
npm run build
```

Preview production build:

```bash
npm run preview
```

Run linting:

```bash
npm run lint
```

## Development Notes

Default local URL:

```text
http://localhost:5173
```

This frontend expects the Spring Boot backend to be running separately.

The API base URL is currently set directly in:

- [src/App.jsx](/C:/Users/datta/Projects/ai-resume-screening-and-job-matching/ai-job-matching-ui/src/App.jsx)

For production-grade environment handling, that should eventually move to a Vite env variable.

## Related Files

- Root project README: [README.md](/C:/Users/datta/Projects/ai-resume-screening-and-job-matching/README.md)
- Backend app: [ai-job-matching](/C:/Users/datta/Projects/ai-resume-screening-and-job-matching/ai-job-matching)

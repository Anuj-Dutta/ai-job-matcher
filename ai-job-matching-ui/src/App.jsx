import { useState } from 'react';

const API_BASE_URL = 'https://ai-job-matcher-5ea8.onrender.com';

function App() {
    const [file, setFile] = useState(null);
    const [email, setEmail] = useState('');
    const [resumeId, setResumeId] = useState(null);
    const [jobs, setJobs] = useState([]);
    const [emailFeedback, setEmailFeedback] = useState(null);
    const [dark, setDark] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [matching, setMatching] = useState(false);

    function toggleTheme() {
        setDark((current) => {
            const next = !current;

            if (next) {
                document.body.classList.add('dark');
            } else {
                document.body.classList.remove('dark');
            }

            return next;
        });
    }

    async function uploadResume() {
        if (!file || !email) {
            alert('Please select a resume and enter an email address.');
            return;
        }

        setUploading(true);

        try {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('email', email);

            const response = await fetch(`${API_BASE_URL}/resume/upload`, {
                method: 'POST',
                body: formData,
            });

            if (!response.ok) {
                throw new Error('Resume upload failed.');
            }

            const data = await response.json();
            setResumeId(data.id);
            setEmailFeedback(null);
        } catch (error) {
            alert(error.message);
        } finally {
            setUploading(false);
        }
    }

    async function matchJobs() {
        if (!resumeId) {
            return;
        }

        setMatching(true);

        try {
            const response = await fetch(`${API_BASE_URL}/match/${resumeId}`);
            if (!response.ok) {
                throw new Error('Job matching failed.');
            }

            const emailStatus = response.headers.get('X-Email-Status');
            const emailMessage = response.headers.get('X-Email-Message');
            const data = await response.json();
            const sortedJobs = data.sort((a, b) => b.matchScore - a.matchScore);

            setJobs(sortedJobs);
            setEmailFeedback(
                emailStatus && emailMessage
                    ? { status: emailStatus, message: emailMessage }
                    : null
            );
        } catch (error) {
            alert(error.message);
        } finally {
            setMatching(false);
        }
    }

    return (
        <div className="container">
            <h1 className="title">Job Matching AI</h1>

            <div className="themeRow">
                <span>Light</span>

                <label className="switch">
                    <input type="checkbox" checked={dark} onChange={toggleTheme} />
                    <span className="slider" />
                </label>

                <span>Dark</span>
            </div>

            <div className="card">
                <input
                    className="input"
                    type="email"
                    placeholder="Enter your email"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                />

                <input type="file" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />

                <div className="buttonStack">
                    <button className="button" onClick={uploadResume}>
                        Upload Resume
                    </button>

                    {resumeId && (
                        <button className="button" onClick={matchJobs}>
                            Find Jobs
                        </button>
                    )}
                </div>

                {uploading && (
                    <div className="loaderContainer">
                        <div className="spinner" />
                        <p>Uploading resume...</p>
                    </div>
                )}

                {matching && (
                    <div className="loaderContainer">
                        <div className="spinner" />
                        <p>AI is matching jobs...</p>
                    </div>
                )}

                {emailFeedback && (
                    <p className={`emailFeedback emailFeedback-${emailFeedback.status}`}>
                        {emailFeedback.message}
                    </p>
                )}
            </div>

            {jobs.map((job, index) => (
                <div key={`${job.applyLink}-${index}`} className={`jobCard ${index === 0 ? 'topMatch' : ''}`}>
                    <div className="jobTitle">
                        {job.title}
                        {index === 0 && (
                            <span className="topMatchLabel">
                                Top Match
                            </span>
                        )}
                    </div>

                    <div className="company">
                        {job.company} at {job.location}
                    </div>

                    <div className="score">Match Score: {(job.matchScore * 100).toFixed(1)}%</div>

                    <div className="scoreBarContainer">
                        <div className="scoreBar" style={{ width: `${job.matchScore * 100}%` }} />
                    </div>

                    <a className="applyBtn" href={job.applyLink} target="_blank" rel="noreferrer">
                        Apply -&gt;
                    </a>
                </div>
            ))}

            <div className="footer">
                <p>
                    <strong>Anuj Dutta</strong>
                </p>
                <p>Email: dattaanuj1804@gmail.com</p>
                <p>AI Resume Job Matcher</p>
            </div>

            <div className="watermark">Anuj Dutta | AI Job Matcher</div>
        </div>
    );
}

export default App;

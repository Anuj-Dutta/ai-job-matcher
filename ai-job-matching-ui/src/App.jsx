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
            <div className="topRightControls">
                <div className="themeRow">
                    <span>Light</span>

                    <label className="switch">
                        <input type="checkbox" checked={dark} onChange={toggleTheme} />
                        <span className="slider" />
                    </label>

                    <span>Dark</span>
                </div>
            </div>

            <div className="brandLockup">
                <img
                    className="brandLogo"
                    src="/jobhunt-logo-alt-3.svg"
                    alt="JobHunt logo"
                />
                <h1 className="title">JobHunt</h1>
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

            {jobs.length === 0 && (
                <div className="instructionsCard">
                    <div className="instructionsHeader">
                        <div>
                            <h2 className="instructionsTitle">How To Use JobHunt</h2>
                            <p className="instructionsSubtitle">A fast workflow from resume upload to ranked opportunities.</p>
                        </div>

                        <div className="instructionsIllustrations" aria-hidden="true">
                            <div className="illustrationCard">
                                <div className="illustrationResume">
                                    <span className="illustrationLine lineShort" />
                                    <span className="illustrationLine" />
                                    <span className="illustrationLine" />
                                </div>
                            </div>
                            <div className="illustrationPulse" />
                            <div className="illustrationCard accentCard">
                                <div className="illustrationBars">
                                    <span className="bar barOne" />
                                    <span className="bar barTwo" />
                                    <span className="bar barThree" />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="instructionGrid">
                        <div className="instructionStep">
                            <div className="instructionBadge">01</div>
                            <div>
                                <h3 className="instructionHeading">Upload Your Profile</h3>
                                <p className="instructionItem">Enter your email and upload your latest resume in PDF or DOC/DOCX format.</p>
                            </div>
                        </div>

                        <div className="instructionStep">
                            <div className="instructionBadge">02</div>
                            <div>
                                <h3 className="instructionHeading">Let The Engine Parse</h3>
                                <p className="instructionItem">JobHunt extracts skills, builds your profile, and prepares targeted job retrieval.</p>
                            </div>
                        </div>

                        <div className="instructionStep">
                            <div className="instructionBadge">03</div>
                            <div>
                                <h3 className="instructionHeading">Review Strong Matches</h3>
                                <p className="instructionItem">Click `Find Jobs` to get ranked opportunities, open the apply links, and check your email for the same shortlist.</p>
                            </div>
                        </div>
                    </div>
                </div>
            )}

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
                <p>JobHunt</p>
                <p>Developer Contact : dattaanuj1804@gmail.com</p>
                <p>
                    <a
                        className="footerLink"
                        href="https://github.com/Anuj-Dutta/ai-job-matcher"
                        target="_blank"
                        rel="noreferrer"
                    >
                        GitHub Project
                    </a>
                </p>
                <p className="watermark">Made with heart in India</p>
            </div>
        </div>
    );
}

export default App;

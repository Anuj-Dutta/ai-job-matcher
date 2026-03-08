import { useState } from "react";

function App(){

const [file,setFile]=useState(null)
const [email,setEmail]=useState("")
const [resumeId,setResumeId]=useState(null)
const [jobs,setJobs]=useState([])
const [dark,setDark]=useState(false)
const [uploading,setUploading]=useState(false)
const [matching,setMatching]=useState(false)

function toggleTheme(){
setDark(!dark)

if(!dark){
document.body.classList.add("dark")
}else{
document.body.classList.remove("dark")
}
}

async function uploadResume(){

if(!file || !email){
alert("Please select a resume and enter email")
return
}

setUploading(true)

const formData=new FormData()
formData.append("file",file)
formData.append("email",email)

const res=await fetch("https://ai-job-matching-backend.onrender.com/resume/upload",{
method:"POST",
body:formData
})

const data=await res.json()

setResumeId(data.id)

setUploading(false)

}
async function matchJobs(){

setMatching(true)

const res=await fetch(`https://ai-job-matching-backend.onrender.com/match/${resumeId}`)

const data=await res.json()

const sorted=data.sort((a,b)=>b.matchScore-a.matchScore)

setJobs(sorted)

setMatching(false)

}
return(

<div className="container">

<h1 className="title">Job Matching AI</h1>

<div className="themeRow">

<span>🌞</span>

<label className="switch">
<input
type="checkbox"
checked={dark}
onChange={toggleTheme}
/>
<span className="slider"></span>
</label>

<span>🌙</span>

</div>

<div className="card">

<input
className="input"
type="email"
placeholder="Enter your email"
value={email}
onChange={(e)=>setEmail(e.target.value)}
/>

<input
type="file"
onChange={(e)=>setFile(e.target.files[0])}
/>

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

<div className="spinner"></div>
<p>Uploading resume...</p>

</div>

)}

{matching && (

<div className="loaderContainer">

<div className="spinner"></div>
<p>AI is matching jobs...</p>

</div>

)}

</div>

{jobs.map((job,i)=>(

<div
key={i}
className={`jobCard ${i===0?"topMatch":""}`}
>

<div className="jobTitle">
{job.title}
{i===0 && <span style={{color:"#f59e0b",marginLeft:"8px"}}>Top Match</span>}
</div>

<div className="company">
{job.company} • {job.location}
</div>

<div className="score">
Match Score: {(job.matchScore*100).toFixed(1)}%
</div>

<div className="scoreBarContainer">
<div
className="scoreBar"
style={{width:`${job.matchScore*100}%`}}
></div>
</div>

<a
className="applyBtn"
href={job.applyLink}
target="_blank"
>
Apply →
</a>

</div>

))}

<div className="footer">

<p><strong>Anuj Dutta</strong></p>
<p>Email: dattaanuj1804@gmail.com</p>
<p>AI Resume Job Matcher</p>

</div>

<div className="watermark">
Anuj Dutta • AI Job Matcher
</div>

</div>

)

}

export default App
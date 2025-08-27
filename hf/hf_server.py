from fastapi import FastAPI, Request
from transformers import pipeline

app = FastAPI()

summarizer = pipeline("summarization", model="sshleifer/distilbart-cnn-12-6")

@app.post("/analyze")
async def analyze(request: Request):
    data = await request.json()
    content = data.get("content", "")
    ftype = data.get("ftype", "unknown")
    summary = summarizer(content[:2000])
    return {"type": ftype, "summary": summary[0]["summary_text"]}

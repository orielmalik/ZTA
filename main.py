from contextlib import asynccontextmanager
from typing import List

import uvicorn
from fastapi import FastAPI, UploadFile
from pydantic import BaseModel
from transformers import pipeline
from Utils.FileUtils import process_file
from KafkaIO import KafkaProducerService
from Utils.Consts import *
from Utils.SecurityUtils import *


class Resource(BaseModel):
    id: str
    resources: List[str]


summarizer = pipeline('summarization', model='sshleifer/distilbart-cnn-12-6')
producer = KafkaProducerService(bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS, topic_name=TOPIC_CREATE)


@asynccontextmanager
async def lifespan(app: FastAPI):
    await producer.start_producer()
    yield
    await producer.stop_producer()


app = FastAPI(lifespan=lifespan)


@app.post("/upload")
async def upload_file(file: UploadFile):
    processed = await process_file(file)
    text = processed.get("text", "")[:2000]
    summary = summarizer(text)[0]['summary_text']
    return {"filename": processed.get("filename"), "summary": summary}


@app.put("/resource")
async def clickedResources(resor: Resource):
    await producer.sendProduce(resor)
    return {"res": resor.id}


if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8088, reload=True)

# main.py
import asyncio
import uvicorn
from Controllers.KafkaIO import KafkaConsumerService
from Utils.Consts import *
from Utils.FileUtils import *

kafka_AuthConservice = KafkaConsumerService(KAFKA_BOOTSTRAP_SERVERS,
                                            TOPIC_CREATE, GROUP_ID)
kafka_ResourceConservice = KafkaConsumerService(KAFKA_BOOTSTRAP_SERVERS,
                                                TOPIC_RESOURCE, GROUP_ID)

app = FastAPI()


async def start_Consumers():
    await kafka_AuthConservice.start_consumer()
    await kafka_ResourceConservice.start_consumer()
    asyncio.create_task(kafka_AuthConservice.consume_Authmessages())  # Background consumer
    asyncio.create_task(kafka_ResourceConservice.consumeResource())  # Background consumer


@app.on_event("startup")
async def startup_event():
    docker_compose_up_all()
    await start_Consumers()


@app.on_event("shutdown")
async def shutdown_event():
    docker_compose_stop_all()
    await kafka_AuthConservice.stop()
    await kafka_ResourceConservice.stop()


if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8081, reload=True)

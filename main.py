# main.py
import asyncio
import uvicorn
from Controllers.KafkaIO import KafkaConsumerService, KafkaProducerService
from Utils.Consts import *
from Utils.FileUtils import *
from Controllers.AppSetting import *
kafka_AuthConservice = KafkaConsumerService(KAFKA_BOOTSTRAP_SERVERS,
                                            TOPIC_CREATE, GROUP_ID)
kafka_ResourceConservice = KafkaConsumerService(KAFKA_BOOTSTRAP_SERVERS,
                                                TOPIC_RESOURCE, GROUP_ID)
producer = KafkaProducerService(bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS, topic_name=TOPIC_RESOURCE)

app = FastAPI()
add_middleware(app)

async def start_Consumers():
    await kafka_AuthConservice.start_consumer()
    await kafka_ResourceConservice.start_consumer()
    await producer.start_producer()
    asyncio.create_task(kafka_AuthConservice.consume_Authmessages())  # Background consumer
    asyncio.create_task(kafka_ResourceConservice.consume_and_forward(producer))  # Background consumer


@app.on_event("startup")
async def startup_event():
    await start_Consumers()



@app.on_event("shutdown")
async def shutdown_event():
    await kafka_AuthConservice.stop()
    await kafka_ResourceConservice.stop()
    await producer.stop_producer()


#spend fastapi adventages,without reveal http port
if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8081, reload=True)

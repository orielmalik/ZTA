# kafka_io.py
import asyncio
import json
from aiokafka import AIOKafkaProducer, AIOKafkaConsumer

from Requests.Logger import printerAnnotion, printer
from Utils.Consts import *
from Utils.SecurityUtils import *
from Utils.ValidationUtils import isJWT
from Utils.ValidationUtils import *


class KafkaBase:
    def __init__(self, bootstrap_servers: str, topic_name: str, group_id: str = None):
        self.bootstrap_servers = bootstrap_servers
        self.topic_name = topic_name
        self.group_id = group_id
        self.loop = asyncio.get_event_loop()


class KafkaProducerService(KafkaBase):
    def __init__(self, bootstrap_servers: str, topic_name: str):
        super().__init__(bootstrap_servers, topic_name)
        self.producer: AIOKafkaProducer | None = None

    async def start_producer(self):
        self.producer = AIOKafkaProducer(
            bootstrap_servers=self.bootstrap_servers,
            loop=self.loop
        )
        await self.producer.start()
        await self.producer.send_and_wait(self.topic_name)

    async def sendProduce(self, message):
        return await self.producer.send_and_wait(self.topic_name, message)

    async def stop_producer(self):
        if self.producer:
            await self.producer.stop()

    async def send_message(self, value: dict):
        if not self.producer:
            await self.start_producer()
        value_str = json.dumps(value)
        await self.producer.send_and_wait(self.topic_name, value_str.encode())


async def validateMsg(s):
    if isJWT(s):
        await printer(f"[KafkaConsumerService] Received: {decode_message(s, jwtsecret)}", "DEBUG")
        s = decode_message(s, jwtsecret)
        print(s)
    else:
        await printer(f"[KafkaConsumerService] Received: {s}")


class KafkaConsumerService(KafkaBase):
    def __init__(self, bootstrap_servers: str, topic_name: str, group_id: str):
        super().__init__(bootstrap_servers, topic_name, group_id)
        self.consumer: AIOKafkaConsumer | None = None

    async def start_consumer(self):
        self.consumer = AIOKafkaConsumer(
            self.topic_name,
            bootstrap_servers=self.bootstrap_servers,
            group_id=self.group_id,
            auto_offset_reset="earliest",
            loop=self.loop
        )
        await self.consumer.start()

    async def stop(self):
        if self.consumer:
            await self.consumer.stop()

    async def consume_messages(self):
        if not self.consumer:
            await self.start_consumer()
        try:
            async for msg in self.consumer:
                await self.crudOps(msg.value.decode())

        except Exception as e:
            await printer(f"[KafkaConsumerService] Consumer error: {e}", "ERROR")

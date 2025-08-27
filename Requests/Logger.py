import functools

import httpx
import asyncio


async def printer(message, typ):
    async with httpx.AsyncClient() as client:
        await client.put("http://localhost:8088/write", params={"message": message, "type": typ})



def printerAnnotion(typ):
    def decorator(func):
        @functools.wraps(func)
        async def wrapper(*args, **kwargs):
            try:
                result = await func(*args, **kwargs)
                await printer(result, typ)
                return result
            except Exception as e:
                await printer(str(e), "ERROR")
                raise
        return wrapper
    return decorator
from functools import wraps

import asyncpg
import subprocess


def run_docker_compose_services_async(compose_file='docker-compose.yml'):
    try:
        subprocess.Popen(['docker-compose', '-f', compose_file, 'up', '-d'])
        print("Docker services starting in background.")
    except Exception as e:
        print("Error starting Docker Compose:", e)


def stop_docker_compose_services_async(compose_file='docker-compose.yml'):
    try:
        subprocess.Popen(['docker-compose', '-f', compose_file, 'down'])
        print("Docker services stopping in background.")
    except Exception as e:
        print("Error stopping Docker Compose:", e)


class ConnectionInterface:
    def __init__(self):
        self.conn: asyncpg.connection = None

    async def connect_to_db(self):
        self.conn = await asyncpg.connect(
            user='navigator',
            password='navigator',
            database='policy_db',
            host='localhost',
            port=5433
        )

    def try_catch_wrapper(func):
        @wraps(func)
        async def wrapper(*args, **kwargs):
            try:
                return await func(*args, **kwargs)
            except asyncpg.PostgresError as e:
                #logger.erro()
                raise
            except Exception as e:
                #logger.erro()
                raise

        return wrapper

    async def disconnect(self):
        self.conn.close()

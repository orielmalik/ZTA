from fastapi.middleware.cors import CORSMiddleware

from Utils.Consts import origins


def add_middleware(app):
    app.add_middleware(
        CORSMiddleware,
        allow_origins=origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"], )

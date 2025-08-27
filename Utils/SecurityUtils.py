import jwt
import time

import re
import base64


def encode_message(message, secret, alg='HS256'):
    payload = {
        'message': message,
        'exp': int(time.time()) + 600
    }
    token = jwt.encode(payload, secret, algorithm=alg)
    return token


def decode_message(token, secret):
    try:
        decoded = jwt.decode(token, secret, algorithms=['HS256'])
        return decoded['message']
    except jwt.ExpiredSignatureError:
        return "Token has expired"
    except jwt.InvalidTokenError:
        return "Invalid token"

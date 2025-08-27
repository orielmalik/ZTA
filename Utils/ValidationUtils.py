import base64
import re

def add_padding(b64_string: str) -> str:
    return b64_string + '=' * (-len(b64_string) % 4)

def isJWT(token: str) -> bool:
    if not re.match(r'^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$', token):
        return False
    try:
        header_b64, payload_b64, _ = token.split(".")
        base64.urlsafe_b64decode(add_padding(header_b64))
        base64.urlsafe_b64decode(add_padding(payload_b64))
        return True
    except Exception as e:
        print("❌ decode failed:", e)
        return False


def dict_to_tuple(d: dict, fields_order=None):
    if fields_order:
        values = []
        for field in fields_order:
            val = d.get(field)
            values.append(val)
    else:
        values = list(d.values())
    return tuple(values)
import subprocess

import requests
import os

OLLAMA_HOST = "http://localhost:11434"
def run_hf_cli(text: str, model: str = "sshleifer/distilbart-cnn-12-6") -> str:
    python_code = (
        f"from transformers import pipeline; "
        f"nlp = pipeline('summarization', model='{model}'); "
        f"print(nlp('''{text}''')[0]['summary_text'])"
    )
    result = subprocess.run(
        ["docker", "exec", "-i", "hf_container", "python3", "-c", python_code],
        capture_output=True,
        text=True
    )
    if result.returncode != 0:
        return result.stderr.strip()
    return result.stdout.strip()


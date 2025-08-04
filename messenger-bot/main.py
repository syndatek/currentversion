from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
import requests
import os
from dotenv import load_dotenv

load_dotenv()

app = FastAPI()  # ✅ THIS IS REQUIRED

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def home():
    return {"message": "Messenger Bot API is running ✅"}

@app.post("/send-messenger")
async def send_messenger(request: Request):
    data = await request.json()
    mac_id = data.get("mac_id")
    timestamp = data.get("timestamp")

    token = os.getenv("FB_PAGE_TOKEN")
    recipient = os.getenv("FB_RECIPIENT_ID")

    if not token or not recipient:
        return {"error": "Missing Facebook credentials in .env"}

    message = {
        "messaging_type": "RESPONSE",
        "recipient": {"id": recipient},
        "message": {"text": f"📡 ECG Recording Started\nMAC ID: {mac_id}\nTimestamp: {timestamp}"}
    }

    response = requests.post(
        f"https://graph.facebook.com/v18.0/me/messages?access_token={token}",
        json=message
    )

    return {"status": response.status_code, "response": response.json()}

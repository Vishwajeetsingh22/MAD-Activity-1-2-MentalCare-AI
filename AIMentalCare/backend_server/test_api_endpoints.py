"""
Test Flask /chat endpoint directly using Flask test client (in-memory)
"""
from app import app
import json

def test_flask_chat():
    client = app.test_client()

    # 1. Empty message -> 400 Bad Request
    res = client.post('/chat', json={"message": "", "conversation_id": "test_empty"})
    assert res.status_code == 400, f"Expected 400, got {res.status_code}"
    data = json.loads(res.data)
    assert "error" in data, "Expected error in response"
    print("Flask Test 1: Empty message returned 400 Bad Request as required.")

    # 2. Normal conversation message -> 200 OK
    res = client.post('/chat', json={"message": "I'm feeling stressed today.", "conversation_id": "test_api_conv"})
    assert res.status_code == 200, f"Expected 200, got {res.status_code}"
    data = json.loads(res.data)
    assert "reply" in data and len(data["reply"]) > 0
    assert data["conversation_id"] == "test_api_conv"
    print(f"Flask Test 2: Valid message returned 200 OK with reply: {data['reply'][:60]}...")

    # 3. Follow-up "Yes ask me questions" -> 200 OK
    res2 = client.post('/chat', json={"message": "Yes ask me questions.", "conversation_id": "test_api_conv"})
    assert res2.status_code == 200
    data2 = json.loads(res2.data)
    assert data2["reply"] != data["reply"], "Reply must not repeat the previous turn!"
    print(f"Flask Test 3: Follow-up returned distinct response: {data2['reply'][:60]}...")

    print("ALL FLASK API ENDPOINT TESTS PASSED!")

if __name__ == "__main__":
    test_flask_chat()

"""
Test suite for Daily Wellness Quiz Backend Endpoints
Verifies:
1. First daily submission creates record (201 Created)
2. Duplicate same-day submission returns 200 with already_completed: true
3. Next-day submission is allowed
4. Multi-user isolation
5. Concurrent requests safety
"""

import sys
import os
import unittest
from concurrent.futures import ThreadPoolExecutor

sys.path.insert(0, os.path.dirname(__file__))
from app import app, get_quiz_db, QUIZ_DB_PATH


class DailyQuizTestCase(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()
        # Clean test table entries
        with get_quiz_db() as conn:
            conn.execute("DELETE FROM daily_quizzes WHERE user_id LIKE 'test_%'")
            conn.commit()

    def tearDown(self):
        with get_quiz_db() as conn:
            conn.execute("DELETE FROM daily_quizzes WHERE user_id LIKE 'test_%'")
            conn.commit()

    def test_first_submission_succeeds(self):
        payload = {
            "user_id": "test_user_1",
            "local_date": "2026-10-01",
            "timezone": "Asia/Kolkata",
            "answers": [3, 2, 4, 3, 2, 4, 3, 2, 4, 3],
            "stress_score": 45,
            "stress_level": "MODERATE",
            "recommendation": "Take a 5-minute breathing break."
        }
        resp = self.client.post("/api/quiz/submit", json=payload)
        self.assertEqual(resp.status_code, 201)
        data = resp.get_json()
        self.assertFalse(data["already_completed"])
        self.assertEqual(data["daily_record_id"], "test_user_1_2026-10-01")

    def test_duplicate_same_day_submission_blocked(self):
        payload = {
            "user_id": "test_user_2",
            "local_date": "2026-10-01",
            "timezone": "Asia/Kolkata",
            "answers": [1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
            "stress_score": 25,
            "stress_level": "LOW",
            "recommendation": "Great job maintaining low stress."
        }
        # First submission
        resp1 = self.client.post("/api/quiz/submit", json=payload)
        self.assertEqual(resp1.status_code, 201)

        # Second submission on SAME day with different score to test no override
        payload_dup = dict(payload)
        payload_dup["stress_score"] = 90
        payload_dup["stress_level"] = "HIGH"
        resp2 = self.client.post("/api/quiz/submit", json=payload_dup)
        self.assertEqual(resp2.status_code, 200)
        data2 = resp2.get_json()
        self.assertTrue(data2["already_completed"])
        self.assertIn("completed your wellness check-in for today", data2["message"])
        # Original score preserved
        self.assertEqual(data2["result"]["stress_score"], 25)
        self.assertEqual(data2["result"]["stress_level"], "LOW")

        # Verify only 1 entry in DB
        with get_quiz_db() as conn:
            cur = conn.cursor()
            cur.execute("SELECT COUNT(*) as cnt FROM daily_quizzes WHERE user_id = 'test_user_2'")
            count = cur.fetchone()["cnt"]
            self.assertEqual(count, 1)

    def test_next_day_submission_allowed(self):
        payload_day1 = {
            "user_id": "test_user_3",
            "local_date": "2026-10-01",
            "timezone": "Asia/Kolkata",
            "answers": [3]*10,
            "stress_score": 50,
            "stress_level": "MODERATE",
            "recommendation": "Moderate routine"
        }
        resp1 = self.client.post("/api/quiz/submit", json=payload_day1)
        self.assertEqual(resp1.status_code, 201)

        payload_day2 = dict(payload_day1)
        payload_day2["local_date"] = "2026-10-02"
        payload_day2["stress_score"] = 35
        payload_day2["stress_level"] = "LOW"
        resp2 = self.client.post("/api/quiz/submit", json=payload_day2)
        self.assertEqual(resp2.status_code, 201)
        self.assertFalse(resp2.get_json()["already_completed"])

        with get_quiz_db() as conn:
            cur = conn.cursor()
            cur.execute("SELECT COUNT(*) as cnt FROM daily_quizzes WHERE user_id = 'test_user_3'")
            count = cur.fetchone()["cnt"]
            self.assertEqual(count, 2)

    def test_status_endpoint(self):
        status_before = self.client.get("/api/quiz/status?user_id=test_user_4&local_date=2026-10-01")
        self.assertEqual(status_before.status_code, 200)
        self.assertFalse(status_before.get_json()["already_completed"])

        self.client.post("/api/quiz/submit", json={
            "user_id": "test_user_4",
            "local_date": "2026-10-01",
            "timezone": "Asia/Kolkata",
            "answers": [2]*10,
            "stress_score": 40,
            "stress_level": "LOW",
            "recommendation": "Balanced day"
        })

        status_after = self.client.get("/api/quiz/status?user_id=test_user_4&local_date=2026-10-01")
        self.assertEqual(status_after.status_code, 200)
        data = status_after.get_json()
        self.assertTrue(data["already_completed"])
        self.assertEqual(data["result"]["stress_score"], 40)

    def test_concurrent_submissions(self):
        payload = {
            "user_id": "test_user_concurrent",
            "local_date": "2026-10-01",
            "timezone": "Asia/Kolkata",
            "answers": [3]*10,
            "stress_score": 55,
            "stress_level": "MODERATE",
            "recommendation": "Take rest"
        }

        def submit():
            with app.test_client() as c:
                return c.post("/api/quiz/submit", json=payload).status_code

        with ThreadPoolExecutor(max_workers=5) as executor:
            results = list(executor.map(lambda _: submit(), range(5)))

        # Exactly one request must return 201, and 4 must return 200
        self.assertEqual(results.count(201), 1)
        self.assertEqual(results.count(200), 4)

        with get_quiz_db() as conn:
            cur = conn.cursor()
            cur.execute("SELECT COUNT(*) as cnt FROM daily_quizzes WHERE user_id = 'test_user_concurrent'")
            self.assertEqual(cur.fetchone()["cnt"], 1)


if __name__ == "__main__":
    unittest.main()


"""
MentalCare AI - Flask REST API
Public deployment configuration for Render.
"""

import os

from flask import Flask, request, jsonify
from flask_cors import CORS

from nlp_engine import NLPEngine
from chat_engine import ChatEngine


app = Flask(__name__)
CORS(app)

# Initialize AI engines
engine = NLPEngine()
chat_engine = ChatEngine()


# --------------------------------------------------
# HOME PAGE
# --------------------------------------------------
@app.route("/", methods=["GET"])
def home():
    return jsonify({
        "app_name": "MentalCare AI",
        "status": "online",
        "message": "Welcome to MentalCare AI Backend",
        "version": "2.0.0",
        "endpoints": {
            "home": "/",
            "health": "/health",
            "predict": "/predict",
            "checkin": "/checkin",
            "analyze_text": "/api/analyze_text",
            "analyze_burnout": "/api/analyze_burnout",
            "chat": "/chat",
            "api_chat": "/api/chat"
        }
    }), 200


# --------------------------------------------------
# HEALTH CHECK
# --------------------------------------------------
@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "ok",
        "app_name": "MentalCare AI",
        "tagline": "Understand. Relax. Thrive.",
        "version": "2.0.0"
    }), 200


# --------------------------------------------------
# STRESS PREDICTION / TEXT ANALYSIS
# --------------------------------------------------
@app.route("/predict", methods=["POST"])
@app.route("/checkin", methods=["POST"])
@app.route("/api/analyze_text", methods=["POST"])
def analyze_text():
    try:
        data = request.get_json(silent=True) or {}

        text = data.get("text") or data.get("journal", "")
        sleep_hrs = data.get("sleep_hrs")
        work_hrs = data.get("work_hrs")

        if not isinstance(text, str) or not text.strip():
            return jsonify({
                "error": "Text parameter is required"
            }), 400

        result = engine.analyze(
            text.strip(),
            sleep_hrs=sleep_hrs,
            work_hrs=work_hrs
        )

        if not isinstance(result, dict):
            raise ValueError("Invalid NLP engine response")

        result = dict(result)

        result["stress_level"] = result.get(
            "stressLevel",
            result.get("stress_level", "LOW")
        )

        confidence = float(result.get("confidence", 90.0))

        if confidence > 1:
            confidence /= 100.0

        result["confidence"] = round(
            max(0.0, min(confidence, 1.0)), 2
        )

        return jsonify(result), 200

    except (TypeError, ValueError) as exc:
        app.logger.warning("Invalid analysis input: %s", exc)
        return jsonify({
            "error": "Invalid analysis input"
        }), 400

    except Exception:
        app.logger.exception("Text analysis failed")
        return jsonify({
            "error": "Failed to analyze text"
        }), 500


# --------------------------------------------------
# BURNOUT TREND ANALYSIS
# --------------------------------------------------
@app.route("/api/analyze_burnout", methods=["POST"])
def analyze_burnout():
    data = request.get_json(silent=True) or {}
    scores = data.get("scores", [])

    if not isinstance(scores, list):
        return jsonify({
            "error": "scores must be a list of numbers"
        }), 400

    try:
        scores = [float(score) for score in scores]
    except (TypeError, ValueError):
        return jsonify({
            "error": "All scores must be numeric"
        }), 400

    if any(not 0 <= score <= 100 for score in scores):
        return jsonify({
            "error": "Scores must be between 0 and 100"
        }), 400

    if len(scores) < 3:
        return jsonify({
            "burnout_risk": "LOW",
            "is_burnout_pattern": False,
            "trend_message": (
                "Insufficient check-in data to detect "
                "a multi-day burnout pattern."
            ),
            "average_score": round(
                sum(scores) / len(scores), 1
            ) if scores else 0
        }), 200

    recent = scores[-5:]

    is_increasing = all(
        recent[i] <= recent[i + 1]
        for i in range(len(recent) - 1)
    )

    avg_score = sum(recent) / len(recent)

    is_burnout = (
        (is_increasing and len(recent) >= 4 and avg_score > 58)
        or avg_score >= 75
    )

    if is_burnout:
        risk = "HIGH"
        message = (
            f"Increasing stress pattern detected. "
            f"Average score over recent days: {avg_score:.0f}%. "
            "Consider prioritizing rest and seeking support."
        )
        recommendations = [
            "Take restorative breaks from work or study.",
            "Discuss workload adjustments where appropriate.",
            "Consider speaking with a qualified wellness professional."
        ]

    elif avg_score > 48:
        risk = "MODERATE"
        message = (
            f"Moderate stress trajectory "
            f"(average {avg_score:.0f}%)."
        )
        recommendations = [
            "Maintain a consistent sleep routine.",
            "Schedule daily relaxation breaks."
        ]

    else:
        risk = "LOW"
        message = (
            f"Lower recent stress scores "
            f"(average {avg_score:.0f}%)."
        )
        recommendations = [
            "Continue your healthy daily wellness routine."
        ]

    return jsonify({
        "burnout_risk": risk,
        "is_burnout_pattern": is_burnout,
        "trend_message": message,
        "average_score": round(avg_score, 1),
        "recent_scores": recent,
        "recommendations": recommendations
    }), 200


# --------------------------------------------------
# AI WELLNESS CHAT
# --------------------------------------------------
@app.route("/chat", methods=["POST"])
@app.route("/api/chat", methods=["POST"])
def chat():
    try:
        data = request.get_json(silent=True) or {}

        message = data.get("message") or data.get("text", "")

        if not isinstance(message, str) or not message.strip():
            return jsonify({
                "error": "Empty message",
                "reply": "Please enter a message to share how you are feeling."
            }), 400

        user_id = data.get("user_id", "default_user")
        stress_level = (
            data.get("current_stress_level")
            or data.get("stress_level")
        )
        conversation_id = data.get("conversation_id")

        # Do not log sensitive message contents.
        app.logger.info(
            "Chat request received; message_length=%s",
            len(message.strip())
        )

        response = chat_engine.respond(
            message.strip(),
            user_id=user_id,
            conversation_id=conversation_id,
            current_stress_level=stress_level
        )

        if not isinstance(response, dict):
            raise ValueError("Invalid chat engine response")

        return jsonify(response), 200

    except Exception:
        app.logger.exception("Chat request failed")
        return jsonify({
            "error": "Failed to process chat message",
            "reply": (
                "An issue occurred while processing your message. "
                "Please try again."
            ),
            "is_crisis": False
        }), 500


# --------------------------------------------------
# DAILY WELLNESS QUIZ API (ONCE-PER-DAY ENFORCEMENT)
# --------------------------------------------------
import sqlite3
import json

QUIZ_DB_PATH = os.path.join(os.path.dirname(__file__), "quiz_records.db")

def get_quiz_db():
    conn = sqlite3.connect(QUIZ_DB_PATH, timeout=10.0)
    conn.row_factory = sqlite3.Row
    return conn

def init_quiz_db():
    with get_quiz_db() as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS daily_quizzes (
                daily_record_id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                local_date TEXT NOT NULL,
                timezone TEXT NOT NULL,
                stress_score INTEGER NOT NULL,
                stress_level TEXT NOT NULL,
                recommendation TEXT NOT NULL,
                answers_json TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)
        conn.execute("""
            CREATE INDEX IF NOT EXISTS idx_user_date ON daily_quizzes(user_id, local_date)
        """)
        conn.commit()

init_quiz_db()


@app.route("/api/quiz/submit", methods=["POST"])
def submit_daily_quiz():
    """
    Submits a daily wellness quiz.
    Enforces that only one submission can be created per user per calendar date.
    If today's record already exists, returns the existing record without creating a duplicate.
    """
    try:
        data = request.get_json(silent=True) or {}
        user_id = (data.get("user_id") or "default_user").strip()
        local_date = (data.get("local_date") or "").strip()
        tz_name = (data.get("timezone") or "UTC").strip()
        answers = data.get("answers", [])
        stress_score = data.get("stress_score", 50)
        stress_level = data.get("stress_level", "MODERATE")
        recommendation = data.get("recommendation", "")

        if not local_date:
            return jsonify({"error": "local_date is required (YYYY-MM-DD)"}), 400

        daily_record_id = f"{user_id}_{local_date}"

        with get_quiz_db() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM daily_quizzes WHERE daily_record_id = ?", (daily_record_id,))
            existing = cur.fetchone()

            if existing:
                # Concurrency-safe once-per-day enforcement
                return jsonify({
                    "already_completed": True,
                    "message": "You've completed your wellness check-in for today. Come back tomorrow!",
                    "daily_record_id": daily_record_id,
                    "result": {
                        "user_id": existing["user_id"],
                        "local_date": existing["local_date"],
                        "stress_score": existing["stress_score"],
                        "stress_level": existing["stress_level"],
                        "recommendation": existing["recommendation"],
                        "created_at": existing["created_at"]
                    }
                }), 200

            try:
                # Atomic insert
                answers_json = json.dumps(answers)
                cur.execute("""
                    INSERT INTO daily_quizzes 
                    (daily_record_id, user_id, local_date, timezone, stress_score, stress_level, recommendation, answers_json)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, (daily_record_id, user_id, local_date, tz_name, stress_score, stress_level, recommendation, answers_json))
                conn.commit()

                return jsonify({
                    "already_completed": False,
                    "message": "Daily wellness quiz submitted successfully.",
                    "daily_record_id": daily_record_id,
                    "result": {
                        "user_id": user_id,
                        "local_date": local_date,
                        "stress_score": stress_score,
                        "stress_level": stress_level,
                        "recommendation": recommendation
                    }
                }), 201

            except sqlite3.IntegrityError:
                # Concurrently inserted by another thread/request
                cur.execute("SELECT * FROM daily_quizzes WHERE daily_record_id = ?", (daily_record_id,))
                existing = cur.fetchone()
                return jsonify({
                    "already_completed": True,
                    "message": "You've completed your wellness check-in for today. Come back tomorrow!",
                    "daily_record_id": daily_record_id,
                    "result": {
                        "user_id": existing["user_id"] if existing else user_id,
                        "local_date": existing["local_date"] if existing else local_date,
                        "stress_score": existing["stress_score"] if existing else stress_score,
                        "stress_level": existing["stress_level"] if existing else stress_level,
                        "recommendation": existing["recommendation"] if existing else recommendation,
                        "created_at": existing["created_at"] if existing else ""
                    }
                }), 200

    except Exception:
        app.logger.exception("Quiz submission error")
        return jsonify({"error": "Failed to submit daily quiz"}), 500


@app.route("/api/quiz/status", methods=["GET"])
def get_daily_quiz_status():
    """
    Checks if the user has completed the daily quiz on the given local calendar date.
    """
    try:
        user_id = (request.args.get("user_id") or "default_user").strip()
        local_date = (request.args.get("local_date") or "").strip()

        if not local_date:
            return jsonify({"error": "local_date query parameter is required"}), 400

        daily_record_id = f"{user_id}_{local_date}"

        with get_quiz_db() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM daily_quizzes WHERE daily_record_id = ?", (daily_record_id,))
            row = cur.fetchone()

            if row:
                return jsonify({
                    "already_completed": True,
                    "message": "You've completed your wellness check-in for today. Come back tomorrow!",
                    "daily_record_id": daily_record_id,
                    "result": {
                        "user_id": row["user_id"],
                        "local_date": row["local_date"],
                        "stress_score": row["stress_score"],
                        "stress_level": row["stress_level"],
                        "recommendation": row["recommendation"],
                        "created_at": row["created_at"]
                    }
                }), 200
            else:
                return jsonify({
                    "already_completed": False,
                    "daily_record_id": daily_record_id,
                    "result": None
                }), 200

    except Exception:
        app.logger.exception("Quiz status query error")
        return jsonify({"error": "Failed to check quiz status"}), 500


# --------------------------------------------------
# JSON ERROR HANDLERS
# --------------------------------------------------
@app.errorhandler(404)
def not_found(error):
    return jsonify({
        "error": "Endpoint not found",
        "message": "Check the URL and HTTP method.",
        "available_endpoints": [
            "GET /",
            "GET /health",
            "POST /predict",
            "POST /checkin",
            "POST /api/analyze_text",
            "POST /api/analyze_burnout",
            "POST /chat",
            "POST /api/chat",
            "POST /api/quiz/submit",
            "GET /api/quiz/status"
        ]
    }), 404


@app.errorhandler(405)
def method_not_allowed(error):
    return jsonify({
        "error": "Method not allowed",
        "message": "This endpoint does not support that HTTP method."
    }), 405


# --------------------------------------------------
# APPLICATION ENTRY POINT
# --------------------------------------------------
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(
        host="0.0.0.0",
        port=port,
        debug=False
    )
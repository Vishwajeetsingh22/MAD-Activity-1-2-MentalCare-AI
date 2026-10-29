warning: in the working copy of 'backend_server/chat_engine.py', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'backend_server/test_chat.py', LF will be replaced by CRLF the next time Git touches it
[1mdiff --git a/backend_server/app.py b/backend_server/app.py[m
[1mindex f75695c..0d586f4 100644[m
[1m--- a/backend_server/app.py[m
[1m+++ b/backend_server/app.py[m
[36m@@ -266,6 +266,176 @@[m [mdef chat():[m
         }), 500[m
 [m
 [m
[32m+[m[32m# --------------------------------------------------[m
[32m+[m[32m# DAILY WELLNESS QUIZ API (ONCE-PER-DAY ENFORCEMENT)[m
[32m+[m[32m# --------------------------------------------------[m
[32m+[m[32mimport sqlite3[m
[32m+[m[32mimport json[m
[32m+[m
[32m+[m[32mQUIZ_DB_PATH = os.path.join(os.path.dirname(__file__), "quiz_records.db")[m
[32m+[m
[32m+[m[32mdef get_quiz_db():[m
[32m+[m[32m    conn = sqlite3.connect(QUIZ_DB_PATH, timeout=10.0)[m
[32m+[m[32m    conn.row_factory = sqlite3.Row[m
[32m+[m[32m    return conn[m
[32m+[m
[32m+[m[32mdef init_quiz_db():[m
[32m+[m[32m    with get_quiz_db() as conn:[m
[32m+[m[32m        conn.execute("""[m
[32m+[m[32m            CREATE TABLE IF NOT EXISTS daily_quizzes ([m
[32m+[m[32m                daily_record_id TEXT PRIMARY KEY,[m
[32m+[m[32m                user_id TEXT NOT NULL,[m
[32m+[m[32m                local_date TEXT NOT NULL,[m
[32m+[m[32m                timezone TEXT NOT NULL,[m
[32m+[m[32m                stress_score INTEGER NOT NULL,[m
[32m+[m[32m                stress_level TEXT NOT NULL,[m
[32m+[m[32m                recommendation TEXT NOT NULL,[m
[32m+[m[32m                answers_json TEXT NOT NULL,[m
[32m+[m[32m                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP[m
[32m+[m[32m            )[m
[32m+[m[32m        """)[m
[32m+[m[32m        conn.execute("""[m
[32m+[m[32m            CREATE INDEX IF NOT EXISTS idx_user_date ON daily_quizzes(user_id, local_date)[m
[32m+[m[32m        """)[m
[32m+[m[32m        conn.commit()[m
[32m+[m
[32m+[m[32minit_quiz_db()[m
[32m+[m
[32m+[m
[32m+[m[32m@app.route("/api/quiz/submit", methods=["POST"])[m
[32m+[m[32mdef submit_daily_quiz():[m
[32m+[m[32m    """[m
[32m+[m[32m    Submits a daily wellness quiz.[m
[32m+[m[32m    Enforces that only one submission can be created per user per calendar date.[m
[32m+[m[32m    If today's record already exists, returns the existing record without creating a duplicate.[m
[32m+[m[32m    """[m
[32m+[m[32m    try:[m
[32m+[m[32m        data = request.get_json(silent=True) or {}[m
[32m+[m[32m        user_id = (data.get("user_id") or "default_user").strip()[m
[32m+[m[32m        local_date = (data.get("local_date") or "").strip()[m
[32m+[m[32m        tz_name = (data.get("timezone") or "UTC").strip()[m
[32m+[m[32m        answers = data.get("answers", [])[m
[32m+[m[32m        stress_score = data.get("stress_score", 50)[m
[32m+[m[32m        stress_level = data.get("stress_level", "MODERATE")[m
[32m+[m[32m        recommendation = data.get("recommendation", "")[m
[32m+[m
[32m+[m[32m        if not local_date:[m
[32m+[m[32m            return jsonify({"error": "local_date is required (YYYY-MM-DD)"}), 400[m
[32m+[m
[32m+[m[32m        daily_record_id = f"{user_id}_{local_date}"[m
[32m+[m
[32m+[m[32m        with get_quiz_db() as conn:[m
[32m+[m[32m            cur = conn.cursor()[m
[32m+[m[32m            cur.execute("SELECT * FROM daily_quizzes WHERE daily_record_id = ?", (daily_record_id,))[m
[32m+[m[32m            existing = cur.fetchone()[m
[32m+[m
[32m+[m[32m            if existing:[m
[32m+[m[32m                # Concurrency-safe once-per-day enforcement[m
[32m+[m[32m                return jsonify({[m
[32m+[m[32m                    "already_completed": True,[m
[32m+[m[32m                    "message": "You've completed your wellness check-in for today. Come back tomorrow!",[m
[32m+[m[32m                    "daily_record_id": daily_record_id,[m
[32m+[m[32m                    "result": {[m
[32m+[m[32m                        "user_id": existing["user_id"],[m
[32m+[m[32m                        "local_date": existing["local_date"],[m
[32m+[m[32m                        "stress_score": existing["stress_score"],[m
[32m+[m[32m                        "stress_level": existing["stress_level"],[m
[32m+[m[32m                        "recommendation": existing["recommendation"],[m
[32m+[m[32m                        "created_at": existing["created_at"][m
[32m+[m[32m                    }[m
[32m+[m[32m                }), 200[m
[32m+[m
[32m+[m[32m            try:[m
[32m+[m[32m                # Atomic insert[m
[32m+[m[32m                answers_json = json.dumps(answers)[m
[32m+[m[32m                cur.execute("""[m
[32m+[m[32m                    INSERT INTO daily_quizzes[m[41m [m
[32m+[m[32m                    (daily_record_id, user_id, local_date, timezone, stress_score, stress_level, recommendation, answers_json)[m
[32m+[m[32m                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)[m
[32m+[m[32m                """, (daily_record_id, user_id, local_date, tz_name, stress_score, stress_level, recommendation, answers_json))[m
[32m+[m[32m                conn.commit()[m
[32m+[m
[32m+[m[32m                return jsonify({[m
[32m+[m[32m                    "already_completed": False,[m
[32m+[m[32m                    "message": "Daily wellness quiz submitted successfully.",[m
[32m+[m[32m                    "daily_record_id": daily_record_id,[m
[32m+[m[32m                    "result": {[m
[32m+[m[32m                        "user_id": user_id,[m
[32m+[m[32m                        "local_date": local_date,[m
[32m+[m[32m                        "stress_score": stress_score,[m
[32m+[m[32m                        "stress_level": stress_level,[m
[32m+[m[32m                        "recommendation": recommendation[m
[32m+[m[32m                    }[m
[32m+[m[32m                }), 201[m
[32m+[m
[32m+[m[32m            except sqlite3.IntegrityError:[m
[32m+[m[32m                # Concurrently inserted by another thread/request[m
[32m+[m[32m                cur.execute("SELECT * FROM daily_quizzes WHERE daily_record_id = ?", (daily_record_id,))[m
[32m+[m[32m                existing = cur.fetchone()[m
[32m+[m[32m                return jsonify({[m
[32m+[m[32m                    "already_completed": True,[m
[32m+[m[32m                    "message": "You've completed your wellness check-in for today. Come back tomorrow!",[m
[32m+[m[32m                    "daily_record_id": daily_record_id,[m
[32m+[m[32m                    "result": {[m
[32m+[m[32m                        "user_id": existing["user_id"] if existing else user_id,[m
[32m+[m[32m                        "local_date": existing["local_date"] if existing else local_date,[m
[32m+[m[32m                        "stress_score": existing["stress_score"] if existing else stress_score,[m
[32m+[m[32m                        "stress_level": existing["stress_level"] if existing else stress_level,[m
[32m+[m[32m                        "recommendation": existing["recommendation"] if existing else recommendation,[m
[32m+[m[32m                        "created_at": existing["created_at"] if existing else ""[m
[32m+[m[32m                    }[m
[32m+[m[32m                }), 200[m
[32m+[m
[32m+[m[32m    except Exception:[m
[32m+[m[32m        app.logger.exception("Quiz submission error")[m
[32m+[m[32m        return jsonify({"error": "Failed to submit daily quiz"}), 500[m
[32m+[m
[32m+[m
[32m+[m[32m@app.route("/api/quiz/status", methods=["GET"])[m
[32m+[m[32mdef get_daily_quiz_status():[m
[32m+[m[32m    """[m
[32m+[m[32m    Checks if the user has completed the daily quiz on the given local calendar date.[m
[32m+[m[32m    """[m
[32m+[m[32m    try:[m
[32m+[m[32m        user_id = (request.args.get("user_id") or "default_user").strip()[m
[32m+[m[32m        local_date = (request.args.get("local_date") or "").strip()[m
[32m+[m
[32m+[m[32m        if not local_date:[m
[32m+[m[32m            return jsonify({"error": "local_date query parameter is required"}), 400[m
[32m+[m
[32m+[m[32m        daily_record_id = f"{user_id}_{local_date}"[m
[32m+[m
[32m+[m[32m        with get_quiz_db() as conn:[m
[32m+[m[32m            cur = conn.cursor()[m
[32m+[m[32m            cur.execute("SELECT * FROM daily_quizzes WHERE daily_record_id = ?", (daily_record_id,))[m
[32m+[m[32m            row = cur.fetchone()[m
[32m+[m
[32m+[m[32m            if row:[m
[32m+[m[32m                return jsonify({[m
[32m+[m[32m                    "already_completed": True,[m
[32m+[m[32m                    "message": "You've completed your wellness check-in for today. Come back tomorrow!",[m
[32m+[m[32m                    "daily_record_id": daily_record_id,[m
[32m+[m[32m                    "result": {[m
[32m+[m[32m                        "user_id": row["user_id"],[m
[32m+[m[32m                        "local_date": row["local_date"],[m
[32m+[m[32m                        "stress_score": row["stress_score"],[m
[32m+[m[32m                        "stress_level": row["stress_level"],[m
[32m+[m[32m                        "recommendation": row["recommendation"],[m
[32m+[m[32m                        "created_at": row["created_at"][m
[32m+[m[32m                    }[m
[32m+[m[32m                }), 200[m
[32m+[m[32m            else:[m
[32m+[m[32m                return jsonify({[m
[32m+[m[32m                    "already_completed": False,[m
[32m+[m[32m                    "daily_record_id": daily_record_id,[m
[32m+[m[32m                    "result": None[m
[32m+[m[32m                }), 200[m
[32m+[m
[32m+[m[32m    except Exception:[m
[32m+[m[32m        app.logger.exception("Quiz status query error")[m
[32m+[m[32m        return jsonify({"error": "Failed to check quiz status"}), 500[m
[32m+[m
[32m+[m
 # --------------------------------------------------[m
 # JSON ERROR HANDLERS[m
 # --------------------------------------------------[m
[36m@@ -282,7 +452,9 @@[m [mdef not_found(error):[m
             "POST /api/analyze_text",[m
             "POST /api/analyze_burnout",[m
             "POST /chat",[m
[31m-            "POST /api/chat"[m
[32m+[m[32m            "POST /api/chat",[m
[32m+[m[32m            "POST /api/quiz/submit",[m
[32m+[m[32m            "GET /api/quiz/status"[m
         ][m
     }), 404[m
 [m
[1mdiff --git a/backend_server/chat_engine.py b/backend_server/chat_engine.py[m
[1mindex 0e7453c..0297bfb 100644[m
[1m--- a/backend_server/chat_engine.py[m
[1m+++ b/backend_server/chat_engine.py[m
[36m@@ -106,7 +106,10 @@[m [mclass ChatEngine:[m
         return False[m
 [m
     def respond(self, message: str, user_id: str = "default_user",[m
[31m-                conversation_id: str = None, current_stress_level: str = None) -> dict:[m
[32m+[m[32m                conversation_id: str = None, current_stress_level: str = None,[m
[32m+[m[32m                stress_level: str = None) -> dict:[m
[32m+[m[32m        if stress_level and not current_stress_level:[m
[32m+[m[32m            current_stress_level = stress_level[m
         msg = (message or "").strip()[m
         if not conversation_id:[m
             conversation_id = f"conv_{int(time.time())}_{uuid.uuid4().hex[:6]}"[m
[36m@@ -280,7 +283,7 @@[m [mclass ChatEngine:[m
             ][m
 [m
         if lower in ["yes", "sure", "yeah", "yep", "ok", "okay", "please", "yes please"]:[m
[31m-            if state.last_assistant_intent == "offered_study_plan" or "mathematics" in e["subjects"]:[m
[32m+[m[32m            if state.last_assistant_intent == "offered_study_plan":[m
                 # User said yes to study plan[m
                 return self._generate_study_plan_response(e)[m
             elif state.last_assistant_intent == "offered_breathing":[m
[36m@@ -332,7 +335,7 @@[m [mclass ChatEngine:[m
                 ][m
 [m
         # CASE 5: MATHEMATICS & UNPREPAREDNESS (e.g. "I haven't studied mathematics")[m
[31m-        if ("mathematics" in lower or "math" in lower or e["prepared"] is False) and ("mathematics" in e["subjects"]):[m
[32m+[m[32m        if any(w in lower for w in ["mathematics", "math", "haven't studied", "havent studied", "not prepared", "not studied"]):[m
             state.last_assistant_intent = "offered_study_plan"[m
             reply = ([m
                 "Facing a mathematics exam tomorrow without feeling prepared is definitely stressful, "[m
[36m@@ -406,6 +409,26 @@[m [mclass ChatEngine:[m
                 "Write down your immediate priorities."[m
             ][m
 [m
[32m+[m[32m        # CASE 10B: RECENT CHECK-INS QUERY[m
[32m+[m[32m        if any(w in lower for w in ["recent check-in", "recent checkin", "recent check ins", "my check-ins", "my checkins", "track my stress", "how my stress has been"]):[m
[32m+[m[32m            if current_stress_level:[m
[32m+[m[32m                reply = ([m
[32m+[m[32m                    f"Your most recent wellness check-in indicated {current_stress_level} stress. "[m
[32m+[m[32m                    "Consistent daily logging helps identify burnout patterns early so you can take restorative pauses. "[m
[32m+[m[32m                    "How are your energy and stress levels feeling right now compared to earlier today?"[m
[32m+[m[32m                )[m
[32m+[m[32m            else:[m
[32m+[m[32m                reply = ([m
[32m+[m[32m                    "Your recent check-in records show a steady baseline. "[m
[32m+[m[32m                    "Completing your Daily Check-in each morning takes less than a minute "[m
[32m+[m[32m                    "and helps me give you personalized wellness suggestions. How are you feeling right now?"[m
[32m+[m[32m                )[m
[32m+[m[32m            return reply, [[m
[32m+[m[32m                "Complete your Daily Check-in.",[m
[32m+[m[32m                "Review stress trends in Insights.",[m
[32m+[m[32m                "Practice a 5-minute breathing pause."[m
[32m+[m[32m            ][m
[32m+[m
         # CASE 11: DYNAMIC CONVERSATION CONTINUATION (No static repeated loops!)[m
         # Synthesize reply based on conversation history[m
         prev_topics = list(e["topics"])[m
[1mdiff --git a/backend_server/test_chat.py b/backend_server/test_chat.py[m
[1mindex 0b79e17..f73a8c6 100644[m
[1m--- a/backend_server/test_chat.py[m
[1m+++ b/backend_server/test_chat.py[m
[36m@@ -91,8 +91,16 @@[m [mdef run_tests():[m
     assert "tomorrow" in r7['reply'].lower(), "Turn 7 failed: Should recall exams are tomorrow"[m
     assert "mathematics" in r7['reply'].lower() or "haven't studied" in r7['reply'].lower(), "Turn 7 failed: Should recall mathematics / lack of preparation"[m
 [m
[32m+[m[32m    # TURN 8 - QUICK PROMPT: RECENT CHECK-INS[m
[32m+[m[32m    t8 = "Tell me about my recent check-ins and how my stress has been tracking."[m
[32m+[m[32m    print(f"\n[Turn 8] User: {t8}")[m
[32m+[m[32m    r8 = engine.respond(t8, conversation_id=conv_id, stress_level="MODERATE")[m
[32m+[m[32m    print(f"[Turn 8] AI:   {r8['reply']}")[m
[32m+[m[32m    assert r8['reply'] != r7['reply'], "Turn 8 FAILED: Repeated response!"[m
[32m+[m[32m    assert any(w in r8['reply'].lower() for w in ["check-in", "moderate", "log", "stress"]), "Turn 8 failed: Should address recent check-ins"[m
[32m+[m
     print("\n" + "=" * 65)[m
[31m-    print(">>> 7-STEP CONTINUOUS CONVERSATION PASSED WITH 100% SUCCESS!")[m
[32m+[m[32m    print(">>> 8-STEP CONTINUOUS CONVERSATION PASSED WITH 100% SUCCESS!")[m
     print("=" * 65)[m
 [m
     # -------------------------------------------------------------[m
[1mdiff --git a/gradle.properties b/gradle.properties[m
[1mindex 887117b..7cbc00f 100644[m
[1m--- a/gradle.
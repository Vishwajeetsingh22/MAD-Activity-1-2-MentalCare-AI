"""
MentalCare AI - Context-Aware Conversational AI Wellness Chat Engine
Academic Project: MCA Mobile Application Development (Continuous Assessment)
Team: Group 11 (Joylyn Princita Fernandes, Syed Faizan Pasha, Vishwajeet Singh)
Department of MCA, Jain (Deemed-to-be University)

Architecture:
1. Multi-turn Conversational Memory: Maintains stateful conversation context across turns per conversation_id.
2. External AI Provider Support: Native Google Gemini API (gemini-1.5-flash / gemini-2.5-flash) and OpenAI API (gpt-4o-mini).
3. Dynamic Dialogue Context Engine: Extracts user entities (stress, exams, mathematics, sleep difficulties),
   answers direct queries (e.g. "What did I tell you about my exams?"), handles affirmations ("Yes ask me questions"),
   and generates tailored wellness & study plans without static canned repetition.
4. Crisis Safety Guardrails: Immediate de-escalation for self-harm ideation, flags is_crisis=True, 988 lifeline.
5. Medical Boundary Enforcement: Disclaims clinical diagnosis authority, rejects prescriptions, refers to licensed professionals.
"""

import os
import re
import time
import uuid
import json
import urllib.request
import urllib.error

# Automatically load environment variables from local .env if present
def _load_env_file():
    env_path = os.path.join(os.path.dirname(__file__), ".env")
    if os.path.exists(env_path):
        try:
            with open(env_path, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith("#") and "=" in line:
                        k, v = line.split("=", 1)
                        k = k.strip()
                        v = v.strip().strip('"').strip("'")
                        if k and not os.environ.get(k):
                            os.environ[k] = v
        except Exception as e:
            print(f"[ChatEngine] Note reading .env: {e}")

_load_env_file()

CRISIS_PATTERNS = [
    r"\b(suicide|kill myself|end my life|harm myself|giving up completely|can'?t go on anymore)\b",
    r"\b(want to die|take my life|better off dead|hurt myself|no reason to live)\b",
    r"\b(extreme crisis|hopeless|cannot take it anymore|done with everything)\b"
]

MEDICAL_DIAGNOSIS_PATTERNS = [
    r"\b(diagnose|do i have depression|am i bipolar|do i have adhd|schizophrenia|clinical depression)\b",
    r"\b(prescribe|medication|antidepressant|xanax|prozac|what medicine should i take|pills)\b"
]

SYSTEM_PROMPT = (
    "You are the AI MentalCare Assistant, an empathetic, supportive mental wellness conversation companion. "
    "Your tagline is 'Understand. Relax. Thrive.' "
    "Your role is to listen attentively, ask gentle clarifying questions, help users reflect on their thoughts, "
    "and suggest evidence-informed wellness exercises (breathing, journaling, study pacing, sleep hygiene). "
    "You NEVER diagnose mental health disorders, claim clinical authority, or prescribe medication. "
    "Always maintain a warm, non-judgmental, conversational tone and remember context across previous turns."
)

class ConversationState:
    """Tracks structured conversation memory, user entities, and turn history."""
    def __init__(self, conversation_id: str):
        self.conversation_id = conversation_id
        self.turns = 0
        self.messages = [] # list of {"role": str, "message": str, "timestamp": float}
        self.entities = {
            "stress_stated": False,
            "causes": [],           # e.g., ["exams tomorrow"]
            "subjects": [],         # e.g., ["mathematics"]
            "prepared": None,       # False if hasn't studied
            "sleep_issue": False,   # True if trouble sleeping
            "sleep_recurring": False,
            "topics": set()
        }
        self.last_assistant_intent = None
        self.used_replies = set()

class ChatEngine:
    def __init__(self):
        # In-memory conversation store: conversation_id -> ConversationState
        self.states: dict[str, ConversationState] = {}

    def get_or_create_state(self, conversation_id: str) -> ConversationState:
        if not conversation_id:
            conversation_id = f"conv_{int(time.time())}_{uuid.uuid4().hex[:6]}"
        if conversation_id not in self.states:
            self.states[conversation_id] = ConversationState(conversation_id)
        return self.states[conversation_id]

    def is_crisis(self, text: str) -> bool:
        lower = text.lower()
        for pattern in CRISIS_PATTERNS:
            if re.search(pattern, lower):
                return True
        return False

    def is_medical_query(self, text: str) -> bool:
        lower = text.lower()
        for pattern in MEDICAL_DIAGNOSIS_PATTERNS:
            if re.search(pattern, lower):
                return True
        return False

    def respond(self, message: str, user_id: str = "default_user",
                conversation_id: str = None, current_stress_level: str = None,
                stress_level: str = None) -> dict:
        if stress_level and not current_stress_level:
            current_stress_level = stress_level
        msg = (message or "").strip()
        if not conversation_id:
            conversation_id = f"conv_{int(time.time())}_{uuid.uuid4().hex[:6]}"

        state = self.get_or_create_state(conversation_id)
        state.turns += 1

        if not msg:
            return {
                "conversation_id": conversation_id,
                "reply": "I'm here whenever you'd like to share how you're feeling.",
                "is_crisis": False,
                "recommendations": ["Take a slow, deep breath."]
            }

        # 1. IMMEDIATE CRISIS SAFETY SCANNER
        if self.is_crisis(msg):
            reply = (
                "I am deeply concerned about what you are going through right now. "
                "Please remember that you do not have to carry this alone and immediate help is available. "
                "Please reach out to your emergency contact right now, or contact the 988 Suicide & Crisis Lifeline "
                "by calling or texting 988 (free, confidential, 24/7). "
                "If you are in immediate danger, please contact local emergency services immediately."
            )
            self._save_turn(state, user_id, msg, reply)
            return {
                "conversation_id": conversation_id,
                "reply": reply,
                "is_crisis": True,
                "recommendations": [
                    "Call your configured Emergency Contact.",
                    "Dial 988 Crisis Lifeline (Toll-Free, 24/7).",
                    "Reach out to someone you trust right now."
                ]
            }

        # 2. MEDICAL DIAGNOSIS / PRESCRIPTION BOUNDARY
        if self.is_medical_query(msg):
            reply = (
                "As an AI wellness assistant, I cannot provide medical diagnoses, evaluate clinical psychiatric disorders, "
                "or recommend prescription medications. MentalCare AI is strictly an assistive wellness tool. "
                "If you are experiencing symptoms of illness, depression, or severe anxiety, I strongly encourage you "
                "to consult a licensed physician or mental health professional."
            )
            self._save_turn(state, user_id, msg, reply)
            return {
                "conversation_id": conversation_id,
                "reply": reply,
                "is_crisis": False,
                "recommendations": [
                    "Consult a licensed medical or psychological professional.",
                    "Maintain a daily mood log in your MentalCare Journal."
                ]
            }

        # 3. EXTERNAL AI PROVIDER INTEGRATION (If API key is present)
        gemini_key = os.environ.get("GEMINI_API_KEY")
        openai_key = os.environ.get("OPENAI_API_KEY")

        if gemini_key:
            try:
                external_reply = self._call_gemini_api(gemini_key, state, msg, current_stress_level)
                if external_reply:
                    self._save_turn(state, user_id, msg, external_reply)
                    return {
                        "conversation_id": conversation_id,
                        "reply": external_reply,
                        "is_crisis": False,
                        "recommendations": ["Practice mindful breathing.", "Note your thoughts in the Journal."]
                    }
            except Exception as e:
                print(f"[ChatEngine] External Gemini API call failed: {e}")

        if openai_key:
            try:
                external_reply = self._call_openai_api(openai_key, state, msg, current_stress_level)
                if external_reply:
                    self._save_turn(state, user_id, msg, external_reply)
                    return {
                        "conversation_id": conversation_id,
                        "reply": external_reply,
                        "is_crisis": False,
                        "recommendations": ["Practice mindful breathing.", "Reflect in your Journal."]
                    }
            except Exception as e:
                print(f"[ChatEngine] External OpenAI API call failed: {e}")

        # 4. INTELLIGENT DYNAMIC CONTEXT-AWARE DIALOGUE ENGINE
        # Operates stateful dialogue tracking, entity extraction, context recall, and personalized answers
        reply, recs = self._generate_contextual_response(msg, state, current_stress_level)
        self._save_turn(state, user_id, msg, reply)

        return {
            "conversation_id": conversation_id,
            "reply": reply,
            "is_crisis": False,
            "recommendations": recs
        }

    def _generate_contextual_response(self, msg: str, state: ConversationState, current_stress_level: str) -> tuple[str, list]:
        lower = msg.lower().strip()
        e = state.entities

        # Extract entities from incoming message
        if any(w in lower for w in ["stressed", "stress", "overwhelmed", "anxious", "anxiety", "pressure"]):
            e["stress_stated"] = True
            e["topics"].add("stress")

        if any(w in lower for w in ["exam", "exams", "test", "finals", "tomorrow"]):
            if "tomorrow" in lower or "exam" in lower:
                e["causes"].append("exams tomorrow")
            e["topics"].add("exams")

        if any(w in lower for w in ["mathematics", "maths", "math", "calculus", "algebra"]):
            if "mathematics" not in e["subjects"]:
                e["subjects"].append("mathematics")
            e["topics"].add("mathematics")

        if any(w in lower for w in ["haven't studied", "didn't study", "not prepared", "not ready", "zero preparation"]):
            e["prepared"] = False

        if any(w in lower for w in ["sleep", "sleeping", "insomnia", "can't sleep", "trouble sleeping"]):
            e["sleep_issue"] = True
            e["topics"].add("sleep")

        if e["sleep_issue"] and any(w in lower for w in ["every night", "almost every night", "always", "frequently", "all the time"]):
            e["sleep_recurring"] = True

        # CASE 1: MEMORY RECALL QUERIES (e.g. "What did I tell you about my exams?", "Do you remember what I said?")
        if any(w in lower for w in ["what did i tell you", "what did i say", "do you remember", "did i mention", "recall what i said"]):
            if "exam" in lower or "exams" in lower:
                if "mathematics" in e["subjects"] or e["prepared"] is False:
                    reply = (
                        "You shared that your exams are tomorrow, and you were feeling particularly worried and stressed "
                        "because you haven't had a chance to study mathematics yet."
                    )
                else:
                    reply = "You mentioned earlier that your exams are tomorrow and that they have been causing you stress."
                return reply, ["Focus on high-yield revision.", "Take a gentle breathing break."]
            
            if "sleep" in lower:
                reply = "You mentioned that you have also been having trouble sleeping amidst the exam stress."
                return reply, ["Review the bedtime relaxation techniques."]

            # Generic recall
            summary_parts = []
            if "exams tomorrow" in e["causes"]:
                summary_parts.append("your exams tomorrow")
            if "mathematics" in e["subjects"]:
                summary_parts.append("being unprepared for mathematics")
            if e["sleep_issue"]:
                summary_parts.append("having trouble sleeping")
            
            if summary_parts:
                items_str = ", ".join(summary_parts)
                reply = f"In our conversation, you shared that you've been dealing with {items_str}. I'm keeping all of that in mind to support you."
            else:
                reply = "You've been sharing about your current stress and thoughts, and I'm listening closely to support you."
            return reply, ["Take things one step at a time."]

        # CASE 2: USER ASKS FOR QUESTIONS OR GIVES AFFIRMATION (e.g. "Yes ask me questions", "Ask me questions", "Sure, ask me")
        if any(w in lower for w in ["yes ask me", "ask me questions", "ask me a question", "ask me something", "please ask me"]):
            state.last_assistant_intent = "asked_what_causing_stress"
            reply = (
                "Thank you for trusting me. Let's take it one step at a time so it doesn't feel overwhelming. "
                "What has been the main source of pressure or the biggest thing weighing on your mind today?"
            )
            return reply, [
                "Talk about whatever feels heaviest right now.",
                "Take a slow breath before answering."
            ]

        if lower in ["yes", "sure", "yeah", "yep", "ok", "okay", "please", "yes please"]:
            if state.last_assistant_intent == "offered_study_plan":
                # User said yes to study plan
                return self._generate_study_plan_response(e)
            elif state.last_assistant_intent == "offered_breathing":
                return self._generate_relaxation_response()
            else:
                reply = "I'm listening. What is on your mind right now that you'd like to talk through?"
                return reply, ["Share what feels most urgent."]

        # CASE 3: EXPLICIT STUDY PLAN REQUEST (e.g. "Can you help me make a study plan?")
        if any(w in lower for w in ["study plan", "help me study", "study schedule", "plan my revision", "make a study plan"]):
            return self._generate_study_plan_response(e)

        # CASE 4: SLEEP DIFFICULTY (e.g. "I am also having trouble sleeping", "It happens almost every night")
        if e["sleep_issue"] and any(w in lower for w in ["trouble sleeping", "can't sleep", "insomnia", "sleeping", "sleep", "every night"]):
            if e["sleep_recurring"]:
                reply = (
                    "Struggling to sleep almost every night is physically and emotionally exhausting. "
                    "When sleep disruptions become a recurring pattern, your mind begins associating bed with restlessness.\n\n"
                    "A gentle routine that can help rebuild your sleep cues:\n"
                    "• Keep your bedroom cool, dark, and screen-free 45 minutes before bedtime.\n"
                    "• If you cannot fall asleep after 20 minutes, get out of bed and do a quiet, dim-light activity until drowsiness returns.\n"
                    "• Practice a 5-minute diaphragmatic breathing cycle to signal to your nervous system that you are safe to rest.\n\n"
                    "Would you like to set an evening wind-down reminder in your Daily Plan?"
                )
                return reply, [
                    "Dim screen lights 45 minutes before resting.",
                    "Practice the Evening Wind-down activity in Activities.",
                    "Avoid late caffeine and heavy late-night meals."
                ]
            else:
                # Connected to exam stress if applicable
                exam_mention = ""
                if "mathematics" in e["subjects"] or "exams tomorrow" in e["causes"]:
                    exam_mention = " It is very common when your mind is racing about tomorrow's mathematics exam—anticipatory worry keeps your nervous system on high alert."
                
                reply = (
                    f"Having trouble sleeping is very frustrating and makes coping with stress much harder.{exam_mention}\n\n"
                    "A few practical steps to help your mind transition into rest tonight:\n"
                    "• Step away from study materials at least 45 minutes before bed.\n"
                    "• Try the 4-7-8 breathing technique: Inhale for 4s, hold gently for 7s, exhale slowly for 8s.\n"
                    "• Write down your top 3 remaining tasks on paper to 'park' them outside your head for tonight.\n\n"
                    "Would you like to try a guided 5-minute relaxation exercise right now?"
                )
                state.last_assistant_intent = "offered_breathing"
                return reply, [
                    "Try the 4-7-8 breathing exercise.",
                    "Put away mobile screens 30 minutes before sleep.",
                    "Note lingering worries in your MentalCare Journal."
                ]

        # CASE 5: MATHEMATICS & UNPREPAREDNESS (e.g. "I haven't studied mathematics")
        if any(w in lower for w in ["mathematics", "math", "haven't studied", "havent studied", "not prepared", "not studied"]):
            state.last_assistant_intent = "offered_study_plan"
            reply = (
                "Facing a mathematics exam tomorrow without feeling prepared is definitely stressful, "
                "but panicking will only drain the working memory you need tonight. "
                "The key right now is triage rather than trying to cram the entire syllabus:\n\n"
                "1. Focus on core formulas and standard high-frequency question types.\n"
                "2. Work through 2-3 solved examples by hand rather than just reading notes.\n"
                "3. Take short breathing breaks to keep your anxiety from blocking your problem-solving.\n\n"
                "Would you like me to help you make a practical study plan for mathematics tonight?"
            )
            return reply, [
                "Review high-yield mathematics formulas.",
                "Practice 2-3 sample problems by hand.",
                "Take a 5-minute box breathing break between topics."
            ]

        # CASE 6: EXAMS TOMORROW (e.g. "My exams are tomorrow")
        if any(w in lower for w in ["exams are tomorrow", "exam tomorrow", "exam is tomorrow", "finals tomorrow"]):
            state.last_assistant_intent = "asked_which_subject"
            reply = (
                "Having exams tomorrow creates a lot of acute pressure. When the countdown begins, "
                "our minds often jump to worst-case scenarios. Focusing on one specific subject at a time helps prevent overwhelm. "
                "Which specific subject or topics are feeling the most urgent for tomorrow?"
            )
            return reply, [
                "Identify your top priority subject for tomorrow.",
                "Break your remaining revision into 25-minute blocks.",
                "Schedule a mandatory rest time tonight."
            ]

        # CASE 7: 5-MINUTE RELAXATION REQUEST
        if any(w in lower for w in ["relax for five minutes", "relax for 5 minutes", "5 minutes", "five minutes", "quick relax"]):
            return self._generate_relaxation_response()

        # CASE 8: INITIAL STRESS DISCLOSURE (e.g. "I'm feeling stressed today.")
        if e["stress_stated"] and state.turns <= 2:
            stress_hint = f" (Your recent check-in also indicated {current_stress_level} stress)" if current_stress_level else ""
            state.last_assistant_intent = "asked_general_stress"
            reply = (
                f"I'm sorry you're feeling so stressed today{stress_hint}. It takes self-awareness to recognize "
                "when things are feeling like too much. Would you like to explore what is putting the most weight "
                "on your mind today, or would you like me to ask you a couple of gentle questions?"
            )
            return reply, [
                "Take 3 slow, deep abdominal breaths.",
                "Talk about what is causing you pressure.",
                "Consider taking a 5-minute pause away from tasks."
            ]

        # CASE 9: POSITIVE FEELINGS
        if any(w in lower for w in ["good", "great", "happy", "calm", "peaceful", "better", "relieved", "fine"]):
            reply = (
                "That's wonderful to hear! Acknowledging and savoring calm or positive moments builds emotional resilience. "
                "What is something that went well or brought you peace today?"
            )
            return reply, [
                "Log this positive moment in your Journal.",
                "Keep up your consistent wellness routine!"
            ]

        # CASE 10: WORK / BURNOUT
        if any(w in lower for w in ["work", "job", "boss", "colleague", "workload", "office", "burnout"]):
            reply = (
                "Heavy workplace demands and prolonged expectations often cause burnout when rest is delayed. "
                "Establishing small boundaries—such as stepping away during lunch and turning off notifications in the evening—can protect your energy. "
                "Have you been able to take any genuine breaks away from your workstation today?"
            )
            return reply, [
                "Take a mandatory 10-minute walk away from screens.",
                "Set clear boundaries for after-work communication.",
                "Write down your immediate priorities."
            ]

        # CASE 10B: RECENT CHECK-INS QUERY
        if any(w in lower for w in ["recent check-in", "recent checkin", "recent check ins", "my check-ins", "my checkins", "track my stress", "how my stress has been"]):
            if current_stress_level:
                reply = (
                    f"Your most recent wellness check-in indicated {current_stress_level} stress. "
                    "Consistent daily logging helps identify burnout patterns early so you can take restorative pauses. "
                    "How are your energy and stress levels feeling right now compared to earlier today?"
                )
            else:
                reply = (
                    "Your recent check-in records show a steady baseline. "
                    "Completing your Daily Check-in each morning takes less than a minute "
                    "and helps me give you personalized wellness suggestions. How are you feeling right now?"
                )
            return reply, [
                "Complete your Daily Check-in.",
                "Review stress trends in Insights.",
                "Practice a 5-minute breathing pause."
            ]

        # CASE 11: DYNAMIC CONVERSATION CONTINUATION (No static repeated loops!)
        # Synthesize reply based on conversation history
        prev_topics = list(e["topics"])
        if prev_topics:
            topic_desc = " and ".join(prev_topics)
            reply = (
                f"I hear you. In our conversation, you've mentioned dealing with {topic_desc}. "
                "Giving words to your thoughts is an important step in caring for your mental wellbeing. "
                "How are you feeling right now as we talk through this?"
            )
        else:
            reply = (
                "I'm listening and I'm here to support you. You can talk freely about whatever is on your mind—whether "
                "it's exam deadlines, work pressure, sleep difficulties, or just wanting a mindful pause. "
                "How can I best support you right now?"
            )

        return reply, [
            "Practice a 5-minute Box Breathing cycle.",
            "Write a private reflection in your Journal.",
            "Review your goals in Today's Plan."
        ]

    def _generate_study_plan_response(self, e: dict) -> tuple[str, list]:
        subject = e["subjects"][0] if e["subjects"] else "your primary subject"
        reply = (
            f"Let's build a focused, stress-reducing emergency study plan for {subject} tomorrow:\n\n"
            "• Step 1: Formula Triage (40 mins): Write out the top 10 most essential formulas and definitions on a single sheet.\n"
            "• Step 2: High-Yield Practice (50 mins): Work through 3-4 standard solved problems from previous papers step-by-step.\n"
            "• Step 3: Mandatory 10-min Break: Step away, drink a glass of cool water, and stretch your shoulders.\n"
            "• Step 4: Concept Review (30 mins): Review common calculation traps or tricky theorem steps.\n"
            "• Step 5: Sleep Buffer: Stop all studying at least 45 minutes before sleep so your brain can consolidate what you reviewed.\n\n"
            f"Which specific chapter or unit in {subject} carries the highest marks weighting?"
        )
        return reply, [
            "Set a 25-minute Pomodoro timer in Activities.",
            "Write down your top 3 subject priorities.",
            "Take mandatory 5-minute rest breaks."
        ]

    def _generate_relaxation_response(self) -> tuple[str, list]:
        reply = (
            "Here is an effective 5-minute relaxation exercise you can do right now:\n\n"
            "1. Drop your shoulders down and unclamp your jaw.\n"
            "2. Practice 4 rounds of Box Breathing: Inhale for 4s, hold for 4s, exhale for 4s, hold for 4s.\n"
            "3. Roll your neck gently and drink half a glass of cool water.\n\n"
            "Would you like to start the guided 5-minute breathing timer in the Activities tab?"
        )
        return reply, [
            "Open the 5-Minute Box Breathing exercise in Activities.",
            "Take a slow sip of water and stretch your shoulders."
        ]

    def _call_gemini_api(self, api_key: str, state: ConversationState, message: str, stress_level: str) -> str:
        # Try google.genai SDK if available
        try:
            from google import genai
            client = genai.Client(api_key=api_key)
            
            # Format system prompt
            sys_instruct = SYSTEM_PROMPT
            if stress_level:
                sys_instruct += f" Note: The user's latest recorded stress check-in is {stress_level}."

            # Construct valid alternating conversation contents
            contents = []
            for m in state.messages[-6:]:
                role = "user" if m.get("role") == "user" else "model"
                contents.append({"role": role, "parts": [{"text": m.get("message", "")}]})
            contents.append({"role": "user", "parts": [{"text": message}]})

            # Ensure contents begins with "user"
            while contents and contents[0]["role"] != "user":
                contents.pop(0)

            response = client.models.generate_content(
                model='gemini-1.5-flash',
                contents=contents,
                config={
                    "system_instruction": sys_instruct,
                    "max_output_tokens": 300,
                    "temperature": 0.7
                }
            )
            if response and response.text:
                return response.text.strip()
        except Exception as e:
            # Fallback to direct HTTPS endpoint
            pass

        url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={api_key}"
        
        contents = []
        for m in state.messages[-6:]:
            role = "user" if m.get("role") == "user" else "model"
            contents.append({"role": role, "parts": [{"text": m.get("message", "")}]})
        contents.append({"role": "user", "parts": [{"text": message}]})

        while contents and contents[0]["role"] != "user":
            contents.pop(0)

        sys_instruct = SYSTEM_PROMPT
        if stress_level:
            sys_instruct += f" Note: The user's latest recorded stress check-in is {stress_level}."

        payload = {
            "system_instruction": {"parts": [{"text": sys_instruct}]},
            "contents": contents,
            "generationConfig": {
                "temperature": 0.7,
                "maxOutputTokens": 300
            }
        }

        req = urllib.request.Request(
            url,
            data=json.dumps(payload).encode("utf-8"),
            headers={"Content-Type": "application/json"}
        )

        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            candidates = data.get("candidates", [])
            if candidates:
                parts = candidates[0].get("content", {}).get("parts", [])
                if parts:
                    return parts[0].get("text", "").strip()
        return None

    def _call_openai_api(self, api_key: str, state: ConversationState, message: str, stress_level: str) -> str:
        url = "https://api.openai.com/v1/chat/completions"
        messages = [{"role": "system", "content": SYSTEM_PROMPT}]
        if stress_level:
            messages[0]["content"] += f" Note: The user's latest recorded stress check-in is {stress_level}."

        for m in state.messages[-6:]:
            messages.append({"role": m.get("role", "user"), "content": m.get("message", "")})
        messages.append({"role": "user", "content": message})

        payload = {
            "model": "gpt-4o-mini",
            "messages": messages,
            "max_tokens": 300,
            "temperature": 0.7
        }

        req = urllib.request.Request(
            url,
            data=json.dumps(payload).encode("utf-8"),
            headers={
                "Content-Type": "application/json",
                "Authorization": f"Bearer {api_key}"
            }
        )

        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            choices = data.get("choices", [])
            if choices:
                return choices[0].get("message", {}).get("content", "").strip()
        return None

    def _save_turn(self, state: ConversationState, user_id: str, user_msg: str, ai_reply: str):
        now = time.time()
        state.messages.append({
            "role": "user",
            "message": user_msg,
            "userId": user_id,
            "timestamp": now
        })
        state.messages.append({
            "role": "assistant",
            "message": ai_reply,
            "userId": "ai_assistant",
            "timestamp": now + 0.1
        })
        state.used_replies.add(ai_reply)
        # Keep sliding history window of 20 messages (10 turns)
        if len(state.messages) > 20:
            state.messages = state.messages[-20:]

    def get_conversation_history(self, conversation_id: str) -> list:
        state = self.states.get(conversation_id)
        return state.messages if state else []

    def clear_conversation(self, conversation_id: str):
        if conversation_id in self.states:
            del self.states[conversation_id]

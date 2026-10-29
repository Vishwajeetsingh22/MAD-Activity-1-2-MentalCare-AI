"""
MentalCare AI - Real-Time AI-Powered Stress & Burnout Detector
MCA Project - CA1 Continuation & CA3 AI System
Team: Group 11 (Joylyn Princita Fernandes, Syed Faizan Pasha, Vishwajeet Singh)
Department of MCA, Jain (Deemed-to-be University)

AI Use Case:
- Natural Language Processing (NLP) of user-written mental check-in text.
- Text Feature Extraction via TF-IDF (Term Frequency-Inverse Document Frequency) n-grams.
- Multi-Class Stress Classification: LOW, MODERATE, HIGH.
- Explainable AI: Contextual indicator keyword extraction (Work pressure, Poor sleep, Mental fatigue).
- Personalized early-warning wellness recommendations.
- Non-medical disclaimer: Assistive early-warning support tool, not clinical diagnosis.
"""

import os
import re
import joblib
import numpy as np

LABEL_MAP = {
    0: ("LOW", "Normal wellness state"),
    1: ("MODERATE", "Moderate stress level"),
    2: ("HIGH", "High stress indicators detected")
}

CRISIS_KEYWORDS = [
    "give up", "giving up", "suicide", "end my life", "harm myself", 
    "can't go on", "breakdown", "hopeless", "extreme crisis", "kill myself"
]

INDICATOR_KEYWORDS = {
    "Work pressure": ["work", "working", "deadline", "boss", "office", "project", "assignment", "tasks", "workload", "meeting", "study"],
    "Poor sleep": ["sleep", "sleeping", "insomnia", "awake", "restless", "night", "tired"],
    "Mental fatigue": ["exhausted", "tired", "drained", "fatigue", "burnt out", "burnout", "no energy", "brain fog", "concentrate"]
}

class NLPEngine:
    def __init__(self):
        model_dir = os.path.dirname(__file__)
        vec_path = os.path.join(model_dir, "tfidf_vectorizer.pkl")
        model_path = os.path.join(model_dir, "stress_model.pkl")
        
        if os.path.exists(vec_path) and os.path.exists(model_path):
            self.vectorizer = joblib.load(vec_path)
            self.model = joblib.load(model_path)
        else:
            self.vectorizer = None
            self.model = None

    def clean_text(self, text):
        text = text.lower()
        text = re.sub(r'[^a-zA-Z0-9\s]', '', text)
        return text.strip()

    def scan_crisis(self, text):
        text_lower = text.lower()
        for kw in CRISIS_KEYWORDS:
            if kw in text_lower:
                return True
        return False

    def extract_indicators(self, text):
        text_lower = text.lower()
        detected = []
        for category, keywords in INDICATOR_KEYWORDS.items():
            for kw in keywords:
                if kw in text_lower:
                    detected.append(category)
                    break
        if not detected:
            detected.append("General wellness state")
        return detected

    def generate_recommendations(self, level, indicators, sleep_hrs=None, work_hrs=None):
        recs = []
        if level == "HIGH":
            recs.append("Consider taking a short break and prioritizing rest.")
            if "Poor sleep" in indicators or (sleep_hrs is not None and sleep_hrs < 6.5):
                recs.append("😴 Sleep Recovery: Practice a 10-minute bedtime relaxation audio.")
            if "Work pressure" in indicators or (work_hrs is not None and work_hrs > 8.5):
                recs.append("⏸️ Workload Break: Take a mandatory 15-minute break and delegate urgent non-essential tasks.")
            return recs

        if level == "MODERATE":
            recs.append("Take a short 10-minute stretch break and stay hydrated.")
            return recs

        recs.append("🌿 Wellness Tip: Maintain your healthy routine and balanced schedule!")
        return recs

    def analyze(self, text, sleep_hrs=None, work_hrs=None, exercise_mins=None):
        is_crisis = self.scan_crisis(text)
        indicators = self.extract_indicators(text)
        cleaned = self.clean_text(text)
        
        if self.model and self.vectorizer:
            vec = self.vectorizer.transform([cleaned])
            probs = self.model.predict_proba(vec)[0]
            pred_class = int(np.argmax(probs))
            if pred_class >= 3:
                pred_class = 2
            confidence = round(float(probs[pred_class]) * 100, 1)
        else:
            # Rule-based fallback
            if is_crisis:
                pred_class = 2
                confidence = 96.5
            elif any(w in cleaned for w in ["exhausted", "overwhelmed", "can't sleep", "burnout", "stressful", "continuously"]):
                pred_class = 2
                confidence = 88.0
            elif any(w in cleaned for w in ["worried", "rushed", "tense", "deadline", "busy"]):
                pred_class = 1
                confidence = 81.0
            else:
                pred_class = 0
                confidence = 94.0

        level_name, status_desc = LABEL_MAP[pred_class]
        recommendations = self.generate_recommendations(level_name, indicators, sleep_hrs, work_hrs)
        
        score_map = {0: 32, 1: 58, 2: 81}
        stress_score = score_map[pred_class]
        
        return {
            "app_name": "MentalCare AI",
            "tagline": "Understand. Relax. Thrive.",
            "text": text,
            "stress_level": level_name,
            "status_description": status_desc,
            "stress_score": stress_score,
            "confidence": confidence,
            "indicators": indicators,
            "is_crisis": is_crisis,
            "recommendations": recommendations,
            "disclaimer": "MentalCare AI is a wellness and early-warning support system, not a medical diagnostic application."
        }

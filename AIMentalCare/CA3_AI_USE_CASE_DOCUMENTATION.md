# CA3 REVIEW 1: IDEA & AI USE-CASE PROPOSAL
## AI MentalCare – Real-Time AI-Powered Stress & Burnout Detection Mobile Application

**Academic Project Information:**
* **Department:** Master of Computer Applications (MCA)
* **Institution:** Jain (Deemed-to-be University)
* **Group:** 11
* **Course Activity:** Mobile Application Development – CA3 (Continuation of CA1)

**Team Members:**
1. **Joylyn Princita Fernandes** – USN: `25MCAR0099`
2. **Syed Faizan Pasha** – USN: `25MCAR0138`
3. **Vishwajeet Singh** – USN: `25MCAR0219`

---

## 1. Executive Summary & AI Use-Case Proposal

**MentalCare AI** is an intelligent, privacy-preserving mobile wellness application designed to help individuals monitor psychological strain, recognize emerging stress indicators, and prevent chronic burnout. 

Building upon the mobile UI, local persistence, and workflow established in **CA1**, the **CA3** phase integrates a specialized **Artificial Intelligence (AI) and Natural Language Processing (NLP)** pipeline. The application transforms unstructured, user-written journal or mental check-in text into structured stress classifications, explains the underlying factors causing distress, and models multi-day stress trends to deliver proactive early-warning notifications.

> 🛡️ **Responsible AI & Non-Diagnostic Disclaimer:**  
> MentalCare AI is strictly an assistive wellness and early-warning support tool. It does **NOT** provide clinical diagnosis, psychiatric evaluations, or medical treatment for depression, anxiety, or clinical mental disorders. It assists users in recognizing lifestyle stress patterns and adopting healthy coping mechanisms.

---

## 2. The AI Use-Case Definition

### Problem Statement
Modern professionals and students experience significant cognitive strain from academic deadlines, prolonged screen time, and erratic sleep cycles. Traditional mental health applications rely heavily on rigid numeric survey forms (e.g., Likert scales), which fail to capture emotional context, subjective burnout nuances, and linguistic indicators of exhaustion.

### Proposed AI Solution
Users express their daily thoughts freely in natural language (e.g., *"I have been working continuously for several days. I am unable to sleep properly and I feel exhausted."*). 

The AI system:
1. **Ingests and Preprocesses** the unstructured text.
2. **Extracts Semantic & Domain Features** using Term Frequency-Inverse Document Frequency (TF-IDF) n-grams and indicator lexicons.
3. **Classifies Stress Level** into three primary ordinal classes: `LOW`, `MODERATE`, or `HIGH`.
4. **Calculates Confidence Scores** to reflect model certainty.
5. **Extracts Explainable Risk Indicators** (e.g., *Work pressure*, *Poor sleep*, *Mental fatigue*).
6. **Analyzes Multi-Day Trajectories** to flag cumulative burnout patterns when stress scores consistently rise across consecutive check-ins.
7. **Generates Actionable, Personalized Recommendations** targeting identified stressors.

---

## 3. End-to-End AI System Flow & Architecture

```
┌────────────────────────────────────────────────────────┐
│                      User Action                       │
│     (Writes journal entry / daily mental check-in)     │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                   Android Mobile App                   │
│        (CheckInFragment with UI Loading State)         │
└───────────────────────────┬────────────────────────────┘
                            │
               ┌────────────┴────────────┐
               ▼ (Online REST API)       ▼ (Offline Fallback)
┌──────────────────────────────┐  ┌──────────────────────────────┐
│     Python Flask Backend     │  │      Embedded On-Device      │
│   (app.py / nlp_engine.py)   │  │   LocalNLPEngine (Kotlin)    │
└──────────────┬───────────────┘  └──────────────┬───────────────┘
               │                                 │
               ▼                                 ▼
┌────────────────────────────────────────────────────────────────┐
│                   NLP & Machine Learning Core                  │
│  1. Text Normalization (Regex, lowercasing, cleaning)          │
│  2. Crisis Keyword Scanning (Safety protocol trigger)          │
│  3. TF-IDF Vectorization (Unigrams & Bigrams)                  │
│  4. Logistic Regression Classifier (Multi-class Inference)     │
│  5. Contextual Indicator Tagging (Work / Sleep / Fatigue)      │
│  6. Recommendation Generation Engine                           │
└──────────────────────────────┬─────────────────────────────────┘
                               │
                               ▼
┌────────────────────────────────────────────────────────────────┐
│                    Prediction Output & Storage                 │
│  • Stress Level: LOW | MODERATE | HIGH                         │
│  • Stress Score: 0 - 100% | Model Confidence %                 │
│  • Stored in Room SQLite (MentalCareDatabase)                  │
└──────────────────────────────┬─────────────────────────────────┘
                               │
                               ▼
┌────────────────────────────────────────────────────────────────┐
│                   Real-Time User Experience                    │
│  • Immediate assessment card with color-coded risk badge       │
│  • Dashboard LiveData sync & 7-day average update              │
│  • Multi-day Analytics progression & Burnout Alert Banner      │
│  • System notification if stress score >= 75% or crisis flag   │
└────────────────────────────────────────────────────────────────┘
```

---

## 4. NLP & Machine Learning Technical Details

### A. Feature Extraction Pipeline
* **Text Preprocessing:** Punctuation removal, lowercasing, and whitespace normalization.
* **Vectorization:** TF-IDF (`ngram_range=(1, 2)`, `max_features=1000`) converts free-form sentences into dense numerical matrices prioritizing emotionally informative words over neutral stopwords.
* **Domain Indicators:** Keyword scanning maps phrases to 3 key psychological domains:
  1. *Work Pressure:* `deadline`, `workload`, `project`, `office`, `exam`, `assignment`.
  2. *Poor Sleep:* `insomnia`, `awake`, `restless`, `sleep`, `night`.
  3. *Mental Fatigue:* `exhausted`, `burnt out`, `brain fog`, `drained`, `no energy`.

### B. Machine Learning Classification
* **Model:** Logistic Regression with `predict_proba()` output.
* **Target Classes:**
  * `0: LOW` (Normal wellness, positive or neutral valence)
  * `1: MODERATE` (Elevated demands, manageable tension)
  * `2: HIGH` (Severe stress indicators, acute mental exhaustion)
* **Crisis Safety Filter:** Independent heuristic scanner for extreme distress keywords (`suicide`, `hopeless`, `can't go on`), automatically surfacing crisis support contacts and hotlines (`988`).

### C. Multi-Day Burnout Pattern Recognition Algorithm
Burnout is not determined by a single isolated bad day, but by sustained, unresolved strain. The burnout detector evaluates the sequence $S = [s_1, s_2, \dots, s_n]$ of the last 5 check-in scores:
$$\text{Average Score} = \frac{1}{k} \sum_{i=n-k+1}^{n} s_i$$
$$\text{Is Increasing} = \forall i \in [1, k-1]: s_i \le s_{i+1}$$
If stress scores are strictly non-decreasing over $\ge 4$ evaluations with an average $>58\%$, or if the multi-day average exceeds $75\%$, the system flags an **Increasing Stress / Burnout Risk Warning**.

---

## 5. Reused CA1 Functionality vs. New CA3 AI Functionality

| Component / Layer | CA1 Implementation (Reused) | CA3 AI System (New / Enhanced) |
| :--- | :--- | :--- |
| **Android UI & Navigation** | Bottom navigation, 16 custom layouts, Green/Mint/Purple styling, custom buttons | Explicit AI analysis loading state (`ProgressBar`), AI Confidence Chip, Responsible AI Disclaimer Card |
| **Check-in Processing** | Basic text input field | End-to-end NLP pipeline connecting to `/predict`, extracting indicators, displaying confidence & tailored recommendations |
| **AI Inference** | Basic local mock scores | Dual-Engine AI Architecture: Python TF-IDF + Logistic Regression REST backend + Offline `LocalNLPEngine` fallback |
| **Database & History** | Room DB (`UserEntity`, `CheckInEntity`, `LifestyleEntity`) | Persistent AI stress scores, indicators, and recommendations synced to live Dashboard & multi-day charts |
| **Pattern Analysis** | Static lifestyle score calculator | Algorithmic multi-day burnout detection analyzing score trends over 3 to 7 days |
| **Crisis & Support** | Emergency dialing intent | Context-aware crisis interception in AI analysis triggering immediate 988 emergency guidance |

---

## 6. REST API Specifications

The Python backend exposes clean JSON endpoints for cloud and local integration:

### 1. `GET /health`
Returns system status, active version, and Group 11 team member information.

### 2. `POST /predict` (also `POST /checkin` & `POST /api/analyze_text`)
**Request:**
```json
{
  "text": "I have been working continuously for several days. I am unable to sleep properly and I feel exhausted."
}
```
**Response:**
```json
{
  "app_name": "MentalCare AI",
  "text": "I have been working continuously for several days. I am unable to sleep properly and I feel exhausted.",
  "stress_level": "HIGH",
  "stress_score": 81,
  "confidence": 0.89,
  "status_description": "High stress indicators detected",
  "indicators": ["Work pressure", "Poor sleep", "Mental fatigue"],
  "is_crisis": false,
  "recommendations": [
    "Consider taking a short break and prioritizing rest.",
    "😴 Sleep Recovery: Practice a 10-minute bedtime relaxation audio.",
    "⏸️ Workload Break: Take a mandatory 15-minute break and delegate urgent non-essential tasks."
  ],
  "disclaimer": "MentalCare AI is a wellness and early-warning support system, not a medical diagnostic application."
}
```

### 3. `POST /api/analyze_burnout`
**Request:**
```json
{
  "scores": [35, 48, 62, 71, 79]
}
```
**Response:**
```json
{
  "average_score": 59.0,
  "burnout_risk": "HIGH",
  "is_burnout_pattern": true,
  "recent_scores": [35, 48, 62, 71, 79],
  "trend_message": "Increasing Stress Pattern Detected! Your stress levels have risen consistently over the past 5 days (Avg 59%). Consider prioritizing rest and speaking with a trusted person.",
  "recommendations": [
    "Take 1-2 restorative rest days away from work/study responsibilities.",
    "Discuss workload adjustments with your manager or team.",
    "Schedule a session with your trusted contact or a wellness counselor."
  ]
}
```

---

## 7. Preparation for CA3 Review 2

For **CA3 Review 2 (AI Pipeline Implementation & Validation)**, the project is structured with:
1. Pre-trained model artifacts (`stress_model.pkl` and `tfidf_vectorizer.pkl`) verified and deployable.
2. Complete standalone deployment bundle with `Procfile`, `requirements.txt` (with `gunicorn`), and `.gitignore`.
3. Fully functional Android APK (`app-debug.apk` and `app-release-unsigned.apk`) verified with `BUILD SUCCESSFUL`.
4. Visual test states covering Low, Moderate, High, and Crisis inputs with explainable badges and notifications.

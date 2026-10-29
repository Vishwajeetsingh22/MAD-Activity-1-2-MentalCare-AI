
# MentalCare AI – Real-Time AI-Powered Stress & Burnout Detector

> *"Understand. Relax. Thrive."*

---

## 👥 Academic Project Information & Team Members
* **Project Name:** MentalCare AI
* **Full Title:** AI MentalCare – Real-Time AI-Powered Stress & Burnout Detection Mobile Application
* **Course:** Mobile Application Development (MCA) – Continuous Assessment (CA1 & CA3)
* **Department:** Department of Master of Computer Applications (MCA)
* **Institution:** Jain (Deemed-to-be University)
* **Group:** Group 11
* **Team Members:**
  1. **Joylyn Princita Fernandes** – USN: `25MCAR0099`
  2. **Syed Faizan Pasha** – USN: `25MCAR0138`
  3. **Vishwajeet Singh** – USN: `25MCAR0219`

---

## ℹ️ Important Wellness Disclaimer & Responsible AI
**MentalCare AI** is engineered strictly as an **assistive wellness and early-warning support application**, not a clinical medical diagnostic tool. It does NOT diagnose clinical depression, anxiety disorders, PTSD, or psychiatric illnesses. The AI estimates stress trends to encourage mindful lifestyle balance, stress awareness, healthy habits, and timely referral to trusted friends or qualified healthcare professionals.

---

## 🌟 Major Features (Comprehensive Master Implementation)

### 1. 🚀 Personalized Onboarding Flow (8 Screens)
* Multi-step structured onboarding storing user preferences per account:
  * **Screen 1 — Welcome:** *"Understand. Relax. Thrive."* with `[Get Started]` and `[I Already Have an Account]`.
  * **Screen 2 — Personalization:** Preferred name, age range (18-24, 25-34, 35-44, 45+), occupation (Student, Working, Other).
  * **Screen 3 — Wellness Goals:** Multi-select chips (Understand stress, Improve focus, Productivity, Sleep better, Feel calmer, Healthier relationships, Motivation, Manage pressure, Healthy habits, Balance + custom goal).
  * **Screen 4 — Current Challenges:** Multi-select chips (Academic pressure, Work pressure, Deadlines, Relationships, Financial, Sleep problems, Overthinking, Focus, Social media, Procrastination, Feeling overwhelmed).
  * **Screen 5 — Habits to Improve:** Sleep, Exercise, Screen time, Relaxation, Morning routine, Time management, Study pacing.
  * **Screen 6 — Sleep Wellbeing:** Selectable patterns (Restorative sleep, Waking tired, Waking during night, Racing mind, Irregular schedule).
  * **Screen 7 — Daily Commitment:** 5 min, 10 min, 15 min, 20+ min per day.
  * **Screen 8 — Generated Personalized Plan:** Tailored daily wellness routine automatically generated from user selections and seeded into Room DB.
* Smart Routing: Returning users skip directly to the Home Dashboard without repeating onboarding.

### 2. 📋 Daily 10-Question Check-in Questionnaire
* Structured 10-question wellness check-in:
  1. Energy level today (1–5 scale)
  2. Overthinking frequency (1–5 scale)
  3. Distraction / concentration level (1–5 scale)
  4. Emotional balance / mood changes (1–5 scale)
  5. Overall feeling overwhelmed (1–5 scale)
  6. Ease of expressing emotions (1–5 scale)
  7. Decision-making clarity (1–5 scale)
  8. Motivation level (1–5 scale)
  9. Productivity output (1–5 scale)
  10. Sleep quality last night (1–5 scale)
* Real-time progress indicator (`Question X of 10`), interactive rating buttons with emoji cues, and immediate stress score calculation.
* Automatically updates today's Daily Plan and consistency streaks upon completion.

### 3. ✍️ Wellness Journal with Optional AI Analysis
* Dedicated private journal section:
  * Create, edit, search, and delete entries.
  * Date/time stamps and mood tag chips (🌿 Calm, 😊 Happy, 😐 Neutral, 😟 Stressed, 😴 Tired).
  * **[✨ Analyze with AI]:** Explicit user-triggered AI analysis using the NLP model to estimate stress level (LOW/MODERATE/HIGH) and actionable wellness recommendations without diagnostic claims.
  * User data isolation ensures private reflections remain strictly per-user.

### 4. 🧠 NLP Stress & Burnout AI Models (Preserved Pipeline)
* Python Machine Learning pipeline:
  * TF-IDF feature extraction with Logistic Regression / Linear SVM trained model (`stress_pipeline_model.joblib`).
  * Classifies input into LOW, MODERATE, HIGH stress levels with confidence scores and indicator extraction.
  * On-device Kotlin NLP engine (`LocalNLPEngine.kt`) guarantees offline continuity without server dependency.

### 5. 💡 Personalized Insights & Real Consistency Streaks
* Strictly backed by real Room SQLite data—**Zero Fabricated Statistics**:
  * **Consistency Streaks:** Consecutive days check-in streak calculation from actual timestamps (e.g. `3 Day Streak`).
  * Total check-ins logged and journal entries count.
  * Recent stress trend (Improving, Stable, Increasing) based on historical scores.
  * Sleep & energy patterns derived from lifestyle records.
  * If insufficient data exists, shows: *"Complete more check-ins to unlock personalized insights."*

### 6. 📈 Custom Canvas Stress Trend Curve
* Custom Android Canvas view (`StressChartView`):
  * Renders real historical stress scores across days with dynamic bezier curves and day/night contrast.
  * 7-day average percentage calculation.
  * Multi-day burnout risk pattern detector with automatic notification alerts.

### 7. 🎯 Wellness Goals
* Goal management interface:
  * Add goal with custom description and category (Sleep, Focus, Mindfulness, Habits, Routine).
  * Mark goals complete with strike-through feedback.
  * Active vs completed goals counter.
  * Delete goals with confirmation prompt.

### 8. 🗓️ Today's Wellness Plan
* Structured daily wellness checklist:
  * 1. Complete mental check-in
  * 2. 5-minute breathing activity
  * 3. Write one journal entry
  * 4. Take a short break
  * 5. Complete evening reflection
* Progress tracker: `X / 5 completed` with auto-check on database events.

### 9. ✨ Wellness Activities Catalog (8 Categories)
* Catalog of evidence-informed exercises:
  1. **Breathing:** 5-Minute Box Breathing
  2. **Relaxation:** 5-4-3-2-1 Sensory Grounding
  3. **Focus:** 25-Minute Pomodoro Focus Block
  4. **Journaling:** Reflective Gratitude Journal
  5. **Sleep Routine:** Evening Screen Wind-down
  6. **Mindfulness:** Gentle Body Scan Meditation
  7. **Productivity:** Top-3 Priority Task Triage
  8. **Healthy Habits:** Hydration & Posture Reset
* Interactive countdown timer modal with start, pause, and completion tracking.

### 10. 💬 Context-Aware AI Chatbot (Multi-Turn Conversational Assistant)
* **Real Conversational AI Architecture:**
  * Multi-turn conversation memory with context sliding window.
  * Understands context across turns:
    * Exam stress -> Exams tomorrow -> Mathematics preparation (Test 1).
    * Sleep difficulty -> Happens almost every night (Test 2).
    * Study plan requests with Pomodoro breakdown (Test 3).
    * 5-minute relaxation requests with guided breathwork (Test 4).
  * `[+ New Chat]` button creates a fresh conversation ID without mixing previous context.
  * `[History]` drawer allows reopening past conversation threads.
  * Typing indicator (*"AI is thinking..."*).
  * **Crisis Safety Guardrails:** Immediately detects self-harm ideation, flags `is_crisis = True`, provides compassionate de-escalation, recommends the 988 Crisis Lifeline, and displays a **`[📞 Call Emergency Contact]`** button with explicit confirmation before dialing.
  * **Medical Boundary Enforcement:** Disclaims clinical authority and refers to licensed doctors.
  * Dual-Engine Execution: Native support for Google Gemini API (`GEMINI_API_KEY`) and OpenAI (`OPENAI_API_KEY`) on the backend, plus intelligent contextual stateful dialog manager when operating locally, and on-device `LocalChatEngine.kt` when offline.

### 11. 👥 Emergency Contacts & 🆘 SOS Settings
* User-isolated emergency contacts list (Name, Phone, Relationship).
* Safe calling protocol: confirmation dialogs before initiating phone calls or deleting contacts.
* Editable SOS number with phone number validation.

### 12. 🌓 Complete Light / Dark / System Theme Switching
* High-contrast day/night switching across all activities and fragments.
* Persistent theme storage in `MentalCarePrefs`.

---

## 🌐 External AI Provider Configuration (Part 26)

If an external AI provider (such as Google Gemini) is used:

1. **AI Provider:** **Google Gemini API** (Recommended: model `gemini-1.5-flash`).
2. **Where to Create Key:** Google AI Studio: [https://aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey)
3. **Where to Place Key:** On your backend server only in:
   `backend_server/.env`
   ```env
   GEMINI_API_KEY=AIzaSyYourActualKeyHere
   ```
4. **Environment Variable:** `GEMINI_API_KEY` (or `OPENAI_API_KEY`).
5. **How to Test:**
   ```powershell
   py backend_server/test_chat.py
   ```
*Security Guarantee:* The API key is **never** embedded inside the Android application. Android communicates exclusively with the backend via `POST /chat`.

---

## 📸 Demonstration Screenshots Catalog

| Screen # | Screen Title | Filename | Key Visual Elements Shown |
|:---:|:---|:---|:---|
| **01** | Welcome & Onboarding | `01_onboarding_welcome.png` | MentalCare AI logo, slogan *"Understand. Relax. Thrive."*, Get Started |
| **02** | Personalization | `02_onboarding_personalize.png` | Name, age range chips, student/working role selection |
| **03** | Wellness Goals | `03_onboarding_goals.png` | Multi-select goals chips and custom goal input |
| **04** | Challenges & Sleep | `04_onboarding_challenges.png` | Academic pressure, deadlines, sleep pattern selector |
| **05** | Generated Plan | `05_onboarding_plan.png` | Tailored daily routine generated from user answers |
| **06** | Home Dashboard | `06_home_dashboard.png` | Greeting, mood selector, AI Check-in, Chat, Insights, Today's Plan, Goals |
| **07** | Daily 10-Q Check-in | `07_daily_checkin_flow.png` | 10 wellness questions with 1-5 scale, progress counter, emojis |
| **08** | Check-in Result | `08_checkin_result.png` | Stress level badge (LOW/MODERATE/HIGH), score, recommendations |
| **09** | Wellness Journal | `09_wellness_journal.png` | List of entries, mood tags, search bar, + New Entry dialog |
| **10** | Journal AI Analysis | `10_journal_ai_analysis.png` | [Analyze with AI] result badge with estimated stress & advice |
| **11** | Insights & Streaks | `11_insights_streaks.png` | Real check-in streak, total counts, personalized trend insights |
| **12** | Stress Trend Chart | `12_stress_trend_chart.png` | Custom canvas curve, 7-day average, burnout risk detector |
| **13** | Today's Plan | `13_todays_plan.png` | 5 daily tasks checklist, completion counter (e.g. 3/5 completed) |
| **14** | Wellness Activities | `14_wellness_activities.png` | 8 exercise categories with countdown timer modal |
| **15** | AI Wellness Chatbot | `15_ai_chatbot_dialog.png` | Multi-turn conversational flow, user/AI bubbles, typing indicator |
| **16** | Chat Crisis Safety | `16_chat_crisis_safety.png` | 988 lifeline, de-escalation response, [Call Emergency Contact] button |
| **17** | Emergency Contacts | `17_emergency_contacts.png` | Contact list per user, safe call confirmation prompt |
| **18** | Settings & Themes | `18_settings_screen.png` | Light / Dark / System mode toggle, Account, Safety, About |

---

## 🚀 How to Run the Project

### 1. Launch Android Application in Android Studio
1. Open **Android Studio**.
2. Click **Open** and select:
   `C:\Users\admin\.gemini\antigravity\scratch\AIMentalCare\AIMentalCareApp`
3. Connect an Android Emulator or device.
4. Press **Run (Shift + F10)**.

### 2. Start Python AI Backend REST API
In a PowerShell or Command Prompt terminal:
```powershell
cd C:\Users\admin\.gemini\antigravity\scratch\AIMentalCare\backend_server
py app.py
```
*(Server listens on port 5000: `http://localhost:5000/`)*

### 3. Run Automated Multi-Turn Chatbot Tests
```powershell
cd C:\Users\admin\.gemini\antigravity\scratch\AIMentalCare\backend_server
py test_chat.py
```
=======
# 🧠 MentalCare AI

## AI-Powered Stress & Burnout Detection Mobile Application

> **Understand. Relax. Thrive.**

MentalCare AI is an AI-powered mobile wellness application developed as an academic project to help users understand and monitor their stress levels through AI-assisted mental check-ins and journal entries.

The application uses **Natural Language Processing (NLP)** and **Machine Learning (ML)** to analyze user-provided text and classify the estimated stress level as **Low, Moderate, or High**. It also maintains stress history, analyzes trends, and provides wellness recommendations.

> ⚠️ **Disclaimer:** MentalCare AI is a wellness and early-warning support application. It is not a medical diagnostic system and should not replace professional medical, psychological, or emergency assistance.

---

## 📌 Project Information

| Detail | Information |
|---|---|
| **Project Name** | MentalCare AI |
| **Project Title** | AI MentalCare – Real-Time AI-Powered Stress & Burnout Detection Mobile Application |
| **Group Number** | 11 |
| **Department** | Master of Computer Applications (MCA) |
| **College** | Jain (Deemed-to-be University) |
| **Campus** | JGI Knowledge Campus, Jayanagar, Bengaluru |

---

## 🎯 Aim

The aim of MentalCare AI is to develop a mobile wellness application that uses AI, NLP, and Machine Learning to analyze users' written mental check-ins and provide an estimated stress level with useful wellness recommendations.

---

## ❗ Problem Statement

Students, employees, and professionals can experience stress due to academic pressure, workload, deadlines, long working hours, lack of sleep, and personal responsibilities.

Users may not always notice that their stress is increasing over time. MentalCare AI provides a simple platform for recording daily check-ins and using AI-assisted analysis to identify possible increasing stress patterns.

---

## 💡 Proposed Solution

MentalCare AI combines:

- 📱 Android mobile application
- 🤖 Artificial Intelligence
- 🧠 Natural Language Processing
- 📊 Machine Learning
- ☁️ Firebase
- 🔗 REST API
- 🔔 Notifications
- 📈 Stress history and trend analysis
- 💡 Wellness recommendations

### Basic Flow

```text
User Check-in
      ↓
Journal / Text Input
      ↓
REST API
      ↓
NLP Processing
      ↓
Machine Learning Model
      ↓
Stress Classification
      ↓
Low / Moderate / High
      ↓
Firebase
      ↓
History + Trend + Recommendation
```

---

# ✨ Key Features

### 🔐 User Authentication
- User registration
- User login
- Logout
- Firebase authentication

### 🏠 Home Dashboard
- Latest stress information
- Recent check-ins
- Quick access to AI check-in
- Recommendations
- Notifications

### 🧠 AI Mental Check-in
Users can enter their thoughts, feelings, or daily experiences.

### 🤖 AI Stress Analysis
The backend analyzes the submitted text using NLP and Machine Learning.

### 📊 Stress Classification

```text
🟢 Low
🟠 Moderate
🔴 High
```

### 📈 Stress History
Stores previous stress assessments and check-ins.

### 📉 Stress Trend
Tracks stress levels over multiple days and can identify increasing patterns.

### ⚠️ Increasing Stress Pattern
The application can display an early-warning message when stress indicators show a sustained increasing pattern.

### 💡 Personalized Recommendations
Provides general wellness suggestions based on the estimated stress level.

### 🔔 Notifications
Provides wellness reminders and relevant application notifications.

### 👤 Profile & Settings
Allows users to manage profile and application settings.

### 🆘 Trusted Support
Provides a section for trusted support/emergency contact information.

### 📱 Responsive UI
Designed for different Android screen sizes with flexible layouts and readable components.

---

# 🔄 Application Workflow

```text
Launch Application
        ↓
Splash Screen
        ↓
Login / Registration
        ↓
Home Dashboard
        ↓
AI Mental Check-in
        ↓
Enter Journal / Feelings
        ↓
Submit for Analysis
        ↓
AI / NLP Processing
        ↓
Stress Prediction
        ↓
Low / Moderate / High
        ↓
Save Result
        ↓
Stress History
        ↓
Trend Analysis
        ↓
Wellness Recommendation
        ↓
Notification / Support
```

---

# 🏗️ System Architecture

```text
┌─────────────────────────────────────┐
│          Android Application        │
│                                     │
│       Kotlin + XML + Material       │
│                                     │
│ Login | Dashboard | Check-in        │
│ Result | History | Profile          │
└──────────────────┬──────────────────┘
                   │
                   │ HTTPS REST API
                   ▼
┌─────────────────────────────────────┐
│          Python Backend             │
│          Flask / FastAPI            │
│                                     │
│ API Handling | Validation           │
│ NLP Processing | ML Prediction      │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│             AI / ML Layer           │
│                                     │
│ Text Preprocessing                  │
│ TF-IDF Feature Extraction           │
│ Machine Learning Model              │
│ Stress Classification               │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│              Firebase               │
│                                     │
│ Authentication                      │
│ Firestore / Realtime Database       │
│ Cloud Messaging                     │
└─────────────────────────────────────┘
```

---

# 🤖 AI / ML Workflow

### 1. Text Input

The user enters a mental check-in.

Example:

```text
I have been working continuously for several days.
I am unable to sleep properly and I feel exhausted.
```

### 2. Text Preprocessing

The backend prepares the text for analysis using appropriate NLP preprocessing.

### 3. Feature Extraction

Text can be converted into numerical features using:

```text
TF-IDF
```

### 4. Machine Learning

A trained classification model analyzes the extracted features.

Possible models include:

```text
Logistic Regression
Support Vector Machine (SVM)
```

### 5. Prediction

The model returns an estimated stress category:

```text
Low
Moderate
High
```

### 6. Result

The result is returned to the Android application.

Example:

```json
{
  "stress_level": "High",
  "confidence": 0.87
}
```

---

# 🛠️ Technology Stack

## Frontend
- Android Studio
- Kotlin
- XML
- Material Design

## Backend
- Python
- Flask / FastAPI
- REST API

## AI / Machine Learning
- Natural Language Processing
- TF-IDF
- Scikit-learn
- Logistic Regression / SVM

## Cloud & Database
- Firebase Authentication
- Cloud Firestore / Realtime Database
- Firebase Cloud Messaging

## Development Tools
- Antigravity
- Git
- GitHub
- Android Studio

## Deployment
- Android APK / AAB
- Cloud-hosted Python backend
- Firebase

---

# 📱 Application Screens

The application includes:

1. Splash Screen
2. Login Screen
3. Registration Screen
4. Home Dashboard
5. AI Mental Check-in
6. Journal / Text Input
7. AI Analysis
8. Stress Result
9. Stress History
10. Stress Trend
11. Increasing Stress Pattern Warning
12. Personalized Recommendations
13. Notifications
14. Profile
15. Settings
16. Trusted Support

---

# 📸 Screenshots

> Place the actual application screenshots inside the `screenshots/` folder.  
> Replace the filenames below if your actual screenshot names are different.

## 1. Splash Screen

<img width="1080" height="2400" alt="WhatsApp Image 2026-09-18 at 9 27 15 PM" src="https://github.com/user-attachments/assets/f18d72c1-1f00-4c85-8180-2b0c6cb5a8a3" />
![Splash Screen](screenshots/splash.png)

## 2. Login Screen

<img width="720" height="1600" alt="WhatsApp Image 2026-09-18 at 9 27 16 PM" src="https://github.com/user-attachments/assets/a0a0ac6f-e7a2-4481-b1ea-02f1d2f52190" />
![Login Screen](screenshots/login.png)

## 3. Registration Screen

<img width="720" height="1600" alt="WhatsApp Image 2026-09-18 at 9 27 16 PM (2)" src="https://github.com/user-attachments/assets/3c02e8c9-23de-49aa-9966-63fb00984b1d" />
![Registration Screen](screenshots/register.png)

## 4. Home Dashboard

<img width="720" height="1600" alt="WhatsApp Image 2026-09-18 at 9 27 16 PM (1)" src="https://github.com/user-attachments/assets/834b9abb-a06f-41f5-90d7-4a436d23425a" />
![Home Dashboard](screenshots/dashboard.png)

## 5. AI Mental Check-in

<img width="720" height="1600" alt="WhatsApp Image 2026-09-18 at 9 27 17 PM" src="https://github.com/user-attachments/assets/acb3f286-83ed-4c32-81c8-ae593980dee7" />
![AI Mental Check-in](screenshots/checkin.png)

## 6. AI Analysis

<img width="720" height="1600" alt="WhatsApp Image 2026-09-18 at 9 27 18 PM" src="https://github.com/user-attachments/assets/4c7663a5-1d3e-4e05-abd0-7b8503cf9219" />
![AI Analysis](screenshots/analysis.png)

## 7. Stress Result

<img width="720" height="1600" alt="WhatsApp Image 2026-09-18 at 9 27 17 PM (1)" src="https://github.com/user-attachments/assets/671c0a2e-864d-4af8-bd98-2561539610cd" />
![Stress Result](screenshots/stress-result.png)

## 12. Profile

<img width="720" height="1600" alt="WhatsApp Image 2026-09-18 at 9 27 17 PM (2)" src="https://github.com/user-attachments/assets/dd8f2faf-d73f-4048-9b52-c87e315ce714" />
![Profile](screenshots/profile.png)

---

# 🔥 Firebase Integration

Firebase can be used for:

### Authentication
- Registration
- Login
- User authentication
- Account management

### Database
- User information
- Mental check-ins
- Stress results
- Stress history
- Trend information

### Cloud Messaging
- Wellness reminders
- Application notifications
- Relevant alerts

---

# 🔗 Backend API

The Android application communicates with the Python backend through REST APIs.

## Health Check

```http
GET /health
```

Example response:

```json
{
  "status": "ok"
}
```

## Stress Prediction

```http
POST /predict
```

Example request:

```json
{
  "text": "I feel exhausted and stressed because of continuous work."
}
```

Example response:

```json
{
  "stress_level": "High",
  "confidence": 0.87
}
```

## Check-in

```http
POST /checkin
```

This endpoint can be used to process and store a user's check-in.

---

# 🚀 Installation

## Prerequisites

Install:

- Android Studio
- JDK
- Android SDK
- Git
- Python 3.x
- Firebase account

---

## 📥 Clone Repository

```bash
git clone https://github.com/Vishwajeetsingh22/MentalCare-AI.git
```

Navigate into the project:

```bash
cd MentalCare-AI
```

Open the project in Android Studio and allow Gradle synchronization to complete.

---

# 📱 Android Setup

1. Open Android Studio.
2. Select **Open Project**.
3. Select the `MentalCare-AI` directory.
4. Allow Gradle to sync.
5. Install required SDK components.
6. Connect an Android device or start an emulator.
7. Run the application.

---

# 🐍 Backend Setup

If the project contains a backend directory:

```bash
cd backend
```

Create a virtual environment:

```bash
python -m venv venv
```

For Windows:

```bash
venv\Scripts\activate
```

Install dependencies:

```bash
pip install -r requirements.txt
```

Run the backend:

```bash
python app.py
```

---

# ⚙️ Environment Configuration

Sensitive configuration should not be hard-coded.

Example:

```text
API_URL=
FIREBASE_PROJECT_ID=
FIREBASE_CLIENT_EMAIL=
FIREBASE_PRIVATE_KEY=
```

Use environment variables or secure configuration methods for sensitive values.

> Never upload private service-account keys, passwords, or secret credentials to GitHub.

---

# ☁️ Deployment

The intended deployment architecture is:

```text
Android Application
        ↓
      HTTPS
        ↓
Python AI Backend
        ↓
 NLP + ML Model
        ↓
     Firebase
        ↓
Authentication / Database / Notifications
```

### Android Application

Generate a release APK/AAB from Android Studio for demonstration and testing.

### AI Backend

The Python Flask/FastAPI backend can be deployed to a cloud hosting platform such as Render.

### Firebase

Firebase provides cloud services such as:

- Authentication
- Database
- Real-time synchronization
- Notifications

---

# 🧪 Testing

## Authentication
- [ ] Registration
- [ ] Login
- [ ] Logout
- [ ] Invalid credentials
- [ ] Authentication errors

## AI Check-in
- [ ] Text input
- [ ] Empty input
- [ ] Low stress example
- [ ] Moderate stress example
- [ ] High stress example
- [ ] Long input

## Backend
- [ ] API connection
- [ ] API response
- [ ] Invalid request
- [ ] Server unavailable
- [ ] Network timeout

## Firebase
- [ ] Authentication
- [ ] Data storage
- [ ] Data retrieval
- [ ] Real-time updates
- [ ] Notifications

## Application
- [ ] Navigation
- [ ] Back button
- [ ] Loading states
- [ ] Error states
- [ ] Responsive layouts
- [ ] Different Android screen sizes

---

# 🔒 Security

The project follows basic security practices:

- Do not hard-code passwords.
- Do not commit private API keys.
- Do not upload Firebase service-account private keys.
- Use environment variables for backend secrets.
- Validate API input.
- Use HTTPS for production API communication.
- Configure appropriate Firebase security rules.
- Store only necessary user information.

---

# ⚠️ Limitations

1. AI predictions depend on the quality of the training dataset.
2. Text-based analysis cannot completely understand a person's mental state.
3. Stress classification is an AI-based estimate.
4. The system does not provide a medical diagnosis.
5. Increasing-stress indicators are pattern-based and should not be treated as a clinical burnout assessment.
6. Cloud-based AI analysis may require internet connectivity.
7. Model performance may vary across writing styles and languages.

---

# 🔮 Future Enhancements

- 🎙️ Voice-based emotional analysis
- 😊 Advanced emotion detection
- 🌍 Multilingual support
- 💬 AI wellness chatbot
- ⌚ Wearable device integration
- ❤️ Heart-rate based stress indicators
- 🧠 Deep-learning models
- 📊 Advanced analytics dashboard
- 🔔 Personalized notifications
- 🧘 Personalized wellness plans
- 📱 Improved accessibility
- ☁️ Advanced cloud model management

---

# 📂 Project Structure

The exact structure may vary depending on the final implementation.

```text
MentalCare-AI/
│
├── app/
│   ├── src/
│   └── build.gradle
│
├── backend/
│   ├── app.py
│   ├── requirements.txt
│   ├── model/
│   └── ...
│
├── screenshots/
│   ├── splash.png
│   ├── login.png
│   ├── register.png
│   ├── dashboard.png
│   ├── checkin.png
│   ├── analysis.png
│   ├── stress-result.png
│   ├── history.png
│   ├── trend.png
│   ├── recommendations.png
│   ├── notifications.png
│   └── profile.png
│
├── .gitignore
├── README.md
└── ...
```

---

# 📋 Review 5 – Final Demonstration Flow

```text
Launch Application
        ↓
Login / Registration
        ↓
Home Dashboard
        ↓
AI Mental Check-in
        ↓
Enter Sample Text
        ↓
AI Analysis
        ↓
Show Stress Result
        ↓
Save Result
        ↓
Stress History
        ↓
Stress Trend
        ↓
Increasing Stress Pattern
        ↓
Recommendation
        ↓
Notification
        ↓
Profile / Settings
```

---

# 🎥 Final Demonstration

The final demonstration can cover:

- Android application
- User authentication
- AI mental check-in
- NLP/ML stress analysis
- Low / Moderate / High classification
- Stress history
- Stress trend
- Wellness recommendations
- Firebase integration
- Notifications
- Backend API
- Deployment
- Responsive UI

---

# 👨‍💻 Authors

## Group 11

### 1. Joylyn Princita Fernandes
**USN:** `25MCAR0099`

### 2. Syed Faizan Pasha
**USN:** `25MCAR0138`

### 3. Vishwajeet Singh
**USN:** `25MCAR0219`

---

# 🎓 College Information

**Jain (Deemed-to-be University)**  
**JGI Knowledge Campus, Jayanagar, Bengaluru**  
**Department of Master of Computer Applications (MCA)**

---

# 📚 Academic Information

**Course:** Mobile Application Development (MAD)  
**Project:** MentalCare AI  
**Group:** 11  
**Academic Year:** 2026  
**Project Type:** Academic / College Project

---

# 🔗 GitHub Repository

**MentalCare AI**

https://github.com/Vishwajeetsingh22/MentalCare-AI

---

# ⚖️ Disclaimer

MentalCare AI is developed as an academic project for educational and wellness-support purposes.

The application's AI-generated stress classification and recommendations are not medical advice and should not be considered a diagnosis of stress, anxiety, depression, burnout, or any other mental-health condition.

Users experiencing serious or emergency concerns should seek appropriate professional or emergency assistance.

---

# 📄 License

This project is developed for **academic and educational purposes**.

© 2026 MentalCare AI – Group 11
>>>>>>> origin/main

# CA3 – REVIEW 3: APP DEVELOPMENT & FUNCTIONALITY REVIEW
## AI MentalCare – Real-Time AI-Powered Stress & Burnout Detection Mobile Application

**Academic Project Information:**
* **Course Activity:** Mobile Application Development – **CA3 (Review 3)**
* **Review Stage:** **App Development & Functionality Review**
* **Department:** Master of Computer Applications (MCA)
* **Institution:** Jain (Deemed-to-be University)
* **Group:** Group 11
* **Team Members:**
  1. **Joylyn Princita Fernandes** – USN: `25MCAR0099`
  2. **Syed Faizan Pasha** – USN: `25MCAR0138`
  3. **Vishwajeet Singh** – USN: `25MCAR0219`

---

## 1. Completed Functionality Verification Matrix

| # | Feature / Screen | Component / Class | Verification Status | Implementation Details |
| :--- | :--- | :--- | :---: | :--- |
| **1** | **Splash Screen** | `SplashActivity.kt` | ✅ Verified | Displays application branding, slogan (*"Understand. Relax. Thrive."*), and auto-routes to `LoginActivity`. |
| **2** | **User Registration** | `RegisterActivity.kt` | ✅ Verified | Validates inputs, saves user session to `SharedPreferences`, displays toast, and routes to `MainActivity`. |
| **3** | **User Login** | `LoginActivity.kt` | ✅ Verified | Validates credentials, establishes user session (`user_email`, `user_name`), and transitions to `MainActivity`. |
| **4** | **User Logout** | `ProfileFragment.kt` | ✅ Verified | Red `🚪 Log Out` button clears `SharedPreferences` session and safely navigates back to `LoginActivity`. |
| **5** | **Home Dashboard** | `DashboardFragment.kt` | ✅ Verified | Personalized greeting (`Good Morning, <User> 👋`), real-time stress gauge, and reactive LiveData alert banner. |
| **6** | **AI Mental Check-in** | `CheckInFragment.kt` | ✅ Verified | Multiline journal input with active analyzing progress indicator and button lock to prevent double clicks. |
| **7** | **Journal / Text Input** | `etCheckInText` | ✅ Verified | Handles empty, short, and extended free-form natural language reflections with validation. |
| **8** | **AI Stress Prediction** | `ApiClient.kt` / `app.py` | ✅ Verified | Calls `POST /predict` using the trained TF-IDF + Logistic Regression model with automatic `LocalNLPEngine.kt` fallback. |
| **9** | **Stress Result Display** | `cardResultContainer` | ✅ Verified | Displays color-coded risk badge (`LOW` #10B981, `MODERATE` #F59E0B, `HIGH` #EF4444), score %, and confidence %. |
| **10** | **Recommendations** | `txtResultRecommendations`| ✅ Verified | Actionable, indicator-specific wellness recommendations (sleep wind-down, workload breaks, box breathing). |
| **11** | **Save Result** | `MentalCareDatabase.kt` | ✅ Verified | Asynchronously inserts check-in entity into Room SQLite DB (`check_ins`) on `Dispatchers.IO`. |
| **12** | **Stress History** | `CheckInDao.kt` | ✅ Verified | Persistent historical queries (`getRecentCheckIns()`, `getLatestCheckIn()`) backing live views. |
| **13** | **Stress Trend View** | `AnalyticsFragment.kt` | ✅ Verified | Custom `StressChartView.kt` renders chronological line graph using actual stored user scores. |
| **14** | **Burnout Pattern Alert** | `runBurnoutScan()` | ✅ Verified | Detects consecutive rising stress scores; shows *"More check-ins are needed to identify a trend."* when < 3 entries exist. |
| **15** | **Alert Notifications** | `NotificationHelper.kt` | ✅ Verified | Fires system status-bar notification when stress score $\ge 75\%$ or multi-day burnout risk is flagged. |
| **16** | **User Profile** | `ProfileFragment.kt` | ✅ Verified | Displays active user name, email, project team info, and session status. |
| **17** | **Settings** | `SettingsFragment.kt` | ✅ Verified | Real-time notification toggle and user preferences. |
| **18** | **Trusted Support** | `SupportFragment.kt` | ✅ Verified | 1-click `988` emergency hotline dialer, contact notifier, and interactive 5-minute box-breathing timer. |
| **19** | **Wellness Disclaimer** | CheckIn / Dashboard / About | ✅ Verified | Prominent non-diagnostic disclaimer: *"AI-generated stress results are estimates for wellness support and are not medical diagnoses."* |

---

## 2. AI Integration & Fault Tolerance

```
Android App (CheckInFragment)
           │
           ▼
User taps "Perform AI Stress Analysis" (Button disabled, loading spinner displayed)
           │
           ▼
Asynchronous HTTP POST /predict to Python Flask
     ├── Online & Server Reachable:
     │     └── TF-IDF vectorizer + Logistic Regression model returns predicted level & confidence
     │
     └── Offline / Server Unavailable / Network Timeout:
           └── Catches exception gracefully and executes LocalNLPEngine.kt
           └── Displays toast: "Unable to connect to the AI service. Using offline analysis."
           │
           ▼
Result displayed on UI (Score, Level, Confidence %, Indicators, Recommendations)
           │
           ▼
Stored in Room Database & LiveData updates Dashboard & Analytics
```

---

## 3. Data Storage & User Isolation

* **Session Management:** Android `SharedPreferences` (`MentalCarePrefs`) isolates the active session (`user_email`, `user_name`).
* **Database Architecture:** Room SQLite Database (`mental_care_db`, version 2) with tables:
  * `check_ins`: id, timestamp, userText, stressLevel, statusDescription, stressScore, confidence, indicatorsCsv, isCrisis, recommendationsCsv.
  * `lifestyle_logs`: sleepHrs, workHrs, screenHrs, exerciseMins, energyLevel, moodEmoji, stressScore.
  * `Users`: userId, fullName, email, createdAt.
  * `TrustedContacts`: name, phone, relationship.

---

## 4. Error Handling States Implemented

| State | User Experience / UI Output |
| :--- | :--- |
| **Loading** | Button text: *"Analyzing your check-in..."* with visible indeterminate `ProgressBar`. |
| **Success** | Toast: *"Analysis completed"* + result card displayed. |
| **Empty Input** | Toast: *"Please enter a check-in before analyzing."* |
| **Network Failure** | Toast: *"Unable to connect to the AI service. Using offline analysis."* |
| **Backend Error** | Toast: *"AI analysis is temporarily unavailable. Using offline analysis."* |
| **Insufficient Data** | Trend card message: *"More check-ins are needed to identify a trend."* |

---

## 5. UI Responsiveness & Layout Adaptability

* **ScrollView Containers:** All screens (`fragment_checkin`, `fragment_dashboard`, `fragment_analytics`, `fragment_lifestyle`, `fragment_support`, `fragment_profile`, `fragment_about`) use `ScrollView` with `fillViewport="true"` to prevent clipping on small screens or when the soft keyboard is open.
* **Density-Independent Layouts:** Sized using `dp` and `sp` units with responsive `layout_weight` horizontal grids.
* **Zero Crash Guarantees:** All coroutines guard UI operations with `_binding ?: return@withContext`, fragment switches use `commitAllowingStateLoss()`, and DB calls use `applicationContext`.

MentalCare AI — Understand. Relax. Thrive.
AI MentalCare: Real-Time AI-Powered Stress & Burnout Detection Mobile Application
MAD Activity 1 & 2 | Group 11 | Jain (Deemed-to-be University), Bengaluru

A student project exploring digital mental wellness through daily check-ins, stress insights, journaling, and supportive AI-powered guidance.

GitHub repository Project type Status

Note: MentalCare AI is an academic wellness project, not a medical device or a substitute for professional mental-health care. Its outputs are intended for general wellness awareness and should not be used to diagnose a condition.

Table of contents
About the project
Project goals
Features
Promotional and demo videos
Screenshots
Technology and project structure
Source code
Getting started
Backend setup
Testing
Privacy and security
Known limitations
Team
Academic use
About the project
MentalCare AI is a mobile application concept designed to help users build awareness of their everyday emotional well-being. It brings together daily self-check-ins, stress-related insights, journaling, lifestyle and wellness activities, and a conversational wellness interface in one place.

The project explores how mobile app development and AI/ML techniques can support reflective habits and make wellness information easier to understand. The app is designed around the idea that small, regular check-ins can help users notice patterns and consider healthy coping strategies.

Project goals
Make daily emotional-wellness check-ins straightforward.
Present check-in history and stress-related insights in an understandable format.
Encourage reflection through journaling and wellness activities.
Provide supportive, non-diagnostic conversational guidance.
Demonstrate mobile development, data handling, API integration, and AI/ML concepts in an academic project.
Features
Area	What it is intended to support
Daily check-in	Record responses to a regular self-reflection questionnaire.
Stress insights	Review stress-related information and patterns based on available check-in data.
AI wellness chat	Interact with a conversational wellness interface for general supportive guidance.
Journal	Write personal reflections and revisit entries.
Lifestyle and wellness	Explore wellness activities and lifestyle-related tracking.
Dashboard	Access key app areas from a central screen.
Profile and settings	Manage available profile and app preferences.
Backend/API	Demonstrate communication between the mobile app and a Python API where configured.
Feature availability and behaviour may differ between the Android and Flutter project implementations included in this repository.

Promotional and demo videos
The promotional videos below are the actual MP4 files uploaded to this repository. They are linked by their current GitHub filenames—not to the advertisement image.

Playback: Select a video title to open its MP4 file on GitHub. GitHub normally provides a video preview on the file page where browser playback is supported.

MentalCare AI — English promotional video
▶ Watch MentalCare AI English Promo

Open MP4 file on GitHub

MentalCare AI — Hindi promotional video
▶ Watch MentalCare AI Hindi Promo

Open MP4 file on GitHub

Additional app demo videos
English app demo
Hindi app demo
Screenshots
The screenshots and screen recordings are stored in the Screenshots & Screenrecordings of app folder. Click a screenshot to open the full-size file on GitHub.

App interface
MentalCare AI app screenshot 1
App screenshot 1	MentalCare AI app screenshot 2
App screenshot 2	MentalCare AI app screenshot 3
App screenshot 3
MentalCare AI app screenshot 4
App screenshot 4	MentalCare AI app screenshot 5
App screenshot 5	MentalCare AI app screenshot 6
App screenshot 6
More images and screen recordings are available in the screenshots folder. GitHub README image previews require the linked files to exist at the exact paths shown above.

Technology and project structure
This repository contains multiple project folders related to MentalCare AI, including Android/Kotlin and Flutter source, plus a Python backend. Check the relevant folder's Gradle or Flutter configuration for the exact dependency versions used.

Main folders
MAD-Activity-1-2-MentalCare-AI/
├── AIMentalCare/
│   ├── AIMentalCareApp/          # Android application (Kotlin/Gradle)
│   ├── backend_server/            # Python API and AI/ML-related files
│   ├── lib/                       # Flutter/Dart source
│   ├── mentalcare_ai_flutter/     # Flutter application project
│   ├── test/                      # Flutter tests
│   └── README.md                  # Project-specific documentation
├── Screenshots & Screenrecordings of app/
├── Advertisment_image.jpeg
├── MentalCare_AI_Promo_English.mp4
├── MentalCare_AI_Promo_Hindi.mp4
├── App_demo_English(version).mp4
├── App_demo_hindi(version).mp4
├── MAD_Activity_Reviews_1&2.pdf
└── README.md
The repository also includes Gradle wrapper files, application resources, test files, model assets, and configuration templates. Not every folder is required for every build path.

Source code
Yes — the repository includes a project source folder named AIMentalCare. Open the links below to browse the source files directly on GitHub.

Component	Source-code location
Complete project source folder	[Browse AIMentalCare](https://github.com/Vishwajeetsingh22/MAD-Activity-1-2-MentalCare-AI/tree/main/AIMentalCare)
Android application (Kotlin/Gradle)	[Browse AIMentalCareApp](https://github.com/Vishwajeetsingh22/MAD-Activity-1-2-MentalCare-AI/tree/main/AIMentalCare/AIMentalCareApp)
Flutter/Dart source (lib)	[Browse lib](https://github.com/Vishwajeetsingh22/MAD-Activity-1-2-MentalCare-AI/tree/main/AIMentalCare/lib)
Flutter application project	[Browse mentalcare_ai_flutter](https://github.com/Vishwajeetsingh22/MAD-Activity-1-2-MentalCare-AI/tree/main/AIMentalCare/mentalcare_ai_flutter)
Python backend and AI/ML-related files	[Browse backend_server](https://github.com/Vishwajeetsingh22/MAD-Activity-1-2-MentalCare-AI/tree/main/AIMentalCare/backend_server)
You can also download the complete repository source using Git:

git clone https://github.com/Vishwajeetsingh22/MAD-Activity-1-2-MentalCare-AI.git
The folder links above are based on the project layout documented in this README. If a particular subfolder link does not open, use the AIMentalCare link to inspect the actual folders currently present in the repository.
Technologies represented in the repository
Android app: Kotlin, Android SDK, Gradle and Android UI resources.
Flutter app: Dart, Flutter SDK, pubspec.yaml and platform-specific project folders.
Backend: Python, Flask-related API code and a requirements.txt file.
AI/ML assets: model/vectorizer files and training or NLP scripts.
Testing: Android unit-test sources and Flutter test sources.
Getting started
Requirements
Install the tools required for the project variant you intend to run:

Git
Android Studio and a compatible JDK for the Android/Kotlin project
Flutter SDK and Dart for the Flutter project
Python for the backend
An Android emulator or a compatible Android device for mobile testing
Option A — Android/Kotlin application
Open Android Studio.
Select Open and choose AIMentalCare/AIMentalCareApp.
Allow Gradle sync to complete.
Configure any required backend or service settings using safe local configuration.
Run the app on an emulator or connected device.
On Windows, you can also open PowerShell in the Android project folder and try:

cd "AIMentalCare\AIMentalCareApp"
.\gradlew.bat tasks
To build a debug APK, if the project configuration and dependencies are available:

.\gradlew.bat assembleDebug
Option B — Flutter application
Install Flutter and confirm it is available in your terminal.
Open a terminal in the Flutter project folder.
Fetch dependencies and check the project.
cd "AIMentalCare\mentalcare_ai_flutter"
flutter pub get
flutter analyze
flutter test
To run on a connected device or emulator:

flutter devices
flutter run
The repository contains another AIMentalCare/lib Flutter source tree as well. Use the folder that matches the version you intend to demonstrate; do not assume both Flutter folders are identical.

Backend setup
The Python backend is located at AIMentalCare/backend_server/.

1. Create a virtual environment
From PowerShell:

cd "AIMentalCare\backend_server"
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
2. Configure environment values
Review .env.example if present.
Create a local .env only if the project requires it.
Add real credentials and API keys only to local environment variables or a secure secret manager.
Never commit .env, signing keys, private service-account files, or real user data.
3. Start the backend
Inspect app.py and the project's README.md for the supported startup command and required environment values. If the app uses Flask's development server and app.py defines the application entry point, the command may be:

python app.py
Use only the startup command that matches the actual backend configuration. The mobile app's API base URL must point to the correct backend address; an emulator and a physical phone may need different network addresses.

Testing
Run the tests relevant to the project you are working on.

Flutter tests:

cd "AIMentalCare\mentalcare_ai_flutter"
flutter test
Android unit tests:

cd "AIMentalCare\AIMentalCareApp"
.\gradlew.bat test
Backend tests: inspect requirements.txt and the test files in backend_server/ for their dependencies, then run the supported test command. For example, if pytest is installed:

cd "AIMentalCare\backend_server"
python -m pytest
Test results can vary by SDK version, dependency availability, configuration, and whether required backend services are running.

Privacy and security
Because this project concerns personal wellness information, privacy and safe handling of data are important.

Do not upload real user check-in answers, journal entries, health information, passwords, or tokens.
Keep .env files, API secrets, Firebase private credentials, and signing keystores out of Git.
Do not publish a release APK until it has been checked and cleared for distribution.
Use synthetic or anonymized information in screenshots and demos.
Before deployment, review authentication, access controls, network transport, data storage, logging, and deletion behaviour.
Do not treat app output as a clinical assessment or emergency service.
Known limitations
This repository includes more than one mobile implementation; screens and functionality may not be identical across them.
Backend availability, model behaviour, and API connectivity depend on local configuration and the environment in which the app is run.
Wearable integrations, external service integrations, and production deployment should be verified against the actual device, service, and configuration.
The project is intended for academic demonstration. Build, test, and security-check the exact version you plan to present.
Team
Group 11 — MCA, Jain (Deemed-to-be University)
JGI Knowledge Campus, Jayanagar, Bengaluru

Team member	Student ID
Joylyn Princita Fernandes	25MCAR0099
Syed Faizan Pasha	25MCAR0138
Vishwajeet Singh	25MCAR0219
Academic use
This repository was prepared as part of MAD Activity 1 & 2. It demonstrates application design and development, mobile UI implementation, API integration, testing, and exploration of AI/ML techniques for general wellness support.

Project tagline: Understand. Relax. Thrive.

Built as a student project with a focus on wellness awareness, learning, and responsible technology.

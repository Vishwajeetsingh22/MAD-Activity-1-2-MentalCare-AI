# MentalCare AI – Demonstration Screenshots Guide

**Project Title:** AI MentalCare – Real-Time AI-Powered Stress & Burnout Detection Mobile Application  
**Course Activity:** MCA CA3 – Review 5: Final Deployment & Demo  
**Institution:** Jain (Deemed-to-be University), Department of Master of Computer Applications (MCA)  
**Team Group 11:**
1. Joylyn Princita Fernandes (USN: 25MCAR0099)
2. Syed Faizan Pasha (USN: 25MCAR0138)
3. Vishwajeet Singh (USN: 25MCAR0219)

---

## Overview of App Demonstration Screens

This directory stores the official screenshots captured for the final MCA evaluation, slide deck, and technical project documentation. The screens illustrate the complete end-to-end user journey across all functional modules.

| Screen # | Filename | Screen Title | Key Elements Shown |
|:---:|:---|:---|:---|
| **01** | `01_splash_screen.png` | Splash Screen | Brand logo, App Title, "Understand. Relax. Thrive." Tagline, CA1 visual green theme |
| **02** | `02_user_registration.png` | User Registration | Input validation, Full name, Email, Password, Firebase/Local account creation |
| **03** | `03_user_login.png` | User Login | Clean auth UI, password toggling, Remember me, Register redirect |
| **04** | `04_home_dashboard.png` | Home Dashboard | Time-aware greeting, Wellness streak, Quick Check-in CTA card, Recent status |
| **05** | `05_ai_checkin_input.png` | AI Journal Input | Multiline journal edit text, sleep slider/input, work hours, character counter |
| **06** | `06_ai_analyzing_state.png` | AI In-Progress | Active circular progress indicator, "AI Analyzing Check-in..." status text |
| **07** | `07_result_low_stress.png` | Result: LOW Stress | Emerald green badge, ~32% stress score, healthy habit recommendation |
| **08** | `08_result_moderate_stress.png` | Result: MODERATE Stress | Amber badge, ~58% stress score, hydration & stretch break recommendations |
| **09** | `09_result_high_stress.png` | Result: HIGH Stress | Rose/Red badge, ~81% stress score, extracted indicators (Work pressure, Poor sleep) |
| **10** | `10_stress_history_feed.png` | Stress History Feed | Room SQLite persistent records, timestamped cards, badge colors, delete/clear |
| **11** | `11_analytics_burnout_chart.png` | Trend & Burnout Chart | Custom canvas `StressChartView`, 7-day trend curve, burnout warning evaluation |
| **12** | `12_support_wellness_hub.png` | Support & Crisis Hub | 5-minute Box Breathing interactive timer, 1-click emergency 988 hotline dialer |

---

## Capturing Instructions for Demonstration
Screenshots can be captured directly via Android Studio / ADB using the following commands:
```bash
# Capture screenshot to device storage
adb shell screencap -p /sdcard/screen.png

# Pull screenshot into this directory
adb pull /sdcard/screen.png screenshots/01_splash_screen.png
```
Or use **Android Studio Logcat/Device Manager** -> Camera icon ("Screen Capture") to save high-resolution PNGs directly to this folder.

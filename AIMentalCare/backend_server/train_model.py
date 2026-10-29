"""
MentalCare AI - Model Training & Evaluation Pipeline
MCA Project - CA3 Review 2: AI/ML Model & Data Review
Team: Group 11 (Joylyn Princita Fernandes, Syed Faizan Pasha, Vishwajeet Singh)
Department of MCA, Jain (Deemed-to-be University)
"""

import os
import joblib
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.svm import LinearSVC
from sklearn.model_selection import train_test_split
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    confusion_matrix,
    classification_report
)

# Labeled mental wellness check-in dataset representing stress levels
# 0: LOW, 1: MODERATE, 2: HIGH
DATA = [
    # LOW STRESS (0)
    ("I had a wonderful relaxing day at the park today.", 0),
    ("Feeling calm, peaceful, and refreshed after a good night sleep.", 0),
    ("Finished my tasks early and spent time with family.", 0),
    ("Everything is under control. Feeling productive and cheerful.", 0),
    ("Enjoyed a nice cup of tea and listened to music.", 0),
    ("I feel relaxed today and happy with my progress.", 0),
    ("Great workout this morning! Ready for a light workday.", 0),

    # MODERATE STRESS (1)
    ("I am slightly worried about tomorrow's project presentation.", 1),
    ("Lots of assignments due this week, feeling a bit rushed.", 1),
    ("I can't concentrate easily today because of noise.", 1),
    ("Had a tight deadline today, but managed to submit on time.", 1),
    ("Work is getting busy, need to prioritize my schedule better.", 1),
    ("Feeling slightly tense after a long meeting with managers.", 1),
    ("Not enough sleep last night, feeling mild headache.", 1),

    # HIGH STRESS (2)
    ("I have been working continuously for the last few days. I can't sleep properly and I feel exhausted.", 2),
    ("I am completely exhausted. Work pressure is destroying my mood.", 2),
    ("I haven't been sleeping well and I feel overwhelmed by everything.", 2),
    ("Today was really stressful. I had three deadlines and couldn't focus.", 2),
    ("Too many demands from boss. I am burning out and feel constant anxiety.", 2),
    ("Heart racing, constant deadlines, sleeping only 3 hours per night.", 2),
    ("My head hurts, work load is overwhelming, I feel hopeless and tired.", 2),

    # CRITICAL DISTRESS / ELEVATED HIGH STRESS (Mapped to 2: HIGH)
    ("I feel completely trapped, unable to cope, and I am breaking down.", 2),
    ("I can't handle this anymore, extreme crisis, total emotional breakdown.", 2),
    ("Severe panicking, cannot function, feeling like giving up completely.", 2),
    ("Intense mental agony, total exhaustion, I need emergency support now.", 2),
    ("I am completely overwhelmed and having severe panic attacks daily.", 2)
]

def train_and_evaluate():
    model_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 1. Dataset Construction & Export
    df = pd.DataFrame(DATA, columns=["text", "label"])
    df["stress_level"] = df["label"].map({0: "LOW", 1: "MODERATE", 2: "HIGH"})
    
    csv_path = os.path.join(model_dir, "dataset.csv")
    df.to_csv(csv_path, index=False)
    print(f"=== Dataset Exported to: {csv_path} ===")
    print(f"Total samples: {len(df)}")
    print(df["stress_level"].value_counts())
    print()

    # 2. Train / Test Split (75% Train, 25% Test with stratification)
    X_train, X_test, y_train, y_test = train_test_split(
        df["text"], df["label"], test_size=0.25, random_state=42, stratify=df["label"]
    )
    print(f"Train samples: {len(X_train)} | Test samples: {len(X_test)}")
    print()

    # 3. Feature Extraction (TF-IDF with unigrams + bigrams)
    vectorizer = TfidfVectorizer(ngram_range=(1, 2), max_features=1000)
    X_train_vec = vectorizer.fit_transform(X_train)
    X_test_vec = vectorizer.transform(X_test)
    print(f"TF-IDF Feature vocabulary size: {len(vectorizer.vocabulary_)}")
    print()

    # 4. Model 1: Logistic Regression
    lr = LogisticRegression(C=1.0, class_weight="balanced", max_iter=500, random_state=42)
    lr.fit(X_train_vec, y_train)
    y_pred_lr = lr.predict(X_test_vec)

    print("==================================================")
    print("MODEL 1: TF-IDF + LOGISTIC REGRESSION (Test Set)")
    print("==================================================")
    acc_lr = accuracy_score(y_test, y_pred_lr)
    prec_lr = precision_score(y_test, y_pred_lr, average="macro", zero_division=0)
    rec_lr = recall_score(y_test, y_pred_lr, average="macro", zero_division=0)
    f1_lr = f1_score(y_test, y_pred_lr, average="macro", zero_division=0)
    cm_lr = confusion_matrix(y_test, y_pred_lr)

    print(f"Accuracy:  {acc_lr:.4f} ({acc_lr*100:.1f}%)")
    print(f"Precision: {prec_lr:.4f}")
    print(f"Recall:    {rec_lr:.4f}")
    print(f"F1-Score:  {f1_lr:.4f}")
    print("Confusion Matrix:\n", cm_lr)
    print("\nClassification Report:\n", classification_report(y_test, y_pred_lr, target_names=["LOW", "MODERATE", "HIGH"], zero_division=0))

    # 5. Model 2: Linear Support Vector Machine (LinearSVC)
    svm = LinearSVC(C=1.0, class_weight="balanced", max_iter=1000, random_state=42)
    svm.fit(X_train_vec, y_train)
    y_pred_svm = svm.predict(X_test_vec)

    print("==================================================")
    print("MODEL 2: TF-IDF + SUPPORT VECTOR MACHINE (Test Set)")
    print("==================================================")
    acc_svm = accuracy_score(y_test, y_pred_svm)
    prec_svm = precision_score(y_test, y_pred_svm, average="macro", zero_division=0)
    rec_svm = recall_score(y_test, y_pred_svm, average="macro", zero_division=0)
    f1_svm = f1_score(y_test, y_pred_svm, average="macro", zero_division=0)
    cm_svm = confusion_matrix(y_test, y_pred_svm)

    print(f"Accuracy:  {acc_svm:.4f} ({acc_svm*100:.1f}%)")
    print(f"Precision: {prec_svm:.4f}")
    print(f"Recall:    {rec_svm:.4f}")
    print(f"F1-Score:  {f1_svm:.4f}")
    print("Confusion Matrix:\n", cm_svm)
    print("\nClassification Report:\n", classification_report(y_test, y_pred_svm, target_names=["LOW", "MODERATE", "HIGH"], zero_division=0))

    # 6. Fit final model on complete dataset for deployment
    full_vectorizer = TfidfVectorizer(ngram_range=(1, 2), max_features=1000)
    X_full = full_vectorizer.fit_transform(df["text"])
    final_model = LogisticRegression(C=1.0, class_weight="balanced", max_iter=500, random_state=42)
    final_model.fit(X_full, df["label"])

    preds_full = final_model.predict(X_full)
    print("==================================================")
    print("FINAL DEPLOYED MODEL EVALUATION (Full Dataset)")
    print("==================================================")
    print(f"Full Dataset Training Accuracy: {accuracy_score(df['label'], preds_full)*100:.1f}%")
    print("Classification Report:\n", classification_report(df["label"], preds_full, target_names=["LOW", "MODERATE", "HIGH"], zero_division=0))
    print("Confusion Matrix:\n", confusion_matrix(df["label"], preds_full))

    # 7. Save production model and vectorizer
    vec_save_path = os.path.join(model_dir, "tfidf_vectorizer.pkl")
    model_save_path = os.path.join(model_dir, "stress_model.pkl")
    joblib.dump(full_vectorizer, vec_save_path)
    joblib.dump(final_model, model_save_path)
    print()
    print(f"Model saved to: {model_save_path}")
    print(f"Vectorizer saved to: {vec_save_path}")
    print("==================================================")

if __name__ == "__main__":
    train_and_evaluate()

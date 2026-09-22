# 🛡️ AI-Based SQL Injection Detection System

## 📌 Project Overview

The **AI-Based SQL Injection Detection System** is a cybersecurity project designed to identify potentially malicious SQL injection patterns in web application inputs using **Artificial Intelligence and Machine Learning**.

The system analyzes user-provided input, identifies suspicious patterns, generates a risk assessment, and provides a security response. The project combines **Machine Learning-based detection** with traditional rule-based security checks to improve threat identification.

This project was developed as part of an **AI Immersion Project**, demonstrating how Artificial Intelligence can be applied to real-world cybersecurity challenges.

---

## 🎯 Objectives

- Detect potential SQL injection patterns in user inputs.
- Use Machine Learning for automated threat classification.
- Analyze input and generate a risk score.
- Identify normal and suspicious inputs.
- Provide security alerts for potentially malicious activity.
- Maintain logs of detected security events.
- Demonstrate the practical application of AI in cybersecurity.

---

## 💡 Key Features

- 🤖 **AI/ML-Based Detection** – Uses a machine learning model to classify input patterns.
- 🔍 **Input Analysis** – Analyzes submitted application inputs for suspicious characteristics.
- 🛡️ **Hybrid Detection** – Combines ML-based analysis with rule-based security checks.
- 📊 **Risk Scoring** – Generates a risk score based on the analysis.
- 🚨 **Threat Alerting** – Identifies and reports potentially dangerous inputs.
- 📝 **Event Logging** – Records detection results for further analysis.
- 📈 **Security Dashboard** – Displays detection statistics and analysis results.
- 💻 **Local Web Application** – Designed for testing in a controlled local environment.

---

## 🏗️ System Architecture

```text
                User Input
                    │
                    ▼
          ┌───────────────────┐
          │  Web Application  │
          └─────────┬─────────┘
                    │
                    ▼
          ┌───────────────────┐
          │ Input Preprocessing│
          └─────────┬─────────┘
                    │
           ┌────────┴────────┐
           ▼                 ▼
   ┌──────────────┐   ┌──────────────┐
   │ Rule-Based   │   │  AI/ML Model │
   │ Detection    │   │  Detection   │
   └──────┬───────┘   └──────┬───────┘
          │                  │
          └────────┬─────────┘
                   ▼
          ┌───────────────────┐
          │   Risk Analysis   │
          └─────────┬─────────┘
                    │
             ┌──────┴──────┐
             ▼             ▼
           SAFE        SUSPICIOUS
             │             │
             ▼             ▼
           Allow       Alert/Review
                           │
                           ▼
                    Event Logging
```

---

## 🧠 AI Component

The project uses a Machine Learning model to classify application input.

### AI Pipeline

```text
Input Data
    ↓
Data Preprocessing
    ↓
Feature Extraction
    ↓
Machine Learning Model
    ↓
Prediction
    ↓
Confidence Score
    ↓
Risk Classification
```

The model is trained using labelled security-related input data containing normal and suspicious examples.

For text feature extraction, the project can use **TF-IDF**, while a classification algorithm such as **Logistic Regression** can be used for the initial implementation.

---

## 🔐 Detection Process

The system follows these stages:

1. The user submits input through the web application.
2. The input is preprocessed.
3. Security rules analyze suspicious characteristics.
4. The Machine Learning model analyzes the input pattern.
5. The results from the detection layers are evaluated.
6. A risk score is generated.
7. The input is classified as safe or suspicious.
8. The result is displayed to the user.
9. The event can be recorded in the security log.

---

## 📊 Example Output

### Normal Input

```text
Classification : SAFE
Risk Level     : LOW
Action         : ALLOW
```

### Suspicious Input

```text
Classification : SUSPICIOUS
Risk Level     : HIGH
Action         : BLOCK / REVIEW
```

> The exact confidence and risk score depend on the trained model and test dataset.

---

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Python | Core programming language |
| Flask | Web application framework |
| Pandas | Dataset processing |
| Scikit-learn | Machine Learning |
| TF-IDF | Text feature extraction |
| Logistic Regression | Input classification |
| SQLite | Event/data storage |
| HTML | Web interface |
| CSS | User interface styling |
| JavaScript | Frontend interactions |
| Git & GitHub | Version control and project hosting |

---

## 📂 Project Structure

```text
AI_SQL_Detection/
│
├── app.py
├── detector.py
├── train_model.py
├── dataset.csv
├── model.pkl
│
├── templates/
│   └── index.html
│
├── static/
│   └── style.css
│
└── README.md
```

---

## ⚙️ Installation

### 1. Clone the repository

```bash
git clone <your-github-repository-url>
```

### 2. Navigate to the project directory

```bash
cd AI_SQL_Detection
```

### 3. Create a virtual environment

```bash
python -m venv venv
```

### 4. Activate the virtual environment

#### Windows

```bash
venv\Scripts\activate
```

#### Linux/macOS

```bash
source venv/bin/activate
```

### 5. Install dependencies

```bash
pip install -r requirements.txt
```
## ▶️ Running the Project

Start the Flask application:
```bash
python app.py
```
The application will run locally.
Open your browser and visit:
```text
http://127.0.0.1:5000

## 🧪 Testing

The project should be tested using **synthetic and authorized security test data**.
Testing categories include:
- Normal application inputs
- Suspicious input patterns
- Boundary cases
- False-positive cases
- False-negative cases
The system's predictions can then be compared with the expected labels to evaluate model performance.

## 📈 Model Evaluation

The Machine Learning model can be evaluated using standard classification metrics:
- Accuracy
- Precision
- Recall
- F1-Score
- Confusion Matrix
These metrics help measure how effectively the model distinguishes normal inputs from suspicious inputs.

## 🔮 Future Enhancements
The project can be extended with:
- Real-time security monitoring
- Advanced anomaly detection
- Improved ML models
- Deep Learning-based classification
- Explainable AI (XAI)
- Real-time notification system
- Advanced security dashboard
- Automatic model retraining using reviewed and labelled data
- Integration with a Security Operations Center (SOC) workflow
- 
## ⚠️ Security & Ethical Use
This project is intended for **educational purposes, cybersecurity research, and authorized testing**.
Testing should only be performed on applications, systems, and datasets that you own or have explicit permission to test.
The project is designed as a defensive detection system and should not be used to perform unauthorized attacks.

## 🎓 AI Immersion
This project demonstrates the application of Artificial Intelligence in cybersecurity through:
- Machine Learning-based threat classification
- Automated input analysis
- Risk assessment
- Security event detection
- Explainable detection results
- Data-driven cybersecurity monitoring

The project demonstrates how AI can complement traditional cybersecurity mechanisms to support automated threat detection.

## 👨‍💻 Project Author : Nakshatra manjari R
This project is intended for educational and research purposes.

Add an appropriate open-source license if you want others to reuse or modify the code.

# Lecta

> **College notice → AI understands it → Student knows exactly what to do.**

![Platform](https://img.shields.io/badge/platform-Android-3DDC84)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF)
![Status](https://img.shields.io/badge/status-UI%20complete%20%7C%20backend%20in%20development-orange)

Lecta is an Android app that helps college students turn official notices into clear, trackable tasks. This repository currently contains the **completed Android UI**. The backend and AI processing are **planned and not yet implemented**.

Built for the **NVIDIA × Nebius Global AI Hackathon**.

---

## Overview

### The problem

College students receive notices containing important information, deadlines, instructions, and events. These notices are often hard to understand and easy to forget.

### The solution

Lecta is designed so that a student can provide a notice, have it understood by AI, and receive a structured result: what the notice is about, what they need to do, and by when. If the notice contains an actionable task, the student can add it to their task list and track it through its deadline.

---

## Current status

| Area | Status |
|---|---|
| Android UI (Kotlin + Jetpack Compose) | ✅ Implemented |
| Backend (FastAPI) | 🛠 Planned / In development |
| PDF text extraction | 🛠 Planned |
| Nebius AI integration | 🛠 Planned |
| Android ↔ backend communication | 🛠 Planned |

> **This repository represents the completed frontend/UI stage of Lecta.** The app does not yet process notices with AI and does not yet communicate with a backend. Everything under "How Lecta will work" and "Planned backend" describes the intended design, not working functionality.

---

## Features (currently implemented)

The current Android project includes the following UI:

- Lecta branding, splash screen and onboarding
- Home / dashboard
- Notice Board
- Calendar that visually represents task dates and priority
- Upcoming Tasks and task cards
- Task completion
- Priority levels: **Critical**, **High**, **Medium**, **Low**
- Task dates / deadlines and task confidence information
- Add-task UI
- Reminders-related UI
- Profile / menu, Account, Settings, Help, and About screens
- Theme management
- Local storage-related code

The UI is designed primarily for **Android phones**.

---

## How Lecta will work (planned)

1. The student provides a college notice.
2. Lecta processes the notice.
3. AI understands the notice.
4. Important information is extracted.
5. The student receives a structured result.
6. If the notice contains an actionable task, the student can add it to their task list.
7. The student tracks the task and its deadline.

### Planned inputs

The immediate backend milestone is **PDF notices**. Screenshots/images and pasted notice text are possible future inputs; none of these are processed yet.

---

## Architecture (planned)

```text
Android App
      │
      │ HTTPS
      ▼
Lecta Backend
      │
      ▼
Nebius Token Factory
      │
      ▼
AI Model
      │
      ▼
Structured JSON
      │
      ▼
Android App
      │
      ▼
LectaTask
```

---

## Technology stack

**Android (current)**
- Kotlin
- Jetpack Compose
- Material 3
- Android Studio, Gradle, Android SDK

**Backend (planned)**
- Python
- FastAPI
- PDF text extraction
- Nebius Token Factory / Nebius AI

---

## Project structure

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/tany/lecta/
│   │   │   ├── AboutScreen.kt
│   │   │   ├── AccountScreen.kt
│   │   │   ├── AddMenu.kt
│   │   │   ├── AppInfo.kt
│   │   │   ├── AppRoot.kt
│   │   │   ├── CalendarBox.kt
│   │   │   ├── Dialogs.kt
│   │   │   ├── Greeting.kt
│   │   │   ├── HeaderAndMenu.kt
│   │   │   ├── HelpScreen.kt
│   │   │   ├── HomeScreen.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── Models.kt
│   │   │   ├── NoticeBoard.kt
│   │   │   ├── OnboardingScreen.kt
│   │   │   ├── PageComponents.kt
│   │   │   ├── Reminders.kt
│   │   │   ├── Settings.kt
│   │   │   ├── SettingsScreen.kt
│   │   │   ├── SplashScreen.kt
│   │   │   ├── Storage.kt
│   │   │   ├── TaskBox.kt
│   │   │   ├── TaskList.kt
│   │   │   ├── ThemeManager.kt
│   │   │   └── ui/theme/
│   │   │       ├── Color.kt
│   │   │       ├── Theme.kt
│   │   │       └── Type.kt
│   │   └── res/
│   └── ...
├── build.gradle.kts
└── ...
```

The project is organized by screen (`*Screen.kt`), reusable UI components (task, calendar, notice board, dialogs, header/menu), app-level setup (`MainActivity`, `AppRoot`), data and storage (`Models`, `Storage`), and theming (`ThemeManager`, `ui/theme/`).

---

## Task model

The task model includes concepts such as ID, title, source, priority, start date, end date, confidence, extracted information, and completion state. Priority is one of `Critical`, `High`, `Medium`, or `Low`.

---

## Planned backend

> **Status: Planned / In Development.** Nothing in this section is implemented yet.

The backend will eventually:

1. Receive a PDF notice.
2. Extract text from the PDF.
3. Send the extracted content to Nebius AI.
4. Ask the AI to understand the notice.
5. Extract structured information.
6. Validate the AI response.
7. Return JSON to the Android app.

```text
PDF Notice
   ↓
Lecta Backend
   ↓
Text Extraction
   ↓
Nebius AI
   ↓
Structured Information
   ↓
Android App
   ↓
Task
```

### Planned response format

An illustrative example of what a structured response may look like. **This is a planned format, not a currently implemented API.**

```json
{
  "summary": "Semester 1 pre-medical examination",
  "action": "Attend the pre-medical examination",
  "deadline": "2026-10-07",
  "audience": "Semester 1 medical students",
  "priority": "Critical",
  "type": "Action",
  "confidence": 93
}
```

---

## Nebius AI integration (planned)

Lecta's AI processing is planned to use **Nebius AI / Nebius Token Factory**. The integration has **not been implemented yet**.

The Nebius API key will live only on the backend. It must **never** be embedded in the Android application or committed to this repository.

---

## Roadmap

### Completed
- [x] Android UI
- [x] Core frontend structure
- [x] Task UI
- [x] Calendar UI
- [x] Notice Board UI
- [x] Navigation and supporting screens
- [x] GitHub repository setup

### Next
- [ ] Create the backend
- [ ] FastAPI endpoint
- [ ] PDF text extraction
- [ ] Nebius integration
- [ ] Structured AI output
- [ ] Backend validation
- [ ] Android networking
- [ ] Connect AI results to Lecta tasks
- [ ] Testing
- [ ] Final hackathon polish

### Future ideas
- Screenshot/image notice input
- Pasted notice text input

---

## Running the project

The app currently runs on its own; no backend or API key is needed.

**Requirements:** a recent version of Android Studio with the Android SDK installed, and an Android emulator or a physical Android device. Check `app/build.gradle.kts` for the project's SDK and Java settings.

```bash
git clone git@github.com:Tany0625/Lecta.git
cd Lecta
```

1. Open Android Studio and choose **Open**, then select the `Lecta` folder.
2. Wait for the Gradle sync to finish.
3. Select an emulator or a connected device.
4. Click **Run**.

Android Studio generates `local.properties` locally (it holds your machine's SDK path). It is listed in `.gitignore` and **should not be committed**.

---

## Security and API keys

- This repository contains no API keys, secrets, keystores, or `.env` files.
- `local.properties`, build outputs, and IDE files are excluded through `.gitignore`.
- When the backend is added, the Nebius API key will be stored in backend configuration only, never in the Android app and never in Git.

---

## Hackathon

Lecta is being developed for the **NVIDIA × Nebius Global AI Hackathon** (deadline: **30 October 2026**). The Android UI is complete; the backend and Nebius integration are the next milestones.

---

## Author

Created by [Tany0625](https://github.com/Tany0625).

# IntervAI 🎙️🤖

**IntervAI** is a real-time, voice-interactive Android application designed to conduct simulated technical and behavioral mock interviews. Powered by Android's native speech engines and Groq's low-latency LLM infrastructure (Llama 3), the app asks domain-specific technical questions aloud, listens to candidate spoken responses, conducts dynamic follow-ups, and produces a final detailed evaluation report.

---

## ✨ Key Features

- **Seamless Voice Interaction**: Eliminates manual typing, forcing candidates to articulate technical concepts verbally just like in real interviews.
- **Zero-Latency Conversation**: Leverages Groq's sub-500ms response speeds to eliminate awkward silences, creating a realistic interview pace.
- **100% Free Stack**: Built entirely on free tools and APIs (Android Native STT/TTS, Groq API free tier, and Google Gemini API free tier).
- **Comprehensive Assessment**: Provides structured feedback at the end of every session, allowing candidates to identify weak spots, practice answer structure, and gain confidence.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Android Jetpack Compose + Material 3
- **Language**: Kotlin
- **LLM Engine**: Groq API (`llama-3.3-70b-versatile` / `llama-3.1-8b-instant`) & Google Gemini API
- **Speech Engine**: Native Android SpeechRecognizer (STT) & TextToSpeech (TTS)
- **Networking**: Retrofit + Gson

---

## 📋 Phased Roadmap

- [x] **Phase 1: Foundation & Credentials Setup**
  - Project setup, permissions, dependencies, and API configuration.
- [ ] **Phase 2: Local Hardware Integration**
  - Android native TTS/STT wrappers & microphone permission handling.
- [ ] **Phase 3: State Management & System Prompting**
  - Groq API client integration, state machine, and interviewer system prompt design.
- [ ] **Phase 4: User Interface (Jetpack Compose)**
  - Topic Selection screen, Live Interview screen, and Post-Interview Review screen.
- [ ] **Phase 5: Refinement & Testing**
  - Interruption handling, error fallbacks, and resource lifecycle management.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug | 2024.2.1 or newer
- JDK 17+
- Android SDK 24+ (Android 7.0)

### Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/stymsaw/IntervAI.git
   cd IntervAI
   ```

2. Open the project in Android Studio.

3. Obtain a free API key from [Groq Console](https://console.groq.com).

4. Build and run the project on an Android device or emulator with microphone support.

---

## 📄 License

This project is open source under the MIT License.

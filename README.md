# DeenTok 🎥✨

DeenTok is a modern, professional, and visually polished short-video social media and learning community platform. It is designed to represent freedom, creativity, and the sharing of beneficial voices and reminders in an elegant, mobile-first interface.

---

## 🌟 Key Features

The application incorporates a rich suite of professional features designed with custom, high-contrast visual themes:

1. **For You**: An immersive, continuous vertical scroll feed for discovering modern short-form video content and spiritual reminders.
2. **Following**: A curated stream focused exclusively on updates from content creators you follow.
3. **Explore**: A powerful search and discover screen with trending hashtags, creator spotlights, and categorical video organization.
4. **Upload Engine**: A robust content generation flow allowing creators to customize templates, add captions, and schedule post streams.
5. **Live Space**: A designated live-stream placeholder built to integrate real-time broadcasts.
6. **Encrypted Messages**: Secure direct messaging utilizing localized, client-side transactional SQLite/Room database encryption strategies.
7. **Creator Studio**: Features the **Voice Stars Creator Fund** dashboard where content creators can analyze views, track earnings, and request support.
8. **Admin Dashboard**: A secure, centralized console for platform administrators to purge datastores, review reports, and manage video templates.
9. **Localization**: Built-in support for multilingual translation layers (including English, Afaan Oromoo, and Amharic).

---

## 🛠️ Architecture & Tech Stack

DeenTok is developed using state-of-the-art Android architectures and Jetpack libraries:

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose (Declarative UI) with **Material Design 3 (M3)**
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **Local Persistence**: **Room Database** utilizing localized relational SQLite streams
- **Concurrency**: Kotlin Coroutines and asynchronous cold `StateFlow`/`SharedFlow` streams
- **Build Configuration**: Gradle (Kotlin DSL - `.gradle.kts`)

---

## 📁 Project Folder Structure

```
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/example
│   │   │   │   ├── MainActivity.kt        # Entry point activity handling Edge-to-Edge and Navigation host
│   │   │   │   ├── data                   # Local persistence layer (Room entities, DAOs, and repository patterns)
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── DeenTokDao.kt
│   │   │   │   │   └── DeenTokRepository.kt
│   │   │   │   ├── viewmodel              # ViewModels handling state flow management and event interactions
│   │   │   │   │   └── DeenTokViewModel.kt
│   │   │   │   └── ui                     # Jetpack Compose Screens, Admin Dashboard, and theme styles
│   │   │   │       ├── Screens.kt
│   │   │   │       ├── AdminDashboard.kt
│   │   │   │       ├── Translations.kt    # Multilingual localized string definitions
│   │   │   │       └── theme
│   │   │   │           ├── Theme.kt
│   │   │   │           ├── Color.kt
│   │   │   │           └── Type.kt
│   │   │   └── res                        # App resources (drawable vectors, strings, themes)
│   └── build.gradle.kts                   # Module level dependencies and SDK configuration
├── build.gradle.kts                       # Project-wide build specifications
├── settings.gradle.kts                    # Project structure settings
└── metadata.json                          # AI Studio Platform Metadata configuration
```

---

## 🚀 How to Run the App

### Standard Build and Installation
1. Ensure you have **Android Studio** (Koala or newer) installed.
2. Clone or export this workspace structure.
3. Open the project root in Android Studio.
4. Let Gradle sync and download dependencies automatically.
5. Click **Run** on your physical device or an Android Emulator.

### Command Line
To build a debug APK from your terminal:
```bash
gradle assembleDebug
```
The resulting APK can be found under `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🛡️ Secure Data Policy
DeenTok prioritizes user data integrity and privacy. Direct messages, search history, and profile records are safely persisted on your local device using high-performance SQLite storage mechanisms. No user-identifying metadata is transmitted to third-party services without explicit request.

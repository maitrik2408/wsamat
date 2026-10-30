# WSAMat Personal Safety Hub

WSAMat is an Android application designed for personal security, automated emergency alert dispatch, and covert incident evidence capture. The application operates using structured software architecture patterns (Repository Pattern, Typed Sealed Result Handling, SQLite Persistence) to ensure reliable background execution during critical emergency scenarios.

---

## Direct Artifact Download

The pre-compiled, production-ready Android Application Package (APK) is available for direct installation:

[Download WSAMat-Safety.apk (Latest Build)](https://github.com/maitrik2408/wsamat/releases/latest/download/WSAMat-Safety.apk)

---

## System Architecture & Technical Specifications

- **Language:** Kotlin 1.9 / Java 17
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 35 (Android 15)
- **Compile SDK:** 37
- **Database:** Local SQLite with Helper Abstraction Version 2
- **Architecture Pattern:** Model-View-Presenter / Repository Pattern with Sealed Result State Wrappers
- **Build System:** Gradle Wrapper 9.8.1 / Android Gradle Plugin 9.4.1

---

## Core Operational Modules

### Emergency SOS Dispatch System
- **Single-Tap Emergency Countdown:** Initiates a configurable 7-second countdown sequence before sending emergency SMS notifications to all registered primary contacts.
- **10-Second Debounce Lock:** Prevents repeated message loops or duplicate SMS dispatch during frantic interactions.
- **Multi-Stage GPS Fallback:** Retrieves real-time coordinates using Fused Location Provider and GPS hardware fallback, attaching Google Maps navigation URLs to outgoing emergency SMS messages.

### Covert Evidence Capture
- **Silent Background Photo Capture:** Utilizes Camera2 API to capture JPEG evidence quietly without displaying camera preview or playing shutter sound effects.
- **Ambient Audio Recording:** Records up to 20 seconds of ambient audio in AAC/MP4 format for local incident documentation.
- **Incident History Ledger:** Persists timestamps, GPS coordinates, photo paths, and audio file URIs in local SQLite database with WhatsApp evidence sharing via FileProvider.

### Deception & Camouflage Mechanics
- **Camouflage Calculator Mode:** Displays a fully functioning mathematical calculator layout that acts as a stealth SOS trigger upon entering a designated numeric sequence.
- **Launcher Icon Disguise:** Dynamically switches application activity aliases to disguise the home screen icon as a Calculator, Compass, or Clock.
- **Simulated Incoming Call:** Generates realistic incoming call screen simulation to facilitate discreet exit from unsafe environments.

### Background Monitoring & Accessibility
- **Dual-Condition Shake & Power Gesture:** Listens to accelerometer g-force thresholds and screen power state transitions to trigger emergency SOS when device is shaken or when power button is pressed rapidly.
- **Keyguard Overlay Service:** Maintains a persistent foreground notification providing direct lock-screen SOS overlay access without unlocking the device.
- **Distress Whistle Synthesizer:** Generates high-decibel 3.8 kHz acoustic distress signals using AudioTrack PCM buffer synthesis.
- **Safety Sandbox:** Provides an isolated testing environment to verify camera, audio, location, and alert dispatches safely without alarming emergency contacts.

---

## Setup & Deployment Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1) or higher
- Android SDK Platform 35 / 37
- Java Development Kit (JDK) 17

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/maitrik2408/wsamat.git
cd wsamat
```

2. Assemble Debug APK:
```bash
./gradlew assembleDebug
```

3. Install on connected Android device via ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Security & Privacy Considerations

- **Data Privacy:** All personal profile data, emergency contact details, and evidence recordings remain strictly on the local device within SQLite database storage and app-private directory storage.
- **File Sharing Security:** External file sharing uses Content URIs provided by Android FileProvider (`com.kirtiam.wsamat.fileprovider`) to ensure secure sandboxed file access.
- **Permission Control:** Permissions for Location, SMS, Camera, Microphone, and Contacts are requested on demand with fallback user controls.

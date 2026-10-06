# Solar System Compass ☀️🧭

**Brought to you**  
**Black falcon 🦅**

An Android app that visualizes the Solar System in a **compass-style interactive view**.

## Features

- 🌞 Beautiful animated Solar System with Sun + 9 planets (including Pluto)
- 🧭 Real device compass integration — hold the phone flat and point North to align the map
- 🪐 Tap any planet (or the Sun) to see distance from Sun and a fun fact
- ✨ Smooth orbital animation
- 🎨 Dark space theme with glowing sun and colored planets
- 📱 Splash screen with branding

## Screenshots / How it works

1. Splash screen appears for 2.5 seconds
2. Main screen shows the Solar System in a circular compass layout
3. Planets slowly orbit the Sun
4. Device orientation rotates the compass rose (N/E/S/W)
5. Tap a planet to learn about it

## Requirements

- Android 7.0 (API 24) or higher
- Device with accelerometer + magnetometer (most phones)

## How to build & run

1. Open the project in **Android Studio** (Hedgehog or newer recommended)
2. Let Gradle sync
3. Connect a device or start an emulator
4. Click **Run** ▶️

Or from terminal:

```bash
./gradlew assembleDebug
```

## Project structure

```
app/
├── src/main/
│   ├── java/com/blackfalcon/solarsystem/
│   │   ├── SplashActivity.kt
│   │   ├── MainActivity.kt
│   │   └── SolarSystemView.kt      ← Custom canvas view
│   ├── res/
│   │   ├── layout/
│   │   ├── values/
│   │   └── drawable/
│   └── AndroidManifest.xml
```

## Credits

Created by **Black falcon 🦅**

---

Made with ❤️ for space enthusiasts.

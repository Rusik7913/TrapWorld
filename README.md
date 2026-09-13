# Level Dash

Android 2D platformer inspired by trap-platform games.

## Features
- 100 levels with gradually increasing difficulty
- 5 visual worlds
- 6 heroes / skins
- Save progress with SharedPreferences
- Touch controls
- No external assets or libraries required
- Designed for GitHub Actions
- AGP 8.9.2 / Gradle 8.11+ recommended / JDK 17 or 21

## GitHub Actions
The project includes no wrapper in this starter ZIP. In GitHub Actions, use the official Gradle setup or add a Gradle wrapper locally.

Typical command after adding wrapper:
`./gradlew assembleDebug`

APK:
`app/build/outputs/apk/debug/app-debug.apk`

# ColorRush 🎨

A fast-paced Android color-matching game built with Jetpack Compose and Clean Architecture.

Tap the correct colors to score points before time runs out!

## Gameplay

- **3×3 color grid** — Green, Red, and Yellow cells appear randomly
- **Score points**: Green (+1), Red (−1, floored at 0), Yellow (+3)
- **Yellow cells add 3 seconds** to the countdown (max 60s cap)
- **30-second timer** — race against the clock
- **Top 10 leaderboard** — save your name and compete

## Screenshots

<!-- TODO: Add screenshots once available -->

| Main Screen | Game Screen | Game Over |
|-------------|-------------|-----------|
| _(screenshot)_ | _(screenshot)_ | _(screenshot)_ |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose (Material 3) |
| Architecture | Clean Architecture (Domain/Data/UI) |
| DI | Hilt |
| Database | Room (SQLite) |
| Preferences | DataStore |
| Navigation | Compose Navigation |
| Audio | MediaPlayer (BGM) + SoundPool (SFX) |
| CI | Docker + GitHub Actions |
| Testing | JUnit 5, Robolectric |

## Project Structure

```
app/src/main/kotlin/com/gentleai/colorrush/
├── audio/          # AudioManager interface + implementation
├── data/
│   ├── local/
│   │   ├── db/     # Room database, DAO, entities
│   │   └── datastore/  # DataStore preferences
│   └── repository/ # Repository implementations
├── di/             # Hilt DI modules
├── domain/
│   ├── engine/     # GameEngine + ColorSpawner (pure Kotlin)
│   ├── model/      # Domain models and enums
│   └── repository/ # Repository interfaces
└── ui/
    ├── game/       # Game screen + ViewModel + components
    ├── gameover/   # GameOver screen + ViewModel
    ├── main/       # Main menu + ViewModel
    ├── navigation/ # NavGraph + LocaleHelper
    └── theme/      # Material 3 theme + colors
```

## Build Instructions

### Prerequisites

- **JDK 17+** — required for Android Gradle Plugin 8.x
- **Android SDK 35** — compileSdk and targetSdk
- **Docker** (optional) — for CI-like local builds

### Local Build

```bash
# Clone the repository
git clone https://github.com/ualonso011/color-rush.git
cd color-rush

# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests (JUnit 5 + Robolectric)
./gradlew :app:testDebugUnitTest

# Run all checks (tests + ktlint)
./gradlew :app:check
```

The debug APK will be at:

```
app/build/outputs/apk/debug/app-debug.apk
```

### Docker Build

```bash
docker compose build
docker compose run --rm build
```

The APK is exported to `app/build/outputs/apk/debug/`.

## Testing

| Test Type | Framework | Location |
|-----------|-----------|----------|
| Unit tests (domain) | JUnit 5 | `test/.../domain/engine/` |
| Integration tests (data) | Robolectric | `test/.../data/repository/`, `test/.../data/local/datastore/` |

```bash
# Run all unit and integration tests
./gradlew :app:testDebugUnitTest

# Generate coverage report (JaCoCo)
./gradlew :app:createDebugCoverageReport
```

## Localization

ColorRush supports three languages:

| Language | Code | Resource Folder |
|----------|------|-----------------|
| Basque (default) | `eu` | `res/values-eu/` |
| Spanish | `es` | `res/values-es/` |
| English | `en` | `res/values/` |

Language can be changed at runtime from the Main screen. The preference is persisted in DataStore.

## Audio Credits

Background music and sound effects are original compositions generated as simple WAV tones for this project.

- BGM: Arcade-style loop at 140 BPM
- SFX: Distinct tone frequencies for each color interaction

## License

This project is for educational and demonstration purposes.

## Delivery

This project was developed in 6 stacked PRs following SDD (Specification-Driven Development):

| PR | Phase | Scope |
|----|-------|-------|
| #1 | Scaffolding | Gradle, CI, theme |
| #2 | Domain | Models, GameEngine, ColorSpawner |
| #3 | Data + DI | Room, DataStore, Hilt modules |
| #4 | Main + GameOver UI | Navigation, screens, ViewModels |
| #5 | Game UI + Audio | Game screen, components, AudioManager |
| #6 | Resources + Testing | Audio assets, app icon, tests, README |

---

Built with ❤️ by [@ualonso011](https://github.com/ualonso011)

# TeamUp

**A small Android app for quickly splitting players into balanced random teams.**

[Русская версия](README.ru.md) · [Latest release](https://github.com/Amazomic/TeamUp/releases/latest)

## Features

- Set 2–30 players and 2–8 teams with simple steppers.
- Enter player names and see each assignment in a team color.
- Shuffle teams without rebuilding the player list.
- Review or delete individual draws in the history.
- Keep the latest player names, settings, and assignments on the device.
- No account, ads, analytics, or network access.

## Download

Get source snapshots and release notes from [GitHub Releases](https://github.com/Amazomic/TeamUp/releases). An Android APK will be attached when a distributable build is available.

## Build from source

Requirements: Android Studio with JDK 17 or newer and Android SDK Platform 36.

```powershell
.\gradlew.bat assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

To build the Play Store App Bundle, configure your own upload key as described in [`signing.properties.example`](signing.properties.example), then run:

```powershell
.\gradlew.bat bundleRelease
```

Keep the keystore and passwords private. The release bundle is written to `app/build/outputs/bundle/release/app-release.aab`.

## Privacy

Player names, team settings, and draw history are stored locally on the device. TeamUp does not send them to a server. See the [privacy policy draft](play-store/privacy-policy-draft-ru.md).

## Project details

- Application ID: `ru.teampicker.app`
- Current version: `1.2.0` (version code `3`)
- Minimum Android version: Android 6.0 (API 23)
- Target Android version: Android 16 (API 36)

See [CHANGELOG.md](CHANGELOG.md) for release history. TeamUp is free software under the terms of the [GNU GPL v3](LICENSE).

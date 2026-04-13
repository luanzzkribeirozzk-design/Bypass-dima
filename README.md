# 8 Ball Pool

Android app project for 8 Ball Pool.

## Build

### Requirements

- Android Studio Flamingo or newer
- JDK 17
- Android SDK API 33

### Build via Android Studio

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Build > Build Bundle(s) / APK(s) > Build APK(s)

### Build via Command Line

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires keystore)
./gradlew assembleRelease
```

## GitHub Actions

The project includes two GitHub Actions workflows:

### `build.yml`
Runs on every push to `main`/`master` and pull requests:
- Builds debug APK
- Builds release APK (if on main/master)
- Uploads APKs as artifacts

### `release.yml`
Runs when a version tag is pushed (e.g., `v1.0.0`):
- Builds signed release APK
- Creates a GitHub Release with the APK attached

### Setting up signing (optional)

To sign the release APK, add the following secrets to your GitHub repository:

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

To encode your keystore:
```bash
base64 -i your-keystore.jks | tr -d '\n'
```

### Creating a release

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Project Structure

```
8BallPool/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/eightball/pool/
│   │       │   ├── MainActivity.kt
│   │       │   ├── FloatingViewService.kt
│   │       │   ├── FloatingWebViewService.kt
│   │       │   └── ShizukuHelper.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── values/
│   │       │   └── mipmap-*/
│   │       ├── assets/
│   │       │   ├── pato.sh
│   │       │   ├── pato0.sh - pato8.sh
│   │       │   ├── rish
│   │       │   ├── rish_shizuku.dex
│   │       │   ├── F.apk
│   │       │   └── fonts/HAPPY.otf
│   │       ├── jniLibs/arm64-v8a/
│   │       │   ├── libanort.so
│   │       │   ├── libduck.so
│   │       │   ├── libpato.so
│   │       │   └── libsticker.so
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── .github/
│   └── workflows/
│       ├── build.yml
│       └── release.yml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Permissions

The app requires the following permissions:
- `INTERNET` - Network access
- `READ/WRITE_EXTERNAL_STORAGE` - File access
- `MANAGE_EXTERNAL_STORAGE` - Full storage access (Android 11+)
- `SYSTEM_ALERT_WINDOW` - Overlay/floating window
- `INSTALL_PACKAGES` - Package installation
- Shizuku API permissions for elevated operations

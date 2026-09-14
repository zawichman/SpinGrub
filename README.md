# 🍽️ SpinGrub

A fun, colorful Android app that helps you decide what to cook by spinning three wheels — **Meat**, **Base** (the way you eat it), and **Sauce** — and combining the results into a meal. Swipe the wheels with your finger (the harder you fling, the longer the spin) or tap **Spin All**. Save your favorite combos and spin a dedicated wheel of favorites when you can't decide!

Built with **Kotlin + Jetpack Compose + Material 3**.

## 📲 Download & install

Grab the prebuilt APK here: **[`dist/SpinGrub.apk`](dist/SpinGrub.apk)** (open the file on GitHub and tap **Download**).

On your Android device, open the downloaded `SpinGrub.apk` and allow **"install from unknown sources"** if prompted. This is a debug-signed APK intended for sideloading/testing.

## ✨ Features

### 🎡 Spinner tab
- Three colorful wheels titled **Meat**, **Base** (how you eat it — bowl / salad / sandwich / wrap …), and **Sauce**.
- **Responsive layout:** wheels stack vertically in portrait, sit side-by-side in landscape.
- **Swipe-to-spin physics:** flick any wheel with your finger — swipe velocity drives spin speed & duration, with natural deceleration.
- **Spin All** button spins all three wheels at once, each with a similar-but-random power.
- **Animated result title box** springs in over the wheels showing what you landed on, and disappears the moment a new spin starts.
- **Favorite this combo:** name and save the current landed combination.
- Optional **confetti** celebration (Lottie), haptic ticks, and sound toggle.

### ⭐ Fav Spinner tab
- A single centered wheel that cycles through your saved favorite combos.
- **Swipe-only** spinning.
- Animated title box shows the favorite's **name** and its **recipe** (the three items).

### ⚙️ Setup tab
- Add / remove entries for each of the three wheels.
- Toggles for **confetti**, **haptics**, and **sound**.
- **Reset wheels to defaults**.

All data (wheel items, favorites, settings) is persisted locally with **DataStore**.

## 🛠️ Tech stack
- Kotlin 1.9.24, Jetpack Compose (BOM 2024.06.00), Material 3
- Custom `Canvas`-drawn wheels with gesture-driven fling physics
- [Lottie](https://airbnb.io/lottie/) for the confetti animation (bundled, works offline)
- Jetpack DataStore Preferences for persistence
- minSdk 24 · targetSdk 34 · AGP 8.5.2

## 🚀 Building

The project builds with the Gradle wrapper. Requires a JDK 17 and an Android SDK with API 34.

```bash
# point Gradle at your Android SDK
echo "sdk.dir=/path/to/android-sdk" > local.properties

./gradlew :app:assembleDebug
```

The debug APK is produced at:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install it on a device (enable "install from unknown sources") or via ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> This is a **debug-signed** APK intended for sideloading/testing. For a Play Store release you'd sign it with your own keystore.

## 📁 Project layout

```
app/src/main/java/com/spingrub/app/
├── MainActivity.kt            # Bottom-nav scaffold, 3 tabs
├── SpinGrubViewModel.kt       # State + actions
├── data/
│   ├── Models.kt              # Category, Favorite, defaults
│   └── SpinGrubRepository.kt  # DataStore persistence
├── ui/
│   ├── SpinnerWheel.kt        # Canvas wheel + fling physics
│   ├── SpinnerScreen.kt       # Spinner tab
│   ├── FavSpinnerScreen.kt    # Fav Spinner tab
│   ├── SetupScreen.kt         # Setup tab
│   ├── Components.kt          # Animated title box + confetti
│   └── theme/Theme.kt         # Colorful Material 3 theme
└── util/Feedback.kt           # Haptics
```

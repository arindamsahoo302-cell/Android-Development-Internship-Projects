# ðŸŽ“ CollegeAppView - Task 3

A native Android WebView application designed to seamlessly load and navigate the official **Panskura Banamali College** web portal (https://www.panskurabanamalicollege.ac.in/) with responsive in-app browsing and device back navigation.

---

## ðŸ“± Key Features

- **Embedded WebView Integration:** Loads web pages directly inside the application without redirecting to an external web browser.
- **In-App Navigation:** Overrides URL loading using WebViewClient to keep browsing contained within the application.
- **JavaScript Enabled:** Supports dynamic portal scripts and interactive components.
- **Hardware Back Navigation:** Handles back-press events using OnBackPressedCallback to navigate back through web browsing history (canGoBack() / goBack()) before exiting.
- **Network Permissions:** Configured with INTERNET permission for smooth connectivity.

---

## ðŸ› ï¸ Tech Stack & Architecture

- **Language:** Kotlin
- **Components:** ndroid.webkit.WebView, ndroid.webkit.WebViewClient
- **Navigation:** ndroidx.activity.OnBackPressedCallback
- **Build System:** Gradle (Kotlin DSL), Android Gradle Plugin (AGP)
- **Minimum SDK:** Android 7.0 (API Level 24+)
- **Target SDK:** Android 14+ (API Level 34+)

---

## ðŸ“‚ Project Structure

`	ext
CollegeAppView/
â”œâ”€â”€ app/
â”‚   â”œâ”€â”€ src/main/
â”‚   â”‚   â”œâ”€â”€ AndroidManifest.xml       # Internet permissions & main activity declaration
â”‚   â”‚   â”œâ”€â”€ java/com/example/mywebapp/
â”‚   â”‚   â”‚   â””â”€â”€ MainActivity.kt       # WebView setup & back navigation logic
â”‚   â”‚   â””â”€â”€ res/
â”‚   â”‚       â”œâ”€â”€ layout/
â”‚   â”‚       â”‚   â””â”€â”€ activity_main.xml # Fullscreen WebView layout
â”‚   â”‚       â”œâ”€â”€ values/
â”‚   â”‚       â”‚   â”œâ”€â”€ colors.xml        # Theme color definitions
â”‚   â”‚       â”‚   â”œâ”€â”€ strings.xml       # App title & string constants
â”‚   â”‚       â”‚   â””â”€â”€ themes.xml        # Application themes
â”‚   â”‚       â””â”€â”€ mipmap-.../           # Launcher icons
â”‚   â””â”€â”€ build.gradle.kts              # Module-level Gradle configuration
â”œâ”€â”€ gradle/
â”‚   â”œâ”€â”€ libs.versions.toml            # Version catalog
â”‚   â””â”€â”€ wrapper/                      # Gradle wrapper binaries
â”œâ”€â”€ build.gradle.kts                  # Root build script
â”œâ”€â”€ README.md                         # Project documentation
â””â”€â”€ settings.gradle.kts               # Project & repository configurations
`

---

## ðŸš€ Getting Started

1. **Open in Android Studio:**
   - Launch **Android Studio**.
   - Select **Open** and choose the CollegeAppView directory.
2. **Allow Gradle Sync:**
   - Wait for Gradle to synchronize dependencies.
3. **Verify Internet Connection:**
   - Ensure an active network connection on your Android Emulator or physical device.
4. **Run Application:**
   - Select your target device and click **Run** (Shift + F10).

---

## ðŸ‘¨â€ðŸ’» Developer Profile

- **Developer:** Arindam Sahoo
- **GitHub:** [@arindamsahoo302-cell](https://github.com/arindamsahoo302-cell)
- **Role:** Android Developer Intern

---

â­ *If you find this project helpful, feel free to give it a star!*
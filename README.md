## Chelonia - Personal Finance Tracker

**Chelonia** is an Android application for tracking personal finances, designed following Material Design principles. Currently under active development, the app includes basic features for user onboarding and note creation. This README describes the implemented functionality, technology stack, architecture, usage instructions, and planned enhancements.

---

### Table of Contents

1. [Project Overview](#project-overview)
2. [Implemented Features](#implemented-features)
3. [Technology Stack](#technology-stack)
4. [Architecture & Structure](#architecture--structure)
5. [User Interface & Design](#user-interface--design)
6. [Setup & Installation](#setup--installation)
7. [Usage](#usage)
8. [Future Enhancements](#future-enhancements)
9. [Contributing](#contributing)
10. [License](#license)

---

## Project Overview

Chelonia is a personal finance tracking application built in Java for Android devices (Android 8.1 and above). The goal is to provide users with an intuitive interface to record income/expense entries, view them by date, and gain insights into their spending or earning patterns. The project follows Material Design guidelines to ensure a modern, consistent look and feel.

> **Note:** The project is in active development. Some core features (e.g., persistent storage for notes, detailed reporting, charts) are not yet implemented.

---

## Implemented Features

Below is a summary of the functionality currently available:

* **User Onboarding (WelcomeDialog):**

    * On first launch, prompts the user to enter first name, last name, and optionally select an avatar image.
    * Stores user data in `SharedPreferences` (`user_data`) and broadcasts registration completion to update UI.
    * Multi-step ViewFlipper dialog with validation and avatar picker using `ActivityResultLauncher`.

* **MainActivity Setup:**

    * Sets up a portrait-only orientation and Edge-to-Edge UI.
    * Configures a `BottomNavigationView` with `NavController` for fragment navigation.
    * Checks onboarding status and shows `WelcomeDialog` if needed.
    * Implements `AddNoteDialog.NoteDialogListener` to show a `Toast` message when a note is created.

* **Data Models:**

    * `Note` model representing a finance entry, with fields: title, description, timestamp, payment type (hourly or fixed), amount, hourly rate, hours worked.
    * `User` model storing first name, last name, and avatar URI.

* **CalendarFragment:**

    * Displays a greeting message based on the time of day (morning, day, evening, night) and user’s first name.
    * Shows an icon corresponding to the time of day.
    * Loads avatar if set, with a fallback default icon.
    * Uses `TabLayout` and `ViewPager2` with custom tab views for "Today", "Tomorrow", "Month" sections (tabs currently set up, content to be implemented).
    * Registers a `BroadcastReceiver` to update user data when onboarding completes.

* **TodayFragment (Skeleton):**

    * Inflates a RecyclerView to display a list of `Note` entries (currently using placeholder/static sample notes).
    * Shows current date, day of week with ordinal date (e.g., "5th"), month name, time (HH\:mm), and country name.
    * Updates time display every minute using a Handler.

* **AddNoteDialog:**

    * A `DialogFragment` that allows entering a new finance note.
    * Contains inputs for title, amount (fixed payment) or hourly rate & hours worked (hourly payment). RadioGroup toggles inputs.
    * Calls back to `MainActivity` via `NoteDialogListener` with collected input, triggering a `Toast` for now.
    * Basic input parsing (note: no comprehensive validation implemented yet).

* **WelcomeDialog:**

    * Multi-step onboarding dialog with steps for name entry and avatar selection.
    * Persists user data in `SharedPreferences`.
    * Broadcasts registration completion to update other UI components.
    * Uses Material-themed styles (custom `WelcomeDialogStyle`), `ViewFlipper` animations, and indicators for steps.

---

## Technology Stack

* **Language:** Java (Android)
* **Minimum SDK:** Android 8.1 (API level 27)
* **UI Framework:** Android Jetpack components:

    * `AppCompatActivity`, `Fragment`, `ViewPager2`, `TabLayoutMediator`
    * `BottomNavigationView`, `NavController` (Jetpack Navigation)
    * `DialogFragment`, `AlertDialog` for dialogs
    * `SharedPreferences` for simple data persistence
    * `LocalBroadcastManager` for in-app broadcasts
* **Design:** Material Design guidelines
* **Libraries & Tools:**

    * AndroidX libraries
    * Google Material Components
    * Activity Result API for avatar selection

---

## Architecture & Structure

The project follows a modular package structure:

```
com.example.chelonia
├── MainActivity.java
├── dialogs
│   ├── AddNoteDialog.java
│   └── WelcomeDialog.java
├── fragments
│   └── Calendar
│       ├── CalendarFragment.java
│       ├── TodayFragment.java
│       └── (TomorrowFragment, MonthFragment to be implemented)
├── information
│   ├── Note.java
│   └── User.java
├── adapters
│   ├── NoteAdapter.java
│   └── TabsPagerAdapter.java
└── res
    ├── layout
    ├── drawable
    ├── values
    └── ...
```

* **MainActivity:** Entry point. Hosts Navigation components and onboarding.
* **Dialogs:** Modular dialogs for user input (note creation, onboarding).
* **Fragments:** Each tab or screen lives in a Fragment (Calendar, Today, Tomorrow, Month).
* **Models (information):** Data classes for Note and User.
* **Adapters:** RecyclerView adapter (`NoteAdapter`), ViewPager2 adapter (`TabsPagerAdapter`).
* **Resources:** Layout XMLs, drawables (icons for greetings, step indicators), styles (Material themes).

> **Skill Demonstrated:** Structuring an Android app with clear separation of concerns; usage of Jetpack Navigation; dynamic UI updates; dialog flows; shared preferences; custom views for tabs; time-based UI logic.

---

## User Interface & Design

* **Material Design Compliance:**

    * Use of Material Components (BottomNavigationView, TabLayout).
    * Custom styles and theming (e.g., `WelcomeDialogStyle`) aligned with Material Design.
    * Edge-to-Edge content rendering.
* **Responsive Layouts:** Portrait orientation enforced for consistent UX.
* **Onboarding Flow:** Multi-step with progress indicators.
* **Dynamic Greetings:** Changes based on time of day with appropriate icons.
* **Data Entry Dialog:** Clear separation between fixed and hourly payments, enabling/disabling fields dynamically.

*Screenshots or mockups can be added here to illustrate the UI.*

---

## Setup & Installation

1. **Prerequisites:**

    * Android Studio (latest stable version recommended).
    * Android SDK with API level 27+ installed.
    * Java 8 or above.

2. **Clone the Repository:**

   ```bash
   git clone https://github.com/yourusername/chelonia.git
   cd chelonia
   ```

3. **Open in Android Studio:**

    * Open the cloned folder as a project in Android Studio.
    * Allow Gradle to sync and download dependencies.

4. **Configure Signing (Optional):**

    * For local testing, use debug signing. For release builds, configure your keystore in `gradle.properties` or `signingConfigs`.

5. **Build & Run:**

    * Connect an Android device or launch an emulator (Android 8.1+).
    * Run the app from Android Studio.
    * On first launch, complete onboarding dialog; subsequent launches will navigate directly to main UI.

---

## Usage

* **Onboarding:** Fill in first and last name, optionally select an avatar. Canceling uses default placeholder.
* **Navigation:** Use bottom navigation to switch between primary sections (e.g., Calendar).
* **Calendar Tab:** Displays greeting and tabs for Today/Tomorrow/Month.
* **Today Tab:** Shows sample notes; future: user-added notes will appear here.
* **Add Note:** (To be integrated via a FloatingActionButton or menu) triggers `AddNoteDialog` to create a new note; currently displays a toast with details.

> **Note:** Integration of persistent storage (e.g., Room database) for notes and displaying real entries is planned in upcoming updates.

---

## Future Enhancements

Planned features and improvements:

1. **Persistent Storage for Notes:**

    * Integrate Room (SQLite) or other local database to store created notes.
    * Ensure data survives app restarts and device reboots.
2. **Data Visualization & Reports:**

    * Generate charts (e.g., monthly spending/income breakdown).
    * Use libraries like MPAndroidChart for graphical insights.
3. **Advanced Filtering & Search:**

    * Filter notes by date range, category, amount.
    * Search functionality for quick access.
4. **Notifications & Reminders:**

    * Scheduled reminders for recurring bills or income tracking.
5. **User Settings & Preferences:**

    * Customize themes (light/dark), currency, locale-specific formatting.
6. **Sync & Backup:**

    * Cloud synchronization or export/import for backup (e.g., via Google Drive or CSV export).
7. **Enhanced Onboarding:**

    * More profile fields (e.g., currency preference, default categories).
    * Tutorial or walkthrough for first-time users.
8. **Robust Input Validation & Error Handling:**

    * Validate numeric inputs; handle edge cases.
    * Graceful error messages and retry options.
9. **UI Polish & Animations:**

    * Smooth transitions between screens.
    * Animations for adding/removing notes.
10. **Localization:**

* Support multiple languages beyond default locale.

Implementing these features will showcase skills in database design, Jetpack libraries, charts, notifications, cloud APIs, and advanced UI/UX.

---

## Contributing

Contributions are welcome! If you’d like to contribute:

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/YourFeatureName`.
3. Commit your changes with clear messages.
4. Push to your fork and open a Pull Request detailing the changes.
5. Ensure code follows project style and includes appropriate tests if applicable.

---

## License

Specify the license under which this project is distributed (e.g., MIT License). Add a LICENSE file accordingly.

---

*Thank you for exploring Chelonia! Feedback and suggestions are appreciated.*

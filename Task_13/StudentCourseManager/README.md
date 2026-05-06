# StudentCourseManager

Android (Kotlin) app that manages courses with **Firebase Realtime Database** under `courses/{studentId}/{courseId}`. The student id is generated once and stored in `SharedPreferences` (UUID). Replace the placeholder Firebase config before running.

**Full step-by-step recreation guide (from scratch):** see **[DOCUMENTATION.md](DOCUMENTATION.md)**.

## Prerequisites

- [Android Studio](https://developer.android.com/studio) (recent stable version)
- JDK 11+
- A Firebase project with **Realtime Database** enabled

## Firebase setup

1. Open the [Firebase Console](https://console.firebase.google.com/), create or select a project.
2. Add an Android app with package name: `com.university.studentcoursemanager`.
3. Download **`google-services.json`** from the project settings and **replace** the placeholder file at:

   `app/google-services.json`

   The committed file contains fake values so the project structure is clear; **Realtime Database will not work until you use your real `google-services.json`.**

4. In Firebase Console, open **Realtime Database** → **Create database**. For coursework / testing you can start in **test mode** (public read/write for a limited time). Example rules (development only):

   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

   **Do not ship production apps with open rules.** Lock rules down (e.g. auth-based) before release.

5. Confirm data appears under: `courses/<your-student-uuid>/...` when you add courses in the app.

## How to run (step by step)

1. Clone or open this repo in Android Studio: **File → Open** → select the `StudentCourseManager` folder.
2. Replace `app/google-services.json` with your file from Firebase (see above).
3. Let Gradle sync finish (**File → Sync Project with Gradle Files** if needed).
4. Connect a device or start an emulator (API 24+).
5. Click **Run** (green triangle) or use **Run → Run 'app'**.

## Screenshots

_Add screenshots of the course list, add form, detail screen, and Firebase console here._

## Git: commit and push (example)

From the repository root (adjust branch and remote as needed):

```bash
git status
git add Task_13/StudentCourseManager
git commit -m "Add StudentCourseManager Firebase Realtime Database lab"
git push origin main
```

If your remote or branch name differs, replace `origin` and `main` accordingly.

## Project structure (high level)

- `MainActivity` — list, search, Firebase `addValueEventListener`, empty state, FAB to add.
- `AddCourseActivity` — create with `push()` and `setValue`.
- `EditCourseActivity` — update with `setValue`, delete with `removeValue`.
- `CourseDetailActivity` — read-only detail; FAB opens edit.
- `Course.kt` — serializable model with defaults for Firebase.

## License / coursework

Created for educational use (mobile applications coursework).

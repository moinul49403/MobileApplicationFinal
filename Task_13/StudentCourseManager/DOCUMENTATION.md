# StudentCourseManager — Full project documentation (recreate from scratch)

This document explains **what this app does**, **how it is structured**, and **how to rebuild the same project step by step** in Android Studio. Follow the phases in order.

---

## 1. What this project is

**StudentCourseManager** is a single-user Android app (Kotlin) that stores **courses** in **Firebase Realtime Database**.

- **Database path:** `courses/{studentId}/{courseId}`
- **`studentId`:** A UUID generated on first launch and saved in **SharedPreferences** (so data stays under one stable folder for that install).
- **`courseId`:** The auto-generated key from Firebase `push()`.

**Screens (four activities):**

| Screen | Purpose |
|--------|---------|
| **MainActivity** | List all courses (live updates), search by name/code, add / edit / delete entry points |
| **AddCourseActivity** | Create a course (`push()` + `setValue`) |
| **EditCourseActivity** | Update a course (`setValue` on existing key) or delete (`removeValue`) |
| **CourseDetailActivity** | Read-only detail; FAB opens edit and refreshes after update |

**CRUD mapping:**

- **Create:** `DatabaseReference.push()` then `setValue(course)` with `course.id = newRef.key`
- **Read:** `addValueEventListener` on `courses/{studentId}`
- **Update:** `child(course.id).setValue(course)`
- **Delete:** `child(course.id).removeValue()` (with confirmation dialogs where required)

---

## 2. Prerequisites

- **Android Studio** (recent stable; this repo targets **AGP 9.x**).
- **JDK 11** (project uses `JavaVersion.VERSION_11` in Gradle).
- A **Google / Firebase** account for Realtime Database.

---

## 3. Phase A — Create the Android Studio project

1. Open Android Studio → **New Project**.
2. Choose **Empty Activity** (or **Empty Views Activity**, depending on your Android Studio wording).
3. Configure:
   - **Name:** `StudentCourseManager`
   - **Package name:** `com.university.studentcoursemanager`
   - **Language:** Kotlin
   - **Minimum SDK:** API **24**
   - **Build configuration language:** Kotlin DSL (recommended, matches this repo)
4. Finish the wizard and wait for the first Gradle sync.

**Note (AGP 9):** The Android Gradle Plugin bundles Kotlin support for app modules. You typically apply **`com.android.application`** and **`com.google.gms.google-services`** only. If you also apply `org.jetbrains.kotlin.android`, you may see a duplicate Kotlin extension error—use **one** approach consistent with your AGP version.

---

## 4. Phase B — Gradle: version catalog

Create or edit **`gradle/libs.versions.toml`** so it declares:

- Versions: `agp`, `googleServices`, `firebaseBom`, AndroidX libraries, `material`, `recyclerview`, etc.
- **Libraries:** `firebase-bom`, `firebase-database-ktx`, plus `androidx-recyclerview`, `material`, `constraintlayout`, `appcompat`, `core-ktx`, `activity`, tests.
- **Plugins:** `com.android.application`, `com.google.gms.google-services`.

Example shape (adjust versions to match your environment):

```toml
[versions]
agp = "9.0.1"
googleServices = "4.4.2"
firebaseBom = "33.7.0"
# ... coreKtx, junit, appcompat, material, activity, constraintlayout, recyclerview ...

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-database-ktx = { group = "com.google.firebase", name = "firebase-database-ktx" }
# ... androidx and test libs ...

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

**Root `build.gradle.kts`:**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
}
```

**`settings.gradle.kts`:** Ensure `rootProject.name = "StudentCourseManager"` and `include(":app")`, with `google()` and `mavenCentral()` in dependency resolution.

---

## 5. Phase C — App module Gradle (`app/build.gradle.kts`)

1. **Plugins:**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}
```

2. **Android block:**

- `namespace` and `applicationId`: `com.university.studentcoursemanager`
- `minSdk = 24`, `targetSdk` / `compileSdk` as required by your AGP (this project uses **36**).
- `compileOptions` **Java 11**.

3. **Dependencies (minimum set for this lab):**

```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database.ktx)
    // tests: junit, androidx junit, espresso
}
```

Sync Gradle. Fix any version conflicts by aligning with Firebase BoM.

---

## 6. Phase D — Firebase and `google-services.json`

1. In [Firebase Console](https://console.firebase.google.com/), create a project (or use an existing one).
2. **Add an Android app** with package **`com.university.studentcoursemanager`**.
3. Download **`google-services.json`** and place it in **`app/google-services.json`**.
4. Enable **Realtime Database** → create database (for coursework, **test mode** is often used temporarily).
5. **Security rules:** For local learning only, open rules are sometimes used; **never** ship that to production. Example (development only):

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

After running the app, you should see nodes under **`courses/<uuid>/...`** matching your device’s stored student id.

---

## 7. Phase E — Android manifest

**File:** `app/src/main/AndroidManifest.xml`

1. Add **internet permission** at the top level:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

2. Register **all four activities** inside `<application>`:

- `MainActivity` — launcher (`MAIN` + `LAUNCHER`).
- `AddCourseActivity`, `EditCourseActivity`, `CourseDetailActivity` — `exported="false"` is appropriate for internal screens.

3. Set `android:theme` to your Material 3 theme (e.g. `@style/Theme.StudentCourseManager`).

---

## 8. Phase F — Theme and base resources (Material 3)

### 8.1 Colors — `res/values/colors.xml`

Define a small palette used by themes and UI:

- `primary`, `primaryDark`, `accent`, `background`, `surface`, `textPrimary`, `textSecondary`, `error`, plus `black` / `white` if needed.

### 8.2 Dimensions — `res/values/dimens.xml`

Typical keys:

- `padding_screen` (16dp), `spacing_element` (8dp)
- `card_corner_radius` (12dp), `card_elevation` (4dp)
- `fab_margin`, `item_card_padding`, `detail_label_width`, etc.

### 8.3 Strings — `res/values/strings.xml`

**Do not hardcode user-visible text in layouts.** Put titles, hints, button labels, errors, dialog strings, and **content descriptions** here.

Include at least:

- App name, screen titles (`My Courses`, `Add Course`, `Edit Course`, `Course details`)
- Search hint, empty-state copy
- Field hints (e.g. course code `CSE301`, schedule, room)
- Semester labels if referenced from code
- `Toast` messages, dialog titles/messages, accessibility strings

### 8.4 Arrays — `res/values/arrays.xml`

**`string-array` `semester_options`:** `Spring 2025`, `Summer 2025`, `Fall 2025` (for the semester **Spinner**).

### 8.5 Themes — `res/values/themes.xml` and `res/values-night/themes.xml`

- Parent: **`Theme.Material3.DayNight.NoActionBar`** (toolbar is set per screen).
- Map `colorPrimary`, `colorSurface`, `android:colorBackground`, text colors, and status bar to your palette.
- Night values can tune the same attributes for dark mode.

---

## 9. Phase G — Drawables and menu

### 9.1 Vector icons (`res/drawable/`)

Create:

| File | Use |
|------|-----|
| `ic_add.xml` | FAB “add” (+) |
| `ic_edit.xml` | Edit actions |
| `ic_delete.xml` | Delete actions |
| `ic_empty_courses.xml` | Empty-state illustration |

Use **`android:contentDescription`** in XML widgets pointing to **`strings.xml`** (not raw text).

### 9.2 Main menu — `res/menu/menu_main.xml`

- One item for **`SearchView`** as `actionViewClass`: `androidx.appcompat.widget.SearchView`, `showAsAction` so it appears in the toolbar.

---

## 10. Phase H — Layouts (XML)

Create these under **`res/layout/`**:

### 10.1 `activity_main.xml`

- Root: **`ConstraintLayout`**
- **`MaterialToolbar`**: title = “My Courses” (from strings)
- **`RecyclerView`** for the list (padding, bottom padding for FAB)
- **`include`** of empty-state layout (see below), centered in the list area; toggle visibility from code
- **`FloatingActionButton`**: `+` icon → opens **AddCourseActivity**

### 10.2 `include_empty_courses.xml`

- Vertical **`LinearLayout`** (or similar): illustration **`ImageView`**, title, short message; **`visibility="gone"`** by default

### 10.3 `item_course.xml`

- Root: **`MaterialCardView`** (corner radius + elevation from **dimens**)
- Inside: **`ConstraintLayout`**
- **TextViews:** course name, code, instructor, credit hours, schedule
- **`ImageButton`s:** edit (pencil), delete (trash), with content descriptions
- Card click = open detail; buttons = edit / delete (do not rely on card click for buttons—child buttons consume the touch)

### 10.4 `activity_add_course.xml`

- Outer: **`ConstraintLayout`** with **`MaterialToolbar`** (title “Add Course”, up navigation)
- **`ScrollView`** → inner **`ConstraintLayout`**
- **`TextInputLayout` + `TextInputEditText`:** name, code (hint e.g. CSE301), instructor, schedule, room
- **`NumberPicker`:** credits **1–4** (or a Spinner with those values)
- **`Spinner`** for semester (array resource)
- **`LinearProgressIndicator`** (or **`ProgressBar`**) for loading; initially **gone**
- **`MaterialButton`:** Save; **`Outlined`:** Cancel

### 10.5 `activity_edit_course.xml`

- Same field structure as add (duplicate XML is fine)
- Primary button: **Update course**
- **Delete** button (outlined / error color)
- Toolbar title: “Edit course”

### 10.6 `activity_course_detail.xml`

- **`ConstraintLayout`** or **`FrameLayout`** hosting:
  - **`MaterialToolbar`** with title and up navigation
  - **`NestedScrollView`** → vertical **`LinearLayout`**
  - One **`MaterialCardView`** containing labeled rows for **all** course fields
- **`FloatingActionButton`** bottom-end: opens **EditCourseActivity** with the same **`Course`**

### 10.7 `item_detail_row.xml`

- Horizontal row: label **`TextView`** (fixed width) + value **`TextView`** (`layout_weight=1`)
- Used via **`<include>`** with **`android:id`** per row (`rowName`, `rowCode`, …)

---

## 11. Phase I — Kotlin: package and files

**Package:** `com.university.studentcoursemanager`

**Suggested files and responsibilities:**

### 11.1 `Course.kt`

- **`data class`** implementing **`java.io.Serializable`**
- Fields with **defaults** (empty string / 0) for Firebase mapping:
  - `id`, `name`, `code`, `instructor`, `creditHours`, `schedule`, `room`, `semester`
- Use **`var`** if you mutate `id` after reading from Firebase (e.g. set from snapshot key).

### 11.2 `IntentKeys.kt`

- Constants for extras, e.g. **`COURSE = "course"`** (required by spec)
- Optional: **`EXTRA_DELETED`** for edit screen signaling delete back to detail

### 11.3 `IntentExtras.kt`

- Small helper for **`getSerializableExtra`** compatible with **API 33+** and older APIs:

```kotlin
inline fun <reified T : java.io.Serializable> Intent.serializableExtra(key: String): T?
```

### 11.4 `StudentIdStore.kt`

- **`SharedPreferences`** name + key
- **`getStudentId(context)`:** return stored UUID or generate, save, return

### 11.5 `FirebaseCourses.kt`

- **`ref(context)`:** `FirebaseDatabase.getInstance().reference.child("courses").child(studentId)`

### 11.6 `CourseAdapter.kt`

- **`RecyclerView.Adapter`**
- Keep a **full list** from Firebase and a **filtered list** for search
- **`submitFirebaseCourses(list)`** + **`setFilterQuery(query)`** — filter **name OR code** (case-insensitive)
- **`notifyDataSetChanged`** (or DiffUtil for polish)
- Callbacks: **card click**, **edit**, **delete**
- Optional callback: **empty vs non-empty** displayed count for empty-state visibility

### 11.7 `MainActivity.kt`

- **`setContentView(R.layout.activity_main)`**
- **`setSupportActionBar(toolbar)`**, inflate **`R.menu.menu_main`**
- Configure **`SearchView.OnQueryTextListener`** → update adapter filter on each change
- **`FirebaseCourses.ref(this).addValueEventListener`:** build `List<Course>`; for each child, **`getValue(Course::class.java)`** and set **`id` from `child.key`**
- Sort if desired (e.g. by name)
- **`RecyclerView` + `LinearLayoutManager`**
- **FAB** → `AddCourseActivity`
- **Item click** → `CourseDetailActivity` with **`putExtra(IntentKeys.COURSE, course)`**
- **Edit icon** → `EditCourseActivity`
- **Delete icon** → **`MaterialAlertDialogBuilder`**: on confirm → **`removeValue()`**
- **`onDestroy`:** **`removeEventListener`**
- **Empty state:** show when **adapter’s displayed count is 0** (including “no search results”)

### 11.8 `AddCourseActivity.kt`

- Toolbar **up** → `onBackPressedDispatcher`
- **`NumberPicker`:** min 1, max 4
- **`Spinner`** with **`ArrayAdapter.createFromResource(..., R.array.semester_options, ...)`**
- **Validate** required fields (name, code, instructor): set **`error`** on **`TextInputLayout`**
- **Save:** `val ref = FirebaseCourses.ref(this).push()`; **`course.id = ref.key`**; **`ref.setValue(course)`**
- While saving: show progress, **disable** Save and Cancel
- Success: **`Toast`**, **`finish()`**
- Cancel: **`finish()`** without saving

### 11.9 `EditCourseActivity.kt`

- Read **`intent.serializableExtra<Course>(IntentKeys.COURSE)`**; if missing or empty id, **`finish()`**
- **Pre-fill** all fields and set **NumberPicker** / **Spinner** selection from `course`
- **Update:** build updated **`Course`** with **same `id`**, **`setValue`** on **`FirebaseCourses.ref(this).child(course.id)`**
- Success: **`setResult(RESULT_OK, Intent().putExtra(IntentKeys.COURSE, updated))`**, **`finish()`**
- **Delete:** dialog **“Delete this course?”** (or equivalent string resource); on confirm **`removeValue()`**; then **`setResult`** with delete flag if detail should close, **`finish()`**
- Progress + disable buttons while busy

### 11.10 `CourseDetailActivity.kt`

- Load **`Course`** from intent
- **`bindCourseToViews()`:** for each included row, set label (from strings) and value
- **`registerForActivityResult`** launching **`EditCourseActivity`**
  - If result indicates **deleted**, **`finish()`**
  - Else if **`Course`** extra returned, update local **`course`** and refresh UI

---

## 12. Phase J — Navigation and data flow (summary diagram)

```text
MainActivity (listener on courses/{studentId})
    │
    ├─ FAB ──────────────────────► AddCourseActivity ── push() ──► Firebase
    ├─ Card ─────────────────────► CourseDetailActivity
    │                                   │
    │                                   └─ FAB ─► EditCourseActivity ── setValue / removeValue
    ├─ Edit icon ─────────────────► EditCourseActivity
    └─ Delete icon + dialog ──────► removeValue()
```

---

## 13. Phase K — Verification checklist (recreate quality gate)

- [ ] Gradle sync and **`assembleDebug`** succeed.
- [ ] **`google-services.json`** is the real file from your Firebase app.
- [ ] **Internet** permission present; all **four** activities registered.
- [ ] **Create:** new node under **`courses/{studentId}/{generatedId}`** with correct fields.
- [ ] **Read:** list updates live when data changes in the console or another client.
- [ ] **Update:** same key preserved; detail screen reflects changes after edit.
- [ ] **Delete:** removed from Firebase after confirmation from list and/or edit screen.
- [ ] **Search** filters by **name** or **code** only on the client.
- [ ] **No hardcoded** UI strings in layouts (use **`strings.xml`**).
- [ ] **Material 3** widgets, spacing (16dp / 8dp), cards 12dp radius / 4dp elevation as specified in your rubric.

---

## 14. Appendix — Repository file tree (reference)

Typical layout for this project:

```text
StudentCourseManager/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── DOCUMENTATION.md          ← this file
├── README.md
└── app/
    ├── build.gradle.kts
    ├── google-services.json
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/university/studentcoursemanager/
        │   ├── MainActivity.kt
        │   ├── AddCourseActivity.kt
        │   ├── EditCourseActivity.kt
        │   ├── CourseDetailActivity.kt
        │   ├── CourseAdapter.kt
        │   ├── Course.kt
        │   ├── IntentKeys.kt
        │   ├── IntentExtras.kt
        │   ├── StudentIdStore.kt
        │   └── FirebaseCourses.kt
        └── res/
            ├── layout/          (activities, item_course, includes, detail row)
            ├── menu/
            ├── drawable/        (vectors)
            ├── values/          (colors, dimens, strings, arrays, themes)
            ├── values-night/    (themes)
            └── xml/             (backup / data extraction if template included)
```

---

## 15. Closing notes

- This guide is written so a second developer (or your future self) can **recreate the same architecture and behaviour** without guessing order of operations: **Gradle → Firebase → manifest → resources → layouts → Kotlin → verify CRUD**.
- For a short “how to run” only, see **`README.md`**.

If you want this document inside a course report, you can cite the **phases** as lab steps and attach screenshots next to **Section 13**.

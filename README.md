# ⚡ Zenith '26 — College Fest Management System

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/ayushsachan610-sys/Kotlin-Android-Event-Portal-App)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Repository-181717?logo=github)](https://github.com/ayushsachan610-sys/Kotlin-Android-Event-Portal-App)

---

## 🌐 24/7 Permanent Live Cloud Deployment

Unlike `localhost` which turns off when your personal computer is asleep or shutdown, this application is configured for **24/7 permanent cloud hosting** with free SSL (`https://`):

| Interface | Permanent Cloud Link | Description |
| :--- | :--- | :--- |
| **Student Event Portal** | **[https://kotlin-android-event-portal-app.onrender.com/](https://kotlin-android-event-portal-app.onrender.com/)** | Discover 14 seeded events, search, category filter, view details & register |
| **Admin Coordinator Dashboard** | **[https://kotlin-android-event-portal-app.onrender.com/admin.html](https://kotlin-android-event-portal-app.onrender.com/admin.html)** | Real-time capacity utilization, attendance stats, registrations & event publishing |
| **Interactive Swagger API Docs** | **[https://kotlin-android-event-portal-app.onrender.com/docs](https://kotlin-android-event-portal-app.onrender.com/docs)** | OpenAPI schema verification & interactive endpoint execution |

### 🚀 1-Click Free Cloud Activation (Takes 60 seconds):
1. Click the **[Deploy to Render](https://render.com/deploy?repo=https://github.com/ayushsachan610-sys/Kotlin-Android-Event-Portal-App)** button above.
2. Sign in with GitHub (free, no credit card required).
3. Click **Apply / Create Web Service**. Render reads [`render.yaml`](render.yaml) and deploys your FastAPI server and SQLite database online 24/7.
4. **On Your Android Phone / App**: Tap the **⚙️ Settings** icon in the header > Tap **`☁️ Permanent Cloud Server (24/7 Online)`** > Tap **Save & Apply**! The phone app will now work anywhere in the world on mobile data (4G/5G) or Wi-Fi.

---

## 🚀 Tech Stack

| Layer | Technologies Used | Key Concepts Demonstrated |
| :--- | :--- | :--- |
| **Android Client** | **Kotlin, Android XML Layouts, Material Design Components, Retrofit 2, OkHttp, Coroutines, ViewBinding** | Native mobile UI, async HTTP networking, lifecycle-aware coroutines, bottom sheets, real-time sync, pixel-accurate Tailwind reproduction |
| **Web Client** | HTML5, Tailwind CSS (Play CDN), Vanilla JavaScript (ES6+) | Responsive cards, debounce search, category filters, dynamic registration modals |
| **Backend (Server Layer)** | Python 3, FastAPI, Uvicorn, Pydantic v2 | RESTful API design, server-side validation ("Never Trust the Client"), lifespan startup hooks |
| **Data Layer** | SQLite (`fest.db`), `openpyxl` | Relational integrity, `FOREIGN KEY`, `UNIQUE` constraints, live seat calculation (no drift) |
| **Testing** | Postman Collection v2.1, Python Test Suite | HTTP Status Codes (`200`, `201`, `400`, `404`, `409`), API contract verification |

---

## 📁 Project Structure

```text
zenith26-fest-system/
├── android/              # Native Android Studio Mobile Project (Kotlin + XML)
│   ├── app/
│   │   ├── build.gradle.kts
│   │   ├── proguard-rules.pro
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/zenith26/fest/
│   │       │   ├── ZenithApplication.kt
│   │       │   ├── data/
│   │       │   │   ├── model/         # Event, Registration, Stats, Requests
│   │       │   │   └── network/       # ZenithApiService, ApiClient, ApiConfig
│   │       │   ├── ui/
│   │       │   │   ├── MainActivity.kt
│   │       │   │   ├── student/       # StudentPortalFragment, EventAdapter, BottomSheet
│   │       │   │   ├── admin/         # AdminDashboardFragment, Adapters, AddEventDialog
│   │       │   │   └── dialogs/       # ServerSettingsDialogFragment
│   │       │   └── utils/             # CategoryHelper, DateHelper
│   │       └── res/
│   │           ├── drawable/          # Tailored Slate & Gradient cards, badges, pills
│   │           ├── layout/            # Native XML layouts for all screens
│   │           ├── values/            # Colors (Tailwind palette), strings, themes
│   │           └── xml/               # network_security_config.xml (Cleartext HTTP)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   └── gradlew / gradlew.bat
├── backend/
│   ├── database.py       # SQLite connection helper & schema creation
│   ├── models.py         # Pydantic request/response models & type safety
│   ├── seed_data.py      # Spreadsheet-to-SQLite seed engine (openpyxl)
│   └── main.py           # FastAPI application, REST endpoints, static file mounting
├── frontend/
│   ├── index.html        # Student event discovery & registration portal
│   ├── admin.html        # Fest coordinator operational dashboard
│   └── js/
│       ├── app.js        # Event catalog, live search, category filtering & registration logic
│       └── admin.js      # Live utilization charts, category breakdowns, registration management
├── data/
│   └── events_seed.xlsx  # 14 initial fest events spreadsheet
├── fest.db               # SQLite database file
├── requirements.txt      # Project Python dependencies
├── zenith26-api.postman_collection.json # Complete Postman test collection
├── Postman-Testing-Commands-Zenith26.md # Companion step-by-step Postman testing guide
└── README.md             # Project documentation
```

---

## ⚡ Quickstart Guide

### 1. Prerequisites
- **Python 3.10+** (tested on Python 3.14)
- **Android Studio** (Hedgehog, Iguana, Jellyfish, Koala, Ladybug, or newer) with Android SDK 34
- A modern web browser (for the web client) or an Android Emulator / physical device (for the Android app)

### 2. Setup Virtual Environment & Start FastAPI Backend
Open a terminal in `zenith26-fest-system/`:

```bash
# Windows PowerShell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt

# Start the FastAPI server (listening on all interfaces so emulator & devices can connect)
uvicorn backend.main:app --reload --host 0.0.0.0 --port 8000
```

Once running:
- **Interactive Swagger API Docs:** [http://localhost:8000/docs](http://localhost:8000/docs)
- **Student Event Portal (Web):** [http://localhost:8000/](http://localhost:8000/)
- **Admin Coordinator Dashboard (Web):** [http://localhost:8000/admin.html](http://localhost:8000/admin.html)

---

## 📱 Native Android Mobile Application

The native Android mobile application has been built from the ground up using **Kotlin** and **Native Android XML layouts** without Jetpack Compose, reproducing the exact visual design, color tokens, typography, layouts, animations, and interactions of the original Tailwind CSS web application.

### Key Screens & Features

1. **Student Event Portal (`StudentPortalFragment`)**:
   - **Hero Section**: Displays the fest badge, gradient title "Experience the Pulse of Zenith '26", subtitle, and the 3 metadata badges (`📅 Nov 14–15, 2026`, `📍 Campus Main Grounds`, `⚡ 14 Official Events`).
   - **Live Search**: Debounced search input that queries `GET /api/events?search=...` directly against the backend.
   - **Category Filters**: Interactive pills (`All`, `Technical`, `Cultural`, `Sports`, `Gaming`, `Workshop`) styled with active violet highlighting that query `GET /api/events?category=...`.
   - **Event Cards (`item_event_card.xml`)**: Pixel-accurate recreation of the web card:
     - Emoji icon box (`bg-slate-900` with border)
     - Category pill (`CategoryHelper` applying exact Tailwind colors)
     - Fee badge (Free in Emerald, ₹XX in Amber)
     - Title & Description clamp
     - Schedule and Venue rows
     - Live Seat Status (`FULL` in red, `X / Y seats left` in amber, `X / Y seats` in slate)
     - Violet "View Details" button
   - **Pull-to-Refresh & State Views**: Integrated `SwipeRefreshLayout`, animated loading spinner, empty state with "Reset Filters" button, and server error view with "Retry Loading".

2. **Event Details & Registration BottomSheet (`EventDetailBottomSheetDialogFragment`)**:
   - Matches the web modal layout:
     - Close button (`X`), emoji icon, category badge, fee, and full event title.
     - Event metadata grid: Date/Time, Venue, Live Seat Availability.
     - "About This Event" full description.
     - Registration Form: Student Full Name *, Roll Number *, Phone (Optional), and College Email *.
   - **Strict Validation Discipline**:
     - Client-side check preventing empty required fields and enforcing `@` in email.
     - Server-side check handling `400 Bad Request`, `404 Not Found`, and `409 Conflict` ("Already registered", "Event is full").
     - Immediate seat-count refresh across the app upon successful registration (`201 Created`).
     - Auto-dismiss after 1.8 seconds with green success confirmation badge.

3. **Admin Coordinator Dashboard (`AdminDashboardFragment`)**:
   - **Header**: Live operation status indicator, "Refresh" button, and "+ Add Event" button.
   - **4 Live Metric Cards**:
     - Total Events (🎪)
     - Total Registrations (👥)
     - Overall Utilization % (📊) with occupied seats subtext
     - Upcoming Events count (⏳)
   - **Category Breakdown Section**: Dynamic category pills with event count badges.
   - **Two Interactive Tabs**:
     - **Tab 1: Events Capacity & Demand**: Table/list of events sorted by utilization % descending, highlighting events exceeding 90% capacity with danger borders.
     - **Tab 2: Student Registration Records**: Filter by Event dropdown spinner, records counter, and registration cards with student details, timestamp, and a **Cancel Registration** button with confirmation dialog.
   - **Add Event Dialog (`AddEventDialogFragment`)**: Allows admins to publish new fest events with full server validation.

4. **Runtime Server Configuration (`ServerSettingsDialogFragment`)**:
   - Easily change the backend IP address on-the-fly without modifying code:
     - **Preset 1 (Android Emulator):** `http://10.0.2.2:8000/` (default)
     - **Preset 2 (Localhost Loopback):** `http://127.0.0.1:8000/`
     - **Preset 3 (Physical Device):** `http://<your-pc-ip>:8000/`
   - Built-in "Test Connection" button checks network reachability with live status feedback.

---

## 🛠️ How to Open and Run the Android App

### Option A: Running in Android Studio

1. Launch **Android Studio**.
2. Select **File > Open...** and navigate to:
   ```text
   zenith26-fest-system/android/
   ```
3. Allow Android Studio to sync the Gradle project.
4. Ensure your FastAPI server is running in a terminal:
   ```bash
   uvicorn backend.main:app --reload --host 0.0.0.0 --port 8000
   ```
5. Select an **Android Virtual Device (AVD)** (API 24 or newer, recommended: API 34).
6. Click the green **Run (▶)** button or press `Shift + F10`.

### Option B: Connecting from an Android Emulator
- The Android app comes pre-configured with `http://10.0.2.2:8000/` as the default base URL.
- On standard Android emulators, `10.0.2.2` automatically aliases the host development computer's `127.0.0.1:8000`, so no network changes are required!

### Option C: Connecting from a Physical Android Device
1. Connect your Android phone to the same Wi-Fi network as your development computer.
2. Find your computer's local Wi-Fi IP address:
   - On Windows: Run `ipconfig` (look for `IPv4 Address`, e.g., `192.168.1.45`).
   - On macOS/Linux: Run `ifconfig` or `ip a`.
3. Start the FastAPI backend with `--host 0.0.0.0`:
   ```bash
   uvicorn backend.main:app --reload --host 0.0.0.0 --port 8000
   ```
4. In the Zenith '26 Android app, tap the **⚙️ Settings** icon in the top header.
5. Enter your computer's IP address: `http://192.168.1.45:8000/` and tap **Test Connection**.
6. Tap **Save & Apply**. The app will immediately reconnect to your live computer backend!

---

## 📡 API Contract Specification

All endpoints are served under `/api/`:

| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/events` | List events with live computed `available_seats`. Supports `?category=` and `?search=` | `200 OK` |
| `GET` | `/api/events/{event_id}` | Fetch detailed event data for a single event | `200 OK`, `404 Not Found` |
| `POST` | `/api/events/{event_id}/register` | Register a student with strict server validation | `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `GET` | `/api/registrations` | List all registrations with event titles. Supports `?event_id=` | `200 OK` |
| `GET` | `/api/stats` | Aggregated live metrics (utilization %, upcoming count, category breakdown) | `200 OK` |
| `POST` | `/api/events` *(Stretch)* | Admin endpoint to publish a new fest event | `201 Created`, `400 Bad Request` |
| `DELETE` | `/api/registrations/{id}` *(Stretch)* | Admin endpoint to cancel a registration & restore seats | `200 OK`, `404 Not Found` |

---

## 🛡️ "Never Trust the Client" Validation Discipline

A core learning objective demonstrated in this application:
1. **Frontend Validation (Kotlin):** Instant feedback preventing empty submissions, malformed emails, and invalid capacity numbers.
2. **Server-Side Validation (FastAPI):** Independent enforcement preventing bypasses:
   - Validates existence of `event_id` (`404 Not Found`)
   - Rejects empty student name, roll number, or email (`400 Bad Request`)
   - Enforces valid email structure containing `@` (`400 Bad Request`)
   - Checks live registration count against capacity (`409 Conflict: "Event is full"`)
   - Checks for duplicate email registrations for the same event (`409 Conflict: "Already registered"`)
   - Database SQLite `UNIQUE(event_id, email)` constraint acts as the final relational guarantee

---

## 🧪 Postman API Testing

1. Open Postman Desktop.
2. Import `zenith26-api.postman_collection.json`.
3. Set `base_url` to `http://localhost:8000`.
4. Follow the test scenarios in `Postman-Testing-Commands-Zenith26.md` covering all 18 cases.

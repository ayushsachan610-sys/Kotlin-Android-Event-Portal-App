# Zenith '26 College Fest Management System — Postman Testing Guide

This guide provides the complete set of Postman requests to verify the Zenith '26 API contract. It covers all 5 core endpoints plus stretch goals, verifying both success (2xx) and client error (4xx) status codes.

---

## 1. Quick Setup in Postman Desktop

1. **Start the FastAPI backend server**:
   ```bash
   cd zenith26-fest-system
   .\.venv\Scripts\uvicorn backend.main:app --reload --port 8000
   ```
2. **Open Postman**:
   - Click **Import** (top left).
   - Drag and drop `zenith26-fest-system/zenith26-api.postman_collection.json`.
3. **Verify Environment Variable**:
   - The collection includes a variable `base_url` set to `http://localhost:8000`.

---

## 2. Testing Requests Checklist (18 Test Scenarios)

### Folder 1: Events Endpoints (`GET /api/events`)

#### Request 1.1: Fetch All Events
- **Method:** `GET`
- **URL:** `{{base_url}}/api/events`
- **Expected Status:** `200 OK`
- **Expected Response:** JSON array with 14 event objects. Each event contains `id`, `title`, `category`, `venue`, `capacity`, `fee`, and `available_seats`.

#### Request 1.2: Filter Events by Category (Technical)
- **Method:** `GET`
- **URL:** `{{base_url}}/api/events?category=Technical`
- **Expected Status:** `200 OK`
- **Expected Response:** JSON array with only `category == "Technical"` events (e.g. Code Sprint 2.0, Robo Race, AI Pitch Fest, Web Design Rumble).

#### Request 1.3: Search Events by Title Keyword
- **Method:** `GET`
- **URL:** `{{base_url}}/api/events?search=Coding`
- **Expected Status:** `200 OK`
- **Expected Response:** Events matching "Coding" in title (e.g. "Intro to Vibe Coding").

#### Request 1.4: Search with Non-Existent Term
- **Method:** `GET`
- **URL:** `{{base_url}}/api/events?search=NonExistentKeywordXYZ`
- **Expected Status:** `200 OK`
- **Expected Response:** `[]` (empty JSON array).

---

### Folder 2: Event Details (`GET /api/events/{event_id}`)

#### Request 2.1: Get Event Details for ID 1
- **Method:** `GET`
- **URL:** `{{base_url}}/api/events/1`
- **Expected Status:** `200 OK`
- **Expected Response:**
  ```json
  {
    "id": 1,
    "title": "Code Sprint 2.0",
    "category": "Technical",
    "description": "A high-intensity coding competition...",
    "event_date": "2026-11-14",
    "event_time": "10:00 AM",
    "venue": "Main Auditorium",
    "capacity": 100,
    "fee": 0,
    "icon": "💻",
    "available_seats": 100
  }
  ```

#### Request 2.2: Event Details Not Found (404)
- **Method:** `GET`
- **URL:** `{{base_url}}/api/events/999`
- **Expected Status:** `404 Not Found`
- **Expected Response:**
  ```json
  {
    "detail": "Event with id 999 not found"
  }
  ```

---

### Folder 3: Registration (`POST /api/events/{event_id}/register`)

#### Request 3.1: Valid Registration (201 Created)
- **Method:** `POST`
- **URL:** `{{base_url}}/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Ayush Sachan",
    "roll_number": "26HSB042",
    "email": "ayush.sachan@college.edu",
    "phone": "9876543210"
  }
  ```
- **Expected Status:** `201 Created`
- **Expected Response:**
  ```json
  {
    "id": 1,
    "event_id": 1,
    "student_name": "Ayush Sachan",
    "registered_at": "2026-..."
  }
  ```

#### Request 3.2: Duplicate Registration on Same Event (409 Conflict)
- **Method:** `POST`
- **URL:** `{{base_url}}/api/events/1/register`
- **Body:** *Repeat the exact body above*
- **Expected Status:** `409 Conflict`
- **Expected Response:**
  ```json
  {
    "detail": "Already registered"
  }
  ```

#### Request 3.3: Missing Required Fields (400 Bad Request)
- **Method:** `POST`
- **URL:** `{{base_url}}/api/events/1/register`
- **Body:**
  ```json
  {
    "student_name": "",
    "roll_number": "26HSB042",
    "email": "ayush@college.edu"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Expected Response:**
  ```json
  {
    "detail": "Student name, roll number, and email must not be empty"
  }
  ```

#### Request 3.4: Invalid Email Format (400 Bad Request)
- **Method:** `POST`
- **URL:** `{{base_url}}/api/events/1/register`
- **Body:**
  ```json
  {
    "student_name": "Priya Sharma",
    "roll_number": "26HSB088",
    "email": "priyasharmacollege.edu"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Expected Response:**
  ```json
  {
    "detail": "Invalid email: email must contain '@'"
  }
  ```

#### Request 3.5: Register for Non-Existent Event (404 Not Found)
- **Method:** `POST`
- **URL:** `{{base_url}}/api/events/999/register`
- **Body:**
  ```json
  {
    "student_name": "Priya Sharma",
    "roll_number": "26HSB088",
    "email": "priya.new@college.edu"
  }
  ```
- **Expected Status:** `404 Not Found`
- **Expected Response:**
  ```json
  {
    "detail": "Event with id 999 not found"
  }
  ```

---

### Folder 4: Registration Records (`GET /api/registrations`)

#### Request 4.1: List All Registrations
- **Method:** `GET`
- **URL:** `{{base_url}}/api/registrations`
- **Expected Status:** `200 OK`
- **Expected Response:** Array of registrations with `event_title` included.

#### Request 4.2: Filter Registrations by Event ID
- **Method:** `GET`
- **URL:** `{{base_url}}/api/registrations?event_id=1`
- **Expected Status:** `200 OK`
- **Expected Response:** Array filtered to registrations for Event 1.

---

### Folder 5: Live Stats (`GET /api/stats`)

#### Request 5.1: Fetch Dashboard Stats
- **Method:** `GET`
- **URL:** `{{base_url}}/api/stats`
- **Expected Status:** `200 OK`
- **Expected Response:**
  ```json
  {
    "total_events": 14,
    "total_registrations": 1,
    "total_capacity": 1507,
    "overall_utilization_pct": 0.1,
    "upcoming_events_count": 14,
    "events_by_category": {
      "Cultural": 5,
      "Technical": 4,
      "Sports": 2,
      "Workshop": 2,
      "Gaming": 1
    }
  }
  ```

---

### Folder 6: Stretch Goals

#### Request 6.1: Admin Create Event (`POST /api/events`)
- **Method:** `POST`
- **URL:** `{{base_url}}/api/events`
- **Body:**
  ```json
  {
    "title": "Quantum Computing Keynote",
    "category": "Technical",
    "description": "Inspiring keynote on quantum algorithms.",
    "event_date": "2026-11-15",
    "event_time": "04:30 PM",
    "venue": "Auditorium Hall B",
    "capacity": 80,
    "fee": 0,
    "icon": "⚛️"
  }
  ```
- **Expected Status:** `201 Created`

#### Request 6.2: Admin Cancel Registration (`DELETE /api/registrations/{id}`)
- **Method:** `DELETE`
- **URL:** `{{base_url}}/api/registrations/1`
- **Expected Status:** `200 OK`
- **Expected Response:**
  ```json
  {
    "message": "Registration cancelled successfully",
    "id": 1,
    "event_id": 1,
    "student_name": "Ayush Sachan"
  }
  ```

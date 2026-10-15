from contextlib import asynccontextmanager
from datetime import datetime
from pathlib import Path
from typing import List, Optional

from fastapi import FastAPI, HTTPException, Query, status
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, JSONResponse
from fastapi.staticfiles import StaticFiles

from backend.database import get_db_connection, init_db
from backend.models import (
    EventDetail,
    EventIn,
    EventOut,
    RegistrationIn,
    RegistrationListItem,
    RegistrationOut,
    StatsOut,
)
from backend.seed_data import seed_events

FRONTEND_DIR = Path(__file__).resolve().parent.parent / "frontend"


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifespan event ensuring the database schema and seed data are initialized on startup."""
    init_db()
    seed_events()
    yield


app = FastAPI(
    title="Zenith '26 Fest Management System API",
    description="API for the Zenith '26 College Fest Management System (Events, Registrations, Analytics)",
    version="1.0.0",
    lifespan=lifespan,
)

# Enable CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc):
    """Ensure missing or invalid request schema returns 400 Bad Request to honor the API contract."""
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={"detail": "Invalid or missing required fields in request payload"},
    )


# ============================================================================
# API Endpoints
# ============================================================================

@app.get("/api/events", response_model=List[EventOut], tags=["Events"])
def get_events(
    category: Optional[str] = Query(None, description="Filter events by category (e.g. Technical, Cultural, Sports, Gaming, Workshop)"),
    search: Optional[str] = Query(None, description="Case-insensitive title search"),
):
    """
    List events with computed available_seats (capacity minus live registration count).
    Supports optional category filter and title search.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT e.id, e.title, e.category, e.description, e.event_date, e.event_time,
               e.venue, e.capacity, e.fee, e.icon,
               (e.capacity - COUNT(r.id)) AS available_seats
        FROM events e
        LEFT JOIN registrations r ON e.id = r.event_id
        WHERE 1=1
    """
    params = []

    if category and category.strip() and category.strip().lower() != "all":
        query += " AND LOWER(e.category) = LOWER(?)"
        params.append(category.strip())

    if search and search.strip():
        query += " AND LOWER(e.title) LIKE LOWER(?)"
        params.append(f"%{search.strip()}%")

    query += " GROUP BY e.id ORDER BY e.id ASC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    events = []
    for r in rows:
        events.append(
            EventOut(
                id=r["id"],
                title=r["title"],
                category=r["category"],
                description=r["description"],
                event_date=r["event_date"],
                event_time=r["event_time"],
                venue=r["venue"],
                capacity=r["capacity"],
                fee=r["fee"],
                icon=r["icon"],
                available_seats=max(0, r["available_seats"]),
            )
        )
    return events


@app.get("/api/events/{event_id}", response_model=EventDetail, tags=["Events"])
def get_event_detail(event_id: int):
    """
    Fetch a single event's details by its ID, including live available_seats.
    Returns 404 if the event does not exist.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
        SELECT e.id, e.title, e.category, e.description, e.event_date, e.event_time,
               e.venue, e.capacity, e.fee, e.icon,
               (e.capacity - COUNT(r.id)) AS available_seats
        FROM events e
        LEFT JOIN registrations r ON e.id = r.event_id
        WHERE e.id = ?
        GROUP BY e.id
    """, (event_id,))
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with id {event_id} not found",
        )

    return EventDetail(
        id=row["id"],
        title=row["title"],
        category=row["category"],
        description=row["description"],
        event_date=row["event_date"],
        event_time=row["event_time"],
        venue=row["venue"],
        capacity=row["capacity"],
        fee=row["fee"],
        icon=row["icon"],
        available_seats=max(0, row["available_seats"]),
    )


@app.post("/api/events/{event_id}/register", response_model=RegistrationOut, status_code=status.HTTP_201_CREATED, tags=["Registrations"])
def register_for_event(event_id: int, payload: RegistrationIn):
    """
    Register a student for an event with strict server-side validation:
    - 404: Event does not exist
    - 400: student_name, roll_number, email empty or missing
    - 400: Email does not contain '@'
    - 409: Event is full (capacity reached)
    - 409: Already registered (same email for this event)
    """
    # 1. Server-side validation of string inputs
    student_name = payload.student_name.strip() if payload.student_name else ""
    roll_number = payload.roll_number.strip() if payload.roll_number else ""
    email = payload.email.strip().lower() if payload.email else ""
    phone = payload.phone.strip() if payload.phone else None

    if not student_name or not roll_number or not email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Student name, roll number, and email must not be empty",
        )

    if "@" not in email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid email: email must contain '@'",
        )

    conn = get_db_connection()
    cursor = conn.cursor()

    # 2. Check if event exists
    cursor.execute("SELECT id, title, capacity FROM events WHERE id = ?", (event_id,))
    event = cursor.fetchone()
    if not event:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with id {event_id} not found",
        )

    capacity = event["capacity"]

    # 3. Check live registration count against capacity
    cursor.execute("SELECT COUNT(*) AS count FROM registrations WHERE event_id = ?", (event_id,))
    current_count = cursor.fetchone()["count"]
    if current_count >= capacity:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Event is full",
        )

    # 4. Check if student email is already registered for this event
    cursor.execute("SELECT id FROM registrations WHERE event_id = ? AND LOWER(email) = ?", (event_id, email))
    existing_reg = cursor.fetchone()
    if existing_reg:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Already registered",
        )

    # 5. Insert new registration with server-generated ISO timestamp
    registered_at = datetime.now().isoformat()
    try:
        cursor.execute("""
            INSERT INTO registrations (event_id, student_name, roll_number, email, phone, registered_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """, (event_id, student_name, roll_number, email, phone, registered_at))
        conn.commit()
        new_id = cursor.lastrowid
    except Exception as e:
        conn.close()
        # Handle database UNIQUE constraint collision as safety net
        if "UNIQUE" in str(e).upper():
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Already registered",
            )
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Database error: {str(e)}",
        )

    conn.close()

    return RegistrationOut(
        id=new_id,
        event_id=event_id,
        student_name=student_name,
        registered_at=registered_at,
    )


@app.get("/api/registrations", response_model=List[RegistrationListItem], tags=["Registrations"])
def get_registrations(event_id: Optional[int] = Query(None, description="Optional filter by event_id")):
    """
    List registrations. Includes event_title in each record to avoid client-side lookups.
    Can be filtered by event_id.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT r.id, r.event_id, e.title AS event_title, r.student_name,
               r.roll_number, r.email, r.phone, r.registered_at
        FROM registrations r
        JOIN events e ON r.event_id = e.id
        WHERE 1=1
    """
    params = []

    if event_id is not None:
        query += " AND r.event_id = ?"
        params.append(event_id)

    query += " ORDER BY r.id DESC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    registrations = []
    for r in rows:
        registrations.append(
            RegistrationListItem(
                id=r["id"],
                event_id=r["event_id"],
                event_title=r["event_title"],
                student_name=r["student_name"],
                roll_number=r["roll_number"],
                email=r["email"],
                phone=r["phone"],
                registered_at=r["registered_at"],
            )
        )
    return registrations


@app.get("/api/stats", response_model=StatsOut, tags=["Stats"])
def get_stats():
    """
    Returns live aggregated stats computed freshly from the database:
    - total_events
    - total_registrations
    - total_capacity
    - overall_utilization_pct (total_registrations / total_capacity * 100, 1 decimal place)
    - upcoming_events_count (events with event_date >= today)
    - events_by_category (dict of category -> count)
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    # 1. Total events and total capacity
    cursor.execute("SELECT COUNT(*) AS total_events, COALESCE(SUM(capacity), 0) AS total_capacity FROM events")
    ev_row = cursor.fetchone()
    total_events = ev_row["total_events"]
    total_capacity = ev_row["total_capacity"]

    # 2. Total registrations
    cursor.execute("SELECT COUNT(*) AS total_registrations FROM registrations")
    total_registrations = cursor.fetchone()["total_registrations"]

    # 3. Overall utilization percentage
    overall_utilization_pct = 0.0
    if total_capacity > 0:
        overall_utilization_pct = round((total_registrations / total_capacity) * 100, 1)

    # 4. Upcoming events count (event_date >= today's date in YYYY-MM-DD format)
    today_str = datetime.now().strftime("%Y-%m-%d")
    cursor.execute("SELECT COUNT(*) AS upcoming FROM events WHERE event_date >= ?", (today_str,))
    upcoming_events_count = cursor.fetchone()["upcoming"]

    # 5. Events count by category
    cursor.execute("SELECT category, COUNT(*) AS count FROM events GROUP BY category ORDER BY count DESC")
    cat_rows = cursor.fetchall()
    events_by_category = {r["category"]: r["count"] for r in cat_rows}

    conn.close()

    return StatsOut(
        total_events=total_events,
        total_registrations=total_registrations,
        total_capacity=total_capacity,
        overall_utilization_pct=overall_utilization_pct,
        upcoming_events_count=upcoming_events_count,
        events_by_category=events_by_category,
    )


# ============================================================================
# Stretch Goal Endpoints (Admin Add Event & Cancel Registration)
# ============================================================================

@app.post("/api/events", response_model=EventOut, status_code=status.HTTP_201_CREATED, tags=["Events"])
def create_event(payload: EventIn):
    """
    Stretch Goal: Allows admin to add a new event with full server-side validation.
    """
    title = payload.title.strip()
    category = payload.category.strip()
    venue = payload.venue.strip()
    event_date = payload.event_date.strip()
    event_time = payload.event_time.strip()

    if not title or not category or not venue or not event_date or not event_time:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Title, category, venue, event_date, and event_time are required",
        )

    if payload.capacity <= 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Capacity must be greater than 0",
        )

    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
        INSERT INTO events (title, category, description, event_date, event_time, venue, capacity, fee, icon)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, (
        title,
        category,
        payload.description,
        event_date,
        event_time,
        venue,
        payload.capacity,
        payload.fee,
        payload.icon or "📅",
    ))
    conn.commit()
    new_event_id = cursor.lastrowid
    conn.close()

    return EventOut(
        id=new_event_id,
        title=title,
        category=category,
        description=payload.description,
        event_date=event_date,
        event_time=event_time,
        venue=venue,
        capacity=payload.capacity,
        fee=payload.fee,
        icon=payload.icon or "📅",
        available_seats=payload.capacity,
    )


@app.delete("/api/registrations/{registration_id}", tags=["Registrations"])
def cancel_registration(registration_id: int):
    """
    Stretch Goal: Allows admin to cancel a registration by ID, automatically
    restoring available seat count for that event.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id, event_id, student_name FROM registrations WHERE id = ?", (registration_id,))
    reg = cursor.fetchone()
    if not reg:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Registration with id {registration_id} not found",
        )

    cursor.execute("DELETE FROM registrations WHERE id = ?", (registration_id,))
    conn.commit()
    conn.close()

    return {
        "message": "Registration cancelled successfully",
        "id": registration_id,
        "event_id": reg["event_id"],
        "student_name": reg["student_name"],
    }


# ============================================================================
# Static Files & Frontend Routing (Step 4)
# ============================================================================

# Mount JavaScript folder so /js/app.js and /js/admin.js are served
if (FRONTEND_DIR / "js").exists():
    app.mount("/js", StaticFiles(directory=FRONTEND_DIR / "js"), name="js")


@app.get("/", include_in_schema=False)
def serve_home():
    """Serves the student event portal (frontend/index.html)."""
    index_file = FRONTEND_DIR / "index.html"
    if index_file.exists():
        return FileResponse(index_file)
    return {"message": "frontend/index.html not found"}


@app.get("/admin.html", include_in_schema=False)
def serve_admin():
    """Serves the admin dashboard (frontend/admin.html)."""
    admin_file = FRONTEND_DIR / "admin.html"
    if admin_file.exists():
        return FileResponse(admin_file)
    return {"message": "frontend/admin.html not found"}

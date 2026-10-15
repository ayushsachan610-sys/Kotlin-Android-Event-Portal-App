import os
import shutil
import sqlite3
from pathlib import Path

# Database path anchored to the zenith26-fest-system project root
ORIGINAL_DB_PATH = Path(__file__).resolve().parent.parent / "fest.db"

# In serverless environments like Vercel, the source tree is read-only at runtime.
# SQLite requires write permissions to create journals and write registrations,
# so we copy fest.db to /tmp when running on Vercel.
if os.environ.get("VERCEL"):
    DB_PATH = Path("/tmp/fest.db")
    if not DB_PATH.exists() and ORIGINAL_DB_PATH.exists():
        shutil.copy2(ORIGINAL_DB_PATH, DB_PATH)
else:
    DB_PATH = ORIGINAL_DB_PATH


def get_db_connection():
    """Returns a SQLite connection with row_factory set to sqlite3.Row and foreign keys enabled."""
    if os.environ.get("VERCEL") and not DB_PATH.exists() and ORIGINAL_DB_PATH.exists():
        shutil.copy2(ORIGINAL_DB_PATH, DB_PATH)
    conn = sqlite3.connect(str(DB_PATH))
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON;")
    return conn


def init_db():
    """Initializes the database schema for events and registrations tables."""
    conn = get_db_connection()
    cursor = conn.cursor()

    # 1. Create events table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS events (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            category TEXT NOT NULL,
            description TEXT,
            event_date TEXT NOT NULL,
            event_time TEXT NOT NULL,
            venue TEXT NOT NULL,
            capacity INTEGER NOT NULL,
            fee INTEGER NOT NULL DEFAULT 0,
            icon TEXT
        )
    """)

    # 2. Create registrations table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS registrations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            event_id INTEGER NOT NULL REFERENCES events(id),
            student_name TEXT NOT NULL,
            roll_number TEXT NOT NULL,
            email TEXT NOT NULL,
            phone TEXT,
            registered_at TEXT NOT NULL,
            UNIQUE(event_id, email)
        )
    """)

    conn.commit()
    conn.close()


if __name__ == "__main__":
    init_db()
    print(f"Database initialized successfully at {DB_PATH}")

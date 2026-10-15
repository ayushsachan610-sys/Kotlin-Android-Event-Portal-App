from typing import Optional, Dict
from pydantic import BaseModel, Field


class EventOut(BaseModel):
    id: int
    title: str
    category: str
    description: Optional[str] = None
    event_date: str
    event_time: str
    venue: str
    capacity: int
    fee: int
    icon: Optional[str] = None
    available_seats: int


# EventDetail uses the same shape as EventOut for this project
class EventDetail(EventOut):
    pass


class RegistrationIn(BaseModel):
    student_name: Optional[str] = Field(default="", description="Full name of student")
    roll_number: Optional[str] = Field(default="", description="College roll number")
    email: Optional[str] = Field(default="", description="Valid email address")
    phone: Optional[str] = Field(default=None, description="Optional contact phone number")


class RegistrationOut(BaseModel):
    id: int
    event_id: int
    student_name: str
    registered_at: str


class RegistrationListItem(BaseModel):
    id: int
    event_id: int
    event_title: str
    student_name: str
    roll_number: str
    email: str
    phone: Optional[str] = None
    registered_at: str


class StatsOut(BaseModel):
    total_events: int
    total_registrations: int
    total_capacity: int
    overall_utilization_pct: float
    upcoming_events_count: int
    events_by_category: Dict[str, int]


# For Stretch Goal: Admin create event
class EventIn(BaseModel):
    title: str = Field(..., min_length=1)
    category: str = Field(..., min_length=1)
    description: Optional[str] = None
    event_date: str = Field(..., min_length=1)
    event_time: str = Field(..., min_length=1)
    venue: str = Field(..., min_length=1)
    capacity: int = Field(..., gt=0)
    fee: int = Field(0, ge=0)
    icon: Optional[str] = "📅"

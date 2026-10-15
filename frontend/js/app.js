// ============================================================================
// Zenith '26 — Student Event Portal Frontend Logic (app.js)
// ============================================================================

let currentCategory = "All";
let currentSearch = "";
let currentSelectedEvent = null;
let searchDebounceTimer = null;

// DOM Elements
const eventsGrid = document.getElementById("events-grid");
const statusContainer = document.getElementById("status-container");
const searchInput = document.getElementById("search-input");
const categoryFilterContainer = document.getElementById("category-filters");

// Modal Elements
const registrationModal = document.getElementById("registration-modal");
const modalCloseBtn = document.getElementById("modal-close-btn");
const registrationForm = document.getElementById("registration-form");
const modalAlert = document.getElementById("modal-alert");
const regSubmitBtn = document.getElementById("reg-submit-btn");

const modalIcon = document.getElementById("modal-icon");
const modalCategory = document.getElementById("modal-category");
const modalFee = document.getElementById("modal-fee");
const modalTitle = document.getElementById("modal-title");
const modalDatetime = document.getElementById("modal-datetime");
const modalVenue = document.getElementById("modal-venue");
const modalSeats = document.getElementById("modal-seats");
const modalDescription = document.getElementById("modal-description");
const modalEventId = document.getElementById("modal-event-id");

// ----------------------------------------------------------------------------
// Category badge style helper
// ----------------------------------------------------------------------------
function getCategoryBadgeClasses(category) {
  switch (category.toLowerCase()) {
    case "technical":
      return "bg-cyan-500/20 text-cyan-300 border-cyan-500/30";
    case "cultural":
      return "bg-fuchsia-500/20 text-fuchsia-300 border-fuchsia-500/30";
    case "sports":
      return "bg-amber-500/20 text-amber-300 border-amber-500/30";
    case "gaming":
      return "bg-emerald-500/20 text-emerald-300 border-emerald-500/30";
    case "workshop":
      return "bg-violet-500/20 text-violet-300 border-violet-500/30";
    default:
      return "bg-slate-700/50 text-slate-300 border-slate-600/50";
  }
}

// ----------------------------------------------------------------------------
// Fetch and Render Events
// ----------------------------------------------------------------------------
async function fetchAndRenderEvents() {
  // Show loading state
  eventsGrid.innerHTML = "";
  statusContainer.className = "py-16 text-center animate-fade-in";
  statusContainer.innerHTML = `
    <div class="inline-flex flex-col items-center justify-center space-y-3">
      <div class="w-10 h-10 border-4 border-violet-500/30 border-t-violet-500 rounded-full animate-spin"></div>
      <p class="text-sm font-medium text-slate-400">Loading Zenith '26 events...</p>
    </div>
  `;

  // Build query string
  const params = new URLSearchParams();
  if (currentCategory && currentCategory !== "All") {
    params.append("category", currentCategory);
  }
  if (currentSearch && currentSearch.trim()) {
    params.append("search", currentSearch.trim());
  }

  const url = `/api/events${params.toString() ? "?" + params.toString() : ""}`;

  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Server returned HTTP ${response.status}`);
    }

    const events = await response.json();

    // Hide status container
    statusContainer.className = "hidden";
    statusContainer.innerHTML = "";

    // Empty state check
    if (!events || events.length === 0) {
      statusContainer.className = "py-16 text-center animate-fade-in";
      statusContainer.innerHTML = `
        <div class="max-w-md mx-auto p-6 bg-slate-800/40 rounded-2xl border border-slate-700/60">
          <span class="text-4xl block mb-2">🔍</span>
          <h3 class="text-base font-bold text-slate-200">No events match your search</h3>
          <p class="text-xs text-slate-400 mt-1">Try clearing your search query or selecting a different category.</p>
          <button id="reset-filters-btn" class="mt-4 px-4 py-1.5 rounded-lg text-xs font-semibold bg-violet-600 hover:bg-violet-500 text-white transition">
            Reset Filters
          </button>
        </div>
      `;
      document.getElementById("reset-filters-btn")?.addEventListener("click", () => {
        searchInput.value = "";
        currentSearch = "";
        setActiveCategory("All");
        fetchAndRenderEvents();
      });
      return;
    }

    // Render event cards
    eventsGrid.innerHTML = events.map(createEventCardHTML).join("");

    // Attach click listeners to "View Details" buttons
    document.querySelectorAll(".view-details-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        const eventId = parseInt(btn.getAttribute("data-event-id"), 10);
        openEventModal(eventId);
      });
    });
  } catch (error) {
    console.error("Failed to fetch events:", error);
    statusContainer.className = "py-16 text-center animate-fade-in";
    statusContainer.innerHTML = `
      <div class="max-w-md mx-auto p-6 bg-rose-950/40 rounded-2xl border border-rose-800/60 text-slate-200">
        <span class="text-4xl block mb-2">⚠️</span>
        <h3 class="text-base font-bold text-rose-300">Could not connect to the server</h3>
        <p class="text-xs text-slate-400 mt-1 mb-4">Make sure the FastAPI backend is running at http://localhost:8021.</p>
        <button id="retry-fetch-btn" class="px-4 py-2 rounded-lg text-xs font-bold bg-rose-600 hover:bg-rose-500 text-white transition">
          Retry Loading
        </button>
      </div>
    `;
    document.getElementById("retry-fetch-btn")?.addEventListener("click", fetchAndRenderEvents);
  }
}

// ----------------------------------------------------------------------------
// Create Event Card HTML
// ----------------------------------------------------------------------------
function createEventCardHTML(event) {
  const isFull = event.available_seats === 0;
  const badgeClasses = getCategoryBadgeClasses(event.category);
  const feeDisplay = event.fee > 0 ? `₹${event.fee}` : "Free";

  // Seats status badge
  let seatBadge;
  if (isFull) {
    seatBadge = `<span class="px-2 py-0.5 rounded-md text-xs font-black bg-rose-500 text-white uppercase tracking-wider animate-pulse">FULL</span>`;
  } else if (event.available_seats <= 10) {
    seatBadge = `<span class="px-2 py-0.5 rounded-md text-xs font-semibold bg-amber-500/20 text-amber-300 border border-amber-500/30">${event.available_seats} / ${event.capacity} seats left</span>`;
  } else {
    seatBadge = `<span class="px-2 py-0.5 rounded-md text-xs font-medium text-slate-400">${event.available_seats} / ${event.capacity} seats</span>`;
  }

  return `
    <article class="group bg-slate-800/80 hover:bg-slate-800 border border-slate-700/80 hover:border-violet-500/50 rounded-2xl p-5 shadow-lg shadow-black/20 hover:shadow-violet-900/20 transition-all duration-200 flex flex-col justify-between animate-fade-in">
      <div>
        <!-- Top row: Icon, Category Badge, Fee -->
        <div class="flex items-center justify-between mb-3.5">
          <span class="text-3xl p-2 rounded-xl bg-slate-900 border border-slate-700/60 group-hover:scale-105 transition-transform">
            ${event.icon || "⚡"}
          </span>
          <div class="flex items-center gap-2">
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold border ${badgeClasses}">
              ${event.category}
            </span>
            <span class="text-xs font-bold ${event.fee > 0 ? 'text-amber-400' : 'text-emerald-400'}">
              ${feeDisplay}
            </span>
          </div>
        </div>

        <!-- Title & Description preview -->
        <h3 class="text-lg font-bold text-white tracking-tight group-hover:text-violet-300 transition-colors line-clamp-1">
          ${event.title}
        </h3>
        <p class="text-xs text-slate-400 mt-1 line-clamp-2 leading-relaxed min-h-[2rem]">
          ${event.description || "Join us for an exciting fest experience."}
        </p>

        <!-- Metadata (Date/Time & Venue) -->
        <div class="mt-4 pt-3 border-t border-slate-700/60 space-y-1.5 text-xs text-slate-300">
          <div class="flex items-center gap-2">
            <span class="text-slate-500">📅</span>
            <span>${event.event_date} &bull; ${event.event_time}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-slate-500">📍</span>
            <span class="truncate">${event.venue}</span>
          </div>
        </div>
      </div>

      <!-- Bottom row: Seats & Action button -->
      <div class="mt-5 pt-3 border-t border-slate-700/60 flex items-center justify-between gap-3">
        <div>${seatBadge}</div>
        <button
          data-event-id="${event.id}"
          class="view-details-btn px-3.5 py-1.5 rounded-xl text-xs font-bold bg-violet-600 hover:bg-violet-500 active:bg-violet-700 text-white shadow-md shadow-violet-900/30 hover:shadow-violet-600/40 transition active:scale-95"
        >
          View Details
        </button>
      </div>
    </article>
  `;
}

// ----------------------------------------------------------------------------
// Open & Populate Event Detail Modal
// ----------------------------------------------------------------------------
async function openEventModal(eventId) {
  modalAlert.className = "hidden";
  modalAlert.innerHTML = "";
  registrationForm.reset();

  try {
    const res = await fetch(`/api/events/${eventId}`);
    if (!res.ok) {
      throw new Error(`Failed to load event details: ${res.status}`);
    }
    const event = await res.json();
    currentSelectedEvent = event;

    // Populate modal fields
    modalEventId.value = event.id;
    modalIcon.textContent = event.icon || "⚡";
    modalTitle.textContent = event.title;
    modalCategory.textContent = event.category;
    modalCategory.className = `px-2.5 py-0.5 rounded-full text-xs font-semibold border ${getCategoryBadgeClasses(event.category)}`;
    modalFee.textContent = event.fee > 0 ? `Fee: ₹${event.fee}` : "Free Entry";
    modalDatetime.textContent = `${event.event_date} at ${event.event_time}`;
    modalVenue.textContent = event.venue;
    modalDescription.textContent = event.description || "No detailed description provided.";

    const isFull = event.available_seats <= 0;
    if (isFull) {
      modalSeats.innerHTML = `<span class="text-rose-400 font-bold">FULL (0 seats available)</span>`;
      regSubmitBtn.disabled = true;
      regSubmitBtn.classList.add("opacity-50", "cursor-not-allowed");
      regSubmitBtn.querySelector("span").textContent = "Registration Closed (Full)";
    } else {
      modalSeats.innerHTML = `<span class="text-emerald-400 font-bold">${event.available_seats} seats available</span> <span class="text-slate-400">(out of ${event.capacity})</span>`;
      regSubmitBtn.disabled = false;
      regSubmitBtn.classList.remove("opacity-50", "cursor-not-allowed");
      regSubmitBtn.querySelector("span").textContent = "Confirm Registration";
    }

    registrationModal.classList.remove("hidden");
  } catch (error) {
    console.error("Modal open error:", error);
    alert("Could not load event details. Please verify your backend server.");
  }
}

function closeEventModal() {
  registrationModal.classList.add("hidden");
  currentSelectedEvent = null;
  registrationForm.reset();
  modalAlert.className = "hidden";
  modalAlert.innerHTML = "";
}

// ----------------------------------------------------------------------------
// Registration Form Submission & Validation
// ----------------------------------------------------------------------------
registrationForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  const eventId = modalEventId.value;
  const nameInput = document.getElementById("reg-name");
  const rollInput = document.getElementById("reg-roll");
  const emailInput = document.getElementById("reg-email");
  const phoneInput = document.getElementById("reg-phone");

  const nameVal = nameInput.value.trim();
  const rollVal = rollInput.value.trim();
  const emailVal = emailInput.value.trim();
  const phoneVal = phoneInput.value.trim();

  // Reset alert
  modalAlert.className = "hidden";
  modalAlert.innerHTML = "";

  // Client-Side Validation
  if (!nameVal || !rollVal || !emailVal) {
    showModalAlert("Please fill in all required fields (Name, Roll Number, and Email).", "error");
    return;
  }

  if (!emailVal.includes("@")) {
    showModalAlert("Please provide a valid email containing '@'.", "error");
    return;
  }

  // Set submitting state
  regSubmitBtn.disabled = true;
  const originalBtnText = regSubmitBtn.querySelector("span").textContent;
  regSubmitBtn.querySelector("span").textContent = "Submitting registration...";

  try {
    const payload = {
      student_name: nameVal,
      roll_number: rollVal,
      email: emailVal,
      phone: phoneVal || null,
    };

    const res = await fetch(`/api/events/${eventId}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    const data = await res.json();

    if (res.status === 201) {
      // Success (201)
      showModalAlert(
        `🎉 Successfully registered for <strong>${currentSelectedEvent?.title || 'the event'}</strong>! Registration ID: #${data.id}`,
        "success"
      );
      registrationForm.reset();

      // Refresh event seat count in modal and grid
      await fetchAndRenderEvents();

      // Automatically close modal after 1.8 seconds
      setTimeout(() => {
        closeEventModal();
      }, 1800);
    } else {
      // 400 or 409 or other server-side validation error
      const errorMessage = data.detail || "Registration failed. Please check your inputs.";
      showModalAlert(errorMessage, "error");
    }
  } catch (err) {
    console.error("Registration request failed:", err);
    showModalAlert("Network error: Could not connect to backend server.", "error");
  } finally {
    regSubmitBtn.disabled = false;
    regSubmitBtn.querySelector("span").textContent = originalBtnText;
  }
});

function showModalAlert(message, type) {
  modalAlert.classList.remove("hidden");
  if (type === "success") {
    modalAlert.className = "mb-4 p-3.5 rounded-xl text-xs sm:text-sm font-medium bg-emerald-950/70 text-emerald-200 border border-emerald-700/80 animate-fade-in";
  } else {
    modalAlert.className = "mb-4 p-3.5 rounded-xl text-xs sm:text-sm font-medium bg-rose-950/70 text-rose-200 border border-rose-700/80 animate-fade-in";
  }
  modalAlert.innerHTML = message;
}

// ----------------------------------------------------------------------------
// Event Listeners for Filters & Search
// ----------------------------------------------------------------------------
function setActiveCategory(category) {
  currentCategory = category;
  document.querySelectorAll(".filter-btn").forEach((btn) => {
    const cat = btn.getAttribute("data-category");
    if (cat === category) {
      btn.className = "filter-btn px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-violet-600 text-white border border-violet-500 shadow-sm transition active:scale-95";
    } else {
      btn.className = "filter-btn px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-slate-800 text-slate-300 border border-slate-700 hover:bg-slate-700 hover:text-white transition active:scale-95";
    }
  });
}

categoryFilterContainer.addEventListener("click", (e) => {
  const btn = e.target.closest(".filter-btn");
  if (!btn) return;
  const category = btn.getAttribute("data-category");
  setActiveCategory(category);
  fetchAndRenderEvents();
});

searchInput.addEventListener("input", (e) => {
  clearTimeout(searchDebounceTimer);
  searchDebounceTimer = setTimeout(() => {
    currentSearch = e.target.value;
    fetchAndRenderEvents();
  }, 250);
});

// Modal close triggers
modalCloseBtn.addEventListener("click", closeEventModal);
registrationModal.addEventListener("click", (e) => {
  if (e.target === registrationModal) {
    closeEventModal();
  }
});

// Escape key closes modal
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape" && !registrationModal.classList.contains("hidden")) {
    closeEventModal();
  }
});

// Initialize on page load
document.addEventListener("DOMContentLoaded", () => {
  fetchAndRenderEvents();
});

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

// Spooky Oracle Elements
const spookyOracleModal = document.getElementById("spooky-oracle-modal");
const oracleCloseBtn = document.getElementById("oracle-close-btn");
const closeOracleBtn = document.getElementById("close-oracle-btn");
const nextJokeBtn = document.getElementById("next-joke-btn");
const oracleJokeBox = document.getElementById("oracle-joke-box");
const spookyOracleBtn = document.getElementById("spooky-oracle-btn");
const spookySoundBtn = document.getElementById("spooky-sound-btn");

// ----------------------------------------------------------------------------
// Spooky Comedy Jokes & Developer Fortunes
// ----------------------------------------------------------------------------
const SPOOKY_JOKES = [
  "🎃 <strong>Developer Horror Story:</strong> Why do programmers love Halloween? Because OCT 31 == DEC 25! 🧙‍♂️",
  "👻 <strong>Spectral Warning:</strong> There are only two things that terrify developers: ghosts, and touching legacy code on a Friday at 5:00 PM! 💀",
  "🧛 <strong>Vampire Fact:</strong> Count Dracula loves programming in <em>C-Sharp</em> because it sharpens his fangs! 🩸",
  "🧟‍♂️ <strong>Zombie Alert:</strong> Why did the zombie join the hackathon? Because he heard developers were running out of brains! 🧠",
  "☕ <strong>Fest Survival Rule:</strong> Coffee machines are currently operating on dark roast witchcraft and 400% caffeine sorcery! ⚡",
  "🔮 <strong>The Oracle Predicts:</strong> Your registration will succeed, but you will definitely need a nap before the midnight hackathon! 🛌",
  "🧙‍♀️ <strong>Grimoire Wisdom:</strong> <em>'Double, double, toil and trouble; undefined is not a function in the bubble!'</em> 🧪",
  "🕷️ <strong>Spider Notice:</strong> The spider in the top-right corner is our chief QA engineer. Bug count is currently 0! 🕸️",
  "⚰️ <strong>Afterlife Assurance:</strong> If your code segfaults at 3:00 AM, the festival ghosts will automatically format your tabs to spaces! 🦇"
];

let currentJokeIndex = 0;

// ----------------------------------------------------------------------------
// Web Audio API Synthesizer (Funny Sound Effects without external files)
// ----------------------------------------------------------------------------
function playSpookySound(type = "cackle") {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();

    if (type === "cackle") {
      const freqs = [587, 659, 784, 880, 784, 988, 1174, 880, 587];
      freqs.forEach((f, idx) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = "sawtooth";
        osc.frequency.setValueAtTime(f + (Math.random() * 40 - 20), ctx.currentTime + idx * 0.07);
        gain.gain.setValueAtTime(0.09, ctx.currentTime + idx * 0.07);
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + idx * 0.07 + 0.11);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(ctx.currentTime + idx * 0.07);
        osc.stop(ctx.currentTime + idx * 0.07 + 0.11);
      });
    } else if (type === "chime") {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = "sine";
      osc.frequency.setValueAtTime(440, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.3);
      gain.gain.setValueAtTime(0.12, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 0.5);
    }
  } catch (e) {
    // Silent catch if audio context is blocked
  }
}


// ----------------------------------------------------------------------------
// Category badge & spooky tag helper
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
      return "bg-purple-500/20 text-purple-300 border-purple-500/30";
    default:
      return "bg-slate-800 text-slate-300 border-slate-700";
  }
}

function getCategorySpookyTag(category) {
  switch (category.toLowerCase()) {
    case "technical":
      return "👻 0 Syntax Errors Guaranteed";
    case "cultural":
      return "🧛 Vampire Waltz Approved";
    case "sports":
      return "🧟 Zombie Cardio Workout";
    case "gaming":
      return "💀 Infinite Respawns Enabled";
    case "workshop":
      return "🧙‍♂️ Potions & Grimoire 101";
    default:
      return "🎃 100% Haunted";
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
      <div class="w-12 h-12 border-4 border-orange-500/30 border-t-orange-500 rounded-full animate-spin"></div>
      <p class="text-sm font-bold text-orange-300 flex items-center gap-2">
        <span>🎃</span>
        <span>Summoning Zenith '26 fest events from the crypts...</span>
        <span>👻</span>
      </p>
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
        <div class="max-w-md mx-auto p-6 bg-slate-900/80 rounded-2xl border border-orange-900/60 shadow-xl text-center">
          <span class="text-5xl block mb-2 animate-wobble-pumpkin">🕷️</span>
          <h3 class="text-base font-bold text-orange-300">No spirits found in this corner!</h3>
          <p class="text-xs text-slate-400 mt-1">Try banishing your search query or picking another cursed category.</p>
          <button id="reset-filters-btn" class="mt-4 px-4 py-2 rounded-xl text-xs font-bold bg-orange-600 hover:bg-orange-500 text-white transition active:scale-95 shadow-md shadow-orange-950">
            🎃 Reset Filters
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
      <div class="max-w-md mx-auto p-6 bg-rose-950/60 rounded-2xl border border-rose-800/60 text-slate-200 shadow-xl">
        <span class="text-5xl block mb-2 animate-pulse">💀</span>
        <h3 class="text-base font-bold text-rose-300">The Server Ghouls Are Sleeping</h3>
        <p class="text-xs text-slate-400 mt-1 mb-4">Could not summon events from the FastAPI backend. Make sure the server is awake!</p>
        <button id="retry-fetch-btn" class="px-4 py-2 rounded-xl text-xs font-bold bg-rose-600 hover:bg-rose-500 text-white transition active:scale-95">
          ⚡ Re-awaken Backend
        </button>
      </div>
    `;
    document.getElementById("retry-fetch-btn")?.addEventListener("click", fetchAndRenderEvents);
  }
}

// ----------------------------------------------------------------------------
// Create Event Card HTML with Halloween Comedy & Animation
// ----------------------------------------------------------------------------
function createEventCardHTML(event) {
  const isFull = event.available_seats === 0;
  const badgeClasses = getCategoryBadgeClasses(event.category);
  const spookyTag = getCategorySpookyTag(event.category);
  const feeDisplay = event.fee > 0 ? `₹${event.fee}` : "Free Ritual";

  // Seats status badge
  let seatBadge;
  if (isFull) {
    seatBadge = `<span class="px-2.5 py-1 rounded-md text-xs font-black bg-rose-600 text-white uppercase tracking-wider animate-pulse flex items-center gap-1"><span>💀</span><span>COFFINS FULL</span></span>`;
  } else if (event.available_seats <= 10) {
    seatBadge = `<span class="px-2.5 py-1 rounded-md text-xs font-semibold bg-orange-500/20 text-orange-300 border border-orange-500/40 flex items-center gap-1"><span>🎃</span><span>${event.available_seats} / ${event.capacity} seats left</span></span>`;
  } else {
    seatBadge = `<span class="px-2.5 py-1 rounded-md text-xs font-medium text-slate-400 flex items-center gap-1"><span>🍬</span><span>${event.available_seats} / ${event.capacity} seats</span></span>`;
  }

  return `
    <article class="group bg-slate-900/90 hover:bg-slate-900 border border-orange-950 hover:border-orange-500/60 rounded-2xl p-5 shadow-lg shadow-black/40 hover:shadow-orange-950/40 transition-all duration-200 flex flex-col justify-between animate-fade-in relative overflow-hidden halloween-glow-card">
      <div>
        <!-- Top row: Icon, Category Badge, Fee -->
        <div class="flex items-center justify-between mb-3.5">
          <span class="text-3xl p-2.5 rounded-xl bg-slate-950 border border-orange-900/60 group-hover:scale-110 group-hover:rotate-6 transition-transform shadow-inner">
            ${event.icon || "🎃"}
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

        <!-- Comedy Tag -->
        <div class="mb-2">
          <span class="text-[10px] font-bold text-orange-400/90 bg-orange-950/60 px-2 py-0.5 rounded border border-orange-800/40">
            ${spookyTag}
          </span>
        </div>

        <!-- Title & Description preview -->
        <h3 class="text-lg font-black text-white tracking-tight group-hover:text-orange-300 transition-colors line-clamp-1">
          ${event.title}
        </h3>
        <p class="text-xs text-slate-400 mt-1 line-clamp-2 leading-relaxed min-h-[2rem]">
          ${event.description || "Join us for an exciting fest experience."}
        </p>

        <!-- Metadata (Date/Time & Venue) -->
        <div class="mt-4 pt-3 border-t border-slate-800 space-y-1.5 text-xs text-slate-300">
          <div class="flex items-center gap-2">
            <span class="text-orange-400">📅</span>
            <span>${event.event_date} &bull; ${event.event_time}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-orange-400">📍</span>
            <span class="truncate">${event.venue}</span>
          </div>
        </div>
      </div>

      <!-- Bottom row: Seats & Action button -->
      <div class="mt-5 pt-3 border-t border-slate-800 flex items-center justify-between gap-3">
        <div>${seatBadge}</div>
        <button
          data-event-id="${event.id}"
          class="view-details-btn px-3.5 py-1.5 rounded-xl text-xs font-bold bg-gradient-to-r from-orange-600 to-amber-600 hover:from-orange-500 hover:to-amber-500 active:from-orange-700 active:to-amber-700 text-white shadow-md shadow-orange-950 hover:shadow-orange-600/40 transition active:scale-95 flex items-center gap-1.5"
        >
          <span>🎃</span>
          <span>View Details</span>
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
      playSpookySound("chime");
      showModalAlert(
        `🎉 <strong>HAUNTED SUCCESS!</strong> Your mortal presence has been secured for <strong>${currentSelectedEvent?.title || 'the event'}</strong>! 🎃 Registration Grimoire ID: <strong>#${data.id}</strong>. A festival spirit has dispatched your confirmation raven! 🦇`,
        "success"
      );
      registrationForm.reset();

      // Refresh event seat count in modal and grid
      await fetchAndRenderEvents();

      // Automatically close modal after 2.4 seconds
      setTimeout(() => {
        closeEventModal();
      }, 2400);
    } else {
      // 400 or 409 or other server-side validation error
      playSpookySound("cackle");
      let errorMessage = data.detail || "Registration ritual failed. Please check your mortal inputs.";
      if (res.status === 409) {
        errorMessage = `💀 <strong>CURSED DUPLICATION:</strong> ${errorMessage} (The Book of the Dead already contains this soul for this event!)`;
      }
      showModalAlert(errorMessage, "error");
    }
  } catch (err) {
    console.error("Registration request failed:", err);
    showModalAlert("💀 <strong>SPELL DISRUPTED:</strong> Network error — Could not reach the festival server.", "error");
  } finally {
    regSubmitBtn.disabled = false;
    regSubmitBtn.querySelector("span").textContent = originalBtnText;
  }
});

function showModalAlert(message, type) {
  modalAlert.classList.remove("hidden");
  if (type === "success") {
    modalAlert.className = "mb-4 p-3.5 rounded-xl text-xs sm:text-sm font-medium bg-emerald-950/80 text-emerald-200 border border-emerald-500/80 animate-fade-in shadow-lg shadow-emerald-950";
  } else {
    modalAlert.className = "mb-4 p-3.5 rounded-xl text-xs sm:text-sm font-medium bg-rose-950/80 text-rose-200 border border-rose-500/80 animate-fade-in shadow-lg shadow-rose-950";
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
      btn.className = "filter-btn px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-orange-600 text-white border border-orange-500 shadow-md shadow-orange-950 transition active:scale-95";
    } else {
      btn.className = "filter-btn px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-slate-900 text-slate-300 border border-slate-800 hover:bg-slate-800 hover:text-orange-300 transition active:scale-95";
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

// ----------------------------------------------------------------------------
// Spooky Oracle Modal Functions & Listeners
// ----------------------------------------------------------------------------
function showNextJoke() {
  playSpookySound("chime");
  currentJokeIndex = (currentJokeIndex + 1) % SPOOKY_JOKES.length;
  if (oracleJokeBox) {
    oracleJokeBox.classList.remove("animate-fade-in");
    void oracleJokeBox.offsetWidth; // trigger reflow
    oracleJokeBox.classList.add("animate-fade-in");
    oracleJokeBox.innerHTML = SPOOKY_JOKES[currentJokeIndex];
  }
}

function openSpookyOracle() {
  playSpookySound("cackle");
  if (oracleJokeBox) {
    oracleJokeBox.innerHTML = SPOOKY_JOKES[currentJokeIndex];
  }
  if (spookyOracleModal) {
    spookyOracleModal.classList.remove("hidden");
  }
}

function closeSpookyOracle() {
  if (spookyOracleModal) {
    spookyOracleModal.classList.add("hidden");
  }
}

if (spookyOracleBtn) {
  spookyOracleBtn.addEventListener("click", openSpookyOracle);
}
if (oracleCloseBtn) {
  oracleCloseBtn.addEventListener("click", closeSpookyOracle);
}
if (closeOracleBtn) {
  closeOracleBtn.addEventListener("click", closeSpookyOracle);
}
if (nextJokeBtn) {
  nextJokeBtn.addEventListener("click", showNextJoke);
}
if (spookySoundBtn) {
  spookySoundBtn.addEventListener("click", () => {
    playSpookySound("cackle");
  });
}

if (spookyOracleModal) {
  spookyOracleModal.addEventListener("click", (e) => {
    if (e.target === spookyOracleModal) {
      closeSpookyOracle();
    }
  });
}

// Modal close triggers
modalCloseBtn.addEventListener("click", closeEventModal);
registrationModal.addEventListener("click", (e) => {
  if (e.target === registrationModal) {
    closeEventModal();
  }
});

// Escape key closes modals
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") {
    if (registrationModal && !registrationModal.classList.contains("hidden")) {
      closeEventModal();
    }
    if (spookyOracleModal && !spookyOracleModal.classList.contains("hidden")) {
      closeSpookyOracle();
    }
  }
});

// Initialize on page load
document.addEventListener("DOMContentLoaded", () => {
  fetchAndRenderEvents();
});


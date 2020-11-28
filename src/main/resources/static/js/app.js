document.addEventListener("DOMContentLoaded", () => {
    initWebSocket();
    initBetslip();
});

// --- GLOBAL STATE ---
let selectedBet = null;

// --- WEBSOCKET CONNECTION ---
function initWebSocket() {
    // Dynamically resolve WebSocket protocol based on page protocol
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const host = window.location.host;
    const wsUrl = `${protocol}//${host}/ws/odds`;

    console.log(`Connecting to odds WebSocket at: ${wsUrl}`);
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
        console.log("Successfully connected to live odds WebSocket stream");
    };

    ws.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            console.log("Received live odds update from WebSocket:", data);
            handleLiveOddsUpdate(data);
        } catch (err) {
            console.error("Failed to parse WebSocket message:", err);
        }
    };

    ws.onclose = () => {
        console.log("WebSocket connection closed. Reconnecting in 5 seconds...");
        setTimeout(initWebSocket, 5000);
    };

    ws.onerror = (error) => {
        console.error("WebSocket error observed:", error);
    };
}

// --- HANDLE LIVE ODDS UPDATES ---
function handleLiveOddsUpdate(update) {
    // Find the odds button on the page by its data-id
    const btn = document.querySelector(`.odds-btn[data-odds-id="${update.oddsId}"]`);
    if (!btn) return;

    // 1. Handle Odds status updates (ACTIVE / SUSPENDED)
    if (update.status === "SUSPENDED") {
        btn.classList.add("suspended-overlay");
    } else {
        btn.classList.remove("suspended-overlay");
    }

    // 2. Handle price changes & flash animation
    const oddsValEl = btn.querySelector(".odds-val");
    if (oddsValEl) {
        const oldVal = parseFloat(oddsValEl.textContent);
        const newVal = parseFloat(update.oddsValue);

        if (oldVal !== newVal) {
            oddsValEl.textContent = newVal.toFixed(2);
            
            // Flash animations
            if (newVal > oldVal) {
                btn.classList.add("flash-up");
                setTimeout(() => btn.classList.remove("flash-up"), 1500);
            } else {
                btn.classList.add("flash-down");
                setTimeout(() => btn.classList.remove("flash-down"), 1500);
            }

            // Update current betslip active selection if the price changed
            if (selectedBet && selectedBet.oddsId === update.oddsId) {
                selectedBet.oddsValue = newVal;
                const slipOddsValEl = document.getElementById("slip-odds-value");
                if (slipOddsValEl) slipOddsValEl.textContent = newVal.toFixed(2);
                updatePayoutCalculation();
                
                // Show a quick tooltip alerting user to the price change
                showNotification("Slip updated: Odds changed to " + newVal.toFixed(2), "info");
            }
        }
    }
}

// --- INTERACTIVE BETSLIP DRAWER ---
function initBetslip() {
    const oddsButtons = document.querySelectorAll(".odds-btn");
    
    oddsButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            if (btn.classList.contains("suspended-overlay")) return;

            const oddsId = btn.getAttribute("data-odds-id");
            const eventId = btn.getAttribute("data-event-id");
            const matchName = btn.getAttribute("data-match-name");
            const selection = btn.getAttribute("data-selection");
            const oddsValue = parseFloat(btn.querySelector(".odds-val").textContent);

            // Populate slip state
            selectedBet = {
                oddsId,
                eventId,
                matchName,
                selection,
                oddsValue,
                stake: 10.0 // default stake
            };

            renderBetslip();
        });
    });
}

function renderBetslip() {
    const emptyEl = document.getElementById("slip-empty");
    const contentEl = document.getElementById("slip-content");

    if (!selectedBet) {
        emptyEl.style.display = "block";
        contentEl.style.display = "none";
        return;
    }

    emptyEl.style.display = "none";
    contentEl.style.display = "block";

    // Set Text Values
    document.getElementById("slip-match-name").textContent = selectedBet.matchName;
    document.getElementById("slip-selection-name").textContent = selectedBet.selection.replace("_", " ");
    document.getElementById("slip-odds-value").textContent = selectedBet.oddsValue.toFixed(2);
    
    const stakeInput = document.getElementById("slip-stake");
    stakeInput.value = selectedBet.stake.toFixed(2);

    // Attach listeners
    stakeInput.oninput = () => {
        selectedBet.stake = parseFloat(stakeInput.value) || 0;
        updatePayoutCalculation();
    };

    updatePayoutCalculation();

    const placeBetBtn = document.getElementById("btn-place-bet");
    placeBetBtn.onclick = submitBetSlip;
}

function updatePayoutCalculation() {
    if (!selectedBet) return;
    const payout = selectedBet.stake * selectedBet.oddsValue;
    document.getElementById("slip-payout").textContent = payout.toFixed(2);
}

// --- SUBMIT BETSLIP AJAX ---
function submitBetSlip() {
    if (!selectedBet || selectedBet.stake <= 0) {
        showNotification("Please enter a valid stake amount", "error");
        return;
    }

    const placeBetBtn = document.getElementById("btn-place-bet");
    placeBetBtn.disabled = true;
    placeBetBtn.textContent = "PLACING BET...";

    const payload = {
        eventId: selectedBet.eventId,
        oddsId: selectedBet.oddsId,
        selectionName: selectedBet.selection,
        oddsValue: selectedBet.oddsValue,
        stake: selectedBet.stake
    };

    fetch("/api/bets", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    })
    .then(async response => {
        const text = await response.text();
        if (response.ok) {
            showNotification("Bet placed successfully! Good luck!", "success");
            selectedBet = null;
            renderBetslip();
            // Refresh page after 1.5 seconds to reflect wallet balance & ticket updates
            setTimeout(() => window.location.reload(), 1500);
        } else {
            showNotification(text || "Failed to place bet. Try again.", "error");
            placeBetBtn.disabled = false;
            placeBetBtn.textContent = "PLACE BET";
        }
    })
    .catch(err => {
        console.error("Error submitting bet:", err);
        showNotification("Connection error. Please try again.", "error");
        placeBetBtn.disabled = false;
        placeBetBtn.textContent = "PLACE BET";
    });
}

// --- USER FEEDBACK NOTIFICATIONS ---
function showNotification(msg, type) {
    const notifyEl = document.getElementById("notification-toast");
    if (!notifyEl) {
        // Create element dynamically if it doesn't exist
        const toast = document.createElement("div");
        toast.id = "notification-toast";
        toast.style.position = "fixed";
        toast.style.bottom = "20px";
        toast.style.right = "20px";
        toast.style.zIndex = "1000";
        toast.style.padding = "1rem 1.5rem";
        toast.style.borderRadius = "8px";
        toast.style.fontWeight = "600";
        toast.style.color = "#fff";
        toast.style.boxShadow = "0 8px 30px rgba(0,0,0,0.5)";
        toast.style.transform = "translateY(100px)";
        toast.style.transition = "transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)";
        document.body.appendChild(toast);
    }

    const toast = document.getElementById("notification-toast");
    toast.textContent = msg;

    if (type === "success") {
        toast.style.background = "linear-gradient(135deg, hsl(142, 70%, 45%), hsl(142, 70%, 35%))";
        toast.style.border = "1px solid hsl(142, 70%, 45%)";
    } else if (type === "error") {
        toast.style.background = "linear-gradient(135deg, hsl(346, 84%, 55%), hsl(346, 84%, 45%))";
        toast.style.border = "1px solid hsl(346, 84%, 55%)";
    } else {
        toast.style.background = "linear-gradient(135deg, hsl(210, 100%, 59%), hsl(210, 100%, 45%))";
        toast.style.border = "1px solid hsl(210, 100%, 59%)";
    }

    // Slide up
    toast.style.transform = "translateY(0)";

    // Slide down after 4 seconds
    setTimeout(() => {
        toast.style.transform = "translateY(150px)";
    }, 4000);
}

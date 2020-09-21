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


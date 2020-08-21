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

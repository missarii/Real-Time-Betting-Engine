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

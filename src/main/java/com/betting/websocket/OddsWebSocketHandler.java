package com.betting.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class OddsWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OddsWebSocketHandler.class);
    
    // Thread-safe set to store active WebSocket sessions
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        logger.info("New WebSocket connection established: session ID = {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        logger.info("WebSocket connection closed: session ID = {}, status = {}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // We only broadcast from server to client in this sportsbook setup. 
        // No client-to-server messaging is processed here, keeping communication efficient.
        logger.debug("Received message from client {}: {}", session.getId(), message.getPayload());
    }

    /**
     * Broadcasts a message to all active WebSocket connections.
     */
    public void broadcast(String messagePayload) {
        logger.debug("Broadcasting update to {} active WebSocket session(s)", sessions.size());
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(messagePayload));
                } catch (IOException e) {
                    logger.error("Error sending WebSocket message to session {}: {}", session.getId(), e.getMessage());
                    try {
                        session.close();
                    } catch (IOException ignored) {}
                    sessions.remove(session);
                }
            } else {
                sessions.remove(session);
            }
        }
    }
}

package com.betting.controller;

import com.betting.model.Event;
import com.betting.repository.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

/**
 * Serves the main Sportsbook page - /events
 * Wallet is injected automatically by GlobalModelAdvice for the navbar.
 */
@Controller
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


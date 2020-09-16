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

    @GetMapping("/events")
    public String listEvents(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        // All non-finished events (SCHEDULED, LIVE, SUSPENDED)
        List<Event> activeEvents = eventRepository.findByStatusNot(Event.EventStatus.FINISHED);
        model.addAttribute("events", activeEvents);

        return "events";
    }
}

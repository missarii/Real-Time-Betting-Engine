package com.betting.controller;

import com.betting.dto.BetRequest;
import com.betting.model.Bet;
import com.betting.service.BettingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Handles bet slip history view (/bets) and the AJAX bet placement API (/api/bets).
 * Wallet injected automatically by GlobalModelAdvice for the navbar balance pill.
 */
@Controller
public class BettingController {

    private final BettingService bettingService;

    public BettingController(BettingService bettingService) {
        this.bettingService = bettingService;
    }

    @GetMapping("/bets")
    public String betHistory(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        List<Bet> bets = bettingService.getBetHistory(principal.getName());
        model.addAttribute("bets", bets);
        return "bet";
    }

    @PostMapping("/api/bets")
    @ResponseBody
    public ResponseEntity<?> placeBet(Principal principal,
                                      @Valid @RequestBody BetRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).body("You must be logged in to place bets");
        }
        try {
            Bet bet = bettingService.placeBet(principal.getName(), request);
            // Return a minimal JSON response to avoid lazy-load serialization issues
            return ResponseEntity.ok().body(
                    java.util.Map.of(
                            "betId", bet.getId().toString(),
                            "status", bet.getStatus().name(),
                            "stake", bet.getStake(),
                            "potentialPayout", bet.getPotentialPayout()
                    )
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }
}

package com.betting.controller;

import com.betting.model.Bet;
import com.betting.model.Event;
import com.betting.model.Transaction;
import com.betting.repository.EventRepository;
import com.betting.service.BettingService;
import com.betting.service.WalletService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

    private final WalletService walletService;
    private final BettingService bettingService;
    private final EventRepository eventRepository;

    public UserController(WalletService walletService,
                          BettingService bettingService,
                          EventRepository eventRepository) {
        this.walletService = walletService;
        this.bettingService = bettingService;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/")
    public String index(Principal principal) {
        return principal != null ? "redirect:/dashboard" : "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String userDashboard(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        String username = principal.getName();

        // wallet is already injected by GlobalModelAdvice — no duplicate fetch needed

        // Recent Transactions (last 5)
        List<Transaction> transactions = walletService.getTransactionHistory(username);
        model.addAttribute("transactions",
                transactions.size() > 5 ? transactions.subList(0, 5) : transactions);

        // Recent Bets (last 5)
        List<Bet> bets = bettingService.getBetHistory(username);
        model.addAttribute("bets",
                bets.size() > 5 ? bets.subList(0, 5) : bets);

        // Live matches for quick betslip on dashboard
        List<Event> liveEvents = eventRepository.findByStatus(Event.EventStatus.LIVE);
        model.addAttribute("liveEvents", liveEvents);

        return "dashboard";
    }
}

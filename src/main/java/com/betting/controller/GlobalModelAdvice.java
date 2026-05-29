package com.betting.controller;

import com.betting.model.Wallet;
import com.betting.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.security.Principal;

/**
 * Automatically injects the authenticated user's wallet into every
 * Thymeleaf model so that the navbar balance pill works on all pages.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalModelAdvice.class);

    private final WalletService walletService;

    public GlobalModelAdvice(WalletService walletService) {
        this.walletService = walletService;
    }

    @ModelAttribute("wallet")
    public Wallet injectWallet(Principal principal) {
        if (principal == null) {
            // Return a dummy wallet with zero balance for unauthenticated pages (login/register)
            Wallet dummy = new Wallet();
            dummy.setBalance(BigDecimal.ZERO);
            dummy.setCurrency("USD");
            return dummy;
        }
        try {
            return walletService.getWalletByUsername(principal.getName());
        } catch (Exception e) {
            logger.warn("Could not load wallet for user {}: {}", principal.getName(), e.getMessage());
            Wallet dummy = new Wallet();
            dummy.setBalance(BigDecimal.ZERO);
            dummy.setCurrency("USD");
            return dummy;
        }
    }
}

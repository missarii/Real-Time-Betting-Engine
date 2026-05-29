package com.betting.controller;

import com.betting.exception.InsufficientBalanceException;
import com.betting.model.Transaction;
import com.betting.model.Wallet;
import com.betting.service.WalletService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/wallet")
    public String viewWallet(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String username = principal.getName();
        Wallet wallet = walletService.getWalletByUsername(username);
        List<Transaction> transactions = walletService.getTransactionHistory(username);

        model.addAttribute("wallet", wallet);
        model.addAttribute("transactions", transactions);
        return "wallet";
    }

    @PostMapping("/wallet/deposit")
    public String handleDeposit(Principal principal,
                                @RequestParam("amount") BigDecimal amount,
                                Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            walletService.deposit(principal.getName(), amount, "User web portal deposit");
            return "redirect:/wallet?depositSuccess=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            // reload wallet details
            loadWalletDetails(principal.getName(), model);
            return "wallet";
        }
    }

    @PostMapping("/wallet/withdraw")
    public String handleWithdraw(Principal principal,
                                 @RequestParam("amount") BigDecimal amount,
                                 Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            walletService.withdraw(principal.getName(), amount, "User web portal withdrawal");
            return "redirect:/wallet?withdrawSuccess=true";
        } catch (InsufficientBalanceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            loadWalletDetails(principal.getName(), model);
            return "wallet";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            loadWalletDetails(principal.getName(), model);
            return "wallet";
        }
    }

    private void loadWalletDetails(String username, Model model) {
        Wallet wallet = walletService.getWalletByUsername(username);
        List<Transaction> transactions = walletService.getTransactionHistory(username);
        model.addAttribute("wallet", wallet);
        model.addAttribute("transactions", transactions);
    }
}

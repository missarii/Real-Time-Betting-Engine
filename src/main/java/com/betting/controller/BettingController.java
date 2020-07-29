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


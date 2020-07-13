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

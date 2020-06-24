package com.betting.controller;

import com.betting.exception.InsufficientBalanceException;
import com.betting.model.Transaction;
import com.betting.model.Wallet;
import com.betting.service.WalletService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

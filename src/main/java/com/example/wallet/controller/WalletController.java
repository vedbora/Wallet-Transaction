package com.example.wallet.controller;

import com.example.wallet.config.OpenApiConfig;
import com.example.wallet.dto.WalletBalanceResponse;
import com.example.wallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class WalletController {

    private final TransactionService transactionService;

    @GetMapping("/balance")
    @Operation(summary = "Current balance", description = "Computed from all transactions for the authenticated user.")
    public WalletBalanceResponse balance() {
        return new WalletBalanceResponse(transactionService.currentBalanceForCurrentUser());
    }
}

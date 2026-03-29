package com.example.wallet.controller;

import com.example.wallet.config.OpenApiConfig;
import com.example.wallet.dto.TransactionCreateRequest;
import com.example.wallet.dto.TransactionResponse;
import com.example.wallet.dto.TransactionUpdateRequest;
import com.example.wallet.model.enums.TransactionType;
import com.example.wallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create transaction", description = "Creates a transaction for the authenticated user.")
    public TransactionResponse create(@Valid @RequestBody TransactionCreateRequest request) {
        return transactionService.create(request);
    }

    @GetMapping
    @Operation(
            summary = "List transactions",
            description = "USER: own transactions only. ADMIN: all users. Optional filters by type and createdAt range (UTC).")
    public List<TransactionResponse> list(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return transactionService.findAll(type, from, to);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "USER: own rows only. ADMIN: any row.")
    public TransactionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        return transactionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete transaction", description = "USER: own rows only. ADMIN: any row.")
    public void delete(@PathVariable Long id) {
        transactionService.delete(id);
    }

    @GetMapping("/balance")
    @Operation(summary = "Get current balance for user")
    public java.util.Map<String, java.math.BigDecimal> getBalance() {
        return java.util.Map.of("balance", transactionService.currentBalanceForCurrentUser());
    }
}

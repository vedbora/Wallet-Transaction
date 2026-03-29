package com.example.wallet.dto;

import com.example.wallet.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Transaction representation")
public class TransactionResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "100.00")
    private BigDecimal amount;

    @Schema(example = "CREDIT")
    private TransactionType type;

    @Schema(example = "Invoice #12")
    private String description;

    @Schema(example = "2")
    private Long userId;

    @Schema(description = "Creation timestamp (UTC)")
    private Instant createdAt;

    @Schema(description = "Running wallet balance for this user immediately after this transaction")
    private BigDecimal balanceAfter;

    @Schema(description = "Other party in a transfer")
    private String counterpartyEmail;

    @Schema(description = "Pairs two rows created by one transfer")
    private String transferGroupId;
}

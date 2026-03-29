package com.example.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Current wallet balance for the authenticated user")
public class WalletBalanceResponse {

    @Schema(example = "1250.50")
    private BigDecimal balance;
}

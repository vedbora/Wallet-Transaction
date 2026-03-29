package com.example.wallet.dto;

import com.example.wallet.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Create a transaction for the authenticated user")
public class TransactionCreateRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
    @Schema(example = "100.50")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    @Schema(example = "CREDIT")
    private TransactionType type;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must be at most 500 characters")
    @Schema(example = "Salary payment")
    private String description;

    @Size(max = 255)
    @Schema(description = "Required when type is TRANSFER: recipient's registered email", example = "friend@example.com")
    private String counterpartyEmail;
}

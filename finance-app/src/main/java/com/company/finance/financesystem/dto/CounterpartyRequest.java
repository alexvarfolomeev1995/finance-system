package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.CounterpartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CounterpartyRequest(
        @NotBlank String name,
        String inn,
        @NotNull CounterpartyType type
) {}
package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        @NotBlank String name,
        @NotNull CategoryType type,
        Long parentId
) {}
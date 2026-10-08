package com.company.finance.financesystem.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
        @NotBlank String name,
        @NotBlank String code,
        Long responsibleUserId
) {}
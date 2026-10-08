package com.company.finance.financesystem.dto;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        Long responsibleUserId
) {}
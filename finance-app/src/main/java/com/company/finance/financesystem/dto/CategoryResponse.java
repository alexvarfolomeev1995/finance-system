package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.CategoryType;

public record CategoryResponse(
        Long id,
        String name,
        CategoryType type,
        Long parentId
) {}
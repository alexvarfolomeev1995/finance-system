package com.company.finance.financesystem.dto;

import java.math.BigDecimal;

public record CashFlowItem(
        Long categoryId,
        String categoryName,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {}
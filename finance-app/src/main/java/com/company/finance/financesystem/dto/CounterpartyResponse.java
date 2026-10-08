package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.CounterpartyType;

public record CounterpartyResponse(
        Long id,
        String name,
        String inn,
        CounterpartyType type
) {}
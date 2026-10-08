package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.AuditAction;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long userId,
        String userEmail,
        AuditAction action,
        String entityType,
        Long entityId,
        String details,
        Instant createdAt
) {}
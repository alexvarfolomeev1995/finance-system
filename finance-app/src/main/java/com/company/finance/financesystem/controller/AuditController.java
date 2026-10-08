package com.company.finance.financesystem.controller;

import com.company.finance.financesystem.domain.enums.AuditAction;
import com.company.finance.financesystem.dto.AuditLogResponse;
import com.company.finance.financesystem.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return auditService.findAll(pageable);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Page<AuditLogResponse> findByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            Pageable pageable
    ) {
        return auditService.findByEntity(entityType, entityId, pageable);
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Page<AuditLogResponse> findByAction(
            @PathVariable AuditAction action,
            Pageable pageable
    ) {
        return auditService.findByAction(action, pageable);
    }
}
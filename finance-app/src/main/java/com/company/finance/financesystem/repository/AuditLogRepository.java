package com.company.finance.financesystem.repository;

import com.company.finance.financesystem.domain.entity.AuditLog;
import com.company.finance.financesystem.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findAllByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findAllByAction(AuditAction action, Pageable pageable);
}
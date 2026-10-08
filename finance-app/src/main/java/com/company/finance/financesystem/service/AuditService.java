package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.AuditLog;
import com.company.finance.financesystem.domain.entity.User;
import com.company.finance.financesystem.domain.enums.AuditAction;
import com.company.finance.financesystem.dto.AuditLogResponse;
import com.company.finance.financesystem.repository.AuditLogRepository;
import com.company.finance.financesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(Long userId, AuditAction action, String entityType, Long entityId, String details) {
        User user = userId != null
                ? userRepository.findById(userId).orElse(null)
                : null;

        AuditLog entry = AuditLog.builder()
                .user(user)
                .userEmail(user != null ? user.getEmail() : "system")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();

        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findAllByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findAllByAction(action, pageable).map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getUser() != null ? a.getUser().getId() : null,
                a.getUserEmail(),
                a.getAction(),
                a.getEntityType(),
                a.getEntityId(),
                a.getDetails(),
                a.getCreatedAt()
        );
    }
}
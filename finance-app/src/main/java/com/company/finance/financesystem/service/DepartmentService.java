package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.Department;
import com.company.finance.financesystem.domain.entity.User;
import com.company.finance.financesystem.dto.DepartmentRequest;
import com.company.finance.financesystem.dto.DepartmentResponse;
import com.company.finance.financesystem.exception.BusinessException;
import com.company.finance.financesystem.exception.NotFoundException;
import com.company.finance.financesystem.repository.DepartmentRepository;
import com.company.finance.financesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DepartmentResponse findById(Long id) {
        return departmentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Department not found: " + id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest req) {
        if (departmentRepository.findByCode(req.code()).isPresent()) {
            throw new BusinessException("Department code already exists: " + req.code());
        }
        User responsible = null;
        if (req.responsibleUserId() != null) {
            responsible = userRepository.findById(req.responsibleUserId())
                    .orElseThrow(() -> new NotFoundException("User not found: " + req.responsibleUserId()));
        }
        Department d = Department.builder()
                .name(req.name())
                .code(req.code())
                .responsibleUser(responsible)
                .build();
        return toResponse(departmentRepository.save(d));
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(
                d.getId(), d.getName(), d.getCode(),
                d.getResponsibleUser() != null ? d.getResponsibleUser().getId() : null
        );
    }
}
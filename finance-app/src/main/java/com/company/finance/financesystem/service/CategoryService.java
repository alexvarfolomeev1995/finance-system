package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.Category;
import com.company.finance.financesystem.dto.CategoryRequest;
import com.company.finance.financesystem.dto.CategoryResponse;
import com.company.finance.financesystem.exception.NotFoundException;
import com.company.finance.financesystem.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        Category parent = null;
        if (req.parentId() != null) {
            parent = categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found: " + req.parentId()));
        }
        Category c = Category.builder()
                .name(req.name())
                .type(req.type())
                .parent(parent)
                .build();
        return toResponse(categoryRepository.save(c));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(), c.getName(), c.getType(),
                c.getParent() != null ? c.getParent().getId() : null
        );
    }
}
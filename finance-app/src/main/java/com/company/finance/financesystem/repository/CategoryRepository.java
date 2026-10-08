package com.company.finance.financesystem.repository;

import com.company.finance.financesystem.domain.entity.Category;
import com.company.finance.financesystem.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByType(CategoryType type);
    List<Category> findAllByParentId(Long parentId);
}
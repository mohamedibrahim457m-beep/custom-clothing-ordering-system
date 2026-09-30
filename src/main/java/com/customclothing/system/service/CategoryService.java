package com.customclothing.system.service;

import com.customclothing.system.dto.CategoryDTO;
import com.customclothing.system.entity.Category;

import java.util.List;

public interface CategoryService {

    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    CategoryDTO findById(Long id);

    List<CategoryDTO> findAllActive();

    void deleteCategory(Long id);

    Category getCategoryEntity(Long id);
}

package com.customclothing.system.service.impl;

import com.customclothing.system.dto.CategoryDTO;
import com.customclothing.system.entity.Category;
import com.customclothing.system.exception.BadRequestException;
import com.customclothing.system.exception.ResourceNotFoundException;
import com.customclothing.system.repository.CategoryRepository;
import com.customclothing.system.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO dto) {
        categoryRepository.findByNameIgnoreCase(dto.getName()).ifPresent(c -> {
            throw new BadRequestException("Category already exists with name: " + dto.getName());
        });

        Category category = new Category(dto.getName(), dto.getDescription(), dto.getImageUrl());
        Category saved = categoryRepository.save(category);
        return new CategoryDTO(saved);
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = getCategoryEntity(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setImageUrl(dto.getImageUrl());
        category.setActive(dto.isActive());
        return new CategoryDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        return new CategoryDTO(getCategoryEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> findAllActive() {
        return categoryRepository.findByActiveTrue().stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryEntity(id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}

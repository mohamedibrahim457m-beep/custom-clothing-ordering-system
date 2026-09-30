package com.customclothing.system.service.impl;

import com.customclothing.system.dto.ProductDTO;
import com.customclothing.system.entity.Category;
import com.customclothing.system.entity.Product;
import com.customclothing.system.exception.ResourceNotFoundException;
import com.customclothing.system.repository.ProductRepository;
import com.customclothing.system.service.CategoryService;
import com.customclothing.system.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Override
    public ProductDTO createProduct(ProductDTO dto) {
        Category category = categoryService.getCategoryEntity(dto.getCategoryId());

        Product product = new Product(
                dto.getName(),
                dto.getDescription(),
                dto.getBasePrice(),
                category,
                dto.getAvailableSizes(),
                dto.getAvailableColors(),
                dto.getAvailableFabrics(),
                dto.getImageUrl()
        );

        Product saved = productRepository.save(product);
        return new ProductDTO(saved);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = getProductEntity(id);

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryService.getCategoryEntity(dto.getCategoryId());
            product.setCategory(category);
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setAvailableSizes(dto.getAvailableSizes());
        product.setAvailableColors(dto.getAvailableColors());
        product.setAvailableFabrics(dto.getAvailableFabrics());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.isActive());

        return new ProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        return new ProductDTO(getProductEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAllActive() {
        return productRepository.findByActiveTrue().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getProductEntity(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }
}

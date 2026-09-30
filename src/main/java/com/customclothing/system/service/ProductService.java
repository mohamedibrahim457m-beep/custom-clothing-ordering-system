package com.customclothing.system.service;

import com.customclothing.system.dto.ProductDTO;
import com.customclothing.system.entity.Product;

import java.util.List;

public interface ProductService {

    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    ProductDTO findById(Long id);

    List<ProductDTO> findAllActive();

    List<ProductDTO> findAll();

    List<ProductDTO> findByCategoryId(Long categoryId);

    List<ProductDTO> searchProducts(String keyword);

    void deleteProduct(Long id);

    Product getProductEntity(Long id);
}

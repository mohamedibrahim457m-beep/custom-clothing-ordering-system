package com.customclothing.system.controller;

import com.customclothing.system.dto.ApiResponse;
import com.customclothing.system.dto.ProductDTO;
import com.customclothing.system.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.createProduct(productDTO);
        return new ResponseEntity<>(ApiResponse.success("Product created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable Long id,
                                                                 @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean all) {

        List<ProductDTO> products;
        if (all) {
            products = productService.findAll();
        } else if (categoryId != null) {
            products = productService.findByCategoryId(categoryId);
        } else if (search != null && !search.isBlank()) {
            products = productService.searchProducts(search.trim());
        } else {
            products = productService.findAllActive();
        }

        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<ProductDTO>> toggleProductStatus(@PathVariable Long id) {
        ProductDTO product = productService.findById(id);
        product.setActive(!product.isActive());
        ProductDTO updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(ApiResponse.success("Product status toggled successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated successfully"));
    }
}

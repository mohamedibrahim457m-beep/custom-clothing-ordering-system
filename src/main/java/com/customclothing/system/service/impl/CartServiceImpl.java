package com.customclothing.system.service.impl;

import com.customclothing.system.dto.AddToCartDTO;
import com.customclothing.system.dto.CartResponseDTO;
import com.customclothing.system.entity.*;
import com.customclothing.system.exception.BadRequestException;
import com.customclothing.system.exception.ResourceNotFoundException;
import com.customclothing.system.repository.CartItemRepository;
import com.customclothing.system.repository.CartRepository;
import com.customclothing.system.service.CartService;
import com.customclothing.system.service.MeasurementService;
import com.customclothing.system.service.ProductService;
import com.customclothing.system.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;
    private final MeasurementService measurementService;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           UserService userService,
                           ProductService productService,
                           MeasurementService measurementService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.productService = productService;
        this.measurementService = measurementService;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartByCustomerId(Long customerId) {
        Cart cart = getOrCreateCartEntity(customerId);
        return new CartResponseDTO(cart);
    }

    @Override
    public CartResponseDTO addToCart(Long customerId, AddToCartDTO dto) {
        Cart cart = getOrCreateCartEntity(customerId);
        Product product = productService.getProductEntity(dto.getProductId());

        if (!product.isActive()) {
            throw new BadRequestException("Product is not currently available: " + product.getName());
        }

        Measurement measurement = null;
        if (dto.getMeasurementId() != null) {
            measurement = measurementService.getMeasurementEntity(dto.getMeasurementId());
        }

        CartItem item = new CartItem(
                cart,
                product,
                dto.getQuantity(),
                product.getBasePrice(),
                dto.getSelectedSize(),
                dto.getSelectedColor(),
                dto.getSelectedFabric(),
                measurement,
                dto.getCustomizationInstructions()
        );

        cart.addItem(item);
        Cart savedCart = cartRepository.save(cart);
        return new CartResponseDTO(savedCart);
    }

    @Override
    public CartResponseDTO removeCartItem(Long customerId, Long cartItemId) {
        Cart cart = getOrCreateCartEntity(customerId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to customer's cart");
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);
        Cart updatedCart = cartRepository.save(cart);
        return new CartResponseDTO(updatedCart);
    }

    @Override
    public void clearCart(Long customerId) {
        Cart cart = getOrCreateCartEntity(customerId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public Cart getOrCreateCartEntity(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userService.getUserEntity(customerId);
                    Cart newCart = new Cart(customer);
                    return cartRepository.save(newCart);
                });
    }
}

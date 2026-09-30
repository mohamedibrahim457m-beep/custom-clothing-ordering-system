package com.customclothing.system.repository;

import com.customclothing.system.entity.Order;
import com.customclothing.system.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderTrackingNumber(String orderTrackingNumber);

    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    List<Order> findByAssignedTailorIdOrderByOrderDateDesc(Long tailorId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByAssignedTailorIdAndStatus(Long tailorId, OrderStatus status);
}

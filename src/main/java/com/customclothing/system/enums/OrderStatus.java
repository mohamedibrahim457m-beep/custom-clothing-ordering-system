package com.customclothing.system.enums;

/**
 * Status lifecycle of a custom clothing order.
 */
public enum OrderStatus {
    PENDING,            // Placed by customer, awaiting review
    CONFIRMED,          // Confirmed by Admin
    ASSIGNED,           // Assigned by Admin to a Tailor
    UNDER_STITCHING,    // Tailor started cutting and stitching the garment
    READY_FOR_DELIVERY, // Tailor completed stitching; ready for shipment
    DELIVERED,          // Order handed over to the customer
    CANCELLED           // Order was cancelled
}

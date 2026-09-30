package com.customclothing.system.enums;

/**
 * System roles for the Custom Clothing Ordering System.
 * - CUSTOMER: Browses clothes, submits measurements, customizes items, places orders, tracks status.
 * - ADMIN: Manages products, categories, customers, orders, and assigns orders to tailors.
 * - TAILOR: Views assigned orders, inspects customer measurements & customization notes, updates stitching status.
 */
public enum Role {
    CUSTOMER,
    ADMIN,
    TAILOR
}

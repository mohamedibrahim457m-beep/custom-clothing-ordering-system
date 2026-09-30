/**
 * Atelier Bespoke - REST API Client
 * Connects frontend directly to Spring Boot backend at http://localhost:8080/api
 */

const API_BASE = '/api';

class ApiClient {
  constructor() {
    this.baseUrl = API_BASE;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers
      }
    };

    if (config.body && typeof config.body === 'object') {
      config.body = JSON.stringify(config.body);
    }

    try {
      const response = await fetch(url, config);
      let data = null;

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        const text = await response.text();
        data = text ? { message: text } : null;
      }

      if (!response.ok) {
        let errorMsg = 'An unexpected error occurred';
        if (data) {
          if (data.message) {
            errorMsg = data.message;
          } else if (data.error) {
            errorMsg = data.error;
          }
          if (data.validationErrors) {
            const valList = Object.entries(data.validationErrors)
              .map(([field, msg]) => `${field}: ${msg}`)
              .join('; ');
            errorMsg += ` (${valList})`;
          }
        }
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error on [${options.method || 'GET'}] ${endpoint}:`, err);
      throw err;
    }
  }

  // ==========================================
  // Health & System
  // ==========================================
  async getHealth() {
    return this.request('/health');
  }

  // ==========================================
  // Authentication & Users
  // ==========================================
  async login(email, password) {
    return this.request('/users/login', {
      method: 'POST',
      body: { email, password }
    });
  }

  async register(userData) {
    return this.request('/users/register', {
      method: 'POST',
      body: userData
    });
  }

  async getUserById(id) {
    return this.request(`/users/${id}`);
  }

  // ==========================================
  // Products & Categories
  // ==========================================
  async getCategories() {
    return this.request('/categories');
  }

  async getProducts(categoryId = null, search = '') {
    const params = new URLSearchParams();
    if (categoryId) params.append('categoryId', categoryId);
    if (search && search.trim()) params.append('search', search.trim());
    
    const query = params.toString() ? `?${params.toString()}` : '';
    return this.request(`/products${query}`);
  }

  async getProductById(id) {
    return this.request(`/products/${id}`);
  }

  // ==========================================
  // Measurements
  // ==========================================
  async getCustomerMeasurements(customerId) {
    return this.request(`/measurements/customer/${customerId}`);
  }

  async saveMeasurement(measurementData) {
    return this.request('/measurements', {
      method: 'POST',
      body: measurementData
    });
  }

  async updateMeasurement(id, measurementData) {
    return this.request(`/measurements/${id}`, {
      method: 'PUT',
      body: measurementData
    });
  }

  async deleteMeasurement(id) {
    return this.request(`/measurements/${id}`, {
      method: 'DELETE'
    });
  }

  // ==========================================
  // Cart
  // ==========================================
  async getCart(customerId) {
    return this.request(`/cart/${customerId}`);
  }

  async addToCart(customerId, itemData) {
    return this.request(`/cart/${customerId}/items`, {
      method: 'POST',
      body: itemData
    });
  }

  async removeCartItem(customerId, itemId) {
    return this.request(`/cart/${customerId}/items/${itemId}`, {
      method: 'DELETE'
    });
  }

  async clearCart(customerId) {
    return this.request(`/cart/${customerId}`, {
      method: 'DELETE'
    });
  }

  // ==========================================
  // Orders
  // ==========================================
  async placeOrder(orderData) {
    return this.request('/orders', {
      method: 'POST',
      body: orderData
    });
  }

  async trackOrderByNumber(trackingNumber) {
    return this.request(`/orders/track/${encodeURIComponent(trackingNumber)}`);
  }

  async getCustomerOrders(customerId) {
    return this.request(`/orders/customer/${customerId}`);
  }

  async getOrderById(id) {
    return this.request(`/orders/${id}`);
  }

  async cancelOrder(id) {
    return this.request(`/orders/${id}/cancel`, {
      method: 'PUT'
    });
  }

  // ==========================================
  // Admin Endpoints
  // ==========================================
  async getAllProducts(includeInactive = false) {
    return this.request(`/products?all=${includeInactive}`);
  }

  async createProduct(productData) {
    return this.request('/products', {
      method: 'POST',
      body: productData
    });
  }

  async updateProduct(id, productData) {
    return this.request(`/products/${id}`, {
      method: 'PUT',
      body: productData
    });
  }

  async toggleProductStatus(id) {
    return this.request(`/products/${id}/toggle-status`, {
      method: 'PUT'
    });
  }

  async deleteProduct(id) {
    return this.request(`/products/${id}`, {
      method: 'DELETE'
    });
  }

  async getCustomers() {
    return this.request('/users/customers');
  }

  async getTailors() {
    return this.request('/users/tailors');
  }

  async getAllOrders(status) {
    const query = status && status !== 'ALL' ? `?status=${encodeURIComponent(status)}` : '';
    return this.request(`/orders${query}`);
  }

  async assignOrderToTailor(orderId, tailorId) {
    return this.request(`/orders/${orderId}/assign-tailor`, {
      method: 'PUT',
      body: { tailorId: Number(tailorId) }
    });
  }

  async updateOrderStatusByAdmin(orderId, status, tailorNotes = '') {
    return this.request(`/orders/${orderId}/status`, {
      method: 'PUT',
      body: { status, tailorNotes }
    });
  }

  // ==========================================
  // Tailor Endpoints
  // ==========================================
  async getTailorAssignedOrders(tailorId) {
    return this.request(`/orders/tailor/${tailorId}`);
  }

  async updateOrderStatusByTailor(orderId, tailorId, status, tailorNotes = '') {
    return this.request(`/orders/${orderId}/tailor/${tailorId}/status`, {
      method: 'PUT',
      body: { status, tailorNotes }
    });
  }
}

const api = new ApiClient();
window.api = api;

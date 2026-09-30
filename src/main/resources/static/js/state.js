/**
 * Atelier Bespoke - State Management & Utility Helpers
 */

class AppState {
  constructor() {
    this.currentUser = this.loadStoredUser();
    this.categories = [];
    this.products = [];
    this.selectedCategoryId = null;
    this.searchQuery = '';
    this.cart = { items: [], grandTotal: 0 };
    this.measurements = [];
    this.currentTrackedOrder = null;
    this.currentView = 'shop'; // 'shop' | 'admin' | 'tailor'
    this.adminActiveTab = 'dashboard'; // 'dashboard' | 'orders' | 'products' | 'customers'
    this.adminOrders = [];
    this.adminCustomers = [];
    this.adminTailors = [];
    this.tailorOrders = [];
    this.listeners = new Map();
  }

  // Session persistence
  loadStoredUser() {
    try {
      const stored = localStorage.getItem('atelier_current_user');
      return stored ? JSON.parse(stored) : null;
    } catch (e) {
      console.error('Failed to parse stored user:', e);
      return null;
    }
  }

  setCurrentUser(user) {
    this.currentUser = user;
    if (user) {
      localStorage.setItem('atelier_current_user', JSON.stringify(user));
      if (user.role === 'ADMIN') {
        this.currentView = 'admin';
      } else if (user.role === 'TAILOR') {
        this.currentView = 'tailor';
      } else {
        this.currentView = 'shop';
      }
    } else {
      localStorage.removeItem('atelier_current_user');
      this.cart = { items: [], grandTotal: 0 };
      this.measurements = [];
      this.currentView = 'shop';
    }
    this.emit('userChanged', this.currentUser);
  }

  get isLoggedIn() {
    return !!this.currentUser && !!this.currentUser.id;
  }

  get isAdmin() {
    return this.isLoggedIn && this.currentUser.role === 'ADMIN';
  }

  get isTailor() {
    return this.isLoggedIn && this.currentUser.role === 'TAILOR';
  }

  get isCustomer() {
    return this.isLoggedIn && this.currentUser.role === 'CUSTOMER';
  }

  // Simple Event Bus
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event).push(callback);
  }

  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(cb => {
        try {
          cb(data);
        } catch (e) {
          console.error(`Error in listener for ${event}:`, e);
        }
      });
    }
  }
}

// Global App State Instance
const appState = new AppState();
window.appState = appState;

// Utility UI Helpers
const utils = {
  formatCurrency(amount) {
    if (amount === undefined || amount === null || isNaN(amount)) return '₹0.00';
    try {
      return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }).format(amount);
    } catch (e) {
      return '₹' + Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
  },

  formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    } catch {
      return dateString;
    }
  },

  formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateString;
    }
  },

  showToast(title, message, type = 'info', duration = 4000) {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let iconSvg = '';
    if (type === 'success') {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>';
    } else if (type === 'error') {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>';
    } else {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>';
    }

    toast.innerHTML = `
      <div class="toast-icon">${iconSvg}</div>
      <div class="toast-content">
        <div class="toast-title">${title}</div>
        <div class="toast-message">${message}</div>
      </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
      toast.classList.add('removing');
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 300);
    }, duration);
  },

  getColorHex(colorName) {
    const map = {
      'midnight navy': '#1b263b',
      'navy': '#001f3f',
      'navy blue': '#0a192f',
      'charcoal grey': '#333f48',
      'royal blue': '#1e3a8a',
      'obsidian black': '#0f172a',
      'classic tuxedo black': '#0a0a0c',
      'crisp white': '#ffffff',
      'natural white': '#f8fafc',
      'ice blue': '#bae6fd',
      'french pink': '#fbcfe8',
      'lavender micro-stripe': '#e9d5ff',
      'sand beige': '#e5d0ba',
      'mediterranean azure': '#0284c7',
      'olive green': '#4d5b44',
      'warm camel': '#c19a6b',
      'camel tan': '#c89d66',
      'houndstooth grey': '#6b7280',
      'forest green': '#14532d',
      'deep burgundy': '#581c87',
      'ivory gold': '#fef08a',
      'royal emerald': '#065f46',
      'deep crimson': '#881337',
      'emerald green': '#047857',
      'midnight sapphire': '#1e1b4b',
      'ruby red': '#991b1b',
      'noir': '#111111'
    };

    const clean = (colorName || '').toLowerCase().trim();
    return map[clean] || '#64748b';
  },

  getStatusBadge(status) {
    const map = {
      'PENDING': { class: 'status-pending', label: 'Pending' },
      'CONFIRMED': { class: 'status-confirmed', label: 'Confirmed' },
      'ASSIGNED': { class: 'status-assigned', label: 'Assigned' },
      'UNDER_STITCHING': { class: 'status-stitching', label: 'Under Stitching' },
      'READY_FOR_DELIVERY': { class: 'status-ready', label: 'Ready for Delivery' },
      'DELIVERED': { class: 'status-delivered', label: 'Delivered' },
      'CANCELLED': { class: 'status-cancelled', label: 'Cancelled' }
    };
    const info = map[status] || { class: 'status-pending', label: (status || 'PENDING').replace(/_/g, ' ') };
    return `<span class="status-badge ${info.class}">${info.label}</span>`;
  }
};

window.utils = utils;

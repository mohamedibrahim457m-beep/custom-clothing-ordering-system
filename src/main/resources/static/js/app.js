/**
 * Atelier Bespoke - Main Application Controller
 * Handles UI interactions, API data fetching, modals, cart, checkout & tracking.
 */

document.addEventListener('DOMContentLoaded', () => {
  App.init();
});

const App = {
  activeCustomizingProduct: null,
  selectedSize: null,
  selectedColor: null,
  selectedFabric: null,
  selectedMeasurementId: null,
  customizerQuantity: 1,

  async init() {
    this.bindEvents();
    await this.checkHealth();
    await this.loadCategories();
    await this.loadProducts();

    if (appState.isLoggedIn) {
      if (appState.isAdmin) {
        this.switchView('admin');
      } else if (appState.isTailor) {
        this.switchView('tailor');
      } else {
        this.switchView('shop');
        await this.loadCart();
        await this.loadMeasurements();
        await this.loadUserOrders();
      }
    } else {
      this.switchView('shop');
    }

    this.updateUserNav();

    // Check if tracking number is passed in URL query param
    const urlParams = new URLSearchParams(window.location.search);
    const trackParam = urlParams.get('track');
    if (trackParam) {
      this.trackOrder(trackParam);
      document.getElementById('tracking-section')?.scrollIntoView({ behavior: 'smooth' });
    }
  },

  // ==========================================
  // Backend Health Check
  // ==========================================
  async checkHealth() {
    const pill = document.getElementById('api-status-pill');
    try {
      const res = await api.getHealth();
      if (res && res.success) {
        if (pill) {
          pill.innerHTML = '<span class="status-dot"></span><span>Spring API Online</span>';
          pill.title = res.message || 'Connected to Spring Boot REST backend';
        }
      }
    } catch {
      if (pill) {
        pill.innerHTML = '<span class="status-dot" style="background:#ef4444;box-shadow:0 0 8px #ef4444"></span><span>API Connecting...</span>';
      }
    }
  },

  // ==========================================
  // Event Bindings
  // ==========================================
  bindEvents() {
    // Auth modal triggers
    document.querySelectorAll('[data-action="open-auth"]').forEach(btn => {
      btn.addEventListener('click', () => this.openAuthModal());
    });

    document.getElementById('auth-modal-close')?.addEventListener('click', () => {
      this.closeModal('auth-modal');
    });

    // Auth tabs
    document.getElementById('tab-btn-login')?.addEventListener('click', () => this.switchAuthTab('login'));
    document.getElementById('tab-btn-register')?.addEventListener('click', () => this.switchAuthTab('register'));

    // Auth Forms
    document.getElementById('form-login')?.addEventListener('submit', (e) => this.handleLogin(e));
    document.getElementById('form-register')?.addEventListener('submit', (e) => this.handleRegister(e));
    document.getElementById('btn-demo-login')?.addEventListener('click', () => this.handleDemoLogin());
    document.getElementById('btn-logout')?.addEventListener('click', () => this.handleLogout());

    // Cart Drawer triggers
    document.getElementById('cart-trigger-btn')?.addEventListener('click', () => this.openCartDrawer());
    document.getElementById('cart-drawer-close')?.addEventListener('click', () => this.closeCartDrawer());
    document.getElementById('cart-drawer-overlay')?.addEventListener('click', (e) => {
      if (e.target.id === 'cart-drawer-overlay') this.closeCartDrawer();
    });
    document.getElementById('btn-clear-cart')?.addEventListener('click', () => this.handleClearCart());
    document.getElementById('btn-proceed-checkout')?.addEventListener('click', () => this.openCheckoutModal());

    // Search input
    const searchInput = document.getElementById('catalog-search-input');
    let debounceTimer;
    searchInput?.addEventListener('input', (e) => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        appState.searchQuery = e.target.value;
        this.loadProducts();
      }, 350);
    });

    // Customizer Modal
    document.getElementById('customizer-modal-close')?.addEventListener('click', () => {
      this.closeModal('customizer-modal');
    });
    document.getElementById('qty-minus')?.addEventListener('click', () => this.changeCustomizerQty(-1));
    document.getElementById('qty-plus')?.addEventListener('click', () => this.changeCustomizerQty(1));
    document.getElementById('btn-add-to-cart')?.addEventListener('click', () => this.handleAddToCart());

    // Custom Measurements quick-toggle inside customizer
    document.getElementById('measurement-profile-select')?.addEventListener('change', (e) => {
      const isNew = e.target.value === 'NEW';
      const accordion = document.getElementById('new-measurement-accordion');
      if (accordion) {
        accordion.style.display = isNew ? 'block' : 'none';
      }
      this.selectedMeasurementId = isNew ? null : (e.target.value ? Number(e.target.value) : null);
    });

    // Suggestion tags for customization notes
    document.querySelectorAll('.suggestion-tag').forEach(tag => {
      tag.addEventListener('click', () => {
        const textarea = document.getElementById('customizer-instructions');
        if (textarea) {
          const text = tag.textContent.trim();
          if (textarea.value.includes(text)) return;
          textarea.value = textarea.value ? `${textarea.value}; ${text}` : text;
        }
      });
    });

    // Checkout Modal
    document.getElementById('checkout-modal-close')?.addEventListener('click', () => {
      this.closeModal('checkout-modal');
    });
    document.getElementById('form-checkout')?.addEventListener('submit', (e) => this.handlePlaceOrder(e));

    // Payment method tabs in checkout
    document.querySelectorAll('.payment-method-card').forEach(card => {
      card.addEventListener('click', () => {
        document.querySelectorAll('.payment-method-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
        const method = card.dataset.method;
        const cardPreview = document.getElementById('card-preview-container');
        if (cardPreview) {
          cardPreview.style.display = method === 'card' ? 'block' : 'none';
        }
      });
    });

    // Tracking Form
    document.getElementById('tracking-form')?.addEventListener('submit', (e) => {
      e.preventDefault();
      const num = document.getElementById('tracking-number-input')?.value.trim();
      if (num) {
        this.trackOrder(num);
      } else {
        utils.showToast('Validation', 'Please enter a valid tracking number', 'info');
      }
    });

    // Quick demo order chips
    document.querySelectorAll('.quick-order-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        const trackingNum = chip.dataset.tracking;
        const input = document.getElementById('tracking-number-input');
        if (input) input.value = trackingNum;
        this.trackOrder(trackingNum);
      });
    });

    // Measurement modal triggers
    document.getElementById('btn-open-measurements')?.addEventListener('click', () => this.openMeasurementsModal());
    document.getElementById('btn-add-measurement-profile')?.addEventListener('click', () => this.openNewMeasurementModal());
    document.getElementById('measurement-modal-close')?.addEventListener('click', () => this.closeModal('measurements-modal'));
    document.getElementById('form-new-measurement')?.addEventListener('submit', (e) => this.handleSaveMeasurement(e));
    document.getElementById('new-measurement-modal-close')?.addEventListener('click', () => this.closeModal('new-measurement-modal'));
    document.getElementById('btn-measurement-guide')?.addEventListener('click', () => this.openModal('measurement-guide-modal'));
    document.getElementById('measurement-guide-close')?.addEventListener('click', () => this.closeModal('measurement-guide-modal'));

    // Generic modal overlay click to close
    document.querySelectorAll('.modal-overlay').forEach(modal => {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          this.closeModal(modal.id);
        }
      });
    });

    // Cancel order button
    document.getElementById('btn-cancel-tracked-order')?.addEventListener('click', () => this.handleCancelTrackedOrder());
  },

  // ==========================================
  // User Authentication
  // ==========================================
  openAuthModal(defaultTab = 'login') {
    this.switchAuthTab(defaultTab);
    this.openModal('auth-modal');
  },

  switchAuthTab(tab) {
    const isLogin = tab === 'login';
    document.getElementById('tab-btn-login')?.classList.toggle('active', isLogin);
    document.getElementById('tab-btn-register')?.classList.toggle('active', !isLogin);
    document.getElementById('login-tab-content').style.display = isLogin ? 'block' : 'none';
    document.getElementById('register-tab-content').style.display = !isLogin ? 'block' : 'none';
  },

  async handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Authenticating...';
    submitBtn.disabled = true;

    try {
      const res = await api.login(email, password);
      if (res && res.success) {
        appState.setCurrentUser(res.data);
        utils.showToast('Welcome Back', `Greetings, ${res.data.fullName} [${res.data.role}]`, 'success');
        this.closeModal('auth-modal');
        this.updateUserNav();

        if (res.data.role === 'ADMIN') {
          this.switchView('admin');
        } else if (res.data.role === 'TAILOR') {
          this.switchView('tailor');
        } else {
          this.switchView('shop');
          await this.loadCart();
          await this.loadMeasurements();
          await this.loadUserOrders();
        }
      }
    } catch (err) {
      utils.showToast('Login Failed', err.message || 'Invalid email or password', 'error');
    } finally {
      submitBtn.textContent = originalText;
      submitBtn.disabled = false;
    }
  },

  async handleRegister(e) {
    e.preventDefault();
    const fullName = document.getElementById('reg-name').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const phoneNumber = document.getElementById('reg-phone').value.trim();
    const address = document.getElementById('reg-address').value.trim();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Creating Account...';
    submitBtn.disabled = true;

    try {
      const res = await api.register({
        fullName,
        email,
        password,
        phoneNumber,
        address,
        role: 'CUSTOMER'
      });

      if (res && res.success) {
        appState.setCurrentUser(res.data);
        utils.showToast('Account Created', `Welcome to Atelier Bespoke, ${res.data.fullName}`, 'success');
        this.closeModal('auth-modal');
        this.updateUserNav();
        this.switchView('shop');
        await this.loadCart();
        await this.loadMeasurements();
      }
    } catch (err) {
      utils.showToast('Registration Error', err.message, 'error');
    } finally {
      submitBtn.textContent = originalText;
      submitBtn.disabled = false;
    }
  },

  async handleDemoLogin() {
    return this.loginAsDemo('customer');
  },

  async loginAsDemo(roleKey) {
    let email = 'customer@example.com';
    let password = 'customer123';
    let roleLabel = 'Customer (Patron)';

    if (roleKey === 'admin') {
      email = 'admin@customclothing.com';
      password = 'admin123';
      roleLabel = 'Atelier Administrator';
    } else if (roleKey === 'tailor-marcus') {
      email = 'tailor.marcus@customclothing.com';
      password = 'tailor123';
      roleLabel = 'Master Tailor Marcus (Bay 1)';
    } else if (roleKey === 'tailor-elena') {
      email = 'tailor.elena@customclothing.com';
      password = 'tailor123';
      roleLabel = 'Master Tailor Elena (Bay 2)';
    }

    try {
      const res = await api.login(email, password);
      if (res && res.success) {
        appState.setCurrentUser(res.data);
        utils.showToast('Role Authenticated', `Logged in as ${res.data.fullName} [${res.data.role}]`, 'success');
        this.closeModal('auth-modal');
        this.updateUserNav();

        if (res.data.role === 'ADMIN') {
          this.switchView('admin');
        } else if (res.data.role === 'TAILOR') {
          this.switchView('tailor');
        } else {
          this.switchView('shop');
          await this.loadCart();
          await this.loadMeasurements();
          await this.loadUserOrders();
        }
      }
    } catch (err) {
      // Fallback for customer demo seeding if necessary
      if (roleKey === 'customer') {
        try {
          const regRes = await api.register({
            fullName: 'David Sterling',
            email: 'customer@example.com',
            password: 'customer123',
            phoneNumber: '+91 98765 43210',
            address: '742 Evergreen Terrace, Mumbai, Maharashtra 400001',
            role: 'CUSTOMER'
          });
          if (regRes && regRes.success) {
            appState.setCurrentUser(regRes.data);
            utils.showToast('Demo Ready', 'Logged in as David Sterling', 'success');
            this.closeModal('auth-modal');
            this.updateUserNav();
            this.switchView('shop');
            await this.loadCart();
          }
        } catch (regErr) {
          utils.showToast('Demo Error', regErr.message, 'error');
        }
      } else {
        utils.showToast('Authentication Notice', err.message || `Unable to authenticate demo ${roleLabel}`, 'error');
      }
    }
  },

  handleLogout() {
    appState.setCurrentUser(null);
    this.switchView('shop');
    this.updateUserNav();
    this.renderCart();
    utils.showToast('Signed Out', 'You have been safely signed out', 'info');
  },

  handleUserMenuClick() {
    if (appState.isAdmin) {
      this.switchView(appState.currentView === 'admin' ? 'shop' : 'admin');
    } else if (appState.isTailor) {
      this.switchView(appState.currentView === 'tailor' ? 'shop' : 'tailor');
    } else {
      this.openMeasurementsModal();
    }
  },

  switchView(view) {
    appState.currentView = view;
    const storeView = document.getElementById('customer-store-view');
    const adminView = document.getElementById('admin-portal-view');
    const tailorView = document.getElementById('tailor-portal-view');
    const storeLink = document.getElementById('nav-store-link');
    const adminLink = document.getElementById('nav-admin-link');
    const tailorLink = document.getElementById('nav-tailor-link');
    const collectionsLink = document.getElementById('nav-collections-link');

    if (storeView) storeView.style.display = view === 'shop' ? 'block' : 'none';
    if (adminView) adminView.style.display = view === 'admin' ? 'block' : 'none';
    if (tailorView) tailorView.style.display = view === 'tailor' ? 'block' : 'none';

    if (storeLink) storeLink.style.display = view === 'shop' ? 'none' : 'inline-block';
    if (adminLink) adminLink.classList.toggle('active', view === 'admin');
    if (tailorLink) tailorLink.classList.toggle('active', view === 'tailor');
    if (collectionsLink) collectionsLink.classList.toggle('active', view === 'shop');

    this.updateUserNav();

    if (view === 'admin') {
      this.loadAdminData();
    } else if (view === 'tailor') {
      this.loadTailorData();
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
  },

  updateUserNav() {
    const authBtn = document.getElementById('nav-auth-btn');
    const userMenu = document.getElementById('nav-user-menu');
    const userNameLabel = document.getElementById('nav-user-name');
    const userRoleBadge = document.getElementById('nav-user-role-badge');
    const userAvatar = document.getElementById('nav-user-avatar');
    const measurementSectionBtn = document.getElementById('nav-measurements-link');
    const adminLink = document.getElementById('nav-admin-link');
    const tailorLink = document.getElementById('nav-tailor-link');
    const storeLink = document.getElementById('nav-store-link');

    if (appState.isLoggedIn) {
      if (authBtn) authBtn.style.display = 'none';
      if (userMenu) userMenu.style.display = 'flex';
      if (userNameLabel) userNameLabel.textContent = appState.currentUser.fullName.split(' ')[0];
      if (userRoleBadge) {
        userRoleBadge.textContent = appState.currentUser.role;
        userRoleBadge.style.color = appState.isAdmin ? '#fbbf24' : (appState.isTailor ? '#60a5fa' : 'var(--gold-light)');
      }
      if (userAvatar) {
        const initials = appState.currentUser.fullName
          .split(' ')
          .map(n => n[0])
          .join('')
          .toUpperCase()
          .slice(0, 2);
        userAvatar.textContent = initials;
      }
      if (measurementSectionBtn) measurementSectionBtn.style.display = appState.isCustomer ? 'inline-block' : 'none';
      if (adminLink) adminLink.style.display = appState.isAdmin ? 'inline-block' : 'none';
      if (tailorLink) tailorLink.style.display = appState.isTailor ? 'inline-block' : 'none';
      if (storeLink) storeLink.style.display = appState.currentView === 'shop' ? 'none' : 'inline-block';
    } else {
      if (authBtn) authBtn.style.display = 'flex';
      if (userMenu) userMenu.style.display = 'none';
      if (measurementSectionBtn) measurementSectionBtn.style.display = 'none';
      if (adminLink) adminLink.style.display = 'none';
      if (tailorLink) tailorLink.style.display = 'none';
      if (storeLink) storeLink.style.display = 'none';
      this.updateCartBadge(0);
    }
  },

  // ==========================================
  // Categories & Products
  // ==========================================
  async loadCategories() {
    const container = document.getElementById('category-tabs-container');
    if (!container) return;

    try {
      const res = await api.getCategories();
      if (res && res.success && res.data) {
        appState.categories = res.data;
        this.renderCategoryTabs(res.data);
      }
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  },

  renderCategoryTabs(categories) {
    const container = document.getElementById('category-tabs-container');
    if (!container) return;

    let html = `
      <button class="category-tab-btn ${appState.selectedCategoryId === null ? 'active' : ''}" data-cat-id="all">
        All Bespoke
      </button>
    `;

    categories.forEach(cat => {
      const isActive = appState.selectedCategoryId === cat.id;
      html += `
        <button class="category-tab-btn ${isActive ? 'active' : ''}" data-cat-id="${cat.id}">
          ${cat.name}
        </button>
      `;
    });

    container.innerHTML = html;

    container.querySelectorAll('.category-tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        container.querySelectorAll('.category-tab-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        const catId = btn.dataset.catId;
        appState.selectedCategoryId = catId === 'all' ? null : Number(catId);
        this.loadProducts();
      });
    });
  },

  async loadProducts() {
    const grid = document.getElementById('products-grid');
    if (!grid) return;

    grid.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 4rem 0;">
        <div class="empty-state-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
          </svg>
        </div>
        <p style="color: var(--gold-light); font-weight: 500;">Loading Atelier Collections...</p>
      </div>
    `;

    try {
      const res = await api.getProducts(appState.selectedCategoryId, appState.searchQuery);
      if (res && res.success && res.data) {
        appState.products = res.data;
        this.renderProducts(res.data);
      }
    } catch (err) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1;">
          <p style="color: var(--accent-danger);">Failed to retrieve products from server.</p>
          <p style="font-size: 0.85rem; color: var(--text-dim); margin-top: 0.5rem;">${err.message}</p>
        </div>
      `;
    }
  },

  renderProducts(products) {
    const grid = document.getElementById('products-grid');
    if (!grid) return;

    if (!products || products.length === 0) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1;">
          <div class="empty-state-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
          </div>
          <h3 style="margin-bottom: 0.5rem;">No Bespoke Garments Found</h3>
          <p style="color: var(--text-muted); font-size: 0.9rem;">Try selecting a different category or clearing search parameters.</p>
        </div>
      `;
      return;
    }

    grid.innerHTML = products.map(product => {
      const firstFabric = (product.availableFabrics || '').split(',')[0]?.trim() || 'Italian Fabric';
      const firstColor = (product.availableColors || '').split(',')[0]?.trim() || 'Custom Hue';

      return `
        <div class="product-card" data-product-id="${product.id}">
          <div class="product-image-container">
            <img src="${product.imageUrl}" alt="${product.name}" class="product-image" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80'"/>
            <span class="product-category-tag">${product.categoryName || 'Bespoke'}</span>
            <span class="product-badge-bespoke">Handmade</span>
          </div>

          <div class="product-content">
            <h3 class="product-name">${product.name}</h3>
            <p class="product-description">${product.description || ''}</p>

            <div class="product-specs-summary">
              <div class="product-spec-row">
                <span>Primary Cloth:</span>
                <span>${firstFabric}</span>
              </div>
              <div class="product-spec-row">
                <span>Featured Shade:</span>
                <span>${firstColor}</span>
              </div>
            </div>

            <div class="product-footer">
              <div class="product-price-box">
                <span class="price-label">Made-To-Order</span>
                <span class="price-amount">${utils.formatCurrency(product.basePrice)}</span>
              </div>
              <button class="btn-customize" data-action="customize" data-id="${product.id}">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 20h9"></path>
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
                </svg>
                <span>Customize</span>
              </button>
            </div>
          </div>
        </div>
      `;
    }).join('');

    // Bind Customize buttons
    grid.querySelectorAll('[data-action="customize"]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const id = Number(e.currentTarget.dataset.id);
        this.openCustomizer(id);
      });
    });
  },

  // ==========================================
  // Product Customizer Modal
  // ==========================================
  async openCustomizer(productId) {
    const product = appState.products.find(p => p.id === productId);
    if (!product) return;

    this.activeCustomizingProduct = product;
    this.customizerQuantity = 1;

    // Fill Product Info
    document.getElementById('customizer-modal-title').textContent = product.name;
    document.getElementById('customizer-img').src = product.imageUrl;
    document.getElementById('customizer-desc').textContent = product.description;
    document.getElementById('customizer-category').textContent = product.categoryName || 'Bespoke Atelier';
    document.getElementById('customizer-price').textContent = utils.formatCurrency(product.basePrice);
    document.getElementById('qty-value').textContent = '1';
    document.getElementById('customizer-instructions').value = '';

    // Render Sizes
    const sizes = (product.availableSizes || '38R, 40R, 42R, Custom Made-to-Measure')
      .split(',')
      .map(s => s.trim())
      .filter(Boolean);
    this.selectedSize = sizes[0] || 'Custom Made-to-Measure';
    this.renderSizeOptions(sizes);

    // Render Colors
    const colors = (product.availableColors || 'Midnight Navy, Charcoal Grey, Obsidian Black')
      .split(',')
      .map(c => c.trim())
      .filter(Boolean);
    this.selectedColor = colors[0] || 'Midnight Navy';
    this.renderColorOptions(colors);

    // Render Fabrics
    const fabrics = (product.availableFabrics || 'Super 130s Merino Wool, Cashmere Blend')
      .split(',')
      .map(f => f.trim())
      .filter(Boolean);
    this.selectedFabric = fabrics[0] || 'Super 130s Merino Wool';
    this.renderFabricOptions(fabrics);

    // Populate Measurement Profiles Dropdown
    await this.populateCustomizerMeasurements();

    this.updateCustomizerSubtotal();
    this.openModal('customizer-modal');
  },

  renderSizeOptions(sizes) {
    const container = document.getElementById('customizer-sizes-container');
    if (!container) return;

    container.innerHTML = sizes.map(size => `
      <div class="option-chip ${size === this.selectedSize ? 'selected' : ''}" data-size="${size}">
        ${size}
      </div>
    `).join('');

    container.querySelectorAll('.option-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        container.querySelectorAll('.option-chip').forEach(c => c.classList.remove('selected'));
        chip.classList.add('selected');
        this.selectedSize = chip.dataset.size;
      });
    });
  },

  renderColorOptions(colors) {
    const container = document.getElementById('customizer-colors-container');
    if (!container) return;

    container.innerHTML = colors.map(color => {
      const hex = utils.getColorHex(color);
      return `
        <div class="color-swatch-item ${color === this.selectedColor ? 'selected' : ''}" data-color="${color}">
          <span class="color-dot" style="background-color: ${hex};"></span>
          <span class="color-name">${color}</span>
        </div>
      `;
    }).join('');

    container.querySelectorAll('.color-swatch-item').forEach(item => {
      item.addEventListener('click', () => {
        container.querySelectorAll('.color-swatch-item').forEach(c => c.classList.remove('selected'));
        item.classList.add('selected');
        this.selectedColor = item.dataset.color;
      });
    });
  },

  renderFabricOptions(fabrics) {
    const container = document.getElementById('customizer-fabrics-container');
    if (!container) return;

    container.innerHTML = fabrics.map(fabric => `
      <div class="fabric-card-item ${fabric === this.selectedFabric ? 'selected' : ''}" data-fabric="${fabric}">
        <span class="fabric-card-name">${fabric}</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="20 6 9 17 4 12"></polyline>
        </svg>
      </div>
    `).join('');

    container.querySelectorAll('.fabric-card-item').forEach(item => {
      item.addEventListener('click', () => {
        container.querySelectorAll('.fabric-card-item').forEach(c => c.classList.remove('selected'));
        item.classList.add('selected');
        this.selectedFabric = item.dataset.fabric;
      });
    });
  },

  async populateCustomizerMeasurements() {
    const select = document.getElementById('measurement-profile-select');
    const accordion = document.getElementById('new-measurement-accordion');
    if (!select) return;

    if (accordion) accordion.style.display = 'none';

    let optionsHtml = '<option value="">-- Standard Sizing (No Custom Measurement) --</option>';

    if (appState.isLoggedIn) {
      if (appState.measurements.length === 0) {
        await this.loadMeasurements();
      }

      if (appState.measurements.length > 0) {
        optionsHtml += '<optgroup label="Your Saved Atelier Profiles">';
        appState.measurements.forEach(m => {
          optionsHtml += `<option value="${m.id}">${m.profileName} (${m.chest ? m.chest + ' ' + m.unit + ' chest' : 'Bespoke'})</option>`;
        });
        optionsHtml += '</optgroup>';
      }
    }

    optionsHtml += '<option value="NEW">+ Enter New Custom Measurements</option>';
    select.innerHTML = optionsHtml;

    // Default select first saved profile if present
    if (appState.measurements.length > 0) {
      select.value = appState.measurements[0].id;
      this.selectedMeasurementId = appState.measurements[0].id;
    } else {
      this.selectedMeasurementId = null;
    }
  },

  changeCustomizerQty(delta) {
    this.customizerQuantity = Math.max(1, this.customizerQuantity + delta);
    const qtyVal = document.getElementById('qty-value');
    if (qtyVal) qtyVal.textContent = this.customizerQuantity;
    this.updateCustomizerSubtotal();
  },

  updateCustomizerSubtotal() {
    if (!this.activeCustomizingProduct) return;
    const subtotal = this.activeCustomizingProduct.basePrice * this.customizerQuantity;
    const totalEl = document.getElementById('customizer-subtotal-price');
    if (totalEl) totalEl.textContent = utils.formatCurrency(subtotal);
  },

  async handleAddToCart() {
    if (!appState.isLoggedIn) {
      utils.showToast('Authentication Required', 'Please sign in or use Demo Login to add garments to your bespoke cart.', 'info');
      this.openAuthModal('login');
      return;
    }

    const select = document.getElementById('measurement-profile-select');
    let measurementId = this.selectedMeasurementId;

    // Check if user chose to enter new measurements
    if (select && select.value === 'NEW') {
      const profileName = document.getElementById('new-m-name')?.value.trim() || 'Custom Fit';
      const chest = parseFloat(document.getElementById('new-m-chest')?.value) || null;
      const waist = parseFloat(document.getElementById('new-m-waist')?.value) || null;
      const hips = parseFloat(document.getElementById('new-m-hips')?.value) || null;
      const shoulderWidth = parseFloat(document.getElementById('new-m-shoulder')?.value) || null;
      const sleeveLength = parseFloat(document.getElementById('new-m-sleeve')?.value) || null;
      const inseam = parseFloat(document.getElementById('new-m-inseam')?.value) || null;
      const neck = parseFloat(document.getElementById('new-m-neck')?.value) || null;
      const unit = document.getElementById('new-m-unit')?.value || 'inches';

      try {
        const savedMeasurement = await api.saveMeasurement({
          customerId: appState.currentUser.id,
          profileName,
          chest,
          waist,
          hips,
          shoulderWidth,
          sleeveLength,
          inseam,
          neck,
          unit,
          notes: 'Created during garment customization'
        });
        if (savedMeasurement && savedMeasurement.success && savedMeasurement.data) {
          measurementId = savedMeasurement.data.id;
          appState.measurements.push(savedMeasurement.data);
          utils.showToast('Fit Profile Saved', `Profile "${profileName}" saved to your atelier account.`, 'success');
        }
      } catch (e) {
        console.warn('Could not save inline measurement profile:', e);
      }
    }

    const customizationInstructions = document.getElementById('customizer-instructions')?.value.trim() || '';

    const addToCartPayload = {
      productId: this.activeCustomizingProduct.id,
      quantity: this.customizerQuantity,
      selectedSize: this.selectedSize,
      selectedColor: this.selectedColor,
      selectedFabric: this.selectedFabric,
      measurementId: measurementId || null,
      customizationInstructions
    };

    const addBtn = document.getElementById('btn-add-to-cart');
    const origText = addBtn.innerHTML;
    addBtn.innerHTML = '<span>Tailoring & Adding...</span>';
    addBtn.disabled = true;

    try {
      const res = await api.addToCart(appState.currentUser.id, addToCartPayload);
      if (res && res.success) {
        appState.cart = res.data;
        utils.showToast('Item Added', `${this.activeCustomizingProduct.name} tailored and added to cart.`, 'success');
        this.closeModal('customizer-modal');
        this.renderCart();
        this.openCartDrawer();
      }
    } catch (err) {
      utils.showToast('Cart Error', err.message, 'error');
    } finally {
      addBtn.innerHTML = origText;
      addBtn.disabled = false;
    }
  },

  // ==========================================
  // Shopping Cart & Drawer
  // ==========================================
  async loadCart() {
    if (!appState.isLoggedIn) return;

    try {
      const res = await api.getCart(appState.currentUser.id);
      if (res && res.success && res.data) {
        appState.cart = res.data;
        this.renderCart();
      }
    } catch (err) {
      console.error('Failed to load cart:', err);
    }
  },

  renderCart() {
    const itemsContainer = document.getElementById('cart-items-container');
    const badge = document.getElementById('cart-badge-count');
    const subtotalEl = document.getElementById('cart-subtotal-price');
    const grandTotalEl = document.getElementById('cart-grand-total-price');
    const checkoutBtn = document.getElementById('btn-proceed-checkout');
    const clearBtn = document.getElementById('btn-clear-cart');

    const items = (appState.cart && appState.cart.items) || [];
    const totalCount = items.reduce((sum, item) => sum + (item.quantity || 1), 0);
    this.updateCartBadge(totalCount);

    if (items.length === 0) {
      if (itemsContainer) {
        itemsContainer.innerHTML = `
          <div style="text-align: center; padding: 4rem 1rem;">
            <div class="empty-state-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="9" cy="21" r="1"></circle>
                <circle cx="20" cy="21" r="1"></circle>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
              </svg>
            </div>
            <h4 style="font-family: var(--font-heading); margin-bottom: 0.5rem;">Your Cart is Empty</h4>
            <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.5rem;">Discover our bespoke collection and customize your perfect attire.</p>
            <button class="btn-primary" onclick="App.closeCartDrawer(); document.getElementById('collections').scrollIntoView({behavior: 'smooth'})">
              Browse Collection
            </button>
          </div>
        `;
      }
      if (subtotalEl) subtotalEl.textContent = utils.formatCurrency(0);
      if (grandTotalEl) grandTotalEl.textContent = utils.formatCurrency(0);
      if (checkoutBtn) checkoutBtn.disabled = true;
      if (clearBtn) clearBtn.style.display = 'none';
      return;
    }

    if (checkoutBtn) checkoutBtn.disabled = false;
    if (clearBtn) clearBtn.style.display = 'inline-block';

    const grandTotal = appState.cart.grandTotal || 0;
    if (subtotalEl) subtotalEl.textContent = utils.formatCurrency(grandTotal);
    if (grandTotalEl) grandTotalEl.textContent = utils.formatCurrency(grandTotal);

    if (itemsContainer) {
      itemsContainer.innerHTML = items.map(item => {
        // Find matching product image if possible
        const prod = appState.products.find(p => p.id === item.productId);
        const imgUrl = prod ? prod.imageUrl : 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80';

        const specs = [
          item.selectedSize ? `Size: ${item.selectedSize}` : null,
          item.selectedColor ? `Color: ${item.selectedColor}` : null,
          item.selectedFabric ? `Fabric: ${item.selectedFabric}` : null
        ].filter(Boolean).join(' • ');

        return `
          <div class="cart-item-card" data-item-id="${item.id}">
            <img src="${imgUrl}" alt="${item.productName}" class="cart-item-img" onerror="this.src='https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80'"/>
            <div class="cart-item-info">
              <h4 class="cart-item-name">${item.productName}</h4>
              <p class="cart-item-tagline">${specs}</p>
              ${item.measurementProfileName ? `<span class="cart-item-spec-pill">Fit: ${item.measurementProfileName}</span>` : ''}
              ${item.customizationInstructions ? `<p style="font-size: 0.74rem; color: var(--gold-light); font-style: italic; margin-top: 2px;">Note: "${item.customizationInstructions}"</p>` : ''}
              <div class="cart-item-price-row">
                <span class="cart-item-price">${utils.formatCurrency(item.subtotal)} <small style="font-size:0.75rem; color:var(--text-dim); font-weight:normal;">(x${item.quantity})</small></span>
                <button class="cart-item-remove-btn" data-action="remove-item" data-id="${item.id}">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                  <span>Remove</span>
                </button>
              </div>
            </div>
          </div>
        `;
      }).join('');

      itemsContainer.querySelectorAll('[data-action="remove-item"]').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const itemId = Number(e.currentTarget.dataset.id);
          this.handleRemoveCartItem(itemId);
        });
      });
    }
  },

  updateCartBadge(count) {
    const badge = document.getElementById('cart-badge-count');
    if (badge) {
      badge.textContent = count;
      badge.style.display = count > 0 ? 'flex' : 'none';
    }
  },

  openCartDrawer() {
    document.getElementById('cart-drawer-overlay')?.classList.add('active');
    document.getElementById('cart-drawer')?.classList.add('active');
  },

  closeCartDrawer() {
    document.getElementById('cart-drawer-overlay')?.classList.remove('active');
    document.getElementById('cart-drawer')?.classList.remove('active');
  },

  async handleRemoveCartItem(itemId) {
    if (!appState.isLoggedIn) return;
    try {
      const res = await api.removeCartItem(appState.currentUser.id, itemId);
      if (res && res.success) {
        appState.cart = res.data;
        this.renderCart();
        utils.showToast('Item Removed', 'Garment removed from bespoke cart', 'info');
      }
    } catch (err) {
      utils.showToast('Error', err.message, 'error');
    }
  },

  async handleClearCart() {
    if (!appState.isLoggedIn) return;
    if (!confirm('Clear all items from your bespoke cart?')) return;

    try {
      await api.clearCart(appState.currentUser.id);
      appState.cart = { items: [], grandTotal: 0 };
      this.renderCart();
      utils.showToast('Cart Cleared', 'All garments removed from cart', 'info');
    } catch (err) {
      utils.showToast('Error', err.message, 'error');
    }
  },

  // ==========================================
  // Order Placement & Checkout
  // ==========================================
  openCheckoutModal() {
    if (!appState.isLoggedIn) {
      this.closeCartDrawer();
      this.openAuthModal('login');
      return;
    }

    const items = (appState.cart && appState.cart.items) || [];
    if (items.length === 0) {
      utils.showToast('Empty Cart', 'Your shopping cart has no items to order', 'info');
      return;
    }

    this.closeCartDrawer();

    // Populate checkout form fields
    const user = appState.currentUser;
    const nameInput = document.getElementById('checkout-customer-name');
    const addressInput = document.getElementById('checkout-address');
    const phoneInput = document.getElementById('checkout-phone');
    const totalEl = document.getElementById('checkout-total-price');

    if (nameInput) nameInput.value = user.fullName || '';
    if (addressInput) addressInput.value = user.address || '';
    if (phoneInput) phoneInput.value = user.phoneNumber || '';
    if (totalEl) totalEl.textContent = utils.formatCurrency(appState.cart.grandTotal);

    // Render items summary inside checkout
    const summaryContainer = document.getElementById('checkout-items-summary');
    if (summaryContainer) {
      summaryContainer.innerHTML = items.map(item => `
        <div style="display: flex; justify-content: space-between; font-size: 0.85rem; padding: 0.4rem 0; border-bottom: 1px dashed var(--border-light);">
          <span>${item.productName} <small style="color:var(--text-dim);">(x${item.quantity})</small></span>
          <span style="font-weight: 600; color: var(--gold-light);">${utils.formatCurrency(item.subtotal)}</span>
        </div>
      `).join('');
    }

    // Reset confirmation screen
    document.getElementById('checkout-form-container').style.display = 'block';
    document.getElementById('checkout-success-container').style.display = 'none';

    this.openModal('checkout-modal');
  },

  async handlePlaceOrder(e) {
    e.preventDefault();
    if (!appState.isLoggedIn) return;

    const shippingAddress = document.getElementById('checkout-address')?.value.trim();
    const contactPhone = document.getElementById('checkout-phone')?.value.trim();
    const customerNotes = document.getElementById('checkout-notes')?.value.trim();

    if (!shippingAddress || !contactPhone) {
      utils.showToast('Required Information', 'Please provide shipping address and contact telephone number.', 'error');
      return;
    }

    const submitBtn = document.getElementById('btn-confirm-order');
    const origText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<span>Bespoke Order Processing...</span>';
    submitBtn.disabled = true;

    try {
      const orderPayload = {
        customerId: appState.currentUser.id,
        shippingAddress,
        contactPhone,
        customerNotes
      };

      const res = await api.placeOrder(orderPayload);
      if (res && res.success && res.data) {
        const order = res.data;

        // Clear local cart
        appState.cart = { items: [], grandTotal: 0 };
        this.renderCart();

        // Switch to Order Success Screen
        document.getElementById('checkout-form-container').style.display = 'none';
        const successBox = document.getElementById('checkout-success-container');
        if (successBox) {
          successBox.style.display = 'block';
          document.getElementById('success-tracking-number').textContent = order.orderTrackingNumber;
          document.getElementById('success-order-amount').textContent = utils.formatCurrency(order.totalAmount);
          document.getElementById('success-delivery-date').textContent = utils.formatDate(order.expectedDeliveryDate);

          const trackBtn = document.getElementById('btn-track-new-order');
          if (trackBtn) {
            trackBtn.onclick = () => {
              this.closeModal('checkout-modal');
              this.trackOrder(order.orderTrackingNumber);
              document.getElementById('tracking-section')?.scrollIntoView({ behavior: 'smooth' });
            };
          }
        }

        utils.showToast('Order Placed!', `Bespoke order ${order.orderTrackingNumber} confirmed.`, 'success');
        await this.loadUserOrders();
      }
    } catch (err) {
      utils.showToast('Order Placement Failed', err.message, 'error');
    } finally {
      submitBtn.innerHTML = origText;
      submitBtn.disabled = false;
    }
  },

  // ==========================================
  // Order Tracking
  // ==========================================
  async trackOrder(trackingNumber) {
    if (!trackingNumber) return;

    const trackBtn = document.getElementById('btn-search-tracking');
    const input = document.getElementById('tracking-number-input');
    if (input) input.value = trackingNumber;

    if (trackBtn) {
      trackBtn.innerHTML = '<span class="status-dot"></span> Searching...';
      trackBtn.disabled = true;
    }

    try {
      const res = await api.trackOrderByNumber(trackingNumber);
      if (res && res.success && res.data) {
        appState.currentTrackedOrder = res.data;
        this.renderTrackedOrder(res.data);
      }
    } catch (err) {
      utils.showToast('Tracking Notice', err.message || 'Tracking number not found', 'error');
      const resultBox = document.getElementById('tracking-result-box');
      if (resultBox) resultBox.style.display = 'none';
    } finally {
      if (trackBtn) {
        trackBtn.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg><span>Track</span>';
        trackBtn.disabled = false;
      }
    }
  },

  renderTrackedOrder(order) {
    const resultBox = document.getElementById('tracking-result-box');
    if (!resultBox) return;

    resultBox.style.display = 'block';

    // Header info
    document.getElementById('tracked-tracking-num').textContent = order.orderTrackingNumber;
    const statusPill = document.getElementById('tracked-status-pill');
    if (statusPill) {
      statusPill.textContent = (order.status || 'PENDING').replace(/_/g, ' ');
      statusPill.className = `status-pill ${(order.status || 'pending').toLowerCase()}`;
    }

    // Progress Stepper Calculation
    const statuses = ['PENDING', 'CONFIRMED', 'ASSIGNED', 'UNDER_STITCHING', 'READY_FOR_DELIVERY', 'DELIVERED'];
    const currentStatus = order.status || 'PENDING';
    const isCancelled = currentStatus === 'CANCELLED';

    const currentIndex = statuses.indexOf(currentStatus);
    const progressPercent = isCancelled ? 0 : Math.round((Math.max(0, currentIndex) / (statuses.length - 1)) * 100);

    const progressFill = document.getElementById('stepper-progress-fill');
    if (progressFill) {
      progressFill.style.width = isCancelled ? '0%' : `${progressPercent}%`;
    }

    // Update each step node
    statuses.forEach((statusName, idx) => {
      const node = document.getElementById(`step-node-${statusName.toLowerCase()}`);
      if (node) {
        node.classList.remove('completed', 'active');
        if (!isCancelled) {
          if (idx < currentIndex) {
            node.classList.add('completed');
          } else if (idx === currentIndex) {
            node.classList.add('active');
          }
        }
      }
    });

    // Breakdown Meta Fields
    document.getElementById('meta-order-date').textContent = utils.formatDate(order.orderDate);
    document.getElementById('meta-delivery-date').textContent = utils.formatDate(order.expectedDeliveryDate);
    document.getElementById('meta-total-amount').textContent = utils.formatCurrency(order.totalAmount);
    document.getElementById('meta-customer-name').textContent = order.customerName || 'N/A';
    document.getElementById('meta-shipping-address').textContent = order.shippingAddress || 'N/A';
    document.getElementById('meta-contact-phone').textContent = order.contactPhone || 'N/A';

    // Assigned Tailor Box
    const tailorBox = document.getElementById('meta-assigned-tailor-box');
    const tailorNameEl = document.getElementById('meta-assigned-tailor-name');
    if (tailorBox && tailorNameEl) {
      if (order.assignedTailorName) {
        tailorNameEl.textContent = order.assignedTailorName;
        tailorBox.style.display = 'block';
      } else {
        tailorNameEl.textContent = 'Allocating Master Tailor...';
        tailorBox.style.display = 'block';
      }
    }

    // Tailor Notes Callout
    const notesCallout = document.getElementById('tailor-notes-callout');
    const notesText = document.getElementById('tailor-notes-text');
    if (notesCallout && notesText) {
      if (order.tailorNotes) {
        notesText.textContent = order.tailorNotes;
        notesCallout.style.display = 'block';
      } else {
        notesCallout.style.display = 'none';
      }
    }

    // Items list
    const itemsContainer = document.getElementById('tracked-items-container');
    if (itemsContainer) {
      itemsContainer.innerHTML = (order.items || []).map(item => `
        <div class="tracked-item-card">
          <div class="tracked-item-info">
            <span class="tracked-item-title">${item.productName}</span>
            <span class="tracked-item-sub">
              ${[
                item.selectedSize ? `Size: ${item.selectedSize}` : null,
                item.selectedColor ? `Color: ${item.selectedColor}` : null,
                item.selectedFabric ? `Fabric: ${item.selectedFabric}` : null
              ].filter(Boolean).join(' • ')}
            </span>
            ${item.measurement ? `<span style="font-size:0.75rem; color:var(--gold-light);">Fit Profile: ${item.measurement.profileName} (Chest: ${item.measurement.chest || 'N/A'} ${item.measurement.unit})</span>` : ''}
            ${item.customizationInstructions ? `<span style="font-size:0.75rem; color:var(--text-muted); font-style: italic;">Instructions: "${item.customizationInstructions}"</span>` : ''}
          </div>
          <div class="tracked-item-price">${utils.formatCurrency(item.subtotal)} <small style="font-size:0.75rem; font-weight:normal; color:var(--text-dim);">(x${item.quantity})</small></div>
        </div>
      `).join('');
    }

    // Cancel order button visibility
    const cancelBtn = document.getElementById('btn-cancel-tracked-order');
    if (cancelBtn) {
      const canCancel = order.status !== 'DELIVERED' && order.status !== 'CANCELLED';
      cancelBtn.style.display = canCancel ? 'inline-flex' : 'none';
    }
  },

  async handleCancelTrackedOrder() {
    if (!appState.currentTrackedOrder) return;
    if (!confirm('Are you sure you want to cancel this bespoke order?')) return;

    try {
      const res = await api.cancelOrder(appState.currentTrackedOrder.id);
      if (res && res.success && res.data) {
        utils.showToast('Order Cancelled', 'Your order was successfully cancelled.', 'info');
        this.renderTrackedOrder(res.data);
      }
    } catch (err) {
      utils.showToast('Cannot Cancel Order', err.message, 'error');
    }
  },

  async loadUserOrders() {
    if (!appState.isLoggedIn) return;

    const listContainer = document.getElementById('user-recent-orders-list');
    if (!listContainer) return;

    try {
      const res = await api.getCustomerOrders(appState.currentUser.id);
      if (res && res.success && res.data) {
        const orders = res.data;
        if (orders.length > 0) {
          listContainer.innerHTML = `
            <div style="margin-top: 1.5rem; padding-top: 1.5rem; border-top: 1px solid var(--border-light);">
              <h4 style="font-family: var(--font-heading); font-size: 1.05rem; margin-bottom: 0.75rem; color: var(--gold-light);">Your Order History</h4>
              <div style="display: flex; gap: 0.65rem; flex-wrap: wrap;">
                ${orders.map(o => `
                  <button class="quick-order-chip" onclick="App.trackOrder('${o.orderTrackingNumber}')">
                    ${o.orderTrackingNumber} (${utils.formatCurrency(o.totalAmount)})
                  </button>
                `).join('')}
              </div>
            </div>
          `;
        }
      }
    } catch (err) {
      console.warn('Could not load customer orders:', err);
    }
  },

  // ==========================================
  // Customer Measurements Management
  // ==========================================
  async loadMeasurements() {
    if (!appState.isLoggedIn) return;

    try {
      const res = await api.getCustomerMeasurements(appState.currentUser.id);
      if (res && res.success && res.data) {
        appState.measurements = res.data;
        this.renderMeasurementsList();
      }
    } catch (err) {
      console.error('Failed to load measurements:', err);
    }
  },

  openMeasurementsModal() {
    if (!appState.isLoggedIn) {
      this.openAuthModal('login');
      return;
    }
    this.renderMeasurementsList();
    this.openModal('measurements-modal');
  },

  renderMeasurementsList() {
    const container = document.getElementById('measurements-cards-container');
    if (!container) return;

    const list = appState.measurements || [];
    if (list.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1;">
          <div class="empty-state-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            </svg>
          </div>
          <h4 style="margin-bottom: 0.5rem;">No Fit Profiles Saved Yet</h4>
          <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.5rem;">Create a personalized profile with your chest, waist, and sleeve measurements for tailor-grade precision.</p>
          <button class="btn-primary" onclick="App.openNewMeasurementModal()">Create First Fit Profile</button>
        </div>
      `;
      return;
    }

    container.innerHTML = list.map(m => `
      <div class="measurement-profile-card">
        <div class="profile-card-header">
          <h4 class="profile-card-title">${m.profileName}</h4>
          <span class="profile-unit-tag">${m.unit || 'inches'}</span>
        </div>

        <div class="measurements-spec-grid">
          <div class="spec-badge">
            <span>Chest:</span>
            <span>${m.chest ? m.chest + ' ' + m.unit : 'N/A'}</span>
          </div>
          <div class="spec-badge">
            <span>Waist:</span>
            <span>${m.waist ? m.waist + ' ' + m.unit : 'N/A'}</span>
          </div>
          <div class="spec-badge">
            <span>Hips:</span>
            <span>${m.hips ? m.hips + ' ' + m.unit : 'N/A'}</span>
          </div>
          <div class="spec-badge">
            <span>Shoulder:</span>
            <span>${m.shoulderWidth ? m.shoulderWidth + ' ' + m.unit : 'N/A'}</span>
          </div>
          <div class="spec-badge">
            <span>Sleeve:</span>
            <span>${m.sleeveLength ? m.sleeveLength + ' ' + m.unit : 'N/A'}</span>
          </div>
          <div class="spec-badge">
            <span>Inseam:</span>
            <span>${m.inseam ? m.inseam + ' ' + m.unit : 'N/A'}</span>
          </div>
        </div>

        ${m.notes ? `<p class="profile-notes">"${m.notes}"</p>` : ''}

        <div class="profile-card-actions">
          <button class="btn-secondary" style="padding: 0.4rem 0.8rem; font-size: 0.8rem; color: var(--accent-danger);" onclick="App.handleDeleteMeasurement(${m.id})">
            Delete
          </button>
        </div>
      </div>
    `).join('');
  },

  openNewMeasurementModal() {
    if (!appState.isLoggedIn) {
      this.openAuthModal('login');
      return;
    }
    document.getElementById('form-new-measurement')?.reset();
    this.openModal('new-measurement-modal');
  },

  async handleSaveMeasurement(e) {
    e.preventDefault();
    if (!appState.isLoggedIn) return;

    const profileName = document.getElementById('modal-m-name').value.trim();
    const chest = parseFloat(document.getElementById('modal-m-chest').value) || null;
    const waist = parseFloat(document.getElementById('modal-m-waist').value) || null;
    const hips = parseFloat(document.getElementById('modal-m-hips').value) || null;
    const shoulderWidth = parseFloat(document.getElementById('modal-m-shoulder').value) || null;
    const sleeveLength = parseFloat(document.getElementById('modal-m-sleeve').value) || null;
    const shirtLength = parseFloat(document.getElementById('modal-m-shirt').value) || null;
    const inseam = parseFloat(document.getElementById('modal-m-inseam').value) || null;
    const neck = parseFloat(document.getElementById('modal-m-neck').value) || null;
    const unit = document.getElementById('modal-m-unit').value || 'inches';
    const notes = document.getElementById('modal-m-notes').value.trim();

    try {
      const res = await api.saveMeasurement({
        customerId: appState.currentUser.id,
        profileName,
        chest,
        waist,
        hips,
        shoulderWidth,
        sleeveLength,
        shirtLength,
        inseam,
        neck,
        unit,
        notes
      });

      if (res && res.success && res.data) {
        appState.measurements.push(res.data);
        utils.showToast('Fit Profile Created', `Profile "${profileName}" saved successfully.`, 'success');
        this.closeModal('new-measurement-modal');
        this.renderMeasurementsList();
      }
    } catch (err) {
      utils.showToast('Save Error', err.message, 'error');
    }
  },

  async handleDeleteMeasurement(id) {
    if (!confirm('Are you sure you want to remove this measurement profile?')) return;

    try {
      await api.deleteMeasurement(id);
      appState.measurements = appState.measurements.filter(m => m.id !== id);
      this.renderMeasurementsList();
      utils.showToast('Profile Deleted', 'Fit profile removed from account', 'info');
    } catch (err) {
      utils.showToast('Delete Error', err.message, 'error');
    }
  },

  // ==========================================
  // Admin Management Module
  // ==========================================
  switchAdminTab(tab) {
    appState.adminActiveTab = tab;
    ['dashboard', 'orders', 'products', 'customers'].forEach(t => {
      const btn = document.getElementById(`btn-admin-tab-${t}`);
      const content = document.getElementById(`admin-tab-content-${t}`);
      if (btn) btn.classList.toggle('active', t === tab);
      if (content) content.style.display = t === tab ? 'block' : 'none';
    });
  },

  async loadAdminData() {
    try {
      const [ordersRes, productsRes, customersRes, tailorsRes] = await Promise.all([
        api.getAllOrders(),
        api.getAllProducts(true),
        api.getCustomers(),
        api.getTailors()
      ]);

      if (ordersRes && ordersRes.success) appState.adminOrders = ordersRes.data || [];
      if (productsRes && productsRes.success) appState.adminProducts = productsRes.data || [];
      if (customersRes && customersRes.success) appState.adminCustomers = customersRes.data || [];
      if (tailorsRes && tailorsRes.success) appState.adminTailors = tailorsRes.data || [];

      // Update count badges
      const ordersBadge = document.getElementById('admin-orders-count-badge');
      if (ordersBadge) ordersBadge.textContent = appState.adminOrders.length;
      const productsBadge = document.getElementById('admin-products-count-badge');
      if (productsBadge) productsBadge.textContent = appState.adminProducts.length;
      const customersBadge = document.getElementById('admin-customers-count-badge');
      if (customersBadge) customersBadge.textContent = appState.adminCustomers.length;

      this.renderAdminDashboard();
      this.renderAdminOrders();
      this.renderAdminProducts();
      this.renderAdminCustomers();
    } catch (err) {
      console.error('Failed to load admin data:', err);
      utils.showToast('Admin Portal', err.message || 'Error loading administration data', 'error');
    }
  },

  renderAdminDashboard() {
    const orders = appState.adminOrders || [];
    const products = appState.adminProducts || [];
    const customers = appState.adminCustomers || [];
    const tailors = appState.adminTailors || [];

    // Compute key metrics
    const validOrders = orders.filter(o => o.status !== 'CANCELLED');
    const totalRevenue = validOrders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);
    const pendingOrders = orders.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED' || !o.assignedTailorName).length;
    const stitchingOrders = orders.filter(o => o.status === 'UNDER_STITCHING' || o.status === 'ASSIGNED').length;
    const activeProducts = products.filter(p => p.active !== false).length;

    // DOM Updates
    const revEl = document.getElementById('metric-total-revenue');
    if (revEl) revEl.textContent = utils.formatCurrency(totalRevenue);
    const ordEl = document.getElementById('metric-total-orders');
    if (ordEl) ordEl.textContent = orders.length;
    const pendEl = document.getElementById('metric-pending-orders');
    if (pendEl) pendEl.textContent = pendingOrders;
    const stitchEl = document.getElementById('metric-stitching-orders');
    if (stitchEl) stitchEl.textContent = stitchingOrders;
    const prodEl = document.getElementById('metric-total-products');
    if (prodEl) prodEl.textContent = `${activeProducts} / ${products.length}`;
    const custEl = document.getElementById('metric-total-customers');
    if (custEl) custEl.textContent = customers.length;

    // Tailor workload grid
    const workloadGrid = document.getElementById('admin-tailor-workload-grid');
    if (workloadGrid) {
      if (tailors.length === 0) {
        workloadGrid.innerHTML = '<p style="color:var(--text-muted); font-size:0.88rem;">No tailors registered in atelier.</p>';
      } else {
        workloadGrid.innerHTML = tailors.map(t => {
          const assigned = orders.filter(o => o.assignedTailorId === t.id);
          const activeBay = assigned.filter(o => o.status === 'ASSIGNED' || o.status === 'UNDER_STITCHING').length;
          const readyCount = assigned.filter(o => o.status === 'READY_FOR_DELIVERY').length;
          const deliveredCount = assigned.filter(o => o.status === 'DELIVERED').length;

          return `
            <div style="background: var(--bg-input); border: 1px solid var(--border-light); border-radius: var(--radius-md); padding: 1.25rem;">
              <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
                <div>
                  <h4 style="font-family: var(--font-heading); color: var(--gold-light); font-size: 1.05rem;">${t.fullName}</h4>
                  <span style="font-size: 0.76rem; color: var(--text-dim);">${t.email} &bull; ${t.phoneNumber || 'N/A'}</span>
                </div>
                <span class="status-badge status-confirmed">Artisan</span>
              </div>
              <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.5rem; margin-top: 1rem; text-align: center;">
                <div style="background: rgba(255,255,255,0.02); padding: 0.5rem; border-radius: var(--radius-sm); border: 1px solid var(--border-light);">
                  <div style="font-size: 0.68rem; color: var(--text-dim); text-transform: uppercase;">In Bay</div>
                  <div style="font-family: var(--font-heading); font-size: 1.15rem; color: #fb923c; font-weight: 700;">${activeBay}</div>
                </div>
                <div style="background: rgba(255,255,255,0.02); padding: 0.5rem; border-radius: var(--radius-sm); border: 1px solid var(--border-light);">
                  <div style="font-size: 0.68rem; color: var(--text-dim); text-transform: uppercase;">Ready</div>
                  <div style="font-family: var(--font-heading); font-size: 1.15rem; color: #38bdf8; font-weight: 700;">${readyCount}</div>
                </div>
                <div style="background: rgba(255,255,255,0.02); padding: 0.5rem; border-radius: var(--radius-sm); border: 1px solid var(--border-light);">
                  <div style="font-size: 0.68rem; color: var(--text-dim); text-transform: uppercase;">Delivered</div>
                  <div style="font-family: var(--font-heading); font-size: 1.15rem; color: #34d399; font-weight: 700;">${deliveredCount}</div>
                </div>
              </div>
            </div>
          `;
        }).join('');
      }
    }

    // Recent orders preview
    const previewContainer = document.getElementById('admin-recent-orders-preview');
    if (previewContainer) {
      const recent = [...orders].sort((a, b) => new Date(b.orderDate || 0) - new Date(a.orderDate || 0)).slice(0, 5);
      if (recent.length === 0) {
        previewContainer.innerHTML = '<p style="color:var(--text-muted); font-size:0.88rem;">No production orders recorded yet.</p>';
      } else {
        previewContainer.innerHTML = `
          <div class="portal-table-container" style="margin-bottom: 0;">
            <table class="portal-table">
              <thead>
                <tr>
                  <th>Tracking #</th>
                  <th>Patron</th>
                  <th>Order Date</th>
                  <th>Total Amount</th>
                  <th>Assigned Tailor</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                ${recent.map(o => `
                  <tr>
                    <td><strong style="color:var(--gold-light); font-family:var(--font-heading);">${o.orderTrackingNumber}</strong></td>
                    <td>${o.customerName || 'N/A'}</td>
                    <td>${utils.formatDate(o.orderDate)}</td>
                    <td><strong style="color:var(--gold-light);">${utils.formatCurrency(o.totalAmount)}</strong></td>
                    <td>${o.assignedTailorName ? `✂️ ${o.assignedTailorName}` : '<span style="color:#fbbf24; font-size:0.8rem;">Unassigned</span>'}</td>
                    <td>${utils.getStatusBadge(o.status)}</td>
                    <td>
                      <button class="btn-secondary btn-sm" onclick="App.switchAdminTab('orders'); App.searchAdminOrders('${o.orderTrackingNumber}');">
                        Manage
                      </button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        `;
      }
    }
  },

  renderAdminOrders(filteredList) {
    const container = document.getElementById('admin-orders-list-container');
    if (!container) return;

    const orders = filteredList !== undefined ? filteredList : (appState.adminOrders || []);
    const tailors = appState.adminTailors || [];

    if (orders.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="padding: 3rem 1rem;">
          <div class="empty-state-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" y1="6" x2="21" y2="6"></line>
            </svg>
          </div>
          <h4 style="margin-bottom: 0.5rem;">No Production Orders Found</h4>
          <p style="font-size: 0.88rem; color: var(--text-muted);">Adjust search query or filter selection to view orders.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = orders.map(order => {
      const items = order.items || [];
      const tailorOptions = tailors.map(t => `
        <option value="${t.id}" ${order.assignedTailorId === t.id ? 'selected' : ''}>
          ${t.fullName} (${t.email})
        </option>
      `).join('');

      const statuses = ['PENDING', 'CONFIRMED', 'ASSIGNED', 'UNDER_STITCHING', 'READY_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];
      const statusOptions = statuses.map(s => `
        <option value="${s}" ${order.status === s ? 'selected' : ''}>${s.replace(/_/g, ' ')}</option>
      `).join('');

      return `
        <div class="order-card" data-order-id="${order.id}">
          <!-- Order Card Header -->
          <div class="order-card-header">
            <div style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap;">
              <span class="order-card-tracking">${order.orderTrackingNumber}</span>
              ${utils.getStatusBadge(order.status)}
              <span style="font-size: 0.82rem; color: var(--text-dim);">Placed: ${utils.formatDateTime(order.orderDate)}</span>
              <span style="font-size: 0.82rem; color: var(--gold-light);">Target: ${utils.formatDate(order.expectedDeliveryDate)}</span>
            </div>
            <div style="font-family: var(--font-heading); font-size: 1.35rem; font-weight: 700; color: var(--gold-light);">
              ${utils.formatCurrency(order.totalAmount)}
            </div>
          </div>

          <!-- Customer Meta Row -->
          <div class="order-card-meta" style="margin-bottom: 1rem; padding: 0.75rem 1rem; background: rgba(255,255,255,0.015); border-radius: var(--radius-sm); border: 1px solid var(--border-light);">
            <div><strong>Patron:</strong> ${order.customerName || 'N/A'} (${order.customerEmail || 'N/A'})</div>
            <div><strong>Telephone:</strong> ${order.contactPhone || 'N/A'}</div>
            <div><strong>Delivery Address:</strong> ${order.shippingAddress || 'N/A'}</div>
            ${order.customerNotes ? `<div style="width: 100%; color: var(--gold-light); font-style: italic;">💬 Patron Note: "${order.customerNotes}"</div>` : ''}
            ${order.tailorNotes ? `<div style="width: 100%; color: #93c5fd; font-style: italic;">✂️ Workshop Log: "${order.tailorNotes}"</div>` : ''}
          </div>

          <!-- Tailored Garments Breakdown -->
          <div style="margin-bottom: 1rem;">
            ${items.map(item => {
              const prod = (appState.adminProducts || []).find(p => p.id === item.productId);
              const imgUrl = prod ? prod.imageUrl : 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80';
              const m = item.measurement;

              return `
                <div class="order-garment-item">
                  <img src="${imgUrl}" alt="${item.productName}" class="garment-thumb" onerror="this.src='https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80'"/>
                  <div class="garment-details">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                      <h4 class="garment-title">${item.productName} <small style="color:var(--text-dim); font-weight:normal;">(x${item.quantity})</small></h4>
                      <span style="font-weight: 700; color: var(--gold-light); font-family: var(--font-heading);">${utils.formatCurrency(item.subtotal)}</span>
                    </div>

                    <div class="garment-tags">
                      ${item.selectedSize ? `<span class="garment-tag-chip">Size: ${item.selectedSize}</span>` : ''}
                      ${item.selectedColor ? `<span class="garment-tag-chip">Color: ${item.selectedColor}</span>` : ''}
                      ${item.selectedFabric ? `<span class="garment-tag-chip">Cloth: ${item.selectedFabric}</span>` : ''}
                    </div>

                    ${m ? `
                      <div class="measurement-callout-pill">
                        <strong>Fit Profile: ${m.profileName}</strong> &bull; Chest: ${m.chest ? m.chest + ' ' + m.unit : 'N/A'} &bull; Waist: ${m.waist ? m.waist + ' ' + m.unit : 'N/A'} &bull; Sleeve: ${m.sleeveLength ? m.sleeveLength + ' ' + m.unit : 'N/A'}
                        ${m.notes ? ` <span style="font-style:italic;">("${m.notes}")</span>` : ''}
                      </div>
                    ` : ''}

                    ${item.customizationInstructions ? `
                      <div class="instructions-callout-pill">
                        Customization: "${item.customizationInstructions}"
                      </div>
                    ` : ''}
                  </div>
                </div>
              `;
            }).join('')}
          </div>

          <!-- Actions & Assignment Bar -->
          <div class="order-card-actions">
            <!-- Tailor Assignment Group -->
            <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
              <span style="font-size: 0.76rem; font-weight: 700; color: var(--gold-light); text-transform: uppercase;">Artisan Tailor:</span>
              <select id="admin-assign-tailor-${order.id}" class="portal-select" style="min-width: 190px;">
                <option value="">-- Choose Tailor Bay --</option>
                ${tailorOptions}
              </select>
              <button class="btn-primary btn-sm" onclick="App.handleAssignTailor(${order.id}, 'admin-assign-tailor-${order.id}')">
                Assign Bay
              </button>
            </div>

            <!-- Order Status Management Group -->
            <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
              <span style="font-size: 0.76rem; font-weight: 700; color: var(--gold-light); text-transform: uppercase;">Lifecycle Status:</span>
              <select id="admin-status-${order.id}" class="portal-select">
                ${statusOptions}
              </select>
              <input type="text" id="admin-notes-${order.id}" class="portal-input" placeholder="Optional status note..." value="${order.tailorNotes || ''}" style="max-width: 180px;">
              <button class="btn-secondary btn-sm" onclick="App.handleAdminStatusUpdate(${order.id}, 'admin-status-${order.id}', 'admin-notes-${order.id}')">
                Update Status
              </button>
            </div>
          </div>
        </div>
      `;
    }).join('');
  },

  filterAdminOrders(status) {
    const filterChips = document.querySelectorAll('#admin-order-status-filters .filter-chip');
    filterChips.forEach(chip => {
      chip.classList.toggle('active', chip.dataset.status === status);
    });

    if (status === 'ALL') {
      this.renderAdminOrders(appState.adminOrders);
    } else {
      const filtered = (appState.adminOrders || []).filter(o => o.status === status);
      this.renderAdminOrders(filtered);
    }
  },

  searchAdminOrders(query) {
    const q = (query || '').toLowerCase().trim();
    if (!q) {
      this.renderAdminOrders(appState.adminOrders);
      return;
    }
    const filtered = (appState.adminOrders || []).filter(o => {
      return (o.orderTrackingNumber || '').toLowerCase().includes(q) ||
             (o.customerName || '').toLowerCase().includes(q) ||
             (o.customerEmail || '').toLowerCase().includes(q) ||
             (o.shippingAddress || '').toLowerCase().includes(q);
    });
    this.renderAdminOrders(filtered);
  },

  async handleAssignTailor(orderId, selectId) {
    const sel = document.getElementById(selectId);
    if (!sel || !sel.value) {
      utils.showToast('Validation', 'Please select an artisan tailor from the bay list.', 'info');
      return;
    }

    try {
      const res = await api.assignOrderToTailor(orderId, sel.value);
      if (res && res.success) {
        utils.showToast('Tailor Allocated', `Order ${res.data.orderTrackingNumber} allocated to ${res.data.assignedTailorName}`, 'success');
        await this.loadAdminData();
      }
    } catch (err) {
      utils.showToast('Allocation Failed', err.message, 'error');
    }
  },

  async handleAdminStatusUpdate(orderId, statusSelectId, notesInputId) {
    const statusSel = document.getElementById(statusSelectId);
    const notesInp = document.getElementById(notesInputId);
    if (!statusSel) return;

    const status = statusSel.value;
    const tailorNotes = notesInp ? notesInp.value.trim() : '';

    try {
      const res = await api.updateOrderStatusByAdmin(orderId, status, tailorNotes);
      if (res && res.success) {
        utils.showToast('Status Updated', `Order ${res.data.orderTrackingNumber} is now ${res.data.status.replace(/_/g, ' ')}`, 'success');
        await this.loadAdminData();
      }
    } catch (err) {
      utils.showToast('Status Update Error', err.message, 'error');
    }
  },

  renderAdminProducts(filteredList) {
    const container = document.getElementById('admin-products-table-container');
    if (!container) return;

    const products = filteredList !== undefined ? filteredList : (appState.adminProducts || []);

    if (products.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="padding: 3rem 1rem;">
          <h4 style="margin-bottom: 0.5rem;">No Bespoke Garments in Catalog</h4>
          <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1.5rem;">Click "Add New Garment" above to introduce custom clothing to your boutique.</p>
          <button class="btn-primary" onclick="App.openAddProductModal()">Add First Garment</button>
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <table class="portal-table">
        <thead>
          <tr>
            <th>Garment</th>
            <th>Category</th>
            <th>Base Price (INR)</th>
            <th>Available Cuts & Cloth</th>
            <th>Status</th>
            <th>Catalog Actions</th>
          </tr>
        </thead>
        <tbody>
          ${products.map(p => `
            <tr>
              <td>
                <div style="display: flex; align-items: center; gap: 0.75rem;">
                  <img src="${p.imageUrl}" alt="${p.name}" style="width: 50px; height: 60px; object-fit: cover; border-radius: var(--radius-sm); border: 1px solid var(--border-light);" onerror="this.src='https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=400&q=80'"/>
                  <div>
                    <strong style="display: block; font-size: 0.95rem; color: var(--text-main);">${p.name}</strong>
                    <span style="font-size: 0.78rem; color: var(--text-dim); max-width: 250px; display: inline-block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${p.description || ''}</span>
                  </div>
                </div>
              </td>
              <td><span style="color: var(--gold-light); font-weight: 500;">${p.categoryName || 'Bespoke'}</span></td>
              <td><strong style="color: var(--gold-light); font-family: var(--font-heading); font-size: 1.05rem;">${utils.formatCurrency(p.basePrice)}</strong></td>
              <td>
                <div style="font-size: 0.78rem; color: var(--text-muted); max-width: 280px;">
                  <div><strong>Cuts:</strong> ${p.availableSizes || 'Standard'}</div>
                  <div style="margin-top: 2px;"><strong>Cloth:</strong> ${p.availableFabrics || 'Italian Wool'}</div>
                </div>
              </td>
              <td>
                ${p.active !== false
                  ? '<span class="status-badge status-delivered">Active</span>'
                  : '<span class="status-badge status-cancelled">Inactive</span>'
                }
              </td>
              <td>
                <div style="display: flex; gap: 0.5rem; align-items: center;">
                  <button class="btn-secondary btn-sm" onclick="App.openEditProductModal(${p.id})">
                    Edit
                  </button>
                  <button class="btn-secondary btn-sm" style="color: ${p.active !== false ? 'var(--accent-danger)' : 'var(--accent-success)'};" onclick="App.handleToggleProductStatus(${p.id})">
                    ${p.active !== false ? 'Deactivate' : 'Activate'}
                  </button>
                </div>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  },

  searchAdminProducts(query) {
    const q = (query || '').toLowerCase().trim();
    if (!q) {
      this.renderAdminProducts(appState.adminProducts);
      return;
    }
    const filtered = (appState.adminProducts || []).filter(p => {
      return (p.name || '').toLowerCase().includes(q) ||
             (p.categoryName || '').toLowerCase().includes(q) ||
             (p.description || '').toLowerCase().includes(q) ||
             (p.availableFabrics || '').toLowerCase().includes(q);
    });
    this.renderAdminProducts(filtered);
  },

  openAddProductModal() {
    const form = document.getElementById('form-manage-product');
    if (form) form.reset();

    const titleEl = document.getElementById('product-modal-title');
    if (titleEl) titleEl.textContent = 'Add New Bespoke Garment';

    const idInput = document.getElementById('prod-form-id');
    if (idInput) idInput.value = '';

    const catSelect = document.getElementById('prod-form-category');
    if (catSelect) {
      catSelect.innerHTML = (appState.categories || []).map(c => `
        <option value="${c.id}">${c.name}</option>
      `).join('');
    }

    const imgInput = document.getElementById('prod-form-image');
    if (imgInput) imgInput.value = 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80';
    const previewImg = document.getElementById('prod-form-preview-img');
    if (previewImg) previewImg.src = 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80';

    const sizesInput = document.getElementById('prod-form-sizes');
    if (sizesInput) sizesInput.value = '38R, 40R, 42R, Custom Made-to-Measure';

    const colorsInput = document.getElementById('prod-form-colors');
    if (colorsInput) colorsInput.value = 'Midnight Navy, Charcoal Grey, Obsidian Black';

    const fabricsInput = document.getElementById('prod-form-fabrics');
    if (fabricsInput) fabricsInput.value = 'Super 130s Merino Wool, Cashmere Blend';

    const activeCheckbox = document.getElementById('prod-form-active');
    if (activeCheckbox) activeCheckbox.checked = true;

    this.openModal('product-modal');
  },

  openEditProductModal(productId) {
    const product = (appState.adminProducts || []).find(p => p.id === productId) ||
                    (appState.products || []).find(p => p.id === productId);
    if (!product) return;

    const titleEl = document.getElementById('product-modal-title');
    if (titleEl) titleEl.textContent = `Edit Garment: ${product.name}`;

    const idInput = document.getElementById('prod-form-id');
    if (idInput) idInput.value = product.id;

    const nameInput = document.getElementById('prod-form-name');
    if (nameInput) nameInput.value = product.name;

    const catSelect = document.getElementById('prod-form-category');
    if (catSelect) {
      catSelect.innerHTML = (appState.categories || []).map(c => `
        <option value="${c.id}" ${c.id === product.categoryId ? 'selected' : ''}>${c.name}</option>
      `).join('');
    }

    const priceInput = document.getElementById('prod-form-price');
    if (priceInput) priceInput.value = product.basePrice;

    const imgInput = document.getElementById('prod-form-image');
    if (imgInput) imgInput.value = product.imageUrl || '';

    const previewImg = document.getElementById('prod-form-preview-img');
    if (previewImg) previewImg.src = product.imageUrl || 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80';

    const sizesInput = document.getElementById('prod-form-sizes');
    if (sizesInput) sizesInput.value = product.availableSizes || '';

    const colorsInput = document.getElementById('prod-form-colors');
    if (colorsInput) colorsInput.value = product.availableColors || '';

    const fabricsInput = document.getElementById('prod-form-fabrics');
    if (fabricsInput) fabricsInput.value = product.availableFabrics || '';

    const descInput = document.getElementById('prod-form-desc');
    if (descInput) descInput.value = product.description || '';

    const activeCheckbox = document.getElementById('prod-form-active');
    if (activeCheckbox) activeCheckbox.checked = product.active !== false;

    this.openModal('product-modal');
  },

  async handleSaveProduct(e) {
    e.preventDefault();

    const idVal = document.getElementById('prod-form-id')?.value;
    const name = document.getElementById('prod-form-name')?.value.trim();
    const categoryId = Number(document.getElementById('prod-form-category')?.value);
    const basePrice = parseFloat(document.getElementById('prod-form-price')?.value);
    const imageUrl = document.getElementById('prod-form-image')?.value.trim();
    const availableSizes = document.getElementById('prod-form-sizes')?.value.trim();
    const availableColors = document.getElementById('prod-form-colors')?.value.trim();
    const availableFabrics = document.getElementById('prod-form-fabrics')?.value.trim();
    const description = document.getElementById('prod-form-desc')?.value.trim();
    const active = document.getElementById('prod-form-active')?.checked ?? true;

    if (!name || isNaN(basePrice) || basePrice <= 0 || !categoryId) {
      utils.showToast('Validation Error', 'Please complete title, valid INR price, and category.', 'error');
      return;
    }

    const payload = {
      name,
      categoryId,
      basePrice,
      imageUrl,
      availableSizes,
      availableColors,
      availableFabrics,
      description,
      active
    };

    const submitBtn = document.getElementById('btn-save-product');
    const origText = submitBtn.textContent;
    submitBtn.textContent = 'Saving Garment...';
    submitBtn.disabled = true;

    try {
      if (idVal) {
        await api.updateProduct(Number(idVal), payload);
        utils.showToast('Garment Updated', `Bespoke piece "${name}" updated successfully.`, 'success');
      } else {
        await api.createProduct(payload);
        utils.showToast('Garment Created', `New bespoke piece "${name}" added to catalog.`, 'success');
      }

      this.closeModal('product-modal');
      await this.loadAdminData();
      await this.loadProducts();
    } catch (err) {
      utils.showToast('Save Failed', err.message, 'error');
    } finally {
      submitBtn.textContent = origText;
      submitBtn.disabled = false;
    }
  },

  async handleToggleProductStatus(productId) {
    try {
      const res = await api.toggleProductStatus(productId);
      if (res && res.success) {
        utils.showToast('Catalog Updated', `Garment status updated successfully.`, 'info');
        await this.loadAdminData();
        await this.loadProducts();
      }
    } catch (err) {
      utils.showToast('Error', err.message, 'error');
    }
  },

  renderAdminCustomers(filteredList) {
    const container = document.getElementById('admin-customers-table-container');
    if (!container) return;

    const customers = filteredList !== undefined ? filteredList : (appState.adminCustomers || []);

    if (customers.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="padding: 3rem 1rem;">
          <h4 style="margin-bottom: 0.5rem;">No Patrons Registered</h4>
          <p style="font-size: 0.88rem; color: var(--text-muted);">Registered customer accounts will appear here.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <table class="portal-table">
        <thead>
          <tr>
            <th>Patron Name</th>
            <th>Email Address</th>
            <th>Telephone</th>
            <th>Default Delivery Address</th>
            <th>Fit & Dossier</th>
          </tr>
        </thead>
        <tbody>
          ${customers.map(c => `
            <tr>
              <td><strong style="color: var(--text-main); font-size: 0.95rem;">${c.fullName}</strong></td>
              <td>${c.email}</td>
              <td>${c.phoneNumber || 'N/A'}</td>
              <td><span style="font-size: 0.82rem; color: var(--text-muted);">${c.address || 'N/A'}</span></td>
              <td>
                <button class="btn-primary btn-sm" onclick="App.viewCustomerProfile(${c.id})">
                  View Fit & Orders
                </button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  },

  searchAdminCustomers(query) {
    const q = (query || '').toLowerCase().trim();
    if (!q) {
      this.renderAdminCustomers(appState.adminCustomers);
      return;
    }
    const filtered = (appState.adminCustomers || []).filter(c => {
      return (c.fullName || '').toLowerCase().includes(q) ||
             (c.email || '').toLowerCase().includes(q) ||
             (c.phoneNumber || '').toLowerCase().includes(q) ||
             (c.address || '').toLowerCase().includes(q);
    });
    this.renderAdminCustomers(filtered);
  },

  async viewCustomerProfile(customerId) {
    const customer = (appState.adminCustomers || []).find(c => c.id === customerId);

    const nameEl = document.getElementById('cust-modal-name');
    if (nameEl) nameEl.textContent = customer ? `Patron Dossier: ${customer.fullName}` : 'Patron Details';
    const emailEl = document.getElementById('cust-modal-email');
    if (emailEl) emailEl.textContent = customer ? customer.email : 'N/A';
    const phoneEl = document.getElementById('cust-modal-phone');
    if (phoneEl) phoneEl.textContent = customer ? (customer.phoneNumber || 'N/A') : 'N/A';
    const addrEl = document.getElementById('cust-modal-address');
    if (addrEl) addrEl.textContent = customer ? (customer.address || 'N/A') : 'N/A';

    const measContainer = document.getElementById('cust-modal-measurements-container');
    const ordersContainer = document.getElementById('cust-modal-orders-container');
    if (measContainer) measContainer.innerHTML = '<p style="color:var(--text-dim); font-size:0.85rem;">Retrieving fit registry...</p>';
    if (ordersContainer) ordersContainer.innerHTML = '<p style="color:var(--text-dim); font-size:0.85rem;">Retrieving bespoke order history...</p>';

    this.openModal('customer-details-modal');

    // Fetch measurements and orders in parallel
    try {
      const [measRes, ordersRes] = await Promise.all([
        api.getCustomerMeasurements(customerId),
        api.getCustomerOrders(customerId)
      ]);

      // Render measurements
      if (measContainer) {
        const measurements = (measRes && measRes.success && measRes.data) || [];
        if (measurements.length === 0) {
          measContainer.innerHTML = '<p style="color:var(--text-muted); font-size:0.85rem; font-style:italic;">No saved measurement profiles recorded for this patron.</p>';
        } else {
          measContainer.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 1rem;">
              ${measurements.map(m => `
                <div style="background: var(--bg-input); border: 1px solid var(--border-light); border-radius: var(--radius-sm); padding: 1rem;">
                  <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">
                    <strong style="color:var(--gold-light); font-size:0.95rem;">${m.profileName}</strong>
                    <span style="font-size:0.7rem; color:var(--text-dim); text-transform:uppercase;">${m.unit || 'inches'}</span>
                  </div>
                  <div class="tailor-measurements-grid" style="grid-template-columns: repeat(4, 1fr);">
                    <div class="tailor-m-box"><div class="tailor-m-box-label">Chest</div><div class="tailor-m-box-val">${m.chest || '—'}</div></div>
                    <div class="tailor-m-box"><div class="tailor-m-box-label">Waist</div><div class="tailor-m-box-val">${m.waist || '—'}</div></div>
                    <div class="tailor-m-box"><div class="tailor-m-box-label">Hips</div><div class="tailor-m-box-val">${m.hips || '—'}</div></div>
                    <div class="tailor-m-box"><div class="tailor-m-box-label">Shoulder</div><div class="tailor-m-box-val">${m.shoulderWidth || '—'}</div></div>
                    <div class="tailor-m-box"><div class="tailor-m-box-label">Sleeve</div><div class="tailor-m-box-val">${m.sleeveLength || '—'}</div></div>
                    <div class="tailor-m-box"><div class="tailor-m-box-label">Shirt</div><div class="tailor-m-box-val">${m.shirtLength || '—'}</div></div>
                    <div class="tailor-m-box"><div class="tailor-m-box-label">Inseam</div><div class="tailor-m-box-val">${m.inseam || '—'}</div></div>
                    <div class="tailor-m-box"><div class="tailor-m-box-label">Neck</div><div class="tailor-m-box-val">${m.neck || '—'}</div></div>
                  </div>
                  ${m.notes ? `<p style="font-size:0.76rem; color:var(--text-muted); font-style:italic; margin-top:0.4rem;">"${m.notes}"</p>` : ''}
                </div>
              `).join('')}
            </div>
          `;
        }
      }

      // Render orders
      if (ordersContainer) {
        const orders = (ordersRes && ordersRes.success && ordersRes.data) || [];
        if (orders.length === 0) {
          ordersContainer.innerHTML = '<p style="color:var(--text-muted); font-size:0.85rem; font-style:italic;">No bespoke order history found for this patron.</p>';
        } else {
          ordersContainer.innerHTML = `
            <div class="portal-table-container" style="margin-bottom:0;">
              <table class="portal-table">
                <thead>
                  <tr>
                    <th>Tracking #</th>
                    <th>Date</th>
                    <th>Total (INR)</th>
                    <th>Status</th>
                    <th>Tailor Assigned</th>
                  </tr>
                </thead>
                <tbody>
                  ${orders.map(o => `
                    <tr>
                      <td><strong style="color:var(--gold-light); font-family:var(--font-heading);">${o.orderTrackingNumber}</strong></td>
                      <td>${utils.formatDate(o.orderDate)}</td>
                      <td><strong style="color:var(--gold-light);">${utils.formatCurrency(o.totalAmount)}</strong></td>
                      <td>${utils.getStatusBadge(o.status)}</td>
                      <td>${o.assignedTailorName ? `✂️ ${o.assignedTailorName}` : '<span style="color:#fbbf24; font-size:0.8rem;">Unassigned</span>'}</td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          `;
        }
      }
    } catch (err) {
      console.error('Failed to load customer profile details:', err);
    }
  },

  // ==========================================
  // Tailor Workshop Bay Module
  // ==========================================
  async loadTailorData() {
    if (!appState.isLoggedIn || !appState.isTailor) return;

    const workshopTitle = document.getElementById('tailor-workshop-name');
    if (workshopTitle) {
      workshopTitle.textContent = `${appState.currentUser.fullName} - Workshop Bay`;
    }

    try {
      const res = await api.getTailorAssignedOrders(appState.currentUser.id);
      if (res && res.success) {
        appState.tailorOrders = res.data || [];
        const orders = appState.tailorOrders;

        // Update metrics
        const totalEl = document.getElementById('metric-tailor-total');
        if (totalEl) totalEl.textContent = orders.length;

        const stitchingEl = document.getElementById('metric-tailor-stitching');
        if (stitchingEl) stitchingEl.textContent = orders.filter(o => o.status === 'UNDER_STITCHING').length;

        const readyEl = document.getElementById('metric-tailor-ready');
        if (readyEl) readyEl.textContent = orders.filter(o => o.status === 'READY_FOR_DELIVERY').length;

        const deliveredEl = document.getElementById('metric-tailor-delivered');
        if (deliveredEl) deliveredEl.textContent = orders.filter(o => o.status === 'DELIVERED').length;

        this.renderTailorOrders();
      }
    } catch (err) {
      console.error('Failed to load tailor orders:', err);
      utils.showToast('Workshop Bay', err.message || 'Error loading tailor orders', 'error');
    }
  },

  renderTailorOrders(filteredList) {
    const container = document.getElementById('tailor-orders-list-container');
    if (!container) return;

    const orders = filteredList !== undefined ? filteredList : (appState.tailorOrders || []);

    if (orders.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="padding: 4rem 1rem;">
          <div class="empty-state-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="6" cy="6" r="3"/><circle cx="6" cy="18" r="3"/>
              <line x1="20" y1="4" x2="8.12" y2="15.88"/><line x1="14.47" y1="14.48" x2="20" y2="20"/><line x1="8.12" y1="8.12" x2="12" y2="12"/>
            </svg>
          </div>
          <h4 style="margin-bottom: 0.5rem;">No Allocated Garments in Workshop Queue</h4>
          <p style="font-size: 0.88rem; color: var(--text-muted);">When bespoke orders are assigned to your bay by the master administrator, they will appear here with full customer measurement dossiers.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = orders.map(order => {
      const items = order.items || [];

      return `
        <div class="order-card" data-order-id="${order.id}">
          <!-- Order Card Header -->
          <div class="order-card-header">
            <div style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap;">
              <span class="order-card-tracking">${order.orderTrackingNumber}</span>
              ${utils.getStatusBadge(order.status)}
              <span style="font-size: 0.82rem; color: var(--text-dim);">Allocated: ${utils.formatDate(order.orderDate)}</span>
              <span style="font-size: 0.82rem; color: var(--gold-light);">Target Delivery: ${utils.formatDate(order.expectedDeliveryDate)}</span>
            </div>
            <div style="font-family: var(--font-heading); font-size: 1.35rem; font-weight: 700; color: var(--gold-light);">
              ${utils.formatCurrency(order.totalAmount)}
            </div>
          </div>

          <!-- Patron Details Box -->
          <div class="order-card-meta" style="margin-bottom: 1rem; padding: 0.75rem 1rem; background: rgba(255,255,255,0.015); border-radius: var(--radius-sm); border: 1px solid var(--border-light);">
            <div><strong>Patron:</strong> ${order.customerName || 'N/A'}</div>
            <div><strong>Contact Tel:</strong> ${order.contactPhone || 'N/A'}</div>
            <div><strong>Fitting / Shipping Address:</strong> ${order.shippingAddress || 'N/A'}</div>
            ${order.customerNotes ? `<div style="width: 100%; color: var(--gold-light); font-style: italic;">💬 Patron Note: "${order.customerNotes}"</div>` : ''}
            ${order.tailorNotes ? `<div style="width: 100%; color: #93c5fd; font-style: italic;">✂️ Current Workshop Log: "${order.tailorNotes}"</div>` : ''}
          </div>

          <!-- Garments with Full Customer Measurements & Customization -->
          <div style="margin-bottom: 1rem;">
            ${items.map(item => {
              const m = item.measurement;

              return `
                <div class="order-garment-item" style="flex-direction: column; gap: 0.75rem;">
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <h4 class="garment-title" style="font-size: 1.05rem;">
                      ${item.productName} <small style="color:var(--text-dim); font-weight:normal;">(Quantity: ${item.quantity})</small>
                    </h4>
                    <span style="color: var(--gold-light); font-weight: 600;">${utils.formatCurrency(item.subtotal)}</span>
                  </div>

                  <div class="garment-tags">
                    ${item.selectedSize ? `<span class="garment-tag-chip">Cut: ${item.selectedSize}</span>` : ''}
                    ${item.selectedColor ? `<span class="garment-tag-chip">Shade: ${item.selectedColor}</span>` : ''}
                    ${item.selectedFabric ? `<span class="garment-tag-chip">Cloth: ${item.selectedFabric}</span>` : ''}
                  </div>

                  <!-- Full Anatomical Measurements Grid for Tailor Precision -->
                  ${m ? `
                    <div style="background: rgba(212, 175, 55, 0.04); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: 0.85rem;">
                      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.6rem;">
                        <span style="font-size: 0.8rem; font-weight: 700; color: var(--gold-light); text-transform: uppercase; letter-spacing: 0.05em;">
                          📏 Customer Anatomical Measurement Profile: ${m.profileName}
                        </span>
                        <span class="profile-unit-tag">${m.unit || 'inches'}</span>
                      </div>

                      <div class="tailor-measurements-grid">
                        <div class="tailor-m-box">
                          <div class="tailor-m-box-label">Chest</div>
                          <div class="tailor-m-box-val">${m.chest ? m.chest + ' ' + m.unit : '—'}</div>
                        </div>
                        <div class="tailor-m-box">
                          <div class="tailor-m-box-label">Waist</div>
                          <div class="tailor-m-box-val">${m.waist ? m.waist + ' ' + m.unit : '—'}</div>
                        </div>
                        <div class="tailor-m-box">
                          <div class="tailor-m-box-label">Hips</div>
                          <div class="tailor-m-box-val">${m.hips ? m.hips + ' ' + m.unit : '—'}</div>
                        </div>
                        <div class="tailor-m-box">
                          <div class="tailor-m-box-label">Shoulder</div>
                          <div class="tailor-m-box-val">${m.shoulderWidth ? m.shoulderWidth + ' ' + m.unit : '—'}</div>
                        </div>
                        <div class="tailor-m-box">
                          <div class="tailor-m-box-label">Sleeve</div>
                          <div class="tailor-m-box-val">${m.sleeveLength ? m.sleeveLength + ' ' + m.unit : '—'}</div>
                        </div>
                        <div class="tailor-m-box">
                          <div class="tailor-m-box-label">Shirt Length</div>
                          <div class="tailor-m-box-val">${m.shirtLength ? m.shirtLength + ' ' + m.unit : '—'}</div>
                        </div>
                        <div class="tailor-m-box">
                          <div class="tailor-m-box-label">Inseam</div>
                          <div class="tailor-m-box-val">${m.inseam ? m.inseam + ' ' + m.unit : '—'}</div>
                        </div>
                        <div class="tailor-m-box">
                          <div class="tailor-m-box-label">Neck Collar</div>
                          <div class="tailor-m-box-val">${m.neck ? m.neck + ' ' + m.unit : '—'}</div>
                        </div>
                      </div>

                      ${m.notes ? `
                        <div style="font-size: 0.78rem; color: var(--text-muted); font-style: italic; margin-top: 0.5rem; padding-top: 0.4rem; border-top: 1px dashed var(--border-light);">
                          <strong>Fitting & Posture Notes:</strong> "${m.notes}"
                        </div>
                      ` : ''}
                    </div>
                  ` : `
                    <div class="measurement-callout-pill" style="font-size: 0.82rem;">
                      Standard Sizing Cut (${item.selectedSize}) - Tailor to standard pattern dimensions.
                    </div>
                  `}

                  <!-- Customization Instructions -->
                  ${item.customizationInstructions ? `
                    <div class="instructions-callout-pill" style="font-size: 0.82rem;">
                      🧵 <strong>Special Customization Request:</strong> "${item.customizationInstructions}"
                    </div>
                  ` : ''}
                </div>
              `;
            }).join('')}
          </div>

          <!-- Workshop Action Bar: Update Stitching Status & Workshop Log -->
          <div class="order-card-actions" style="background: rgba(0,0,0,0.2); padding: 1rem; border-radius: var(--radius-sm);">
            <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; width: 100%;">
              <span style="font-size: 0.78rem; font-weight: 700; color: var(--gold-light); text-transform: uppercase;">Update Stitching Status:</span>
              <select id="tailor-status-${order.id}" class="portal-select">
                <option value="UNDER_STITCHING" ${order.status === 'UNDER_STITCHING' ? 'selected' : ''}>Under Stitching (Active Tailoring)</option>
                <option value="READY_FOR_DELIVERY" ${order.status === 'READY_FOR_DELIVERY' ? 'selected' : ''}>Ready for Delivery (Fitting Done)</option>
                <option value="DELIVERED" ${order.status === 'DELIVERED' ? 'selected' : ''}>Delivered to Patron</option>
              </select>

              <input type="text" id="tailor-notes-${order.id}" class="portal-input" style="flex: 1; min-width: 250px;" placeholder="Workshop log: e.g. Fabric chalked, canvas basted, ready for sleeve attachment..." value="${order.tailorNotes || ''}">

              <button class="btn-primary btn-sm" onclick="App.handleTailorStatusUpdate(${order.id}, 'tailor-status-${order.id}', 'tailor-notes-${order.id}')">
                Update Workshop Status
              </button>
            </div>
          </div>
        </div>
      `;
    }).join('');
  },

  filterTailorOrders(status) {
    const filterChips = document.querySelectorAll('#tailor-order-status-filters .filter-chip');
    filterChips.forEach(chip => {
      chip.classList.toggle('active', chip.dataset.status === status);
    });

    if (status === 'ALL') {
      this.renderTailorOrders(appState.tailorOrders);
    } else {
      const filtered = (appState.tailorOrders || []).filter(o => o.status === status);
      this.renderTailorOrders(filtered);
    }
  },

  async handleTailorStatusUpdate(orderId, statusSelectId, notesInputId) {
    if (!appState.isLoggedIn || !appState.isTailor) return;

    const statusSel = document.getElementById(statusSelectId);
    const notesInp = document.getElementById(notesInputId);
    if (!statusSel) return;

    const status = statusSel.value;
    const tailorNotes = notesInp ? notesInp.value.trim() : '';

    try {
      const res = await api.updateOrderStatusByTailor(orderId, appState.currentUser.id, status, tailorNotes);
      if (res && res.success) {
        utils.showToast('Workshop Log Updated', `Order ${res.data.orderTrackingNumber} updated to ${res.data.status.replace(/_/g, ' ')}`, 'success');
        await this.loadTailorData();
      }
    } catch (err) {
      utils.showToast('Workshop Update Error', err.message, 'error');
    }
  },
  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('active');
      document.body.style.overflow = 'hidden';
    }
  },

  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove('active');
      if (!document.querySelector('.modal-overlay.active') && !document.querySelector('.cart-drawer.active')) {
        document.body.style.overflow = '';
      }
    }
  }
};

window.App = App;

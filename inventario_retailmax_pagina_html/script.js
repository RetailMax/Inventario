// Configuration
const API_BASE_URL = 'http://localhost:8080/api/inventario';

// Global state
let currentSection = 'dashboard';
let productos = [];
let movimientos = [];
let alertas = [];
let editingProduct = null;
let editingAlert = null;

// DOM Elements
const sections = document.querySelectorAll('.section');
const navButtons = document.querySelectorAll('.nav-btn');
const loadingOverlay = document.getElementById('loading-overlay');
const toastContainer = document.getElementById('toast-container');

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    showSection('dashboard');
    loadDashboardData();
});

// Event Listeners
function initializeEventListeners() {
    // Navigation
    navButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const section = e.target.closest('.nav-btn').dataset.section;
            showSection(section);
        });
    });

    // Product management
    document.getElementById('btn-nuevo-producto').addEventListener('click', () => openProductModal());
    document.getElementById('form-producto').addEventListener('submit', handleProductSubmit);
    document.getElementById('search-productos').addEventListener('input', filterProducts);
    document.getElementById('filter-estado').addEventListener('change', filterProducts);

    // Stock management
    document.getElementById('form-actualizar-stock').addEventListener('submit', handleStockUpdate);
    document.getElementById('form-ajuste-manual').addEventListener('submit', handleManualAdjustment);
    document.getElementById('btn-stock-bajo').addEventListener('click', () => queryStockBajo());
    document.getElementById('btn-stock-exceso').addEventListener('click', () => queryStockExceso());

    // Movements
    document.getElementById('btn-buscar-movimientos').addEventListener('click', searchMovements);

    // Alerts
    document.getElementById('btn-nueva-alerta').addEventListener('click', () => openAlertModal());
    document.getElementById('form-alerta').addEventListener('submit', handleAlertSubmit);

    // Modals
    document.querySelectorAll('.modal-close, .modal-cancel').forEach(btn => {
        btn.addEventListener('click', closeModals);
    });

    // Close modals on outside click
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModals();
        });
    });
}

// Navigation
function showSection(sectionName) {
    // Update navigation
    navButtons.forEach(btn => btn.classList.remove('active'));
    document.querySelector(`[data-section="${sectionName}"]`).classList.add('active');

    // Update sections
    sections.forEach(section => section.classList.remove('active'));
    document.getElementById(sectionName).classList.add('active');

    currentSection = sectionName;

    // Load section data
    switch(sectionName) {
        case 'dashboard':
            loadDashboardData();
            break;
        case 'productos':
            loadProducts();
            break;
        case 'stock':
            // Stock section doesn't need initial data load
            break;
        case 'movimientos':
            // Movements are loaded on demand
            break;
        case 'alertas':
            loadAlerts();
            break;
    }
}

// API Functions
async function apiRequest(endpoint, options = {}) {
    showLoading(true);
    try {
        const url = `${API_BASE_URL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/hal+json',
                ...options.headers
            },
            ...options
        };

        const response = await fetch(url, config);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API Error:', error);
        showToast('Error en la comunicación con el servidor', 'error');
        throw error;
    } finally {
        showLoading(false);
    }
}

// Dashboard Functions
async function loadDashboardData() {
    try {
        await loadProducts();
        updateDashboardStats();
        loadActiveAlerts();
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

function updateDashboardStats() {
    const totalProductos = productos.length;
    const stockTotal = productos.reduce((sum, p) => sum + (p.cantidadDisponible || 0), 0);
    const stockBajo = productos.filter(p => (p.cantidadDisponible || 0) <= (p.cantidadMinimaStock || 0)).length;

    document.getElementById('total-productos').textContent = totalProductos;
    document.getElementById('stock-total').textContent = stockTotal;
    document.getElementById('stock-bajo').textContent = stockBajo;
    document.getElementById('movimientos-hoy').textContent = '0'; // Would need date filtering

    // Update chart
    updateEstadosChart();
}

function updateEstadosChart() {
    const estadosCount = {};
    productos.forEach(p => {
        const estado = p.estado || 'DISPONIBLE';
        estadosCount[estado] = (estadosCount[estado] || 0) + 1;
    });

    const chartContainer = document.getElementById('chart-estados');
    chartContainer.innerHTML = '';

    Object.entries(estadosCount).forEach(([estado, count]) => {
        const bar = document.createElement('div');
        bar.style.cssText = `
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.5rem;
            margin-bottom: 0.5rem;
            background: var(--gray-100);
            border-radius: var(--border-radius);
        `;
        bar.innerHTML = `
            <span>${estado.replace('_', ' ')}</span>
            <span class="font-bold">${count}</span>
        `;
        chartContainer.appendChild(bar);
    });
}

async function loadActiveAlerts() {
    try {
        const response = await apiRequest('/umbrales');
        const activeAlerts = response._embedded?.umbralAlertaDTOList?.filter(alert => alert.activo) || [];
        
        const container = document.getElementById('alertas-activas');
        container.innerHTML = '';

        if (activeAlerts.length === 0) {
            container.innerHTML = '<p class="text-center text-gray-500">No hay alertas activas</p>';
            return;
        }

        activeAlerts.forEach(alert => {
            const alertElement = document.createElement('div');
            alertElement.style.cssText = `
                padding: 0.75rem;
                margin-bottom: 0.5rem;
                background: var(--warning-color);
                color: white;
                border-radius: var(--border-radius);
                font-size: 0.875rem;
            `;
            alertElement.innerHTML = `
                <strong>${alert.sku}</strong> - ${alert.tipoAlerta.replace('_', ' ')}
                <br>Umbral: ${alert.umbralCantidad}
            `;
            container.appendChild(alertElement);
        });
    } catch (error) {
        console.error('Error loading alerts:', error);
    }
}

// Product Functions
async function loadProducts() {
    try {
        const response = await apiRequest('/productos');
        productos = response._embedded?.productoInventarioDTOList || [];
        renderProductsTable();
    } catch (error) {
        console.error('Error loading products:', error);
        productos = [];
        renderProductsTable();
    }
}

function renderProductsTable() {
    const tbody = document.querySelector('#tabla-productos tbody');
    tbody.innerHTML = '';

    if (productos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay productos registrados</td></tr>';
        return;
    }

    productos.forEach(producto => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${producto.sku}</td>
            <td>${producto.cantidadDisponible || 0}</td>
            <td>${producto.cantidadReservada || 0}</td>
            <td>${producto.ubicacionAlmacen || '-'}</td>
            <td><span class="status-badge ${(producto.estado || 'disponible').toLowerCase().replace('_', '-')}">${producto.estado || 'DISPONIBLE'}</span></td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn edit" onclick="editProduct('${producto.sku}')">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn delete" onclick="deleteProduct('${producto.sku}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function filterProducts() {
    const searchTerm = document.getElementById('search-productos').value.toLowerCase();
    const estadoFilter = document.getElementById('filter-estado').value;

    const filteredProducts = productos.filter(producto => {
        const matchesSearch = producto.sku.toLowerCase().includes(searchTerm);
        const matchesEstado = !estadoFilter || producto.estado === estadoFilter;
        return matchesSearch && matchesEstado;
    });

    renderFilteredProducts(filteredProducts);
}

function renderFilteredProducts(filteredProducts) {
    const tbody = document.querySelector('#tabla-productos tbody');
    tbody.innerHTML = '';

    if (filteredProducts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">No se encontraron productos</td></tr>';
        return;
    }

    filteredProducts.forEach(producto => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${producto.sku}</td>
            <td>${producto.cantidadDisponible || 0}</td>
            <td>${producto.cantidadReservada || 0}</td>
            <td>${producto.ubicacionAlmacen || '-'}</td>
            <td><span class="status-badge ${(producto.estado || 'disponible').toLowerCase().replace('_', '-')}">${producto.estado || 'DISPONIBLE'}</span></td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn edit" onclick="editProduct('${producto.sku}')">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn delete" onclick="deleteProduct('${producto.sku}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function openProductModal(product = null) {
    editingProduct = product;
    const modal = document.getElementById('modal-producto');
    const title = document.getElementById('modal-producto-title');
    const form = document.getElementById('form-producto');

    title.textContent = product ? 'Editar Producto' : 'Nuevo Producto';
    
    if (product) {
        document.getElementById('producto-sku').value = product.sku;
        document.getElementById('producto-cantidad').value = product.cantidadDisponible || 0;
        document.getElementById('producto-ubicacion').value = product.ubicacionAlmacen || '';
        document.getElementById('producto-minimo').value = product.cantidadMinimaStock || 0;
        document.getElementById('producto-base').value = product.productoBaseSku || '';
        document.getElementById('producto-talla').value = product.talla || '';
        document.getElementById('producto-color').value = product.color || '';
        document.getElementById('producto-estado').value = product.estado || 'DISPONIBLE';
        document.getElementById('producto-sku').disabled = true;
    } else {
        form.reset();
        document.getElementById('producto-sku').disabled = false;
    }

    modal.classList.add('active');
}

async function handleProductSubmit(e) {
    e.preventDefault();
    
    const formData = {
        sku: document.getElementById('producto-sku').value,
        cantidadInicial: parseInt(document.getElementById('producto-cantidad').value),
        ubicacionAlmacen: document.getElementById('producto-ubicacion').value,
        cantidadMinimaStock: parseInt(document.getElementById('producto-minimo').value) || 0,
        productoBaseSku: document.getElementById('producto-base').value,
        talla: document.getElementById('producto-talla').value,
        color: document.getElementById('producto-color').value,
        estado: document.getElementById('producto-estado').value
    };

    try {
        if (editingProduct) {
            await apiRequest(`/productos/${editingProduct.sku}`, {
                method: 'PUT',
                body: JSON.stringify(formData)
            });
            showToast('Producto actualizado exitosamente', 'success');
        } else {
            await apiRequest('/productos', {
                method: 'POST',
                body: JSON.stringify(formData)
            });
            showToast('Producto creado exitosamente', 'success');
        }
        
        closeModals();
        loadProducts();
        if (currentSection === 'dashboard') {
            updateDashboardStats();
        }
    } catch (error) {
        showToast('Error al guardar el producto', 'error');
    }
}

async function editProduct(sku) {
    try {
        const response = await apiRequest(`/productos/${sku}`);
        openProductModal(response);
    } catch (error) {
        showToast('Error al cargar el producto', 'error');
    }
}

async function deleteProduct(sku) {
    if (!confirm('¿Está seguro de que desea eliminar este producto?')) {
        return;
    }

    try {
        await apiRequest(`/productos/${sku}`, { method: 'DELETE' });
        showToast('Producto eliminado exitosamente', 'success');
        loadProducts();
        if (currentSection === 'dashboard') {
            updateDashboardStats();
        }
    } catch (error) {
        showToast('Error al eliminar el producto', 'error');
    }
}

// Stock Functions
async function handleStockUpdate(e) {
    e.preventDefault();
    
    const formData = {
        sku: document.getElementById('stock-sku').value,
        cantidad: parseInt(document.getElementById('stock-cantidad').value),
        tipoMovimiento: document.getElementById('stock-tipo').value,
        referenciaExterna: document.getElementById('stock-referencia').value,
        motivo: document.getElementById('stock-motivo').value
    };

    try {
        await apiRequest('/productos/stock', {
            method: 'PUT',
            body: JSON.stringify(formData)
        });
        showToast('Stock actualizado exitosamente', 'success');
        document.getElementById('form-actualizar-stock').reset();
        loadProducts();
    } catch (error) {
        showToast('Error al actualizar el stock', 'error');
    }
}

async function handleManualAdjustment(e) {
    e.preventDefault();
    
    const formData = {
        sku: document.getElementById('ajuste-sku').value,
        cantidad: parseInt(document.getElementById('ajuste-cantidad').value),
        tipoMovimiento: document.getElementById('ajuste-tipo').value,
        motivo: document.getElementById('ajuste-motivo').value
    };

    try {
        await apiRequest('/productos/stock/ajuste-manual', {
            method: 'POST',
            body: JSON.stringify(formData)
        });
        showToast('Ajuste manual realizado exitosamente', 'success');
        document.getElementById('form-ajuste-manual').reset();
        loadProducts();
    } catch (error) {
        showToast('Error al realizar el ajuste manual', 'error');
    }
}

async function queryStockBajo() {
    const umbral = parseInt(document.getElementById('umbral-bajo').value);
    
    try {
        const response = await apiRequest(`/productos/bajo-stock/${umbral}`);
        const productos = response._embedded?.productoInventarioDTOList || [];
        displayQueryResults(productos, 'Productos con Stock Bajo');
    } catch (error) {
        showToast('Error al consultar productos con stock bajo', 'error');
    }
}

async function queryStockExceso() {
    const umbral = parseInt(document.getElementById('umbral-exceso').value);
    
    try {
        const response = await apiRequest(`/productos/exceso-stock/${umbral}`);
        const productos = response._embedded?.productoInventarioDTOList || [];
        displayQueryResults(productos, 'Productos con Stock Excesivo');
    } catch (error) {
        showToast('Error al consultar productos con stock excesivo', 'error');
    }
}

function displayQueryResults(productos, title) {
    const container = document.getElementById('resultados-consulta');
    container.innerHTML = `<h4>${title}</h4>`;

    if (productos.length === 0) {
        container.innerHTML += '<p>No se encontraron productos.</p>';
        return;
    }

    const table = document.createElement('table');
    table.className = 'data-table';
    table.innerHTML = `
        <thead>
            <tr>
                <th>SKU</th>
                <th>Stock Disponible</th>
                <th>Stock Mínimo</th>
                <th>Ubicación</th>
            </tr>
        </thead>
        <tbody>
            ${productos.map(p => `
                <tr>
                    <td>${p.sku}</td>
                    <td>${p.cantidadDisponible || 0}</td>
                    <td>${p.cantidadMinimaStock || 0}</td>
                    <td>${p.ubicacionAlmacen || '-'}</td>
                </tr>
            `).join('')}
        </tbody>
    `;
    
    container.appendChild(table);
}

// Movement Functions
async function searchMovements() {
    const skuRaw = document.getElementById('movimientos-sku').value;
    const sku = skuRaw.trim().toUpperCase(); // O solo trim() si no quieres forzar mayúsculas
    
    if (!sku) {
        showToast('Por favor ingrese un SKU para buscar', 'warning');
        return;
    }

    try {
        const response = await apiRequest(`/movimientos/${sku}`);
        movimientos = response._embedded?.movimientoStockDTOList || [];
        renderMovementsTable();
    } catch (error) {
        showToast('Error al buscar movimientos', 'error');
        movimientos = [];
        renderMovementsTable();
    }
}

function renderMovementsTable() {
    const tbody = document.querySelector('#tabla-movimientos tbody');
    tbody.innerHTML = '';

    if (movimientos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No se encontraron movimientos</td></tr>';
        return;
    }

    movimientos.forEach(movimiento => {
        const row = document.createElement('tr');
        const fecha = new Date(movimiento.fechaMovimiento).toLocaleString();
        
        row.innerHTML = `
            <td>${movimiento.id}</td>
            <td>${movimiento.sku}</td>
            <td>${movimiento.tipoMovimiento}</td>
            <td>${movimiento.cantidadMovida}</td>
            <td>${movimiento.stockFinalDespuesMovimiento}</td>
            <td>${movimiento.motivo || '-'}</td>
            <td>${fecha}</td>
        `;
        tbody.appendChild(row);
    });
}

// Alert Functions
async function loadAlerts() {
    try {
        const response = await apiRequest('/umbrales');
        alertas = response._embedded?.umbralAlertaDTOList || [];
        renderAlertsTable();
    } catch (error) {
        console.error('Error loading alerts:', error);
        alertas = [];
        renderAlertsTable();
    }
}

function renderAlertsTable() {
    const tbody = document.querySelector('#tabla-alertas tbody');
    tbody.innerHTML = '';

    if (alertas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay alertas configuradas</td></tr>';
        return;
    }

    alertas.forEach(alerta => {
        const row = document.createElement('tr');
        const fecha = new Date(alerta.fechaCreacion).toLocaleDateString();
        
        row.innerHTML = `
            <td>${alerta.sku}</td>
            <td>${alerta.tipoAlerta?.replace('_', ' ') || '-'}</td>
            <td>${alerta.umbralCantidad}</td>
            <td><span class="status-badge ${alerta.activo ? 'disponible' : 'dado-de-baja'}">${alerta.activo ? 'Activo' : 'Inactivo'}</span></td>
            <td>${fecha}</td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn edit" onclick="editAlert('${alerta.sku}')">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn delete" onclick="deleteAlert('${alerta.sku}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function openAlertModal(alert = null) {
    editingAlert = alert;
    const modal = document.getElementById('modal-alerta');
    const title = document.getElementById('modal-alerta-title');
    const form = document.getElementById('form-alerta');

    title.textContent = alert ? 'Editar Alerta' : 'Nueva Alerta';
    
    if (alert) {
        document.getElementById('alerta-sku').value = alert.sku;
        document.getElementById('alerta-tipo').value = alert.tipoAlerta || '';
        document.getElementById('alerta-umbral').value = alert.umbralCantidad || 0;
        document.getElementById('alerta-activo').value = alert.activo ? 'true' : 'false';
        document.getElementById('alerta-sku').disabled = true;
    } else {
        form.reset();
        document.getElementById('alerta-sku').disabled = false;
    }

    modal.classList.add('active');
}

async function handleAlertSubmit(e) {
    e.preventDefault();
    
    const formData = {
        sku: document.getElementById('alerta-sku').value,
        tipoAlerta: document.getElementById('alerta-tipo').value,
        umbralCantidad: parseInt(document.getElementById('alerta-umbral').value),
        activo: document.getElementById('alerta-activo').value === 'true'
    };

    try {
        if (editingAlert) {
            await apiRequest(`/umbrales/${editingAlert.sku}`, {
                method: 'PUT',
                body: JSON.stringify(formData)
            });
            showToast('Alerta actualizada exitosamente', 'success');
        } else {
            await apiRequest('/umbrales', {
                method: 'POST',
                body: JSON.stringify(formData)
            });
            showToast('Alerta creada exitosamente', 'success');
        }
        
        closeModals();
        loadAlerts();
        if (currentSection === 'dashboard') {
            loadActiveAlerts();
        }
    } catch (error) {
        showToast('Error al guardar la alerta', 'error');
    }
}

async function editAlert(sku) {
    try {
        const response = await apiRequest(`/umbrales/${sku}`);
        openAlertModal(response);
    } catch (error) {
        showToast('Error al cargar la alerta', 'error');
    }
}

async function deleteAlert(sku) {
    if (!confirm('¿Está seguro de que desea eliminar esta alerta?')) {
        return;
    }

    try {
        await apiRequest(`/umbrales/${sku}`, { method: 'DELETE' });
        showToast('Alerta eliminada exitosamente', 'success');
        loadAlerts();
        if (currentSection === 'dashboard') {
            loadActiveAlerts();
        }
    } catch (error) {
        showToast('Error al eliminar la alerta', 'error');
    }
}

// Utility Functions
function showLoading(show) {
    const overlay = document.getElementById('loading-overlay');
    if (show) {
        overlay.classList.add('active');
    } else {
        overlay.classList.remove('active');
    }
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const iconMap = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        warning: 'fas fa-exclamation-triangle',
        info: 'fas fa-info-circle'
    };

    toast.innerHTML = `
        <div class="toast-content">
            <i class="toast-icon ${iconMap[type]}"></i>
            <span class="toast-message">${message}</span>
            <button class="toast-close">&times;</button>
        </div>
    `;

    // Add close functionality
    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.remove();
    });

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentNode) {
            toast.remove();
        }
    }, 5000);

    toastContainer.appendChild(toast);
}

function closeModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('active');
    });
    editingProduct = null;
    editingAlert = null;
}

// Global functions for onclick handlers
window.editProduct = editProduct;
window.deleteProduct = deleteProduct;
window.editAlert = editAlert;
window.deleteAlert = deleteAlert;


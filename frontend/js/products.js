document.addEventListener('DOMContentLoaded', () => {
    
    const tableBody = document.getElementById('product-table-body');
    const modal = document.getElementById('productModal');
    const addProductBtn = document.getElementById('addProductBtn');
    const closeBtn = modal.querySelector('.modal-close');
    const cancelBtn = modal.querySelector('.modal-cancel');
    const productForm = document.querySelector('#productModal form');

    // --- Modal Control ---
    const showModal = () => modal.classList.add('active');
    const hideModal = () => {
        modal.classList.remove('active');
        productForm.reset(); // Reset form fields when hiding
    };

    addProductBtn.addEventListener('click', showModal);
    closeBtn.addEventListener('click', hideModal);
    cancelBtn.addEventListener('click', hideModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) hideModal();
    });

    // --- API Functions ---
    async function loadProducts() {
        try {
            const products = await fetchWithAuth('/products');
            tableBody.innerHTML = '';
            window.mockProducts = products; // Make product data globally available

            products.forEach(product => {
                const row = `
                    <tr>
                        <td>${product.id}</td>
                        <td>${product.name}</td>
                        <td>₹${product.sellingPrice.toFixed(2)}</td>
                        <td>${product.categoryName}</td>
                        <td>${product.gstPercentage}%</td>
                    </tr>
                `;
                tableBody.innerHTML += row;
            });
        } catch (error) {
            // Error is already handled by fetchWithAuth
        }
    }

    // --- Form Submission ---
    productForm.addEventListener('submit', async (e) => {
        e.preventDefault(); // This is the crucial fix to prevent page reload
        
        const newProductData = {
            name: document.getElementById('productName').value,
            sellingPrice: document.getElementById('sellingPrice').value,
            categoryId: document.getElementById('productCategory').value,
        };

        try {
            await fetchWithAuth('/products', {
                method: 'POST',
                body: JSON.stringify(newProductData),
            });
            
            hideModal();
            loadProducts(); // Refresh the product list
            
        } catch (error) {
            alert(`Failed to add product: ${error.message}`);
        }
    });

    // --- Populate Categories ---
    const categorySelect = document.getElementById('productCategory');
    // This list now matches the data you inserted into your database
    const categories = [
        { id: 1, name: 'Daily Essentials' }, { id: 2, name: 'Dairy & Spreads' },
        { id: 3, name: 'Packaged Snacks' }, { id: 4, name: 'Electronics' },
        { id: 5, name: 'Hardware' }, { id: 6, name: 'Automotive Parts' },
        { id: 7, name: 'Apparel' }, { id: 8, name: 'Stationery' }
    ];
    categories.forEach(cat => {
        const option = document.createElement('option');
        option.value = cat.id;
        option.textContent = cat.name;
        categorySelect.appendChild(option);
    });

    // Initial load of products
    loadProducts();
});
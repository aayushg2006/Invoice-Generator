document.addEventListener('DOMContentLoaded', () => {
    // We assume mockProducts is available globally from products.js
    // In a real app, you'd fetch this from your API.
    const products = window.mockProducts; 

    const addItemBtn = document.getElementById('addItemBtn');
    const itemsTableBody = document.getElementById('invoice-items-body');
    const itemTemplate = document.getElementById('invoice-item-template');

    // Function to add a new item row
    function addNewItem() {
        const templateClone = itemTemplate.content.cloneNode(true);
        const newRow = templateClone.querySelector('tr');
        
        // Populate the product dropdown in the new row
        const selectElement = newRow.querySelector('.item-select');
        products.forEach(product => {
            const option = document.createElement('option');
            option.value = product.id;
            option.textContent = product.name;
            option.dataset.price = product.selling_price;
            selectElement.appendChild(option);
        });
        
        // Add the new row to the table
        itemsTableBody.appendChild(newRow);
        
        // Trigger a change event to set the initial price
        selectElement.dispatchEvent(new Event('change'));
    }

    // Function to calculate all totals
    function calculateTotals() {
        let subtotal = 0;
        const rows = itemsTableBody.querySelectorAll('tr');
        
        rows.forEach(row => {
            const price = parseFloat(row.querySelector('.item-price').value) || 0;
            const qty = parseInt(row.querySelector('.item-qty').value) || 0;
            const total = price * qty;
            row.querySelector('.item-total').textContent = `₹${total.toFixed(2)}`;
            subtotal += total;
        });

        const gst = subtotal * 0.05; // Assuming a flat 5% GST for now
        const grandTotal = subtotal + gst;

        document.getElementById('subtotal').textContent = `₹${subtotal.toFixed(2)}`;
        document.getElementById('gst').textContent = `₹${gst.toFixed(2)}`;
        document.getElementById('grandTotal').textContent = `₹${grandTotal.toFixed(2)}`;
    }

    // --- Event Listeners ---
    
    // Add item button
    addItemBtn.addEventListener('click', addNewItem);
    
    // Listen for changes within the table body for delegation
    itemsTableBody.addEventListener('change', (e) => {
        // If a product is selected from dropdown
        if (e.target.classList.contains('item-select')) {
            const selectedOption = e.target.options[e.target.selectedIndex];
            const price = selectedOption.dataset.price || 0;
            const row = e.target.closest('tr');
            row.querySelector('.item-price').value = parseFloat(price).toFixed(2);
        }
        // Recalculate if any select or quantity input changes
        calculateTotals();
    });

    // Listen for quantity input changes
    itemsTableBody.addEventListener('input', (e) => {
        if (e.target.classList.contains('item-qty')) {
            calculateTotals();
        }
    });

    // Listen for delete button clicks
    itemsTableBody.addEventListener('click', (e) => {
        if (e.target.closest('.btn-delete')) {
            e.target.closest('tr').remove();
            calculateTotals();
        }
    });

    // Set today's date
    document.getElementById('issueDate').valueAsDate = new Date();
    
    // Add one item row by default when the page loads
    addNewItem();
});
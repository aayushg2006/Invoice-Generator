document.addEventListener('DOMContentLoaded', async () => {
    const itemsTableBody = document.getElementById('invoice-items-body');
    const itemTemplate = document.getElementById('invoice-item-template');
    const addItemBtn = document.getElementById('addItemBtn');
    const customerInput = document.getElementById('customerName');
    const customerDataList = document.getElementById('customer-list');
    const statusSelect = document.getElementById('paymentStatus');
    const invoiceForm = document.getElementById('invoiceForm');
    let products = [];
    let customers = [];
    
    // Fetch products and customers to populate dropdowns
    try {
        products = await fetchWithAuth('/products');
        customers = await fetchWithAuth('/customers');

        // Populate the customer datalist for autocomplete
        customers.forEach(customer => {
            const option = document.createElement('option');
            option.value = customer.name;
            option.dataset.id = customer.id;
            customerDataList.appendChild(option);
        });
    } catch (error) {
        // Errors are handled in the fetchWithAuth function
    }

    function addNewItem() {
        const newRow = itemTemplate.content.cloneNode(true);
        const selectElement = newRow.querySelector('.item-select');
        selectElement.innerHTML = '<option value="">Select a product</option>';
        products.forEach(product => {
            const option = document.createElement('option');
            option.value = product.id;
            option.textContent = product.name;
            option.dataset.price = product.sellingPrice;
            selectElement.appendChild(option);
        });
        itemsTableBody.appendChild(newRow);
    }

    function calculateTotals() {
        let subtotal = 0;
        let totalGst = 0;
        itemsTableBody.querySelectorAll('tr').forEach(row => {
            const price = parseFloat(row.querySelector('.item-price').value) || 0;
            const qty = parseInt(row.querySelector('.item-qty').value) || 0;
            const selectedProductId = row.querySelector('.item-select').value;
            const product = products.find(p => p.id == selectedProductId);
            
            if (product) {
                const itemSubtotal = price * qty;
                const gstRate = product.gstPercentage / 100;
                const itemGst = itemSubtotal * gstRate;

                subtotal += itemSubtotal;
                totalGst += itemGst;

                row.querySelector('.item-total').textContent = `₹${(itemSubtotal).toFixed(2)}`;
            }
        });
        
        document.getElementById('subtotal').textContent = `₹${subtotal.toFixed(2)}`;
        document.getElementById('gst').textContent = `₹${totalGst.toFixed(2)}`;
        document.getElementById('grandTotal').textContent = `₹${(subtotal + totalGst).toFixed(2)}`;
    }

    // --- All Event Listeners ---
    addItemBtn.addEventListener('click', addNewItem);

    itemsTableBody.addEventListener('change', (e) => {
        if (e.target.classList.contains('item-select')) {
            const price = e.target.options[e.target.selectedIndex].dataset.price || 0;
            e.target.closest('tr').querySelector('.item-price').value = parseFloat(price).toFixed(2);
        }
        calculateTotals();
    });

    itemsTableBody.addEventListener('input', (e) => {
        if (e.target.classList.contains('item-qty')) calculateTotals();
    });

    itemsTableBody.addEventListener('click', (e) => {
        if (e.target.closest('.btn-delete')) {
            e.target.closest('tr').remove();
            calculateTotals();
        }
    });

    // --- FORM SUBMISSION LOGIC ---
    invoiceForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const items = [];
        itemsTableBody.querySelectorAll('tr').forEach(row => {
            const productId = row.querySelector('.item-select').value;
            if (productId) {
                items.push({
                    productId: productId,
                    quantity: row.querySelector('.item-qty').value
                });
            }
        });

        if (items.length === 0) {
            alert('Please add at least one item to the invoice.');
            return;
        }

        const invoiceData = {
            status: statusSelect.value,
            items: items
        };

        const enteredCustomerName = customerInput.value;
        const existingCustomer = customers.find(c => c.name.toLowerCase() === enteredCustomerName.toLowerCase());

        if (existingCustomer) {
            invoiceData.customerId = existingCustomer.id;
        } else {
            invoiceData.newCustomerName = enteredCustomerName;
        }

        try {
            // Use a raw fetch call to handle the file download response
            const token = getToken(); // getToken() is from api.js
            const response = await fetch(`${API_BASE_URL}/invoices`, { // API_BASE_URL is from api.js
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(invoiceData)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Failed to create invoice: ${errorText}`);
            }

            // --- PDF DOWNLOAD HANDLING ---
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            
            const contentDisposition = response.headers.get('content-disposition');
            let fileName = 'invoice.pdf';
            if (contentDisposition) {
                const fileNameMatch = contentDisposition.match(/filename="(.+)"/);
                if (fileNameMatch && fileNameMatch.length === 2)
                    fileName = fileNameMatch[1];
            }
            
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            a.remove();
            
            alert('Invoice created and downloaded successfully!');
            window.location.href = 'invoices-list.html';

        } catch (error) {
            alert(error.message);
        }
    });

    // --- Initial Page Setup ---
    document.getElementById('issueDate').valueAsDate = new Date();
    addNewItem();
});
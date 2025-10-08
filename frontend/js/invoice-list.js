document.addEventListener('DOMContentLoaded', async () => {
    const tableBody = document.getElementById('invoices-table-body');
    const searchInput = document.getElementById('searchInput');
    let allInvoices = [];

    async function loadInvoices() {
        try {
            allInvoices = await fetchWithAuth('/invoices');
            renderInvoices(allInvoices);
        } catch (error) {
            // Error is handled by fetchWithAuth
        }
    }

    function renderInvoices(invoicesToRender) {
        tableBody.innerHTML = '';
        if (invoicesToRender.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No invoices found.</td></tr>`;
            return;
        }
        invoicesToRender.forEach(invoice => {
            const statusClass = getStatusClass(invoice.status);
            const row = `
                <tr>
                    <td>${invoice.invoiceNumber}</td>
                    <td>${invoice.customerName}</td>
                    <td>${new Date(invoice.issueDate).toLocaleDateString()}</td>
                    <td>₹${invoice.totalAmount.toFixed(2)}</td>
                    <td><span class="status-badge ${statusClass}">${invoice.status}</span></td>
                    <td class="table-actions">
                        <a href="invoice-detail.html?id=${invoice.id}" title="View Details" class="action-icon">
                            <i class="fas fa-eye"></i>
                        </a>
                        <select class="status-dropdown" data-invoice-id="${invoice.id}">
                            <option value="PENDING" ${invoice.status === 'PENDING' ? 'selected' : ''}>Pending</option>
                            <option value="PAID" ${invoice.status === 'PAID' ? 'selected' : ''}>Paid</option>
                            <option value="CANCELLED" ${invoice.status === 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                        </select>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    }

    function getStatusClass(status) {
        if (status === 'PAID') return 'status-paid';
        if (status === 'PENDING') return 'status-pending';
        return 'status-overdue'; // For CANCELLED
    }

    searchInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();
        const filteredInvoices = allInvoices.filter(invoice =>
            (invoice.customerName && invoice.customerName.toLowerCase().includes(searchTerm)) ||
            (invoice.invoiceNumber && invoice.invoiceNumber.toLowerCase().includes(searchTerm))
        );
        renderInvoices(filteredInvoices);
    });
    
    tableBody.addEventListener('change', async (e) => {
        if (e.target.classList.contains('status-dropdown')) {
            const invoiceId = e.target.dataset.invoiceId;
            const newStatus = e.target.value;

            try {
                await fetchWithAuth(`/invoices/${invoiceId}/status?status=${newStatus}`, {
                    method: 'PUT'
                });
                // Find the invoice in our local array and update its status
                const updatedInvoice = allInvoices.find(inv => inv.id == invoiceId);
                if (updatedInvoice) {
                    updatedInvoice.status = newStatus;
                }
                // Re-render the table with the updated data
                renderInvoices(allInvoices);

            } catch (error) {
                alert(`Failed to update status: ${error.message}`);
                // If the API call fails, reload from the server to revert any visual changes
                loadInvoices();
            }
        }
    });

    // Initial load
    loadInvoices();
});
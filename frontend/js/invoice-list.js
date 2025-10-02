document.addEventListener('DOMContentLoaded', () => {

    const mockInvoices = [
        { id: 'INV-001', customer: 'Rohan Sharma', issue_date: '2025-09-01', amount: 550.00, status: 'Paid' },
        { id: 'INV-002', customer: 'Priya Patel', issue_date: '2025-09-03', amount: 1200.50, status: 'Pending' },
        { id: 'INV-003', customer: 'Amit Singh', issue_date: '2025-09-05', amount: 875.00, status: 'Paid' },
        { id: 'INV-004', customer: 'Sneha Reddy', issue_date: '2025-09-08', amount: 2500.00, status: 'Overdue' },
        { id: 'INV-005', customer: 'Vikram Kumar', issue_date: '2025-09-10', amount: 350.75, status: 'Pending' }
    ];

    const tableBody = document.getElementById('invoices-table-body');
    const searchInput = document.getElementById('searchInput');

    // Function to render the invoices in the table
    function renderInvoices(invoicesToRender) {
        tableBody.innerHTML = ''; // Clear existing rows

        if (invoicesToRender.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No invoices found.</td></tr>`;
            return;
        }

        invoicesToRender.forEach(invoice => {
            const statusClass = getStatusClass(invoice.status);
            const row = `
                <tr>
                    <td>${invoice.id}</td>
                    <td>${invoice.customer}</td>
                    <td>${new Date(invoice.issue_date).toLocaleDateString()}</td>
                    <td>₹${invoice.amount.toFixed(2)}</td>
                    <td><span class="status-badge ${statusClass}">${invoice.status}</span></td>
                    <td class="table-actions">
                        <a href="#" title="View"><i class="fas fa-eye"></i></a>
                        <a href="#" title="Edit"><i class="fas fa-pen"></i></a>
                        <a href="#" title="Delete"><i class="fas fa-trash"></i></a>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    }

    // Helper function to get the CSS class for a status
    function getStatusClass(status) {
        if (status === 'Paid') return 'status-paid';
        if (status === 'Pending') return 'status-pending';
        // You'll need to add CSS for 'status-overdue'
        if (status === 'Overdue') return 'status-overdue'; 
        return '';
    }
    
    // Event listener for the search input
    searchInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();
        
        const filteredInvoices = mockInvoices.filter(invoice => 
            invoice.customer.toLowerCase().includes(searchTerm) ||
            invoice.id.toLowerCase().includes(searchTerm)
        );
        
        renderInvoices(filteredInvoices);
    });

    // Initial render of all invoices
    renderInvoices(mockInvoices);
});
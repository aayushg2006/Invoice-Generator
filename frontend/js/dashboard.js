document.addEventListener('DOMContentLoaded', () => {

    // Mock data for the dashboard
    const mockInvoices = [
        { id: 'INV-001', customer: 'Rohan Sharma', amount: 550.00, status: 'Paid' },
        { id: 'INV-002', customer: 'Priya Patel', amount: 1200.50, status: 'Pending' },
        { id: 'INV-003', customer: 'Amit Singh', amount: 875.00, status: 'Paid' },
        { id: 'INV-004', customer: 'Sneha Reddy', amount: 2500.00, status: 'Pending' },
    ];

    // Function to populate the stats cards
    function populateStats() {
        const totalRevenue = mockInvoices
            .filter(inv => inv.status === 'Paid')
            .reduce((sum, inv) => sum + inv.amount, 0);

        const invoicesDue = mockInvoices.filter(inv => inv.status === 'Pending').length;
        const invoicesPaid = mockInvoices.filter(inv => inv.status === 'Paid').length;

        document.getElementById('total-revenue').textContent = `₹${totalRevenue.toFixed(2)}`;
        document.getElementById('invoices-due').textContent = invoicesDue;
        document.getElementById('invoices-paid').textContent = invoicesPaid;
    }

    // Function to populate the recent invoices table
    function populateRecentInvoices() {
        const tableBody = document.getElementById('recent-invoices-body');
        tableBody.innerHTML = ''; // Clear existing rows

        mockInvoices.forEach(invoice => {
            const statusClass = invoice.status === 'Paid' ? 'status-paid' : 'status-pending';
            const row = `
                <tr>
                    <td>${invoice.id}</td>
                    <td>${invoice.customer}</td>
                    <td>₹${invoice.amount.toFixed(2)}</td>
                    <td><span class="status-badge ${statusClass}">${invoice.status}</span></td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    }

    // Call the functions to load the dashboard data
    populateStats();
    populateRecentInvoices();
});
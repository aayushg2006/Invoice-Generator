document.addEventListener('DOMContentLoaded', () => {
    const mockCustomers = [
        { id: 1, name: 'Rohan Sharma', phone: '9876543210', email: 'rohan.sharma@example.com' },
        { id: 2, name: 'Priya Patel', phone: '9876543211', email: 'priya.patel@example.com' },
        { id: 3, name: 'Amit Singh', phone: '9876543212', email: 'amit.singh@example.com' },
        { id: 4, name: 'Sneha Reddy', phone: '9876543213', email: 'sneha.reddy@example.com' }
    ];

    const tableBody = document.getElementById('customers-table-body');

    // --- Modal Control ---
    const modal = document.getElementById('customerModal');
    const addCustomerBtn = document.getElementById('addCustomerBtn');
    const closeBtn = modal.querySelector('.modal-close');
    const cancelBtn = modal.querySelector('.modal-cancel');

    const showModal = () => modal.classList.add('active');
    const hideModal = () => modal.classList.remove('active');

    addCustomerBtn.addEventListener('click', showModal);
    closeBtn.addEventListener('click', hideModal);
    cancelBtn.addEventListener('click', hideModal);
    // Hide modal if user clicks on the overlay
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            hideModal();
        }
    });

    // --- Render Table ---
    function renderCustomers() {
        tableBody.innerHTML = '';
        mockCustomers.forEach(customer => {
            const row = `
                <tr>
                    <td>${customer.name}</td>
                    <td>${customer.phone}</td>
                    <td>${customer.email}</td>
                    <td class="table-actions">
                        <a href="#" title="Edit"><i class="fas fa-pen"></i></a>
                        <a href="#" title="Delete"><i class="fas fa-trash"></i></a>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    }

    // Initial render of the customers
    renderCustomers();
});
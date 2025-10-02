// This file remains exactly the same!

const mockProducts = [
  { id: 1, name: 'Parle-G Biscuit 50g', selling_price: 5.00, category: { category_name: 'Daily Essentials', gst_percentage: 5.00 }},
  { id: 2, name: 'Amul Butter 100g', selling_price: 55.00, category: { category_name: 'Dairy & Spreads', gst_percentage: 5.00 }},
  { id: 3, name: 'Lays Classic Chips', selling_price: 20.00, category: { category_name: 'Packaged Snacks', gst_percentage: 12.00 }}
];

function renderProducts() {
    const tableBody = document.getElementById('product-table-body');
    tableBody.innerHTML = ''; 

    mockProducts.forEach(product => {
        const row = `
            <tr>
                <td>${product.id}</td>
                <td>${product.name}</td>
                <td>₹${product.selling_price.toFixed(2)}</td>
                <td>${product.category.category_name}</td>
                <td>${product.category.gst_percentage}%</td>
            </tr>
        `;
        tableBody.innerHTML += row;
    });
}

window.onload = renderProducts;
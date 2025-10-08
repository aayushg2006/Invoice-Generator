document.addEventListener('DOMContentLoaded', async () => {
    const shopNameInput = document.getElementById('shopName');
    const gstinInput = document.getElementById('shopGstin');
    const addressTextarea = document.getElementById('shopAddress');
    const settingsForm = document.getElementById('settingsForm');
    const logoUploadInput = document.getElementById('logoUpload');
    const logoPreview = document.getElementById('logoPreview');
    let selectedLogoFile = null; // Variable to store the selected file object

    // --- Function to load current shop settings from the backend ---
    async function loadSettings() {
        try {
            const shop = await fetchWithAuth('/settings');
            shopNameInput.value = shop.shopName || '';
            gstinInput.value = shop.gstin || '';
            addressTextarea.value = shop.address || '';
            if (shop.logoPath) {
                // Construct the full URL to the logo on the backend and add a timestamp to prevent caching
                logoPreview.src = `http://localhost:8080${shop.logoPath}?t=${new Date().getTime()}`;
            }
        } catch (error) {
            // Error is handled by the fetchWithAuth function
        }
    }

    // --- Event listener for when a new logo file is selected ---
    logoUploadInput.addEventListener('change', (event) => {
        const file = event.target.files[0];
        if (file) {
            selectedLogoFile = file; // Store the actual file to be uploaded
            const reader = new FileReader();

            // Show a temporary preview of the new logo
            reader.onload = (e) => {
                logoPreview.src = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    });

    // --- Event listener for the main form submission ---
    settingsForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // --- Step 1: Upload the logo if a new one was selected ---
        if (selectedLogoFile) {
            const formData = new FormData();
            formData.append('logo', selectedLogoFile);

            try {
                // We use a separate fetch for file upload because it's not JSON
                const token = getToken();
                const response = await fetch('http://localhost:8080/api/files/uploadLogo', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`
                        // 'Content-Type' is set automatically by the browser for FormData
                    },
                    body: formData,
                });

                if (!response.ok) {
                    throw new Error('Logo upload failed! Please try again.');
                }
                selectedLogoFile = null; // Clear the file after successful upload

            } catch (error) {
                alert(error.message);
                return; // Stop the process if logo upload fails
            }
        }

        // --- Step 2: Save the rest of the text-based settings ---
        const settingsData = {
            shopName: shopNameInput.value,
            gstin: gstinInput.value,
            address: addressTextarea.value,
        };

        try {
            await fetchWithAuth('/settings', {
                method: 'PUT',
                body: JSON.stringify(settingsData),
            });
            alert('Settings updated successfully!');
            // Reload settings to get the latest data, including the new logo path from the server
            loadSettings(); 
        } catch (error) {
            alert(`Failed to update settings: ${error.message}`);
        }
    });

    // --- Initial load of settings when the page is opened ---
    loadSettings();
});
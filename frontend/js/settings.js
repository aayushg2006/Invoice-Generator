document.addEventListener('DOMContentLoaded', () => {

    const logoUploadInput = document.getElementById('logoUpload');
    const logoPreview = document.getElementById('logoPreview');

    // Event listener for the file input change
    logoUploadInput.addEventListener('change', (event) => {
        const file = event.target.files[0];

        if (file) {
            const reader = new FileReader();

            // This function runs when the reader has finished loading the file
            reader.onload = (e) => {
                // Set the src of the image preview to the result
                logoPreview.src = e.target.result;
            };

            // Start reading the file as a Data URL (a base64-encoded string)
            reader.readAsDataURL(file);
        }
    });
});
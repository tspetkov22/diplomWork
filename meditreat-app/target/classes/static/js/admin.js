document.addEventListener('DOMContentLoaded', function() {
    // Language switcher logic
    const langEnButton = document.getElementById('langEnButton');
    const langBgButton = document.getElementById('langBgButton');

    function setLanguage(lang) {
        localStorage.setItem('currentLanguage', lang);
        const currentSearchParams = new URLSearchParams(window.location.search);
        currentSearchParams.set('lang', lang);
        window.location.search = currentSearchParams.toString();
    }

    if (langEnButton) {
        langEnButton.addEventListener('click', () => setLanguage('en'));
    }
    if (langBgButton) {
        langBgButton.addEventListener('click', () => setLanguage('bg'));
    }

    const initialLang = new URLSearchParams(window.location.search).get('lang') || localStorage.getItem('currentLanguage') || 'en';
    if (initialLang === 'en') {
        if(langEnButton) langEnButton.classList.add('active');
        if(langBgButton) langBgButton.classList.remove('active');
    } else {
        if(langEnButton) langEnButton.classList.remove('active');
        if(langBgButton) langBgButton.classList.add('active');
    }
    if (!new URLSearchParams(window.location.search).get('lang')) {
        localStorage.setItem('currentLanguage', initialLang);
    }

    // Existing admin.js code
    let editingDoctorId = null; // Store ID of doctor being edited

    // Function to render doctors in the table
    function renderDoctors(doctors) {
        const tableBody = document.getElementById("doctorsTable").getElementsByTagName("tbody")[0];
        tableBody.innerHTML = ""; // Clear existing rows

        doctors.forEach(doctor => {
            const newRow = tableBody.insertRow();
            newRow.setAttribute('data-id', doctor.id); // Store doctor ID on the row
            newRow.innerHTML = `
                <td>${doctor.fullName}</td>
                <td>${doctor.email}</td>
                <td>${doctor.specialization}</td>
                <td>${doctor.licenseNumber}</td>
                <td>
                    <button class="btn btn-action edit-btn">Edit</button>
                    <button class="btn btn-action delete-btn">Delete</button>
                </td>
            `;
            attachEventListenersToRow(newRow); // Note: Renamed for clarity
        });
    }

    // Function to fetch and render doctors
    async function loadDoctors() {
        try {
            const response = await fetch('/api/doctors');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const doctors = await response.json();
            renderDoctors(doctors);
        } catch (error) {
            console.error("Could not load doctors:", error);
            // Display an error message to the user on the page if appropriate
            alert("Error loading doctors. Please try again later.");
        }
    }

    // Attach event listeners to buttons in a row
    function attachEventListenersToRow(row) {
        const doctorId = row.getAttribute('data-id');
        const doctorCells = row.cells;

        row.querySelector(".edit-btn").addEventListener("click", function () {
            editingDoctorId = doctorId;
            document.getElementById("modalTitle").innerText = "Edit Doctor Information";
            // Assuming doctor object is readily available or fetched again by ID if complex
            document.getElementById("fullName").value = doctorCells[0].innerText;
            document.getElementById("email").value = doctorCells[1].innerText;
            document.getElementById("specialization").value = doctorCells[2].innerText;
            document.getElementById("licenseNumber").value = doctorCells[3].innerText;
            new bootstrap.Modal(document.getElementById("doctorModal")).show();
        });

        row.querySelector(".delete-btn").addEventListener("click", async function () {
            if (confirm(`Are you sure you want to delete doctor ${doctorCells[0].innerText}?`)) {
                try {
                    const response = await fetch(`/api/doctors/${doctorId}`, {
                        method: 'DELETE'
                    });
                    if (response.ok) {
                        loadDoctors(); // Refresh the list
                        alert('Doctor deleted successfully.');
                    } else {
                        const errorText = await response.text();
                        alert(`Failed to delete doctor: ${errorText || response.status}`);
                    }
                } catch (error) {
                    console.error('Error deleting doctor:', error);
                    alert('An error occurred while deleting the doctor.');
                }
            }
        });
    }

    // Open "Add Doctor" modal
    document.getElementById("openAddModal").addEventListener("click", function () {
        editingDoctorId = null; // Clear editing ID
        document.getElementById("modalTitle").innerText = "Add New Doctor";
        document.getElementById("doctorForm").reset();
        // Note: Admin add doctor form does not have password. We'll handle this in saveDoctor.
        new bootstrap.Modal(document.getElementById("doctorModal")).show();
    });

    // Save doctor (add new or update existing)
    document.getElementById("saveDoctor").addEventListener("click", async function () {
        const fullName = document.getElementById("fullName").value;
        const email = document.getElementById("email").value;
        const specialization = document.getElementById("specialization").value;
        const licenseNumber = document.getElementById("licenseNumber").value;

        if (!fullName || !email || !specialization || !licenseNumber) {
            alert("Please fill in all fields.");
            return;
        }

        let doctorData = {
            fullName: fullName,
            email: email,
            specialization: specialization,
            licenseNumber: licenseNumber
        };

        let url = '/api/doctors';
        let method = 'POST';

        if (editingDoctorId) { // If editing an existing doctor
            url += `/${editingDoctorId}`;
            method = 'PUT';
            // Password is not updated through this admin flow for existing doctors
            // If it were, it would need careful handling (e.g. separate 'change password' feature)
        } else { // Adding a new doctor
            // For adding via admin, we send a default password. This is NOT secure for production.
            // The doctor should be prompted to change it on first login.
            doctorData.password = "Password123!"; // Temporary default password
            url += '/register'; // Use the registration endpoint for admin adding new doctor
                               // Or create a dedicated POST /api/doctors if different logic is needed
        }

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(doctorData)
            });

            const modalInstance = bootstrap.Modal.getInstance(document.getElementById("doctorModal"));
            modalInstance.hide();

            if (response.ok) {
                loadDoctors(); // Refresh the list
                alert(editingDoctorId ? 'Doctor updated successfully!' : 'Doctor added successfully!');
                editingDoctorId = null; // Reset editing ID
                document.getElementById("doctorForm").reset();
            } else {
                const errorResult = await response.text();
                alert((editingDoctorId ? 'Update' : 'Add') + ' doctor failed: ' + errorResult);
            }
        } catch (error) {
            console.error('Error saving doctor:', error);
            alert('An error occurred while saving doctor data. Please try again.');
            const modalInstance = bootstrap.Modal.getInstance(document.getElementById("doctorModal"));
            modalInstance.hide();
        }
    });

    // Logout functionality - this was redirecting to login.html, ensure it uses / if that's the root for login page
    // The HTML link <a href="/" ...> should handle this now.
    // document.getElementById("logoutButton").addEventListener("click", function () {
    //     window.location.href = "/"; 
    // });

    // Initial load of doctors when the page is ready
    loadDoctors();

    // Logout functionality for admin page
    const adminLogoutButton = document.getElementById('logoutButton'); 
    if (adminLogoutButton) {
        adminLogoutButton.addEventListener('click', function() {
            localStorage.removeItem('loggedInDoctor');
            localStorage.removeItem('userRole'); // Specifically clear admin role marker
            // The link's href will handle the redirect to '/'
            alert('You have been logged out.'); // Optional
        });
    }
});

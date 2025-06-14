document.addEventListener('DOMContentLoaded', function() {
    // Language switcher logic
    const langEnButton = document.getElementById('langEnButton');
    const langBgButton = document.getElementById('langBgButton');

    function activateLang(btn) {
        langEnButton.classList.remove('lang-active');
        langBgButton.classList.remove('lang-active');
         btn.classList.add('lang-active');
     }

    function setLanguage(lang) {
        localStorage.setItem('currentLanguage', lang);
        const currentSearchParams = new URLSearchParams(window.location.search);
        currentSearchParams.set('lang', lang);
        window.location.search = currentSearchParams.toString();
    }

    if (langEnButton) {
        langEnButton.addEventListener('click', () => {activateLang(langEnButton); setLanguage('en')});
    }
    if (langBgButton) {
        langBgButton.addEventListener('click', () => {activateLang(langBgButton); setLanguage('bg')});
    }

    const initialLang = new URLSearchParams(window.location.search).get('lang') || localStorage.getItem('currentLanguage') || 'en';
    if (initialLang === 'bg') {
    activateLang(langBgButton);
    } else {
    activateLang(langEnButton);
    }
    // if (initialLang === 'en') {
    //     if(langEnButton) langEnButton.classList.add('active');
    //     if(langBgButton) langBgButton.classList.remove('active');
    // } else {
    //     if(langEnButton) langEnButton.classList.remove('active');
    //     if(langBgButton) langBgButton.classList.add('active');
    // }
    if (!new URLSearchParams(window.location.search).get('lang')) {
        localStorage.setItem('currentLanguage', initialLang);
    }

    const loggedInDoctorString = localStorage.getItem('loggedInDoctor');

    if (loggedInDoctorString) {
        try {
            const doctor = JSON.parse(loggedInDoctorString);
            
            // Populate profile page fields
            document.getElementById('name').value = doctor.fullName || 'N/A';
            document.getElementById('email').value = doctor.email || 'N/A';
            document.getElementById('specialty').value = doctor.specialization || 'N/A';
            document.getElementById('license').value = doctor.licenseNumber || 'N/A';

            // If you have other elements to populate, do it here.
            // For example, if there's a welcome message: 
            // const welcomeMessageElement = document.getElementById('welcomeMessage');
            // if (welcomeMessageElement) {
            //     welcomeMessageElement.textContent = `Welcome, ${doctor.fullName}!`;
            // }

        } catch (error) {
            console.error("Error parsing stored doctor data:", error);
            // Optionally, clear corrupted data and redirect to login
            localStorage.removeItem('loggedInDoctor');
            // window.location.href = '/'; // Redirect to login
            alert("Error loading profile data. Please log in again.");
        }
    } else {
        // No logged-in doctor found in localStorage, redirect to login page
        // alert("You are not logged in. Redirecting to login page."); // Removed alert
        window.location.href = '/'; // Adjust if your login path is different
        return; // Stop further script execution
    }

    // Logout functionality
    const logoutButton = document.getElementById('logoutButton'); 
    if (logoutButton) {
        logoutButton.addEventListener('click', function(event) {
            // Prevent default link behavior if we want to ensure localStorage clear happens first,
            // though href='/' will navigate away anyway.
            // event.preventDefault(); 
            
            localStorage.removeItem('loggedInDoctor');
            localStorage.removeItem('userRole');
            // The link's href will handle the redirect to '/'
            // window.location.href = '/'; // This line is redundant if href='/' is on the anchor
            alert('You have been logged out.'); // Optional: notify user
        });
    }
}); 
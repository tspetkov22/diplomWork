document.addEventListener('DOMContentLoaded', function() {
    // Language switcher logic
    const btnEn = document.getElementById('langEnButton');
    const btnBg = document.getElementById('langBgButton');

    function activate(button) {
        btnEn.classList.remove('lang-active');
        btnBg.classList.remove('lang-active');
        button.classList.add('lang-active');
    }

    function setLanguage(lang) {
        localStorage.setItem('currentLanguage', lang);
        const currentSearchParams = new URLSearchParams(window.location.search);
        currentSearchParams.set('lang', lang);
        window.location.search = currentSearchParams.toString();
        // Button active state will be set on page reload by the initialization logic below
    }

    if (langEnButton) {
        langEnButton.addEventListener('click', () => { activate(btnEn); setLanguage('en')});
    }
    if (langBgButton) {
        langBgButton.addEventListener('click', () => { activate(btnBg); setLanguage('bg')});
    }

    // Initialize button states on load based on current URL param or localStorage
    // const initialLang = new URLSearchParams(window.location.search).get('lang') || localStorage.getItem('currentLanguage') || 'en';
    // if (initialLang === 'en') {
    //     if(langEnButton) langEnButton.classList.add('active');
    //     if(langBgButton) langBgButton.classList.remove('active');
    // } else {
    //     if(langEnButton) langEnButton.classList.remove('active');
    //     if(langBgButton) langBgButton.classList.add('active');
    // }

    const initialLang = new URLSearchParams(window.location.search).get('lang') || localStorage.getItem('currentLanguage')|| 'en';
    if (initialLang === 'bg') activate(btnBg);
    else activate(btnEn);
    // });

    // Store the initial language in localStorage if not already set by URL param
    if (!new URLSearchParams(window.location.search).get('lang')) {
        localStorage.setItem('currentLanguage', initialLang);
    }

    // Handle Login Form Submission
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            // Hardcoded admin login (as per original requirement, can be kept or removed if API handles all)
            if (email === 'admin@gmail.com' && password === 'admin') { // Assuming admin password is 'admin'
              // Store admin status or a simple marker if needed by admin.html
              localStorage.setItem('userRole', 'admin'); 
              localStorage.removeItem('loggedInDoctor'); // Clear any doctor login
              window.location.href = '/admin'; // Ensure admin.html is mapped or served correctly
              return;
            }

            try {
                const response = await fetch('/api/doctors/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email: email, password: password })
                });

                if (response.ok) {
                    const doctor = await response.json();
                    localStorage.setItem('loggedInDoctor', JSON.stringify(doctor)); // Store doctor object
                    localStorage.removeItem('userRole'); // Clear admin role
                    window.location.href = '/home'; 
                } else {
                    const errorText = await response.text();
                    alert('Login failed: ' + errorText);
                }
            } catch (error) {
                console.error('Error during login:', error);
                alert('An error occurred during login. Please try again.');
            }
        });
    }

    // Show Register Modal on Button Click
    const registerBtn = document.getElementById('registerBtn');
    if (registerBtn) {
        registerBtn.addEventListener('click', function () {
            const registerModal = new bootstrap.Modal(document.getElementById('registerModal'));
            registerModal.show();
        });
    }

    // Handle Register Form Submission
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async function (e) {
            e.preventDefault();

            // Assuming you will add a fullName field to your registration form in login.html
            // with id="register-fullName"
            const fullName = document.getElementById('register-fullName') ? document.getElementById('register-fullName').value : 'Default Name'; // Placeholder
            const email = document.getElementById('register-email').value;
            const password = document.getElementById('register-password').value;
            const specialty = document.getElementById('specialty').value;
            const licenseNumber = document.getElementById('license-number').value;

            const doctorData = {
                fullName: fullName, // Make sure your form has this field
                email: email,
                password: password, // Will be sent as plain text, ensure backend hashes it
                specialization: specialty,
                licenseNumber: licenseNumber
            };

            try {
                const response = await fetch('/api/doctors/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(doctorData)
                });

                const registerModalInstance = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
                registerModalInstance.hide();

                if (response.ok) {
                    // const result = await response.json(); // Contains the registered doctor
                    alert('Registration successful! You can now log in.'); 
                    // Optionally, clear form fields or redirect
                    document.getElementById('registerForm').reset();
                } else {
                    const errorResult = await response.text();
                    alert('Registration failed: ' + errorResult);
                }
            } catch (error) {
                console.error('Error during registration:', error);
                alert('An error occurred during registration. Please try again.');
                const registerModalInstance = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
                registerModalInstance.hide();
            }
        });
    }
});
  
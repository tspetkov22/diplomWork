document.addEventListener('DOMContentLoaded', function() {
    let currentSearchedSymptom = null; // Changed from currentSearchedIllness
    let currentTreatmentForEmail = null;
    let activeTreatmentList = [];
    let currentLanguage = localStorage.getItem('currentLanguage') || 'en';
    // let currentIllnessDetails = null; // Seems unused, replaced by currentSearchedSymptom
    let currentTreatmentId = null;

    const langEnButton = document.getElementById('langEnButton');
    const langBgButton = document.getElementById('langBgButton');

    // Setup jQuery AJAX to include Accept-Language header
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Accept-Language', currentLanguage);
        }
    });

    function updateButtonStates() {
      if (currentLanguage === 'en') {
          if(langEnButton) langEnButton.classList.add('active');
            langEnButton.style.backgroundColor = '#8CDBC1';
            langEnButton.style.color = '#fff';
          if(langBgButton) langBgButton.classList.remove('active');
            langBgButton.style.backgroundColor = '#fff';
            langBgButton.style.color = 'grey';
      } else {
          if(langEnButton) langEnButton.classList.remove('active');
            langEnButton.style.backgroundColor = '#fff';
            langEnButton.style.color = 'grey';
          if(langBgButton) langBgButton.classList.add('active');
            langBgButton.style.backgroundColor = '#8CDBC1';
            langBgButton.style.color = '#fff';
      }
    }

    function setLanguage(lang) {
        localStorage.setItem('currentLanguage', lang);
        currentLanguage = lang; // Update global variable
        $.ajaxSetup({ // Update ajaxSetup for new language
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Accept-Language', currentLanguage);
            }
        });
        updateButtonStates();
        // Reload translations for dynamic content if necessary, or re-fetch data
        // This might involve re-running searches or updating displayed text
        // For example, if a search result is on screen, re-fetch it or update its text fields.
        // The page reload is a simpler way if server-side rendering handles all text.
        const currentSearchParams = new URLSearchParams(window.location.search);
        currentSearchParams.set('lang', lang);
        window.location.search = currentSearchParams.toString(); // This will cause a page reload
    }

    if (langEnButton) langEnButton.addEventListener('click', () => setLanguage('en'));
    if (langBgButton) langBgButton.addEventListener('click', () => setLanguage('bg'));
    
    const initialLang = new URLSearchParams(window.location.search).get('lang') || localStorage.getItem('currentLanguage') || 'en';
    currentLanguage = initialLang; // Ensure currentLanguage is set on load
    updateButtonStates();
    if (!new URLSearchParams(window.location.search).get('lang')) {
        localStorage.setItem('currentLanguage', initialLang);
    }
    $.ajaxSetup({ // Initial ajaxSetup on load
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Accept-Language', currentLanguage);
        }
    });

    const loggedInDoctorString = localStorage.getItem('loggedInDoctor');
    if (loggedInDoctorString) {
        try {
            const doctor = JSON.parse(loggedInDoctorString);
            const doctorNameElement = document.getElementById('doctorName');
            if (doctorNameElement) doctorNameElement.textContent = doctor.fullName || 'N/A';
            const doctorSpecialtyElement = document.getElementById('doctorSpecialty');
            if (doctorSpecialtyElement) doctorSpecialtyElement.textContent = doctor.specialization || 'N/A';
        } catch (error) {
            console.error("Error parsing stored doctor data:", error);
            localStorage.removeItem('loggedInDoctor');
            localStorage.removeItem('userRole');
            window.location.href = '/'; 
            return;
        }
    } else {
        window.location.href = '/'; 
        return;
    }

    const illnessInput = $('#illnessInput'); // Use jQuery selector
    const resultContainer = document.getElementById('resultContainer');
    const resultTitle = document.getElementById('resultTitle');
    const resultDescription = document.getElementById('resultDescription'); // Ensure this is selected
    const treatmentButtons = document.querySelector('.treatment-buttons');
    const treatmentDetailModalLabel = document.getElementById('treatmentDetailModalLabel');
    const treatmentDetailContent = document.getElementById('treatmentDetailContent');
    const sendEmailButtonModal = document.getElementById('sendEmailButton');

    if(treatmentButtons) treatmentButtons.style.display = 'none';
    if(sendEmailButtonModal) sendEmailButtonModal.style.display = 'none';

    // Symptom Autocomplete
    illnessInput.autocomplete({
        source: function(request, response) {
            $.ajax({
                url: "/api/symptoms/search",
                dataType: "json",
                data: {
                    name: request.term
                },
                success: function(data) {
                    response($.map(data, function(item) {
                        return {
                            label: item.name, // Text to display in the suggestion list
                            value: item.name, // Value to put in the input field when selected
                            symptom: item    // Store the whole SymptomDto object
                        };
                    }));
                }
            });
        },
        minLength: 2,
        select: function(event, ui) {
            currentSearchedSymptom = ui.item.symptom; // Store the selected SymptomDto
            console.log("Symptom selected from autocomplete:", currentSearchedSymptom); // DEBUG
            if(resultContainer) resultContainer.style.display = 'block';
            if(resultTitle) resultTitle.innerText = currentSearchedSymptom.name; // Display symptom name
            
            if(resultDescription) { // Display symptom description
                console.log("Updating resultDescription with:", currentSearchedSymptom.description); // DEBUG
                resultDescription.innerText = currentSearchedSymptom.description || ''; 
                resultDescription.style.display = currentSearchedSymptom.description ? 'block' : 'none';
            } else {
                console.error("resultDescription element not found!"); // DEBUG
            }
            if(treatmentButtons) treatmentButtons.style.display = 'flex';
        }
    });

    const searchButton = document.getElementById('searchButton');
    if (searchButton) {
        searchButton.addEventListener('click', function () {
            const symptomName = illnessInput.val().trim(); 
            if (!symptomName) {
                alert(currentLanguage === 'en' ? "Please enter a symptom to search." : "Моля, въведете симптом за търсене.");
                return;
            }
            
            console.log("Search button clicked for symptom name:", symptomName); // DEBUG

            if (currentSearchedSymptom && currentSearchedSymptom.name.toLowerCase() === symptomName.toLowerCase()) {
                console.log("Using already selected symptom:", currentSearchedSymptom); // DEBUG
                if(resultContainer) resultContainer.style.display = 'block';
                if(resultTitle) resultTitle.innerText = currentSearchedSymptom.name;
                if(resultDescription) { 
                    console.log("Updating resultDescription with:", currentSearchedSymptom.description); // DEBUG
                    resultDescription.innerText = currentSearchedSymptom.description || '';
                    resultDescription.style.display = currentSearchedSymptom.description ? 'block' : 'none';
                } else {
                    console.error("resultDescription element not found!"); // DEBUG
                }
                if(treatmentButtons) treatmentButtons.style.display = 'flex';
            } else {
                console.log("Performing AJAX search for symptom:", symptomName); // DEBUG
                $.ajax({
                    url: "/api/symptoms/search",
                    dataType: "json",
                    data: { name: symptomName },
                    success: function(symptoms) {
                        console.log("AJAX search successful, symptoms found:", symptoms); // DEBUG
                        currentSearchedSymptom = null;
                        if (treatmentButtons) treatmentButtons.style.display = 'none';
                        if (resultDescription) resultDescription.style.display = 'none'; 
                        
                        if (symptoms && symptoms.length > 0) {
                            currentSearchedSymptom = symptoms[0]; 
                            console.log("Symptom found via AJAX:", currentSearchedSymptom); // DEBUG
                            if(resultContainer) resultContainer.style.display = 'block';
                            if(resultTitle) resultTitle.innerText = currentSearchedSymptom.name;
                            if(resultDescription) { 
                                console.log("Updating resultDescription with:", currentSearchedSymptom.description); // DEBUG
                                resultDescription.innerText = currentSearchedSymptom.description || '';
                                resultDescription.style.display = currentSearchedSymptom.description ? 'block' : 'none';
                            } else {
                                console.error("resultDescription element not found!"); // DEBUG
                            }
                            if(treatmentButtons) treatmentButtons.style.display = 'flex';
                        } else {
                            console.log("No symptom found via AJAX for:", symptomName); // DEBUG
                            if(resultContainer) resultContainer.style.display = 'block';
                            if(resultTitle) resultTitle.innerText = currentLanguage === 'en' ? 'No Symptom Found' : 'Не е намерен симптом';
                            if(resultDescription) {
                                resultDescription.innerText = currentLanguage === 'en' ? 'No symptom found matching your search criteria.' : 'Няма намерен симптом, отговарящ на вашите критерии за търсене.';
                                resultDescription.style.display = 'block';
                            } else {
                                console.error("resultDescription element not found!"); // DEBUG
                            }
                        }
                    },
                    error: function(xhr) {
                        console.error("Error searching symptom via AJAX:", xhr.responseText); // DEBUG
                        if(resultContainer) resultContainer.style.display = 'block';
                        if(resultTitle) resultTitle.innerText = currentLanguage === 'en' ? 'Search Error' : 'Грешка при търсene';
                        if(resultDescription) {
                            resultDescription.innerText = currentLanguage === 'en' ? 'Error searching for symptom. Please try again.' : 'Грешка при търсене на симптом. Моля, опитайте отново.';
                            resultDescription.style.display = 'block';
                        } else {
                            console.error("resultDescription element not found!"); // DEBUG
                        }
                    }
                });
            }
        });
    }

    function displayTreatmentDetails(treatmentDto) { // Expects a TreatmentDto
        if (!treatmentDto) {
            treatmentDetailContent.innerHTML = currentLanguage === 'en' ? '<p>Treatment details not found.</p>' : '<p>Детайли за лечението не са намерени.</p>';
            if(sendEmailButtonModal) sendEmailButtonModal.style.display = 'none';
            currentTreatmentForEmail = null;
            return;
        }
        currentTreatmentId = treatmentDto.id;
        currentTreatmentForEmail = treatmentDto;

        let detailsHtml = `<h4>${treatmentDto.name}</h4>`;
        if (treatmentDto.description) {
            detailsHtml += `<p><strong>${currentLanguage === 'en' ? 'Description' : 'Описание'}:</strong> ${treatmentDto.description}</p>`;
        }
        if (treatmentDto.usageInstructions) {
            detailsHtml += `<p><strong>${currentLanguage === 'en' ? 'Usage Instructions' : 'Инструкции за употреба'}:</strong> ${treatmentDto.usageInstructions}</p>`;
        }
        if (treatmentDto.recommendedDose) {
            detailsHtml += `<p><strong>${currentLanguage === 'en' ? 'Recommended Dose' : 'Препоръчителна доза'}:</strong> ${treatmentDto.recommendedDose}</p>`;
        }
        if (treatmentDto.symptoms && treatmentDto.symptoms.length > 0) {
            detailsHtml += `<p><strong>${currentLanguage === 'en' ? 'Associated Symptoms' : 'Свързани симптоми'}:</strong> `;
            detailsHtml += Array.from(treatmentDto.symptoms).map(s => s.name).join(', ');
            detailsHtml += `</p>`;
        }
        treatmentDetailContent.innerHTML = detailsHtml;
        if(sendEmailButtonModal) sendEmailButtonModal.style.display = 'block';
    }

    function displayTreatmentList(treatments, treatmentTypeName) {
        treatmentDetailContent.innerHTML = '';
        const ul = document.createElement('ul');
        ul.classList.add('list-group');

        if (treatmentDetailModalLabel && currentSearchedSymptom) {
            const typeDisplayName = treatmentTypeName.charAt(0).toUpperCase() + treatmentTypeName.slice(1);
            treatmentDetailModalLabel.innerText = `${typeDisplayName} ${currentLanguage === 'en' ? 'Treatments for' : 'Лечения за'} ${currentSearchedSymptom.name}`;
        }

        if (!treatments || treatments.length === 0) {
            const li = document.createElement('li');
            li.classList.add('list-group-item');
            li.textContent = currentLanguage === 'en' ? 'No treatments found for this type and symptom.' : 'Няма намерени лечения за този тип и симптом.';
            ul.appendChild(li);
        } else {
            treatments.forEach((treatment, index) => {
                const li = document.createElement('li');
                li.classList.add('list-group-item', 'list-group-item-action');
                li.textContent = treatment.name; // TreatmentDto has name directly for the correct language
                li.style.cursor = 'pointer';
                li.dataset.treatmentIndex = index; // Use index to get from activeTreatmentList
                li.addEventListener('click', function() {
                    const selectedIndex = parseInt(this.dataset.treatmentIndex);
                    const selectedTreatment = activeTreatmentList[selectedIndex]; 
                    if (treatmentDetailModalLabel && selectedTreatment.type) {
                        treatmentDetailModalLabel.innerText = `${selectedTreatment.type.name}: ${selectedTreatment.name}`;
                    }
                    displayTreatmentDetails(selectedTreatment);
                });
                ul.appendChild(li);
            });
        }
        treatmentDetailContent.appendChild(ul);
        if(sendEmailButtonModal) sendEmailButtonModal.style.display = 'none'; // Hide for list view
    }

    async function handleTreatmentTypeSelection(treatmentTypeName) {
        if (!currentSearchedSymptom || !currentSearchedSymptom.name) {
            alert(currentLanguage === 'en' ? "Please search for and select a symptom first." : "Моля, първо потърсете и изберете симптом.");
            return;
        }
        activeTreatmentList = [];
        currentTreatmentForEmail = null;
        if(sendEmailButtonModal) sendEmailButtonModal.style.display = 'none';

        try {
            // Add console log to debug
            console.log(`Searching treatments for symptom: ${currentSearchedSymptom.name}, type: ${treatmentTypeName}`);
            const response = await $.ajax({
                url: "/api/treatments/search",
                method: "GET",
                dataType: "json",
                data: {
                    symptomName: currentSearchedSymptom.name,
                    typeName: treatmentTypeName
                },
                // Handle 204 No Content responses
                statusCode: {
                    204: function() {
                        return []; // Return empty array for 204 No Content
                    }
                }
            });
            // Make sure we handle empty response or 204 status code
            activeTreatmentList = response || [];
            console.log('Received treatments:', activeTreatmentList);
            displayTreatmentList(activeTreatmentList, treatmentTypeName);
            var treatmentModal = new bootstrap.Modal(document.getElementById('treatmentDetailModal'));
            treatmentModal.show();

        } catch (error) {
            console.error("Error fetching treatments by type:", error);
            treatmentDetailContent.innerHTML = `<p>${currentLanguage === 'en' ? 'Error loading treatments. Please try again.' : 'Грешка при зареждането на лечения. Моля, опитайте отново.'}</p>`;
            if (treatmentDetailModalLabel) treatmentDetailModalLabel.innerText = currentLanguage === 'en' ? 'Error' : 'Грешка';
            var treatmentModal = new bootstrap.Modal(document.getElementById('treatmentDetailModal'));
            treatmentModal.show();
        }
    }

    // Add event listeners to treatment type buttons
    const typeButtons = document.querySelectorAll('.treatment-buttons .btn-custom');
    typeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const typeName = currentLanguage === 'en' ? 
                this.dataset.typeNameEn : 
                this.dataset.typeNameBg;
            if (typeName) {
                handleTreatmentTypeSelection(typeName);
            }
        });
    });

    // Send Email button click handler
    const sendEmailButton = document.getElementById('sendEmailButton');
    if (sendEmailButton) {
        sendEmailButton.addEventListener('click', function() {
            // Open the email modal
            const emailModal = new bootstrap.Modal(document.getElementById('emailModal'));
            emailModal.show();
        });
    }
    
    // Email form submission
    const emailForm = document.getElementById('emailForm');
    if (emailForm) {
        emailForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            const recipientEmail = document.getElementById('recipientEmail').value;
            if (!currentTreatmentForEmail || !currentTreatmentId) { // check currentTreatmentId as well
                alert(currentLanguage === 'en' ? 'No treatment selected or details are missing.' : 'Няма избрано лечение или липсват детайли.');
                return;
            }

            // Get the doctor information from localStorage
            const loggedInDoctorString = localStorage.getItem('loggedInDoctor');
            let doctorFullName = 'Your Doctor';
            
            if (loggedInDoctorString) {
                try {
                    const doctor = JSON.parse(loggedInDoctorString);
                    doctorFullName = doctor.fullName || 'Your Doctor';
                } catch (e) {
                    console.error('Error parsing doctor data:', e);
                }
            }
            
            const emailRequest = {
                patientEmail: recipientEmail,
                treatmentId: currentTreatmentId,
                language: currentLanguage,
                doctorFullName: doctorFullName
            };

            // Log the request to help with debugging
            console.log('Sending email request:', emailRequest);
            
            try {
                const response = await $.ajax({
                    url: '/api/treatments/send-email',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(emailRequest),
                });
                console.log('Email sent successfully, response:', response);
                alert(currentLanguage === 'en' ? 'Email sent successfully!' : 'Имейлът е изпратен успешно!');
                var emailModal = bootstrap.Modal.getInstance(document.getElementById('emailModal'));
                if(emailModal) emailModal.hide();
            } catch (error) {
                console.error('Error sending email:', error);
                let errorMessage = '';
                if (error.responseJSON && error.responseJSON.message) {
                    errorMessage = error.responseJSON.message;
                } else if (error.responseText) {
                    errorMessage = error.responseText;
                } else if (error.statusText) {
                    errorMessage = error.statusText;
                } else {
                    errorMessage = 'Unknown error. Check console for details.';
                }
                
                alert(currentLanguage === 'en' ? 
                    'Failed to send email. ' + errorMessage : 
                    'Неуспешно изпращане на имейл. ' + errorMessage);
            }
        });
    }
    
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', function(event) {
            event.preventDefault(); // Prevent default link behavior
            localStorage.removeItem('loggedInDoctor');
            localStorage.removeItem('userRole');
            localStorage.removeItem('currentLanguage'); // Also clear language preference on logout
            window.location.href = '/'; // Redirect to root path
        });
    }

    // Full text search functionality removed as requested
    
    // Full text search functionality removed as requested
});
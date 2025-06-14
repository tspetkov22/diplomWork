document.addEventListener('DOMContentLoaded', function() {
    const loggedInDoctorString = localStorage.getItem('loggedInDoctor');
    if (!loggedInDoctorString) {
        window.location.href = '/'; // Redirect to login if not logged in
        return;
    }

    let currentLanguage = document.getElementById('treatmentLanguage')?.value || localStorage.getItem('currentLanguage') || 'en';

    const langEnButton = document.getElementById('langEnButton');
    const langBgButton = document.getElementById('langBgButton');
    const addTreatmentForm = document.getElementById('addTreatmentForm');
    
    // Symptom search elements
    const symptomSearchInput = document.getElementById('symptomSearch');
    const searchSymptomsBtn = document.getElementById('searchSymptomsBtn');
    const symptomsDropdown = document.getElementById('symptomsDropdown');
    const selectedSymptomsContainer = document.getElementById('selectedSymptoms');
    const symptomsHiddenInput = document.getElementById('symptoms');
    
    // Selected symptoms array to store both existing and new symptoms
    let selectedSymptoms = [];

    // Setup jQuery AJAX to include Accept-Language header
    if (window.jQuery) {
        $.ajaxSetup({
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Accept-Language', currentLanguage);
            }
        });
    }
    
    function updateButtonStates() {
        if (currentLanguage === 'en') {
            if(langEnButton) langEnButton.classList.add('active');
            if(langBgButton) langBgButton.classList.remove('active')
                langBgButton.style.backgroundColor = '#fff';
                langBgButton.style.color = 'grey';
        } else {
            if(langEnButton) langEnButton.classList.remove('active');
            langEnButton.style.backgroundColor = '#fff';
            langEnButton.style.color = 'grey';
            if(langBgButton) langBgButton.classList.add('active');
        }
    }

    function setLanguage(lang) {
        localStorage.setItem('currentLanguage', lang);
        currentLanguage = lang;
        if (window.jQuery) {
            $.ajaxSetup({ // Update ajaxSetup for new language
                beforeSend: function(xhr) {
                    xhr.setRequestHeader('Accept-Language', currentLanguage);
                }
            });
        }
        // Update hidden field if it exists, for form submission consistency
        const hiddenLangField = document.getElementById('treatmentLanguage');
        if(hiddenLangField) hiddenLangField.value = lang;
        
        updateButtonStates();
        // Reload page to get new translations from Thymeleaf and new treatment types for the selected language
        const currentSearchParams = new URLSearchParams(window.location.search);
        currentSearchParams.set('lang', lang);
        window.location.search = currentSearchParams.toString();
    }

    if (langEnButton) langEnButton.addEventListener('click', () => setLanguage('en'));
    if (langBgButton) langBgButton.addEventListener('click', () => setLanguage('bg'));

    // Initialize based on URL param (from reload) or localStorage, then update hidden field
    const urlLang = new URLSearchParams(window.location.search).get('lang');
    if (urlLang) {
        currentLanguage = urlLang;
        localStorage.setItem('currentLanguage', urlLang);
    }
    const hiddenLangField = document.getElementById('treatmentLanguage');
    if (hiddenLangField && hiddenLangField.value !== currentLanguage) {
         // This case implies the JS variable might be out of sync with Thymeleaf-set value after page load
         // For add-treatment, currentLanguage from Thymeleaf via hidden field is primary for the form data language.
        currentLanguage = hiddenLangField.value;
        localStorage.setItem('currentLanguage', currentLanguage); // Sync localStorage too
    }
    updateButtonStates();
    // Ensure jQuery AJAX setup uses the most definitive current language after all checks
    if (window.jQuery) {
        $.ajaxSetup({
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Accept-Language', currentLanguage);
            }
        });
    }
    
    // Function to search for symptoms
    function searchSymptoms(query) {
        if (!query || query.trim() === '') {
            symptomsDropdown.style.display = 'none';
            return;
        }
        
        console.log(`Searching symptoms for: "${query}" in language: ${currentLanguage}`);
        fetch(`/api/symptoms/search?name=${encodeURIComponent(query)}`, {
            headers: {
                'Accept-Language': currentLanguage
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            
            // Handle empty response
            if (response.status === 204 || response.headers.get('content-length') === '0') {
                return [];
            }
            
            return response.json();
        })
        .then(data => {
            // Clear the dropdown
            symptomsDropdown.innerHTML = '';
            
            if (data && data.length > 0) {
                // Add each symptom to the dropdown
                data.forEach(symptom => {
                    // Check if this symptom is already selected
                    const isAlreadySelected = selectedSymptoms.some(
                        s => s.id === symptom.id || s.name === symptom.name
                    );
                    
                    if (!isAlreadySelected) {
                        const item = document.createElement('a');
                        item.classList.add('dropdown-item');
                        item.textContent = symptom.name;
                        item.dataset.id = symptom.id;
                        item.dataset.name = symptom.name;
                        
                        item.addEventListener('click', () => {
                            addSymptom(symptom);
                            symptomsDropdown.style.display = 'none';
                            symptomSearchInput.value = '';
                        });
                        
                        symptomsDropdown.appendChild(item);
                    }
                });
                
                // Add option to create a new symptom if query doesn't exactly match any existing symptom
                const exactMatch = data.some(s => s.name.toLowerCase() === query.toLowerCase());
                if (!exactMatch) {
                    const createNewItem = document.createElement('a');
                    createNewItem.classList.add('dropdown-item', 'text-primary');
                    createNewItem.textContent = `Create new: "${query}"`;
                    
                    createNewItem.addEventListener('click', () => {
                        addSymptom({ name: query });
                        symptomsDropdown.style.display = 'none';
                        symptomSearchInput.value = '';
                    });
                    
                    symptomsDropdown.appendChild(createNewItem);
                }
                
                symptomsDropdown.style.display = 'block';
            } else {
                // No results, show option to create new
                const createNewItem = document.createElement('a');
                createNewItem.classList.add('dropdown-item', 'text-primary');
                createNewItem.textContent = `Create new: "${query}"`;
                
                createNewItem.addEventListener('click', () => {
                    addSymptom({ name: query });
                    symptomsDropdown.style.display = 'none';
                    symptomSearchInput.value = '';
                });
                
                symptomsDropdown.appendChild(createNewItem);
                symptomsDropdown.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error searching symptoms:', error);
            symptomSearchInput.value = '';
            symptomsDropdown.style.display = 'none';
        });
    }
    
    // Function to add a symptom to the selected symptoms
    function addSymptom(symptom) {
        // Check if already selected
        if (selectedSymptoms.some(s => 
            (s.id && s.id === symptom.id) || 
            (!s.id && !symptom.id && s.name === symptom.name))) {
            return;
        }
        
        // Add to selected symptoms array
        selectedSymptoms.push({
            id: symptom.id || null,
            name: symptom.name,
            language: currentLanguage
        });
        
        // Create visual representation
        const tag = document.createElement('div');
        tag.classList.add('symptom-tag');
        
        const name = document.createElement('span');
        name.textContent = symptom.name;
        tag.appendChild(name);
        
        const removeBtn = document.createElement('span');
        removeBtn.classList.add('remove-symptom');
        removeBtn.innerHTML = '&times;';
        removeBtn.addEventListener('click', () => {
            // Remove from array
            selectedSymptoms = selectedSymptoms.filter(s => 
                (s.id !== symptom.id) || 
                (!s.id && !symptom.id && s.name !== symptom.name)
            );
            
            // Remove tag
            tag.remove();
            
            // Update hidden input
            updateSymptomsHiddenInput();
        });
        tag.appendChild(removeBtn);
        
        selectedSymptomsContainer.appendChild(tag);
        
        // Update hidden input with JSON data
        updateSymptomsHiddenInput();
    }
    
    // Function to update the hidden input with the selected symptoms
    function updateSymptomsHiddenInput() {
        symptomsHiddenInput.value = JSON.stringify(selectedSymptoms);
    }
    
    // Event listeners for symptom search
    if (symptomSearchInput) {
        symptomSearchInput.addEventListener('input', function() {
            searchSymptoms(this.value);
        });
        
        // Hide dropdown when clicking outside
        document.addEventListener('click', function(event) {
            if (!symptomSearchInput.contains(event.target) && !symptomsDropdown.contains(event.target)) {
                symptomsDropdown.style.display = 'none';
            }
        });
    }
    
    if (searchSymptomsBtn) {
        searchSymptomsBtn.addEventListener('click', function() {
            searchSymptoms(symptomSearchInput.value);
        });
    }

    if (addTreatmentForm) {
        addTreatmentForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            
            // Log the state of the form to see if the element is present at submission time
            console.log("addTreatmentForm HTML at submission:", addTreatmentForm.innerHTML);
            console.log("Attempting to find #treatmentLanguage directly:", document.getElementById('treatmentLanguage'));

            // const treatmentNameEn = document.getElementById('treatmentNameEn')?.value.trim() || ""; // Old
            // const treatmentNameBg = document.getElementById('treatmentNameBg')?.value.trim() || ""; // Old
            const treatmentNameValue = document.getElementById('treatmentName')?.value.trim() || "";
            
            const treatmentTypeElement = document.getElementById('treatmentType');
            const treatmentTypeId = treatmentTypeElement ? treatmentTypeElement.value : null;
            
            const descriptionElement = document.getElementById('description');
            const description = descriptionElement ? descriptionElement.value.trim() : "";
            
            const usageInstructionsElement = document.getElementById('usageInstructions');
            const usageInstructions = usageInstructionsElement ? usageInstructionsElement.value.trim() : "";
            
            const recommendedDoseElement = document.getElementById('recommendedDose');
            const recommendedDose = recommendedDoseElement ? recommendedDoseElement.value.trim() : "";
            
            const treatmentLanguageElement = document.getElementById('treatmentLanguage');
            if (!treatmentLanguageElement) {
                console.error("CRITICAL: Hidden input 'treatmentLanguage' not found in the DOM!");
                alert("A critical error occurred with form language. Please contact support. (TL_NF)");
                return; 
            }
            const formProcessingLanguage = treatmentLanguageElement.value;

            // if (!treatmentNameEn && !treatmentNameBg) { // Old validation
            if (!treatmentNameValue) { // New validation
                alert(currentLanguage === 'en' ? 'Please fill in the Treatment Name.' : 'Моля, попълнете име на лечението.');
                return;
            }
            if (!treatmentTypeId) {
                alert(currentLanguage === 'en' ? 'Please select a Treatment Type.' : 'Моля, изберете тип на лечението.');
                return;
            }

            const symptomsDtoArray = selectedSymptoms.map(symptom => ({
                id: symptom.id || null,
                name: symptom.name,
                language: symptom.language 
            }));

            let nameEnForDto = null;
            let nameBgForDto = null;
            if (formProcessingLanguage === 'en') {
                nameEnForDto = treatmentNameValue;
            } else if (formProcessingLanguage === 'bg') {
                nameBgForDto = treatmentNameValue;
            }

            const treatmentData = {
                nameEn: nameEnForDto,
                nameBg: nameBgForDto,
                type: { id: parseInt(treatmentTypeId) }, 
                description: description,
                usageInstructions: usageInstructions,
                recommendedDose: recommendedDose,
                symptoms: symptomsDtoArray
            };

            try {
                const response = await fetch('/api/treatments', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept-Language': currentLanguage 
                    },
                    body: JSON.stringify(treatmentData)
                });

                if (response.ok) {
                    alert(currentLanguage === 'en' ? 'Treatment added successfully!' : 'Лечението е добавено успешно!');
                    addTreatmentForm.reset();
                    // Clear selected symptoms
                    selectedSymptoms = [];
                    selectedSymptomsContainer.innerHTML = '';
                    updateSymptomsHiddenInput();
                } else {
                    const errorResult = await response.json(); // Assuming error response is JSON
                    alert((currentLanguage === 'en' ? 'Failed to add treatment: ' : 'Неуспешно добавяне на лечение: ') + (errorResult.message || response.statusText));
                    console.error("Error adding treatment:", errorResult);
                }
            } catch (error) {
                alert(currentLanguage === 'en' ? 'An error occurred while adding the treatment.' : 'Възникна грешка при добавянето на лечението.');
                console.error("Error during treatment creation:", error);
            }
        });
    }

    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', function() {
            localStorage.removeItem('loggedInDoctor');
            localStorage.removeItem('userRole');
            localStorage.removeItem('currentLanguage');
            window.location.href = '/'; // Changed to redirect to root
        });
    }
});

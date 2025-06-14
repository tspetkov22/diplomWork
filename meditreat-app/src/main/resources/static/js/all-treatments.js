let currentEditingTreatmentId = null;
// let currentEditingRowElement = null; // Less critical if we fully refresh table
// let originalIllnessIdForEdit = null; // Not used in the new DTO structure
let currentLanguage = localStorage.getItem('currentLanguage') || 'en';
let allFetchedTreatments = []; // To store all treatments for re-rendering

// Ensure jQuery is ready for Bootstrap components and potentially other uses
$(document).ready(function () {
    const loggedInDoctorString = localStorage.getItem('loggedInDoctor');
    if (!loggedInDoctorString) {
        window.location.href = '/'; // Redirect to login if not logged in
        return;
    }

    const langEnButton = document.getElementById('langEnButton');
    const langBgButton = document.getElementById('langBgButton');

    function updateLanguageUI(lang) {
    currentLanguage = lang;

    if (lang === 'en') {
        // EN active
        if (langEnButton) {
            langEnButton.style.backgroundColor = '#8CDBC1';
            langEnButton.style.color           = '#fff';
            // langEnButton.style.borderColor     = '#8CDBC1';
            // langEnButton.style.borderRadius    = '0.75rem';
            langEnButton.setAttribute('aria-pressed', 'true');
        }
        // BG inactive
        if (langBgButton) {
            langBgButton.style.backgroundColor = '#fff';
            langBgButton.style.color           = '#6c757cd';
            // langBgButton.style.borderColor     = '#8CDBC1';
            // langBgButton.style.borderRadius    = '0.75rem';
            langBgButton.setAttribute('aria-pressed', 'false');
        }
    } else {
        // BG active
        if (langBgButton) {
            langBgButton.style.backgroundColor = '#8CDBC1';
            langBgButton.style.color           = '#fff';
            // langBgButton.style.borderColor     = '#8CDBC1';
            // langBgButton.style.borderRadius    = '0.75rem';
            langBgButton.setAttribute('aria-pressed', 'true');
        }
        // EN inactive
        if (langEnButton) {
            langEnButton.style.backgroundColor = '#fff';
            langEnButton.style.color           = 'grey';
            // langEnButton.style.borderColor     = '#8CDBC1';
            // langEnButton.style.borderRadius    = '0.75rem';
            langEnButton.setAttribute('aria-pressed', 'false');
            }
        }
    }

    // function updateLanguageUI(lang) {
    //     currentLanguage = lang;
    //     if (lang === 'en') {
    //         if(langEnButton) {
    //             langEnButton.classList.add('active');
    //             langEnButton.setAttribute('aria-pressed', 'true');
    //         }
    //         if(langBgButton) {
    //             langBgButton.classList.remove('active');
    //             langBgButton.setAttribute('aria-pressed', 'false');
    //         }
    //     } else {
    //         if(langEnButton) {
    //             langEnButton.classList.remove('active');
    //             langEnButton.setAttribute('aria-pressed', 'false');
    //         }
    //         if(langBgButton) {
    //             langBgButton.classList.add('active');
    //             langBgButton.setAttribute('aria-pressed', 'true');
    //         }
    //     }
    // }
    
    function setLanguage(lang) {
        localStorage.setItem('currentLanguage', lang);
        const currentSearchParams = new URLSearchParams(window.location.search);
        currentSearchParams.set('lang', lang);
        window.location.search = currentSearchParams.toString(); 
    }

    if (langEnButton) langEnButton.addEventListener('click', () => setLanguage('en'));
    if (langBgButton) langBgButton.addEventListener('click', () => setLanguage('bg'));
    
    const urlLang = new URLSearchParams(window.location.search).get('lang');
    const storedLang = localStorage.getItem('currentLanguage');
    currentLanguage = urlLang || storedLang || 'en';

    if (urlLang && urlLang !== storedLang) {
        localStorage.setItem('currentLanguage', urlLang);
    }
    updateLanguageUI(currentLanguage);

    fetchAndRenderTreatments();

    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', function () {
            localStorage.removeItem('loggedInDoctor');
            localStorage.removeItem('userRole');
            window.location.href = '/';
        });
    }

    const searchButton = document.querySelector('.search-container button');
    if (searchButton) {
        searchButton.addEventListener('click', searchTreatmentsInList);
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('keypress', function(event) {
                if (event.key === 'Enter') {
                    searchTreatmentsInList();
                }
            });
        }
    }

    $('#saveTreatmentChangesBtn').on('click', handleSaveTreatmentModalChanges);
    $('#editSearchSymptomsBtn').on('click', searchSymptomsForEditModal);
    $('#editSymptomSearch').on('keypress', function(event) {
        if (event.key === 'Enter') {
            searchSymptomsForEditModal();
            event.preventDefault();
        }
    });

    $(document).on('click', '#editSymptomsDropdown .dropdown-item', function (e) {
        e.preventDefault();
        const symptomId = $(this).data('id');
        const symptomName = $(this).data('name');
        const symptomLang = $(this).data('lang');
        addSymptomToEditModalList(symptomId, symptomName, symptomLang, false);
        $('#editSymptomsDropdown').empty().hide();
        $('#editSymptomSearch').val('');
    });

    $(document).on('click', '#editSelectedSymptoms .remove-symptom', function (e) {
        e.preventDefault();
        $(this).closest('.symptom-tag').remove();
        updateHiddenEditSymptomsInput();
    });

    $(document).on('click', function(event) {
        if (!$(event.target).closest('#editSymptomSearch, #editSymptomsDropdown').length) {
            $('#editSymptomsDropdown').hide();
        }
    });

}); // End of document.ready

async function fetchAndRenderTreatments() {
    console.log("Fetching treatments in language:", currentLanguage);
    try {
        const response = await fetch('/api/treatments?lang=' + currentLanguage);
        if (!response.ok) {
            console.error('Failed to fetch treatments:', response.status, await response.text());
            allFetchedTreatments = [];
        } else {
            const responseText = await response.text();
            if (!responseText) {
                allFetchedTreatments = [];
            } else {
                try {
                    allFetchedTreatments = JSON.parse(responseText);
                } catch (e) {
                    console.error("Error parsing treatments JSON:", e, "Response text:", responseText);
                    allFetchedTreatments = [];
                }
            }
        }
    } catch (error) {
        console.error('Error fetching treatments:', error);
        allFetchedTreatments = [];
    }
    renderTreatmentsTable(allFetchedTreatments);
}

function renderTreatmentsTable(treatments) {
    const tableBody = document.getElementById('treatmentTable');
    if (!tableBody) return;
    tableBody.innerHTML = '';

    if (!treatments || treatments.length === 0) {
        const row = tableBody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 3;
        cell.textContent = currentLanguage === 'bg' ? 'Няма намерени лечения.' : 'No treatments found.';
        cell.style.textAlign = 'center';
        return;
    }

    treatments.forEach(treatment => {
        const row = tableBody.insertRow();
        row.dataset.treatmentId = treatment.id;

        row.insertCell().textContent = treatment.name || (currentLanguage === 'bg' ? 'Н/И' : 'N/A');
        row.insertCell().textContent = treatment.type && treatment.type.name ? treatment.type.name : (currentLanguage === 'bg' ? 'Н/И' : 'N/A');

        const actionsCell = row.insertCell();
        actionsCell.classList.add('actions-cell');

        const editButton = document.createElement('button');
        editButton.classList.add('btn', 'btn-sm', 'btn-success', 'edit-btn', 'me-1');
        editButton.innerHTML = '<i class="fas fa-edit"></i> ' + (currentLanguage === 'bg' ? 'Редакт.' : 'Edit');
        editButton.dataset.id = treatment.id;
        editButton.style.backgroundColor = '#8CDBC1';
        editButton.style.borderColor = '#8CDBC1';
        editButton.style.borderRadius = 0.75 + 'rem';
        actionsCell.appendChild(editButton);

        const deleteButton = document.createElement('button');
        deleteButton.classList.add('btn', 'btn-sm', 'btn-success', 'delete-btn');
        deleteButton.innerHTML = '<i class="fas fa-trash"></i> ' + (currentLanguage === 'bg' ? 'Изтрий' : 'Delete');
        deleteButton.dataset.id = treatment.id;
        deleteButton.style.backgroundColor = '#8CDBC1';
        deleteButton.style.borderColor = '#8CDBC1';
        deleteButton.style.borderRadius = 0.75 + 'rem';
        actionsCell.appendChild(deleteButton);
    });

    attachDynamicEventListeners();
}

function attachDynamicEventListeners() {
    console.log("Attaching dynamic event listeners - START");
    
    const editButtons = document.querySelectorAll(".edit-btn");
    console.log("Found edit buttons:", editButtons.length);
    
    editButtons.forEach((button, index) => {
        console.log(`Edit button ${index}:`, button);
    button.removeEventListener('click', handleEditButtonClick);
    button.addEventListener("click", handleEditButtonClick);
  });

    const deleteButtons = document.querySelectorAll(".delete-btn");
    console.log("Found delete buttons:", deleteButtons.length);
    
    deleteButtons.forEach((button, index) => {
        console.log(`Delete button ${index}:`, button);
    button.removeEventListener('click', handleDeleteButtonClick);
    button.addEventListener("click", handleDeleteButtonClick);
  });
}

function handleEditButtonClick(event) {
    console.log("Edit button clicked - START");
    console.log("Event:", event);
    console.log("Event target:", event.target);
    console.log("Event currentTarget:", event.currentTarget);
    
    const buttonElement = event.currentTarget;
    if (!buttonElement) {
        console.error("Button element is null!");
        return;
    }

    const treatmentId = buttonElement.dataset.id;
    console.log("Treatment ID from button:", treatmentId);

    if (!treatmentId) {
        console.error("No treatment ID found on button!");
        return;
    }

            try {
                openEditModal(treatmentId, currentLanguage); 
            } catch (e) {
        console.error("Error in handleEditButtonClick:", e);
        alert("An error occurred while trying to open the edit modal.");
    }
}

function handleDeleteButtonClick(event) {
    const treatmentId = event.currentTarget.dataset.id;
    const treatmentRow = event.currentTarget.closest('tr');
    const treatmentName = treatmentRow.cells[0].textContent;
    const confirmMessage = currentLanguage === 'bg' 
        ? `Сигурни ли сте, че искате да изтриете лечението "${treatmentName}" (ID: ${treatmentId})?`
        : `Are you sure you want to delete treatment "${treatmentName}" (ID: ${treatmentId})?`;

    if (confirm(confirmMessage)) {
        deleteTreatment(treatmentId);
    }
}

async function openEditModal(treatmentId, lang) {
    console.log("openEditModal - START");
    console.log("Parameters:", { treatmentId, lang });

    // First, ensure the modal element exists
    const modalElement = document.getElementById('editTreatmentModal');
    if (!modalElement) {
        console.error("Modal element not found!");
        alert("Error: Modal element not found on page.");
        return;
    }
    
    // Check for the treatment ID field
    const treatmentIdField = document.getElementById('editTreatmentId');
    if (!treatmentIdField) {
        console.error("Treatment ID field not found!");
        alert("Error: Treatment ID field not found in modal.");
        return;
    }

    try {
        // Set the treatment ID
        treatmentIdField.value = treatmentId;
        currentEditingTreatmentId = treatmentId;
        
        // Set the language
        const langField = document.getElementById('editFormLanguage');
        if (langField) {
            langField.value = lang;
        }

        console.log("About to fetch treatment details");
        const response = await fetch(`/api/treatments/edit-details/${treatmentId}?lang=${lang}`);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error("API Error:", response.status, errorText);
            throw new Error(`API Error: ${response.status} ${errorText}`);
        }

        const viewModel = await response.json();
        console.log("Received viewModel:", viewModel);

        // Safely set form values with null checks
        const safeSetValue = (id, value) => {
            const element = document.getElementById(id);
            if (element) {
                element.value = value || '';
            } else {
                console.warn(`Element not found: ${id}`);
            }
        };

        safeSetValue('editTreatmentNameEn', viewModel.nameEn);
        safeSetValue('editTreatmentNameBg', viewModel.nameBg);
        safeSetValue('editDescription', viewModel.description);
        safeSetValue('editUsageInstructions', viewModel.usageInstructions);
        safeSetValue('editRecommendedDose', viewModel.recommendedDose);

        // Conditionally show/hide name fields based on form language
        const nameEnInput = document.getElementById('editTreatmentNameEn');
        const nameBgInput = document.getElementById('editTreatmentNameBg');

        // Assuming the input is wrapped in a div, e.g., <div class="mb-3"><label>...</label><input...></div>
        // Try to find a common Bootstrap parent like .mb-3 or fall back to direct parentElement.
        const nameEnGroup = nameEnInput ? nameEnInput.closest('.mb-3') || nameEnInput.parentElement : null;
        const nameBgGroup = nameBgInput ? nameBgInput.closest('.mb-3') || nameBgInput.parentElement : null;

        if (nameEnGroup && nameBgGroup) {
            if (lang === 'en') {
                nameEnGroup.style.display = ''; // Show English name field
                nameBgGroup.style.display = 'none'; // Hide Bulgarian name field
            } else if (lang === 'bg') {
                nameEnGroup.style.display = 'none'; // Hide English name field
                nameBgGroup.style.display = ''; // Show Bulgarian name field
            } else {
                // Default: show both if lang is undefined or unexpected
                nameEnGroup.style.display = '';
                nameBgGroup.style.display = '';
                console.warn("Unexpected language value in openEditModal, showing both name fields:", lang);
            }
        } else {
            console.warn("Could not find parent groups for one or both name input fields to toggle visibility. Ensure #editTreatmentNameEn and #editTreatmentNameBg exist and have suitable parent wrappers (e.g., a div with class 'mb-3').");
        }

        // Handle treatment type dropdown
        const typeDropdown = document.getElementById('editTreatmentType');
        if (typeDropdown && viewModel.availableTypes) {
            typeDropdown.innerHTML = '<option value="">Select a type</option>';
            viewModel.availableTypes.forEach(type => {
                const option = document.createElement('option');
                option.value = type.id;
                option.textContent = type.name;
                typeDropdown.appendChild(option);
            });
            typeDropdown.value = viewModel.typeId || '';
        }

        // Clear and populate symptoms
        const symptomsContainer = document.getElementById('editSelectedSymptoms');
        if (symptomsContainer) {
            symptomsContainer.innerHTML = '';
        if (viewModel.symptomIds && viewModel.symptomIds.length > 0) {
            for (const symptomId of viewModel.symptomIds) {
                await fetchSymptomForEditModal(symptomId, lang);
            }
        }
        }

        // Show the modal
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
        console.log("Modal shown successfully");

    } catch (error) {
        console.error("Error in openEditModal:", error);
        alert("An error occurred while fetching treatment details: " + error.message);
    }
}

async function fetchSymptomForEditModal(symptomId, formDisplayLang) {
    try {
        const symptomRes = await fetch(`/api/symptoms/${symptomId}`);
        if (!symptomRes.ok) {
            console.warn(`Could not fetch symptom details for ID ${symptomId}. Status: ${symptomRes.status}`);
            addSymptomToEditModalList(symptomId, `Symptom ID: ${symptomId} (Error fetching name)`, 'unknown', true);
            return;
        }
        const symptom = await symptomRes.json();

        if (symptom.language === formDisplayLang) {
            addSymptomToEditModalList(symptom.id, symptom.name, symptom.language, true);
        } else {
            const translationRes = await fetch(`/api/symptoms/${symptom.id}/translations/${formDisplayLang}`);
            if (translationRes.ok) {
                const translatedSymptom = await translationRes.json();
                addSymptomToEditModalList(translatedSymptom.id, translatedSymptom.name, translatedSymptom.language, true);
            } else {
                console.warn(`No translation found for symptom '${symptom.name}' from '${symptom.language}' to '${formDisplayLang}'. Using original.`);
                addSymptomToEditModalList(symptom.id, `${symptom.name} (${symptom.language})`, symptom.language, true);
            }
        }
    } catch (error) {
        console.error(`Error fetching details/translation for symptom ${symptomId}:`, error);
        addSymptomToEditModalList(symptomId, `Symptom ID: ${symptomId} (Error)`, 'unknown', true);
    }
}

function addSymptomToEditModalList(id, name, langOfSymptom, isInitial) {
    if ($(`#editSelectedSymptoms .symptom-tag[data-id='${id}']`).length > 0) {
        return; 
    }
    const displayName = name; 
    $('#editSelectedSymptoms').append(
        `<span class="symptom-tag badge bg-secondary me-1 mb-1" data-id="${id}" data-name="${name}" data-lang="${langOfSymptom}">${displayName} <a href="#" class="remove-symptom text-white" style="text-decoration: none;">&times;</a></span>`
    );
    if (!isInitial) {
        updateHiddenEditSymptomsInput();
    }
}

function updateHiddenEditSymptomsInput() {
    const symptomsArray = [];
    $('#editSelectedSymptoms .symptom-tag').each(function() {
        const id = $(this).data('id');
        const name = $(this).data('name'); // Get name from data attribute
        const language = $(this).data('lang'); // Get language from data attribute
        // Backend expects SymptomDto which can have id, or name+language for new ones.
        // If ID is present, backend uses it. Otherwise, it might try to find by name+lang or create.
        // All symptoms added to the list via search should have an ID.
        symptomsArray.push({ id: id, name: name, language: language }); 
    });
    $('#editSymptoms').val(JSON.stringify(symptomsArray));
    console.log("Updated hidden symptoms for edit:", $('#editSymptoms').val());
}

async function searchSymptomsForEditModal() {
    const searchTerm = $('#editSymptomSearch').val().trim();
    const langForSearch = $('#editFormLanguage').val() || currentLanguage;

    if (searchTerm.length < 1) {
        $('#editSymptomsDropdown').empty().hide();
        return;
    }

    try {
        const response = await fetch(`/api/symptoms/search?name=${encodeURIComponent(searchTerm)}`, {
            headers: {
                'Accept-Language': langForSearch
            }
        });
        const dropdown = $('#editSymptomsDropdown');
        dropdown.empty(); // Clear previous results or messages

        if (!response.ok) {
            if (response.status === 204) {
                 dropdown.text(langForSearch === 'bg' ? 'Няма намерени симптоми.' : 'No symptoms found.').show();
            } else {
                 dropdown.text(langForSearch === 'bg' ? 'Грешка при търсене.' : 'Error searching.').show();
                 console.error(`Symptom search failed: ${response.status} ${await response.text()}`);
            }
            return;
        }
        
        const data = await response.json();
        if (data.length > 0) {
            data.forEach(symptom => {
                dropdown.append(`<a class="dropdown-item" href="#" data-id="${symptom.id}" data-name="${symptom.name}" data-lang="${symptom.language}">${symptom.name} (${symptom.language})</a>`);
            });
        } else {
             dropdown.text(langForSearch === 'bg' ? 'Няма намерени симптоми.' : 'No symptoms found.');
        }
        dropdown.show();
    } catch (error) {
        console.error("Error searching symptoms for edit modal:", error);
        $('#editSymptomsDropdown').empty().text('Error searching.').show();
    }
}

async function handleSaveTreatmentModalChanges() {
    if (!currentEditingTreatmentId) {
        alert("No treatment selected for editing.");
        return;
    }
    updateHiddenEditSymptomsInput();

    const treatmentData = {
        id: currentEditingTreatmentId,
        nameEn: $('#editTreatmentNameEn').val().trim(),
        nameBg: $('#editTreatmentNameBg').val().trim(),
        type: { id: $('#editTreatmentType').val() },
        description: $('#editDescription').val().trim(),
        usageInstructions: $('#editUsageInstructions').val().trim(),
        recommendedDose: $('#editRecommendedDose').val().trim(),
        symptoms: JSON.parse($('#editSymptoms').val() || '[]')
    };

    // Determine the language context of the form
    const formLanguage = $('#editFormLanguage').val() || currentLanguage;

    if (formLanguage === 'en' && !treatmentData.nameEn) {
        alert("English name must be provided.");
        return;
    } else if (formLanguage === 'bg' && !treatmentData.nameBg) {
        alert("Bulgarian name must be provided.");
        return;
    } else if (formLanguage !== 'en' && formLanguage !== 'bg' && !treatmentData.nameEn && !treatmentData.nameBg) {
        // Fallback for unexpected language or if both are somehow relevant and empty
        alert(currentLanguage === 'bg' ? "Трябва да бъде предоставено поне едно име (английско или българско)." : "At least one name (English or Bulgarian) must be provided.");
        return;
    }

    if (!treatmentData.type.id) {
        alert(currentLanguage === 'bg' ? "Моля, изберете тип на лечението." : "Please select a treatment type.");
        return;
    }
    
    console.log("Saving treatment data:", JSON.stringify(treatmentData));

    try {
        const response = await fetch(`/api/treatments/${currentEditingTreatmentId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept-Language': $('#editFormLanguage').val() || currentLanguage 
            },
            body: JSON.stringify(treatmentData)
        });

        if (response.ok) {
            alert(currentLanguage === 'bg' ? 'Лечението е актуализирано успешно!' : 'Treatment updated successfully!');
            bootstrap.Modal.getInstance($('#editTreatmentModal')[0]).hide();
            fetchAndRenderTreatments();
        } else {
            const errorText = await response.text(); // Get text for more detailed error
            let errorMessage = errorText;
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorText;
            } catch (e) { /* Ignore if not JSON */ }
            console.error('Error updating treatment:', response.status, errorMessage);
            alert(`Error updating treatment: ${errorMessage}`);
        }
    } catch (error) {
        console.error('Error saving treatment changes:', error);
        alert(`An unexpected error occurred: ${error.message}`);
    }
}

async function deleteTreatment(treatmentId) {
    try {
        const response = await fetch(`/api/treatments/${treatmentId}`, {
            method: 'DELETE',
            headers: {
                 'Accept-Language': currentLanguage
            }
        });
        if (response.ok) {
            alert(currentLanguage === 'bg' ? 'Лечението е изтрито успешно.' : 'Treatment deleted successfully.');
            fetchAndRenderTreatments();
        } else {
            const errorText = await response.text();
            console.error('Failed to delete treatment:', response.status, errorText);
            alert(`Failed to delete treatment: ${errorText || response.statusText}`);
        }
    } catch (error) {
        console.error('Error deleting treatment:', error);
        alert(`An error occurred while deleting the treatment: ${error.message}`);
    }
}

function searchTreatmentsInList() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const filteredTreatments = allFetchedTreatments.filter(treatment => {
        const nameMatch = treatment.name && treatment.name.toLowerCase().includes(searchTerm);
        const typeMatch = treatment.type && treatment.type.name && treatment.type.name.toLowerCase().includes(searchTerm);
        return nameMatch || typeMatch;
    });
    renderTreatmentsTable(filteredTreatments);
    if (filteredTreatments.length === 0 && searchTerm) {
         const tableBody = document.getElementById('treatmentTable');
         if(tableBody.rows.length === 1 && tableBody.rows[0].cells[0].colSpan === 3) {
            tableBody.rows[0].cells[0].textContent = currentLanguage === 'bg' ? `Няма резултати за "${searchTerm}".` : `No results for "${searchTerm}".`;
         }
    }
}

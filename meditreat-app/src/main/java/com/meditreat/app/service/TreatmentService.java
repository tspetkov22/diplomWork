package com.meditreat.app.service;

import com.meditreat.app.dto.SymptomDto;
import com.meditreat.app.dto.TreatmentDto;
import com.meditreat.app.dto.TreatmentFormViewModel;
import com.meditreat.app.dto.TreatmentTypeDto;
import com.meditreat.model.Symptom;
import com.meditreat.model.Treatment;
import com.meditreat.model.TreatmentType;
import com.meditreat.repository.SymptomRepository;
import com.meditreat.repository.TreatmentRepository;
import com.meditreat.repository.TreatmentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final SymptomRepository symptomRepository;
    private final TreatmentTypeRepository treatmentTypeRepository;
    private final SymptomService symptomService; // For creating/finding symptoms
    private final TranslationService translationService; // For auto-translation

    @Autowired
    public TreatmentService(TreatmentRepository treatmentRepository,
            SymptomRepository symptomRepository,
            TreatmentTypeRepository treatmentTypeRepository,
            SymptomService symptomService,
            TranslationService translationService) {
        this.treatmentRepository = treatmentRepository;
        this.symptomRepository = symptomRepository;
        this.treatmentTypeRepository = treatmentTypeRepository;
        this.symptomService = symptomService;
        this.translationService = translationService;
    }

    @Transactional
    public TreatmentDto createTreatment(TreatmentDto treatmentDto, String currentRequestLanguage) {
        // This 'currentRequestLanguage' is the language of the UI/request,
        // not necessarily the language of the treatment being created if names in both
        // are provided.

        Treatment savedEnglishTreatment = null;
        Treatment savedBulgarianTreatment = null;

        // Validate common fields
        if (treatmentDto.getType() == null || treatmentDto.getType().getId() == null) {
            throw new IllegalArgumentException("Treatment type ID must be provided.");
        }

        // Create English treatment if English name is provided
        if (treatmentDto.getNameEn() != null && !treatmentDto.getNameEn().trim().isEmpty()) {
            Treatment englishTreatment = new Treatment();
            englishTreatment.setLanguage("en");
            englishTreatment.setName(treatmentDto.getNameEn());
            englishTreatment.setDescription(treatmentDto.getDescription());
            englishTreatment.setUsageInstructions(treatmentDto.getUsageInstructions());
            englishTreatment.setRecommendedDose(treatmentDto.getRecommendedDose());

            TreatmentType englishType = treatmentTypeRepository
                    .findByIdAndLanguage(treatmentDto.getType().getId(), "en")
                    .orElseThrow(() -> new IllegalArgumentException(
                            "English TreatmentType not found with ID: " + treatmentDto.getType().getId()));
            englishTreatment.setType(englishType);

            Set<Symptom> englishSymptoms = new HashSet<>();
            if (treatmentDto.getSymptoms() != null) {
                for (SymptomDto symptomDto : treatmentDto.getSymptoms()) {
                    // Assuming symptomDto might contain an ID that is language-neutral,
                    // or a name that needs to be resolved/created in 'en'
                    Symptom symptom = handleSymptom(symptomDto, "en");
                    englishSymptoms.add(symptom);
                }
            }
            englishTreatment.setSymptoms(englishSymptoms);
            savedEnglishTreatment = treatmentRepository.save(englishTreatment);
        }

        // Create Bulgarian treatment if Bulgarian name is provided
        if (treatmentDto.getNameBg() != null && !treatmentDto.getNameBg().trim().isEmpty()) {
            Treatment bulgarianTreatment = new Treatment();
            bulgarianTreatment.setLanguage("bg");
            bulgarianTreatment.setName(treatmentDto.getNameBg());
            bulgarianTreatment.setDescription(treatmentDto.getDescription()); // Shared
            bulgarianTreatment.setUsageInstructions(treatmentDto.getUsageInstructions()); // Shared
            bulgarianTreatment.setRecommendedDose(treatmentDto.getRecommendedDose()); // Shared

            // Find the corresponding Bulgarian treatment type.
            // This assumes the DTO carries the ID of the type in the
            // 'currentRequestLanguage'.
            // We need to find its 'bg' counterpart.
            TreatmentType requestLangType = treatmentTypeRepository.findById(treatmentDto.getType().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Original TreatmentType not found with ID: " + treatmentDto.getType().getId()));

            // Attempt to find by name in Bulgarian or use translation.
            // This logic might need refinement based on how types are linked across
            // languages.
            // For now, assume a direct name match or a translated name.
            Optional<TreatmentType> bgTypeOpt = treatmentTypeRepository.findByNameAndLanguage(requestLangType.getName(),
                    "bg");
            if (!bgTypeOpt.isPresent() && translationService != null) { // Check if translationService is available
                String translatedTypeName = translationService.translate(requestLangType.getName(),
                        requestLangType.getLanguage(), "bg");
                bgTypeOpt = treatmentTypeRepository.findByNameAndLanguage(translatedTypeName, "bg");
            }

            TreatmentType bulgarianType = bgTypeOpt.orElseThrow(() -> new IllegalArgumentException(
                    "Bulgarian TreatmentType corresponding to ID " + treatmentDto.getType().getId()
                            + " not found. Original name: '" + requestLangType.getName() + "'"));
            bulgarianTreatment.setType(bulgarianType);

            Set<Symptom> bulgarianSymptoms = new HashSet<>();
            if (treatmentDto.getSymptoms() != null) {
                for (SymptomDto symptomDto : treatmentDto.getSymptoms()) {
                    // Resolve/create symptoms in 'bg'
                    Symptom symptom = handleSymptom(symptomDto, "bg");
                    bulgarianSymptoms.add(symptom);
                }
            }
            bulgarianTreatment.setSymptoms(bulgarianSymptoms);
            savedBulgarianTreatment = treatmentRepository.save(bulgarianTreatment);
        }

        if (savedEnglishTreatment == null && savedBulgarianTreatment == null) {
            throw new IllegalArgumentException("At least one treatment name (English or Bulgarian) must be provided.");
        }

        // Return DTO of the treatment in the current request language, or English by
        // default if available.
        if ("en".equals(currentRequestLanguage) && savedEnglishTreatment != null) {
            return toTreatmentDto(savedEnglishTreatment);
        } else if ("bg".equals(currentRequestLanguage) && savedBulgarianTreatment != null) {
            return toTreatmentDto(savedBulgarianTreatment);
        } else if (savedEnglishTreatment != null) { // Fallback
            return toTreatmentDto(savedEnglishTreatment);
        } else { // Must be savedBulgarianTreatment
            return toTreatmentDto(savedBulgarianTreatment);
        }
    }

    @Transactional(readOnly = true)
    public TreatmentFormViewModel prepareTreatmentEditViewModel(Long primaryTreatmentId, String formLanguage) {
        Treatment primaryTreatment = treatmentRepository.findById(primaryTreatmentId)
                .orElseThrow(() -> new IllegalArgumentException("Treatment not found with ID: " + primaryTreatmentId));

        TreatmentFormViewModel viewModel = new TreatmentFormViewModel();
        viewModel.setId(primaryTreatment.getId());
        viewModel.setDescription(primaryTreatment.getDescription());
        viewModel.setUsageInstructions(primaryTreatment.getUsageInstructions());
        viewModel.setRecommendedDose(primaryTreatment.getRecommendedDose());
        viewModel.setLanguageOfForm(formLanguage);

        if (primaryTreatment.getType() != null) {
            viewModel.setTypeId(primaryTreatment.getType().getId());
        }

        if (primaryTreatment.getSymptoms() != null) {
            viewModel.setSymptomIds(primaryTreatment.getSymptoms().stream()
                    .map(Symptom::getId)
                    .collect(Collectors.toSet()));
        }

        String primaryLang = primaryTreatment.getLanguage();
        String counterpartLang = "en".equals(primaryLang) ? "bg" : "en";

        if ("en".equals(primaryLang)) {
            viewModel.setNameEn(primaryTreatment.getName());
            // Try to find Bulgarian counterpart by translated name or a conventional link
            if (translationService != null) {
                String translatedName = translationService.translate(primaryTreatment.getName(), "en", "bg");
                treatmentRepository.findByNameAndLanguage(translatedName, "bg")
                        .ifPresent(counterpart -> viewModel.setNameBg(counterpart.getName()));
            }
        } else if ("bg".equals(primaryLang)) {
            viewModel.setNameBg(primaryTreatment.getName());
            // Try to find English counterpart
            if (translationService != null) {
                String translatedName = translationService.translate(primaryTreatment.getName(), "bg", "en");
                treatmentRepository.findByNameAndLanguage(translatedName, "en")
                        .ifPresent(counterpart -> viewModel.setNameEn(counterpart.getName()));
            }
        }

        // If, after attempting to find a counterpart, one of the names is still null,
        // and the primary treatment's language matches the expected field, fill it.
        // This handles cases where no counterpart exists or translation failed.
        if (viewModel.getNameEn() == null && "en".equals(primaryLang)) {
            viewModel.setNameEn(primaryTreatment.getName());
        }
        if (viewModel.getNameBg() == null && "bg".equals(primaryLang)) {
            viewModel.setNameBg(primaryTreatment.getName());
        }

        // Available types should be in the language of the form for the dropdown
        viewModel.setAvailableTypes(
                treatmentTypeRepository.findByLanguage(formLanguage).stream()
                        .map(tt -> new TreatmentTypeDto(tt.getId(), tt.getName(), tt.getLanguage()))
                        .collect(Collectors.toList()));

        return viewModel;
    }

    private Symptom handleSymptom(SymptomDto symptomDto, String language) {
        if (symptomDto.getId() != null) {
            Symptom existingSymptom = symptomRepository.findById(symptomDto.getId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Symptom not found with ID: " + symptomDto.getId()));
            if (!existingSymptom.getLanguage().equals(language)) {
                throw new IllegalArgumentException("Symptom language does not match treatment language.");
            }
            return existingSymptom;
        } else if (symptomDto.getName() != null && !symptomDto.getName().trim().isEmpty()) {
            Optional<Symptom> existingSymptom = symptomRepository.findByNameIgnoreCaseAndLanguage(symptomDto.getName(),
                    language);
            return existingSymptom
                    .orElseGet(() -> symptomService.createSymptom(new Symptom(null, symptomDto.getName(), language)));
        } else {
            throw new IllegalArgumentException("Symptom must have an ID or a Name.");
        }
    }

    @Transactional(readOnly = true)
    public List<TreatmentDto> getAllTreatments(String language) {
        // This might need a more specific repository method if we only want treatments
        // of a certain language.
        // For now, fetching all and then filtering, or assuming controller passes a
        // language to a specific repo method.
        // Let's assume a method like findAllByLanguage(language) in repository or
        // filter here.
        // For simplicity, if Treatment entity has 'language' field, JpaRepository could
        // have findByLanguage(language).
        // The current Treatment model has a language field.
        // So, we would add List<Treatment> findByLanguage(String language); to
        // TreatmentRepository.
        // For now, I will just use findAll() and expect it to be filtered at some point
        // or add the method later.
        return treatmentRepository.findByLanguage(language).stream()
                .map(this::toTreatmentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TreatmentDto> getTreatmentById(Long id, String language) {
        return treatmentRepository.findById(id)
                .filter(t -> t.getLanguage().equals(language))
                .map(this::toTreatmentDto);
    }

    @Transactional(readOnly = true)
    public List<TreatmentDto> findTreatments(String symptomName, String typeName, String language) {
        System.out.println("DEBUG: Finding treatments with symptom=" + symptomName + ", type=" + typeName
                + ", language=" + language);
        List<Treatment> treatments = treatmentRepository.findBySymptomNameAndTypeNameAndLanguage(symptomName, typeName,
                language);
        System.out.println("DEBUG: Found " + treatments.size() + " treatments");

        if (treatments.isEmpty()) {
            // Log some diagnostic information to help troubleshoot
            System.out.println("DEBUG: No treatments found, checking if symptom exists");
            List<Symptom> symptoms = symptomRepository.findByNameStartingWithIgnoreCaseAndLanguage(symptomName,
                    language);
            System.out.println("DEBUG: Found " + symptoms.size() + " symptoms matching name: " + symptomName);

            List<TreatmentType> types = treatmentTypeRepository.findByNameAndLanguage(typeName, language)
                    .map(List::of).orElse(List.of());
            System.out.println("DEBUG: Found " + types.size() + " treatment types matching name: " + typeName);
        }

        return treatments.stream()
                .map(this::toTreatmentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TreatmentDto> findTreatmentsBySymptomsAndType(Set<Long> symptomIds, Long typeId, String language) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            // If no symptoms are specified, perhaps fetch all treatments of a type? Or
            // throw error?
            // For now, returning empty or you might adjust logic.
            // Alternative: query by typeId and language only if symptomIds is empty.
            return treatmentRepository.findAll().stream()
                    .filter(t -> t.getLanguage().equals(language) && t.getType().getId().equals(typeId))
                    .map(this::toTreatmentDto)
                    .collect(Collectors.toList());
        }
        return treatmentRepository
                .findByAllSymptomIdsAndTypeIdAndLanguage(symptomIds, typeId, language, (long) symptomIds.size())
                .stream()
                .map(this::toTreatmentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TreatmentDto updateTreatment(Long id, TreatmentDto treatmentDto, String formLanguageContext) {
        // id is the ID of ONE of the treatment entries (e.g., the English one if edited
        // from an English context).
        // formLanguageContext is the language the user was viewing the form in.
        // treatmentDto contains nameEn, nameBg, and shared fields.

        Treatment existingPrimaryTreatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Treatment with ID: " + id + " not found."));

        String primaryLang = existingPrimaryTreatment.getLanguage();
        String counterpartLang = "en".equals(primaryLang) ? "bg" : "en";

        Treatment englishTreatmentToSave = null;
        Treatment bulgarianTreatmentToSave = null;

        // Update or create English treatment part
        if (treatmentDto.getNameEn() != null && !treatmentDto.getNameEn().trim().isEmpty()) {
            if ("en".equals(primaryLang)) {
                englishTreatmentToSave = existingPrimaryTreatment;
            } else { // Primary is Bulgarian, find or create English counterpart
                // Attempt to find by translated name of the BG treatment, or a potential
                // existing EN name if provided.
                final String searchNameEn = treatmentDto.getNameEn();
                englishTreatmentToSave = treatmentRepository.findByNameAndLanguage(searchNameEn, "en")
                        .orElse(new Treatment());
                if (englishTreatmentToSave.getId() == null && translationService != null) { // If not found by DTO's
                                                                                            // nameEn, try translating
                                                                                            // original BG name
                    String translatedName = translationService.translate(existingPrimaryTreatment.getName(), "bg",
                            "en");
                    englishTreatmentToSave = treatmentRepository.findByNameAndLanguage(translatedName, "en")
                            .orElse(new Treatment());
                }
            }
            englishTreatmentToSave.setLanguage("en");
            englishTreatmentToSave.setName(treatmentDto.getNameEn());
            englishTreatmentToSave.setDescription(treatmentDto.getDescription());
            englishTreatmentToSave.setUsageInstructions(treatmentDto.getUsageInstructions());
            englishTreatmentToSave.setRecommendedDose(treatmentDto.getRecommendedDose());

            // Type for English treatment
            TreatmentType enType = treatmentTypeRepository.findByIdAndLanguage(treatmentDto.getType().getId(), "en")
                    .orElseThrow(() -> new IllegalArgumentException("English TreatmentType not found with ID: "
                            + treatmentDto.getType().getId() + " (referenced by form)"));
            englishTreatmentToSave.setType(enType);

            // Symptoms for English treatment
            Set<Symptom> enSymptoms = new HashSet<>();
            if (treatmentDto.getSymptoms() != null) {
                for (SymptomDto symptomDto : treatmentDto.getSymptoms()) {
                    enSymptoms.add(handleSymptom(symptomDto, "en"));
                }
            }
            englishTreatmentToSave.setSymptoms(enSymptoms);
        } else {
            // If NameEn is empty/null, consider deleting the English version if it's the
            // counterpart
            if ("bg".equals(primaryLang)) { // and we are editing the Bulgarian one
                // Optional: find and delete English counterpart if its name is now blanked.
                // String originalBgName = existingPrimaryTreatment.getName();
                // String translatedEnName = translationService.translate(originalBgName, "bg",
                // "en");
                // treatmentRepository.findByNameAndLanguage(translatedEnName,
                // "en").ifPresent(treatmentRepository::delete);
            } else if ("en".equals(primaryLang)
                    && (treatmentDto.getNameBg() == null || treatmentDto.getNameBg().trim().isEmpty())) {
                // If primary was EN, and EN name is blanked, AND BG name is also blank, this is
                // an issue.
                // If primary was EN, and EN name is blanked, but BG name is present, it implies
                // EN version should be deleted.
                // This case might require deleting existingPrimaryTreatment if no BG version
                // takes over.
                // For now, if NameEn is blank, we won't save an English version. If it was the
                // primary, it might get orphaned or deleted based on cascading or other logic.
            }
        }

        // Update or create Bulgarian treatment part
        if (treatmentDto.getNameBg() != null && !treatmentDto.getNameBg().trim().isEmpty()) {
            if ("bg".equals(primaryLang)) {
                bulgarianTreatmentToSave = existingPrimaryTreatment;
            } else { // Primary is English, find or create Bulgarian counterpart
                final String searchNameBg = treatmentDto.getNameBg();
                bulgarianTreatmentToSave = treatmentRepository.findByNameAndLanguage(searchNameBg, "bg")
                        .orElse(new Treatment());
                if (bulgarianTreatmentToSave.getId() == null && translationService != null) { // If not found by DTO's
                                                                                              // nameBg, try translating
                                                                                              // original EN name
                    String translatedName = translationService.translate(existingPrimaryTreatment.getName(), "en",
                            "bg");
                    bulgarianTreatmentToSave = treatmentRepository.findByNameAndLanguage(translatedName, "bg")
                            .orElse(new Treatment());
                }
            }
            bulgarianTreatmentToSave.setLanguage("bg");
            bulgarianTreatmentToSave.setName(treatmentDto.getNameBg());
            bulgarianTreatmentToSave.setDescription(treatmentDto.getDescription()); // Shared
            bulgarianTreatmentToSave.setUsageInstructions(treatmentDto.getUsageInstructions()); // Shared
            bulgarianTreatmentToSave.setRecommendedDose(treatmentDto.getRecommendedDose()); // Shared

            // Type for Bulgarian treatment - resolve from the type ID provided in the DTO
            // (which is lang-specific)
            // We need to find the BG equivalent of the type selected in the form.
            TreatmentType formSelectedType = treatmentTypeRepository.findById(treatmentDto.getType().getId())
                    .orElseThrow(); // This is the type in formLanguageContext
            TreatmentType bgType;
            if (formSelectedType.getLanguage().equals("bg")) {
                bgType = formSelectedType;
            } else { // form type was EN, find BG equivalent
                Optional<TreatmentType> bgTypeOpt = treatmentTypeRepository
                        .findByNameAndLanguage(formSelectedType.getName(), "bg");
                if (!bgTypeOpt.isPresent() && translationService != null) {
                    String translatedTypeName = translationService.translate(formSelectedType.getName(),
                            formSelectedType.getLanguage(), "bg");
                    bgTypeOpt = treatmentTypeRepository.findByNameAndLanguage(translatedTypeName, "bg");
                }
                bgType = bgTypeOpt.orElseThrow(() -> new IllegalArgumentException(
                        "Bulgarian counterpart for TreatmentType '" + formSelectedType.getName() + "' not found."));
            }
            bulgarianTreatmentToSave.setType(bgType);

            // Symptoms for Bulgarian treatment
            Set<Symptom> bgSymptoms = new HashSet<>();
            if (treatmentDto.getSymptoms() != null) {
                for (SymptomDto symptomDto : treatmentDto.getSymptoms()) {
                    bgSymptoms.add(handleSymptom(symptomDto, "bg"));
                }
            }
            bulgarianTreatmentToSave.setSymptoms(bgSymptoms);
        } else {
            // If NameBg is empty/null, consider deleting the Bulgarian version if it's the
            // counterpart
            if ("en".equals(primaryLang)) { // and we are editing the English one
                // Optional: find and delete BG counterpart if its name is now blanked.
            }
        }

        // Save the entities
        Treatment finalSavedTreatment = null;
        if (englishTreatmentToSave != null) {
            if (englishTreatmentToSave.getId() == null && "en".equals(primaryLang)
                    && (treatmentDto.getNameBg() == null || treatmentDto.getNameBg().trim().isEmpty())) {
                // Avoid creating a new EN entry if the original EN entry was blanked and no BG
                // entry is being made.
            } else {
                finalSavedTreatment = treatmentRepository.save(englishTreatmentToSave);
            }
        }
        if (bulgarianTreatmentToSave != null) {
            if (bulgarianTreatmentToSave.getId() == null && "bg".equals(primaryLang)
                    && (treatmentDto.getNameEn() == null || treatmentDto.getNameEn().trim().isEmpty())) {
                // Avoid creating a new BG entry if the original BG entry was blanked and no EN
                // entry is being made.
            } else {
                finalSavedTreatment = treatmentRepository.save(bulgarianTreatmentToSave);
            }
        }

        // If the original primary treatment was effectively deleted (e.g., its name
        // blanked and no counterpart took over its ID)
        // and it wasn't one of the entities saved, explicitly delete it.
        // This logic is tricky and depends on whether blanking a name means deletion.
        boolean primaryWasDeleted = false;
        if ("en".equals(primaryLang) && (treatmentDto.getNameEn() == null || treatmentDto.getNameEn().trim().isEmpty()))
            primaryWasDeleted = true;
        if ("bg".equals(primaryLang) && (treatmentDto.getNameBg() == null || treatmentDto.getNameBg().trim().isEmpty()))
            primaryWasDeleted = true;

        if (primaryWasDeleted && existingPrimaryTreatment.getId() != null &&
                (englishTreatmentToSave == null
                        || !existingPrimaryTreatment.getId().equals(englishTreatmentToSave.getId()))
                &&
                (bulgarianTreatmentToSave == null
                        || !existingPrimaryTreatment.getId().equals(bulgarianTreatmentToSave.getId()))) {
            treatmentRepository.delete(existingPrimaryTreatment);
            if (finalSavedTreatment == null)
                return null; // Indicates deletion without creation of a new version
        }

        if (finalSavedTreatment == null) {
            throw new IllegalStateException(
                    "Update operation resulted in no treatment being saved or identified as primary.");
        }

        // Return DTO based on the formLanguageContext or the language of the saved
        // entity
        if (formLanguageContext.equals(finalSavedTreatment.getLanguage())) {
            return toTreatmentDto(finalSavedTreatment);
        } else if ("en".equals(finalSavedTreatment.getLanguage()) && englishTreatmentToSave != null) {
            return toTreatmentDto(englishTreatmentToSave);
        } else if ("bg".equals(finalSavedTreatment.getLanguage()) && bulgarianTreatmentToSave != null) {
            return toTreatmentDto(bulgarianTreatmentToSave);
        } else if (englishTreatmentToSave != null) { // Fallback to English if available
            return toTreatmentDto(englishTreatmentToSave);
        } else { // Fallback to Bulgarian if available (must be bulgarianTreatmentToSave)
            return toTreatmentDto(bulgarianTreatmentToSave);
        }
    }

    @Transactional
    public void deleteTreatment(Long id) {
        if (!treatmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Treatment not found with ID: " + id);
        }
        treatmentRepository.deleteById(id);
    }

    // Conversion utility
    private TreatmentDto toTreatmentDto(Treatment treatment) {
        TreatmentDto dto = new TreatmentDto();
        dto.setId(treatment.getId());
        dto.setName(treatment.getName());
        dto.setDescription(treatment.getDescription());
        dto.setUsageInstructions(treatment.getUsageInstructions());
        dto.setRecommendedDose(treatment.getRecommendedDose());
        dto.setLanguage(treatment.getLanguage());

        if (treatment.getType() != null) {
            dto.setType(new TreatmentTypeDto(treatment.getType().getId(), treatment.getType().getName(),
                    treatment.getType().getLanguage()));
        }

        if (treatment.getSymptoms() != null) {
            dto.setSymptoms(treatment.getSymptoms().stream()
                    .map(s -> new SymptomDto(s.getId(), s.getName(), s.getLanguage()))
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}
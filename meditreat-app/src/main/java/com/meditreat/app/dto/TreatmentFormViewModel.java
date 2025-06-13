package com.meditreat.app.dto;

import java.util.List;
import java.util.Set;

public class TreatmentFormViewModel {
    private Long id; // ID of the primary treatment entry being edited
    private String nameEn;
    private String nameBg;
    private Long typeId;
    private String description;
    private String usageInstructions;
    private String recommendedDose;
    private Set<Long> symptomIds; // Using IDs for simplicity in form binding

    // Data for populating form options
    private List<TreatmentTypeDto> availableTypes;
    private String languageOfForm; // e.g., "en" or "bg", the language context of the page

    // Default constructor
    public TreatmentFormViewModel() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameBg() {
        return nameBg;
    }

    public void setNameBg(String nameBg) {
        this.nameBg = nameBg;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsageInstructions() {
        return usageInstructions;
    }

    public void setUsageInstructions(String usageInstructions) {
        this.usageInstructions = usageInstructions;
    }

    public String getRecommendedDose() {
        return recommendedDose;
    }

    public void setRecommendedDose(String recommendedDose) {
        this.recommendedDose = recommendedDose;
    }

    public Set<Long> getSymptomIds() {
        return symptomIds;
    }

    public void setSymptomIds(Set<Long> symptomIds) {
        this.symptomIds = symptomIds;
    }

    public List<TreatmentTypeDto> getAvailableTypes() {
        return availableTypes;
    }

    public void setAvailableTypes(List<TreatmentTypeDto> availableTypes) {
        this.availableTypes = availableTypes;
    }

    public String getLanguageOfForm() {
        return languageOfForm;
    }

    public void setLanguageOfForm(String languageOfForm) {
        this.languageOfForm = languageOfForm;
    }
}
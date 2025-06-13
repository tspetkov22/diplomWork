package com.meditreat.app.dto;

import java.util.Set;
import java.util.stream.Collectors;
import com.meditreat.model.Treatment;
import com.meditreat.model.Symptom; // Assuming Symptom model exists
import com.meditreat.model.TreatmentType; // Assuming TreatmentType model exists

public class TreatmentDto {
    private Long id;
    private TreatmentTypeDto type; // Changed to TreatmentTypeDto
    private String name;
    private String description;
    private String usageInstructions;
    private String recommendedDose;
    private String language; // Added language field
    private String nameEn; // Added for English name
    private String nameBg; // Added for Bulgarian name
    private Set<SymptomDto> symptoms; // Changed to Set<SymptomDto>

    public TreatmentDto() {
    }

    // Constructor from Treatment entity
    public TreatmentDto(Treatment treatment) {
        this.id = treatment.getId();
        if (treatment.getType() != null) {
            this.type = new TreatmentTypeDto(treatment.getType().getId(), treatment.getType().getName(),
                    treatment.getType().getLanguage());
        }
        this.name = treatment.getName();
        this.description = treatment.getDescription();
        this.usageInstructions = treatment.getUsageInstructions();
        this.recommendedDose = treatment.getRecommendedDose();
        this.language = treatment.getLanguage();
        if (treatment.getSymptoms() != null) {
            this.symptoms = treatment.getSymptoms().stream()
                    .map(symptom -> new SymptomDto(symptom.getId(), symptom.getName(), symptom.getLanguage()))
                    .collect(Collectors.toSet());
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TreatmentTypeDto getType() {
        return type;
    }

    public void setType(TreatmentTypeDto type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    // Getter and Setter for nameEn
    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    // Getter and Setter for nameBg
    public String getNameBg() {
        return nameBg;
    }

    public void setNameBg(String nameBg) {
        this.nameBg = nameBg;
    }

    public Set<SymptomDto> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(Set<SymptomDto> symptoms) {
        this.symptoms = symptoms;
    }
}
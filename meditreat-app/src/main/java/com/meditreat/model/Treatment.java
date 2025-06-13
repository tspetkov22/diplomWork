package com.meditreat.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "treatment")
public class Treatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "treatment_type_id", foreignKey = @ForeignKey(name = "fk_treatment_type"))
    private TreatmentType type; // Assumes type is fetched for the correct language by service

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "usage_instructions", columnDefinition = "TEXT")
    private String usageInstructions;

    @Column(name = "recommended_dose", columnDefinition = "TEXT")
    private String recommendedDose;

    @Column(nullable = false, length = 2) // e.g., "en", "bg"
    private String language;

    @ManyToMany
    @JoinTable(name = "treatment_symptom", joinColumns = @JoinColumn(name = "treatment_id"), inverseJoinColumns = @JoinColumn(name = "symptom_id"))
    private Set<Symptom> symptoms = new HashSet<>();

    public Treatment() {
    }
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TreatmentType getType() {
        return type;
    }

    public void setType(TreatmentType type) {
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

    public Set<Symptom> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(Set<Symptom> symptoms) {
        this.symptoms = symptoms;
    }
}
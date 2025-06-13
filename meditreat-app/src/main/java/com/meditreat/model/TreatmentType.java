package com.meditreat.model;

import jakarta.persistence.*;

@Entity
@Table(name = "treatment_type", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "name", "language" }),
        @UniqueConstraint(columnNames = { "normalized_name", "language" })
})
public class TreatmentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "normalized_name", length = 100)
    private String normalizedName;

    @Column(nullable = false, length = 2)
    private String language;

    // Standard getters/setters, constructors…
    public TreatmentType() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        // If normalizedName is not set, use the English name as normalized name
        if (normalizedName == null && "en".equals(language)) {
            this.normalizedName = name;
        }
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
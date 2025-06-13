package com.meditreat.model;

import jakarta.persistence.*;

@Entity
@Table(name = "symptom", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "language" }))
public class Symptom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100) // Name is no longer unique by itself
    private String name;

    @Column(nullable = false, length = 2) // e.g., "en", "bg"
    private String language;

    @Column(length = 500) // Added description field
    private String description;

    @Column(length = 100) // Can be null, shared by symptoms that are translations of each other
    private String translationKey;

    public Symptom(Long id, String name, String language) {
        this.id = id;
        this.name = name;
        this.language = language;
    } 

    // Constructor including translationKey
    public Symptom(Long id, String name, String language, String translationKey) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.translationKey = translationKey;
    }

    // Constructor including description
    public Symptom(Long id, String name, String language, String description, String translationKey) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.description = description;
        this.translationKey = translationKey;
    }

    // Default constructor for JPA
    public Symptom() {
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
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }
}
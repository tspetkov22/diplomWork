package com.meditreat.app.dto;

public class SymptomDto {
    private Long id;
    private String name;
    private String language;
    private String translationKey;
    private String targetTranslation;
    private String description;

    public SymptomDto() {
    }

    public SymptomDto(Long id, String name, String language) {
        this.id = id;
        this.name = name;
        this.language = language;
    }

    public SymptomDto(Long id, String name, String language, String translationKey) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.translationKey = translationKey;
    }

    public SymptomDto(Long id, String name, String language, String translationKey, String description) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.translationKey = translationKey;
        this.description = description;
    }

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

    public String getTranslationKey() {
        return translationKey;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetTranslation() {
        return targetTranslation;
    }

    public void setTargetTranslation(String targetTranslation) {
        this.targetTranslation = targetTranslation;
    }
}
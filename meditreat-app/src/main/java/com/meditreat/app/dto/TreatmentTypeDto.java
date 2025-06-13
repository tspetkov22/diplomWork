package com.meditreat.app.dto;

public class TreatmentTypeDto {
    private Long id;
    private String name;
    private String language;

    public TreatmentTypeDto() {
    }

    public TreatmentTypeDto(Long id, String name, String language) {
        this.id = id;
        this.name = name;
        this.language = language;
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
}
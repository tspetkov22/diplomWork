package com.meditreat.app.dto;

public class EmailRequestDto {

    private String patientEmail;
    private String language; // "en" or "bg"
    private Long treatmentId;
    private String doctorFullName;

    // Constructors
    public EmailRequestDto() {
    }

    public EmailRequestDto(String patientEmail, String language, Long treatmentId, String doctorFullName) {
        this.patientEmail = patientEmail;
        this.language = language;
        this.treatmentId = treatmentId;
        this.doctorFullName = doctorFullName;
    }

    // Getters and Setters
    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(Long treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getDoctorFullName() {
        return doctorFullName;
    }

    public void setDoctorFullName(String doctorFullName) {
        this.doctorFullName = doctorFullName;
    }
}
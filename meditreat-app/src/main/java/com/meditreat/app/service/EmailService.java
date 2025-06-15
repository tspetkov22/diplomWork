package com.meditreat.app.service;

import com.meditreat.app.dto.EmailRequestDto;
import com.meditreat.app.dto.SymptomDto;
import com.meditreat.app.dto.TreatmentDto;
// Import Treatment entity if needed for other parts, but not for the DTO handling here
// import com.meditreat.app.entity.Illness; // No longer needed for this method if using DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailService {

        private final JavaMailSender mailSender;
        private final TreatmentService treatmentService; // To fetch treatment details

        @Autowired
        public EmailService(JavaMailSender mailSender, TreatmentService treatmentService) {
                this.mailSender = mailSender;
                this.treatmentService = treatmentService;
        }

        public void sendTreatmentEmail(EmailRequestDto emailRequestDto) {
                String langFromDto = emailRequestDto.getLanguage();
                final String emailLang;
                if ("bg".equalsIgnoreCase(langFromDto)) {
                        emailLang = "bg";
                } else {
                        emailLang = "en"; // Default to English if not "bg" or if langFromDto is null/invalid
                }

                TreatmentDto treatmentDto = treatmentService
                                .getTreatmentById(emailRequestDto.getTreatmentId(), emailLang)
                                .orElseThrow(() -> new NoSuchElementException(
                                                "Treatment not found for ID: " + emailRequestDto.getTreatmentId()
                                                                + " with language: " + emailLang));

                Set<SymptomDto> symptoms = treatmentDto.getSymptoms();
                String symptomNames;
                if (symptoms == null || symptoms.isEmpty()) {
                        // Fallback if symptoms are unexpectedly missing
                        symptomNames = (emailLang.equals("bg")) ? "Вашето състояние" : "your condition";
                } else {
                        symptomNames = symptoms.stream().map(SymptomDto::getName).collect(Collectors.joining(", "));
                }

                String subject;
                String body;

                String treatmentName = treatmentDto.getName();
                String treatmentDescription = treatmentDto.getDescription();
                String treatmentWayOfUse = treatmentDto.getUsageInstructions();
                String doctorFullName = emailRequestDto.getDoctorFullName();

                if ("bg".equalsIgnoreCase(emailLang)) {
                        subject = "Информация за лечение относно " + symptomNames + " от Д-р " + doctorFullName;
                        body = String.format(
                                        "Уважаеми Пациент,\n\nД-р %s Ви изпраща следната информация за лечение относно %s:\n\nЛечение: %s\nОписание: %s\nНачин на употреба: %s\n\nМоля, консултирайте се с Вашия лекар за допълнителни въпроси.",
                                        doctorFullName, symptomNames, treatmentName, treatmentDescription,
                                        treatmentWayOfUse);
                } else { // Default to English
                        subject = "Treatment Information for " + symptomNames + " from Dr. " + doctorFullName;
                        body = String.format(
                                        "Dear Patient,\n\nDr. %s has sent you the following treatment information regarding %s:\n\nTreatment: %s\nDescription: %s\nUsage Instructions: %s\n\nPlease consult with your doctor for any further questions.",
                                        doctorFullName, symptomNames, treatmentName, treatmentDescription,
                                        treatmentWayOfUse);
                }

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(emailRequestDto.getPatientEmail());
                message.setSubject(subject);
                message.setText(body);
                // Set 'from' address from application.properties
                message.setFrom("meditreat@example.com");

                mailSender.send(message);
        }
}
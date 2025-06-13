package com.meditreat.app.controller;

import com.meditreat.app.dto.TreatmentDto;
import com.meditreat.app.service.TreatmentService;
import com.meditreat.app.dto.EmailRequestDto;
import com.meditreat.app.service.EmailService;
import com.meditreat.app.dto.TreatmentFormViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final EmailService emailService;

    @Autowired
    public TreatmentController(TreatmentService treatmentService, EmailService emailService) {
        this.treatmentService = treatmentService;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> createTreatment(@RequestBody TreatmentDto treatmentDto,
            Locale locale) {
        String language = (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                ? locale.getLanguage()
                : "en";
        try {
            TreatmentDto createdTreatment = treatmentService.createTreatment(treatmentDto, language);
            return new ResponseEntity<>(createdTreatment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log e for server-side details
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping
    public ResponseEntity<List<TreatmentDto>> getAllTreatments(
            Locale locale) {
        String language = (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                ? locale.getLanguage()
                : "en";
        List<TreatmentDto> treatments = treatmentService.getAllTreatments(language);
        if (treatments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentDto> getTreatmentById(@PathVariable Long id,
            Locale locale) {
        String language = (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                ? locale.getLanguage()
                : "en";
        return treatmentService.getTreatmentById(id, language)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/edit-details/{id}")
    public ResponseEntity<?> getTreatmentEditDetails(@PathVariable Long id,
            @RequestParam(name = "lang", required = false) String lang,
            Locale locale) {
        String effectiveLang = lang;
        // If lang parameter is not provided, try to use locale from header
        if (effectiveLang == null || effectiveLang.isEmpty()) {
            if (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty()) {
                effectiveLang = locale.getLanguage();
            } else {
                effectiveLang = "en"; // Default to English if neither is available
            }
        }

        try {
            TreatmentFormViewModel viewModel = treatmentService.prepareTreatmentEditViewModel(id, effectiveLang);
            return ResponseEntity.ok(viewModel);
        } catch (IllegalArgumentException e) { // Catching IllegalArgumentException as thrown by
                                               // prepareTreatmentEditViewModel
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Log e for server-side details
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching treatment details: " + e.getMessage());
        }
    }

    // Search by a single symptom name and treatment type name
    @GetMapping("/search")
    public ResponseEntity<List<TreatmentDto>> searchTreatmentsBySymptomAndType(
            @RequestParam("symptomName") String symptomName,
            @RequestParam("typeName") String typeName,
            Locale locale) {
        String language = (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                ? locale.getLanguage()
                : "en";
        try {
            List<TreatmentDto> treatments = treatmentService.findTreatments(symptomName, typeName, language);
            if (treatments.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(treatments);
        } catch (IllegalArgumentException e) {
            // Return a 400 Bad Request with an empty list or a specific error DTO if
            // preferred
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    // Search by multiple symptom IDs and a treatment type ID
    @GetMapping("/by-criteria")
    public ResponseEntity<List<TreatmentDto>> getTreatmentsByCriteria(
            @RequestParam(name = "symptomIds", required = false) Set<Long> symptomIds,
            @RequestParam(name = "typeId") Long typeId,
            Locale locale) {
        String language = (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                ? locale.getLanguage()
                : "en";
        try {
            List<TreatmentDto> treatments = treatmentService.findTreatmentsBySymptomsAndType(symptomIds, typeId,
                    language);
            if (treatments.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(treatments);
        } catch (IllegalArgumentException e) {
            // Return a 400 Bad Request with an empty list or a specific error DTO if
            // preferred
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTreatment(@PathVariable Long id,
            @RequestBody TreatmentDto treatmentDto,
            Locale locale) {
        String language = (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                ? locale.getLanguage()
                : "en";
        try {
            TreatmentDto updatedTreatment = treatmentService.updateTreatment(id, treatmentDto, language);
            return ResponseEntity.ok(updatedTreatment);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().toLowerCase().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTreatment(@PathVariable Long id) {
        try {
            treatmentService.deleteTreatment(id);
            return ResponseEntity.noContent().build(); // Standard for successful DELETE with no body
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Log e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/send-email")
    public ResponseEntity<?> sendTreatmentEmail(@RequestBody EmailRequestDto emailRequestDto) {
        System.out.println("Received email request: patientEmail=" + emailRequestDto.getPatientEmail()
                + ", treatmentId=" + emailRequestDto.getTreatmentId()
                + ", language=" + emailRequestDto.getLanguage()
                + ", doctorFullName=" + emailRequestDto.getDoctorFullName());

        try {
            // Process the email request
            emailService.sendTreatmentEmail(emailRequestDto);
            System.out.println("Email sent successfully");
            return ResponseEntity.ok().body("Email sent successfully.");
        } catch (NoSuchElementException e) {
            System.out.println("ERROR - Email not sent: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR - Email not sent: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace to server logs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending email: " + e.getMessage());
        }
    }
}
package com.meditreat.app.controller;

import com.meditreat.app.service.TreatmentTypeService;
import com.meditreat.model.TreatmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatment-types")
public class TreatmentTypeController {

    private final TreatmentTypeService treatmentTypeService;

    @Autowired
    public TreatmentTypeController(TreatmentTypeService treatmentTypeService) {
        this.treatmentTypeService = treatmentTypeService;
    }

    @PostMapping
    public ResponseEntity<TreatmentType> createTreatmentType(@RequestBody TreatmentType treatmentType,
            @RequestHeader("Accept-Language") String language) {
        treatmentType.setLanguage(language.split(",")[0].split("-")[0]);
        try {
            TreatmentType createdType = treatmentTypeService.createTreatmentType(treatmentType);
            return new ResponseEntity<>(createdType, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TreatmentType>> getAllTreatmentTypesByLanguage(
            @RequestHeader("Accept-Language") String language) {
        String lang = language.split(",")[0].split("-")[0];
        List<TreatmentType> types = treatmentTypeService.getAllTreatmentTypesByLanguage(lang);
        if (types.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(types);
    }

    @GetMapping("/all") // Added an endpoint to truly get ALL types regardless of language, if ever
                        // needed.
    public ResponseEntity<List<TreatmentType>> getAllTreatmentTypes() {
        List<TreatmentType> types = treatmentTypeService.getAllTreatmentTypes();
        if (types.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(types);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentType> getTreatmentTypeById(@PathVariable Long id) {
        return treatmentTypeService.getTreatmentTypeById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreatmentType> updateTreatmentType(@PathVariable Long id,
            @RequestBody TreatmentType typeDetails,
            @RequestHeader("Accept-Language") String language) {
        typeDetails.setLanguage(language.split(",")[0].split("-")[0]);
        try {
            TreatmentType updatedType = treatmentTypeService.updateTreatmentType(id, typeDetails);
            return ResponseEntity.ok(updatedType);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Or more specific
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTreatmentType(@PathVariable Long id) {
        try {
            treatmentTypeService.deleteTreatmentType(id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
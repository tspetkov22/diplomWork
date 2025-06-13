package com.meditreat.app.controller;

import com.meditreat.app.dto.SymptomDto;
import com.meditreat.app.dto.TreatmentDto;
import com.meditreat.app.service.FullTextSearchService;
import com.meditreat.app.service.FullTextSearchService.SearchResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final FullTextSearchService searchService;

    @Autowired
    public SearchController(FullTextSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Search for symptoms by text
     */
    @GetMapping("/symptoms")
    public ResponseEntity<List<SymptomDto>> searchSymptoms(
            @RequestParam("query") String query,
            @RequestHeader("Accept-Language") String acceptLanguage) {

        String language = acceptLanguage.split(",")[0].split("-")[0]; // Extract primary language
        List<SymptomDto> results = searchService.searchSymptoms(query, language);

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Search for treatments by text
     */
    @GetMapping("/treatments")
    public ResponseEntity<List<TreatmentDto>> searchTreatments(
            @RequestParam("query") String query,
            @RequestHeader("Accept-Language") String acceptLanguage) {

        String language = acceptLanguage.split(",")[0].split("-")[0]; // Extract primary language
        List<TreatmentDto> results = searchService.searchTreatments(query, language);

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Search across all entities
     */
    @GetMapping("/all")
    public ResponseEntity<SearchResults> searchAll(
            @RequestParam("query") String query,
            @RequestHeader("Accept-Language") String acceptLanguage) {

        String language = acceptLanguage.split(",")[0].split("-")[0]; // Extract primary language
        SearchResults results = searchService.searchAll(query, language);

        if (results.getSymptoms().isEmpty() && results.getTreatments().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Search for symptoms with translations
     */
    @GetMapping("/symptoms-with-translations")
    public ResponseEntity<List<SymptomDto>> searchSymptomsWithTranslations(
            @RequestParam("query") String query,
            @RequestHeader("Accept-Language") String acceptLanguage,
            @RequestParam("targetLanguage") String targetLanguage) {

        String sourceLanguage = acceptLanguage.split(",")[0].split("-")[0]; // Extract primary language

        // Use the SymptomService to search with translations
        List<SymptomDto> results = searchService.searchSymptoms(query, sourceLanguage);

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(results);
    }
}
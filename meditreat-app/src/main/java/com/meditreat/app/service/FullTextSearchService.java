package com.meditreat.app.service;

import com.meditreat.app.dto.SymptomDto;
import com.meditreat.app.dto.TreatmentDto;
import com.meditreat.app.dto.TreatmentTypeDto;
import com.meditreat.model.Symptom;
import com.meditreat.model.Treatment;
import com.meditreat.repository.SymptomRepository;
import com.meditreat.repository.TreatmentRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FullTextSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    private final SymptomService symptomService;
    private final SymptomRepository symptomRepository;
    private final TreatmentRepository treatmentRepository;

    @Autowired
    public FullTextSearchService(
            SymptomService symptomService,
            SymptomRepository symptomRepository,
            TreatmentRepository treatmentRepository) {
        this.symptomService = symptomService;
        this.symptomRepository = symptomRepository;
        this.treatmentRepository = treatmentRepository;
    }

    /**
     * Search for symptoms by text and language
     * 
     * @param searchText The text to search for
     * @param language   The language to search in
     * @return List of matching symptoms
     */
    @Transactional(readOnly = true)
    public List<SymptomDto> searchSymptoms(String searchText, String language) {
        // Use JPA Criteria API for searching
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Symptom> query = cb.createQuery(Symptom.class);
        Root<Symptom> root = query.from(Symptom.class);

        // Create predicates for search
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("language"), language));
        predicates.add(cb.like(cb.lower(root.get("name")), "%" + searchText.toLowerCase() + "%"));

        // Combine predicates and execute
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        List<Symptom> symptoms = entityManager.createQuery(query).setMaxResults(20).getResultList();

        // Convert to DTOs
        return symptoms.stream()
                .map(symptomService::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search for treatments by text and language
     * 
     * @param searchText The text to search for
     * @param language   The language to search in
     * @return List of matching treatments
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto> searchTreatments(String searchText, String language) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Treatment> query = cb.createQuery(Treatment.class);
        Root<Treatment> root = query.from(Treatment.class);

        // Create predicates for search
        String searchPattern = "%" + searchText.toLowerCase() + "%";
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("language"), language));

        // Search in multiple fields
        List<Predicate> fieldPredicates = new ArrayList<>();
        fieldPredicates.add(cb.like(cb.lower(root.get("name")), searchPattern));
        fieldPredicates.add(cb.like(cb.lower(root.get("description")), searchPattern));
        fieldPredicates.add(cb.like(cb.lower(root.get("usageInstructions")), searchPattern));

        // Combine predicates and execute
        predicates.add(cb.or(fieldPredicates.toArray(new Predicate[0])));
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);

        List<Treatment> treatments = entityManager.createQuery(query).setMaxResults(20).getResultList();

        // Convert to DTOs
        return treatments.stream()
                .map(this::convertTreatmentToDto)
                .collect(Collectors.toList());
    }

    /**
     * Manually convert Treatment to TreatmentDto
     */
    private TreatmentDto convertTreatmentToDto(Treatment treatment) {
        TreatmentDto dto = new TreatmentDto();
        dto.setId(treatment.getId());
        dto.setName(treatment.getName());
        dto.setDescription(treatment.getDescription());
        dto.setUsageInstructions(treatment.getUsageInstructions());
        dto.setRecommendedDose(treatment.getRecommendedDose());
        dto.setLanguage(treatment.getLanguage());

        // Handle type if not null
        if (treatment.getType() != null) {
            dto.setType(new TreatmentTypeDto(
                    treatment.getType().getId(),
                    treatment.getType().getName(),
                    treatment.getType().getLanguage()));
        }

        // Convert symptoms if any
        if (treatment.getSymptoms() != null && !treatment.getSymptoms().isEmpty()) {
            Set<SymptomDto> symptomDtos = new HashSet<>();
            for (Symptom symptom : treatment.getSymptoms()) {
                symptomDtos.add(symptomService.toDto(symptom));
            }
            dto.setSymptoms(symptomDtos);
        }

        return dto;
    }

    /**
     * Multi-entity search across symptoms and treatments
     * 
     * @param searchText The text to search for
     * @param language   The language to search in
     * @return Object with lists of matching symptoms and treatments
     */
    @Transactional(readOnly = true)
    public SearchResults searchAll(String searchText, String language) {
        List<SymptomDto> symptoms = searchSymptoms(searchText, language);
        List<TreatmentDto> treatments = searchTreatments(searchText, language);

        return new SearchResults(symptoms, treatments);
    }

    /**
     * Container class for combined search results
     */
    public static class SearchResults {
        private final List<SymptomDto> symptoms;
        private final List<TreatmentDto> treatments;

        public SearchResults(List<SymptomDto> symptoms, List<TreatmentDto> treatments) {
            this.symptoms = symptoms;
            this.treatments = treatments;
        }

        public List<SymptomDto> getSymptoms() {
            return symptoms;
        }

        public List<TreatmentDto> getTreatments() {
            return treatments;
        }
    }
}
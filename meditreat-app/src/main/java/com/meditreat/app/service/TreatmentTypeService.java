package com.meditreat.app.service;

import com.meditreat.model.TreatmentType;
import com.meditreat.repository.TreatmentTypeRepository;
import com.meditreat.app.dto.TreatmentTypeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TreatmentTypeService {

    private final TreatmentTypeRepository treatmentTypeRepository;

    @Autowired
    public TreatmentTypeService(TreatmentTypeRepository treatmentTypeRepository) {
        this.treatmentTypeRepository = treatmentTypeRepository;
    }

    @Transactional
    public TreatmentType createTreatmentType(TreatmentType treatmentType) {
        Optional<TreatmentType> existing = treatmentTypeRepository.findByNameAndLanguage(treatmentType.getName(),
                treatmentType.getLanguage());
        if (existing.isPresent()) {
            throw new IllegalStateException("TreatmentType with name '" + treatmentType.getName() + "' and language '"
                    + treatmentType.getLanguage() + "' already exists.");
        }
        return treatmentTypeRepository.save(treatmentType);
    }

    @Transactional(readOnly = true)
    public List<TreatmentType> getAllTreatmentTypes() {
        return treatmentTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TreatmentType> getAllTreatmentTypesByLanguage(String language) {
        return treatmentTypeRepository.findByLanguage(language);
    }

    @Transactional(readOnly = true)
    public List<TreatmentTypeDto> getAllTreatmentTypeDtosByLanguage(String language) {
        return treatmentTypeRepository.findByLanguage(language).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private TreatmentTypeDto toDto(TreatmentType treatmentType) {
        if (treatmentType == null)
            return null;
        return new TreatmentTypeDto(treatmentType.getId(), treatmentType.getName(), treatmentType.getLanguage());
    }

    @Transactional(readOnly = true)
    public Optional<TreatmentType> getTreatmentTypeById(Long id) {
        return treatmentTypeRepository.findById(id);
    }

    // Add update and delete methods if needed
    @Transactional
    public TreatmentType updateTreatmentType(Long id, TreatmentType typeDetails) {
        TreatmentType type = treatmentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("TreatmentType not found with id: " + id));

        Optional<TreatmentType> existing = treatmentTypeRepository.findByNameAndLanguage(typeDetails.getName(),
                typeDetails.getLanguage());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalStateException("Another TreatmentType with name '" + typeDetails.getName()
                    + "' and language '" + typeDetails.getLanguage() + "' already exists.");
        }

        type.setName(typeDetails.getName());
        type.setLanguage(typeDetails.getLanguage());
        return treatmentTypeRepository.save(type);
    }

    @Transactional
    public void deleteTreatmentType(Long id) {
        if (!treatmentTypeRepository.existsById(id)) {
            throw new IllegalStateException("TreatmentType not found with id: " + id);
        }
        treatmentTypeRepository.deleteById(id);
    }
}
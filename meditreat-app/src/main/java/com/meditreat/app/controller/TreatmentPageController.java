package com.meditreat.app.controller;

import com.meditreat.app.service.TreatmentService;
import com.meditreat.app.service.TreatmentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Locale;

@Controller
public class TreatmentPageController {

    private final TreatmentService treatmentService;
    private final TreatmentTypeService treatmentTypeService;

    @Autowired
    public TreatmentPageController(TreatmentService treatmentService, TreatmentTypeService treatmentTypeService) {
        this.treatmentService = treatmentService;
        this.treatmentTypeService = treatmentTypeService;
    }

    @GetMapping("/add-treatment")
    public String showAddTreatmentForm(Model model, Locale locale) {
        String language = locale.getLanguage();
        if (language == null || language.isEmpty()) {
            language = "en"; // Default to English
        }
        model.addAttribute("currentLanguage", language);
        model.addAttribute("treatmentTypes", treatmentTypeService.getAllTreatmentTypeDtosByLanguage(language));
        // Initialize an empty DTO or ViewModel if needed for form binding, similar to
        // edit but empty
        // model.addAttribute("treatmentDto", new TreatmentDto());
        return "add-treatment";
    }

}
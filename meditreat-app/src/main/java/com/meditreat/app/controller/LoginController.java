package com.meditreat.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    public LoginController() {
    }

    @GetMapping("/")
    public String login() {
        return "login"; // This will look for login.html in src/main/resources/templates
    }

    @GetMapping("/home")
    public String home() {
        return "home"; // This will look for home.html in src/main/resources/templates
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin"; // This will look for admin.html in src/main/resources/templates
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile"; // This will look for profile.html in src/main/resources/templates
    }

    @GetMapping("/all-treatments")
    public String allTreatments() {
        return "all-treatments"; // This will look for all-treatments.html in src/main/resources/templates
    }
}
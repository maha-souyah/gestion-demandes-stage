package com.demande.dmstage.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String afficherLogin() {
        return "login"; // nom du fichier html dans templates (login.html)
    }
}

package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.services.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@Controller
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "inscription";  // ici on retourne inscription.html
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute Utilisateur utilisateur, Model model) {
        try {
            utilisateurService.creerCompte(utilisateur);
            return "redirect:/login?registerSuccess";
        } catch (Exception e) {
            model.addAttribute("error", "Email déjà utilisé");
            return "inscription";
        }
    }
}

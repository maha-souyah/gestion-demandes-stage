package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurService utilisateurService;

    public AuthController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody Utilisateur utilisateur) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Tous les nouveaux utilisateurs sont des USER
            utilisateur.setRole(Utilisateur.Role.USER);
            
            Utilisateur nouvelUtilisateur = utilisateurService.creerCompte(utilisateur);
            response.put("success", true);
            response.put("message", "Inscription réussie");
            response.put("utilisateur", Map.of(
                "id", nouvelUtilisateur.getId(),
                "nom", nouvelUtilisateur.getNom(),
                "email", nouvelUtilisateur.getEmail(),
                "role", nouvelUtilisateur.getRole().name()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = credentials.get("email");
            String motDePasse = credentials.get("motDePasse");
            
            System.out.println("=== TENTATIVE CONNEXION ===");
            System.out.println("Email: " + email);
            
            Optional<Utilisateur> utilisateur = utilisateurService.authentifier(email, motDePasse);
            
            if (utilisateur.isPresent()) {
                System.out.println("Connexion réussie pour: " + utilisateur.get().getNom() + " (" + utilisateur.get().getRole() + ")");
                
                response.put("success", true);
                response.put("message", "Connexion réussie");
                response.put("utilisateur", Map.of(
                    "id", utilisateur.get().getId(),
                    "nom", utilisateur.get().getNom(),
                    "email", utilisateur.get().getEmail(),
                    "role", utilisateur.get().getRole().name()
                ));
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Échec de connexion pour: " + email);
                response.put("success", false);
                response.put("message", "Email ou mot de passe invalide");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            System.out.println("ERREUR lors de la connexion: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Erreur lors de la connexion");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            
            Optional<Utilisateur> utilisateur = utilisateurService.trouverParEmail(email);
            
            if (utilisateur.isPresent()) {
                response.put("success", true);
                response.put("utilisateur", Map.of(
                    "id", utilisateur.get().getId(),
                    "nom", utilisateur.get().getNom(),
                    "email", utilisateur.get().getEmail(),
                    "role", utilisateur.get().getRole().name()
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération du profil");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
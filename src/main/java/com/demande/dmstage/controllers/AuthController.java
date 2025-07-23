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

    /**
     * Endpoint pour créer des comptes administrateurs
     * Modifié pour permettre plusieurs admins (limite de 5 pour sécurité entreprise)
     */
    @PostMapping("/create-admin")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Map<String, String> adminData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifier la limite d'admins (5 maximum pour une entreprise)
            long nombreAdmins = utilisateurService.compterUtilisateursParRole(Utilisateur.Role.ADMIN);
            if (nombreAdmins >= 5) {
                response.put("success", false);
                response.put("message", "Limite maximale d'administrateurs atteinte (5 maximum)");
                response.put("current_admins", nombreAdmins);
                response.put("max_admins", 5);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Créer le compte admin avec les données fournies ou par défaut
            String nom = adminData.getOrDefault("nom", "Admin");
            String email = adminData.getOrDefault("email", "admin@berkane.ma");
            String motDePasse = adminData.getOrDefault("motDePasse", "admin123456");
            
            System.out.println("=== CRÉATION ADMIN ===");
            System.out.println("Nom: " + nom);
            System.out.println("Email: " + email);
            System.out.println("Admins actuels: " + nombreAdmins);
            
            Utilisateur admin = new Utilisateur();
            admin.setNom(nom);
            admin.setEmail(email);
            admin.setMotDePasse(motDePasse);
            admin.setRole(Utilisateur.Role.ADMIN);
            
            Utilisateur nouvelAdmin = utilisateurService.creerCompte(admin);
            
            System.out.println("Admin créé avec succès - ID: " + nouvelAdmin.getId());
            
            response.put("success", true);
            response.put("message", "Compte administrateur créé avec succès");
            response.put("admin", Map.of(
                "id", nouvelAdmin.getId(),
                "nom", nouvelAdmin.getNom(),
                "email", nouvelAdmin.getEmail(),
                "role", nouvelAdmin.getRole().name()
            ));
            response.put("total_admins", nombreAdmins + 1);
            response.put("remaining_slots", 5 - (nombreAdmins + 1));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERREUR lors de la création admin: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la création de l'admin: " + e.getMessage());
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
                System.out.println("Connexion réussie pour: " + utilisateur.get().getNom() + 
                                 " (" + utilisateur.get().getRole() + ")");
                
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

    /**
     * Endpoint pour vérifier le statut du système (nombre d'admins, etc.)
     */
    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalUtilisateurs = utilisateurService.compterUtilisateurs();
            long nombreAdmins = utilisateurService.compterUtilisateursParRole(Utilisateur.Role.ADMIN);
            long nombreUsers = utilisateurService.compterUtilisateursParRole(Utilisateur.Role.USER);
            
            response.put("success", true);
            response.put("statistiques", Map.of(
                "total_utilisateurs", totalUtilisateurs,
                "nombre_admins", nombreAdmins,
                "nombre_users", nombreUsers,
                "admin_existe", nombreAdmins > 0,
                "max_admins", 5,
                "slots_disponibles", 5 - nombreAdmins
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération du statut système");
            return ResponseEntity.badRequest().body(response);
        }
    }
    /**
 * MÉTHODE TEMPORAIRE DE CONTOURNEMENT - SANS SÉCURITÉ
 */
@PostMapping("/create-admin-bypass")
public ResponseEntity<Map<String, Object>> createAdminBypass(@RequestBody Map<String, String> adminData) {
    Map<String, Object> response = new HashMap<>();
    
    try {
        System.out.println("🚨 CRÉATION ADMIN - CONTOURNEMENT SÉCURITÉ");
        System.out.println("📝 Données reçues: " + adminData);
        
        // Vérifier la limite d'admins (5 maximum pour une entreprise)
        long nombreAdmins = utilisateurService.compterUtilisateursParRole(Utilisateur.Role.ADMIN);
        if (nombreAdmins >= 5) {
            response.put("success", false);
            response.put("message", "Limite maximale d'administrateurs atteinte (5 maximum)");
            response.put("current_admins", nombreAdmins);
            response.put("max_admins", 5);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Créer le compte admin avec les données fournies ou par défaut
        String nom = adminData.getOrDefault("nom", "Admin");
        String email = adminData.getOrDefault("email", "admin@berkane.ma");
        String motDePasse = adminData.getOrDefault("motDePasse", "admin123456");
        
        System.out.println("=== CRÉATION ADMIN BYPASS ===");
        System.out.println("Nom: " + nom);
        System.out.println("Email: " + email);
        System.out.println("Admins actuels: " + nombreAdmins);
        
        Utilisateur admin = new Utilisateur();
        admin.setNom(nom);
        admin.setEmail(email);
        admin.setMotDePasse(motDePasse);
        admin.setRole(Utilisateur.Role.ADMIN);
        
        Utilisateur nouvelAdmin = utilisateurService.creerCompte(admin);
        
        System.out.println("Admin créé avec succès - ID: " + nouvelAdmin.getId());
        
        response.put("success", true);
        response.put("message", "Compte administrateur créé avec succès (BYPASS)");
        response.put("admin", Map.of(
            "id", nouvelAdmin.getId(),
            "nom", nouvelAdmin.getNom(),
            "email", nouvelAdmin.getEmail(),
            "role", nouvelAdmin.getRole().name()
        ));
        response.put("total_admins", nombreAdmins + 1);
        response.put("remaining_slots", 5 - (nombreAdmins + 1));
        response.put("method", "BYPASS - SECURITY DISABLED");
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        System.out.println("ERREUR lors de la création admin bypass: " + e.getMessage());
        e.printStackTrace();
        response.put("success", false);
        response.put("message", "Erreur lors de la création de l'admin: " + e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
}
package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.services.DemandeStageService;
import com.demande.dmstage.services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DemandeStageService demandeStageService;
    private final UtilisateurService utilisateurService;

    public AdminController(DemandeStageService demandeStageService, 
                          UtilisateurService utilisateurService) {
        this.demandeStageService = demandeStageService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/demandes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getToutesLesDemandes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<DemandeStage> demandes = demandeStageService.toutesLesDemandes();
            response.put("success", true);
            response.put("data", demandes);
            response.put("total", demandes.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des demandes");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/demandes/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> modifierStatutDemande(
            @PathVariable Long id,
            @RequestBody Map<String, String> statutData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String statut = statutData.get("statut");
            String commentaire = statutData.get("commentaire");
            
            DemandeStage demande = demandeStageService.modifierStatut(id, statut);
            if (commentaire != null && !commentaire.trim().isEmpty()) {
                demande.setCommentaire(commentaire);
                demande = demandeStageService.creerDemande(demande);
            }
            
            response.put("success", true);
            response.put("message", "Statut modifié avec succès");
            response.put("data", demande);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la modification du statut");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/utilisateurs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTousLesUtilisateurs() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Utilisateur> utilisateurs = utilisateurService.tousLesUtilisateurs();
            response.put("success", true);
            response.put("data", utilisateurs);
            response.put("total", utilisateurs.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des utilisateurs");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/utilisateurs/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> modifierRoleUtilisateur(
            @PathVariable Long id,
            @RequestBody Map<String, String> roleData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String role = roleData.get("role");
            Utilisateur utilisateur = utilisateurService.modifierRole(id, Utilisateur.Role.valueOf(role));
            
            response.put("success", true);
            response.put("message", "Rôle modifié avec succès");
            response.put("data", Map.of(
                "id", utilisateur.getId(),
                "nom", utilisateur.getNom(),
                "email", utilisateur.getEmail(),
                "role", utilisateur.getRole().name()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la modification du rôle");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = demandeStageService.getStatistiques();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des statistiques");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
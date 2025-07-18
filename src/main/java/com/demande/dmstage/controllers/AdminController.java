package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.services.DemandeStageService;
import com.demande.dmstage.services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public ResponseEntity<Map<String, Object>> getToutesLesDemandes(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String typeStage,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String search) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== ADMIN - RECHERCHE DEMANDES ===");
            System.out.println("Filtres reçus:");
            System.out.println("- Nom: " + nom);
            System.out.println("- Prénom: " + prenom);
            System.out.println("- Type stage: " + typeStage);
            System.out.println("- Statut: " + statut);
            System.out.println("- Date début: " + dateDebut);
            System.out.println("- Recherche: " + search);
            
            // Récupérer toutes les demandes
            List<DemandeStage> toutesLesDemandes = demandeStageService.toutesLesDemandes();
            System.out.println("Total demandes en base: " + toutesLesDemandes.size());
            
            // Appliquer les filtres (même logique que dans DemandeStageController)
            List<DemandeStage> demandesFiltrees = new ArrayList<>();
            
            for (DemandeStage demande : toutesLesDemandes) {
                boolean estRetenue = true;
                
                // Filtre par nom
                if (nom != null && !nom.trim().isEmpty()) {
                    if (!demande.getNom().toLowerCase().contains(nom.toLowerCase())) {
                        estRetenue = false;
                    }
                }
                
                // Filtre par prénom
                if (estRetenue && prenom != null && !prenom.trim().isEmpty()) {
                    if (!demande.getPrenom().toLowerCase().contains(prenom.toLowerCase())) {
                        estRetenue = false;
                    }
                }
                
                // Filtre par type de stage
                if (estRetenue && typeStage != null && !typeStage.trim().isEmpty()) {
                    if (!demande.getTypeStage().equalsIgnoreCase(typeStage)) {
                        estRetenue = false;
                    }
                }
                
                // Filtre par statut
                if (estRetenue && statut != null && !statut.trim().isEmpty()) {
                    if (!demande.getStatut().equalsIgnoreCase(statut)) {
                        estRetenue = false;
                    }
                }
                
                // Recherche globale
                if (estRetenue && search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase();
                    boolean searchMatch = demande.getNom().toLowerCase().contains(searchLower) ||
                                        demande.getPrenom().toLowerCase().contains(searchLower) ||
                                        demande.getEmail().toLowerCase().contains(searchLower) ||
                                        demande.getCin().toLowerCase().contains(searchLower);
                    if (!searchMatch) {
                        estRetenue = false;
                    }
                }
                
                if (estRetenue) {
                    demandesFiltrees.add(demande);
                }
            }
            
            System.out.println("Demandes trouvées: " + demandesFiltrees.size());
            
            response.put("success", true);
            response.put("data", demandesFiltrees);
            response.put("total", demandesFiltrees.size());
            response.put("filtres", Map.of(
                "nom", nom != null ? nom : "",
                "prenom", prenom != null ? prenom : "",
                "typeStage", typeStage != null ? typeStage : "",
                "statut", statut != null ? statut : "",
                "dateDebut", dateDebut != null ? dateDebut : "",
                "search", search != null ? search : ""
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERREUR lors de la recherche admin: " + e.getMessage());
            e.printStackTrace();
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
            
            System.out.println("=== ADMIN - MODIFICATION STATUT ===");
            System.out.println("ID demande: " + id);
            System.out.println("Nouveau statut: " + statut);
            System.out.println("Commentaire: " + commentaire);
            
            // Vérifier que le statut est valide
            if (!statut.equals("ACCEPTE") && !statut.equals("REFUSE") && !statut.equals("EN_ATTENTE")) {
                response.put("success", false);
                response.put("message", "Statut invalide. Utilisez: ACCEPTE, REFUSE ou EN_ATTENTE");
                return ResponseEntity.badRequest().body(response);
            }
            
            DemandeStage demande = demandeStageService.modifierStatut(id, statut);
            if (commentaire != null && !commentaire.trim().isEmpty()) {
                demande.setCommentaire(commentaire);
                demande = demandeStageService.creerDemande(demande);
            }
            
            System.out.println("Statut modifié avec succès");
            
            response.put("success", true);
            response.put("message", "Statut modifié avec succès");
            response.put("data", demande);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERREUR lors de la modification: " + e.getMessage());
            e.printStackTrace();
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
}
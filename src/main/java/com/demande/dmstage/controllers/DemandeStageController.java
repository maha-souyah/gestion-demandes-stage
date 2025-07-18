package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.services.DemandeStageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
public class DemandeStageController {

    private final DemandeStageService service;

    public DemandeStageController(DemandeStageService service) {
        this.service = service;
    }

    @GetMapping("/suivi")
    public ResponseEntity<Map<String, Object>> suiviDemande(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<DemandeStage> demandes = service.trouverParEmail(email);
            if (demandes.isEmpty()) {
                response.put("success", false);
                response.put("message", "Aucune demande trouvée pour cet email");
                return ResponseEntity.ok(response);
            }
            
            response.put("success", true);
            response.put("data", demandes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la recherche");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> creerDemande(
            @ModelAttribute DemandeStage demande,
            @RequestParam("convention_stage") MultipartFile conventionStage,
            @RequestParam("demande_stage") MultipartFile demandeStage,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("lettre_motivation") MultipartFile lettreMotivation,
            @RequestParam("cin_recto") MultipartFile cinRecto,
            @RequestParam("cin_verso") MultipartFile cinVerso,
            @RequestParam("photo") MultipartFile photo
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            demande.setConventionStage(conventionStage.getOriginalFilename());
            demande.setDemandeStage(demandeStage.getOriginalFilename());
            demande.setCv(cv.getOriginalFilename());
            demande.setLettreMotivation(lettreMotivation.getOriginalFilename());
            demande.setCinRecto(cinRecto.getOriginalFilename());
            demande.setCinVerso(cinVerso.getOriginalFilename());
            demande.setPhoto(photo.getOriginalFilename());

            DemandeStage nouvelleDemande = service.creerDemande(demande);
            
            response.put("success", true);
            response.put("message", "Demande créée avec succès");
            response.put("data", nouvelleDemande);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la création de la demande: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

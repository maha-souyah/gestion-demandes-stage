package com.demande.dmstage.services;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.repositories.DemandeStageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DemandeStageService {

    private final DemandeStageRepository repository;

    public DemandeStageService(DemandeStageRepository repository) {
        this.repository = repository;
    }

    public DemandeStage creerDemande(DemandeStage demande) {
        return repository.save(demande);
    }

    public List<DemandeStage> toutesLesDemandes() {
        return repository.findAll();
    }

    public Optional<DemandeStage> trouverParId(Long id) {
        return repository.findById(id);
    }

    public DemandeStage modifierStatut(Long id, String statut) {
        DemandeStage demande = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        
        demande.setStatut(statut);
        demande.setDateTraitement(LocalDateTime.now());
        
        return repository.save(demande);
    }

    public List<DemandeStage> trouverParEmail(String email) {
        return repository.findByEmail(email);
    }

    public Map<String, Object> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();
        
        List<DemandeStage> toutesLesDemandes = repository.findAll();
        
        // Statistiques générales
        stats.put("totalDemandes", toutesLesDemandes.size());
        
        // Statistiques par statut
        long enAttente = toutesLesDemandes.stream()
            .filter(d -> "EN_ATTENTE".equals(d.getStatut()))
            .count();
        long acceptees = toutesLesDemandes.stream()
            .filter(d -> "ACCEPTE".equals(d.getStatut()))
            .count();
        long refusees = toutesLesDemandes.stream()
            .filter(d -> "REFUSE".equals(d.getStatut()))
            .count();
        long enCours = toutesLesDemandes.stream()
            .filter(d -> "EN_COURS_DE_TRAITEMENT".equals(d.getStatut()))
            .count();
            
        Map<String, Long> parStatut = new HashMap<>();
        parStatut.put("EN_ATTENTE", enAttente);
        parStatut.put("ACCEPTE", acceptees);
        parStatut.put("REFUSE", refusees);
        parStatut.put("EN_COURS_DE_TRAITEMENT", enCours);
        stats.put("parStatut", parStatut);
        
        // Statistiques par type de stage
        Map<String, Long> parType = new HashMap<>();
        parType.put("normal", toutesLesDemandes.stream()
            .filter(d -> "normal".equals(d.getTypeStage()))
            .count());
        parType.put("initiation", toutesLesDemandes.stream()
            .filter(d -> "initiation".equals(d.getTypeStage()))
            .count());
        parType.put("fin_formation", toutesLesDemandes.stream()
            .filter(d -> "fin_formation".equals(d.getTypeStage()))
            .count());
        parType.put("pfe", toutesLesDemandes.stream()
            .filter(d -> "pfe".equals(d.getTypeStage()))
            .count());
        stats.put("parTypeStage", parType);
        
        return stats;
    }
}
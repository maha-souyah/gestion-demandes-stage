package com.demande.dmstage.services;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.repositories.DemandeStageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
}
package com.demande.dmstage.repositories;

import com.demande.dmstage.entities.DemandeStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeStageRepository extends JpaRepository<DemandeStage, Long> {
    List<DemandeStage> findByEmail(String email);
}

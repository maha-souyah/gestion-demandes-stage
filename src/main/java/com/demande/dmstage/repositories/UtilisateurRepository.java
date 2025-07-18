package com.demande.dmstage.repositories;

import com.demande.dmstage.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByEmail(String email);
    
    long countByRole(Utilisateur.Role role);
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.actif = true")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.actif = false")
    long countInactiveUsers();
}
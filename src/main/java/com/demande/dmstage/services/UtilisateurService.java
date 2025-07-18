package com.demande.dmstage.services;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.repositories.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Utilisateur creerCompte(Utilisateur utilisateur) {
        // Vérifier si l'email existe déjà
        Optional<Utilisateur> existant = repository.findByEmail(utilisateur.getEmail());
        if (existant.isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }
        
        // Encoder le mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        
        // Par défaut, le rôle est USER (sauf si spécifié autrement)
        if (utilisateur.getRole() == null) {
            utilisateur.setRole(Utilisateur.Role.USER);
        }
        
        return repository.save(utilisateur);
    }

    public Optional<Utilisateur> authentifier(String email, String motDePasse) {
        Optional<Utilisateur> userOpt = repository.findByEmail(email);
        if (userOpt.isPresent()) {
            Utilisateur utilisateur = userOpt.get();
            if (passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse()) && utilisateur.getActif()) {
                return Optional.of(utilisateur);
            }
        }
        return Optional.empty();
    }

    public Optional<Utilisateur> trouverParEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<Utilisateur> trouverParId(Long id) {
        return repository.findById(id);
    }

    public List<Utilisateur> tousLesUtilisateurs() {
        return repository.findAll();
    }

    public Utilisateur modifierRole(Long id, Utilisateur.Role nouveauRole) {
        Utilisateur utilisateur = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        utilisateur.setRole(nouveauRole);
        return repository.save(utilisateur);
    }

    public Utilisateur activerDesactiverUtilisateur(Long id, Boolean actif) {
        Utilisateur utilisateur = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        utilisateur.setActif(actif);
        return repository.save(utilisateur);
    }

    public long compterUtilisateurs() {
        return repository.count();
    }

    public long compterUtilisateursParRole(Utilisateur.Role role) {
        return repository.countByRole(role);
    }
}
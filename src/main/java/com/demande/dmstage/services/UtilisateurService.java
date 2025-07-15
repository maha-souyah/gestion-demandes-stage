package com.demande.dmstage.services;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.repositories.UtilisateurRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UtilisateurService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    public Utilisateur creerCompte(Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return repository.save(utilisateur);
    }

    public Optional<Utilisateur> authentifier(String email, String motDePasse) {
        Optional<Utilisateur> userOpt = repository.findByEmail(email);
        if (userOpt.isPresent()) {
            Utilisateur utilisateur = userOpt.get();
            if (passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
                return Optional.of(utilisateur);
            }
        }
        return Optional.empty();
    }
}

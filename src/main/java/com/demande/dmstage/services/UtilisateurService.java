package com.demande.dmstage.services;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.repositories.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService implements UserDetailsService {

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

    // Cette méthode est appelée par Spring Security lors de la connexion
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec email : " + email));

        // Ici on crée un UserDetails Spring Security à partir de ton Utilisateur
        return new org.springframework.security.core.userdetails.User(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // ici on fixe un rôle USER
        );
    }
}

package com.demande.dmstage.entities;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.boot.context.properties.bind.DefaultValue;

import com.mysql.cj.protocol.a.LocalDateTimeValueEncoder;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private Boolean actif = true;

    private LocalDateTime dateCreation = LocalDateTime.now();

    // Enum pour les rôles
    public enum Role {
        USER, ADMIN
    }

    // Constructeurs
    public Utilisateur() {
        this.dateCreation = LocalDateTime.now();
        this.role = Role.USER;
        this.actif = true;
    }

    public Utilisateur(String nom, String email, String motDePasse) {
        this();
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
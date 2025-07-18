package com.demande.dmstage.entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes_stage")
public class DemandeStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String sexe;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false, unique = true)
    private String cin;

    @Column(nullable = false)
    private String adresseDomicile;

    @Column(nullable = false)
    private String typeStage;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private String duree;

    // Fichiers
    @Column(name = "convention_stage")
    private String conventionStage;

    @Column(name = "demande_stage")
    private String demandeStage;

    @Column(name = "cv")
    private String cv;

    @Column(name = "lettre_motivation")
    private String lettreMotivation;

    @Column(name = "cin_recto")
    private String cinRecto;

    @Column(name = "cin_verso")
    private String cinVerso;

    @Column(name = "photo")
    private String photo;

    // Statut et dates
    @Column(nullable = false)
    private String statut = "EN_ATTENTE";

    @Column(nullable = false)
    private LocalDateTime dateDemande;

    @Column
    private LocalDateTime dateTraitement;

    @Column
    private String commentaire;

    // CONSTRUCTEUR UNIQUE - Supprimez les doublons
    public DemandeStage() {
        this.dateDemande = LocalDateTime.now();
        this.statut = "EN_ATTENTE";
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getAdresseDomicile() { return adresseDomicile; }
    public void setAdresseDomicile(String adresseDomicile) { this.adresseDomicile = adresseDomicile; }

    public String getTypeStage() { return typeStage; }
    public void setTypeStage(String typeStage) { this.typeStage = typeStage; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }

    public String getConventionStage() { return conventionStage; }
    public void setConventionStage(String conventionStage) { this.conventionStage = conventionStage; }

    public String getDemandeStage() { return demandeStage; }
    public void setDemandeStage(String demandeStage) { this.demandeStage = demandeStage; }

    public String getCv() { return cv; }
    public void setCv(String cv) { this.cv = cv; }

    public String getLettreMotivation() { return lettreMotivation; }
    public void setLettreMotivation(String lettreMotivation) { this.lettreMotivation = lettreMotivation; }

    public String getCinRecto() { return cinRecto; }
    public void setCinRecto(String cinRecto) { this.cinRecto = cinRecto; }

    public String getCinVerso() { return cinVerso; }
    public void setCinVerso(String cinVerso) { this.cinVerso = cinVerso; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public LocalDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDateTime dateTraitement) { this.dateTraitement = dateTraitement; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
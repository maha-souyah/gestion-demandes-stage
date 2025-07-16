package com.demande.dmstage.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class DemandeStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String sexe;
    private String email;
    private String telephone;
    private String cin;
    private String adresseDomicile;
    private String typeStage;

    private LocalDate dateDebut;

    private String duree;
    private String conventionStage;
    private String demandeStage;
    private String cv;
    private String lettreMotivation;
    private String cinRecto;
    private String cinVerso;
    private String photo;
    private String statut;
    private LocalDate dateDemande;

    public DemandeStage() {
        this.dateDemande = LocalDate.now();
        this.statut = "EN_ATTENTE";
    }

    // Getters & Setters

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

    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }
}

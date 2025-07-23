package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.services.DemandeStageService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
public class DemandeStageController {

    private final DemandeStageService service;

    public DemandeStageController(DemandeStageService service) {
        this.service = service;
    }

    /**
     * SUIVI PUBLIC ET SÉCURISÉ PAR EMAIL
     * - Sans authentification : suivi public (n'importe qui peut suivre avec un email)
     * - Avec authentification : suivi sécurisé (utilisateur ne peut suivre que ses demandes, admin peut tout suivre)
     */
    @GetMapping("/suivi")
    public ResponseEntity<Map<String, Object>> suiviDemandeParEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== SUIVI PAR EMAIL ===");
            System.out.println("Email recherché: '" + email + "'");
            
          // Vérifier si l'utilisateur est authentifié
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
boolean estAuthentifie = auth != null && auth.isAuthenticated() && 
                       auth.getName() != null && !auth.getName().equals("anonymousUser");

String modeAcces;

if (estAuthentifie) {
    // MODE SÉCURISÉ - Utilisateur authentifié
    String emailUtilisateur = auth.getName(); // Maintenant sûr car vérifié au-dessus
    boolean estAdmin = auth.getAuthorities().stream()
        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    
    System.out.println("Utilisateur authentifié: " + emailUtilisateur);
    System.out.println("Est admin: " + estAdmin);
                
                // Vérifier les permissions
                if (!estAdmin && !emailUtilisateur.equals(email)) {
                    response.put("success", false);
                    response.put("message", "Vous ne pouvez consulter que vos propres demandes");
                    response.put("mode", "securise_refuse");
                    return ResponseEntity.status(403).body(response);
                }
                
                modeAcces = estAdmin ? "admin_securise" : "utilisateur_securise";
            } else {
                // MODE PUBLIC - Aucune authentification
                modeAcces = "public";
                System.out.println("Accès public - aucune authentification");
            }
            
            // Récupérer les demandes
            List<DemandeStage> demandes = service.trouverParEmail(email);
            System.out.println("Demandes trouvées: " + demandes.size());
            
            response.put("success", true);
            response.put("data", demandes);
            response.put("total", demandes.size());
            response.put("mode_acces", modeAcces);
            response.put("email_recherche", email);
            
            if (demandes.isEmpty()) {
                response.put("message", "Aucune demande trouvée pour cet email");
            } else {
                response.put("message", demandes.size() + " demande(s) trouvée(s)");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * MES DEMANDES - Suivi via l'application (authentification requise)
     * L'utilisateur voit seulement ses propres demandes
     */
    @GetMapping("/mes-demandes")
    public ResponseEntity<Map<String, Object>> mesDemandes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== MES DEMANDES (VIA APPLICATION) ===");
            
            // Récupérer l'utilisateur connecté
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Authentification requise pour accéder à vos demandes");
                return ResponseEntity.status(401).body(response);
            }
            
            String emailUtilisateur = auth.getName();
            System.out.println("Utilisateur connecté: " + emailUtilisateur);
            
            // Récupérer UNIQUEMENT les demandes de cet utilisateur
            List<DemandeStage> demandes = service.trouverParEmail(emailUtilisateur);
            System.out.println("Demandes trouvées pour " + emailUtilisateur + ": " + demandes.size());
            
            response.put("success", true);
            response.put("message", demandes.isEmpty() ? 
                "Vous n'avez soumis aucune demande de stage" : 
                "Vos demandes de stage");
            response.put("data", demandes);
            response.put("total", demandes.size());
            response.put("mode_acces", "application_authentifiee");
            response.put("utilisateur", emailUtilisateur);
            
            // Statistiques personnelles
            if (!demandes.isEmpty()) {
                Map<String, Object> stats = new HashMap<>();
                long enAttente = demandes.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
                long acceptees = demandes.stream().filter(d -> "ACCEPTE".equals(d.getStatut())).count();
                long refusees = demandes.stream().filter(d -> "REFUSE".equals(d.getStatut())).count();
                
                stats.put("EN_ATTENTE", enAttente);
                stats.put("ACCEPTE", acceptees);
                stats.put("REFUSE", refusees);
                response.put("mes_statistiques", stats);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERREUR lors de la récupération des demandes: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération de vos demandes: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * CRÉATION D'UNE NOUVELLE DEMANDE DE STAGE
     * Accessible à tous les utilisateurs authentifiés
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> creerDemande(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam("telephone") String telephone,
            @RequestParam("cin") String cin,
            @RequestParam("sexe") String sexe,
            @RequestParam("adresseDomicile") String adresseDomicile,
            @RequestParam("typeStage") String typeStage,
            @RequestParam("dateDebut") String dateDebut,
            @RequestParam("duree") String duree,
            @RequestParam(value = "convention_stage", required = false) MultipartFile conventionStage,
            @RequestParam(value = "demande_stage", required = false) MultipartFile demandeStage,
            @RequestParam(value = "cv", required = false) MultipartFile cv,
            @RequestParam(value = "lettre_motivation", required = false) MultipartFile lettreMotivation,
            @RequestParam(value = "cin_recto", required = false) MultipartFile cinRecto,
            @RequestParam(value = "cin_verso", required = false) MultipartFile cinVerso,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== CREATION DEMANDE ===");
            System.out.println("- Nom: " + nom);
            System.out.println("- Prénom: " + prenom);
            System.out.println("- Email: " + email);
            
            DemandeStage demande = new DemandeStage();
            demande.setNom(nom);
            demande.setPrenom(prenom);
            demande.setEmail(email);
            demande.setTelephone(telephone);
            demande.setCin(cin);
            demande.setSexe(sexe);
            demande.setAdresseDomicile(adresseDomicile);
            demande.setTypeStage(typeStage);
            demande.setDuree(duree);
            
            // Validation et parsing de la date
            try {
                demande.setDateDebut(LocalDate.parse(dateDebut));
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Format de date invalide. Utilisez YYYY-MM-DD");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Gestion des noms de fichiers uploadés
            demande.setConventionStage(getFileName(conventionStage, "convention_non_fourni.pdf"));
            demande.setDemandeStage(getFileName(demandeStage, "demande_non_fourni.pdf"));
            demande.setCv(getFileName(cv, "cv_non_fourni.pdf"));
            demande.setLettreMotivation(getFileName(lettreMotivation, "lettre_non_fourni.pdf"));
            demande.setCinRecto(getFileName(cinRecto, "cin_recto_non_fourni.jpg"));
            demande.setCinVerso(getFileName(cinVerso, "cin_verso_non_fourni.jpg"));
            demande.setPhoto(getFileName(photo, "photo_non_fourni.jpg"));

            DemandeStage nouvelleDemande = service.creerDemande(demande);
            
            response.put("success", true);
            response.put("message", "Demande créée avec succès");
            response.put("data", nouvelleDemande);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la création de la demande: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * RECHERCHE DE DEMANDES AVEC FILTRES
     * - Utilisateur normal : voit seulement ses demandes
     * - Admin : voit toutes les demandes
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> rechercherDemandes(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String typeStage,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔍 === RECHERCHE DEMANDES ===");
            System.out.println("Filtres appliqués:");
            System.out.println("- nom: " + nom);
            System.out.println("- prenom: " + prenom);
            System.out.println("- typeStage: " + typeStage);
            System.out.println("- statut: " + statut);
            System.out.println("- search: " + search);
            
            // Vérifier l'authentification et les permissions
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Authentification requise");
                return ResponseEntity.status(401).body(response);
            }
            
            String emailUtilisateur = auth.getName();
            boolean estAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            System.out.println("Utilisateur: " + emailUtilisateur + " (Admin: " + estAdmin + ")");
            
            List<DemandeStage> toutesLesDemandes;
            String porteeRecherche;
            
            if (estAdmin) {
                // Admin peut voir toutes les demandes
                toutesLesDemandes = service.toutesLesDemandes();
                porteeRecherche = "toutes_demandes";
                System.out.println("Admin - Total demandes: " + toutesLesDemandes.size());
            } else {
                // Utilisateur normal ne voit que ses demandes
                toutesLesDemandes = service.trouverParEmail(emailUtilisateur);
                porteeRecherche = "mes_demandes_uniquement";
                System.out.println("User - Ses demandes: " + toutesLesDemandes.size());
            }
            
            // Appliquer les filtres
            List<DemandeStage> demandesFiltrees = appliquerFiltres(toutesLesDemandes, nom, prenom, typeStage, statut, search);
            
            System.out.println("Résultats après filtrage: " + demandesFiltrees.size());
            
            response.put("success", true);
            response.put("data", demandesFiltrees);
            response.put("total", demandesFiltrees.size());
            response.put("portee_recherche", porteeRecherche);
            response.put("utilisateur", emailUtilisateur);
            response.put("est_admin", estAdmin);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la recherche: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * EXPORT EXCEL SÉCURISÉ AVEC FILTRAGE PAR DATES
     * - Utilisateur normal : exporte seulement ses demandes
     * - Admin : exporte toutes les demandes
     * - Filtrage par dates possible avec noms de fichiers descriptifs
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exporterDemandesExcel(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String typeStage,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin) {
        
        try {
            System.out.println("📊 === EXPORT EXCEL SÉCURISÉ ===");
            System.out.println("Filtres d'export:");
            System.out.println("- nom: " + nom);
            System.out.println("- prenom: " + prenom);
            System.out.println("- typeStage: " + typeStage);
            System.out.println("- statut: " + statut);
            System.out.println("- search: " + search);
            System.out.println("- dateDebut: " + dateDebut);
            System.out.println("- dateFin: " + dateFin);
            
            // Vérifier l'authentification
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                System.out.println("ERREUR: Accès non authentifié à l'export Excel");
                return ResponseEntity.status(401).build();
            }
            
            String emailUtilisateur = auth.getName();
            boolean estAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            System.out.println("Export demandé par: " + emailUtilisateur + " (Admin: " + estAdmin + ")");
            
            List<DemandeStage> toutesLesDemandes;
            String prefixeFichier;
            
            if (estAdmin) {
                // Admin exporte toutes les demandes
                toutesLesDemandes = service.toutesLesDemandes();
                prefixeFichier = "toutes_demandes_";
                System.out.println("Export admin - " + toutesLesDemandes.size() + " demandes totales");
            } else {
                // Utilisateur exporte seulement ses demandes
                toutesLesDemandes = service.trouverParEmail(emailUtilisateur);
                prefixeFichier = "mes_demandes_";
                System.out.println("Export utilisateur - " + toutesLesDemandes.size() + " demandes personnelles");
            }
            
            // Appliquer tous les filtres y compris les dates
            List<DemandeStage> demandesFiltrees = appliquerFiltresAvecDates(
                toutesLesDemandes, nom, prenom, typeStage, statut, search, dateDebut, dateFin);
            
            System.out.println("Demandes à exporter après filtrage: " + demandesFiltrees.size());
            
            // Générer le fichier Excel
            byte[] excelData = genererFichierExcel(demandesFiltrees);
            
            // Créer le nom de fichier descriptif
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String suffixeDates = construireSuffixeDates(dateDebut, dateFin);
            String filename = prefixeFichier + timestamp + suffixeDates + ".xlsx";
            
            System.out.println("Fichier Excel généré: " + filename + " (" + excelData.length + " bytes)");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
                
        } catch (Exception e) {
            System.out.println("ERREUR lors de l'export Excel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * CONSULTATION D'UNE DEMANDE SPÉCIFIQUE PAR ID
     * Vérification des permissions d'accès
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDemandeParId(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== CONSULTATION DEMANDE ID: " + id + " ===");
            
            // Vérifier l'authentification
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Authentification requise");
                return ResponseEntity.status(401).body(response);
            }
            
            String emailUtilisateur = auth.getName();
            boolean estAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            System.out.println("Demande par: " + emailUtilisateur + " (Admin: " + estAdmin + ")");
            
            return service.trouverParId(id)
                .map(demande -> {
                    // Vérifier que l'utilisateur peut accéder à cette demande
                    if (!estAdmin && !demande.getEmail().equals(emailUtilisateur)) {
                        System.out.println("ACCÈS REFUSÉ: Utilisateur " + emailUtilisateur + 
                                         " tente d'accéder à la demande de " + demande.getEmail());
                        response.put("success", false);
                        response.put("message", "Accès non autorisé à cette demande");
                        return ResponseEntity.status(403).body(response);
                    }
                    
                    System.out.println("ACCÈS AUTORISÉ: " + (estAdmin ? "Admin" : "Propriétaire"));
                    response.put("success", true);
                    response.put("data", demande);
                    response.put("acces_autorise", estAdmin ? "admin" : "proprietaire");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    System.out.println("DEMANDE NON TROUVÉE: ID " + id);
                    response.put("success", false);
                    response.put("message", "Demande non trouvée");
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Récupère le nom de fichier original ou retourne un nom par défaut
     */
    private String getFileName(MultipartFile file, String defaultName) {
        return (file != null && file.getOriginalFilename() != null) 
            ? file.getOriginalFilename() : defaultName;
    }

    /**
     * Construit le suffixe du nom de fichier basé sur les filtres de dates
     */
    private String construireSuffixeDates(String dateDebut, String dateFin) {
        if (dateDebut != null && !dateDebut.trim().isEmpty() && 
            dateFin != null && !dateFin.trim().isEmpty()) {
            return "_du_" + dateDebut + "_au_" + dateFin;
        } else if (dateDebut != null && !dateDebut.trim().isEmpty()) {
            return "_depuis_" + dateDebut;
        } else if (dateFin != null && !dateFin.trim().isEmpty()) {
            return "_jusqu_" + dateFin;
        }
        return "";
    }

    /**
     * Applique les filtres standard (sans dates)
     */
    private List<DemandeStage> appliquerFiltres(List<DemandeStage> demandes, String nom, String prenom, 
                                              String typeStage, String statut, String search) {
        return appliquerFiltresAvecDates(demandes, nom, prenom, typeStage, statut, search, null, null);
    }

    /**
     * Applique tous les filtres y compris les filtres par dates
     * CORRIGÉ : Protection contre les null pointers
     */
    private List<DemandeStage> appliquerFiltresAvecDates(List<DemandeStage> demandes, String nom, String prenom, 
                                                       String typeStage, String statut, String search,
                                                       String dateDebut, String dateFin) {
        List<DemandeStage> demandesFiltrees = new ArrayList<>();
        
        // Vérification de sécurité pour la liste d'entrée
        if (demandes == null) {
            System.out.println("ATTENTION: Liste de demandes null reçue");
            return demandesFiltrees;
        }
        
        // Parser les dates si fournies
        LocalDate dateDebutParsee = null;
        LocalDate dateFinParsee = null;
        
        try {
            if (dateDebut != null && !dateDebut.trim().isEmpty()) {
                dateDebutParsee = LocalDate.parse(dateDebut);
                System.out.println("Date début parsée: " + dateDebutParsee);
            }
            if (dateFin != null && !dateFin.trim().isEmpty()) {
                dateFinParsee = LocalDate.parse(dateFin);
                System.out.println("Date fin parsée: " + dateFinParsee);
            }
        } catch (Exception e) {
            System.out.println("ERREUR parsing dates: " + e.getMessage());
            // En cas d'erreur de parsing, ignorer les filtres de date
            dateDebutParsee = null;
            dateFinParsee = null;
        }
        
        int demandesRetenues = 0;
        int demandesRejetees = 0;
        
        for (DemandeStage demande : demandes) {
            boolean estRetenue = true;
            
            // CORRECTION PRINCIPALE: Vérification de sécurité pour éviter les null pointers
            if (demande == null) {
                System.out.println("ATTENTION: Demande null trouvée, ignorée");
                continue;
            }
            
            // Filtre par nom - CORRIGÉ
            if (estRetenue && nom != null && !nom.trim().isEmpty()) {
                if (demande.getNom() == null || !demande.getNom().toLowerCase().contains(nom.toLowerCase())) {
                    estRetenue = false;
                }
            }
            
            // Filtre par prénom - CORRIGÉ
            if (estRetenue && prenom != null && !prenom.trim().isEmpty()) {
                if (demande.getPrenom() == null || !demande.getPrenom().toLowerCase().contains(prenom.toLowerCase())) {
                    estRetenue = false;
                }
            }
            
            // Filtre par type de stage - CORRIGÉ
            if (estRetenue && typeStage != null && !typeStage.trim().isEmpty()) {
                if (demande.getTypeStage() == null || !demande.getTypeStage().equalsIgnoreCase(typeStage)) {
                    estRetenue = false;
                }
            }
            
            // Filtre par statut - CORRIGÉ
            if (estRetenue && statut != null && !statut.trim().isEmpty()) {
                if (demande.getStatut() == null || !demande.getStatut().equalsIgnoreCase(statut)) {
                    estRetenue = false;
                }
            }
            
            // Filtre par date de début - CORRIGÉ
            if (estRetenue && dateDebutParsee != null) {
                if (demande.getDateDebut() == null || demande.getDateDebut().isBefore(dateDebutParsee)) {
                    estRetenue = false;
                }
            }
            
            // Filtre par date de fin - CORRIGÉ
            if (estRetenue && dateFinParsee != null) {
                if (demande.getDateDebut() == null || demande.getDateDebut().isAfter(dateFinParsee)) {
                    estRetenue = false;
                }
            }
            
            // Recherche globale - CORRIGÉ AVEC PROTECTION NULL
            if (estRetenue && search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                boolean searchMatch = false;
                
                // Vérifier chaque champ individuellement pour éviter les null pointers
                if (demande.getNom() != null && demande.getNom().toLowerCase().contains(searchLower)) {
                    searchMatch = true;
                }
                if (!searchMatch && demande.getPrenom() != null && demande.getPrenom().toLowerCase().contains(searchLower)) {
                    searchMatch = true;
                }
                if (!searchMatch && demande.getEmail() != null && demande.getEmail().toLowerCase().contains(searchLower)) {
                    searchMatch = true;
                }
                if (!searchMatch && demande.getCin() != null && demande.getCin().toLowerCase().contains(searchLower)) {
                    searchMatch = true;
                }
                
                if (!searchMatch) {
                    estRetenue = false;
                }
            }
            
            if (estRetenue) {
                demandesFiltrees.add(demande);
                demandesRetenues++;
            } else {
                demandesRejetees++;
            }
        }
        
        System.out.println("Filtrage terminé: " + demandesRetenues + " retenues, " + demandesRejetees + " rejetées");
        
        return demandesFiltrees;
    }

    /**
     * Génère le fichier Excel avec toutes les demandes fournies
     */
    private byte[] genererFichierExcel(List<DemandeStage> demandes) throws Exception {
        System.out.println("=== GÉNÉRATION FICHIER EXCEL ===");
        System.out.println("Nombre de demandes à exporter: " + demandes.size());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Demandes de Stage");
            
            // Style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Créer l'en-tête avec toutes les colonnes
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "Nom", "Prénom", "Email", "Téléphone", "CIN", "Sexe",
                "Adresse", "Type Stage", "Date Début", "Durée", "Statut",
                "Date Demande", "Date Traitement", "Commentaire"
                };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Formatters pour les dates
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            // Remplir les données
            int rowNum = 1;
            for (DemandeStage demande : demandes) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(demande.getId());
                row.createCell(1).setCellValue(demande.getNom() != null ? demande.getNom() : "");
                row.createCell(2).setCellValue(demande.getPrenom() != null ? demande.getPrenom() : "");
                row.createCell(3).setCellValue(demande.getEmail() != null ? demande.getEmail() : "");
                row.createCell(4).setCellValue(demande.getTelephone() != null ? demande.getTelephone() : "");
                row.createCell(5).setCellValue(demande.getCin() != null ? demande.getCin() : "");
                row.createCell(6).setCellValue(demande.getSexe() != null ? demande.getSexe() : "");
                row.createCell(7).setCellValue(demande.getAdresseDomicile() != null ? demande.getAdresseDomicile() : "");
                row.createCell(8).setCellValue(demande.getTypeStage() != null ? demande.getTypeStage() : "");
                row.createCell(9).setCellValue(demande.getDateDebut() != null ? demande.getDateDebut().format(dateFormatter) : "");
                row.createCell(10).setCellValue(demande.getDuree() != null ? demande.getDuree() : "");
                row.createCell(11).setCellValue(demande.getStatut() != null ? demande.getStatut() : "");
                row.createCell(12).setCellValue(demande.getDateDemande() != null ? demande.getDateDemande().format(dateTimeFormatter) : "");
                row.createCell(13).setCellValue(demande.getDateTraitement() != null ? 
                    demande.getDateTraitement().format(dateTimeFormatter) : "");
                row.createCell(14).setCellValue(demande.getCommentaire() != null ? 
                    demande.getCommentaire() : "");
            }
            
            // Ajuster automatiquement la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            System.out.println("Excel généré avec " + (rowNum - 1) + " lignes de données");
            
            // Convertir le workbook en tableau de bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] excelBytes = outputStream.toByteArray();
            
            System.out.println("Fichier Excel converti en bytes: " + excelBytes.length + " bytes");
            
            return excelBytes;
        }
    }
}
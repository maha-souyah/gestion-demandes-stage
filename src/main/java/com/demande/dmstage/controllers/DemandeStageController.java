package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.services.DemandeStageService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/suivi")
    public ResponseEntity<Map<String, Object>> suiviDemande(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== SUIVI PAR EMAIL - VERSION DEBUG ===");
            System.out.println("Email reçu: '" + email + "'");
            
            // Récupérer TOUTES les demandes pour debug
            List<DemandeStage> toutesLesDemandes = service.toutesLesDemandes();
            System.out.println("Total demandes en base: " + toutesLesDemandes.size());
            
            for (DemandeStage d : toutesLesDemandes) {
                System.out.println("Demande ID " + d.getId() + ": email='" + d.getEmail() + "'");
            }
            
            // Chercher avec la méthode du service
            List<DemandeStage> demandes = service.trouverParEmail(email);
            System.out.println("Résultat service.trouverParEmail: " + demandes.size());
            
            // Chercher manuellement
            List<DemandeStage> demandesManuelles = new ArrayList<>();
            for (DemandeStage d : toutesLesDemandes) {
                if (d.getEmail().equals(email)) {
                    demandesManuelles.add(d);
                }
            }
            System.out.println("Résultat recherche manuelle: " + demandesManuelles.size());
            
            if (demandes.isEmpty() && demandesManuelles.isEmpty()) {
                response.put("success", false);
                response.put("message", "Aucune demande trouvée pour cet email");
                response.put("debug_info", Map.of(
                    "email_recherche", email,
                    "total_demandes", toutesLesDemandes.size(),
                    "result_service", demandes.size(),
                    "result_manuel", demandesManuelles.size()
                ));
                return ResponseEntity.ok(response);
            }
            
            // Utiliser le résultat qui marche
            List<DemandeStage> resultatFinal = !demandes.isEmpty() ? demandes : demandesManuelles;
            
            response.put("success", true);
            response.put("data", resultatFinal);
            response.put("type", "suivi_email");
            response.put("total", resultatFinal.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/debug-emails")
    public ResponseEntity<Map<String, Object>> debugEmails() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== DEBUG EMAILS ===");
            
            List<DemandeStage> toutesLesDemandes = service.toutesLesDemandes();
            System.out.println("Total demandes: " + toutesLesDemandes.size());
            
            List<Map<String, Object>> demandesDebug = new ArrayList<>();
            
            for (DemandeStage demande : toutesLesDemandes) {
                Map<String, Object> demandeInfo = new HashMap<>();
                demandeInfo.put("id", demande.getId());
                demandeInfo.put("nom", demande.getNom());
                demandeInfo.put("prenom", demande.getPrenom());
                demandeInfo.put("email", demande.getEmail());
                demandeInfo.put("email_length", demande.getEmail().length());
                demandeInfo.put("email_trimmed", demande.getEmail().trim());
                
                System.out.println("Demande " + demande.getId() + ":");
                System.out.println("  - Email brut: '" + demande.getEmail() + "'");
                System.out.println("  - Email length: " + demande.getEmail().length());
                System.out.println("  - Email trimmed: '" + demande.getEmail().trim() + "'");
                
                demandesDebug.add(demandeInfo);
            }
            
            response.put("success", true);
            response.put("demandes", demandesDebug);
            response.put("total", toutesLesDemandes.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERREUR DEBUG: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur debug: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    public ResponseEntity<Map<String, Object>> mesDemandes(
            @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== SUIVI VIA APPLICATION (AUTHENTIFIÉ) ===");
            
            // Récupérer l'utilisateur connecté depuis l'authentification
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(401).body(response);
            }
            
            String emailUtilisateur = auth.getName();
            System.out.println("Utilisateur connecté: " + emailUtilisateur);
            
            // Récupérer les demandes de cet utilisateur
            List<DemandeStage> demandes = service.trouverParEmail(emailUtilisateur);
            System.out.println("Demandes trouvées pour " + emailUtilisateur + ": " + demandes.size());
            
            if (demandes.isEmpty()) {
                response.put("success", true);
                response.put("message", "Vous n'avez soumis aucune demande de stage");
                response.put("data", demandes);
                response.put("total", 0);
                response.put("type", "suivi_application");
                response.put("utilisateur", emailUtilisateur);
                return ResponseEntity.ok(response);
            }
            
            // Ajouter des informations de suivi enrichies
            response.put("success", true);
            response.put("message", "Vos demandes de stage");
            response.put("data", demandes);
            response.put("total", demandes.size());
            response.put("type", "suivi_application");
            response.put("utilisateur", emailUtilisateur);
            
            // Statistiques personnelles
            Map<String, Object> statistiquesPersonnelles = new HashMap<>();
            long enAttente = demandes.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
            long acceptees = demandes.stream().filter(d -> "ACCEPTE".equals(d.getStatut())).count();
            long refusees = demandes.stream().filter(d -> "REFUSE".equals(d.getStatut())).count();
            
            statistiquesPersonnelles.put("EN_ATTENTE", enAttente);
            statistiquesPersonnelles.put("ACCEPTE", acceptees);
            statistiquesPersonnelles.put("REFUSE", refusees);
            response.put("mes_statistiques", statistiquesPersonnelles);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERREUR lors du suivi via application: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération de vos demandes: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

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
            System.out.println("=== DEBUT CREATION DEMANDE ===");
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
            
            try {
                demande.setDateDebut(LocalDate.parse(dateDebut));
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Format de date invalide. Utilisez YYYY-MM-DD");
                return ResponseEntity.badRequest().body(response);
            }
            
            String conventionName = (conventionStage != null && conventionStage.getOriginalFilename() != null) 
                ? conventionStage.getOriginalFilename() : "convention_non_fourni.pdf";
            String demandeName = (demandeStage != null && demandeStage.getOriginalFilename() != null) 
                ? demandeStage.getOriginalFilename() : "demande_non_fourni.pdf";
            String cvName = (cv != null && cv.getOriginalFilename() != null) 
                ? cv.getOriginalFilename() : "cv_non_fourni.pdf";
            String lettreName = (lettreMotivation != null && lettreMotivation.getOriginalFilename() != null) 
                ? lettreMotivation.getOriginalFilename() : "lettre_non_fourni.pdf";
            String cinRectoName = (cinRecto != null && cinRecto.getOriginalFilename() != null) 
                ? cinRecto.getOriginalFilename() : "cin_recto_non_fourni.jpg";
            String cinVersoName = (cinVerso != null && cinVerso.getOriginalFilename() != null) 
                ? cinVerso.getOriginalFilename() : "cin_verso_non_fourni.jpg";
            String photoName = (photo != null && photo.getOriginalFilename() != null) 
                ? photo.getOriginalFilename() : "photo_non_fourni.jpg";
            
            demande.setConventionStage(conventionName);
            demande.setDemandeStage(demandeName);
            demande.setCv(cvName);
            demande.setLettreMotivation(lettreName);
            demande.setCinRecto(cinRectoName);
            demande.setCinVerso(cinVersoName);
            demande.setPhoto(photoName);

            DemandeStage nouvelleDemande = service.creerDemande(demande);
            
            response.put("success", true);
            response.put("message", "Demande créée avec succès");
            response.put("data", nouvelleDemande);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("ERREUR EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la création de la demande: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getToutesDemandes(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String typeStage,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔍 === CONTRÔLEUR - RECHERCHE DEMANDES ===");
            System.out.println("URL appelée avec paramètres:");
            System.out.println("- nom: '" + nom + "'");
            System.out.println("- prenom: '" + prenom + "'");
            System.out.println("- typeStage: '" + typeStage + "'");
            System.out.println("- statut: '" + statut + "'");
            System.out.println("- search: '" + search + "'");
            
            // Récupérer TOUTES les demandes d'abord
            List<DemandeStage> toutesLesDemandes = service.toutesLesDemandes();
            System.out.println("Total demandes récupérées: " + toutesLesDemandes.size());
            
            // APPLIQUER LES FILTRES DIRECTEMENT DANS LE CONTRÔLEUR
            List<DemandeStage> demandesFiltrees = new ArrayList<>();
            
            for (DemandeStage demande : toutesLesDemandes) {
                boolean estRetenue = true;
                
                System.out.println("\n--- Test demande: " + demande.getNom() + " " + demande.getPrenom() + " ---");
                
                // Filtre par nom
                if (nom != null && !nom.trim().isEmpty()) {
                    boolean nomMatch = demande.getNom().toLowerCase().contains(nom.toLowerCase());
                    System.out.println("Filtre nom '" + nom + "' vs '" + demande.getNom() + "' → " + nomMatch);
                    if (!nomMatch) {
                        System.out.println("❌ ÉLIMINÉ par nom");
                        estRetenue = false;
                    }
                }
                
                // Filtre par prénom
                if (estRetenue && prenom != null && !prenom.trim().isEmpty()) {
                    boolean prenomMatch = demande.getPrenom().toLowerCase().contains(prenom.toLowerCase());
                    System.out.println("Filtre prénom '" + prenom + "' vs '" + demande.getPrenom() + "' → " + prenomMatch);
                    if (!prenomMatch) {
                        System.out.println("❌ ÉLIMINÉ par prénom");
                        estRetenue = false;
                    }
                }
                
                // Filtre par type de stage
                if (estRetenue && typeStage != null && !typeStage.trim().isEmpty()) {
                    boolean typeMatch = demande.getTypeStage().equalsIgnoreCase(typeStage);
                    System.out.println("Filtre type '" + typeStage + "' vs '" + demande.getTypeStage() + "' → " + typeMatch);
                    if (!typeMatch) {
                        System.out.println("❌ ÉLIMINÉ par type");
                        estRetenue = false;
                    }
                }
                
                // Filtre par statut
                if (estRetenue && statut != null && !statut.trim().isEmpty()) {
                    boolean statutMatch = demande.getStatut().equalsIgnoreCase(statut);
                    System.out.println("Filtre statut '" + statut + "' vs '" + demande.getStatut() + "' → " + statutMatch);
                    if (!statutMatch) {
                        System.out.println("❌ ÉLIMINÉ par statut");
                        estRetenue = false;
                    }
                }
                
                // Recherche globale
                if (estRetenue && search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase();
                    boolean searchMatch = demande.getNom().toLowerCase().contains(searchLower) ||
                                        demande.getPrenom().toLowerCase().contains(searchLower) ||
                                        demande.getEmail().toLowerCase().contains(searchLower) ||
                                        demande.getCin().toLowerCase().contains(searchLower);
                    System.out.println("Recherche '" + search + "' → " + searchMatch);
                    if (!searchMatch) {
                        System.out.println("❌ ÉLIMINÉ par recherche");
                        estRetenue = false;
                    }
                }
                
                if (estRetenue) {
                    System.out.println("✅ DEMANDE RETENUE");
                    demandesFiltrees.add(demande);
                } else {
                    System.out.println("❌ DEMANDE ÉLIMINÉE");
                }
            }
            
            System.out.println("\n🎯 RÉSULTAT FINAL: " + demandesFiltrees.size() + " demandes trouvées");
            
            response.put("success", true);
            response.put("data", demandesFiltrees);
            response.put("total", demandesFiltrees.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des demandes: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exporterDemandesExcel(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String typeStage,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search) {
        
        try {
            System.out.println("📊 === EXPORT EXCEL DEMANDES ===");
            System.out.println("Filtres pour export:");
            System.out.println("- nom: '" + nom + "'");
            System.out.println("- prenom: '" + prenom + "'");
            System.out.println("- typeStage: '" + typeStage + "'");
            System.out.println("- statut: '" + statut + "'");
            System.out.println("- search: '" + search + "'");
            
            // Récupérer les demandes avec les mêmes filtres que la liste
            List<DemandeStage> toutesLesDemandes = service.toutesLesDemandes();
            List<DemandeStage> demandesFiltrees = new ArrayList<>();
            
            // Appliquer les mêmes filtres que dans getToutesDemandes
            for (DemandeStage demande : toutesLesDemandes) {
                boolean estRetenue = true;
                
                if (nom != null && !nom.trim().isEmpty()) {
                    if (!demande.getNom().toLowerCase().contains(nom.toLowerCase())) {
                        estRetenue = false;
                    }
                }
                
                if (estRetenue && prenom != null && !prenom.trim().isEmpty()) {
                    if (!demande.getPrenom().toLowerCase().contains(prenom.toLowerCase())) {
                        estRetenue = false;
                    }
                }
                
                if (estRetenue && typeStage != null && !typeStage.trim().isEmpty()) {
                    if (!demande.getTypeStage().equalsIgnoreCase(typeStage)) {
                        estRetenue = false;
                    }
                }
                
                if (estRetenue && statut != null && !statut.trim().isEmpty()) {
                    if (!demande.getStatut().equalsIgnoreCase(statut)) {
                        estRetenue = false;
                    }
                }
                
                if (estRetenue && search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase();
                    boolean searchMatch = demande.getNom().toLowerCase().contains(searchLower) ||
                                        demande.getPrenom().toLowerCase().contains(searchLower) ||
                                        demande.getEmail().toLowerCase().contains(searchLower) ||
                                        demande.getCin().toLowerCase().contains(searchLower);
                    if (!searchMatch) {
                        estRetenue = false;
                    }
                }
                
                if (estRetenue) {
                    demandesFiltrees.add(demande);
                }
            }
            
            System.out.println("Demandes à exporter: " + demandesFiltrees.size());
            
            // Générer le fichier Excel
            byte[] excelData = genererFichierExcel(demandesFiltrees);
            
            // Nom du fichier avec timestamp
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String filename = "demandes_stage_" + timestamp + ".xlsx";
            
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

    private byte[] genererFichierExcel(List<DemandeStage> demandes) throws Exception {
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
            
            // Créer l'en-tête
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
            
            // Remplir les données
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            int rowNum = 1;
            for (DemandeStage demande : demandes) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(demande.getId());
                row.createCell(1).setCellValue(demande.getNom());
                row.createCell(2).setCellValue(demande.getPrenom());
                row.createCell(3).setCellValue(demande.getEmail());
                row.createCell(4).setCellValue(demande.getTelephone());
                row.createCell(5).setCellValue(demande.getCin());
                row.createCell(6).setCellValue(demande.getSexe());
                row.createCell(7).setCellValue(demande.getAdresseDomicile());
                row.createCell(8).setCellValue(demande.getTypeStage());
                row.createCell(9).setCellValue(demande.getDateDebut().format(dateFormatter));
                row.createCell(10).setCellValue(demande.getDuree());
                row.createCell(11).setCellValue(demande.getStatut());
                row.createCell(12).setCellValue(demande.getDateDemande().format(dateTimeFormatter));
                row.createCell(13).setCellValue(demande.getDateTraitement() != null ? 
                    demande.getDateTraitement().format(dateTimeFormatter) : "");
                row.createCell(14).setCellValue(demande.getCommentaire() != null ? 
                    demande.getCommentaire() : "");
            }
            
            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convertir en bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDemandeParId(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        return service.trouverParId(id)
                .map(demande -> {
                    response.put("success", true);
                    response.put("data", demande);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "Demande non trouvée");
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<Map<String, Object>> modifierStatut(
            @PathVariable Long id, 
            @RequestBody Map<String, String> statutData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String statut = statutData.get("statut");
            String commentaire = statutData.get("commentaire");
            
            DemandeStage demande = service.modifierStatut(id, statut);
            if (commentaire != null && !commentaire.trim().isEmpty()) {
                demande.setCommentaire(commentaire);
                demande = service.creerDemande(demande);
            }
            
            response.put("success", true);
            response.put("message", "Statut modifié avec succès");
            response.put("data", demande);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la modification du statut: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
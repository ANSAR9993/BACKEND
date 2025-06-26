package com.dgapr.demo.Model;



import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;



@Entity
@Table(name = "marche")
public class Marche {
  


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    
    private Long id;
    private String titre_marche;
    private String id_matricule;
    private String societe;
    private LocalDate date_ordre_service;
    private LocalDate date_execution;
    private LocalDate date_reception;
    private Integer garantie_duree;
    private String garantie_unite;

    


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre_marche() {
        return titre_marche;
    }

    public void setTitre_marche(String titre_marche) {
        this.titre_marche = titre_marche;
    }

    public String getId_matricule() {
        return id_matricule;
    }

    public void setId_matricule(String id_matricule) {
        this.id_matricule = id_matricule;
    }

    public String getSociete() {
        return societe;
    }

    public void setSociete(String societe) {
        this.societe = societe;
    }

    public LocalDate getDate_ordre_service() {
        return date_ordre_service;
    }

    public void setDate_ordre_service(LocalDate date_ordre_service) {
        this.date_ordre_service = date_ordre_service;
    }

    public LocalDate getDate_execution() {
        return date_execution;
    }

    public void setDate_execution(LocalDate date_execution) {
        this.date_execution = date_execution;
    }

    public LocalDate getDate_reception() {
        return date_reception;
    }

    public void setDate_reception(LocalDate date_reception) {
        this.date_reception = date_reception;
    }
    
    public Integer getGarantie_duree() {
        return garantie_duree;
    }

    public void setGarantie_duree(Integer garantie_duree) {
        this.garantie_duree = garantie_duree;
    }

    public String getGarantie_unite() {
        return garantie_unite;
    }

    public void setGarantie_unite(String garantie_unite) {
        this.garantie_unite = garantie_unite;
    }

    

   


}


package com.dgapr.demo.Dto;

public class MaterielDto {
    private Long id;
    private String type_equipement;
    private String gamme;
    private String numero_serie;
    private String emplacement;
    private Integer garantie_duree;
    private String garantie_unite;
    private long marcheId; // ID of the associated Marche entity
   
    

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType_equipement() {
        return type_equipement;
    }

    public void setType_equipement(String type_equipement) {
        this.type_equipement = type_equipement;
    }

    public String getGamme() {
        return gamme;
    }

    public void setGamme(String gamme) {
        this.gamme = gamme;
    }

   

    public String getNumero_serie() {
        return numero_serie;
    }

    public void setNumero_serie(String numero_serie) {
        this.numero_serie = numero_serie;
    }
    

    public String getEmplacement() {
        return emplacement;
    }
    public void setEmplacement(String emplacement) {
        this.emplacement = emplacement;
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

    public Long getMarcheId() {
        return marcheId;
    }

    public void setMarcheId(Long marcheId) {
        this.marcheId = marcheId;
    }
    
}

package com.dgapr.demo.Model;

import jakarta.persistence.*;



@Entity
public class Materiel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type_equipement;
    private String gamme;
    private String numero_serie;
    private String emplacement;
    private Integer garantie_duree;
    private String garantie_unite;
    

    @ManyToOne
    @JoinColumn(name = "marche_id")
    private Marche marche; // relation to Marche entity

    // getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType_equipement() { return type_equipement; }
    public void setType_equipement(String type_equipement) { this.type_equipement = type_equipement; }

    public String getGamme() { return gamme; }
    public void setGamme(String gamme) { this.gamme = gamme; }

    public String getNumero_serie() { return numero_serie; }
    public void setNumero_serie(String numero_serie) { this.numero_serie = numero_serie; }

    public String getEmplacement() { return emplacement; }
    public void setEmplacement(String emplacement) { this.emplacement = emplacement; }

    public Marche getMarche() { return marche; }
    public void setMarche(Marche marche) { this.marche = marche; }

    public Integer getGarantie_duree() { return garantie_duree; }
    public void setGarantie_duree(Integer garantie_duree) { this.garantie_duree = garantie_duree; }

    public String getGarantie_unite() { return garantie_unite; }
    public void setGarantie_unite(String garantie_unite) { this.garantie_unite = garantie_unite; }
    
    
    

}


    



    




package com.dgapr.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Certificate")
public class Certificat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ID_Demand", nullable = false, unique = true)
    private String idDemand;

    @Column(name = "Demande_Name", nullable = false, length = 100)
    private String demandeName;

    @Column(name = "Model", nullable = false, length = 50)
    private String model;

    @Column(name = "Type", nullable = false, length = 10)
    private String type;

    @Column(name = "Organizational_Unit", length = 100)
    private String organizationalUnit;

    @Column(name = "Common_Name", nullable = false)
    private String commonName;

    @Column(name = "Creation_Date")
    private java.time.LocalDate creationDate;

    @Column(name = "Expiration_Date", nullable = false)
    private java.time.LocalDate expirationDate;

}

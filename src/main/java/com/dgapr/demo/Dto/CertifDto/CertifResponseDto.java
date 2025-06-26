package com.dgapr.demo.Dto.CertifDto;

import java.time.LocalDate;
import java.util.Objects;

public class CertifResponseDto {
    
    private Integer id;
    private String idDemand;
    private String demandeName;
    private String model;
    private String type;
    private String organizationalUnit;
    private String commonName;
    private LocalDate creationDate;
    private LocalDate expirationDate;

    // No-args constructor
    public CertifResponseDto() {
    }

    // All-args constructor
    public CertifResponseDto(Integer id, String idDemand, String demandeName, String model, 
                           String type, String organizationalUnit, String commonName,
                           LocalDate creationDate, LocalDate expirationDate) {
        this.id = id;
        this.idDemand = idDemand;
        this.demandeName = demandeName;
        this.model = model;
        this.type = type;
        this.organizationalUnit = organizationalUnit;
        this.commonName = commonName;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getIdDemand() {
        return idDemand;
    }

    public String getDemandeName() {
        return demandeName;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    public String getCommonName() {
        return commonName;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setIdDemand(String idDemand) {
        this.idDemand = idDemand;
    }

    public void setDemandeName(String demandeName) {
        this.demandeName = demandeName;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    // toString() method
    @Override
    public String toString() {
        return "CertifResponseDto{" +
               "id=" + id +
               ", idDemand='" + idDemand + '\'' +
               ", demandeName='" + demandeName + '\'' +
               ", model='" + model + '\'' +
               ", type='" + type + '\'' +
               ", organizationalUnit='" + organizationalUnit + '\'' +
               ", commonName='" + commonName + '\'' +
               ", creationDate=" + creationDate +
               ", expirationDate=" + expirationDate +
               '}';
    }

    // equals() and hashCode() methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CertifResponseDto that = (CertifResponseDto) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(idDemand, that.idDemand) &&
               Objects.equals(demandeName, that.demandeName) &&
               Objects.equals(model, that.model) &&
               Objects.equals(type, that.type) &&
               Objects.equals(organizationalUnit, that.organizationalUnit) &&
               Objects.equals(commonName, that.commonName) &&
               Objects.equals(creationDate, that.creationDate) &&
               Objects.equals(expirationDate, that.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idDemand, demandeName, model, type, 
                          organizationalUnit, commonName, creationDate, expirationDate);
    }
}
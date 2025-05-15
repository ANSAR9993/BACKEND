package com.dgapr.demo.Model;

import com.dgapr.demo.Audit.AuditListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@ToString
@Entity
@SQLDelete(sql = "UPDATE Certificate SET Is_Deleted = true WHERE id = ?")
@SQLRestriction("Is_Deleted = false")
@Table(name = "Certificate", uniqueConstraints = {
        @UniqueConstraint(columnNames = "ID_Demand")
})
@EntityListeners(AuditListener.class)
public class Certificat extends AuditedEntity implements Identifiable<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ID_Demand", nullable = false)
    private String idDemand;

    @Column(name = "Demande_Name", nullable = false)
    private String demandeName;

    @Column(name = "Model", nullable = false)
    private String model;

    @Column(name = "Type", nullable = false)
    private String type;

    @Column(name = "Organizational_Unit")
    private String organizationalUnit;

    @Column(name = "Common_Name", nullable = false)
    private String commonName;

    @Column(name = "Creation_Date")
    private java.time.LocalDate creationDate;

    @Column(name = "Expiration_Date", nullable = false)
    private java.time.LocalDate expirationDate;

    @Override
    public Integer getId() {
        return id;
    }

}


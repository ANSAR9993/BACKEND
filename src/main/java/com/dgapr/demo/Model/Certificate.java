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
@SQLDelete(sql = "UPDATE Certificate SET Is_Deleted = 1 WHERE id = ?")
@Table(name = "Certificate")
@EntityListeners(AuditListener.class)
public class Certificate extends AuditedEntity implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    public Long getId() {
        return id;
    }

}


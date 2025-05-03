package com.dgapr.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="role_name",length = 50, unique = true, nullable = false)
    private String name;  // e.g., "ADMIN", "USER"

    @Column(name = "role_description")
    private String description;

}

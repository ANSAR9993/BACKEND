package com.dgapr.demo.Dto.CertifDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

}

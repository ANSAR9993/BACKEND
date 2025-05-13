package com.dgapr.demo.Dto.CertifDto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CertifCreateDto {

    private String idDemand;
    private String demandeName;
    private String model;
    private String type;
    private String organizationalUnit;
    private String commonName;
    private LocalDate creationDate;
    private LocalDate expirationDate;

}

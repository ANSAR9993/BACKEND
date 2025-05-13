package com.dgapr.demo.Repository;

import com.dgapr.demo.Model.Certificat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CertifRepository extends JpaRepository<Certificat, Integer>, JpaSpecificationExecutor<Certificat> {

}

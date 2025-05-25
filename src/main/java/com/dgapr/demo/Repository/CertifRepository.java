package com.dgapr.demo.Repository;

import com.dgapr.demo.Model.Certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CertifRepository extends JpaRepository<Certificate, Integer>, JpaSpecificationExecutor<Certificate> {

}
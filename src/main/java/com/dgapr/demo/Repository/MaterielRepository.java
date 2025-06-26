package com.dgapr.demo.Repository;



import com.dgapr.demo.Model.Marche;
import com.dgapr.demo.Model.Materiel;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;



public interface MaterielRepository extends JpaRepository<Materiel, Long> {
 

    List<Materiel> findByMarcheId(Long marcheId);
    
    @Query("SELECT DISTINCT m.type_equipement FROM Materiel m")
    List<String> findDistinctTypeEquipement();
    @Query("SELECT DISTINCT m.gamme FROM Materiel m")
    List<String> findDistinctGamme();
  

}



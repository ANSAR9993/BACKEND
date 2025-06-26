package com.dgapr.demo.Repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dgapr.demo.Model.Marche;

@Repository


public interface MarcheRepository extends JpaRepository<Marche, Long> {
     
    @Query("SELECT DISTINCT m.societe FROM Marche m ")
    List<String> findDistinctSocietes();
    


}


    


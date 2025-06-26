package com.dgapr.demo.Service;



import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dgapr.demo.Model.Marche;
import com.dgapr.demo.Repository.MarcheRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MarcheService {

    @Autowired
    private MarcheRepository marcheRepository;

    public Marche addMarche(Marche marche) {
        return marcheRepository.save(marche);
    }

    public List<Marche> getAllMarches() {
        return marcheRepository.findAll();
    }

    public Marche getMarcheById(Long id) {
        Optional<Marche> optional = marcheRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new EntityNotFoundException("Marché non trouvé avec l'ID: " + id);
        }
    }

    public Marche updateMarche(Long id, Marche marcheDetails) {
        Marche marche = getMarcheById(id);
        marche.setTitre_marche(marcheDetails.getTitre_marche());
        marche.setId_matricule(marcheDetails.getId_matricule());
        marche.setSociete(marcheDetails.getSociete());
        marche.setDate_ordre_service(marcheDetails.getDate_ordre_service());
        marche.setDate_execution(marcheDetails.getDate_execution());
        marche.setDate_reception(marcheDetails.getDate_reception());
        marche.setGarantie_duree(marcheDetails.getGarantie_duree());
        marche.setGarantie_unite(marcheDetails.getGarantie_unite());

        return marcheRepository.save(marche);
    }

    public void deleteMarche(Long id) {
        Marche marche = getMarcheById(id);
        marcheRepository.delete(marche);
    }

    public List<String> getDistinctSocietes() {
        return marcheRepository.findDistinctSocietes();
    }

    public void someMethod(Marche marche) {
        System.out.println("Garantie durée: " + marche.getGarantie_duree());
        System.out.println("Unité de garantie: " + marche.getGarantie_unite());
    }
}


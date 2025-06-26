package com.dgapr.demo.Service;



import com.dgapr.demo.Model.Marche;
import com.dgapr.demo.Model.Materiel;
import com.dgapr.demo.Repository.MaterielRepository;
import com.dgapr.demo.Repository.MarcheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.dgapr.demo.Dto.MaterielDto;

import java.util.List;
import java.util.Optional;

@Service
public class MaterielService {
    @Autowired
    private MaterielRepository materielRepository;

    @Autowired
    private MarcheRepository marcheRepository;
    

    public Materiel saveMateriel(Materiel materiel) {
        return materielRepository.save(materiel);
    }

    public List<Materiel> getAllMateriels() {
        return materielRepository.findAll();
    }

    public Optional<Materiel> getMaterielById(Long id) {
        return materielRepository.findById(id);
    }

    public List<Materiel> getMaterielsByMarcheId(Long marcheId) {
        return materielRepository.findByMarcheId(marcheId);
    }

    public void deleteMateriel(Long id) {
        materielRepository.deleteById(id);
    }
    public List<String> getAllTypeEquipements() {
        return materielRepository.findDistinctTypeEquipement();
    }
    public List<String> getAllGammes() {
        return materielRepository.findDistinctGamme();
    }

    public void createMateriel(MaterielDto dto)
    {
        Materiel materiel = new Materiel();
        materiel.setType_equipement(dto.getType_equipement());
        materiel.setGamme(dto.getGamme());
        materiel.setNumero_serie(dto.getNumero_serie());
        materiel.setEmplacement(dto.getEmplacement());
        materiel.setGarantie_duree(dto.getGarantie_duree());
        materiel.setGarantie_unite(dto.getGarantie_unite());
        
      // charge marche
      Marche marche = 
        marcheRepository.findById(dto.getMarcheId())
                .orElseThrow(() -> new RuntimeException("Marche not found " ));
        materiel.setMarche(marche);
        
        // Save the Materiel entity
        materielRepository.save(materiel);
    }
     public void someMethod(Materiel materiel) {
        System.out.println("Garantie durée: " + materiel.getGarantie_duree());
        System.out.println("Unité de garantie: " + materiel.getGarantie_unite());
    }
}


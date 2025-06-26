package com.dgapr.demo.Controller;


import com.dgapr.demo.Dto.MaterielDto;
import com.dgapr.demo.Model.Marche;
import com.dgapr.demo.Model.Materiel;
import com.dgapr.demo.Service.MaterielService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dgapr.demo.Repository.MaterielRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/materiels") // Base URL for Materiel endpoints
public class MaterielController {

    @Autowired
    private MaterielService materielService;
    
    @PostMapping
     public ResponseEntity<?> addMateriel(@RequestBody MaterielDto dto) {
        try {
            materielService.createMateriel(dto);
            return ResponseEntity.ok("Materiel created successfully");
        } catch (Exception e) {
            
            return 
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
     }
    

    @GetMapping
    public List<Materiel> getAllMateriels() {
        return materielService.getAllMateriels();
    }

    @GetMapping("/{id}")
    public Optional<Materiel> getMaterielById(@PathVariable Long id) {
        return materielService.getMaterielById(id);
    }

    @GetMapping("/marches/{marcheId}")
    public List<Materiel> getMaterielsByMarcheId(@PathVariable Long marcheId) {
        return materielService.getMaterielsByMarcheId(marcheId);
    }

    @DeleteMapping("/{id}")
    public void deleteMateriel(@PathVariable Long id) {
        materielService.deleteMateriel(id);
    }

    @GetMapping("/typesequipements")
    public List<String> getAllTypeEquipements() {
        return materielService.getAllTypeEquipements();
    }
    @GetMapping("/gammes")
    public List<String> getAllGammes() {
        return materielService.getAllGammes();
    }
     public void someMethod(Materiel materiel) {
        System.out.println("Garantie durée: " + materiel.getGarantie_duree());
        System.out.println("Unité de garantie: " + materiel.getGarantie_unite());
    }
}


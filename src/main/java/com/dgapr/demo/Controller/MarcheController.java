package com.dgapr.demo.Controller;



import com.dgapr.demo.Model.Marche;
import com.dgapr.demo.Service.MarcheService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import com.dgapr.demo.Repository.MarcheRepository;
import java.util.List;


@RestController
@RequestMapping("/api/marches") // 

//@CrossOrigin(origins = "*") // 
  
public class MarcheController {

    private final MarcheService marcheService;
    
    @Autowired
    private MarcheRepository marcheRepository;

    @Autowired
    public MarcheController(MarcheService marcheService) {
        this.marcheService = marcheService;
    }

    // add new marche
    @PostMapping
    public Marche createMarche(@RequestBody Marche marche) {
        return marcheService.addMarche(marche);
    }

    // show all marchés
    @GetMapping
    public List<Marche> getAllMarches() {
        return marcheService.getAllMarches();
    }

    // get all marches by id
    @GetMapping("/{id}")
    public Marche getMarcheById(@PathVariable Long id) {
        return marcheService.getMarcheById(id);
    }

    //  update marché
    @PutMapping("/{id}")
    public Marche updateMarche(@PathVariable Long id, @RequestBody Marche marche) {
        return marcheService.updateMarche(id, marche);
    }

    //  delete marché
    @DeleteMapping("/{id}")
    public void deleteMarche(@PathVariable Long id) {
        marcheService.deleteMarche(id);
    }
    //  get all distinct sociétés
    @GetMapping("/societes")
    public List<String> getSocietes() {
        return marcheRepository.findDistinctSocietes();
    }

    // 

    public void someMethod(Marche marche) {
        System.out.println("Garantie duree: " + marche.getGarantie_duree());
        System.out.println("Garantie unite: " + marche.getGarantie_unite());
    }


    
    
    
}


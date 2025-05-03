package com.dgapr.demo.Controller;

import com.dgapr.demo.Dto.CertifDto.CertificatCreateDto;
import com.dgapr.demo.Dto.CertifDto.CertificatResponseDto;
import com.dgapr.demo.Dto.CertifDto.CertificatUpdateDto;
import com.dgapr.demo.Entity.Certificat;
import com.dgapr.demo.Repository.CertificatRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dgapr/certificats")
public class CertificatController {

    private final CertificatRepository certificatRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CertificatController(CertificatRepository certificatRepository, ModelMapper modelMapper) {
        this.certificatRepository = certificatRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public List<CertificatResponseDto> getAllCertificats() {

        return certificatRepository.findAll()
                .stream()
                .map(certificat -> modelMapper.map(certificat, CertificatResponseDto.class))
                .collect(Collectors.toList());
    }



    @GetMapping("/{id}")
    public ResponseEntity<CertificatResponseDto> getCertificatById(@PathVariable Integer id) {
        return certificatRepository.findById(id)
                .map(certificat -> modelMapper.map(certificat, CertificatResponseDto.class))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> createCertificat(@RequestBody CertificatCreateDto dto) {
        Certificat certificat = modelMapper.map(dto, Certificat.class);
        certificatRepository.save(certificat);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificatResponseDto> updateCertificat(
            @PathVariable Integer id,
            @RequestBody CertificatUpdateDto dto
    ) {
        return certificatRepository.findById(id)
                .map(certificat -> {
                    certificat.setDemandeName(dto.getDemandeName());
                    certificat.setModel(dto.getModel());
                    certificat.setType(dto.getType());
                    certificat.setOrganizationalUnit(dto.getOrganizationalUnit());
                    certificat.setCommonName(dto.getCommonName());
                    certificat.setExpirationDate(dto.getExpirationDate());
                    certificatRepository.save(certificat);
                    return modelMapper.map(certificat, CertificatResponseDto.class);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificat(@PathVariable Integer id) {
        if (!certificatRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        certificatRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

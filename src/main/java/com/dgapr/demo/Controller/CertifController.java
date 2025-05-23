package com.dgapr.demo.Controller;

import com.dgapr.demo.Dto.CertifDto.CertifCreateDto;
import com.dgapr.demo.Dto.CertifDto.CertifResponseDto;
import com.dgapr.demo.Dto.CertifDto.CertifUpdateDto;
import com.dgapr.demo.Service.CertifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/certificats")
public class CertifController {

    private final CertifService certifService;

    @Autowired
    public CertifController(CertifService certifService) {
        this.certifService = certifService;
    }

    @GetMapping
    public ResponseEntity<Page<CertifResponseDto>> getCertificatsPaginated(
            @PageableDefault(size = 100, sort = "expirationDate", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam Map<String, String> filterParams
    ) {
        Page<CertifResponseDto> certificatsPage = certifService.getCertificats(pageable, filterParams);
        return ResponseEntity.ok(certificatsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertifResponseDto> getCertificateById(
            @PathVariable Integer id
    ) {
        return certifService.getCertificatById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> createCertificate(
            @RequestBody CertifCreateDto dto
    ) {
        try {
            certifService.createCertificat(dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertifResponseDto> updateCertificate(
            @PathVariable Integer id,
            @RequestBody CertifUpdateDto dto
    ) {
        return certifService.updateCertificat(id, dto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificate(
            @PathVariable Integer id
    ) {
        if (!certifService.deleteCertificat(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

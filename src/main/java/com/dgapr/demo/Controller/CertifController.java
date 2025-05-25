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

/**
 * REST controller for managing certificates.
 * Provides endpoints for retrieving, creating, updating, and deleting certificates.
 */
@RestController
@RequestMapping("/api/certificats")
public class CertifController {

    private final CertifService certifService;

    @Autowired
    public CertifController(CertifService certifService) {
        this.certifService = certifService;
    }

    /**
     * Retrieves a paginated list of certificates, with optional filtering.
     *
     * @param pageable A {@link Pageable} object containing pagination and sorting information.
     * Defaults to 50 items per page, sorted by 'expirationDate' in ascending order.
     * @param filterParams A {@link Map} of filter parameters to apply to the certificate search.
     * @return A {@link ResponseEntity} containing a {@link Page} of {@link CertifResponseDto} objects
     * and an HTTP status of OK.
     */
    @GetMapping
    public ResponseEntity<Page<CertifResponseDto>> getCertificatsPaginated(
            @PageableDefault(size = 50, sort = "expirationDate", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam Map<String, String> filterParams
    ) {
        Page<CertifResponseDto> certificatsPage = certifService.getCertificats(pageable, filterParams);
        return ResponseEntity.ok(certificatsPage);
    }

    /**
     * Retrieves a single certificate by its ID.
     *
     * @param id The unique identifier of the certificate to retrieve.
     * @return A {@link ResponseEntity} containing the {@link CertifResponseDto} if found (HTTP 200 OK),
     * or an HTTP 404 Not Found status if no certificate with the given ID exists.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CertifResponseDto> getCertificateById(
            @PathVariable Integer id
    ) {
        return certifService.getCertificatById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new certificate.
     *
     * @param dto The {@link CertifCreateDto} containing the data for the new certificate.
     * @return A {@link ResponseEntity} with:
     * - HTTP 201 Created if the certificate was successfully created.
     * - HTTP 400 Bad Request if one or more required fields are null.
     * - HTTP 409 Conflict if there's a data integrity violation (e.g., duplicate unique key).
     */
    @PostMapping
    public ResponseEntity<Void> createCertificate(@RequestBody CertifCreateDto dto) {
        certifService.createCertificat(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Updates an existing certificate.
     *
     * @param id The unique identifier of the certificate to update.
     * @param dto The {@link CertifUpdateDto} containing the updated certificate data.
     * @return A {@link ResponseEntity} containing the updated {@link CertifResponseDto} if found and updated (HTTP 200 OK),
     * or an HTTP 404 Not Found status if no certificate with the given ID exists.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CertifResponseDto> updateCertificate(
            @PathVariable Integer id,
            @RequestBody CertifUpdateDto dto
    ) {
        return certifService.updateCertificat(id, dto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a certificate by its ID (soft delete).
     * The certificate's 'isDeleted' flag will be set to true.
     *
     * @param id The unique identifier of the certificate to delete.
     * @return A {@link ResponseEntity} with:
     * - HTTP 204 No Content if the certificate was successfully soft-deleted.
     * - HTTP 404 Not Found if no active certificate with the given ID exists.
     */
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

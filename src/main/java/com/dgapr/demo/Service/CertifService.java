package com.dgapr.demo.Service;

import com.dgapr.demo.Dto.CertifDto.CertifCreateDto;
import com.dgapr.demo.Dto.CertifDto.CertifResponseDto;
import com.dgapr.demo.Dto.CertifDto.CertifUpdateDto;
import com.dgapr.demo.Exception.DuplicateCertificateException;
import com.dgapr.demo.Model.Certificate.Certificate;
import com.dgapr.demo.Repository.CertifRepository;
import com.dgapr.demo.Specification.CertificatSpecification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing certificate operations.
 * Provides business logic for retrieving, creating, updating, and soft-deleting certificates.
 */
@Service
public class CertifService {

    private final CertifRepository certifRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CertifService(CertifRepository certifRepository, ModelMapper modelMapper) {
        this.certifRepository = certifRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves a paginated list of certificates based on provided filters.
     * Applies dynamic filtering using {@link CertificatSpecification}.
     *
     * @param pageable A {@link Pageable} object containing pagination and sorting information.
     * @param filterParams A {@link Map} of filter parameters to apply to the certificate search.
     * @return A {@link Page} of {@link CertifResponseDto} objects representing the filtered and paginated certificates.
     */
    public Page<CertifResponseDto> getCertificats(Pageable pageable, Map<String, String> filterParams) {
        CertificatSpecification spec = new CertificatSpecification(filterParams);
        Page<Certificate> certificatPage = certifRepository.findAll(spec, pageable);

        List<CertifResponseDto> content = certificatPage.getContent()
                .stream()
                .map(certificate -> modelMapper.map(certificate, CertifResponseDto.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, certificatPage.getPageable(), certificatPage.getTotalElements());
    }

    /**
     * Retrieves a single certificate by its ID.
     *
     * @param id The unique identifier of the certificate.
     * @return An {@link Optional} containing the {@link CertifResponseDto} if an active certificate with the given ID is found,
     * otherwise an empty {@link Optional}.
     */
    public Optional<CertifResponseDto> getCertificatById(Integer id) {
        return certifRepository.findById(id)
                .filter(cert -> !cert.getIsDeleted())
                .map(certificate -> modelMapper.map(certificate, CertifResponseDto.class));
    }

    /**
     * Creates a new certificate from the provided DTO.
     *
     * @param dto The {@link CertifCreateDto} containing the data for the new certificate.
     * @throws IllegalArgumentException if one or more required fields in the DTO are null,
     * or if a data integrity violation occurs (e.g., unique constraint violation).
     */
    public void createCertificat(CertifCreateDto dto) {
        if (dto.getIdDemand() == null || dto.getDemandeName() == null || dto.getModel() == null ||
            dto.getType() == null || dto.getCommonName() == null || dto.getExpirationDate() == null) {
            throw new IllegalArgumentException("One or more required fields are null.");
        }

        Certificate certificate = modelMapper.map(dto, Certificate.class);
        try {
            certifRepository.save(certificate);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCertificateException("Certificate with this Common Name already exists.", e);
        }
    }

    /**
     * Updates an existing certificate.
     *
     * @param id The unique identifier of the certificate to update.
     * @param dto The {@link CertifUpdateDto} containing the updated certificate data.
     * @return An {@link Optional} containing the updated {@link CertifResponseDto} if the certificate was found and updated,
     * otherwise an empty {@link Optional}.
     * @throws IllegalArgumentException if a data integrity violation occurs during the update.
     */
    public Optional<CertifResponseDto> updateCertificat(Integer id, CertifUpdateDto dto) {
        return certifRepository.findById(id)
                .map(certificate -> {
                    certificate.setDemandeName(dto.getDemandeName());
                    certificate.setModel(dto.getModel());
                    certificate.setType(dto.getType());
                    certificate.setOrganizationalUnit(dto.getOrganizationalUnit());
                    certificate.setCommonName(dto.getCommonName());
                    certificate.setExpirationDate(dto.getExpirationDate());
                    try {
                        certifRepository.save(certificate);
                    } catch (DataIntegrityViolationException e) {
                        throw new IllegalArgumentException("Unique constraint violation or invalid data.");
                    }
                    return modelMapper.map(certificate, CertifResponseDto.class);
                });
    }

    /**
     * Performs a soft delete on a certificate by setting its 'isDeleted' flag to true.
     *
     * @param id The unique identifier of the certificate to delete.
     * @return {@code true} if the certificate was found and successfully soft-deleted,
     * {@code false} if no active certificate with the given ID was found.
     */
    public boolean deleteCertificat(Integer id) {
        return certifRepository.findById(id)
                .filter(cert -> !cert.getIsDeleted()) // Only delete if not already deleted
                .map(cert -> {
                    cert.setIsDeleted(true);
                    certifRepository.save(cert);
                    return true;
                })
                .orElse(false);
    }
}

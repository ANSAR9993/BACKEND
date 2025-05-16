package com.dgapr.demo.Service;

import com.dgapr.demo.Dto.CertifDto.CertifCreateDto;
import com.dgapr.demo.Dto.CertifDto.CertifResponseDto;
import com.dgapr.demo.Dto.CertifDto.CertifUpdateDto;
import com.dgapr.demo.Model.Certificate;
import com.dgapr.demo.Repository.CertifRepository;
import com.dgapr.demo.Specification.CertificatSpecification;
import jakarta.validation.constraints.NotNull;
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

@Service
public class CertifService {

    private final CertifRepository certifRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CertifService(CertifRepository certifRepository, ModelMapper modelMapper) {
        this.certifRepository = certifRepository;
        this.modelMapper = modelMapper;
    }

    public Page<CertifResponseDto> getCertificatsPaginated(Pageable pageable, Map<String, String> filterParams) {
        return getCertifResponseDtos(pageable, filterParams);
    }

    @NotNull
    private Page<CertifResponseDto> getCertifResponseDtos(Pageable pageable, Map<String, String> filterParams) {
        CertificatSpecification spec = new CertificatSpecification(filterParams);
        Page<Certificate> certificatPage = certifRepository.findAll(spec, pageable);

        List<CertifResponseDto> content = certificatPage.getContent()
                .stream()
                .map(certificate -> modelMapper.map(certificate, CertifResponseDto.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, certificatPage.getPageable(), certificatPage.getTotalElements());
    }

    public Optional<CertifResponseDto> getCertificatById(Integer id) {
        return certifRepository.findById(id)
                .filter(cert -> !cert.getIsDeleted())
                .map(certificate -> modelMapper.map(certificate, CertifResponseDto.class));
    }

    public void createCertificat(CertifCreateDto dto) {
        if (dto.getIdDemand() == null || dto.getDemandeName() == null || dto.getModel() == null ||
            dto.getType() == null || dto.getCommonName() == null || dto.getExpirationDate() == null) {
            throw new IllegalArgumentException("One or more required fields are null.");
        }

        Certificate certificate = modelMapper.map(dto, Certificate.class);
        try {
            certifRepository.save(certificate);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Unique constraint violation or invalid data.");
        }
    }

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

    public boolean deleteCertificat(Integer id) {
        return certifRepository.findById(id)
                .filter(cert -> !cert.getIsDeleted())
                .map(cert -> {
                    cert.setIsDeleted(true);
                    certifRepository.save(cert);
                    return true;
                })
                .orElse(false);
    }

//    public List<CertifResponseDto> getAllCertificats() {
//        return certifRepository.findAll()
//                .stream()
//                .map(certificat -> modelMapper.map(certificat, CertifResponseDto.class))
//                .collect(Collectors.toList());
//    }

}

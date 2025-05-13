package com.dgapr.demo.Service;

import com.dgapr.demo.Dto.CertifDto.CertifCreateDto;
import com.dgapr.demo.Dto.CertifDto.CertifResponseDto;
import com.dgapr.demo.Dto.CertifDto.CertifUpdateDto;
import com.dgapr.demo.Model.Certificat;
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
        Page<Certificat> certificatPage = certifRepository.findAll(spec, pageable);

        List<CertifResponseDto> content = certificatPage.getContent()
                .stream()
                .map(certificat -> modelMapper.map(certificat, CertifResponseDto.class))
                .collect(Collectors.toList());

        return new PageImpl<>(content, certificatPage.getPageable(), certificatPage.getTotalElements());
    }

    public Optional<CertifResponseDto> getCertificatById(Integer id) {
        return certifRepository.findById(id)
                .filter(cert -> !cert.getIsDeleted())
                .map(certificat -> modelMapper.map(certificat, CertifResponseDto.class));
    }

    public void createCertificat(CertifCreateDto dto) {
        if (dto.getIdDemand() == null || dto.getDemandeName() == null || dto.getModel() == null ||
            dto.getType() == null || dto.getCommonName() == null || dto.getExpirationDate() == null) {
            throw new IllegalArgumentException("One or more required fields are null.");
        }

        Certificat certificat = modelMapper.map(dto, Certificat.class);
        try {
            certifRepository.save(certificat);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Unique constraint violation or invalid data.");
        }
    }

    public Optional<CertifResponseDto> updateCertificat(Integer id, CertifUpdateDto dto) {
        return certifRepository.findById(id)
                .map(certificat -> {
                    certificat.setDemandeName(dto.getDemandeName());
                    certificat.setModel(dto.getModel());
                    certificat.setType(dto.getType());
                    certificat.setOrganizationalUnit(dto.getOrganizationalUnit());
                    certificat.setCommonName(dto.getCommonName());
                    certificat.setExpirationDate(dto.getExpirationDate());
                    try {
                        certifRepository.save(certificat);
                    } catch (DataIntegrityViolationException e) {
                        throw new IllegalArgumentException("Unique constraint violation or invalid data.");
                    }
                    return modelMapper.map(certificat, CertifResponseDto.class);
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

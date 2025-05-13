package com.dgapr.demo.Config;

import com.dgapr.demo.Dto.CertifDto.CertifCreateDto;
import com.dgapr.demo.Model.Certificat;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        mm.typeMap(CertifCreateDto.class, Certificat.class)
                .addMappings(m -> m.skip(Certificat::setId));
        return mm;
    }
}

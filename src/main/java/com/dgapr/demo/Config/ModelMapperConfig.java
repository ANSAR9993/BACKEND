package com.dgapr.demo.Config;

import com.dgapr.demo.Dto.CertifDto.CertifCreateDto;
import com.dgapr.demo.Model.Certificate;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        mm.typeMap(CertifCreateDto.class, Certificate.class)
                .addMappings(m -> m.skip(Certificate::setId));
        return mm;
    }
}

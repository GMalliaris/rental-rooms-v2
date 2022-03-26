package org.gmalliaris.rental.rooms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gmalliaris.rental.rooms.converter.UserRoleConverter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RentalRoomsConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(){
        return new ObjectMapper()
                .findAndRegisterModules();
    }

    @Bean
    public ModelMapper modelMapper() {

        var modelMapper = new ModelMapper();

        modelMapper.addConverter(new UserRoleConverter());

        return modelMapper;
    }
}

package org.gmalliaris.rental.rooms;

import org.gmalliaris.rental.rooms.config.WebSecurityConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("disable-jwt-auth")
@ComponentScan("org.gmalliaris.rental.rooms.config")
public class UnitTestConfig {

    @Bean
    public WebSecurityConfigService webSecurityServiceConfig(){
        return new WebSecurityConfigServiceTestImpl();
    }
}

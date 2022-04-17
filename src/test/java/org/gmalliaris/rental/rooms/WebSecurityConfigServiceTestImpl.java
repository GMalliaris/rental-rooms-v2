package org.gmalliaris.rental.rooms;

import org.gmalliaris.rental.rooms.config.WebSecurityConfigService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

public class WebSecurityConfigServiceTestImpl implements WebSecurityConfigService {

    @Override
    public void configure(AuthenticationManagerBuilder auth, BCryptPasswordEncoder bCryptPasswordEncoder) {
        // Do nothing
    }

    @Override
    public void configure(HttpSecurity http) {
        // Do nothing
    }
}

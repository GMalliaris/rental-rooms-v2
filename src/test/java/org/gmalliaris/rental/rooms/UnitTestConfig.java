package org.gmalliaris.rental.rooms;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ActiveProfiles("test-security")
@ComponentScan("org.gmalliaris.rental.rooms.config")
public class UnitTestConfig {
}

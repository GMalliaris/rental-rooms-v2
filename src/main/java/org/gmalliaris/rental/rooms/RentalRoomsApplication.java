package org.gmalliaris.rental.rooms;

import org.gmalliaris.rental.rooms.config.JwtConfigurationProperties;
import org.gmalliaris.rental.rooms.config.RedisConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@ConfigurationPropertiesScan(basePackageClasses = {
		RedisConfigurationProperties.class,
		JwtConfigurationProperties.class
})
public class  RentalRoomsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalRoomsApplication.class, args);
	}

}

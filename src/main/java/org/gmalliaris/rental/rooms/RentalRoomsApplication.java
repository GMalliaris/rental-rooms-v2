package org.gmalliaris.rental.rooms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class  RentalRoomsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalRoomsApplication.class, args);
	}

}

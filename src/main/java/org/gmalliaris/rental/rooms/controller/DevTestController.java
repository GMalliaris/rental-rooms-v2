package org.gmalliaris.rental.rooms.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/dev")
public class DevTestController {

    @GetMapping("/test")
    public String getStatus(){
        return "Application is up.";
    }
}



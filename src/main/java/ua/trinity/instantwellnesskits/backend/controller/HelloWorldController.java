package ua.trinity.instantwellnesskits.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping
    public String helloWorld() {
        return "Hello World";
    }
}

package com.turnerturn.httpstreamer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@EnableScheduling
@SpringBootApplication
@RestController
public class HttpStreamerApplication {

    @RequestMapping("/")
    public String home() {
        return "Hello World";
    }
	public static void main(String[] args) {
		SpringApplication.run(HttpStreamerApplication.class, args);
	}

}



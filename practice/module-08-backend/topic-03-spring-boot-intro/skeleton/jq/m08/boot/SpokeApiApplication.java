package jq.m08.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The Spoke API. Spring Boot starts here and finds every class in jq.m08.boot. */
@SpringBootApplication
public class SpokeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpokeApiApplication.class, args);
    }
}

package jq.m08.mvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the Spoke API. The tests find this class by its package. */
@SpringBootApplication
public class SpokeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpokeApiApplication.class, args);
    }
}

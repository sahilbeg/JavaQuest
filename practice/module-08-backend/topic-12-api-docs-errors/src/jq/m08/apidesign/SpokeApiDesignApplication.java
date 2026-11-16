package jq.m08.apidesign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The public rentals API that partners such as Riverton Transit build on. */
@SpringBootApplication
public class SpokeApiDesignApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpokeApiDesignApplication.class, args);
    }
}

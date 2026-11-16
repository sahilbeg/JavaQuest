package jq.m08.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The Spoke API for this ticket: stations, a rider's own rentals, and an ops-only bike move. */
@SpringBootApplication
public class SpokeSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpokeSecurityApplication.class, args);
    }
}

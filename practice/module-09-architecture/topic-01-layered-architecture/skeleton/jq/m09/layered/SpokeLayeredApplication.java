package jq.m09.layered;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The rentals API, split into layers: web, service, domain and persistence. */
@SpringBootApplication
public class SpokeLayeredApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpokeLayeredApplication.class, args);
    }
}

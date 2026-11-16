package jq.m08.di;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The Spoke API. Spring scans jq.m08.di and every package below it for beans. */
@SpringBootApplication
public class SpokeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpokeApiApplication.class, args);
    }
}

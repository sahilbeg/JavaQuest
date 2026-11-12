package dev.javaquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JavaQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaQuestApplication.class, args);
    }
}

package jq.m10.launch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spoke's Bramford rentals service. The rental rules live in the core (RentalService and friends);
 * everything Spring-shaped is an adapter around it. This service talks to its database with plain JDBC,
 * so the JPA and Hibernate auto-configuration is switched off.
 */
@SpringBootApplication(excludeName = {
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
        "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"})
public class LaunchApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaunchApplication.class, args);
    }
}

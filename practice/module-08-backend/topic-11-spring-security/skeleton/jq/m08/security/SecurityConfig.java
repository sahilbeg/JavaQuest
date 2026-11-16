package jq.m08.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** Who may call what. Spring Security reads this before any controller runs. */
@Configuration
// TODO 1: add @EnableWebSecurity here. Until then, Spring Security is switched off and every URL is open.
public class SecurityConfig {

    // TODO 2: put @Bean on this method, so Spring uses your filter chain (it needs TODO 1 first)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO 3: authorizeHttpRequests: GET /stations permitAll, /admin/** hasRole("OPS"), anyRequest authenticated
        // TODO 4: switch on httpBasic(Customizer.withDefaults()), make sessions STATELESS and disable csrf
        return http.build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    PasswordEncoder passwordEncoder() {
        // TODO 5: return a new BCryptPasswordEncoder() instead. NoOp keeps passwords as plain text.
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder) {
        // TODO 6: add ada (password "ride-on", role RIDER) and theo (password "fix-it", role OPS),
        //         each built with User.withUsername(...).password(encoder.encode(...)).roles(...).build()
        return new InMemoryUserDetailsManager();
    }
}

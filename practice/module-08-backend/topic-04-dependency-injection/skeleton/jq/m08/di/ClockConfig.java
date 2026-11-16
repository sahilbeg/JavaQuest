package jq.m08.di;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Clock is a JDK class, so we cannot put @Component on it. A @Bean method hands Spring one instead. */
// TODO 3: put @Configuration above the class and @Bean above the clock() method
public class ClockConfig {

    public Clock clock() {
        return Clock.systemUTC();
    }
}

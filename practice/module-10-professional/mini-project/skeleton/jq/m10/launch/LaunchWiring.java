package jq.m10.launch;

import java.time.Clock;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The composition root: the one place that builds the adapters and plugs them into Bramford's core.
 * The core classes carry no Spring annotations at all.
 */
@Configuration
public class LaunchWiring {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public CityConfig city() {
        return CityConfig.bramford();
    }

    @Bean
    public InMemoryBikeInventory bikes() {
        return bramfordFleet();
    }

    @Bean
    public RecordingEventPublisher events() {
        return new RecordingEventPublisher();
    }

    @Bean
    public RentalRepository rentalRepository(JdbcTemplate jdbc) {
        // TODO 12 (stage 5): when JdbcRentalRepository is done, plug it in here instead of the map:
        //          return new JdbcRentalRepository(jdbc);  The core does not change at all.
        return new InMemoryRentalRepository();
    }

    @Bean
    public RentalUseCases rentals(CityConfig city, RentalRepository rentalRepository, BikeInventory bikes,
                                  EventPublisher events, Clock clock) {
        return new RentalService(city, rentalRepository, bikes, events, clock);
    }

    @Bean
    public StationDirectory stationDirectory(CityConfig city) {
        return new StationDirectory(city);
    }

    /** Runs once the app has started: loads Bramford's stations. Until then, /health/ready says DOWN. */
    @Bean
    public ApplicationRunner loadStations(StationDirectory stations) {
        return args -> stations.loadAll();
    }

    /**
     * Launch day fleet: four bikes at each station, two CLASSIC (…1, …2) and two EBIKE (…3, …4).
     * The truck also dropped BR-2900, a CARGO bike, at market-square by mistake. Bramford does not rent CARGO.
     */
    static InMemoryBikeInventory bramfordFleet() {
        InMemoryBikeInventory fleet = new InMemoryBikeInventory();
        String[] stations = {"castle-hill", "canal-street", "market-square", "university", "bramford-central"};
        for (int s = 0; s < stations.length; s++) {
            for (int n = 1; n <= 4; n++) {
                String id = "BR-20" + s + n;
                BikeType type = n <= 2 ? BikeType.CLASSIC : BikeType.EBIKE;
                fleet.add(Bike.docked(id, "bramford", type, stations[s]));
            }
        }
        fleet.add(Bike.docked("BR-2900", "bramford", BikeType.CARGO, "market-square"));
        return fleet;
    }
}

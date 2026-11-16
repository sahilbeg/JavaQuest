package jq.m08.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(StationReport.class)
class StationReportTest {

    @Autowired TestEntityManager em;
    @Autowired StationReport report;

    @BeforeEach
    void fillTheFleet() {
        Station harbor = em.persist(new Station("harbor", "Harbor", 12));
        Station uptown = em.persist(new Station("uptown", "Uptown", 12));
        Station riverside = em.persist(new Station("riverside", "Riverside", 12));
        Station oldMill = em.persist(new Station("old-mill", "Old Mill", 12));

        // Saved out of order on purpose, so sorting has to come from your query
        em.persist(new Bike("B-1050", BikeType.EBIKE, BikeStatus.AVAILABLE, harbor));
        em.persist(new Bike("B-1042", BikeType.EBIKE, BikeStatus.AVAILABLE, harbor));
        em.persist(new Bike("B-1047", BikeType.EBIKE, BikeStatus.RENTED, harbor));
        em.persist(new Bike("B-1044", BikeType.CLASSIC, BikeStatus.MAINTENANCE, harbor));
        em.persist(new Bike("B-1043", BikeType.CLASSIC, BikeStatus.AVAILABLE, harbor));

        em.persist(new Bike("B-2001", BikeType.EBIKE, BikeStatus.AVAILABLE, uptown));
        em.persist(new Bike("B-2002", BikeType.CLASSIC, BikeStatus.AVAILABLE, uptown));
        em.persist(new Bike("B-2003", BikeType.CARGO, BikeStatus.RENTED, uptown));

        em.persist(new Bike("B-3003", BikeType.EBIKE, BikeStatus.AVAILABLE, riverside));
        em.persist(new Bike("B-3001", BikeType.EBIKE, BikeStatus.AVAILABLE, riverside));
        em.persist(new Bike("B-3002", BikeType.EBIKE, BikeStatus.AVAILABLE, riverside));

        em.persist(new Bike("B-4001", BikeType.CLASSIC, BikeStatus.MAINTENANCE, oldMill));

        em.flush();   // send the inserts to the database now
        em.clear();   // forget the cached objects, so every read below really asks the database
    }

    @Test
    @DisplayName("Free bikes at Harbor come back as ids, only AVAILABLE ones, sorted by id")
    void freeBikesAtHarbor() {
        List<String> ids = report.freeBikeIds("harbor");

        assertEquals(List.of("B-1042", "B-1043", "B-1050"), ids,
                "freeBikeIds(\"harbor\") should list Harbor's AVAILABLE bikes, sorted by id. "
                        + "B-1047 is RENTED and B-1044 is in MAINTENANCE, so they stay out");
    }

    @Test
    @DisplayName("Free bikes at Uptown stay at Uptown, and Old Mill has none")
    void freeBikesOnlyFromThatStation() {
        assertEquals(List.of("B-2001", "B-2002"), report.freeBikeIds("uptown"),
                "freeBikeIds(\"uptown\") should list only Uptown's AVAILABLE bikes, not bikes from other stations");
        assertEquals(List.of(), report.freeBikeIds("old-mill"),
                "freeBikeIds(\"old-mill\") should be an empty list: Old Mill's only bike is in MAINTENANCE");
    }

    @Test
    @DisplayName("Bikes are counted by status, with one number for every status")
    void countsByStatus() {
        Map<BikeStatus, Long> counts = report.countByStatus();

        assertEquals(8L, counts.get(BikeStatus.AVAILABLE),
                "countByStatus() should count 8 AVAILABLE bikes (3 at Harbor, 2 at Uptown, 3 at Riverside)");
        assertEquals(2L, counts.get(BikeStatus.RENTED),
                "countByStatus() should count 2 RENTED bikes (B-1047 and B-2003)");
        assertEquals(2L, counts.get(BikeStatus.MAINTENANCE),
                "countByStatus() should count 2 bikes in MAINTENANCE (B-1044 and B-4001)");
    }

    @Test
    @DisplayName("Stations with at least 2 free e-bikes are Harbor and Riverside, sorted by id")
    void stationsWithTwoFreeEbikes() {
        assertEquals(List.of("harbor", "riverside"), report.stationsWithFreeEbikes(2),
                "stationsWithFreeEbikes(2) should find harbor (2 free e-bikes) and riverside (3). "
                        + "Uptown has only 1, and Harbor's rented e-bike does not count");
    }

    @Test
    @DisplayName("A minimum of 1 finds three stations, 3 finds one, and 4 finds none")
    void stationsWithOtherMinimums() {
        assertEquals(List.of("harbor", "riverside", "uptown"), report.stationsWithFreeEbikes(1),
                "stationsWithFreeEbikes(1) should find every station with a free e-bike, sorted by id");
        assertEquals(List.of("riverside"), report.stationsWithFreeEbikes(3),
                "stationsWithFreeEbikes(3) should find only riverside, which has 3 free e-bikes");
        assertEquals(List.of(), report.stationsWithFreeEbikes(4),
                "stationsWithFreeEbikes(4) should be an empty list: no station has 4 free e-bikes");
    }

    @Test
    @DisplayName("Bike type and status are stored in the database as words, not numbers")
    void enumsAreStoredAsText() {
        Object[] row = (Object[]) em.getEntityManager()
                .createNativeQuery("select type, status from bike where id = 'B-1047'")
                .getSingleResult();

        assertEquals("EBIKE", String.valueOf(row[0]),
                "the type column for B-1047 should hold the word EBIKE. A number means the field "
                        + "is missing @Enumerated(EnumType.STRING)");
        assertEquals("RENTED", String.valueOf(row[1]),
                "the status column for B-1047 should hold the word RENTED. A number means the field "
                        + "is missing @Enumerated(EnumType.STRING)");
    }
}

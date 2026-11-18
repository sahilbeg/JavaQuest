package dev.cadence.career;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.cadence.web.LocalOnlyFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** An incident and a feature in a throwaway repository, graded through the real runner. */
@SpringBootTest
@AutoConfigureMockMvc
class IncidentFlowTest {

    private static final String MODULE = "module-00-foundations";

    @Autowired
    MockMvc mvc;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        Path root = Files.createTempDirectory("cadence-fixture");
        writeModule(root);
        writeUnit(root, "boss", "INC-00", "theo", """
                public static int bronze() { return 1; }
                public static int silver() { return 2; }
                public static int gold() { return 0; }
                """, "Bronze", "Silver", "Gold");
        writeUnit(root, "mini-project", "FEAT-00", "priya", """
                public static int bronze() { return 1; }
                public static int silver() { return 2; }
                public static int gold() { return 3; }
                """, "");
        Path classpathFile = Files.createTempFile("runner-classpath", ".txt");
        Files.writeString(classpathFile, System.getProperty("junit.console.jar"));
        registry.add("cadence.root", root::toString);
        registry.add("cadence.runner.classpath-file", classpathFile::toString);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:incident-flow;DB_CLOSE_DELAY=-1");
    }

    private static void writeModule(Path root) throws IOException {
        Path dir = Files.createDirectories(root.resolve("content").resolve(MODULE));
        Files.writeString(dir.resolve("module.yaml"), "id: " + MODULE + "\ntitle: Foundations\nproject: Day one\n");
    }

    private static void writeUnit(Path root, String unit, String key, String from, String body, String... tiers) throws IOException {
        Path content = Files.createDirectories(root.resolve("content").resolve(MODULE).resolve(unit));
        Files.writeString(content.resolve("meta.yaml"), """
                id: %s/%s
                title: %s
                order: 99
                minutes: 10
                practice: { kind: code }
                ticket: { key: %s, from: %s, title: Fix it, body: Something broke. }
                hints: [one, two, three]
                review: false
                """.formatted(MODULE, unit, key, key, from));
        Files.writeString(content.resolve("lesson.md"), "## Hook\nIt broke.\n");
        Path practice = root.resolve("practice").resolve(MODULE).resolve(unit);
        String kiosk = "package fx;\n\npublic class Kiosk {\n" + body + "}\n";
        for (String folder : new String[] {"src", "skeleton", "solution"}) {
            Files.createDirectories(practice.resolve(folder).resolve("fx"));
            Files.writeString(practice.resolve(folder).resolve("fx/Kiosk.java"), kiosk);
        }
        Files.createDirectories(practice.resolve("test/fx"));
        int expected = 1;
        for (String tier : tiers) {
            String method = tier.isEmpty() ? "bronze" : tier.toLowerCase();
            Files.writeString(practice.resolve("test/fx/Kiosk" + tier + "Test.java"), """
                    package fx;

                    import static org.junit.jupiter.api.Assertions.assertEquals;
                    import org.junit.jupiter.api.Test;

                    class Kiosk%sTest {
                        @Test
                        void works() {
                            assertEquals(%d, Kiosk.%s());
                        }
                    }
                    """.formatted(tier, expected++, method));
        }
    }

    @Test
    void incidentPaysPerTierAndFeaturePaysOnce() throws Exception {
        String boss = "/api/topics/" + MODULE + "/boss";
        mvc.perform(get(boss))
                .andExpect(jsonPath("$.hasQuiz").value(false))
                .andExpect(jsonPath("$.kind").value("incident"));
        mvc.perform(get(boss + "/quiz")).andExpect(status().isNotFound());

        mvc.perform(post(boss + "/submit").header(LocalOnlyFilter.WRITE_HEADER, "1"))
                .andExpect(jsonPath("$.result.outcome").value("FAILED"))
                .andExpect(jsonPath("$.result.tests[0].className").value(Matchers.startsWith("fx.Kiosk")));
        mvc.perform(post(boss + "/submit").header(LocalOnlyFilter.WRITE_HEADER, "1"));
        mvc.perform(get(boss + "/progress"))
                .andExpect(jsonPath("$.progress.mastered").value(true))
                .andExpect(jsonPath("$.progress.masteryPercent").value(100));
        mvc.perform(get("/api/career"))
                .andExpect(jsonPath("$.level.xp").value(300 + 100 + 5))
                .andExpect(jsonPath("$.kudos[?(@.id == 'boss-slayer')].earned").value(Matchers.contains(true)))
                .andExpect(jsonPath("$.kudos[?(@.id == 'gold-standard')].earned").value(Matchers.contains(false)))
                .andExpect(jsonPath("$.reviewsDue.length()").value(0));

        String feature = "/api/topics/" + MODULE + "/mini-project";
        mvc.perform(post(feature + "/submit").header(LocalOnlyFilter.WRITE_HEADER, "1"))
                .andExpect(jsonPath("$.result.outcome").value("PASSED"));
        mvc.perform(post(feature + "/submit").header(LocalOnlyFilter.WRITE_HEADER, "1"));
        mvc.perform(get("/api/career"))
                .andExpect(jsonPath("$.level.xp").value(405 + 400))
                .andExpect(jsonPath("$.kudos[?(@.id == 'shipped-" + MODULE + "')].earned").value(Matchers.contains(true)));
    }
}

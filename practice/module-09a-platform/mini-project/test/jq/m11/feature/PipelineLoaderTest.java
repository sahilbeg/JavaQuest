package jq.m11.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PipelineLoaderTest {

    private static final String HEADER = "name: spoke-api\nstages:\n";

    private static void assertRejected(String yaml, String mustMention, String why) {
        PipelineException error = assertThrows(PipelineException.class, () -> PipelineLoader.load(yaml),
                "load should throw PipelineException: " + why);
        String message = String.valueOf(error.getMessage());
        assertTrue(message.contains(mustMention),
                "the error message should mention \"" + mustMention + "\" so the engineer can find the problem, but was: " + message);
    }

    @Test
    @DisplayName("Spoke's pipeline loads: its name, nine stages in order, and every setting")
    void loadsSpokePipeline() {
        Pipeline pipeline = PipelineLoader.load(Pipelines.SPOKE_YAML);
        assertEquals("spoke-api", pipeline.name(), "the pipeline's name comes from name:");
        assertEquals(Pipelines.spoke().stages(), pipeline.stages(),
                "the nine stages should load in file order, with their types and settings "
                        + "(canary percent 10, approval 60 minutes, analysis 0.5 points and 20%)");
    }

    @Test
    @DisplayName("A whole number for maxErrorRateIncrease is read as a decimal, and other stages ignore extra keys")
    void numbersAndExtraKeys() {
        Pipeline pipeline = PipelineLoader.load(HEADER + """
                  - { name: canary, type: canary, percent: 1 }
                  - { name: analysis, type: analysis, maxErrorRateIncrease: 1, maxP99IncreasePercent: 0 }
                  - { name: promote, type: promote, note: "Friday is fine now" }
                """);
        assertEquals(List.of(Stage.canary("canary", 1), Stage.analysis("analysis", 1.0, 0), Stage.of("promote", StageType.PROMOTE)),
                pipeline.stages(), "maxErrorRateIncrease: 1 means 1.0 points; percent 1 and maxP99IncreasePercent 0 are allowed");
    }

    @Test
    @DisplayName("A file that is empty, not YAML, or has no name or no stages is refused")
    void refusesBrokenFiles() {
        assertRejected("", "empty", "an empty file is not a pipeline");
        assertRejected("name: spoke-api\nstages: [build, test", "YAML", "a list that is never closed is not valid YAML");
        assertRejected("- build\n- test\n", "name", "the top level must be a map with name and stages");
        assertRejected("stages:\n  - { name: build, type: build }\n", "name", "the pipeline needs a name");
        assertRejected("name: spoke-api\n", "stages", "the pipeline needs stages");
        assertRejected("name: spoke-api\nstages: []\n", "stages", "an empty list of stages is not a pipeline");
    }

    @Test
    @DisplayName("A stage needs a name and a known type, and the error names the stage")
    void refusesBadStages() {
        assertRejected(HEADER + "  - { type: build }\n", "stage 1", "the first stage has no name, so say \"stage 1\"");
        assertRejected(HEADER + "  - { name: build, type: build }\n  - { name: test }\n", "type",
                "the stage test has no type");
        assertRejected(HEADER + "  - { name: build, type: compile }\n", "compile", "compile is not a stage type");
        assertRejected(HEADER + "  - { name: build, type: compile }\n", "build", "the error should name the stage too");
        assertRejected(HEADER + "  - just-a-string\n", "stage 1", "a stage must be a map");
    }

    @Test
    @DisplayName("Two stages with the same name are refused")
    void refusesDuplicateNames() {
        assertRejected(HEADER + "  - { name: test, type: test }\n  - { name: test, type: smoke }\n", "test",
                "two stages are called test; the audit log and resume need unique names");
    }

    @Test
    @DisplayName("Canary percent must be a whole number from 1 to 50, and an approval needs expiresAfterMinutes of 1 or more")
    void refusesBadCanaryAndApproval() {
        for (String percent : List.of("0", "51", "ten", "10.5")) {
            assertRejected(HEADER + "  - { name: canary, type: canary, percent: " + percent + " }\n", "percent",
                    "percent " + percent + " is not a whole number from 1 to 50");
        }
        assertRejected(HEADER + "  - { name: canary, type: canary }\n", "percent", "a canary needs a percent");
        assertRejected(HEADER + "  - { name: approval, type: approval }\n", "expiresAfterMinutes",
                "an approval needs expiresAfterMinutes");
        assertRejected(HEADER + "  - { name: approval, type: approval, expiresAfterMinutes: 0 }\n", "expiresAfterMinutes",
                "an approval that expires after 0 minutes could never be approved");
    }

    @Test
    @DisplayName("Analysis needs both thresholds, and a canary stage before it")
    void refusesBadAnalysis() {
        String canary = "  - { name: canary, type: canary, percent: 10 }\n";
        assertRejected(HEADER + "  - { name: analysis, type: analysis, maxErrorRateIncrease: 0.5, maxP99IncreasePercent: 20 }\n",
                "canary", "there is no canary to analyse before this analysis");
        assertRejected(HEADER + canary + "  - { name: analysis, type: analysis, maxP99IncreasePercent: 20 }\n",
                "maxErrorRateIncrease", "the analysis has no maxErrorRateIncrease");
        assertRejected(HEADER + canary + "  - { name: analysis, type: analysis, maxErrorRateIncrease: -1, maxP99IncreasePercent: 20 }\n",
                "maxErrorRateIncrease", "a negative maxErrorRateIncrease makes no sense");
        assertRejected(HEADER + canary + "  - { name: analysis, type: analysis, maxErrorRateIncrease: 0.5 }\n",
                "maxP99IncreasePercent", "the analysis has no maxP99IncreasePercent");
        assertRejected(HEADER + canary + "  - { name: analysis, type: analysis, maxErrorRateIncrease: 0.5, maxP99IncreasePercent: 2.5 }\n",
                "maxP99IncreasePercent", "maxP99IncreasePercent must be a whole number");
    }
}

package jq.m11.dockerfiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Reads a Dockerfile and reports the findings described in the README, sorted by line, then by rule. */
public final class DockerfileLinter {

    /** Parts of a variable name that mark it as a secret, compared in capitals. */
    static final List<String> SECRET_WORDS =
            List.of("PASSWORD", "PASSWD", "SECRET", "TOKEN", "API_KEY", "APIKEY", "PRIVATE_KEY", "CREDENTIAL");

    /** One build stage: its FROM instruction, its name (or null) and the instructions after the FROM. */
    record Stage(Instruction from, String image, String name, List<Instruction> body) {
    }

    private DockerfileLinter() {
    }

    public static List<Finding> lint(String text) {
        List<Instruction> instructions = DockerfileParser.parse(text);
        List<Stage> stages = stages(instructions);
        List<Finding> findings = new ArrayList<>();

        checkBaseImages(stages, findings);
        if (!stages.isEmpty()) {
            Stage last = stages.get(stages.size() - 1);
            checkFinalStageImage(stages, last, findings);
            checkUser(stages, last, findings);
            checkShellForm(last, findings);
        }
        for (Instruction in : instructions) {
            checkAdd(in, findings);
            checkSecrets(in, findings);
            checkApt(in, findings);
        }
        for (Stage stage : stages) {
            checkDependencyOrder(stage, findings);
        }

        // TODO 4: sort the findings by line, then by rule (the order of the Rule enum).
        //         Comparator.comparingInt(...).thenComparing(...) does it in one line.
        return findings;
    }

    // ---------- stages ----------

    static List<Stage> stages(List<Instruction> instructions) {
        List<Stage> stages = new ArrayList<>();
        // TODO 5: split the instructions into stages. Each FROM starts a new stage; the instructions after it,
        //         up to the next FROM, are its body. Build each one with stage(from, body).
        //         Instructions before the first FROM (only ARG is allowed there) belong to no stage.
        return stages;
    }

    private static Stage stage(Instruction from, List<Instruction> body) {
        List<String> words = withoutFlags(tokens(from.args()));
        String image = words.isEmpty() ? "" : words.get(0);
        String name = null;
        if (words.size() >= 3 && words.get(1).equalsIgnoreCase("AS")) {
            name = words.get(2).toLowerCase(Locale.ROOT);
        }
        return new Stage(from, image, name, List.copyOf(body));
    }

    /** The stage before {@code index} whose name is {@code image}, or null when the image is not a stage name. */
    private static Stage earlierStage(List<Stage> stages, int index, String image) {
        for (int i = index - 1; i >= 0; i--) {
            if (image.equalsIgnoreCase(stages.get(i).name())) {
                return stages.get(i);
            }
        }
        return null;
    }

    /** The real image a stage starts from, following FROM <earlier stage> back to that stage's image. */
    private static String baseImage(List<Stage> stages, Stage stage) {
        int index = stages.indexOf(stage);
        Stage earlier = earlierStage(stages, index, stage.image());
        return earlier == null ? stage.image() : baseImage(stages, earlier);
    }

    // ---------- rules ----------

    private static void checkBaseImages(List<Stage> stages, List<Finding> findings) {
        // TODO 6: for each stage, skip the image "scratch" and images that are an earlier stage's name
        //         (earlierStage(stages, i, image) != null). Skip images pinned by a digest (they contain @).
        //         Otherwise, when tagOf(image) is null or "latest", add BASE_IMAGE_NOT_PINNED on the stage's FROM line.
    }

    private static void checkFinalStageImage(List<Stage> stages, Stage last, List<Finding> findings) {
        // TODO 7: take baseImage(stages, last) in lower case. Add JDK_IN_FINAL_STAGE on the final FROM line when it
        //         contains "jdk", or when the last part of repositoryOf(image) (after the last /) is maven or gradle.
    }

    private static void checkUser(List<Stage> stages, Stage last, List<Finding> findings) {
        // TODO 8: find the LAST USER in the final stage's body.
        //         None: add RUNS_AS_ROOT on the final FROM line, unless baseImage(stages, last) contains "nonroot".
        //         Otherwise take its user, the part before any ":". When it is "root" or "0",
        //         add RUNS_AS_ROOT on that USER's line.
    }

    private static void checkShellForm(Stage last, List<Finding> findings) {
        // TODO 9: add SHELL_FORM_COMMAND for each ENTRYPOINT or CMD in the final stage whose args do not start with "[".
    }

    private static void checkAdd(Instruction in, List<Finding> findings) {
        // TODO 10: for an ADD, look at sources(in.args()). If any source does not start with http:// or https://,
        //          add ONE ADD_FOR_LOCAL_FILE finding for this instruction.
    }

    private static void checkSecrets(Instruction in, List<Finding> findings) {
        // TODO 11: for ENV and ARG, collect the variable names from tokens(in.args()):
        //          - ENV whose first word has no "=" is the old form ENV NAME value: only the first word is a name.
        //          - otherwise every word is a name, cut at its first "=" (ARG NAME has no "=" at all).
        //          Add one SECRET_IN_ENV_OR_ARG per name whose capitals contain any of SECRET_WORDS.
    }

    private static void checkApt(Instruction in, List<Finding> findings) {
        // TODO 12: for a RUN whose args contain "apt-get install" or "apt install", but not
        //          "rm -rf /var/lib/apt/lists", add APT_LISTS_NOT_CLEANED.
    }

    private static void checkDependencyOrder(Stage stage, List<Finding> findings) {
        // TODO 13: only for a stage where some RUN contains "mvn", and no RUN contains "--mount=type=cache".
        //          Walk the body in order and stop at the first RUN that contains "dependency:go-offline" or
        //          "dependency:resolve". Before it, add SOURCES_BEFORE_DEPENDENCIES for every COPY without a
        //          --from flag (hasFlag) whose sources copy the code (copiesSources).
    }

    // ---------- helpers ----------

    /** Splits on whitespace, but keeps text inside double quotes together (the quotes stay in the word). */
    static List<String> tokens(String args) {
        List<String> words = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        boolean quoted = false;
        for (char c : args.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
            }
            if (Character.isWhitespace(c) && !quoted) {
                if (!word.isEmpty()) {
                    words.add(word.toString());
                    word.setLength(0);
                }
            } else {
                word.append(c);
            }
        }
        if (!word.isEmpty()) {
            words.add(word.toString());
        }
        return words;
    }

    /** The words that do not start with "--". */
    static List<String> withoutFlags(List<String> words) {
        return words.stream().filter(w -> !w.startsWith("--")).toList();
    }

    static boolean hasFlag(String args, String flag) {
        return tokens(args).stream().anyMatch(w -> w.equals(flag) || w.startsWith(flag + "="));
    }

    /**
     * The source paths of a COPY or ADD: every word except flags and the last word (the destination).
     * The JSON form, ["a", "b", "/dest/"], works too.
     */
    static List<String> sources(String args) {
        List<String> words;
        String trimmed = args.strip();
        if (trimmed.startsWith("[")) {
            words = new ArrayList<>();
            for (String part : trimmed.substring(1, trimmed.length() - 1).split(",")) {
                words.add(part.strip().replace("\"", ""));
            }
        } else {
            words = withoutFlags(tokens(trimmed));
        }
        return words.size() < 2 ? List.of() : words.subList(0, words.size() - 1);
    }

    private static boolean copiesSources(String args) {
        for (String source : sources(args)) {
            String path = source.startsWith("./") ? source.substring(2) : source;
            if (path.isEmpty() || path.equals(".") || path.equals("src") || path.startsWith("src/")) {
                return true;
            }
        }
        return false;
    }

    /** The tag of an image reference, or null when it has none. "localhost:5000/app" has no tag. */
    static String tagOf(String image) {
        int colon = image.lastIndexOf(':');
        int slash = image.lastIndexOf('/');
        return colon > slash ? image.substring(colon + 1) : null;
    }

    /** The image reference without its tag or digest. */
    static String repositoryOf(String image) {
        int at = image.indexOf('@');
        String withoutDigest = at < 0 ? image : image.substring(0, at);
        String tag = tagOf(withoutDigest);
        return tag == null ? withoutDigest : withoutDigest.substring(0, withoutDigest.length() - tag.length() - 1);
    }
}

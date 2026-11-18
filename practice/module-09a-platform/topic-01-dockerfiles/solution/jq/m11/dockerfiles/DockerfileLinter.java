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

        findings.sort(Comparator.comparingInt(Finding::line).thenComparing(Finding::rule));
        return findings;
    }

    // ---------- stages ----------

    static List<Stage> stages(List<Instruction> instructions) {
        List<Stage> stages = new ArrayList<>();
        Instruction from = null;
        List<Instruction> body = new ArrayList<>();
        for (Instruction in : instructions) {
            if (in.keyword().equals("FROM")) {
                if (from != null) {
                    stages.add(stage(from, body));
                }
                from = in;
                body = new ArrayList<>();
            } else if (from != null) {
                body.add(in);
            }
        }
        if (from != null) {
            stages.add(stage(from, body));
        }
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
        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            String image = stage.image();
            if (image.equalsIgnoreCase("scratch") || earlierStage(stages, i, image) != null) {
                continue;
            }
            if (image.contains("@")) {
                continue;                                    // pinned by digest
            }
            String tag = tagOf(image);
            if (tag == null || tag.equals("latest")) {
                findings.add(new Finding(Rule.BASE_IMAGE_NOT_PINNED, stage.from().line(),
                        "Pin the base image " + image + " to a version tag (or a digest), not "
                                + (tag == null ? "no tag" : "latest") + "."));
            }
        }
    }

    private static void checkFinalStageImage(List<Stage> stages, Stage last, List<Finding> findings) {
        String image = baseImage(stages, last).toLowerCase(Locale.ROOT);
        String repository = repositoryOf(image);
        String lastSegment = repository.substring(repository.lastIndexOf('/') + 1);
        if (image.contains("jdk") || lastSegment.equals("maven") || lastSegment.equals("gradle")) {
            findings.add(new Finding(Rule.JDK_IN_FINAL_STAGE, last.from().line(),
                    "The final stage runs on " + image + ". Build in an earlier stage and run on a JRE image."));
        }
    }

    private static void checkUser(List<Stage> stages, Stage last, List<Finding> findings) {
        Instruction lastUser = null;
        for (Instruction in : last.body()) {
            if (in.keyword().equals("USER")) {
                lastUser = in;
            }
        }
        if (lastUser == null) {
            if (!baseImage(stages, last).toLowerCase(Locale.ROOT).contains("nonroot")) {
                findings.add(new Finding(Rule.RUNS_AS_ROOT, last.from().line(),
                        "The final stage has no USER, so the app runs as root. Add USER with a numeric id, such as USER 10001."));
            }
            return;
        }
        String user = lastUser.args().strip();
        int colon = user.indexOf(':');
        if (colon >= 0) {
            user = user.substring(0, colon);
        }
        if (user.equals("root") || user.equals("0")) {
            findings.add(new Finding(Rule.RUNS_AS_ROOT, lastUser.line(),
                    "The last USER is root. Switch to a non-root user before the app starts."));
        }
    }

    private static void checkShellForm(Stage last, List<Finding> findings) {
        for (Instruction in : last.body()) {
            if ((in.keyword().equals("ENTRYPOINT") || in.keyword().equals("CMD")) && !in.args().startsWith("[")) {
                findings.add(new Finding(Rule.SHELL_FORM_COMMAND, in.line(),
                        in.keyword() + " is in shell form. Use exec form, " + in.keyword()
                                + " [\"java\", \"-jar\", \"app.jar\"], so java is PID 1 and gets SIGTERM."));
            }
        }
    }

    private static void checkAdd(Instruction in, List<Finding> findings) {
        if (!in.keyword().equals("ADD")) {
            return;
        }
        for (String source : sources(in.args())) {
            if (!source.startsWith("http://") && !source.startsWith("https://")) {
                findings.add(new Finding(Rule.ADD_FOR_LOCAL_FILE, in.line(),
                        "ADD copies the local path " + source + ". Use COPY: it does only that, with no surprises."));
                return;
            }
        }
    }

    private static void checkSecrets(Instruction in, List<Finding> findings) {
        if (!in.keyword().equals("ENV") && !in.keyword().equals("ARG")) {
            return;
        }
        List<String> words = tokens(in.args());
        if (words.isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<>();
        if (in.keyword().equals("ENV") && !words.get(0).contains("=")) {
            names.add(words.get(0));                         // legacy form: ENV NAME value
        } else {
            for (String word : words) {
                int eq = word.indexOf('=');
                names.add(eq < 0 ? word : word.substring(0, eq));
            }
        }
        for (String name : names) {
            String upper = name.toUpperCase(Locale.ROOT);
            if (SECRET_WORDS.stream().anyMatch(upper::contains)) {
                findings.add(new Finding(Rule.SECRET_IN_ENV_OR_ARG, in.line(),
                        in.keyword() + " " + name + " looks like a secret. Its value is saved in the image: use a BuildKit secret mount."));
            }
        }
    }

    private static void checkApt(Instruction in, List<Finding> findings) {
        if (!in.keyword().equals("RUN")) {
            return;
        }
        String args = in.args();
        boolean installs = args.contains("apt-get install") || args.contains("apt install");
        if (installs && !args.contains("rm -rf /var/lib/apt/lists")) {
            findings.add(new Finding(Rule.APT_LISTS_NOT_CLEANED, in.line(),
                    "Remove the package lists in the same RUN: && rm -rf /var/lib/apt/lists/*"));
        }
    }

    private static void checkDependencyOrder(Stage stage, List<Finding> findings) {
        boolean usesMaven = false;
        for (Instruction in : stage.body()) {
            if (in.keyword().equals("RUN") && in.args().contains("--mount=type=cache")) {
                return;                                      // a cache mount keeps downloads between builds
            }
            if (in.keyword().equals("RUN") && in.args().contains("mvn")) {
                usesMaven = true;
            }
        }
        if (!usesMaven) {
            return;
        }
        for (Instruction in : stage.body()) {
            if (in.keyword().equals("RUN")
                    && (in.args().contains("dependency:go-offline") || in.args().contains("dependency:resolve"))) {
                return;                                      // dependencies resolved: later COPYs are fine
            }
            if (in.keyword().equals("COPY") && !hasFlag(in.args(), "--from") && copiesSources(in.args())) {
                findings.add(new Finding(Rule.SOURCES_BEFORE_DEPENDENCIES, in.line(),
                        "This COPY brings in the source code before Maven downloads the dependencies. "
                                + "Copy pom.xml first and run mvn dependency:go-offline."));
            }
        }
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

## Hook

Picture a school play. Every actor practises alone at home, and every actor is word-perfect. Then, on opening night, they run the whole play together for the first time. Two people walk on at once. Nobody brought the props.

That is roughly what happened at Spoke last week. A pull request worked on one laptop and got merged. Then nobody else could even compile `main`. The Harbor kiosk update went out late while the team untangled it.

The cure is a dress rehearsal after every single change. A robot copies your code to a clean computer, builds it and runs every test. A few minutes later it answers: green or red.

## Concept

### Continuous integration

```callout jargon
**Continuous integration** (CI) means every push is built and tested automatically on a fresh machine. **Integration** means joining everyone's changes together. **Continuous** means you do it all the time, not once a month.
```

Four ideas make CI work:

- **Often.** You push small changes many times a day, so each one is easy to check.
- **Automatic.** Nobody has to remember to run the tests. Every push runs them.
- **Fresh machine.** The build starts on an empty computer with only what is in the repository.
- **Fast feedback.** You hear back in minutes, while the change is still fresh in your head.

A **green build** means every step passed. A **red build** means something failed. Red is a stop sign. The team fixes it, or reverts the change, before anything else gets merged.

```callout analogy
CI is a smoke alarm. It is cheap and always on. But it only helps if you act when it beeps, instead of taking out the battery.
```

### Pipelines, jobs, steps and runners

```callout jargon
A **pipeline** is the list of things CI does to your code, in order. A **job** (some tools say **stage**) is one big chunk of it, such as "build". A **step** is one command inside a job. A **runner** is the machine that runs a job. A **trigger** is the event that starts the pipeline, such as a push.
```

A typical Java pipeline looks like this:

```mermaid
flowchart LR
    Checkout --> Compile --> Tests["Unit tests"] --> Package --> Publish
```

- **Checkout**: copy the code from the repository onto the runner.
- **Compile**: turn `.java` files into `.class` files.
- **Unit tests**: run the fast tests.
- **Package**: build the jar.
- **Publish**: store the jar where people and servers can fetch it.

Notice the order: cheap to slow. A compile error shows up in seconds, so why wait for the tests first? The pipeline stops at the first red step. This is called **failing fast**. You hear about the cheapest problem first, and the runner stops wasting time.

Maven's lifecycle already follows this order. One `./mvnw verify` compiles, tests and packages, and stops at the first failure.

A runner is usually a brand-new **virtual machine**: a pretend computer that runs as a program on a bigger, real one. It exists for one run and is thrown away afterwards. Nothing from your laptop is there unless the repository brings it.

### Syntax: a GitHub Actions workflow file

**GitHub Actions** is the CI built into GitHub. You describe your pipeline in a **workflow file**. It is a YAML file inside the `.github/workflows/` folder of your repository, for example `.github/workflows/ci.yml`.

```callout jargon
**YAML** is a text format for settings. Indentation shows what belongs inside what, so spaces matter. An **action** is a ready-made step that someone published, like a library for pipelines.
```

```yaml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - run: ./mvnw -B verify
```

Line by line:

1. `name: CI` is the name GitHub shows in the Actions tab.
2. `on: [push, pull_request]` lists the triggers. The pipeline runs on every push and on every pull request.
3. `jobs:` starts the list of jobs. This file has one job, called `build`.
4. `runs-on: ubuntu-latest` asks GitHub for a fresh Linux runner.
5. `steps:` starts the steps. Each `-` is one step, run from top to bottom.
6. `uses: actions/checkout@v4` runs a ready-made action that copies your repository onto the runner. The `@v4` pins which version of the action you get.
7. `uses: actions/setup-java@v4` installs a JDK. The lines under `with:` are its settings.
8. `distribution: temurin` picks Eclipse Temurin, a free build of Java. `java-version: '21'` matches your project.
9. `cache: maven` saves downloaded libraries between runs, so later builds start faster.
10. `run: ./mvnw -B verify` runs a shell command. `-B` means batch mode: Maven asks no questions and prints a plain log.

The quotes around `'21'` keep the version as text. Without quotes, YAML would read a version like `21.10` as the number 21.1.

```callout tip
Gojo says: other CI systems, such as GitLab CI and Jenkins, use different file names and keys. The ideas are the same everywhere: triggers, jobs, steps, runners, green and red.
```

### Checks on a pull request

When you open a pull request, the workflow runs on it. You learned pull requests in [the Git workflow topic](/t/module-07-tooling/topic-03-git-workflow). GitHub shows the result near the bottom as a **check**. A yellow dot means running, a green tick means passed, and a red cross means failed. "Details" opens the log.

### Branch protection: keep main green

A check on its own is only advice. Someone can still press Merge on a red pull request. Branch protection turns the advice into a rule.

```callout jargon
A **branch protection rule** is a setting on a branch, such as `main`, that limits how it can change. A **required status check** is a check that must pass before GitHub lets anyone merge.
```

You find this in the repository's **Settings**, under **Branches**. Newer repositories may use **Rules**, where the same idea is called a ruleset. Turn on "Require status checks to pass before merging" and choose the `build` job. Many teams also require one review and block direct pushes to `main`.

The goal is a **green main**: the main branch always builds and passes its tests. Anyone can branch from it and start work without inheriting someone else's mess.

### Artifacts, delivery and deployment

An **artifact** is a file the build produces and keeps, such as the kiosk jar. A step can upload it, so people download the exact jar that passed the tests.

```yaml
      - uses: actions/upload-artifact@v4
        with:
          name: kiosk-jar
          path: target/*.jar
```

This step goes after the `./mvnw` step, at the same indentation.

CI is the first part of a longer road:

- **Continuous delivery**: every green build makes a release that is ready to ship. A person presses the button.
- **Continuous deployment**: every green build goes live by itself, with no button at all.

### Secrets stay out of the repository

Some pipelines need a password, such as a key for uploading to a server. That is a **secret**. Never write it in the workflow file or the code. Everyone who can read the repository could read it, forever, in the Git history.

Store it in **Settings**, under **Secrets and variables**, then **Actions**. The workflow reads it by name:

```yaml
      - run: ./upload.sh
        env:
          UPLOAD_KEY: ${{ secrets.UPLOAD_KEY }}
```

GitHub replaces the value with `***` if it ever appears in a log.

### Why it passes on your laptop and fails in CI

"Works on my machine" is the oldest excuse in software. The runner is a clean, strange computer. Anything your code quietly borrows from your laptop is missing there. When CI is red and your laptop is green, ask one question: what does my laptop have that a fresh machine does not?

#### 1. A hard-coded path

An **absolute path** starts at the top of the disk, like `/Users/intern/spoke/stations.txt`. Only your laptop has that folder. On the Linux runner, home folders live in `/home`, and yours is not there.

```java nocompile
// Only works on one laptop
List<String> names = Files.readAllLines(Path.of("/Users/intern/spoke/stations.txt"));
```

Fix it by letting the caller decide where the file is. Take a `Path` parameter:

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class StationFile {
    static List<String> load(Path file) throws IOException {
        return Files.readAllLines(file);
    }
}
```

The app passes the real location from a setting. A test passes a file it made itself, with `@TempDir`. You will see that test in the worked example.

```callout jargon
`@TempDir` is a JUnit annotation. JUnit makes a new, empty folder before the test, hands you its `Path`, and deletes it afterwards. Every machine has one, so the test runs anywhere.
```

#### 2. A file that was never committed

The runner only gets what is in the repository. A file you forgot to `git add` is missing. So is a file listed in `.gitignore`, the list of files Git deliberately skips.

```bash
git status                                   # shows changed files and files Git is not tracking
git check-ignore -v config/stations.txt      # says which .gitignore line hides the file
```

Commit the file if it is safe to share. If it is not, make a test copy in `src/test/resources`, or let the test write it into a `@TempDir`.

#### 3. The real clock

Code that calls `LocalTime.now()` asks the computer for the real time. A test of a time rule then passes or fails depending on when it runs. A test written at 6 am can pass on your laptop, then fail in an afternoon build.

```java nocompile
long discountCents() {
    LocalTime now = LocalTime.now();   // the real clock of whatever machine runs this
    ...
}
```

```callout jargon
A `java.time.Clock` is an object that answers "what time is it?". `Clock.systemDefaultZone()` asks the real computer. `Clock.fixed(...)` always answers the same moment, which is perfect for tests.
```

Fix it by **injecting** the clock: pass it in through the constructor. The app passes the real clock. A test passes a fixed one. Spoke's early-bird rule takes 20 cents off any ride that starts before 07:00.

```java run
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class Main {
    public static void main(String[] args) {
        Clock early = Clock.fixed(Instant.parse("2026-11-16T06:30:00Z"), ZoneOffset.UTC);
        Clock late = Clock.fixed(Instant.parse("2026-11-16T09:00:00Z"), ZoneOffset.UTC);
        System.out.println("06:30 discount: " + new EarlyBird(early).discountCents() + " cents");
        System.out.println("09:00 discount: " + new EarlyBird(late).discountCents() + " cents");
    }
}

class EarlyBird {
    private final Clock clock;

    EarlyBird(Clock clock) {
        this.clock = clock;
    }

    long discountCents() {
        LocalTime now = LocalTime.now(clock);
        return now.isBefore(LocalTime.of(7, 0)) ? 20 : 0;
    }
}
```

`LocalTime.now(clock)` reads the time from the clock you were given. The output is the same on every machine, at every hour.

#### 4. The time zone

GitHub's runners usually run in **UTC**, the world's reference time zone. Your laptop runs in your local zone. The same moment can be early morning in one zone and nearly noon in another.

```java run
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public class Main {
    public static void main(String[] args) {
        Instant moment = Instant.parse("2026-11-16T11:30:00Z");
        System.out.println("UTC:      " + LocalTime.ofInstant(moment, ZoneId.of("UTC")));
        System.out.println("New York: " + LocalTime.ofInstant(moment, ZoneId.of("America/New_York")));
    }
}
```

At 06:30 in New York a rider gets the early-bird discount. Code that uses the default zone gets a different answer on each machine. Name the zone you mean, or let the injected `Clock` carry it. Scheduled nightly builds on GitHub also use UTC times by default.

#### 5. Test order and shared static state

A `static` field is shared by every test in the run. If one test changes it, the next test sees the change. Test order then decides who passes. Your laptop may run only one test class, while CI runs them all.

```java run
public class Main {
    static int rides = 0;   // shared by every "test" below

    static String startsEmpty() { return rides == 0 ? "pass" : "FAIL"; }
    static String countsOneRide() { rides++; return rides == 1 ? "pass" : "FAIL"; }

    public static void main(String[] args) {
        System.out.println("order A: startsEmpty " + startsEmpty() + ", countsOneRide " + countsOneRide());
        rides = 0;
        System.out.println("order B: countsOneRide " + countsOneRide() + ", startsEmpty " + startsEmpty());
    }
}
```

Fix it by keeping state in instance fields that `@BeforeEach` sets up fresh. Avoid static fields that tests change.

#### 6. The network

A test that calls a real payment server or website depends on that server. On a runner the server may be slow, down or blocked. Replace it with a fake or a Mockito mock, as you did for the payment gateway.

### Flaky tests

```callout jargon
A **flaky test** passes sometimes and fails sometimes, on exactly the same code. The usual causes are the ones above: the clock, the time zone, test order, the network, and randomness.
```

Flaky tests are dangerous. After a few false alarms, people stop believing red. Then a real bug slips through. When a test is flaky, find the cause and fix it. Pressing "Re-run" until it goes green only hides the problem.

## Worked example

Spoke's first pipeline for the kiosk lives in `.github/workflows/ci.yml`:

```yaml
name: Kiosk CI

on:
  push:
    branches: [main]
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - run: ./mvnw -B verify
```

This version runs on pushes to `main` and on every pull request. Pushes to other branches get checked once you open their pull request.

The first pull request goes red. Here is the end of the log:

```text
Run ./mvnw -B verify
[INFO] Running dev.spoke.kiosk.StationFileTest
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0
[ERROR] readsEveryStation  Time elapsed: 0.004 s  <<< ERROR!
java.nio.file.NoSuchFileException: /Users/intern/spoke/stations.txt
[INFO] BUILD FAILURE
Error: Process completed with exit code 1.
```

Read it from the bottom up:

1. `exit code 1` means the command failed, so the step and the job are red.
2. `BUILD FAILURE` is Maven saying a phase failed.
3. `NoSuchFileException` names the missing file. It is a path on the intern's laptop.
4. `readsEveryStation` is the test that broke, in `StationFileTest`.

The test passed on the laptop because that file really exists there. The runner has no `/Users/intern` at all. Here is the fixed test. It makes its own file in a temporary folder:

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StationFileTest {

    @Test
    void readsEveryStation(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("stations.txt");
        Files.writeString(file, "Riverside\nOld Mill\nHarbor\nUptown\nDepot\n");

        List<String> names = StationFile.load(file);

        assertEquals(List.of("Riverside", "Old Mill", "Harbor", "Uptown", "Depot"), names,
                "every station in the file, in order");
    }
}

class StationFile {
    static List<String> load(Path file) throws IOException {
        return Files.readAllLines(file);
    }
}
```

- `@TempDir Path dir` asks JUnit for a fresh, empty folder.
- `dir.resolve("stations.txt")` builds a path inside that folder.
- `Files.writeString` writes the test data, so the test never needs a file from anyone's laptop.
- `StationFile.load(file)` takes the path as a parameter, so the test decides where the file is.

The intern pushes the fix to the same branch. The pull request runs again and goes green. Now the Merge button works.

## Try it

Here is the early-bird rule with one fixed clock. Before you press Run, write down your prediction. Then run it and check.

```java run
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class Main {
    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-11-16T06:59:00Z"), ZoneOffset.UTC);
        LocalTime now = LocalTime.now(clock);
        long discount = now.isBefore(LocalTime.of(7, 0)) ? 20 : 0;
        System.out.println(now + " discount: " + discount + " cents");
    }
}
```

Now change `06:59` to `07:00`, then `00:00`, then `12:30`. Predict each answer before you run it. The output never changes with the real time, which is exactly why a test can trust it.

## Recap

- Continuous integration builds and tests every push on a fresh machine, often, with fast feedback. Red means stop and fix.
- A pipeline is jobs made of steps, started by a trigger and run on a runner. Order the steps cheap to slow so they fail fast.
- A GitHub Actions workflow lives in `.github/workflows/`. Branch protection with required status checks keeps `main` green.
- "Works on my machine" means your laptop helped: a local path, an uncommitted file, the clock, the time zone, test order or the network.
- Pass in what changes: a `Path` (with `@TempDir` in tests) and a `Clock` (with `Clock.fixed` in tests). Fix flaky tests instead of re-running them.

## Common mistakes

**1. Pushing on red with "fix later".**

```bash
git commit -m "wip, fix tests later"
git push
```

```text
CI / build (push): failing
```

What you see: `main` stays red. The next person's pull request also fails, and nobody can tell which failure is theirs. Fix: when `main` goes red, fixing it comes first. Push the fix quickly, or revert your change with `git revert`.

**2. Re-running a flaky test until it goes green.**

```text
06:41 Attempt 1: EarlyBirdTest > noDiscountAfterSeven FAILED
06:55 Attempt 2: EarlyBirdTest > noDiscountAfterSeven FAILED
07:04 Attempt 3: passed
```

What you see: a green tick today, and the same failure in tomorrow's early build. The re-runs only waited for the real clock to pass 07:00. The team slowly learns to ignore red. Fix: find what the test depends on, here the real clock. Inject a `Clock` and use `Clock.fixed` in the test.

**3. Writing a secret into the workflow file.**

```yaml
      - run: ./upload.sh
        env:
          UPLOAD_KEY: spoke-upload-7f3a9c
```

What you see: nothing goes wrong at first. But anyone who can read the repository can now read the key, even after you delete the line, because Git keeps history. Fix: store it under Settings, Secrets and variables, Actions, and use `${{ secrets.UPLOAD_KEY }}`. Also ask for the leaked key to be replaced.

**4. Depending on a file that only exists on your laptop.**

```text
# .gitignore
config/stations.txt
```

```text
java.nio.file.NoSuchFileException: config/stations.txt
```

What you see: green on your laptop, red in CI. The file is listed in `.gitignore`, so it never reached the repository. Fix: commit a safe copy, put test data in `src/test/resources`, or have the test write the file into a `@TempDir`.

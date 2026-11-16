## Hook

Maya keeps a text file called `build-steps.txt`. It holds twelve `javac` and `jar` commands, typed in the right order, with paths that only exist on her laptop.

A new teammate copies the commands. Step four fails because their folders are different. Another teammate skips the tests to save time and ships an old jar to the Harbor kiosk.

The problem is not the code. The problem is that building it is a recipe in someone's head. You want the recipe written down once, in a file the computer follows the same way on every machine. That is what a build tool does.

## Concept

```callout jargon
A **build** turns your source code into something you can ship: compiled classes, test results and a jar file. A **build tool** runs all those steps for you, in the same order, from one command.
```

**Maven** is the most common build tool for Java. You describe your project in one file. Maven reads it and does the compiling, testing and packaging.

A build tool gives you three things:

- **One command.** `./mvnw package` replaces the whole text file of `javac` commands.
- **The same result everywhere.** Your laptop, a new teammate's laptop and a build server all follow the same file.
- **Jars fetched for you.** You name a library and Maven downloads it. Nobody emails jar files around.

### Syntax: the POM

```callout jargon
The **POM** (Project Object Model) is a file called `pom.xml` in the top folder of the project. It describes the project: its name, its Java version, the libraries it needs and how to build it.
```

The POM is written in **XML**, a text format where every value sits between an opening tag like `<version>` and a closing tag like `</version>`. Here is the smallest useful POM.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>dev.spoke</groupId>
  <artifactId>kiosk</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</project>
```

`<modelVersion>` tells Maven which POM format you use. Write `4.0.0`, which works with Maven 3 and Maven 4. The next three lines are the project's name.

You already have a real POM. Open `practice/pom.xml` in your IDE. It is the file that lets your IDE see all your practice folders and JUnit.

### Syntax: coordinates

```callout jargon
**Coordinates** are the three values that name a project or a library anywhere in the world: `groupId:artifactId:version`.
```

| Part | What it means | Example |
|---|---|---|
| `groupId` | The team or company, written like a reversed web address | `dev.spoke` |
| `artifactId` | The name of this one project | `kiosk` |
| `version` | Which release of it | `1.0.0` |

Written on one line, the kiosk is `dev.spoke:kiosk:1.0.0`. JUnit is `org.junit.jupiter:junit-jupiter:6.1.3`. The same three parts find any library.

A version that ends in `-SNAPSHOT` means "work in progress". A **SNAPSHOT** can change from day to day while you keep coding. A **release** version, like `1.0.0`, never changes once it is published. If you change the code, you publish `1.0.1`.

```callout tip
Ship releases, not snapshots. "Which 1.0.0-SNAPSHOT is on the Harbor kiosk?" has no good answer. "1.0.1 is on the Harbor kiosk" does.
```

### Syntax: the standard layout

Maven expects every project to use the same folders. You never tell it where your code is, because it already knows.

```text
kiosk/
  pom.xml
  src/
    main/
      java/          your code, the classes you ship
      resources/     non-code files you ship, like kiosk.properties
    test/
      java/          your tests, never shipped
      resources/     files only the tests read
  target/            everything Maven builds; safe to delete
```

```callout jargon
A **convention** is a rule everyone agrees to follow, so nobody has to write it down again. Maven calls this "convention over configuration": follow the layout and you need almost no settings.
```

`target/` is output, not source. Maven creates it, fills it and can delete it at any time. Never edit files in `target/` by hand, and never commit it to Git.

### Syntax: running a phase

```callout jargon
A **lifecycle** is a fixed list of steps a build goes through. Each step is called a **phase**.
```

Maven's main lifecycle has these phases, always in this order:

```mermaid
flowchart LR
    validate --> compile --> test --> package --> verify --> install --> deploy
```

| Phase | What happens |
|---|---|
| `validate` | Checks the POM is complete and correct |
| `compile` | Compiles `src/main/java` into `target/classes` |
| `test` | Compiles and runs the tests in `src/test/java` |
| `package` | Bundles the compiled classes into a jar in `target/` |
| `verify` | Runs extra checks on the package, such as integration tests |
| `install` | Copies the jar into your local repository, `~/.m2/repository` |
| `deploy` | Uploads the jar to a shared remote repository for other teams |

You run a phase by naming it:

```bash
./mvnw package
```

Here is the most important rule. **Running one phase runs every phase before it.** `./mvnw package` runs validate, compile and test first, then package. Nobody can forget the tests, because they run on every package.

If a test fails, the build stops there. No jar is made. A broken kiosk cannot ship by accident.

`clean` is a separate lifecycle with one job: it deletes `target/`. You can put it in front of another phase.

```bash
./mvnw clean package
```

This throws away every old file, then builds everything fresh. Use it whenever you are about to ship.

### Syntax: the wrapper

```callout jargon
The **Maven Wrapper** is a small script, `mvnw` (and `mvnw.cmd` on Windows), that downloads one exact Maven version and runs it. The version is written in `.mvn/wrapper/maven-wrapper.properties`.
```

```bash
./mvnw -v
```

`-v` prints which Maven and which Java the wrapper uses. Teams commit `mvnw` and the `.mvn` folder to Git. A new teammate clones the project and runs `./mvnw package`. They need Java, but they never install Maven themselves, and they get the same Maven version as everyone else.

Look at the JavaQuest folder. It has `mvnw`, `mvnw.cmd` and `.mvn/wrapper/maven-wrapper.properties`. That file pins Maven 3.9.11.

```callout tip
`-q` (quiet) hides the progress lines and shows only warnings and errors. `./mvnw -q test` prints almost nothing when every test passes.
```

### Syntax: a dependency

```callout jargon
A **dependency** is a library your project uses, named by its coordinates. Maven downloads it and puts it on the **classpath**, the list of places Java searches for classes.
```

```xml
<dependencies>
  <dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.6.6</version>
  </dependency>
</dependencies>
```

The first time you build, Maven downloads the jar from **Maven Central**, the big public library store at `repo.maven.apache.org`. It saves a copy in your **local repository**, the folder `~/.m2/repository` on your laptop. The next build uses the saved copy and needs no internet.

```callout analogy
Maven Central is the public library in town. `~/.m2/repository` is the shelf in your bedroom. Maven borrows each book once and keeps it on your shelf.
```

Libraries need libraries too. Logback needs SLF4J, for example. Maven reads Logback's own POM and fetches SLF4J as well. A library you get this way is a **transitive dependency**: you did not list it, but something you listed needs it.

To see every jar and where it came from, ask for the dependency tree:

```bash
./mvnw dependency:tree
```

```text
[INFO] dev.javaquest:practice:jar:1.0
[INFO] +- org.junit.platform:junit-platform-console-standalone:jar:6.1.3:compile
[INFO] +- org.mockito:mockito-junit-jupiter:jar:5.24.0:test
[INFO] |  \- org.mockito:mockito-core:jar:5.24.0:test
[INFO] |     +- net.bytebuddy:byte-buddy:jar:1.17.7:test
[INFO] |     +- net.bytebuddy:byte-buddy-agent:jar:1.17.7:test
[INFO] |     \- org.objenesis:objenesis:jar:3.3:test
[INFO] \- ch.qos.logback:logback-classic:jar:1.6.6:compile
[INFO]    +- ch.qos.logback:logback-core:jar:1.6.6:compile
[INFO]    \- org.slf4j:slf4j-api:jar:2.0.19:compile
```

That is the real tree of your practice folder. The POM lists three libraries. The indented lines under them are transitive. You never typed `slf4j-api`, yet it is there.

### Syntax: scope

```callout jargon
A **scope** says when a dependency is on the classpath: while compiling your code, while running the tests, or while the program runs.
```

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>6.1.3</version>
  <scope>test</scope>
</dependency>
```

| Scope | Compiling `src/main/java` | Compiling and running tests | When the program runs | Typical example |
|---|---|---|---|---|
| `compile` | yes | yes | yes | Logback, your logging library |
| `test` | no | yes | no | JUnit, Mockito |
| `provided` | yes | yes | no, the server already has it | a jar the web server already has |
| `runtime` | no | yes | yes | a database driver your code never names |

`compile` is the default. If you write no `<scope>`, you get `compile`.

`provided` is for a jar that something else supplies when the program runs. A web server, for example, comes with its own jar for handling web requests. Your code compiles against it, but you do not ship a second copy.

`test` keeps testing tools out of the kiosk. Your shipped jar never needs JUnit, so the tests can use it but the main code cannot.

```callout tip
The practice POM puts the JUnit jar in `compile` scope on purpose. In module 6 you wrote check classes in `src`, and they use `Assertions`. A normal project keeps JUnit in `test` scope.
```

### Syntax: plugins and goals

```callout jargon
A **plugin** is a jar of Maven tools. Each tool inside it is a **goal**. You write a goal as `plugin:goal`, like `dependency:tree`.
```

Maven itself knows only the lifecycle. Plugins do the real work. Each phase has plugin goals **bound** to it, which means they run when the build reaches that phase.

| Phase | Goal that runs for a jar project |
|---|---|
| `compile` | `compiler:compile` |
| `test` | `surefire:test`, from the Surefire plugin, which runs your tests |
| `package` | `jar:jar`, from the jar plugin, which builds the jar |
| `install` | `install:install` |

You can also run a goal on its own. `./mvnw dependency:tree` runs one goal and no phases at all.

To choose a plugin's version or change its settings, add it under `<build>`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.5.6</version>
    </plugin>
  </plugins>
</build>
```

Plugins have coordinates too. They download from Maven Central, just like libraries.

### Syntax: properties

`<properties>` holds named settings that plugins read. Two of them go in almost every POM.

```xml
<properties>
  <maven.compiler.release>21</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

`maven.compiler.release` tells the compiler plugin to build for Java 21. `project.build.sourceEncoding` says your source files are saved as UTF-8, so letters like é read the same on every machine.

You can also make your own property and use it with `${...}`. The practice POM does this with `<junit.version>`.

## Worked example

You turn Maya's text file into a Maven project. The kiosk starts small: one class that prices a ride, and one test.

The folders:

```text
kiosk/
  pom.xml
  mvnw
  mvnw.cmd
  .mvn/wrapper/maven-wrapper.properties
  src/main/java/dev/spoke/kiosk/FareCalculator.java
  src/main/resources/kiosk.properties
  src/test/java/dev/spoke/kiosk/FareCalculatorTest.java
```

The whole `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>dev.spoke</groupId>
  <artifactId>kiosk</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>jar</packaging>

  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>6.1.3</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.5.6</version>
      </plugin>
    </plugins>
  </build>
</project>
```

Line by line:

1. The coordinates name the project `dev.spoke:kiosk:1.0.0-SNAPSHOT`.
2. `<packaging>jar</packaging>` says the result is a jar. `jar` is the default, so you could leave it out.
3. The properties pick Java 21 and UTF-8.
4. JUnit is the only dependency, in `test` scope.
5. The Surefire plugin is pinned to one version, so every machine runs tests the same way.

The class, in `src/main/java/dev/spoke/kiosk/FareCalculator.java`. The `package` line matches the folders under `src/main/java`.

```java
package dev.spoke.kiosk;

public class FareCalculator {
    static final long UNLOCK_CENTS = 100;
    static final long PER_MINUTE_CENTS = 15;

    public long fareCents(int minutes) {
        return UNLOCK_CENTS + PER_MINUTE_CENTS * minutes;
    }
}
```

The test, in `src/test/java/dev/spoke/kiosk/FareCalculatorTest.java`. It sits in the same package, under the test folder.

```java nocompile
package dev.spoke.kiosk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FareCalculatorTest {
    private final FareCalculator calc = new FareCalculator();

    @Test
    @DisplayName("A 12 minute ride costs 280 cents")
    void twelveMinutes() {
        assertEquals(280, calc.fareCents(12), "100 to unlock plus 12 x 15");
    }

    @Test
    @DisplayName("A 0 minute ride costs only the unlock")
    void zeroMinutes() {
        assertEquals(100, calc.fareCents(0), "only the 100 cent unlock");
    }
}
```

Now one command builds it. Here is what `./mvnw package` prints, with the download lines removed:

```text
[INFO] --------------------------< dev.spoke:kiosk >---------------------------
[INFO] Building kiosk 1.0.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] --- resources:3.3.1:resources (default-resources) @ kiosk ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] --- compiler:3.13.0:compile (default-compile) @ kiosk ---
[INFO] Compiling 1 source file with javac [debug release 21] to target/classes
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ kiosk ---
[INFO] Compiling 1 source file with javac [debug release 21] to target/test-classes
[INFO] --- surefire:3.5.6:test (default-test) @ kiosk ---
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running dev.spoke.kiosk.FareCalculatorTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- jar:3.4.1:jar (default-jar) @ kiosk ---
[INFO] Building jar: kiosk/target/kiosk-1.0.0-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

Read it top to bottom. Each `---` line names the plugin goal that is running.

1. `resources:resources` copies `kiosk.properties` into `target/classes`.
2. `compiler:compile` compiles `FareCalculator` with `release 21`, from the property.
3. `compiler:testCompile` compiles the test into `target/test-classes`.
4. `surefire:test` runs both tests. Both pass, so the build carries on.
5. `jar:jar` builds the jar. Its name is `artifactId-version.jar`.

And `target/` now holds:

```text
target/
  classes/dev/spoke/kiosk/FareCalculator.class
  classes/kiosk.properties
  test-classes/dev/spoke/kiosk/FareCalculatorTest.class
  surefire-reports/
  kiosk-1.0.0-SNAPSHOT.jar
```

The jar holds what is in `classes/`, plus a few lines about the build. The test class and JUnit stay out of it. That is the `test` scope and the standard layout working together.

## Try it

Your practice folder is a Maven project. Open a terminal in the JavaQuest folder. `-f` tells Maven which POM to use.

1. See every jar your practice code uses:

```bash
./mvnw -f practice/pom.xml dependency:tree
```

2. Compile everything and run every check in one go:

```bash
./mvnw -q -f practice/pom.xml test
```

Checks for tickets you have not finished yet will fail. That is expected, and it shows the build stops at the `test` phase.

3. Break a scope on purpose. In `practice/pom.xml`, add `<scope>test</scope>` to the `logback-classic` dependency. Run `./mvnw -q -f practice/pom.xml compile`. The promo engine you built for FEAT-06 logs with SLF4J, so it no longer compiles: `package org.slf4j does not exist`. Remove the line again and the error goes away.

## Recap

- Maven builds, tests and packages a project from one file, `pom.xml`, with one command.
- Coordinates `groupId:artifactId:version` name every project and library. A `-SNAPSHOT` version can still change; a release never does.
- Code goes in `src/main/java`, tests in `src/test/java`, and Maven writes everything to `target/`.
- Running a phase runs every phase before it, so `./mvnw package` always compiles and tests first. `clean` deletes `target/`.
- Dependencies come from Maven Central into `~/.m2/repository`, with their own dependencies too. Scopes decide when each jar is on the classpath. Plugin goals, like `surefire:test` and `jar:jar`, do the real work.

## Common mistakes

**1. Using a test library in your main code.**

JUnit is in `test` scope. A class in `src/main/java` uses its `Assertions`.

```java
package dev.spoke.kiosk;

import org.junit.jupiter.api.Assertions;

public class FareCheck {
    public static void check(long cents) {
        Assertions.assertTrue(cents >= 100, "a fare is at least the unlock");
    }
}
```

What you see when you run `./mvnw package`:

```text
[ERROR] COMPILATION ERROR :
[ERROR] .../src/main/java/dev/spoke/kiosk/FareCheck.java:[3,29] package org.junit.jupiter.api does not exist
```

Fix: main code must not need test tools. Use a plain `if` and throw an exception instead. Put real JUnit checks in `src/test/java`. Change the scope to `compile` only if the shipped code truly needs that library.

**2. A test in the wrong folder, or with the wrong name.**

Surefire only runs test classes whose names end in `Test`, `Tests` or `TestCase`, or start with `Test`. It only looks in `src/test/java`.

```text
src/test/java/dev/spoke/kiosk/FareChecks.java       name does not end in Test
src/main/java/dev/spoke/kiosk/FareCalculatorTest.java   test in the main folder
```

What you see: with `FareChecks.java`, the `surefire:test` line appears, then `BUILD SUCCESS`, with no `T E S T S` block at all. Nothing ran, yet the build looks green. With the test in `src/main/java`, you get `package org.junit.jupiter.api does not exist`, as in mistake 1.

Fix: put tests under `src/test/java`, in the same package as the class they test. Name them after that class plus `Test`, like `FareCalculatorTest`. Check the output says `Tests run:` with the number you expect.

**3. Building with a different Maven, or patching `target/` by hand.**

```bash
mvn package
cp FareCalculator.class target/classes/dev/spoke/kiosk/
```

What you see: on a fresh laptop, `mvn: command not found`. On another, an older Maven that builds differently or stops because a plugin needs a newer Maven. A hand-copied class looks fine until the next `./mvnw clean` deletes it. Then a different jar ships.

Fix: always run `./mvnw`, so everyone uses the Maven version pinned in `.mvn/wrapper`. Never edit or copy files in `target/`. Change the source, then run `./mvnw clean package` to rebuild the jar from scratch.

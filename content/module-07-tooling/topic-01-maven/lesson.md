## Hook

Tsunade keeps a text file called `build-steps.txt`. It holds twelve `javac` and `jar` commands, typed in the right order, with paths that only exist on her laptop.

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

### Version numbers: semantic versioning

Most libraries number their releases with three parts: **MAJOR.MINOR.PATCH**. Take `slf4j-api` 2.0.19. Its major is 2, its minor is 0, and its patch is 19.

```callout jargon
**Semantic versioning** (semver) is a promise about what a version number tells you. Which part went up says what kind of change you are getting.
```

| Part that goes up | What changed | What it means for you |
|---|---|---|
| PATCH, 2.0.19 to 2.0.20 | A bug fix. Nothing new, nothing removed. | Safe. Take it soon. |
| MINOR, 2.0.20 to 2.1.0 | New features. Old code still works. | Should be safe. |
| MAJOR, 2.1.0 to 3.0.0 | Something old code relied on changed or was removed. | Read the release notes. Your code may need changes. |

A few more rules:

- A version that starts with `0.`, like `0.4.2`, is still young. Anything may change in any release.
- A `-SNAPSHOT` version is not a release at all. `1.1.0-SNAPSHOT` means "work in progress, on the way to 1.1.0".
- Semver is a promise people make, not a law the computer checks. Run your tests after every upgrade, even a patch.

Bump your own versions the same way. If kiosk 1.4.0 renames a public method, the next release is 2.0.0, not 1.5.0.

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

Here is the top of your practice folder's real tree. A `...` marks lines left out.

```text
[INFO] dev.javaquest:practice:jar:1.0
[INFO] +- org.junit.platform:junit-platform-console-standalone:jar:6.1.3:compile
[INFO] +- org.mockito:mockito-junit-jupiter:jar:5.24.0:test
[INFO] |  \- org.mockito:mockito-core:jar:5.24.0:test
...
[INFO] +- ch.qos.logback:logback-classic:jar:1.6.6:compile
[INFO] |  +- ch.qos.logback:logback-core:jar:1.6.6:compile
[INFO] |  \- org.slf4j:slf4j-api:jar:2.0.19:compile
[INFO] +- org.springframework.boot:spring-boot-starter-webmvc:jar:4.1.2:compile
...
```

Each line is `groupId:artifactId:jar:version:scope`. A line pushed further right is transitive: the line above it brought it in. You never typed `slf4j-api`, yet it is there.

The practice POM lists twelve libraries. The whole tree runs past 100 lines, mostly Spring jars for module 8.

### When two versions collide: dependency conflicts

Two libraries can need the same third library, at different versions. In your practice tree, Logback asks for `slf4j-api` 2.0.19. Spring's logging jars, deeper down, ask for 2.0.17 and 2.0.20.

```callout jargon
A **dependency conflict** is one library showing up in your tree at two or more versions. Maven puts only one version on the classpath, so it has to pick.
```

Maven's rule is **nearest wins**. It counts the steps from your POM down to each copy. The copy with the fewest steps wins. On a tie, the one listed first in the POM wins.

Add `-Dverbose` to see the losers too. `-Dincludes` keeps only the library you ask about.

```bash
./mvnw -f practice/pom.xml dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

```text
[INFO] dev.javaquest:practice:jar:1.0
[INFO] +- ch.qos.logback:logback-classic:jar:1.6.6:compile
[INFO] |  \- org.slf4j:slf4j-api:jar:2.0.19:compile
[INFO] +- org.springframework.boot:spring-boot-starter-webmvc:jar:4.1.2:compile
[INFO] |  \- org.springframework.boot:spring-boot-starter:jar:4.1.2:compile (scope not updated to compile)
[INFO] |     \- org.springframework.boot:spring-boot-starter-logging:jar:4.1.2:compile
[INFO] |        +- org.apache.logging.log4j:log4j-to-slf4j:jar:2.25.5:compile
[INFO] |        |  \- (org.slf4j:slf4j-api:jar:2.0.17:compile - omitted for conflict with 2.0.19)
[INFO] |        \- org.slf4j:jul-to-slf4j:jar:2.0.20:compile
[INFO] |           \- (org.slf4j:slf4j-api:jar:2.0.20:compile - omitted for conflict with 2.0.19)
...
```

Lines in brackets are the losers. You can ignore the `scope not updated` note: Maven is only explaining a scope choice.

```animate
title: Nearest wins
kind: scene
nodes:
  - { id: pom, label: Your POM, emoji: "📄", row: 0, col: 1 }
  - { id: logback, label: logback-classic 1.6.6, emoji: "📦", row: 1, col: 0 }
  - { id: spring, label: Spring starters, emoji: "📦", value: 4 jars deep, row: 1, col: 2 }
  - { id: slfa, label: slf4j-api 2.0.19, emoji: "🧩", row: 2, col: 0 }
  - { id: slfb, label: slf4j-api 2.0.20, emoji: "🧩", row: 2, col: 2 }
steps:
  - say: Maven starts at your POM and walks down the tree, counting steps.
    focus: [pom]
  - say: One step down is Logback. One more step, and Logback asks for slf4j-api 2.0.19.
    send: [{ from: pom, to: logback, label: "1" }, { from: logback, to: slfa, label: "2" }]
    set: { slfa: 2 steps }
  - say: Down the Spring side, a jar four steps deep asks for slf4j-api 2.0.20.
    send: [{ from: pom, to: spring, label: "1 to 4" }, { from: spring, to: slfb, label: "5" }]
    set: { slfb: 5 steps }
  - say: Same library, two versions. Two steps beats five, so 2.0.19 wins and 2.0.20 is left out.
    set: { slfa: on the classpath, slfb: omitted }
    focus: [slfa]
  - say: The newer version lost. Nearest wins is not newest wins. Spring code now runs against 2.0.19.
    focus: [slfb]
```

2.0.19 and 2.0.20 differ only in the patch, so nothing breaks here. A bigger gap, like a different major version, is where trouble starts.

### What a conflict looks like at run time

The compiler never notices a conflict. Each library was compiled long ago, against the version it wanted. The trouble shows up only when the program runs.

Say a route library was compiled against a maps library, `geo` 2.0, which has `walkMinutes`. Nearest wins picked `geo` 1.0, which does not. The build is green. Then a rider asks for directions:

```text
Exception in thread "main" java.lang.NoSuchMethodError: 'int geo.Maps.walkMinutes(java.lang.String, java.lang.String)'
	at route.Planner.plan(Planner.java:8)
	at Kiosk.main(Kiosk.java:3)
```

Two errors point at a conflict:

- `NoSuchMethodError`: the class is there, but not the method the caller was compiled against.
- `NoClassDefFoundError`: the whole class is missing. The jar was left out, or the chosen version moved or deleted that class.

One look-alike is not a conflict. `NoClassDefFoundError: Could not initialize class FareTable` means the class was found, but its `static` setup threw an exception earlier. Scroll up the log to the first `ExceptionInInitializerError`.

```callout tip
Gojo says: the package name in the error tells you which library to check. Here it is `geo`. Run `dependency:tree -Dverbose -Dincludes=` with that group, and see which version won and who wanted the other one.
```

### Syntax: choosing the version yourself

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>2.0.20</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

`<dependencyManagement>` adds no library at all. It says: "wherever this library shows up in the tree, use this version." It works for transitive dependencies too. So it settles a conflict without moving anything.

Listing a library directly in `<dependencies>` also settles it, because one step is as near as it gets. The practice POM does this. Spring wanted Logback 1.5.38, but the POM lists 1.6.6 itself, so 1.6.6 wins.

Pick the version that works for both sides. Usually that is the newer one, if it only changes the minor or the patch.

### Syntax: exclusions

```xml
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-junit-jupiter</artifactId>
  <version>5.24.0</version>
  <scope>test</scope>
  <exclusions>
    <exclusion>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>*</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

An **exclusion** cuts one branch off the tree. Here Mockito would bring its own JUnit jars. The practice POM already has JUnit in its standalone jar, so it excludes Mockito's copies. It cuts `org.junit.platform` the same way. `*` means every artifact in that group.

Exclude a jar only when something else supplies it. Otherwise you get a `NoClassDefFoundError` at run time.

### Syntax: a BOM

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>4.1.2</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

```callout jargon
A **BOM** (bill of materials) is a POM that holds only a long list of versions, all tested together. `<type>pom</type>` and `<scope>import</scope>` copy that list into your `<dependencyManagement>`.
```

Now you leave `<version>` off the libraries the BOM knows:

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
</dependency>
```

Here is the real tree of a small kiosk project with just that BOM and that one dependency:

```text
[INFO] dev.spoke:kiosk:jar:1.0.0
[INFO] \- ch.qos.logback:logback-classic:jar:1.5.38:compile
[INFO]    +- ch.qos.logback:logback-core:jar:1.5.38:compile
[INFO]    \- org.slf4j:slf4j-api:jar:2.0.20:compile
```

The BOM chose Logback 1.5.38, and it lined up `slf4j-api` at 2.0.20 too. Upgrading Spring Boot later means changing one version, and every library moves together. Spring Boot projects in module 8 lean on this BOM.


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

### A jar that runs on its own

The jar that `./mvnw package` makes is for other Java code to use. It cannot start by itself. Here is what happens with a small kiosk that logs with SLF4J:

```bash
java -jar target/kiosk-1.0.0.jar
```

```text
no main manifest attribute, in target/kiosk-1.0.0.jar
```

The jar's **manifest**, a small text file inside it, does not name a main class. So name the class yourself:

```bash
java -cp target/kiosk-1.0.0.jar dev.spoke.kiosk.Kiosk
```

```text
Exception in thread "main" java.lang.NoClassDefFoundError: org/slf4j/LoggerFactory
	at dev.spoke.kiosk.Kiosk.<clinit>(Kiosk.java:7)
Caused by: java.lang.ClassNotFoundException: org.slf4j.LoggerFactory
```

A plain `package` jar holds only your own classes. SLF4J and Logback stayed in `~/.m2/repository`. On a kiosk computer they are nowhere.

In FEAT-07 you write the manifest yourself. That kiosk uses no libraries, so one plain jar with `Main-Class` is enough. Most real apps need more.

### Syntax: a fat jar with the Shade plugin

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.6.0</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals><goal>shade</goal></goals>
      <configuration>
        <transformers>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
            <mainClass>dev.spoke.kiosk.Kiosk</mainClass>
          </transformer>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
        </transformers>
        <filters>
          <filter>
            <artifact>*:*</artifact>
            <excludes>
              <exclude>META-INF/*.SF</exclude>
              <exclude>META-INF/*.DSA</exclude>
              <exclude>META-INF/*.RSA</exclude>
            </excludes>
          </filter>
        </filters>
      </configuration>
    </execution>
  </executions>
</plugin>
```

```callout jargon
A **fat jar** (or uber jar) holds your classes and the classes of every runtime dependency, in one file. `java -jar` can start it on any machine with the right Java.
```

This goes under `<build><plugins>`. In the `package` phase, the Shade plugin unpacks every runtime jar into yours and writes `Main-Class` into the manifest. Your old thin jar is kept as `original-kiosk-1.0.0.jar`.

Two more parts matter once real libraries go in:

- **`ServicesResourceTransformer`** merges the files in `META-INF/services`. Libraries use them to find each other. SLF4J 2 finds Logback this way, so without the merge your logs quietly vanish.
- **The `<filters>`** drop signature files. Some jars are signed, and a copied signature no longer matches the fat jar, so Java refuses to load it.

Other ways to get there:

- **Spring Boot's plugin**, `spring-boot-maven-plugin`, builds a runnable jar with the library jars nested inside. You will use it in module 8.
- **A folder of jars plus a start script.** Gradle's `application` plugin builds this, as you will see in the next topic.

### Many modules, one build

Big projects split into several Maven projects, called **modules**, that build together. JavaQuest itself does this. Its top `pom.xml` lists them:

```xml
<modules>
  <module>runner</module>
  <module>content-model</module>
  <module>app</module>
  <module>tools</module>
</modules>
```

```callout jargon
A **parent POM** holds settings every module shares, such as the Java version and plugin versions. Each module's POM names it in a `<parent>` block and inherits from it.
```

From the top folder, `./mvnw package` builds every module, in the order they depend on each other. `-pl tools` (project list) builds only `tools`. Add `-am` (also make) to build the modules `tools` needs first.

```callout tip
Companies often send every download through their own copy of Maven Central, called a **mirror**. It is set once in `~/.m2/settings.xml`, outside the project, so the POM never changes.
```

## Worked example

You turn Tsunade's text file into a Maven project. The kiosk starts small: one class that prices a ride, and one test.

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

The whole `pom.xml`. The `<?xml ...?>` line and the `xmlns` and `xsi` parts of `<project>` tell editors which XML rules the file follows. Copy them as they are.

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

3. Find a conflict that Maven settled for you:

```bash
./mvnw -f practice/pom.xml dependency:tree -Dverbose -Dincludes=ch.qos.logback:logback-classic
```

Find the line that says `omitted for conflict`. Which Logback version did Spring want, and which one won? Then explain why, using nearest wins. (Spring wanted 1.5.38. The POM lists 1.6.6 directly, one step away, so 1.6.6 wins.)

## Recap

- Maven builds, tests and packages a project from one file, `pom.xml`, with one command. Code goes in `src/main/java`, tests in `src/test/java`, output in `target/`.
- Coordinates `groupId:artifactId:version` name every library. Versions follow MAJOR.MINOR.PATCH, and a `-SNAPSHOT` can still change.
- Running a phase runs every phase before it, so `./mvnw package` always compiles and tests first. Plugin goals, like `surefire:test` and `jar:jar`, do the real work.
- Dependencies come from Maven Central into `~/.m2/repository`, with their own dependencies too. Scopes decide when each jar is on the classpath.
- When two versions of one library meet, the nearest one wins. Check with `dependency:tree -Dverbose`, and pin versions with `<dependencyManagement>` or a BOM. A plain jar leaves its libraries out; a fat jar packs them in.

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

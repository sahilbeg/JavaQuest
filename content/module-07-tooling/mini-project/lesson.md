## The design

A **jar** (Java ARchive) is a zip file of compiled classes. One extra file inside it, the **manifest**, tells Java how to run it.

```callout jargon
A **manifest** is the text file `META-INF/MANIFEST.MF` inside a jar. Each line is a `Name: value` pair. `Main-Class` names the class whose `main` method `java -jar` starts.
```

Here is the manifest your packager writes:

```text
Manifest-Version: 1.0
Main-Class: jq.m07.feature.Kiosk
Implementation-Title: Spoke kiosk
Implementation-Version: 1.10.0
```

Maven's jar plugin and the JDK's `jar` tool both make files like this. Today you build one by hand, so you see every piece.

```bash
# What you replace: a folder of classes and a long command at every station
java -cp classes jq.m07.feature.Kiosk 12
# What you ship: one file
java -jar kiosk.jar 12
```

The feature has three parts.

```mermaid
flowchart LR
  A[compiled classes] --> P[JarPackager.pack]
  P --> J[kiosk.jar]
  J -->|java -jar| K[Kiosk.main]
  K --> R[Kiosk.run]
  R --> B[BuildInfo.version]
```

### Syntax: an exit code

Every program ends with a number for whoever started it. `0` means it worked. Any other number means something went wrong. Scripts and CI read it.

```java nocompile
public static void main(String[] args) {
    System.exit(run(args, System.out, System.err));
}
```

`main` is already written. `run` does the work and returns the code, so tests can call it without ending the test JVM.

### Syntax: reading the manifest at runtime

```java nocompile
String version = BuildInfo.class.getPackage().getImplementationVersion();
```

Java reads `Implementation-Version` from the manifest of the jar the class came from. Outside a jar there is no manifest, so the answer is `null`.

### Syntax: writing a jar entry

```java nocompile
try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarFile))) {
    jar.putNextEntry(new ZipEntry("jq/m07/feature/Kiosk.class"));
    Files.copy(classFile, jar);
    jar.closeEntry();
}
```

Each entry is a name, then bytes. Names always use `/` between folders, even on Windows.

```callout jargon
A **reproducible build** gives the same bytes every time it builds the same code. Then anyone can rebuild a release and check it matches what is running at Depot.
```

Two things change between builds even when the code does not: the time, and the order the file system lists files in. Fix both. Give every entry one fixed date and time, and sort the entries by name.

A zip stores each entry's time as a local date and time, with no time zone. So set it with `setTimeLocal(LocalDateTime)`. The older `setTime(long)` converts through the machine's time zone, and a laptop and a CI machine in UTC would store different times.

## What you build

Everything is in package `jq.m07.feature`. Follow the numbered TODOs.

1. `Kiosk.run(args, out, err)`:
   - With exactly one argument, `--version`, print `Spoke kiosk ` and the version, and return 0.
   - With exactly one argument that is a whole number of minutes, 0 or more, print the fare and return 0. The fare is 100 cents to unlock plus 15 cents a minute. For `12` it prints `Fare for 12 min: $2.80`.
   - For anything else (no arguments, two arguments, `abc`, `-5`), print the `USAGE` line to `err`, print nothing to `out`, and return 2.
2. `BuildInfo.version()` returns the manifest's `Implementation-Version`, or `dev` when it is `null`.
3. `JarPackager.pack(classesDir, jarFile, mainClass, version)`:
   - The manifest has `Manifest-Version` 1.0, `Main-Class`, `Implementation-Title` `Spoke kiosk` and `Implementation-Version`.
   - The manifest is the first entry. Every file below `classesDir` follows, sorted by entry name. There are no folder entries.
   - Every entry has the local date and time `FIXED_TIME`, set with `setTimeLocal`, so two builds of the same files are identical, byte for byte, in any time zone.

The last three checks are the real test. They copy your compiled kiosk classes, pack them with your `JarPackager`, and start `java -jar kiosk.jar` in a new JVM.

## Recap

- A jar is a zip file with a manifest. `Main-Class` makes it runnable with `java -jar`.
- The build can stamp facts, like the version, into the manifest. The code reads them back at runtime.
- Exit code 0 means success. Scripts and CI depend on it.
- Fixed entry times and sorted entries make a build reproducible.

## Common mistakes

**Forgetting `Main-Class`.** The jar builds fine, but `java -jar kiosk.jar` says `no main manifest attribute, in kiosk.jar`. Put the main class in the manifest.

**Building entry names with `toString()` alone.** On Windows, `Path.toString()` uses `\`, and Java cannot find `jq\m07\feature\Kiosk.class` inside the jar. Replace `\` with `/`.

**Printing the usage line to `out`.** A script that reads the fare would read the usage text as if it were a fare. Errors belong on `err`.

**Using the file's own modified time.** It changes whenever the code is checked out or rebuilt, so two builds still differ. Use `FIXED_TIME` for every entry.

**Using `setTime` with milliseconds.** It looks fixed, but it is turned into local time with the machine's time zone. A build in Riverton and a build on a UTC CI machine then differ. Use `setTimeLocal`.

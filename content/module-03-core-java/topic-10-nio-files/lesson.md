## Hook

Your java.io code from the last ticket works. But Tsunade counted: reading one file took a `File`, a `FileReader`, a `BufferedReader`, a loop and a `try`. That is a lot of code to review.

Java has a newer toolbox for files, called NIO. With it, reading a whole log is one line. The Harbor kiosk also needs a new trick: each morning, move yesterday's log into an archive folder.

## Concept

NIO stands for "New Input/Output". It lives in the package `java.nio.file`. Two names do most of the work:

- `Path` holds the address of a file or folder. It replaces `File`.
- `Files` is a class full of static methods that read, write, copy and move.

```callout analogy
A `Path` is a written address on an envelope. `Files` is the post office. The address alone delivers nothing; you hand it to the post office to act on it.
```

### Syntax: Path.of

```java nocompile
Path log = Path.of("rides.csv");
Path deep = Path.of("logs", "harbor", "rides.csv");
```

- `Path.of` builds a path from one or more parts.
- Java puts the right separator between parts for your system, `/` on Mac and `\` on Windows.

### Syntax: resolve and getFileName

```java nocompile
Path folder = Path.of("archive");
Path file = folder.resolve("rides-2026-11-11.csv");
System.out.println(file);                // archive/rides-2026-11-11.csv
System.out.println(file.getFileName());  // rides-2026-11-11.csv
```

- `resolve(name)` gives the path of `name` inside `folder`.
- `getFileName()` gives just the last part, as a `Path`. Call `toString()` on it to get a `String`.

### Syntax: reading and writing

```java nocompile
Files.writeString(log, "B-1042,Riverside,ebike,25\n", StandardCharsets.UTF_8);
List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
Files.write(log, lines, StandardCharsets.UTF_8);
```

- `Files.writeString` writes one `String` to the file. It creates the file, or replaces what was there.
- `Files.readAllLines` reads the whole file into a `List<String>`, one item per line.
- `Files.write` writes a list of lines, with a line break after each one.
- All of these open and close the file for you. No try-with-resources needed.

```callout jargon
A **charset** is the same idea as the character encoding from the last topic. `StandardCharsets.UTF_8` names the UTF-8 charset.
```

### Syntax: open options

To add to the end of a file, pass **open options** after the charset:

```java nocompile
Files.writeString(log, "B-2001,Harbor,classic,10\n", StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
```

- `CREATE` makes the file if it is missing.
- `APPEND` adds to the end instead of replacing.

### Syntax: checking and creating

```java nocompile
if (!Files.exists(log)) {
    System.out.println("No rides yet");
}
Files.createDirectories(Path.of("archive", "2026"));
```

- `Files.exists(path)` returns `true` if the file or folder is there.
- `Files.createDirectories(path)` makes the folder and any missing parents. It does nothing if the folder already exists.

### Syntax: copy and move

```java nocompile
Files.copy(log, Path.of("backup.csv"));
Files.move(log, archive.resolve("rides-2026-11-11.csv"));
```

- `Files.copy(from, to)` makes a second file. The original stays.
- `Files.move(from, to)` takes the file away from `from` and puts it at `to`. It can also rename a file.
- Both throw an exception if `to` already exists.

### Syntax: listing a folder

```java nocompile
try (DirectoryStream<Path> entries = Files.newDirectoryStream(folder)) {
    for (Path entry : entries) {
        System.out.println(entry.getFileName());
    }
}
```

- `Files.newDirectoryStream(folder)` lets you loop over every entry in a folder.
- It holds the folder open, so put it in try-with-resources.
- The order is not fixed. Sort the names yourself if order matters.

`Files.list(folder)` also lists a folder. It returns a **Stream**, a tool you meet in a later topic. For now, you only need to turn it into a list:

```java nocompile
try (Stream<Path> entries = Files.list(folder)) {
    List<Path> all = entries.toList();
}
```

Every `Files` method can throw `IOException`, the same checked exception as before.

## Worked example

This program writes a log, archives it, and lists the archive folder.

```java run
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Path log = Path.of("harbor-rides.csv");
        Files.writeString(log, "B-1042,Harbor,ebike,25\n", StandardCharsets.UTF_8);
        Files.writeString(log, "B-2001,Harbor,classic,10\n", StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);

        List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
        System.out.println("Rides: " + lines.size());

        Path archive = Path.of("archive-demo");
        Files.createDirectories(archive);
        Path target = archive.resolve("rides-2026-11-11.csv");
        Files.deleteIfExists(target);
        Files.move(log, target);

        System.out.println("Log still there? " + Files.exists(log));
        System.out.println("Archived as " + target.getFileName());
    }
}
```

Line by line:

- The first `writeString` creates the file with one line. The second adds a line with `APPEND`.
- `readAllLines` gives a list of 2 lines, so it prints `Rides: 2`.
- `createDirectories` makes the archive folder, or quietly does nothing if it exists.
- `deleteIfExists` clears an old copy, so the move works when you run the program again.
- After `move`, the original path is empty, so `exists` prints `false`.

The output is:

```text
Rides: 2
Log still there? false
Archived as rides-2026-11-11.csv
```

## Try it

Change the date labels below, or add a third file. Notice that you sort the names yourself.

```java run
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Path folder = Path.of("archive-try");
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("rides-2026-11-10.csv"), "x");
        Files.writeString(folder.resolve("rides-2026-11-09.csv"), "x");

        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(folder)) {
            for (Path entry : entries) {
                names.add(entry.getFileName().toString());
            }
        }
        Collections.sort(names);
        System.out.println(names);
    }
}
```

```callout tip
Gojo says: before you read a file that might be missing, check `Files.exists`. Then you can return an empty list instead of crashing.
```

## Recap

- `Path.of` builds a path. `resolve` goes into a folder, and `getFileName` gives the last part.
- `Files.readAllLines` and `Files.writeString` read and write a whole file in one call.
- Add `StandardOpenOption.CREATE` and `APPEND` to add to the end of a file.
- `Files.exists`, `createDirectories`, `copy` and `move` manage files and folders.
- Loop over a folder with `Files.newDirectoryStream`, and sort the names yourself.

## Common mistakes

**1. Expecting writeString to append.**

```java nocompile
Files.writeString(log, "B-1042,Riverside,ebike,25\n", StandardCharsets.UTF_8);
Files.writeString(log, "B-2001,Harbor,classic,10\n", StandardCharsets.UTF_8);
```

This compiles, but the file ends up with only the B-2001 line. The second call replaces the file. Pass `StandardOpenOption.CREATE, StandardOpenOption.APPEND` to add instead.

**2. Moving into a folder that does not exist.**

```java nocompile
Files.move(log, Path.of("archive").resolve("rides-2026-11-11.csv"));
```

At run time you see `java.nio.file.NoSuchFileException: harbor-rides.csv -> archive/rides-2026-11-11.csv`. Call `Files.createDirectories(Path.of("archive"))` first.

**3. Treating getFileName() as a String.**

```java nocompile
String name = file.getFileName();
```

The compiler says: `error: incompatible types: Path cannot be converted to String`. `getFileName()` returns a `Path`. Write `file.getFileName().toString()`.

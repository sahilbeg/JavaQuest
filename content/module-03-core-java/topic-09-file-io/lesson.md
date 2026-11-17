## Hook

Every night, Kakashi's team opens the ride log on the Riverside kiosk. Then someone types each line into a spreadsheet by hand. It takes an hour, and last week they skipped bike B-1042.

The kiosk already has the log saved as a file. A Java program can open that file, read every line, and write a new file just as easily. In this lesson you teach the kiosk to do the copying itself.

## Concept

Until now, your data lived in variables. Variables vanish when the program stops. A **file** keeps data on the disk, so it is still there tomorrow.

```callout jargon
A **file** is a named chunk of data saved on the disk, like `rides.csv`. A **folder** (also called a directory) holds files. A **path** is the address of a file, such as `logs/rides.csv`.
```

A path can be **relative** or **absolute**. A relative path like `rides.csv` starts from the folder your program runs in. An absolute path like `/home/kiosk/rides.csv` starts from the top of the disk.

### Syntax: the File class

```java nocompile
import java.io.File;

File log = new File("rides.csv");
```

- `java.io` is the package with Java's classic file tools. The `io` stands for input and output.
- A `File` object only holds the path. Creating it does not create or open anything.
- `log.exists()` returns `true` if the file is really there.
- `log.getName()` returns the name part, here `rides.csv`.

### Syntax: reading with BufferedReader

```java nocompile
BufferedReader reader = new BufferedReader(new FileReader("rides.csv", StandardCharsets.UTF_8));
String line = reader.readLine();
```

- `FileReader` opens the file and reads characters from it.
- `BufferedReader` wraps the `FileReader`. It reads big chunks at once, which is much faster.
- `readLine()` gives back the next line as a `String`, without the line break.
- When there are no lines left, `readLine()` returns `null`.

```callout analogy
A `FileReader` is a straw into the file. A `BufferedReader` is a cup. You fill the cup in one go, then drink from it line by line.
```

The usual pattern reads one line, then loops until `readLine()` returns `null`:

```java nocompile
String line = reader.readLine();
while (line != null) {
    System.out.println(line);
    line = reader.readLine();
}
```

### Syntax: writing with BufferedWriter

```java nocompile
BufferedWriter writer = new BufferedWriter(new FileWriter("rides.csv", StandardCharsets.UTF_8));
writer.write("B-1042,Riverside,ebike,25");
writer.newLine();
```

- `FileWriter` creates the file, or empties it if it already exists.
- `write(text)` adds text. It does not add a line break.
- `newLine()` adds the line break.
- `new FileWriter(name, StandardCharsets.UTF_8, true)` adds to the end instead of emptying the file. The `true` means append.

### Syntax: PrintWriter

`PrintWriter` lets you use the `println` you already know:

```java nocompile
PrintWriter out = new PrintWriter(new FileWriter("report.txt", StandardCharsets.UTF_8));
out.println("Rides today: 3");
```

### Syntax: try-with-resources for files

An open file is a **resource**. The operating system lends it to you, and you must give it back by closing it. If you forget, the data may never reach the disk.

```java nocompile
try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
    // read here
}
```

You met try-with-resources in the last topic. Java closes `reader` at the closing brace, even if an exception is thrown.

### IOException is a checked exception

Almost every file method can throw `IOException`. The file might be missing, or the disk might be full. `IOException` is **checked**, so the compiler makes you deal with it. You either catch it, or add `throws IOException` to your method.

```java nocompile
public static int countLines(File file) throws IOException {
    // file code here
}
```

### Character encoding

```callout jargon
**Character encoding** is the rule that turns letters into bytes on the disk. **UTF-8** is the common one. It handles every language and symbol.
```

Always pass `StandardCharsets.UTF_8` when you open a reader or writer. Then a station name like "Café Plaza" reads back correctly on every computer.

## Worked example

This program writes two rides to a file, then reads them back.

```java run
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {
        File log = new File("rides.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(log, StandardCharsets.UTF_8))) {
            writer.write("B-1042,Riverside,ebike,25");
            writer.newLine();
            writer.write("B-2001,Harbor,classic,10");
            writer.newLine();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(log, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(",");
                System.out.println(parts[0] + " rode " + parts[3] + " minutes");
                line = reader.readLine();
            }
        }
    }
}
```

Line by line:

- `main ... throws IOException` passes any file error up, so you do not need a catch here.
- The first `try` opens a writer. Two `write` and `newLine` pairs make two lines.
- At the closing brace, Java closes the writer. The text is now safely on disk.
- The second `try` opens a reader on the same file.
- `split(",")` cuts each line into four parts. `parts[3]` is the minutes.

The output is:

```text
B-1042 rode 25 minutes
B-2001 rode 10 minutes
```

## Try it

This program catches the error from a missing file. Run it, then change the name to a file that exists.

```java run
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        File log = new File("no-such-log.csv");
        try (BufferedReader reader = new BufferedReader(new FileReader(log, StandardCharsets.UTF_8))) {
            System.out.println("First line: " + reader.readLine());
        } catch (IOException e) {
            System.out.println("Could not read " + log.getName());
        }
    }
}
```

Now try `PrintWriter` with append mode. Run it twice and watch the count grow.

```java run
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter("visits.txt", StandardCharsets.UTF_8, true))) {
            out.println("Uptown kiosk opened");
        }
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("visits.txt", StandardCharsets.UTF_8))) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        System.out.println("Lines in visits.txt: " + count);
    }
}
```

```callout tip
Byte says: real code reads files from a known folder. The practice tests use a fresh temporary folder each time, so your own files are never touched.
```

## Recap

- A `File` holds a path. `FileReader` and `FileWriter` open the real file.
- Wrap them in `BufferedReader` and `BufferedWriter` for speed and for `readLine()` and `newLine()`.
- Loop on `readLine()` until it returns `null`.
- Open files inside try-with-resources so they always close.
- `IOException` is checked: catch it or declare `throws IOException`. Pass `StandardCharsets.UTF_8` every time.

## Common mistakes

**1. Forgetting about IOException.**

```java nocompile
public static String firstLine(File file) {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    return reader.readLine();
}
```

The compiler says: `error: unreported exception FileNotFoundException; must be caught or declared to be thrown`. Fix it by adding `throws IOException` to the method, or by catching it.

**2. Never closing the writer.**

```java nocompile
BufferedWriter writer = new BufferedWriter(new FileWriter("rides.csv"));
writer.write("B-1042,Riverside,ebike,25");
```

This compiles, but `rides.csv` stays empty. The text waits in the buffer and never reaches the disk. Open the writer in `try ( ... )` so it closes and saves.

**3. Calling readLine() twice per loop.**

```java nocompile
while (reader.readLine() != null) {
    System.out.println(reader.readLine());
}
```

On a file with the lines `a`, `b`, `c`, this prints `b` and then `null`. Each call reads a new line, so the check eats every other line. Store the line in a variable first, then use that variable.

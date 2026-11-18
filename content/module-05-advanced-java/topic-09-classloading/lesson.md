## Hook

Picture a theatre. The script lists every actor who appears. On opening night an actor is called on stage, and nobody comes out. The script was fine. The actor simply did not turn up at the theatre.

Tsunade's ticket is that story. The kiosk code compiled without a complaint. Then the Old Mill kiosk started and died with `NoClassDefFoundError`. To fix it, you need to know how the JVM finds and loads classes while your program runs.

## Concept

### Compile time and run time are separate

`javac` turns your `.java` files into `.class` files. Each `.class` file holds the bytecode of one class. At run time the JVM must find those `.class` files again, plus the ones from every library you use.

Finding a class at compile time proves nothing about run time. The two steps can look in different places.

### The life of a class

A class goes through three steps before your code can use it:

1. **Load.** A class loader finds the `.class` file's bytes and reads them into metaspace, the class data area from the JVM memory topic.
2. **Link.** The JVM checks the bytecode is valid and safe, and makes room for the `static` fields with default values such as `0` and `null`.
3. **Initialize.** The JVM runs the class's static initializers: the `static` field assignments and any `static { }` blocks, top to bottom.

```mermaid
flowchart LR
  F[".class file"] --> L["Load"] --> K["Link"] --> I["Initialize"] --> U["Ready to use"]
```

```callout jargon
A **class loader** is a Java object whose job is to find a class's bytes and turn them into a class the JVM can use.
```

### Lazy loading

The JVM does not load every class at start-up. It loads and initializes a class the first time your code really needs it. This is **lazy loading**.

A class is initialized on first use, such as:

- creating an object with `new`
- calling a `static` method
- reading or writing a `static` field (other than a constant)

### Syntax: a static initializer block

```java
class FareTable {
    static long classicCentsPerMinute;

    static {
        classicCentsPerMinute = 15;
        System.out.println("FareTable ready");
    }
}
```

- `static { ... }` is a **static initializer block**. It runs once, when the class is initialized.
- It never runs again, however many times you use the class.

```java run
public class Main {
    public static void main(String[] args) {
        System.out.println("Kiosk starting");
        System.out.println("Classic rate: " + FareTable.classicCentsPerMinute() + " cents");
        System.out.println("E-bike rate: " + FareTable.ebikeCentsPerMinute() + " cents");
    }
}

class FareTable {
    static {
        System.out.println("FareTable loaded");
    }

    static long classicCentsPerMinute() {
        return 15;
    }

    static long ebikeCentsPerMinute() {
        return 30;
    }
}
```

The output is always this, in this order:

```text
Kiosk starting
FareTable loaded
Classic rate: 15 cents
E-bike rate: 30 cents
```

`FareTable loaded` appears after `Kiosk starting`, because the class waits until its first use. It appears only once, before the first rate.

```callout tip
Java first builds the whole text `"Classic rate: " + ...` before `println` runs. The method call inside triggers initialization, so `FareTable loaded` prints before that line.
```

### Class initialization is thread-safe

What if eight threads touch a class for the first time at the same moment? The JVM makes sure only one of them runs the static initializers. The other seven wait, then see the finished result. Initialization happens exactly once, even under a race.

That gives you a neat way to build something expensive once, and only when it is first needed. It is called the **holder idiom**:

```java run
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        Set<StationMap> seen = ConcurrentHashMap.newKeySet();
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < 8; i++) {
                pool.submit(() -> seen.add(StationMap.get()));
            }
        }
        System.out.println("Different maps seen: " + seen.size());
    }
}

class StationMap {
    private StationMap() {
        System.out.println("Building the station map");
    }

    private static class Holder {
        static final StationMap INSTANCE = new StationMap();
    }

    static StationMap get() {
        return Holder.INSTANCE;
    }
}
```

`Holder` is not initialized until `get()` first reads `Holder.INSTANCE`. Then the JVM builds the map once, under its own lock. Eight threads, one map, and no `synchronized` in sight.

The JVM also guarantees that every thread sees the finished object, with all its fields set. The [Java Memory Model ticket](/t/module-05-advanced-java/topic-14-memory-model) explains why that guarantee matters so much.

### The class loader hierarchy

Java uses three built-in class loaders, stacked like a family tree:

- The **bootstrap** loader loads the core of Java, such as `String` and `ArrayList`. It is built into the JVM, so Java code sees it as `null`.
- The **platform** loader loads the other parts of the JDK, such as `java.sql`.
- The **application** loader (also called the system loader) loads your classes and your libraries from the classpath.

### Syntax: asking for a class loader

```java nocompile
ClassLoader loader = FareTable.class.getClassLoader();
```

`FareTable.class` is the `Class` object that describes `FareTable`. Its `getClassLoader()` returns the loader that loaded it.

```java run
public class Main {
    public static void main(String[] args) {
        System.out.println(String.class.getClassLoader());
        System.out.println(java.sql.Date.class.getClassLoader().getName());
        System.out.println(ClassLoader.getSystemClassLoader().getName());
        System.out.println(ClassLoader.getSystemClassLoader().getParent().getName());
    }
}
```

This prints:

```text
null
platform
app
platform
```

`String` came from the bootstrap loader, so you see `null`. The application loader is named `app`, and its parent is the platform loader.

### Parent delegation

When a loader is asked for a class, it first asks its parent. Only when the parent cannot find the class does the loader search itself. This is **parent delegation**.

```mermaid
flowchart TB
  A["Application loader (your classpath)"] -->|asks first| P["Platform loader (rest of the JDK)"]
  P -->|asks first| B["Bootstrap loader (core Java)"]
```

Parent delegation keeps Java safe. If you write your own class called `java.lang.String`, the bootstrap loader finds the real one first. Your copy never replaces it.

### The classpath

The **classpath** is the list of folders and `.jar` files where the application loader looks for classes. A `.jar` file is a zip file full of `.class` files. You set the classpath with `-cp`.

### Syntax: running with a classpath

```text
java -cp build/classes:libs/fares.jar jq.kiosk.Kiosk
```

- The parts are separated by `:` on macOS and Linux, and by `;` on Windows.
- The last word is the main class, with its full package name.

If `fares.jar` is missing from that list, the classes inside it are missing at run time, even though `javac` saw them.

## Worked example

### ClassNotFoundException and NoClassDefFoundError

Both errors mean "a class could not be found", but they come from different places.

- `ClassNotFoundException` is a checked exception. You get it when you ask for a class **by name** at run time, for example with `Class.forName`, and no loader finds it.
- `NoClassDefFoundError` is an error. The JVM throws it when compiled code refers to a class directly, and that class is missing at run time or failed to initialize earlier.

### Syntax: loading a class by name

```java nocompile
Class<?> type = Class.forName("jq.kiosk.FareTable");
```

`Class.forName` loads and initializes the named class. The name must include the package. It throws `ClassNotFoundException` when no loader can find it.

```java run
public class Main {
    public static void main(String[] args) {
        try {
            Class<?> type = Class.forName("java.util.ArrayList");
            System.out.println("Found " + type.getSimpleName());
            Class.forName("jq.kiosk.FareTable");
        } catch (ClassNotFoundException e) {
            System.out.println("Missing: " + e.getMessage());
        }
    }
}
```

Line by line:

1. `Class.forName("java.util.ArrayList")` finds the class through parent delegation. It prints `Found ArrayList`.
2. `Class.forName("jq.kiosk.FareTable")` finds nothing, so it throws.
3. The `catch` prints `Missing: jq.kiosk.FareTable`.

For Tsunade's ticket, the likely story is this. The build compiled against `fares.jar`. The kiosk's start script runs `java -cp` with a list that leaves that jar out. When `Kiosk` first touches `FareTable`, the JVM cannot find it and throws `NoClassDefFoundError: jq/kiosk/FareTable`. The fix is to add the jar to the run-time classpath.

### Version conflicts: NoSuchMethodError

A third error comes from libraries, not from a missing jar. Your code was compiled against one version of a library, but a different version is on the classpath at run time.

Say the kiosk was compiled against `fares-2.jar`, where `FareLib.fareCents(int minutes)` exists. At run time, `fares-1.jar` is on the classpath instead. In version 1, that method also took a bike type. A real run:

```text
Exception in thread "main" java.lang.NoSuchMethodError: 'long FareLib.fareCents(int)'
	at Kiosk.main(Kiosk.java:3)
```

The class was found, but the method your bytecode asks for is not in it. Its cousins mean the same thing:

- `NoSuchFieldError`: a field your code uses is gone.
- `IncompatibleClassChangeError`: a class changed shape, for example it became an interface.
- `NoClassDefFoundError`: the whole class is gone in that version.

How does the wrong version get there? Two of your libraries each need `fares`, but different versions. The build tool picks one, and the other library breaks. This mess is nicknamed **jar hell**. The [Maven ticket](/t/module-07-tooling/topic-01-maven) shows how `mvn dependency:tree` finds which library pulled in which version.

## Try it

`NoClassDefFoundError` also appears when a class fails during its static initializer. Run this, then change the `throw` line so the block only prints, and run it again.

```java run
public class Main {
    public static void main(String[] args) {
        for (int i = 1; i <= 2; i++) {
            try {
                System.out.println("Rate: " + Rates.cargo());
            } catch (Throwable t) {
                System.out.println("Try " + i + ": " + t.getClass().getSimpleName());
            }
        }
    }
}

class Rates {
    static {
        if (true) {
            throw new IllegalStateException("rates file missing");
        }
    }

    static long cargo() {
        return 25;
    }
}
```

The first use throws `ExceptionInInitializerError`. The JVM marks the class as broken. Every later use throws `NoClassDefFoundError`. So when you see `NoClassDefFoundError`, scroll up the log for an earlier `ExceptionInInitializerError` too.

## Recap

- A class is loaded, linked and initialized. Static initializers run once, on first use, and only one thread runs them, which makes the holder idiom safe.
- Three built-in loaders form a hierarchy: bootstrap (shown as `null`), platform and application.
- Parent delegation means a loader asks its parent before searching itself. The application loader searches the classpath that you set with `-cp`.
- `ClassNotFoundException` comes from loading by name. `NoClassDefFoundError` means a class your compiled code uses is missing or broken at run time. `NoSuchMethodError` means the wrong library version is on the classpath.

## Common mistakes

**Calling a method on the bootstrap loader.**

```java
public class Main {
    public static void main(String[] args) {
        System.out.println(String.class.getClassLoader().getName());
    }
}
```

Java says: `Exception in thread "main" java.lang.NullPointerException: Cannot invoke "java.lang.ClassLoader.getName()" because the return value of "java.lang.Class.getClassLoader()" is null`. Core classes come from the bootstrap loader, which shows up as `null`. Check for `null` before you call a method on it.

**Forgetting the package in Class.forName.**

```java
public class Main {
    public static void main(String[] args) throws Exception {
        Class.forName("ArrayList");
    }
}
```

Java says: `Exception in thread "main" java.lang.ClassNotFoundException: ArrayList`. Class names given as text need the full package: `Class.forName("java.util.ArrayList")`.

**Expecting a static block to run at start-up.**

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Kiosk ready");
    }
}

class Docks {
    static {
        System.out.println("12 docks checked");
    }
}
```

The output is only `Kiosk ready`. Nothing uses `Docks`, so the JVM never initializes it. Touch the class first, for example by calling a static method, when you need its setup to run.

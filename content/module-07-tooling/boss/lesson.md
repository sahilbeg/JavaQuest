## What happened

Kiosk 1.10.0 is ready. It has to reach Riverside, Old Mill, Harbor, Uptown and Depot tonight. The nightly pipeline runs on a fresh CI machine at 02:00, and three of its stages are red.

Every one of them passes on Tsunade's laptop. That is the clue. Each bug depends on something that is true on her laptop and false on the CI machine.

```callout analogy
A recipe that says "use the flour on the left shelf" works in your own kitchen. In a friend's kitchen it fails. Good code, like a good recipe, says what it needs and gets it handed over.
```

Run `Pipeline` to see the three stages. Each line says GREEN or RED.

```text
release gate: RED
load config: RED (NoSuchFileException: /Users/maya/spoke/kiosk.properties)
night fare: RED
```

## What you need

This builds on topic 04 (CI basics) and module 6's tests.

### Syntax: comparing versions as numbers

A version like `1.10.0` has three whole numbers: **major**, **minor** and **patch**. Compare the most important number first. Only when it is equal, move on to the next.

```java nocompile
if (major != other.major) {
    return Integer.compare(major, other.major);
}
```

```callout jargon
**Semantic versioning** is the rule `major.minor.patch`. A breaking change raises major, a new feature raises minor, and a bug fix raises patch.
```

Text compares one character at a time. `"1.10.0"` and `"1.9.3"` agree on `1.`, then `'1'` meets `'9'`. Since `'1'` comes first, text calls 1.10.0 older.

### Syntax: a path inside a given folder

```java nocompile
Path file = configDir.resolve("kiosk.properties");
if (!Files.exists(file)) {
    return defaults();
}
```

`resolve` joins a folder and a file name, on any operating system. The test hands in a fresh `@TempDir` folder, so nothing depends on whose laptop it is.

### Syntax: values with spaces

`Properties` removes the spaces before a value but keeps the spaces after it. `"18 "` is not a number, so `Long.parseLong` throws. A station called `"Old Mill  "` never matches `"Old Mill"`. Call `strip()` on every value.

```java nocompile
String value = properties.getProperty("cents.per.minute");   // "18 " or null
long cents = value == null ? 15 : Long.parseLong(value.strip());
```

### Syntax: asking an injected clock for the time

```java nocompile
LocalTime now = LocalTime.now(clock);
```

A `Clock` is an object that answers "what time is it?". The real program gets the system clock. A test gets `Clock.fixed(...)`, which always answers the same instant, in the time zone it was given.

## The tiers

1. **Bronze** resolves the incident. `Version.compareTo` compares major, then minor, then patch, as numbers. `ReleaseGate.canShip("1.9.3", "1.10.0")` is then true.
2. **Silver**: `KioskConfig.load(configDir)` reads `kiosk.properties` from `configDir`. With no file it returns `defaults()`. A missing key gets its default value, and spaces around values are ignored.
3. **Gold**: `NightFare.surchargeCents()` reads the time from its own clock. It charges 50 cents from 22:00 up to, but not including, 06:00, in the clock's time zone.

Change only the code under each `TODO`. The checks are the CI pipeline: make them all green.

## Recap

- Text order is not number order. Compare version parts with `Integer.compare`.
- Never hard-code a path from your own machine. Take the folder as a parameter, and give missing settings a default.
- `strip()` a value from a file before you parse it.
- Code that reads the time should take a `Clock`. Tests then pick any time, and the nightly build agrees with the daytime one.

## Common mistakes

**Splitting on `.` without escaping it.** `split(".")` treats the dot as "any character" and returns an empty array. Use `split("\\.")`. The `parse` method already does this for you.

**Swapping one fixed path for another.** Changing `/Users/maya/...` to your own home folder still fails on CI. Use the `configDir` you were given.

**Catching the exception and returning null.** The stage stops crashing, but the next line that uses the config throws a `NullPointerException`. Return `defaults()` instead.

**Passing the clock and still calling `LocalTime.now()`.** The test gives a clock, and the code ignores it. The surcharge then depends on when the pipeline happens to run. Every `now()` call needs the clock.

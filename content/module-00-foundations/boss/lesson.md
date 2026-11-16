## What happened

The kiosk at Riverside Station shows the wrong welcome screen. Riders should see exactly these three lines:

```text
Welcome to Spoke!
Tap your card to unlock a bike.
Riverside Station
```

Instead, the screen shows this:

```text
welcome to spoke.Tap your card to unlock a bike.
```

```callout analogy
An incident is like a flat tyre during a race. Fix the part that stops riders first, then make the repair solid.
```

## What you need

You know most of this from SPK-0005. One new tool is `System.out.print`:

- Text goes inside double quotes, and Java compares it exactly, capital letters and all.
- `System.out.println` prints text and then moves to a new line.
- `System.out.print` prints text and stays on the same line.

### Syntax: print versus println

```java
System.out.print("Same ");
System.out.print("line");
System.out.println();
System.out.println("Own line");
```

```animate
title: print waits, println ends the line
kind: code
code: |
  System.out.print("Same ");
  System.out.print("line");
  System.out.println();
  System.out.println("Own line");
steps:
  - line: 1
    say: print sends "Same " and stays on that line. The line is not finished, so the next text lands right after it.
  - line: 2
    say: print adds "line" just after "Same ". Still the same line, which is how the broken kiosk ran its text together.
  - line: 3
    out: Same line
    say: println() with nothing inside just ends the line. Now "Same line" shows up, finished.
  - line: 4
    out: Own line
    say: println prints "Own line" on a fresh line, then ends that line too.
```

```callout tip
Run the program with the Run button to see what the screen shows. Then run the checks to see which tier passes.
```

## The tiers

Each tier is a group of checks. Fix them in order.

1. **Bronze** resolves the incident: `welcome()` returns exactly `Welcome to Spoke!`.
2. **Silver**: `main` prints the welcome and the tap instruction on two separate lines.
3. **Gold**: `main` prints `Riverside Station` as the third line, and nothing else.

Bronze earns the most XP. Silver and gold each add a bonus.

## Recap

- Text in Java must match exactly, including capitals and punctuation.
- `print` stays on the line. `println` ends the line.
- Fix what blocks riders first, then polish the rest.

## Common mistakes

**Fixing the text inside `main` only.** The bronze check calls `welcome()` directly. Change the text that `welcome()` returns, and print `welcome()` from `main`.

**Adding a space at the end.** `"Welcome to Spoke! "` with a trailing space is a different text. The checks will say what they expected and what they got.

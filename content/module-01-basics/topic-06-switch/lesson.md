## Hook

Picture a vending machine. You press B4, and it drops a bag of crisps. You press C2, and out comes a drink. The machine does not ask a chain of yes or no questions. It looks at your button and jumps straight to the right slot.

Spoke now rents three kinds of bike: classic, e-bike and cargo. Each one has its own price per minute. You could write a long chain of `if` and `else if`. Java has a neater tool for "pick one answer from a list": the `switch`.

## Concept

A `switch` looks at one value. Then it jumps to the `case` that matches that value.

```callout jargon
A **case** is one labelled option inside a switch. A **label** is the value written after the word `case`, like `case 1` or `case "ebike"`.
```

### Syntax: the classic switch statement

```java
int value = 1;
switch (value) {
    case 1:
        // runs when value is 1
        break;
    case 2:
        // runs when value is 2
        break;
    default:
        // runs when no case matches
}
```

- The value to check goes in round brackets after `switch`.
- Each `case` ends with a colon `:`.
- `break` jumps out of the switch.
- `default` runs when no case matches. It works like the final `else`.

```java
int dock = 2;
switch (dock) {
    case 1:
        System.out.println("Dock one");
        break;
    case 2:
        System.out.println("Dock two");
        break;
    default:
        System.out.println("Some other dock");
}
```

This prints `Dock two`.

### Syntax: fall-through

```java
int value = 2;
switch (value) {
    case 1:
    case 2:
        // runs for 1 and for 2
        break;
}
```

Without a `break`, Java keeps going into the next case. This is called **fall-through**. You can use it on purpose to share code between cases.

```java
int dock = 5;
switch (dock) {
    case 4:
    case 5:
    case 6:
        System.out.println("Middle row");
        break;
    default:
        System.out.println("Other row");
}
```

Dock 5 jumps to `case 5`, which has no code of its own, so it falls into `case 6` and prints `Middle row`.

### Syntax: switching on Strings

```java
String type = "ebike";
switch (type) {
    case "classic":
        System.out.println("0.15 a minute");
        break;
    case "ebike":
        System.out.println("0.30 a minute");
        break;
    default:
        System.out.println("Unknown bike");
}
```

A switch can check Strings. It compares the text the same way `.equals` does, so capital letters matter. `"EBike"` will not match `"ebike"`.

```callout warning
If the String is `null`, the switch crashes with a `NullPointerException`, even when there is a `default`. Java 21 lets you add `case null ->` to an arrow switch to handle it.
```

Watch the switch jump to the matching case:

```animate
title: The switch jumps to a case
kind: code
code: |
  String type = "ebike";
  switch (type) {
      case "classic":
          System.out.println("0.15 a minute");
          break;
      case "ebike":
          System.out.println("0.30 a minute");
          break;
      default:
          System.out.println("Unknown bike");
  }
  System.out.println("Price shown");
steps:
  - line: 1
    vars: { type: '"ebike"' }
    say: The rider picked an e-bike.
  - line: 2
    say: The switch reads the value in brackets, "ebike", and looks for the case with that label.
  - line: 6
    say: It lands on case "ebike". The classic case is skipped, and its lines never light up.
  - line: 7
    out: "0.30 a minute"
    say: The e-bike price prints.
  - line: 8
    say: break jumps out of the switch, so default never runs.
  - line: 12
    out: Price shown
    say: Java carries on after the switch's closing brace.
```

### Fall-through by accident

```callout warning
A forgotten `break` causes fall-through by accident. Java runs the next case too, even though its label does not match.
```

Watch a classic bike get the e-bike price too, because one `break` is missing:

```animate
title: The missing break
kind: code
code: |
  String type = "classic";
  switch (type) {
      case "classic":
          System.out.println("0.15");
      case "ebike":
          System.out.println("0.30");
          break;
      default:
          System.out.println("Unknown");
  }
steps:
  - line: 1
    vars: { type: '"classic"' }
    say: The rider picked a classic bike.
  - line: 3
    say: The switch jumps straight to the case labelled "classic".
  - line: 4
    out: "0.15"
    say: The classic price prints. But there is no break after it.
  - line: 5
    say: So Java walks past the next label without checking it. "ebike" does not match, and that does not matter.
  - line: 6
    out: "0.30"
    say: The e-bike price prints too, for a classic bike. That is fall-through by accident.
  - line: 7
    say: break finally jumps out of the switch. The default case never runs.
```

### Syntax: the switch expression (Java 14 and later)

```java
String type = "ebike";
double price = switch (type) {
    case "classic" -> 0.15;
    case "ebike" -> 0.30;
    default -> -1;
};
```

The modern switch uses arrows `->` and gives back a value. That makes it an **expression**: code that works out to a value, like `2 + 3`.

- Each arrow case runs only its own code. There is no fall-through, so you never need `break`.
- A switch expression must cover every possible value. With Strings and ints, that means you need a `default`.
- Notice the semicolon after the closing brace `};`. The `;` after `}` ends the line `double price = switch ... };`, just like `double price = 0.15;`.

```callout analogy
The classic switch is a staircase. You land on a step and keep walking down until `break` stops you. The arrow switch is a row of separate doors. You go through one door and you are done.
```

### Syntax: multiple labels

```java
int dock = 5;
String row = switch (dock) {
    case 1, 2, 3 -> "front";
    case 4, 5, 6 -> "middle";
    default -> "back";
};
```

Put several labels on one case, separated by commas. This replaces the fall-through trick from the classic switch.

### Syntax: return a switch expression

```java
public static String size(String type) {
    return switch (type) {
        case "cargo" -> "big";
        default -> "normal";
    };
}
```

You can `return` the switch directly from a method.

### Syntax: arrow cases with yield

```java nocompile
case "cargo" -> {
    // several steps
    yield value;
}
```

An arrow case can run a whole block in curly braces. Inside a switch expression, `yield` hands back the block's value, like `return` does for a method.

```java
String type = "cargo";
int minutes = 20;
long cents = switch (type) {
    case "classic" -> 15L * minutes;
    case "cargo" -> {
        System.out.println("Cargo bikes cost more per minute.");
        yield 25L * minutes;
    }
    default -> 0;
};
System.out.println(cents);   // 500
```

Arrows also work in a plain switch statement that gives back nothing. Each case runs only its own line, with no fall-through and no `break`:

```java
String type = "ebike";
switch (type) {
    case "classic" -> System.out.println("Classic bike");
    case "ebike", "cargo" -> System.out.println("Big battery or big basket");
    default -> System.out.println("Unknown bike");
}
```

## Worked example

This program prices a 20 minute ride for each bike type.

```java run
public class Main {
    public static void main(String[] args) {
        String type = "cargo";
        int minutes = 20;

        double price = switch (type) {
            case "classic" -> 0.15;
            case "ebike" -> 0.30;
            case "cargo" -> 0.25;
            default -> -1;
        };

        if (price == -1) {
            System.out.println("Unknown bike type");
        } else {
            System.out.println("Fare: " + (1.00 + minutes * price));
        }
    }
}
```

1. `type` holds `"cargo"`, so the switch jumps to `case "cargo"`.
2. That case gives back `0.25`, which goes into `price`.
3. `price == -1` is `false`, so the `else` block runs.
4. The fare is `1.00 + 20 * 0.25`, so the program prints `Fare: 6.0`.

The value `-1` is a signal. No real price is negative, so `-1` means "I do not know this bike".

```callout warning
A `-1` signal is a beginner shortcut, and your ticket uses it. Real code avoids it, because a caller who forgets to check can add `-1` into a total by mistake. Professionals throw an exception instead (module 03), or use an enum, a type that lists only the real bike types (module 02).
```

## Try it

Change `dock` to different numbers from 1 to 12, then press **Run** each time.

```java run
public class Main {
    public static void main(String[] args) {
        int dock = 4;

        String color = switch (dock) {
            case 1, 2, 3 -> "blue";
            case 4, 5, 6 -> "green";
            default -> "grey";
        };
        System.out.println("Dock " + dock + " is " + color);

        switch (dock) {
            case 1:
                System.out.println("First dock");
                break;
            case 12:
                System.out.println("Last dock");
                break;
            default:
                System.out.println("A dock in between");
        }
    }
}
```

Next, delete the first `break;` and set `dock` to `1`. Watch what fall-through does.

## Recap

- A `switch` looks at one value and jumps to the matching `case`.
- In the classic switch, `break` stops the code. Without it, Java falls through into the next case.
- `default` runs when no case matches.
- The arrow switch expression `case x -> value;` gives back a value and never falls through.
- One case can hold several labels: `case 1, 2, 3 ->`. A switch can also check Strings.

## Common mistakes

**Forgetting `break` in a classic switch.**

```java
public class Main {
    public static void main(String[] args) {
        String type = "classic";
        switch (type) {
            case "classic":
                System.out.println("0.15");
            case "ebike":
                System.out.println("0.30");
                break;
        }
    }
}
```

This compiles, but it prints `0.15` and then `0.30`. Java fell through into the `"ebike"` case. Add `break;` at the end of each case.

**Leaving out `default` in a switch expression.**

```java nocompile
int dock = 7;
String color = switch (dock) {
    case 1, 2, 3 -> "blue";
    case 4, 5, 6 -> "green";
};
```

Java says: `the switch expression does not cover all possible input values`. An int has billions of possible values. Add `default -> "grey";` as the last case.

**Forgetting the semicolon after a switch expression.**

```java nocompile
String color = switch (dock) {
    case 1 -> "blue";
    default -> "grey";
}
```

Java says: `';' expected`. The `;` after `}` ends the line `String color = switch ... };`, just like `String color = "grey";`.

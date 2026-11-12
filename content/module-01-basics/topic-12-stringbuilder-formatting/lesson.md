## Hook

Priya holds up a photo of a crumpled receipt. The ride line says `$2.4999999`. The prices wander left and right like a bike with a flat tyre. The maths was right, but the receipt looks broken.

Computers store many decimal numbers with tiny rounding errors. Riders should never see them. In this lesson you learn to round prices to two decimals, line them up in columns, and build long text quickly.

## Concept

**Formatting** means turning values into text with a fixed shape. You write a pattern once, and Java fills in the values.

### Syntax: String.format

```java nocompile
String text = String.format("pattern", value1, value2);
```

The pattern is normal text with **placeholders**. A placeholder starts with `%` and marks a spot for a value. Java fills them in from left to right.

| Placeholder | Fills in | Example result |
|---|---|---|
| `%s` | any value as text | `Riverside` |
| `%d` | a whole number (`int`) | `12` |
| `%.2f` | a decimal with exactly 2 digits after the point | `2.50` |
| `%n` | a line break | |

```java
String a = String.format("Station: %s", "Harbor");        // Station: Harbor
String b = String.format("%d docks", 12);                 // 12 docks
String c = String.format("$%.2f", 2.4999999);             // $2.50
String d = String.format("%s has %d docks", "Uptown", 12); // Uptown has 12 docks
```

`%.2f` rounds the number. `2.4999999` becomes `2.50`.

```callout warning
`%.2f` follows your computer's language setting. JavaQuest's checks always use a decimal point.
```

```callout warning
The type must match. `%d` with a `double` such as `2.5` crashes with `IllegalFormatConversionException`. Use `%.2f` for decimals.
```

### Syntax: formatted

```java nocompile
String text = "pattern".formatted(value1, value2);
```

`formatted` does the same job as `String.format`. You call it on the pattern itself.

```java
String e = "$%.2f".formatted(1.0);   // $1.00
```

### Syntax: widths

```java
String.format("%10s", "Uptown");    // "    Uptown"
String.format("%-10s", "Uptown");   // "Uptown    "
String.format("%6.2f", 2.5);        // "  2.50"
```

- A number after `%` is the **width**: the smallest number of characters to use.
- Java pads with spaces on the left, so the value sits on the right. This is **right-aligned**.
- A minus sign `-` pads on the right instead. The value sits on the left. This is **left-aligned**.
- `%6.2f` means 2 decimals and at least 6 characters in total.

Widths make columns. Every row uses the same widths, so the edges line up.

```java run
public class Main {
    public static void main(String[] args) {
        System.out.println(String.format("%-12s%8s", "Unlock fee", "$1.00"));
        System.out.println(String.format("%-12s%8s", "Ride", "$12.45"));
        System.out.println(String.format("%-12s%8.2f", "Classic/min", 0.15));
    }
}
```

### The problem with + in a loop

You can glue Strings together with `+`. Inside a loop, that gets slow. Strings are immutable, so every `+` builds a whole new String and copies all the old text into it.

```java
String text = "";
for (int i = 1; i <= 3; i++) {
    text = text + "Dock " + i + "\n";   // a new copy every time
}
```

With three lines, nobody notices. With ten thousand lines, the program copies the same text over and over.

```callout analogy
Using `+` in a loop is like rewriting your whole shopping list each time you add one item. A StringBuilder is a notepad. You just write the next line at the bottom.
```

### Syntax: StringBuilder

```java
StringBuilder sb = new StringBuilder();
sb.append("text");
String result = sb.toString();
```

- A **StringBuilder** is a box of text that you *can* change.
- `new StringBuilder()` makes an empty one. The word `new` creates a fresh object. An **object** is a value that has its own methods, like a String or a StringBuilder.
- `append(x)` adds `x` to the end. It accepts Strings, numbers and characters.
- `toString()` gives back the finished text as a normal String.

```java
StringBuilder sb = new StringBuilder();
sb.append("Riverside");
sb.append(" ");
sb.append(12);
System.out.println(sb.toString());   // Riverside 12
```

### Syntax: text blocks

```java
String card = """
        Spoke Bikes
        Thanks for riding!
        """;
```

A **text block** is a String that spans several lines. It starts and ends with three double quotes. Java removes the shared indentation on the left. Text blocks are handy for fixed text, like a receipt header.

## Worked example

This program builds a small receipt with a loop and a StringBuilder.

```java run
public class Main {
    public static void main(String[] args) {
        String[] labels = {"Unlock fee", "Ride 10 min"};
        double[] amounts = {1.0, 1.5};
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (int i = 0; i < labels.length; i++) {
            sb.append(String.format("%-12s%8.2f", labels[i], amounts[i]));
            sb.append("\n");
            total = total + amounts[i];
        }
        sb.append(String.format("%-12s%8.2f", "TOTAL", total));
        System.out.println(sb.toString());
    }
}
```

1. Two arrays hold the labels and the amounts, in matching order.
2. `sb` starts empty, and `total` starts at 0.
3. Each loop turn appends one formatted line and a line break, then adds to the total.
4. After the loop, the program appends the TOTAL line.
5. `toString()` turns the builder into a String, and `println` prints it.

## Try it

Add a third item, like `"Ride 5 min"` with `0.75`. Then change the widths from 12 and 8 to 15 and 10. Press **Run** and watch the columns move together.

```java run
public class Main {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("Spoke Bikes, Old Mill\n");
        sb.append("%-12s%8s%n".formatted("Unlock fee", "$1.00"));
        sb.append("%-12s%8s%n".formatted("Ride", "$2.50"));
        System.out.print(sb.toString());
    }
}
```

```callout tip
Print a ruler line such as `12345678901234567890` above your output. It helps you count where each column starts.
```

## Recap

- `String.format` and `formatted` fill placeholders: `%s` for text, `%d` for whole numbers, `%.2f` for money.
- A width like `%8s` pads on the left. A minus, as in `%-12s`, pads on the right.
- Using `+` in a loop copies text again and again. A `StringBuilder` with `append` avoids that.
- Call `toString()` to get the finished String out of a StringBuilder.
- Text blocks use `"""` for fixed text over several lines.

## Common mistakes

**Printing money without rounding.**

```java
double fare = 0.1 + 0.2;
System.out.println("$" + fare);
```

You see `$0.30000000000000004`. Fix: `System.out.println(String.format("$%.2f", fare));` prints `$0.30`.

**Using %d for a decimal.**

```java
System.out.println(String.format("%d", 2.5));
```

The program crashes with `IllegalFormatConversionException: d != java.lang.Double`. Fix: use `%.2f` for a `double`.

**Forgetting toString().**

```java nocompile
StringBuilder sb = new StringBuilder();
sb.append("B-1042");
String code = sb;
```

Java says: `incompatible types: StringBuilder cannot be converted to String`. Fix: `String code = sb.toString();`

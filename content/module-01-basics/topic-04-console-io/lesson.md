## Hook

A shop till that never asks anything is not much use. A good till asks a question, waits for your answer, and then does the sum.

Theo is testing a kiosk at Old Mill. Right now it only talks. It never listens. His ticket asks the kiosk to ask the rider how many minutes they rode, then print the fare. That means your program has to read what a person types. In this lesson you learn how.

## Concept

The **console** is the plain text window where your program prints and where a person can type. Printing is called **output**. Reading what someone types is called **input**.

```callout jargon
**Output** is anything your program shows. **Input** is anything your program receives, such as keys a person types.
```

### Syntax: print and println, a quick recap

```java
System.out.print("How many minutes? ");
System.out.println("Fare: $2.5");
```

- `print` shows the text and stays on the same line.
- `println` shows the text and then moves to a new line.

`print` is handy for a question, because the rider types right after it. Here is the difference in one program:

```java run
public class Main {
    public static void main(String[] args) {
        System.out.print("Riverside ");
        System.out.print("has ");
        System.out.println("12 docks");
        System.out.println("Next line");
    }
}
```

### Syntax: creating a Scanner

A **Scanner** is a ready-made Java class that reads input piece by piece. It lives in a package called `java.util`. A **package** is a named folder of related Java classes. You **import** it at the top of the file so Java knows where to find it.

```java nocompile
import java.util.Scanner;

Scanner in = new Scanner(System.in);
```

- `import java.util.Scanner;` goes above `public class`.
- `new Scanner(System.in)` makes a Scanner that reads from the keyboard. `System.in` is the keyboard's side of the console.
- `in` is just a variable name. You can pick another name.

```callout jargon
`new` builds a fresh object from a class. Here it builds one Scanner that you then use through the variable `in`.
```

```callout analogy
A Scanner is like a cashier reading a shopping list. Each time you ask, it reads the next item and moves its finger along.
```

### Syntax: reading numbers

```java nocompile
int minutes = in.nextInt();
double price = in.nextDouble();
```

- `nextInt()` reads the next whole number.
- `nextDouble()` reads the next decimal number.

### Syntax: reading a whole line

```java nocompile
String station = in.nextLine();
String clean = in.nextLine().trim();
```

- `nextLine()` reads everything up to the end of the line, spaces included.
- `.trim()` removes extra spaces at the start and the end of the text. So `"  Old Mill  "` becomes `"Old Mill"`.

### Syntax: a Scanner that reads from text

You can also give a Scanner some text to read. It treats that text as if someone typed it. `\n` inside a String means "new line", like pressing Enter.

```java nocompile
Scanner in = new Scanner("15\nUptown\n");
```

Tests use this trick, because a test has no person at a keyboard. The snippets below use it too, so they run with one click.

### Syntax: a prompt

A **prompt** is a short question that tells the person what to type. Always print the prompt before you read.

```java nocompile
System.out.println("How many minutes did you ride?");
int minutes = in.nextInt();
```

## Worked example

This program plays the kiosk. The pretend rider types `10`.

```java run
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner("10\n");
        System.out.println("How many minutes did you ride?");
        int minutes = in.nextInt();
        double fare = 1.00 + minutes * 0.15;
        System.out.println("Fare: $" + fare);
    }
}
```

1. The `import` line lets you use `Scanner`.
2. The Scanner gets the text `10` followed by Enter.
3. The prompt prints first, so the rider knows what to type.
4. `nextInt()` reads `10` and stores it in `minutes`.
5. The fare sum from the last topic gives `2.5`.
6. The last line prints `Fare: $2.5`.

On a real kiosk you write `new Scanner(System.in)` instead. Then the program waits until the rider types a number and presses Enter.

## Try it

Change the text inside `new Scanner(...)`. Try `"30\nHarbor\n"`, then try `"30\n  Uptown  \n"` (spaces around Uptown on the second line).

```java run
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner("20\nRiverside\n");
        int minutes = in.nextInt();
        in.nextLine();
        String station = in.nextLine().trim();
        System.out.println("Station: " + station);
        System.out.println("Minutes: " + minutes);
    }
}
```

The lone `in.nextLine();` looks strange. The last mistake below explains why it is there.

```callout tip
Read input in the same order the person types it. A Scanner never goes backwards.
```

## Recap

- `print` stays on the line. `println` moves to a new line.
- Add `import java.util.Scanner;` and create `new Scanner(System.in)` to read the keyboard.
- `nextInt()` reads a whole number, `nextDouble()` a decimal, and `nextLine()` a full line.
- Print a prompt before you read, so the person knows what to type.
- After `nextInt()`, call `nextLine()` once to skip the rest of that line.

## Common mistakes

**Forgetting the import.**

```java nocompile
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
    }
}
```

Java says: `cannot find symbol` and points at `Scanner`. Add `import java.util.Scanner;` at the top of the file, above `public class` and below any `package` line.

**Reading text with nextInt.**

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner("ten\n");
        int minutes = in.nextInt();
    }
}
```

The program crashes with `java.util.InputMismatchException`. `nextInt()` only understands digits such as `10`. Type the number as digits.

**Calling nextLine right after nextInt.**

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner("12\nRiverside\n");
        int minutes = in.nextInt();
        String station = in.nextLine();
        System.out.println("Station: [" + station + "]");
    }
}
```

You see `Station: []`. `nextInt()` reads `12` and stops before the Enter. Then `nextLine()` reads the empty rest of that line. Fix it with one extra `in.nextLine();` straight after `nextInt()`.

## Hook

A shop till that never asks anything is not much use. A good till asks a question, waits for your answer, and then does the sum.

Kakashi is testing a kiosk at Old Mill. Right now it only talks. It never listens. His ticket asks the kiosk to ask the rider how many minutes they rode, then print the fare. That means your program has to read what a person types. In this lesson you learn how.

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

Watch the kiosk ask, the rider answer, and the Scanner carry the answer to your program:

```animate
title: The kiosk asks, the rider answers
kind: scene
nodes:
  - { id: program, label: Your program, emoji: "📄", value: running, col: 0 }
  - { id: scanner, label: Scanner, emoji: "🔎", col: 1 }
  - { id: console, label: Console, emoji: "🖥️", col: 2 }
  - { id: rider, label: Rider, emoji: "🧑", col: 3 }
steps:
  - say: The program prints a prompt first, so the rider knows what to type.
    focus: [program]
    send: { from: program, to: console, label: "How many minutes?" }
    set: { console: "How many minutes?" }
  - say: nextInt() asks the Scanner for a number. The program waits right here until one arrives.
    send: { from: program, to: scanner, label: "nextInt()" }
    set: { program: waiting }
  - say: The rider types 10 and presses Enter. The keys go into System.in, the keyboard side of the console.
    focus: [rider]
    send: { from: rider, to: console, label: "10 ⏎" }
    set: { console: "How many minutes? 10" }
  - say: The Scanner reads from System.in. It picks out the digits 1 and 0 as one whole number.
    focus: [scanner]
    send: { from: console, to: scanner, label: "10⏎" }
    set: { scanner: "10" }
  - say: nextInt() hands back the int 10, and it lands in the minutes box.
    send: { from: scanner, to: program, label: "10", back: true }
    set: { program: "minutes = 10" }
  - say: Now the program does the fare sum and prints the answer.
    focus: [program]
    send: { from: program, to: console, label: "Fare: $2.5" }
    set: { console: "Fare: $2.5" }
```

### The leftover Enter

A Scanner reads the text in order, like a finger moving along it. `nextInt()` stops right after the digits, so the Enter after them is still waiting. Watch where the finger goes (⏎ marks an Enter):

```animate
title: The Enter that nextInt leaves behind
kind: code
code: |
  import java.util.Scanner;
  public class Main {
      public static void main(String[] args) {
          Scanner in = new Scanner("12\nRiverside\n");
          int minutes = in.nextInt();
          String rest = in.nextLine();
          String station = in.nextLine();
          System.out.println(minutes + " min from " + station);
          System.out.println("rest was [" + rest + "]");
      }
  }
steps:
  - line: 4
    objects: { sc: { type: Scanner, fields: { unread: '"12⏎Riverside⏎"' } } }
    vars: { in: "@sc" }
    say: The Scanner gets its text, as if a rider typed 12, pressed Enter, typed Riverside and pressed Enter again.
  - line: 5
    vars: { minutes: 12 }
    objects: { sc: { type: Scanner, fields: { unread: '"⏎Riverside⏎"' } } }
    say: nextInt() reads 12 and stops. The Enter after 12 is still unread.
  - line: 6
    vars: { rest: '""' }
    objects: { sc: { type: Scanner, fields: { unread: '"Riverside⏎"' } } }
    say: nextLine() reads up to the next Enter. Only the leftover Enter was there, so rest is empty text.
  - line: 7
    vars: { station: '"Riverside"' }
    objects: { sc: { type: Scanner, fields: { unread: '""' } } }
    say: Now the finger sits at the start of a fresh line, so this nextLine() gets Riverside.
  - line: 8
    out: 12 min from Riverside
    say: Both values arrived safely, because the extra nextLine() swallowed the leftover Enter.
  - line: 9
    out: "rest was []"
    say: That empty rest is the trap. After nextInt(), call nextLine() once before you read a line of text.
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

The prompt prints first, so the rider knows what to type. Then `nextInt()` reads `10` into `minutes`, and the fare sum from the last topic prints `Fare: $2.5`.

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

The lone `in.nextLine();` looks strange. "The leftover Enter" above shows why it is there.

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

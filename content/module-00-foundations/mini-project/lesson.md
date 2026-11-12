## The design

The design arrived with the ticket. The kiosk printer must print exactly these five lines:

```text
+---------------------------+
| Welcome to Spoke, rider!  |
| Your first 30 minutes are |
| free. Enjoy the ride.     |
+---------------------------+
```

Every line is 29 characters wide. The frame is made of `+`, `-` and `|`.

```callout analogy
Think of the card as a stencil. Each `println` paints one row of the stencil, top to bottom.
```

## What you build

`WelcomeCard.java` has two parts:

- `border()` returns the top and bottom line of the frame.
- `main` prints the whole card, using `border()` twice.

### Syntax: printing a method's text

```java
public class Card {
    static String border() {
        return "+---+";
    }

    public static void main(String[] args) {
        System.out.println(border());
        System.out.println("| A |");
        System.out.println(border());
    }
}
```

```callout tip
Reusing `border()` means a change to the frame happens in one place. That habit matters more and more as programs grow.
```

## Recap

- One `println` per line of output.
- Text in quotes must match the design exactly, spaces included.
- A method that returns text can be printed many times.

## Common mistakes

**Uneven spaces.** The right-hand `|` must line up. Count the spaces at the end of each middle line.

**Typing the border twice.** It works, but a change to the frame would then need two edits. Print `border()` instead, so the frame lives in one place.

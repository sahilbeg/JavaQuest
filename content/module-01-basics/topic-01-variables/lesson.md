## Hook

Picture the back room at the Riverside station. It has shelves full of cardboard boxes. Each box has a label, like "spare bells" or "brake pads". You find things by reading the label, and you can swap what is inside any time.

Right now the kiosk has no boxes at all. A rider unlocks bike B-1042, rides for 20 minutes, and the kiosk forgets everything. Your ticket is to give it boxes with labels, so it can remember a ride.

## Concept

In Java, a labelled box is called a **variable**. A variable has three parts:

- a **type**, which says what kind of thing fits in the box,
- a **name**, which is the label you read,
- a **value**, which is the thing inside.

```callout analogy
A variable is a box with a label. The type is the shape of the box. A box shaped for numbers cannot hold words.
```

This topic uses two types:

- `int` holds a whole number, like `20` or `-3`. It has no decimal point.
- `String` holds text, like `"B-1042"`. Text always goes inside double quotes.

### Syntax: declaring a variable

```java
int minutes;
```

To **declare** a variable means to create the box. You write the type, then the name, then a semicolon. The box exists now, but it is still empty.

More examples:

```java
String bikeId;
String station;
int docks;
```

### Syntax: assigning a value

```java nocompile
minutes = 20;
```

To **assign** means to put a value in the box. In Java, the `=` sign means "store this value in this box". Read `minutes = 20;` as "minutes gets 20".

```java nocompile
bikeId = "B-1042";
station = "Riverside";
docks = 12;
```

### Syntax: declare and assign in one line

```java
int minutes = 20;
```

Most of the time you do both steps at once. Java reads it as: make an int box called minutes, then put 20 in it.

```java
String bikeId = "B-1042";
String station = "Old Mill";
int docks = 12;
```

### Syntax: using a variable

```java nocompile
System.out.println(minutes);
```

Write the name without quotes, and Java uses the value inside the box. With quotes, Java prints the word itself.

```java nocompile
System.out.println(station);   // prints Old Mill
System.out.println("station"); // prints station
```

### Syntax: reassigning a value

```java nocompile
minutes = 35;
```

The value in a variable can change. That is why it is called a variable. To **reassign** means to put a new value in a box that already exists. The old value is thrown away.

Notice there is no `int` in front this time. You only write the type once, when you create the box.

```java
String station = "Harbor";
station = "Uptown";       // the box now holds Uptown
```

### Syntax: returning a variable

```java
public static int rideMinutes() {
    int minutes = 20;
    return minutes;
}
```

You already know `return "some text";` from module 0. You can return a variable too. Java hands back the value inside the box. The method says `int` before its name because it gives back a whole number.

### Naming rules

Java has a few strict rules for names:

- A name can use letters, digits, `_` and `$`.
- A name cannot start with a digit. `1stStation` breaks this rule.
- A name cannot contain a space or a `-`.
- A name cannot be a Java keyword, like `int` or `class`.
- Names are case sensitive. `minutes` and `Minutes` are two different boxes.

Java programmers also follow a habit called **camelCase**. The first word is lowercase. Each new word starts with a capital letter, like the humps on a camel.

```java
int rideMinutes = 20;
String startStation = "Riverside";
String bikeId = "B-1042";
```

```callout tip
Pick names that say what the box holds. `startStation` tells the next reader far more than `s` or `x`.
```

## Worked example

This program remembers one ride:

```java run
public class Main {
    public static void main(String[] args) {
        String bikeId = "B-1042";
        String station = "Riverside";
        int minutes = 20;
        System.out.println(bikeId);
        System.out.println(station);
        System.out.println(minutes);
    }
}
```

1. Line 3 makes a String box called `bikeId` and puts `B-1042` in it.
2. Line 4 makes a String box called `station` holding `Riverside`.
3. Line 5 makes an int box called `minutes` holding `20`.
4. The three `println` lines print the values inside the boxes, one per line.

Now the rider extends the ride. Watch the value change:

```java run
public class Main {
    public static void main(String[] args) {
        int minutes = 20;
        System.out.println(minutes);
        minutes = 35;
        System.out.println(minutes);
    }
}
```

The first `println` prints `20`. Then line 5 reassigns `minutes`, so the second `println` prints `35`. Java runs the lines from top to bottom, so the order matters.

## Try it

Change the bike id to `B-2077` and the station to `Harbor`. Then reassign `minutes` to `42` before the last line. Press **Run**.

```java run
public class Main {
    public static void main(String[] args) {
        String bikeId = "B-1042";
        String station = "Uptown";
        int minutes = 15;
        System.out.println(bikeId);
        System.out.println(station);
        System.out.println(minutes);
    }
}
```

## Recap

- A **variable** is a labelled box with a type, a name and a value.
- `int` holds whole numbers. `String` holds text in double quotes.
- Declare with `int minutes;`, assign with `minutes = 20;`, or do both with `int minutes = 20;`.
- Reassign with `minutes = 35;`. Write the type only once.
- Names use camelCase, cannot start with a digit, and cannot contain spaces.

## Common mistakes

**Putting text in an int box.**

```java nocompile
int bikeId = "B-1042";
```

Java says: `incompatible types: String cannot be converted to int`. Text needs a String box: `String bikeId = "B-1042";`

**Writing the type again when you reassign.**

```java nocompile
int minutes = 20;
int minutes = 35;
```

Java says: `variable minutes is already defined in method main(String[])`. Drop the type on the second line: `minutes = 35;`

**Using a variable before you give it a value.**

```java nocompile
int minutes;
System.out.println(minutes);
```

Java says: `variable minutes might not have been initialized`. Put a value in the box first: `int minutes = 20;`

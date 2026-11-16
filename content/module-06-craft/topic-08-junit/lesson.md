## Hook

Picture a climber on a tall wall. Under the wall hangs a net. The climber still tries not to fall. The net means one slip is not the end of the story.

Spoke's fare code has no net. Maya wants to rewrite billing, but nobody dares touch `StandardFare`. One wrong number could charge every PRO rider for their free hour. In this topic you build the net: small programs that check the fare code for you, every time, in under a second.

## Concept

### What a unit test is

A **unit test** is a small piece of code that calls one piece of your program and checks the answer. The "unit" is usually one method or one class. A test passes when the answer is right. It fails loudly when the answer is wrong.

```callout jargon
A **test framework** is a library that finds your tests, runs them, and reports which passed. **JUnit** is the standard test framework for Java. Its modern part is called **Jupiter**. JUnit 6 keeps the same Jupiter API as JUnit 5, so everything here works in both.
```

You already read tests in earlier topics. Now you write them.

### Syntax: a test method

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FareTest {

    @Test
    void basicClassicTenMinutes() {
        long fare = 100 + 10 * 15;
        assertEquals(250, fare);
    }
}
```

- `@Test` is an annotation. It tells JUnit "run this method as a test".
- The method returns `void` and takes no arguments.
- The class and method do not need to be `public`.
- `import static` lets you write `assertEquals` without the class name in front.

### Syntax: assertEquals

```java nocompile
assertEquals(expected, actual);
assertEquals(expected, actual, "message shown when it fails");
```

An **assertion** is a line that checks one fact. If the fact is false, it throws an `AssertionFailedError`. That error is a kind of `AssertionError`, and JUnit marks the test as failed.

The order matters. The value you expect goes first. The value your code produced goes second.

```java run crash
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Main {
    public static void main(String[] args) {
        long fare = 100 + 10 * 15;
        assertEquals(250, fare, "BASIC classic 10 minutes");
        System.out.println("first check passed");
        long broken = 10 * 15;
        assertEquals(250, broken, "BASIC classic 10 minutes, forgot the unlock fee");
        System.out.println("you never see this line");
    }
}
```

The second assertion throws. The message tells you which ride broke and by how much.

### Syntax: assertTrue, assertFalse, assertThrows, assertAll

```java nocompile
assertTrue(fare > 0, "a BASIC ride always costs something");
assertFalse(fare < 0, "a fare is never negative");
assertThrows(IllegalArgumentException.class, () -> calc.fareCents("classic", -1, "BASIC"));
assertAll(
    () -> assertEquals(400, calc.fareCents("ebike", 10, "BASIC")),
    () -> assertEquals(350, calc.fareCents("cargo", 10, "BASIC")));
```

- `assertTrue` and `assertFalse` check a yes or no fact.
- `assertThrows` runs the lambda and passes only if it throws that exception type. You met lambdas in [functional interfaces](/t/module-03-core-java/topic-13-functional-interfaces).
- `assertAll` runs every check inside it, even after one fails. Then it reports all the failures together.

```java run
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Main {
    public static void main(String[] args) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> fare(-1));
        System.out.println("threw as expected: " + e.getMessage());
    }

    static long fare(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes cannot be negative");
        }
        return 100 + minutes * 15;
    }
}
```

`assertThrows` also hands the exception back, so you can check its message too.

### Arrange, act, assert

Good tests follow three steps, in this order:

1. **Arrange**: set up the objects and inputs.
2. **Act**: call the one method you are testing.
3. **Assert**: check the result.

```java nocompile
@Test
void plusRiderPaysFromMinute31() {
    FareCalculator calc = new StandardFare();          // arrange
    long fare = calc.fareCents("classic", 31, "PLUS"); // act
    assertEquals(15, fare);                            // assert
}
```

```callout tip
Byte says: one test, one behaviour. When a test called plusRiderPaysFromMinute31 fails, you know the bug before you open the file.
```

### Syntax: good names and @DisplayName

A test name should describe the behaviour. `test1` says nothing. `plusRiderPaysFromMinute31` says everything. `@DisplayName` adds a plain sentence that test reports show instead of the method name.

```java nocompile
@Test
@DisplayName("A PLUS rider pays 15 cents for minute 31")
void plusRiderPaysFromMinute31() { ... }
```

### Syntax: @BeforeEach

JUnit makes a fresh object of your test class for every test. A method marked `@BeforeEach` runs before each test. Use it for the "arrange" lines that every test shares.

```java nocompile
class StandardFareTest {
    FareCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new StandardFare();
    }

    @Test
    void cargoCosts25CentsAMinute() {
        assertEquals(350, calc.fareCents("cargo", 10, "BASIC"));
    }
}
```

Each test gets its own clean `calc`. One test cannot spoil the next one.

### Syntax: @ParameterizedTest with @CsvSource

Many fare tests look the same with different numbers. A **parameterized test** is one test method that JUnit runs once per row of data. `@CsvSource` gives the rows as comma-separated text.

```java nocompile
@ParameterizedTest
@CsvSource({
    "classic, 10, BASIC, 250",
    "ebike,   10, BASIC, 400",
    "cargo,   10, BASIC, 350",
    "classic, 31, PLUS,  15"
})
void fareTable(String type, int minutes, String membership, long expected) {
    assertEquals(expected, calc.fareCents(type, minutes, membership));
}
```

JUnit converts each column to the parameter type. This one method becomes four tests in the report.

### Edge cases

An **edge case** is an input at the very limit of a rule. Bugs love edges. For the fare rule, the edges are:

- **Zero**: a 0 minute ride. BASIC still pays the 100 cent unlock fee.
- **Boundaries**: the last free minute and the first paid one. PLUS minute 30 is free; minute 31 costs 15 cents.
- **Negative**: -1 minutes is impossible, so the code should throw.

```callout warning
A test at minute 40 cannot tell 30 free minutes from 29. The fare is 150 cents with one rule and 165 with the other. Only minute 30 versus 31 pins the boundary exactly.
```

## Worked example

Here is a check from the practice task. It takes any `FareCalculator`, so the same check can run against the real class or a broken copy.

```java nocompile
public static void checkAfterFreeMinutes(FareCalculator calc) {
    assertAll(
        () -> assertEquals(15, calc.fareCents("classic", 31, "PLUS"),
                "PLUS, 31 minutes: one paid minute at 15 cents"),
        () -> assertEquals(15, calc.fareCents("classic", 61, "PRO"),
                "PRO, 61 minutes: one paid minute at 15 cents"));
}
```

- Line 1: the check receives the calculator. That is the arrange step done by the caller.
- Line 2: `assertAll` groups two facts, so you see both failures at once.
- Lines 3 and 5: each lambda acts (calls `fareCents`) and asserts (compares) in one line.
- The messages work out the answer in words. A failing report then explains itself.

Now watch a check catch a bug. The program below runs the same check against a correct rule and a broken one.

```java run
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Main {
    public static void main(String[] args) {
        run("correct", new Correct());
        run("off by one", new OffByOne());
    }

    static void check(Fare calc) {
        assertEquals(0, calc.plusClassic(30), "PLUS minute 30 is free");
        assertEquals(15, calc.plusClassic(31), "PLUS minute 31 costs 15 cents");
    }

    static void run(String name, Fare calc) {
        try {
            check(calc);
            System.out.println(name + ": passed");
        } catch (AssertionError e) {
            System.out.println(name + ": FAILED, " + e.getMessage());
        }
    }
}

interface Fare { long plusClassic(int minutes); }

class Correct implements Fare {
    public long plusClassic(int minutes) { return Math.max(0, minutes - 30) * 15; }
}

class OffByOne implements Fare {
    public long plusClassic(int minutes) { return Math.max(0, minutes - 29) * 15; }
}
```

This is the idea behind the hidden tests in your practice task. They run your checks against copies of the fare code with one bug each. Your checks must pass on the real code and fail on every broken copy. A broken copy counts as caught only when one of your assertions fails.

## Try it

Change the check below so it also catches the broken e-bike rate. Add a line with `assertEquals` for a 10 minute e-bike ride. Run it and watch the second line change to FAILED.

```java run
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Main {
    public static void main(String[] args) {
        run("correct", 30);
        run("e-bike priced as classic", 15);
    }

    static long fare(String type, int minutes, long ebikeRate) {
        long perMinute = 15;
        if (type.equals("ebike")) {
            perMinute = ebikeRate;
        }
        return 100 + minutes * perMinute;
    }

    static void run(String name, long ebikeRate) {
        try {
            assertEquals(250, fare("classic", 10, ebikeRate), "classic 10 minutes");
            System.out.println(name + ": passed");
        } catch (AssertionError e) {
            System.out.println(name + ": FAILED, " + e.getMessage());
        }
    }
}
```

## Recap

- A unit test calls one small piece of code and checks the answer with an assertion.
- Mark tests with `@Test`. Check facts with `assertEquals`, `assertTrue`, `assertThrows` and `assertAll`.
- Write each test as arrange, act, assert, with a name or `@DisplayName` that states the behaviour.
- `@BeforeEach` gives every test a fresh setup. `@ParameterizedTest` with `@CsvSource` runs one test over many rows.
- Test the edges: zero, both sides of every boundary, and negative input.

## Common mistakes

**1. Swapping expected and actual.**

```java
long fare = 10 * 15;
org.junit.jupiter.api.Assertions.assertEquals(fare, 250, "BASIC classic 10 minutes");
```

```
org.opentest4j.AssertionFailedError: BASIC classic 10 minutes ==> expected: <150> but was: <250>
```

The code forgot the unlock fee and produced 150. The report claims the code produced 250, which is the opposite of the truth. Fix: put the expected value first, `assertEquals(250, fare, ...)`.

**2. Comparing money as doubles with no tolerance.**

```java
double fare = 0.15 * 3;
org.junit.jupiter.api.Assertions.assertEquals(0.45, fare);
```

```
org.opentest4j.AssertionFailedError: expected: <0.45> but was: <0.44999999999999996>
```

Doubles cannot store 0.15 exactly. Fix: keep money in whole cents as a `long`, like the practice code does. If you must use doubles, pass a tolerance: `assertEquals(0.45, fare, 0.001)`.

**3. Calling the method instead of passing a lambda to assertThrows.**

```java nocompile
assertThrows(IllegalArgumentException.class, calc.fareCents("classic", -1, "BASIC"));
```

```
error: no suitable method found for assertThrows(Class<IllegalArgumentException>,long)
```

The call runs straight away and returns a `long`, so `assertThrows` never gets the chance to catch anything. Fix: wrap the call in a lambda, `() -> calc.fareCents("classic", -1, "BASIC")`.

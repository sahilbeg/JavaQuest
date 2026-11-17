## Hook

Think of the labels on a parcel: "Fragile" and "This side up". The label does not change what is inside the box. It tells the people who handle the box how to treat it.

Jiraiya's ticket has the same shape. The rule "between 1 and 12 docks" lives in a check far away from the setting. People update one and forget the other. If the rule sat on the setting like a label, one small program could read every label and run the checks. Java calls these labels annotations.

## Concept

### What an annotation is

An **annotation** is a label that starts with `@` and sits on a class, a method, a field or a parameter. It adds information about the code. On its own it does nothing. Some other program reads it and acts on it: the compiler, a framework or your own code.

You have met three already:

```java nocompile
@Override                 // the compiler checks that this method really overrides one
public String toString() {
    return "Bike B-1042";
}

@Deprecated               // the compiler warns anyone who still calls this method
public double oldFare(int minutes) {
    return 1.00 + minutes * 0.15;
}

@FunctionalInterface      // the compiler checks there is exactly one abstract method
interface Promotion {
    double apply(String type, double fare);
}
```

All three are read by the compiler. In this topic, you write labels of your own and read them with reflection while the program runs.

### Syntax: declaring your own annotation

```java
@interface NotBlank {
}
```

- `@interface` declares an annotation type. It looks like an interface with an `@` in front.
- This one is empty. Its only message is "I am here".
- Use it like this: `@NotBlank String stationName;`

### Syntax: elements and defaults

```java
@interface Range {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}
```

- An **element** is a setting inside the annotation. It is written like a method with no parameters.
- `default` gives the value used when the label leaves that element out.
- Element types are limited to primitives, `String`, `Class`, enums, other annotations and arrays of these.

```java nocompile
@Range(min = 1, max = 12) int docks;   // both elements set
@Range(min = 1) int minutes;           // max uses its default
```

If an element has the name `value`, you may skip the name: `@Label("Harbor")` means `@Label(value = "Harbor")`.

### Syntax: @Retention and @Target

Annotations can sit on annotations too. Two of them matter most.

```java nocompile
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NotBlank {
}
```

**`@Retention`** says how long the label survives:

| Policy | Kept until | Who can read it |
|---|---|---|
| `SOURCE` | compiling ends | only the compiler |
| `CLASS` | in the `.class` file | tools that read class files (the default) |
| `RUNTIME` | while the program runs | your code, through reflection |

**`@Target`** says where the label may go. `ElementType.FIELD` allows fields only. Other choices include `METHOD`, `TYPE` (a class, interface, record or enum) and `PARAMETER`.

```callout warning
The default retention is `CLASS`. That means reflection cannot see your annotation at run time. Forget `@Retention(RetentionPolicy.RUNTIME)` and your checks find no labels at all, with no error to warn you.
```

### Syntax: reading annotations with reflection

```java nocompile
boolean marked = field.isAnnotationPresent(NotBlank.class);
Range range = field.getAnnotation(Range.class);   // null when the field has no @Range
int low = range.min();
```

- `isAnnotationPresent` answers yes or no.
- `getAnnotation` returns the annotation itself, or `null` when it is missing. You read its elements by calling them like methods.
- `Class`, `Method` and `Field` all have these two methods.

```java run
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class Main {
    public static void main(String[] args) {
        for (Field field : Kiosk.class.getDeclaredFields()) {
            Range range = field.getAnnotation(Range.class);
            if (range == null) {
                System.out.println(field.getName() + ": no range");
            } else {
                System.out.println(field.getName() + ": " + range.min() + " to " + range.max());
            }
        }
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Range {
    int min() default 0;
    int max() default 100;
}

class Kiosk {
    String station = "Harbor";
    @Range(min = 1, max = 12) int docks = 12;
    @Range int brightness = 80;
}
```

`brightness` sets no elements, so it gets both defaults: 0 to 100.

This run shows what retention does. The two annotations are the same except for `@Retention`.

```java run
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Main {
    public static void main(String[] args) {
        System.out.println(Kiosk.class.isAnnotationPresent(Visible.class));
        System.out.println(Kiosk.class.isAnnotationPresent(Hidden.class));
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Visible {
}

@interface Hidden {
}

@Visible
@Hidden
class Kiosk {
}
```

`Hidden` uses the default `CLASS` retention, so reflection reports `false`.

```callout tip
On a record, write the annotation on the part in the header: `record KioskSettings(@NotBlank String stationName)`. Java copies it to the private field, because `@Target(ElementType.FIELD)` allows fields. So `getDeclaredFields()` sees it.
```

### A tiny processor

A **processor** is code that reads annotations and does the work they ask for. Yours runs at run time. It loops over the fields, reads each label and checks the value. That is the whole idea behind validation libraries such as Jakarta Bean Validation, which has its own `@NotBlank`.

### Processors at compile time

Some processors run inside the compiler instead. A **compile-time annotation processor** reads `SOURCE` or `CLASS` annotations while `javac` works. It can report errors or write brand new `.java` files. Lombok works this way: you write `@Getter` on a field and the getter appears without you typing it. You will not write one here. Just know that the labels are the same idea, read at a different time.

## Worked example

This program is a small version of Jiraiya's validator. Each rule sits on its field, and one loop checks them all.

```java run
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IllegalAccessException {
        System.out.println(validate(new Kiosk("Harbor", 12)));
        System.out.println(validate(new Kiosk("", 40)));
    }

    static List<String> validate(Object settings) throws IllegalAccessException {
        List<String> messages = new ArrayList<>();
        for (Field field : settings.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(settings);

            if (field.isAnnotationPresent(NotBlank.class) && value.toString().isBlank()) {
                messages.add(field.getName() + " must not be blank");
            }

            Range range = field.getAnnotation(Range.class);
            if (range != null && value instanceof Integer number) {
                if (number < range.min() || number > range.max()) {
                    messages.add(field.getName() + " must be between " + range.min() + " and " + range.max());
                }
            }
        }
        return messages;
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NotBlank {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Range {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}

record Kiosk(@NotBlank String stationName, @Range(min = 1, max = 12) int docks) {
}
```

Step by step:

1. `getDeclaredFields()` gives the two private fields of the record, `stationName` and `docks`.
2. `field.get(settings)` reads each value. An `int` comes back as an `Integer`.
3. For `@NotBlank`, `isBlank()` is true for an empty text or only spaces.
4. For `@Range`, `instanceof Integer number` checks the type and names the number in one step. Then `range.min()` and `range.max()` give the limits from the label.
5. The first kiosk passes every rule, so its list is empty.

A new setting with a new rule needs only a label. The loop stays the same.

## Try it

Add a third part to the `Kiosk` record: `@Range(min = 5, max = 60) int lockMinutes`. Pass `90` for it in the second call. Press **Run** and look for `lockMinutes must be between 5 and 60`.

Then delete `@Retention(RetentionPolicy.RUNTIME)` from `Range` and run again. Every range message should vanish.

## Recap

- An annotation is a label starting with `@`. Some other code reads it and acts on it.
- `@Override`, `@Deprecated` and `@FunctionalInterface` are read by the compiler.
- `@interface` declares your own annotation. Elements look like methods, and `default` gives a fallback value.
- `@Retention(RetentionPolicy.RUNTIME)` keeps the label for reflection. `@Target` limits where it may go.
- `isAnnotationPresent` and `getAnnotation` read labels from a `Class`, `Method` or `Field`.
- A run-time processor loops over fields and acts on their labels. Compile-time processors, such as Lombok, act while `javac` runs.

## Common mistakes

**Forgetting @Retention.**

```java
@interface NotBlank {
}
```

The code compiles, and the validator prints `[]` for every object, even a blank station name. Without `@Retention(RetentionPolicy.RUNTIME)`, the label is gone at run time. Add the retention line above the `@interface`.

**Leaving out an element that has no default.**

```java nocompile
@interface Range {
    int min();
    int max();
}

class Kiosk {
    @Range(min = 1) int docks;
}
```

Java says: `annotation @Range is missing a default value for the element 'max'`. Set every element that has no default, as in `@Range(min = 1, max = 12)`, or give `max` a `default` in the `@interface`.

**Putting a label where its @Target does not allow it.**

```java nocompile
@Target(ElementType.FIELD)
@interface NotBlank {
}

class Kiosk {
    @NotBlank
    String name() {
        return "Harbor";
    }
}
```

Java says: `annotation interface not applicable to this kind of declaration`. `NotBlank` allows fields only, and `name()` is a method. Move the label to a field, or add `ElementType.METHOD` to the target: `@Target({ElementType.FIELD, ElementType.METHOD})`.

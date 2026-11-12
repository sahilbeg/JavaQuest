## Hook

Picture a mirror in the bike workshop. You hold up any bike, and the mirror shows every part: the battery, the lock, the frame number. You never had to label the bike first.

Sam's ticket asks for that mirror. Support needs to see every field of a `Bike`, a `Station` or a `Ride`. Writing one printer per class means new code every time a class changes. Java has a tool that lets a program look at its own classes while it runs. That tool is called reflection.

## Concept

### What reflection is

**Reflection** means code that inspects classes, fields and methods while the program runs. It can also read fields, call methods and build objects, using only their names as text.

Normal code knows its classes when you compile it. You write `bike.getId()`, and the compiler checks that `getId` exists. Reflection code finds out at run time. It asks the class "what fields do you have?" and gets an answer back.

```callout analogy
Normal code is a mechanic who knows the bike model by heart. Reflection is a mechanic who opens the manual first and reads the parts list.
```

### The Class object

In the classloading topic, you saw that the JVM loads each class once. For each loaded class, it keeps one object of type `Class`. That **Class object** describes the class: its name, its fields, its methods and its constructors.

### Syntax: three ways to get a Class object

```java nocompile
Class<?> a = bike.getClass();                          // from an object
Class<?> b = Bike.class;                               // from the class name in code
Class<?> c = Class.forName("jq.m05.reflection.Bike");  // from text, at run time
```

- `getClass()` works on any object. It gives the class of the real object.
- `Bike.class` works when you know the class while you write the code.
- `Class.forName` takes the **fully qualified name**: the package plus the class name.
- `Class<?>` means "a Class of some type". The `?` is a generics wildcard: you do not say which type.

All three give the very same object, because the JVM keeps one `Class` object per class.

```java run
public class Main {
    public static void main(String[] args) throws Exception {
        Bike bike = new Bike("B-1042", 80);

        Class<?> a = bike.getClass();
        Class<?> b = Bike.class;
        Class<?> c = Class.forName("Bike");

        System.out.println(a.getSimpleName());
        System.out.println(a == b);
        System.out.println(b == c);
    }
}

class Bike {
    private String id;
    private int battery;

    Bike(String id, int battery) {
        this.id = id;
        this.battery = battery;
    }
}
```

This `Bike` has no package, so its full name is just `Bike`. In the practice folder, the full name is `jq.m05.reflection.Bike`.

```callout tip
`throws Exception` on `main` lets the snippets skip try and catch. Many reflection methods throw checked exceptions. Real code catches them, as the practice task does.
```

### Syntax: listing fields, methods and constructors

```java nocompile
Field[] fields = type.getDeclaredFields();
Method[] methods = type.getDeclaredMethods();
Constructor<?>[] constructors = type.getDeclaredConstructors();
```

- These live in the package `java.lang.reflect`, so you import `Field`, `Method` and `Constructor` from there.
- "Declared" means the ones written in this class, including private ones. Inherited ones are left out.
- `getFields()` and `getMethods()` exist too. They give only public members, inherited ones included.

Each `Field` knows its name and its type:

```java nocompile
field.getName();   // "battery"
field.getType();   // int
```

```callout warning
The Java rules do not promise any order for `getDeclaredFields()`. On the standard JVM (HotSpot) you get declaration order in practice. Keep classes simple and do not build a feature that breaks if the order changes.
```

### Syntax: reading a private field

```java nocompile
Field field = Bike.class.getDeclaredField("battery");
field.setAccessible(true);
Object value = field.get(bike);   // 80, boxed as an Integer
```

- `getDeclaredField("battery")` finds one field by name.
- `setAccessible(true)` switches off the `private` check for this `Field` object.
- `field.get(bike)` reads the value from one particular object. It returns `Object`, so an `int` comes back boxed.

### Syntax: the modifiers of a member

```java nocompile
int mods = field.getModifiers();
boolean shared = Modifier.isStatic(mods);
boolean open = Modifier.isPublic(mods);
```

`getModifiers()` returns the keywords such as `public`, `private` and `static`, packed into one number. The class `Modifier` unpacks it for you.

```java run
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Main {
    public static void main(String[] args) throws Exception {
        Bike bike = new Bike("B-1042", 80);

        for (Field field : Bike.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                System.out.println("skip static " + field.getName());
                continue;
            }
            field.setAccessible(true);
            System.out.println(field.getName() + " (" + field.getType().getSimpleName() + ") = " + field.get(bike));
        }
    }
}

class Bike {
    static int bikesBuilt = 0;
    private String id;
    private int battery;

    Bike(String id, int battery) {
        this.id = id;
        this.battery = battery;
    }
}
```

### Syntax: calling a method by its name

```java nocompile
Method method = Bike.class.getMethod("isLow");
Object result = method.invoke(bike);
```

- `getMethod("isLow")` finds a public method with that name and no parameters.
- `invoke(bike)` calls it on the object `bike`. The result comes back as `Object`.
- For a method with parameters, list the parameter types: `getMethod("charge", int.class)`. Then pass the values: `method.invoke(bike, 20)`.

### Syntax: creating an object from a class name

```java nocompile
Class<?> type = Class.forName("Station");
Object station = type.getDeclaredConstructor().newInstance();
```

- `getDeclaredConstructor()` with no arguments finds the no-arg constructor.
- `newInstance()` calls that constructor and returns the new object.
- If the class has no no-arg constructor, you get a `NoSuchMethodException`.

```java run
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws Exception {
        Object station = Class.forName("Station").getDeclaredConstructor().newInstance();

        Method name = station.getClass().getMethod("getName");
        Method free = station.getClass().getMethod("freeDocks");
        System.out.println(name.invoke(station));
        System.out.println(free.invoke(station));

        Method park = station.getClass().getMethod("park", int.class);
        park.invoke(station, 5);
        System.out.println(free.invoke(station));
    }
}

class Station {
    private String name = "Riverside";
    private int docks = 12;
    private int bikesDocked = 0;

    public String getName() {
        return name;
    }

    public int freeDocks() {
        return docks - bikesDocked;
    }

    public void park(int bikes) {
        bikesDocked = bikesDocked + bikes;
    }
}
```

### Syntax: the parts of a record

```java nocompile
for (RecordComponent part : Ride.class.getRecordComponents()) {
    System.out.println(part.getName() + " = " + part.getAccessor().invoke(ride));
}
```

A record lists its parts in its header. `getRecordComponents()` gives them back in that order, and the order is guaranteed. `getAccessor()` returns the `Method` that reads the part, such as `minutes()`. `RecordComponent` lives in `java.lang.reflect`.

### The costs of reflection

Reflection is powerful, and it has three real costs:

- **It is slower.** The JVM must look things up by name and check access on every call. A normal method call skips all that.
- **It breaks encapsulation.** **Encapsulation** means a class hides its fields behind methods. `setAccessible(true)` walks straight past `private`.
- **Mistakes show up at run time.** Misspell `"isLow"` and the compiler says nothing. The program crashes later with `NoSuchMethodException`.

So use reflection for tools that must work on any class: debug screens, test runners, settings loaders. For everyday code, call methods the normal way.

Frameworks use reflection all the time. A **framework** is a big library that calls your code. JUnit finds every method marked `@Test` with reflection and calls it. Spring builds your objects with reflection and fills in their fields.

## Worked example

This program is a small version of Sam's debug screen. One method describes a plain class, and another describes a record.

```java run
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println(describe(new Bike("B-1042", 80)));
        System.out.println(describeRecord(new Ride("B-1042", "Old Mill", "ebike", 25)));
    }

    static String describe(Object o) throws IllegalAccessException {
        List<String> parts = new ArrayList<>();
        for (Field field : o.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            parts.add(field.getName() + "=" + field.get(o));
        }
        return o.getClass().getSimpleName() + "{" + String.join(", ", parts) + "}";
    }

    static String describeRecord(Record r) throws Exception {
        List<String> parts = new ArrayList<>();
        for (RecordComponent part : r.getClass().getRecordComponents()) {
            parts.add(part.getName() + "=" + part.getAccessor().invoke(r));
        }
        return r.getClass().getSimpleName() + "[" + String.join(", ", parts) + "]";
    }
}

class Bike {
    static int bikesBuilt = 0;
    private String id;
    private int battery;

    Bike(String id, int battery) {
        this.id = id;
        this.battery = battery;
    }
}

record Ride(String bikeId, String station, String type, int minutes) {
}
```

Step by step:

1. `o.getClass()` gives the `Class` object of whatever came in.
2. The loop visits each declared field and skips `bikesBuilt`, because it is static. A static field belongs to the whole class, so it says nothing about this one bike.
3. `setAccessible(true)` lets `get` read the private fields.
4. `String.join(", ", parts)` glues the pieces together with a comma and a space.
5. `describeRecord` takes a `Record`, the parent type of every record. It reads each part through its accessor method.

Notice that `describe` never mentions `id` or `battery`. Add a field to `Bike` and the output grows by itself.

## Try it

Add a field `private String station = "Harbor";` to `Bike` in the worked example, below `battery`. Press **Run**. The first line should now end with `station=Harbor}`.

Then add `System.out.println(Bike.class.getDeclaredConstructors().length);` to `main`. Predict the number before you run it.

## Recap

- Reflection lets code inspect and use classes while the program runs.
- Get a `Class` object with `getClass()`, `Bike.class` or `Class.forName("full.Name")`.
- `getDeclaredFields`, `getDeclaredMethods` and `getDeclaredConstructors` list what a class declares, private ones included.
- `setAccessible(true)` then `field.get(o)` reads a private field. `method.invoke(o)` calls a method.
- `getDeclaredConstructor().newInstance()` builds an object. `getRecordComponents()` lists the parts of a record.
- Reflection is slower, breaks encapsulation and fails at run time. Frameworks such as JUnit and Spring use it anyway, because they must work on classes they have never seen.

## Common mistakes

**Reading a private field without setAccessible.**

```java nocompile
Field field = Bike.class.getDeclaredField("battery");
Object value = field.get(bike);
```

Java says: `java.lang.IllegalAccessException: class Main cannot access a member of class Bike with modifiers "private"`. Call `field.setAccessible(true)` before `field.get(bike)`.

**Forgetting the package in Class.forName.**

```java nocompile
Class<?> type = Class.forName("Bike");   // the class lives in jq.m05.reflection
```

Java says: `java.lang.ClassNotFoundException: Bike`. `Class.forName` needs the fully qualified name: `Class.forName("jq.m05.reflection.Bike")`. `getName()` on a `Class` object shows you that full name.

**Ignoring the checked exceptions.**

```java nocompile
public static void main(String[] args) {
    Class<?> type = Class.forName("Bike");
}
```

Java says: `unreported exception ClassNotFoundException; must be caught or declared to be thrown`. Wrap the call in `try` and `catch (ReflectiveOperationException e)`, or add `throws Exception` to the method. `ReflectiveOperationException` is the parent of `ClassNotFoundException`, `NoSuchMethodException` and their siblings.

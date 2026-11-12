## Hook

When you order a pizza, the shop asks for your address before it starts cooking. It does not bake the pizza first and hope someone writes the address on the box later.

Right now the kiosk builds a bike with `new Bike()` and fills in the fields afterwards. If someone forgets the `id`, the bike's `id` stays `null` and the kiosk crashes. This page shows you how to ask for the basics at the moment the object is built.

## Concept

```callout jargon
A **constructor** is a special block of code that runs once, when `new` builds an object. Its job is to set up the object's fields.
```

### Syntax: a constructor

```java
class Bike {
    String id;
    String type;

    Bike(String newId, String newType) {
        id = newId;
        type = newType;
    }
}
```

- The constructor has the **same name as the class**: `Bike`.
- It has **no return type**, not even `void`.
- Its parameters receive the values you pass to `new`.

```java nocompile
Bike bike = new Bike("B-1042", "classic");
System.out.println(bike.id);   // B-1042
```

Now the id goes in on the same line that builds the bike. You cannot forget it.

### Syntax: this

Programmers like to give the parameter the same name as the field. That creates a clash: which `id` is which?

```java
class Bike {
    String id;
    String type;

    Bike(String id, String type) {
        this.id = id;
        this.type = type;
    }
}
```

```callout jargon
**this** means "the object being built or used right now". `this.id` is the field. Plain `id` is the parameter.
```

Read `this.id = id;` as "copy the parameter `id` into this object's `id` field".

### Syntax: constructor overloading

A class can have several constructors, as long as their parameter lists differ. This is **overloading**, the same idea you met with methods.

```java
class Station {
    String name;
    int docks;

    Station(String name) {
        this.name = name;
        this.docks = 12;
    }

    Station(String name, int docks) {
        this.name = name;
        this.docks = docks;
    }
}
```

```java nocompile
Station a = new Station("Riverside");      // 12 docks
Station b = new Station("Depot", 20);      // 20 docks
```

Spoke's repair depot is bigger than a rider station, so it uses the two-number constructor.

Java picks the constructor whose parameters match the arguments you pass.

### Syntax: chaining with this(...)

The two `Station` constructors above repeat the same line. One constructor can call another with `this(...)`.

```java
class Station {
    String name;
    int docks;

    Station(String name) {
        this(name, 12);
    }

    Station(String name, int docks) {
        this.name = name;
        this.docks = docks;
    }
}
```

- `this(name, 12)` runs the two-parameter constructor.
- It must be the **first line** of the constructor.
- The real setup now lives in one place, so a fix happens once.

```callout warning
Writing any constructor removes the free one. If you never write a constructor, Java gives you an empty one, so `new Bike()` works. Once you write `Bike(String id, String type)`, plain `new Bike()` stops compiling.
```

```mermaid
flowchart LR
  A["new Bike(&quot;B-1042&quot;)"] --> B["Bike(String id)"]
  B -- "this(id, &quot;classic&quot;)" --> C["Bike(String id, String type)"]
  C --> D["fields set"]
```

## Worked example

```java run
public class Main {
    public static void main(String[] args) {
        Bike a = new Bike("B-1042", "ebike");
        Bike b = new Bike("B-2001");

        System.out.println(a.id + " is " + a.type);
        System.out.println(b.id + " is " + b.type);
    }
}

class Bike {
    String id;
    String type;

    Bike(String id, String type) {
        this.id = id;
        this.type = type;
    }

    Bike(String id) {
        this(id, "classic");
    }
}
```

1. `new Bike("B-1042", "ebike")` has two arguments, so Java runs the two-parameter constructor.
2. That constructor copies both values into the fields with `this.id = id;` and `this.type = type;`.
3. `new Bike("B-2001")` has one argument, so Java runs `Bike(String id)`.
4. Its first line, `this(id, "classic")`, hands the work to the two-parameter constructor.
5. The output is `B-1042 is ebike`, then `B-2001 is classic`.

## Try it

Add a second station, Old Mill, using `new Station("Old Mill")`. Print how many docks are free when 5 bikes are docked. You should see 7.

```java run
public class Main {
    public static void main(String[] args) {
        Station s = new Station("Harbor");
        System.out.println(s.name + ": " + s.freeDocks(9) + " free");
    }
}

class Station {
    String name;
    int docks;

    Station(String name) {
        this(name, 12);
    }

    Station(String name, int docks) {
        this.name = name;
        this.docks = docks;
    }

    int freeDocks(int bikesDocked) {
        return docks - bikesDocked;
    }
}
```

```callout tip
A constructor is the place to set every field the object cannot live without. Then no one can build a half-finished object.
```

## Recap

- A **constructor** has the class name, no return type, and runs once when `new` builds the object.
- `this.field = field;` copies a parameter into the field with the same name.
- A class can have several constructors with different parameters: **overloading**.
- `this(...)` calls another constructor of the same class. It must be the first line.
- Once you write any constructor, Java stops giving you the empty default one.

## Common mistakes

**Forgetting this, so the field stays empty.**

```java
class Bike {
    String id;

    Bike(String id) {
        id = id;
    }
}
```

It compiles, but `new Bike("B-1042").id` prints `null`. The line copies the parameter into itself. Write `this.id = id;`.

**Calling new Bike() after writing a constructor.**

```java nocompile
Bike bike = new Bike();
```

Java says: `constructor Bike in class Bike cannot be applied to given types;`. The free empty constructor is gone. Pass the values it asks for: `new Bike("B-1042", "classic")`.

**Putting this(...) after another line.**

```java nocompile
Bike(String id) {
    System.out.println("New bike");
    this(id, "classic");
}
```

Java says: `call to this must be first statement in constructor`. Move `this(id, "classic");` to the top.

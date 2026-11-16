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

Read `this.id = id;` as "copy the parameter `id` into this object's `id` field". Watch a constructor fill in a new bike:

```animate
title: A constructor builds a bike
kind: code
code: |
  public class Main {
      public static void main(String[] args) {
          Bike bike = new Bike("B-1042", "classic");
          System.out.println(bike.id + " is " + bike.type);
      }
  }
  class Bike {
      String id;
      String type;
      Bike(String id, String type) {
          this.id = id;
          this.type = type;
      }
  }
steps:
  - line: 3
    objects: { b1: { type: Bike, fields: { id: null, type: null } } }
    say: new first makes an empty Bike object. Both fields start as null.
  - line: 10
    vars: { this: "@b1", id: '"B-1042"', type: '"classic"' }
    say: Then the constructor runs. this points to the new object. The parameters hold the two values passed to new.
  - line: 11
    objects: { b1: { type: Bike, fields: { id: '"B-1042"', type: null } } }
    say: this.id = id copies the parameter into the object's id field.
  - line: 12
    objects: { b1: { type: Bike, fields: { id: '"B-1042"', type: '"classic"' } } }
    say: this.type = type fills the second field. The bike is complete.
  - line: 13
    vars: { this: ~, id: ~, type: ~ }
    say: The constructor ends. Its parameters and this disappear, but the object stays.
  - line: 3
    vars: { bike: "@b1" }
    say: new hands back the arrow, so bike points to a finished bike. No field was left null.
  - line: 4
    out: B-1042 is classic
    say: println reads both fields through the arrow.
```

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

```animate
title: One constructor hands over to another
kind: code
code: |
  public class Main {
      public static void main(String[] args) {
          Station s = new Station("Riverside");
          System.out.println(s.name + ": " + s.docks + " docks");
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
  }
steps:
  - line: 3
    objects: { s1: { type: Station, fields: { name: null, docks: 0 } } }
    say: new gets one value, "Riverside". Java makes an empty Station object first.
  - line: 10
    vars: { this: "@s1", name: '"Riverside"' }
    say: One argument, so Java picks the constructor with one parameter.
  - line: 11
    say: this(name, 12) hands the job to the two-parameter constructor, passing 12 for the docks.
  - line: 13
    vars: { docks: 12 }
    say: The two-parameter constructor starts. Its parameters hold "Riverside" and 12.
  - line: 14
    objects: { s1: { type: Station, fields: { name: '"Riverside"', docks: 0 } } }
    say: The real setup lives here. this.name gets "Riverside".
  - line: 15
    objects: { s1: { type: Station, fields: { name: '"Riverside"', docks: 12 } } }
    say: this.docks gets 12. Every field is set.
  - line: 3
    vars: { this: ~, name: ~, docks: ~, s: "@s1" }
    say: Both constructors finish. Their parameters disappear, and s points to the finished station.
  - line: 4
    out: "Riverside: 12 docks"
    say: println shows the 12 docks the first constructor chose.
```

### The free constructor disappears

```callout warning
Writing any constructor removes the free one. If you never write a constructor, Java gives you an empty one, so `new Bike()` works. Once you write `Bike(String id, String type)`, plain `new Bike()` stops compiling.
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

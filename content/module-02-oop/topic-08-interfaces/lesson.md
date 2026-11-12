## Hook

Think about a phone charger. The wall socket does not care what you plug in. A phone, a speaker or a lamp all work. Each one just has to fit the plug shape.

Spoke's charging dock is pickier. It only knows how to charge an `EBike`. The new electric scooters roll up, and the dock ignores them. Scooter S-7 sat at Harbor all night and woke up at 4%. Your ticket is to give the dock a plug shape that any battery vehicle can fit.

## Concept

Putting `charge()` in `Vehicle` would be wrong, because a classic bike has no battery. A scooter is not one of Spoke's bikes either. You need a type for "has a battery" that cuts across classes. Java has a tool for "these different things can all do the same job".

```callout jargon
An **interface** is a list of methods that a class promises to have. It describes what a thing can do. It says nothing about what the thing is.
```

### Syntax: declaring an interface

```java
interface Chargeable {
    int batteryLevel();
    void charge(int percent);
}
```

Write `interface` where you would write `class`. Each method has no body, just like an abstract method. You do not need the word `abstract`. Every method without a body in an interface is abstract on its own.

### Syntax: implements

```java nocompile
class EBike implements Chargeable {
    private int battery;

    public int batteryLevel() {
        return battery;
    }

    public void charge(int percent) {
        battery = battery + percent;
    }
}
```

`implements Chargeable` means "EBike keeps the Chargeable promise". The class must write a body for every method in the interface. Mark those methods `public`, because interface methods are always public.

A scooter makes the same promise in its own way:

```java nocompile
class Scooter implements Chargeable {
    private int battery;

    public int batteryLevel() {
        return battery;
    }

    public void charge(int percent) {
        battery = battery + percent;
    }
}
```

### Syntax: an interface type as a variable or parameter

You cannot write `new Chargeable()`. An interface is not a complete class, so you cannot create one directly. You can still use it as a type.

```java nocompile
Chargeable c = new Scooter();
c.charge(30);
```

A parameter can have an interface type too. The method then accepts any object that keeps the promise.

```java nocompile
static void topUp(Chargeable item) {
    item.charge(50);
}
```

`topUp(new EBike())` and `topUp(new Scooter())` both work. The dock no longer needs to know the exact class.

```callout analogy
An interface is like a "USB" sticker on a device. The computer does not care whether the device is a mouse or a keyboard. It only checks for the sticker.
```

### Syntax: default methods

```java
interface Chargeable {
    int batteryLevel();
    void charge(int percent);

    default boolean needsCharge() {
        return batteryLevel() < 20;
    }
}
```

A **default method** is an interface method with a body. It starts with the word `default`. Every class that implements the interface gets it for free. Notice that it calls `batteryLevel()`, which each class supplies. That is the same template idea from abstract classes.

A class can still write its own `needsCharge()` to replace the default one.

### Syntax: implementing several interfaces

A class can extend only one parent class. It can implement as many interfaces as it likes. Separate them with commas.

```java nocompile
interface Lockable {
    void lock();
}

class EBike extends Vehicle implements Chargeable, Lockable {
    // must write pricePerMinute, kind, batteryLevel, charge and lock
}
```

### extends or implements?

- `extends` says what a class **is**. An `EBike` is a `Vehicle`. It inherits fields, constructors and method bodies. You get one parent only.
- `implements` says what a class **can do**. An `EBike` can be charged. An interface has no fields to inherit. You can implement many interfaces.

```callout tip
A quick test: if you can say "X is a Y", use extends. If you can say "X can do Y", use an interface. Interface names often end in "able" for that reason.
```

## Worked example

Here is a dock that charges both kinds of vehicle.

```java run
public class Main {
    public static void main(String[] args) {
        Chargeable[] dock = { new EBike(90), new Scooter(4) };
        for (Chargeable item : dock) {
            System.out.println(item.batteryLevel() + " needs charge: " + item.needsCharge());
            item.charge(30);
        }
        for (Chargeable item : dock) {
            System.out.println(item.batteryLevel());
        }
    }
}

interface Chargeable {
    int batteryLevel();
    void charge(int percent);

    default boolean needsCharge() {
        return batteryLevel() < 20;
    }
}

class EBike implements Chargeable {
    private int battery;

    EBike(int battery) {
        this.battery = battery;
    }

    public int batteryLevel() {
        return battery;
    }

    public void charge(int percent) {
        battery = battery + percent;
        if (battery > 100) {
            battery = 100;
        }
    }
}

class Scooter implements Chargeable {
    private int battery;

    Scooter(int battery) {
        this.battery = battery;
    }

    public int batteryLevel() {
        return battery;
    }

    public void charge(int percent) {
        battery = battery + percent;
        if (battery > 100) {
            battery = 100;
        }
    }
}
```

It prints:

```text
90 needs charge: false
4 needs charge: true
100
34
```

Line by line:

- `interface Chargeable` lists two promised methods and one default method.
- `EBike` and `Scooter` both say `implements Chargeable` and write `batteryLevel()` and `charge()`.
- `charge` stops at 100, so the e-bike goes from 90 to 100 and no higher.
- `Chargeable[] dock` holds both kinds of object in one array.
- `item.needsCharge()` runs the default method. It calls each object's own `batteryLevel()`.

## Try it

Add a third class, `Phone`, that also implements `Chargeable`. Put a `new Phone(10)` in the array and run it again.

```java run
public class Main {
    public static void main(String[] args) {
        Chargeable s = new Scooter(15);
        System.out.println(s.needsCharge());
        s.charge(10);
        System.out.println(s.needsCharge());
    }
}

interface Chargeable {
    int batteryLevel();
    void charge(int percent);

    default boolean needsCharge() {
        return batteryLevel() < 20;
    }
}

class Scooter implements Chargeable {
    private int battery;

    Scooter(int battery) {
        this.battery = battery;
    }

    public int batteryLevel() {
        return battery;
    }

    public void charge(int percent) {
        battery = battery + percent;
    }
}
```

## Recap

- An interface is a list of methods a class promises to have.
- A class keeps the promise with `implements` and writes every method as `public`.
- A class can implement several interfaces, but it extends only one class.
- An interface type works as a variable, array or parameter type. It accepts any class that implements it.
- A default method has a body, and every implementing class gets it for free.

## Common mistakes

**Forgetting `public` on an interface method.**

```java nocompile
interface Chargeable {
    int batteryLevel();
}

class Scooter implements Chargeable {
    int batteryLevel() {
        return 50;
    }
}
```

Java says: `batteryLevel() in Scooter cannot implement batteryLevel() in Chargeable` with `attempting to assign weaker access privileges; was public`. Interface methods are always public. Write `public int batteryLevel()`.

**Using extends with an interface.**

```java nocompile
interface Chargeable {
    int batteryLevel();
}

class Scooter extends Chargeable {
    public int batteryLevel() {
        return 50;
    }
}
```

Java says: `no interface expected here`. A class extends a class and implements an interface. Write `class Scooter implements Chargeable`.

**Creating an object from an interface.**

```java nocompile
Chargeable c = new Chargeable();
```

Java says: `Chargeable is abstract; cannot be instantiated`. An interface is not a complete class, so you cannot create one directly. Create a class that implements it: `Chargeable c = new Scooter(15);`

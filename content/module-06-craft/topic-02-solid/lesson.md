## Hook

Picture a power strip. You plug in a lamp, a phone charger, then a fan. You never open the strip and rewire it to add the fan. The plug shape is the agreement, and anything with that shape fits.

Spoke's `PaymentProcessor` is the opposite. Every new payment method means opening the class and adding a case to a switch. Kakashi's ticket says the wallet change broke card payments for a whole morning. In this topic you learn five design rules that turn code into a power strip.

## Concept

```callout jargon
**SOLID** is a set of five design principles. A **principle** is a rule of thumb, not a law. The five letters stand for Single responsibility, Open/closed, Liskov substitution, Interface segregation and Dependency inversion.
```

Each rule fights one kind of pain: a change in one place that breaks something far away.

### S: Single responsibility

The **single responsibility principle** says a class should have one reason to change. A **reason to change** is a person or team who might ask for a change.

This class has three reasons to change:

```java
class RideService {
    long fareCents(int minutes) { return 100 + minutes * 15; }
    String receiptText(long cents) { return "Paid " + cents + " cents"; }
    void saveToDatabase(long cents) { }
}
```

Jiraiya might change prices. Itachi might change the receipt wording. Someone else might move the database. Split it so each class has one owner.

```java
class FareCalculator {
    long fareCents(int minutes) { return 100 + minutes * 15; }
}
class ReceiptPrinter {
    String receiptText(long cents) { return "Paid " + cents + " cents"; }
}
```

### O: Open/closed

The **open/closed principle** says code should be open for extension and closed for modification. You add new behaviour by adding new code. You do not edit code that already works.

A switch on a string breaks this rule. Every new case means editing the method.

```java nocompile
switch (method) {
    case "card": return "card: charged " + cents + " cents";
    case "wallet": return "wallet: charged " + cents + " cents";
}
```

The fix is an interface. Each payment method becomes its own class. A new method is a new class, and the old classes stay closed.

### Syntax: one interface, many classes

```java
interface PaymentMethod {
    String name();
    String pay(long cents);
}

class CardPayment implements PaymentMethod {
    public String name() { return "card"; }
    public String pay(long cents) { return "card: charged " + cents + " cents"; }
}
```

Interface methods are public, so the class must write `public` on each one. You saw that rule in module 2.

```java run
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<PaymentMethod> methods = List.of(new CardPayment(), new WalletPayment());
        for (PaymentMethod method : methods) {
            System.out.println(method.pay(250));
        }
    }
}

interface PaymentMethod {
    String name();
    String pay(long cents);
}

class CardPayment implements PaymentMethod {
    public String name() { return "card"; }
    public String pay(long cents) { return "card: charged " + cents + " cents"; }
}

class WalletPayment implements PaymentMethod {
    public String name() { return "wallet"; }
    public String pay(long cents) { return "wallet: charged " + cents + " cents"; }
}
```

The loop never asks which kind it holds. That is polymorphism doing the work.

### L: Liskov substitution

The **Liskov substitution principle** says a subtype must work anywhere its parent type works. A **subtype** is a class that extends a class or implements an interface. If code works with a `PaymentMethod`, it must work with every class that implements it.

Here is a classic broken subtype. A transit pass cannot give money back, so its author throws an exception.

```java run crash
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Refundable> methods = List.of(new CardRefund(), new TransitRefund());
        for (Refundable method : methods) {
            System.out.println(method.refund(250));
        }
    }
}

interface Refundable {
    String refund(long cents);
}

class CardRefund implements Refundable {
    public String refund(long cents) { return "card: refunded " + cents + " cents"; }
}

class TransitRefund implements Refundable {
    public String refund(long cents) {
        throw new UnsupportedOperationException("transit passes cannot refund");
    }
}
```

The loop trusted the interface and crashed. `TransitRefund` promised to refund, then refused. The fix is to stop promising: a transit pass should not implement `Refundable` at all.

```callout warning
A method that throws `UnsupportedOperationException` from an interface you chose to implement is a red flag. It usually means the class is in the wrong family.
```

### I: Interface segregation

The **interface segregation principle** says no class should be forced to write methods it does not use. Prefer several small interfaces over one big one.

```java nocompile
interface KioskDevice {
    void printReceipt(String text);
    void scanCard(String cardId);
    boolean isDocked(String bikeId);
}
```

A dock sensor at Harbor only knows about docks. With this interface, it must still fake `printReceipt` and `scanCard`. Split it instead.

```java
interface ReceiptPrinter { void printReceipt(String text); }
interface CardReader { void scanCard(String cardId); }
interface DockSensor { boolean isDocked(String bikeId); }
```

A class can implement as many small interfaces as it needs: `class Kiosk implements ReceiptPrinter, CardReader`.

### D: Dependency inversion

The **dependency inversion principle** says high-level code should depend on an interface, not on a specific class. A **dependency** is any object a class needs to do its job.

Here the checkout creates its own card payment. It can never use anything else.

```java nocompile
class Checkout {
    private final CardPayment card = new CardPayment();
}
```

Here the checkout asks for any `PaymentMethod` through its constructor. This is called **constructor injection**: the caller hands the dependency in.

```java run
public class Main {
    public static void main(String[] args) {
        Checkout atHarbor = new Checkout(new WalletPayment());
        System.out.println(atHarbor.finish(400));
    }
}

class Checkout {
    private final PaymentMethod payment;

    Checkout(PaymentMethod payment) {
        this.payment = payment;
    }

    String finish(long cents) {
        return payment.pay(cents);
    }
}

interface PaymentMethod {
    String name();
    String pay(long cents);
}

class WalletPayment implements PaymentMethod {
    public String name() { return "wallet"; }
    public String pay(long cents) { return "wallet: charged " + cents + " cents"; }
}
```

```callout analogy
A wall socket is dependency inversion. The house does not know about your lamp. It only offers the plug shape, and the lamp is handed in when you plug it.
```

## Worked example

Now put the rules together. The processor below gets its payment methods through the constructor and routes by name.

```java run
import java.util.List;

public class Main {
    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor(
                List.of(new CardPayment(), new TransitPassPayment()));
        System.out.println(processor.pay("card", 250));
        System.out.println(processor.pay("transit", 150));
    }
}

class PaymentProcessor {
    private final List<PaymentMethod> methods;

    PaymentProcessor(List<PaymentMethod> methods) {
        this.methods = methods;
    }

    String pay(String method, long cents) {
        for (PaymentMethod candidate : methods) {
            if (candidate.name().equals(method)) {
                return candidate.pay(cents);
            }
        }
        throw new IllegalArgumentException("unknown payment method: " + method);
    }
}

interface PaymentMethod {
    String name();
    String pay(long cents);
}

class CardPayment implements PaymentMethod {
    public String name() { return "card"; }
    public String pay(long cents) { return "card: charged " + cents + " cents"; }
}

class TransitPassPayment implements PaymentMethod {
    public String name() { return "transit"; }
    public String pay(long cents) { return "transit: charged " + cents + " cents"; }
}
```

Line by line:

1. `PaymentMethod` is the plug shape. Every payment class implements it.
2. The constructor receives the list. That is dependency inversion: `PaymentProcessor` never writes `new CardPayment()`.
3. `pay` loops over the list and compares names. It has no switch, so it stays closed to edits.
4. The last line is a guard for names nobody registered. It throws with a clear message.
5. Adding `TransitPassPayment` touched no existing class. That is open/closed in action.

The output is `card: charged 250 cents`, then `transit: charged 150 cents`.

## Try it

Add a fourth payment method to the worked example above:

1. Write a class `GiftCardPayment` that implements `PaymentMethod`, with the name `gift`.
2. Add `new GiftCardPayment()` to the list in `main`.
3. Call `processor.pay("gift", 300)` and print it.

Check that you did not change one line inside `PaymentProcessor`. Then try `processor.pay("cash", 100)` and read the exception message.

## Recap

- Single responsibility: one class, one reason to change.
- Open/closed: add behaviour with new classes, and leave working classes alone.
- Liskov substitution: every subtype must keep the parent's promises.
- Interface segregation: many small interfaces beat one big one.
- Dependency inversion: depend on an interface, and receive it through the constructor.

## Common mistakes

**1. Forgetting `public` on an interface method.**

```java nocompile
interface PaymentMethod {
    String pay(long cents);
}

class CardPayment implements PaymentMethod {
    String pay(long cents) { return "card: charged " + cents + " cents"; }
}
```

```
Main.java:6: error: pay(long) in CardPayment cannot implement pay(long) in PaymentMethod
    String pay(long cents) { return "card: charged " + cents + " cents"; }
           ^
  attempting to assign weaker access privileges; was public
```

Interface methods are always public. Fix: write `public String pay(long cents)`.

**2. Comparing names with `==`.**

```java
public class Main {
    public static void main(String[] args) {
        String wanted = new String("card");
        String name = "card";
        System.out.println(name == wanted);
    }
}
```

```
false
```

`==` checks whether two variables point at the same object. A name typed at a kiosk is usually a different object. The routing loop then finds nothing and throws. Fix: use `name.equals(wanted)`.

**3. Creating the dependency inside the class.**

```java nocompile
class PaymentProcessor {
    private final List<PaymentMethod> methods = List.of(new CardPayment(), new WalletPayment());
}
```

This compiles and runs. But the list is fixed forever, so adding the transit pass means editing `PaymentProcessor` again. That is the exact pain from Kakashi's ticket. Fix: take `List<PaymentMethod> methods` as a constructor parameter and store it.

## Hook

When you travel abroad, your phone charger does not change. You buy a small plug adapter for each country. The phone only knows its own plug, and the adapter deals with the strange wall socket.

Spoke is switching payment providers, to PayRiver. In the old code, the old provider's JSON fields and status codes are mixed right into the fare logic. Changing provider means rewriting business rules. In this ticket you give the business code its own plug shape, and push PayRiver out to an adapter.

## Concept

### What layers leave behind

In [Layered architecture](/t/module-09-architecture/topic-01-layered-architecture), dependencies point down: web, then service, then persistence. That fixed the god controller. But look at where the arrows end.

```text
RentalController  →  RentalService  →  RentalRepository (persistence)  →  database
                                   →  OldPayClient     (the provider's library)
```

The service, which holds the most important rules, depends on the most technical parts: storage and a provider's library. Change the provider, and the service changes. Upgrade the database library, and the domain may change too.

```callout jargon
The **core** (or **domain core**) is the code that holds the business rules: fares, rentals, what happens when a ride ends. **Infrastructure** is everything technical around it: databases, web frameworks, payment and SMS providers.
```

### The old payment code

Here is a shortened version of the old "end a ride" code:

```java nocompile
public Receipt endRide(String rentalId) {
    Rental rental = repository.findById(rentalId).orElseThrow();
    long fare = 100 + 30 * minutes(rental);
    JSONObject body = new JSONObject();
    body.put("amount_minor", fare);                       // the old provider's field name
    body.put("ccy", "USD");
    JSONObject reply = oldPayClient.post("/v2/charges", body);
    if (reply.getInt("code") == 0 || reply.getInt("code") == 7) {   // 7 meant "approved after check"
        rental.markPaid();
    }
    // ...
}
```

The fare rule and the provider's codes sit in one method. Nobody remembers what code 7 means. To move to PayRiver, which uses different fields and different status words, someone must edit the business logic and hope nothing else breaks.

### Ports and adapters

In 2005, Alistair Cockburn described a fix called **hexagonal architecture**, also known as **ports and adapters**.

```callout jargon
A **port** is an interface that the core defines, in its own words, for talking to the outside. An **adapter** is a class that connects one outside technology to a port. The **hexagon** is the core with its ports around the edge.
```

The idea in three lines:

- The core sits in the middle and knows only its ports.
- Every outside thing, from the web to PayRiver, connects through an adapter.
- All dependencies point **inward**, towards the core.

The six sides of the hexagon mean nothing special. Cockburn just wanted a shape with room for many ports, and one that did not look like layers stacked top to bottom.

### The hexagon picture

```animate
title: Ending a ride through the hexagon
kind: scene
nodes:
  - { id: web, label: Rider app controller, emoji: "📱", row: 0, col: 0 }
  - { id: test, label: A JUnit test, emoji: "🧪", row: 2, col: 0 }
  - { id: core, label: EndRideService, emoji: "⬡", row: 1, col: 2, value: core }
  - { id: store, label: Rental store adapter, emoji: "🗄️", row: 0, col: 4 }
  - { id: pay, label: PayRiver adapter, emoji: "💳", row: 1, col: 4 }
  - { id: sms, label: Notifier adapter, emoji: "💬", row: 2, col: 4 }
steps:
  - say: The core sits in the middle. It holds the fare rule and owns every port around it.
    focus: [core]
  - say: On the left, a driving adapter. The app's controller calls the core's EndRideUseCase port.
    send: { from: web, to: core, label: "endRide(rental-17)" }
  - say: The core needs the rental. It calls its own RentalStore port, and a storage adapter answers.
    send: [{ from: core, to: store, label: "findById" }, { from: store, to: core, label: "Rental", back: true }]
  - say: The core charges 460 cents through PaymentPort. Only the adapter knows PayRiver wants "4.60" and "USD".
    send: { from: core, to: pay, label: "charge(rider-7, 460)" }
    set: { pay: '"4.60" USD' }
  - say: PayRiver answers "ok". The adapter translates that into Spoke's own word, PAID.
    send: { from: pay, to: core, label: "PAID", back: true }
  - say: The core tells the rider through RiderNotifier. Push, SMS or email is the adapter's choice.
    send: { from: core, to: sms, label: "rideEnded(receipt)" }
  - say: A test can drive the very same port, with fake adapters on the right. The core cannot tell the difference.
    send: { from: test, to: core, label: "endRide(rental-17)" }
```

### Driving ports: how the world calls the core

A **driving port** (also called a **primary** port) is how the outside asks the core to do something. In Java it is an interface for a use case.

```java nocompile
public interface EndRideUseCase {
    RideReceipt endRide(String rentalId);
}
```

Many things can drive it: the app's REST controller, the kiosk, a command-line tool for support staff, a message listener, and your tests. Each one is a **driving adapter**. It turns its own format (HTTP, a kiosk tap, a command) into one call on the port.

The core class implements the port: `class EndRideService implements EndRideUseCase`. Callers depend on the interface, so they never see the class behind it.

### Driven ports: what the core needs

A **driven port** (also called a **secondary** port) is something the core needs **from** the outside. The core calls it. Spoke's end-ride core needs four:

| Driven port | In the core's words | Adapters that implement it |
|---|---|---|
| `RentalStore` | find and save rentals | in-memory map, JDBC, JPA |
| `PaymentPort` | charge a rider some cents | PayRiver, the old provider, a test fake |
| `RiderNotifier` | tell a rider their ride ended | push, SMS, email, a recording fake |
| `Clock` | what time is it now | the system clock, a fixed test clock |

```callout tip
Gojo says: the clock is a driven port too. `java.time.Clock` is an abstract class built for this, so the core can take one. Tests pass `Clock.fixed(...)` and never wait for real time.
```

Name driven ports after what the core needs, not after the technology. `PaymentPort.charge(riderId, rentalId, amountCents)` is a port. `PayRiverApi.createCharge(PayRiverChargeRequest)` is just PayRiver's library.

### Adapters on both sides

Every adapter sits outside the core and depends on it.

```text
DRIVING ADAPTERS              CORE                          DRIVEN ADAPTERS
RentalController   ──calls──▶ EndRideUseCase
SupportCli         ──calls──▶   ▲ implemented by
JUnit test         ──calls──▶ EndRideService ──calls──▶ RentalStore    ◀──implements── JdbcRentalStore
                                             ──calls──▶ PaymentPort    ◀──implements── PayRiverPaymentAdapter
                                             ──calls──▶ RiderNotifier  ◀──implements── PushNotifier
```

Look at the arrows on the right. `PayRiverPaymentAdapter` points **at** the core's `PaymentPort`. The core never points at the adapter. That is the whole trick.

### Dependency inversion: who owns the interface

You met this in [SOLID](/t/module-06-craft/topic-02-solid): high-level code depends on an interface, and **the high-level code owns that interface**.

Compare the two shapes:

- **Layered:** `RentalRepository` lives in the persistence package. Its methods are often shaped by the database, such as `extends JpaRepository<RentalRow, Long>`. The service depends on the persistence layer.
- **Hexagonal:** `RentalStore` lives in the core, shaped by what the core needs. The JDBC adapter imports the core and implements it. The persistence code depends on the core.

The interface moved across the line, and the arrow flipped. That flip is the "inversion" in **dependency inversion**. Now the most stable code, the business rules, depends on nothing that changes often.

### The core imports no framework and no SQL

A strict hexagon keeps the core free of `org.springframework`, `jakarta.persistence`, `java.sql` and any provider's library. Why so strict?

- **Testable.** A core test is plain JUnit. No Spring context, no database, no network. Hundreds of tests run in a second.
- **Swappable.** Moving from the old provider to PayRiver means one new adapter. The fare rule is untouched, so it cannot break.
- **Long-lived.** Frameworks change under you. Spring Boot 4 moved Jackson to new package names. A core with no framework imports did not notice.
- **Readable.** The core reads like the business: rentals, fares, payments. No `ResultSet`, no `HttpStatus`.

Many teams relax this a little and allow, say, `@Transactional` on core services. That is a trade, not a sin. But know the price: the core now needs Spring to test that behaviour.

### Syntax: a port and an adapter

```java nocompile
// in the core: the port, in Spoke's words
public interface PaymentPort {
    PaymentResult charge(String riderId, String rentalId, long amountCents);
}

// outside the core: the adapter, in PayRiver's words
public class PayRiverPaymentAdapter implements PaymentPort {
    private final PayRiverClient client;
    public PaymentResult charge(String riderId, String rentalId, long amountCents) {
        // translate Spoke -> PayRiver, call PayRiver, translate PayRiver -> Spoke
    }
}
```

The port uses only core types: `String`, `long`, and the core's own `PaymentResult` enum. The adapter is the only class that mentions `PayRiverClient`.

Here is the core charging through a port, with a fake adapter plugged in:

```java run
public class Main {
    public static void main(String[] args) {
        PaymentPort fake = (riderId, rentalId, cents) -> {
            System.out.println("fake charged " + riderId + " " + cents + " cents");
            return cents > 5000 ? PaymentResult.DECLINED : PaymentResult.PAID;
        };
        FareCore core = new FareCore(fake);
        System.out.println(core.finish("rider-7", "rental-17", 12));
        System.out.println(core.finish("rider-8", "rental-18", 200));
    }
}

enum PaymentResult { PAID, DECLINED, FAILED }

interface PaymentPort {
    PaymentResult charge(String riderId, String rentalId, long amountCents);
}

class FareCore {
    private final PaymentPort payments;

    FareCore(PaymentPort payments) {
        this.payments = payments;
    }

    String finish(String riderId, String rentalId, int ebikeMinutes) {
        long fare = 100 + 30L * ebikeMinutes;
        return rentalId + ": " + payments.charge(riderId, rentalId, fare);
    }
}
```

The core has no idea which provider it talks to. Swap the lambda for a PayRiver adapter, and `FareCore` does not change by a single character.

### Mapping between adapter models and domain models

Each side of a port has its own data shapes, and the adapter maps between them.

| Outside model (adapter) | Core model (domain) |
|---|---|
| `PayRiverChargeRequest(customer, amount "4.60", currency, reference)` | `charge(riderId, rentalId, 460)` |
| `PayRiverResponse(status "declined", chargeId)` | `PaymentResult.DECLINED` |
| a JPA `@Entity RentalRow` with a `Long` key and columns | a `Rental` record with rules and no annotations |
| a JSON `EndRideRequest` from the app | the `rentalId` string |

The rule: **outside models never cross into the core.** The adapter converts on the way in and on the way out. Yes, that means more mapping code, the same cost you saw with DTOs in the last ticket.

### Anti-corruption layer

When a third-party system has odd formats, its oddness tries to spread into your code. An **anti-corruption layer** is an adapter whose job is to stop that.

```callout jargon
An **anti-corruption layer** (a name from Eric Evans' book *Domain-Driven Design*) is a translating layer between your model and a foreign one. It keeps the foreign system's words, formats and quirks from "corrupting" your core.
```

PayRiver is a good example of oddness to contain:

- Amounts are text with a dot, like `"4.60"`, not whole cents.
- Statuses are words, `"ok"` and `"declined"`, plus others it may add one day.
- Its client throws `PayRiverException` when it times out.

The adapter turns all three into Spoke's terms: cents, `PaymentResult`, and `FAILED` instead of an exception. If PayRiver adds a status like `"pending_review"`, only the adapter must learn it.

### Trap: money as text

Turning 460 cents into `"4.60"` looks easy. It hides two classic bugs.

```animate
title: Turning 460 cents into PayRiver's text
kind: code
code: |
  long cents = 460;
  double asDouble = cents / 100.0;
  System.out.println(asDouble);
  String formatted = String.format(java.util.Locale.GERMANY, "%.2f", asDouble);
  System.out.println(formatted);
  String exact = java.math.BigDecimal.valueOf(cents, 2).toPlainString();
  System.out.println(exact);
steps:
  - line: 1
    vars: { cents: 460 }
    say: The fare is 460 cents, a whole number, as Spoke always stores money.
  - line: 2
    vars: { asDouble: '4.6' }
    say: Divide by 100.0 and you get a double, 4.6.
  - line: 3
    out: "4.6"
    say: Printed, it is "4.6". PayRiver wants exactly two digits after the dot, so this is rejected.
  - line: 4
    vars: { formatted: '"4,60"' }
    say: String.format uses a locale. On a computer set to German, the decimal mark is a comma.
  - line: 5
    out: "4,60"
    say: '"4,60" is wrong for PayRiver too. The bug only appears on some machines, which makes it nasty.'
  - line: 6
    vars: { exact: '"4.60"' }
    say: BigDecimal.valueOf(460, 2) means 460 with the dot moved two places. No double, no locale.
  - line: 7
    out: "4.60"
    say: Always "4.60", on every machine. Keeping money in whole cents until the very edge avoids rounding bugs.
```

`String.format` without a locale uses the computer's default language. A server in Frankfurt and a laptop in Riverton then send different text. Doubles add a second risk: `0.1 + 0.2` is `0.30000000000000004`.

### In-memory adapters for tests

Ports make test adapters easy. There are two kinds:

- A **fake** is a small working version, such as `InMemoryRentalStore` with a map. It behaves like the real thing, only faster.
- A **recording fake** (or **spy**) remembers each call, such as a `PaymentPort` that adds every charge to a list. Tests then check "charged once, 460 cents".

```callout jargon
A **fake** is a simple working stand-in for a real adapter. A **mock** is an object made by a library like Mockito, scripted with `when(...)` and checked with `verify(...)`. You used mocks in the [testing ticket](/t/module-06-craft/topic-09-mockito).
```

Fakes shine in a hexagon. The core's tests use fakes on every driven port, and run in milliseconds. Then a few slower **adapter tests** check each real adapter alone, such as the PayRiver adapter against a fake PayRiver client.

A strong habit: write one set of **contract tests** for a port, and run them against both the fake and the real adapter. Then you know the fake does not lie.

### The composition root

Somebody must build the adapters and plug them into the core. That one place is the **composition root**.

```callout jargon
The **composition root** is the single place where the app's objects are created and connected. It is the only code that knows every class, core and adapters alike.
```

Without Spring, it is a `main` method:

```java nocompile
RentalStore store = new JdbcRentalStore(dataSource);
PaymentPort payments = new PayRiverPaymentAdapter(new PayRiverHttpClient(apiKey));
RiderNotifier notifier = new PushNotifier(pushClient);
EndRideUseCase endRide = new EndRideService(store, payments, notifier, Clock.systemUTC());
```

With Spring, it is a `@Configuration` class, so the core classes need no Spring annotations at all:

```java nocompile
@Configuration
class EndRideWiring {
    @Bean
    EndRideUseCase endRide(RentalStore store, PaymentPort payments, RiderNotifier notifier, Clock clock) {
        return new EndRideService(store, payments, notifier, clock);
    }
}
```

Switching provider is now a one-line change here: build a different `PaymentPort`.

### Clean and Onion: the same idea, other names

You will hear two more names. They differ in drawings and words, but share one rule: **source code dependencies point inward, towards the business rules.**

| Name | Who, when | Its words for the parts |
|---|---|---|
| **Hexagonal** (ports and adapters) | Alistair Cockburn, 2005 | core, driving and driven ports, adapters |
| **Onion** | Jeffrey Palermo, 2008 | domain model in the centre, then domain services, application services, infrastructure outside |
| **Clean** | Robert C. Martin, 2012 | entities, use cases, interface adapters, frameworks and drivers |

Clean architecture's "use case" is your driving port's implementation. Its "interface adapters" are your adapters. Onion's rings are the hexagon drawn as circles. If you understand one, you understand all three.

### The costs, and when it is overkill

A hexagon is not free:

- **More interfaces.** Each outside thing gets a port, even when there is only ever one adapter.
- **More mapping.** Entity to domain, domain to DTO, domain to provider format.
- **More indirection.** A reader jumps from port to adapter to find what really happens.

For a small CRUD service, this is overkill. **CRUD** means create, read, update, delete: an app that mostly stores and shows records, with few rules. There, a plain layered app is simpler and fine.

Reach for a hexagon when you see these signs:

- rich business rules that deserve their own fast tests;
- outside systems that will be swapped, like payment or SMS providers;
- many ways in, like an app, a kiosk, jobs and messages;
- code that must live for years while frameworks change.

### In an interview

Hexagonal architecture comes up in system design rounds, in "how would you make this testable?" and in "we must switch vendor" questions. Spot it when you hear a third-party provider, several entry points, or "test the business logic without the database".

- **"How would you switch payment providers with little risk?"** Keep a `PaymentPort` in the core. Write a new adapter, and change one line in the composition root.
- **"Ports and adapters versus layers?"** Layers point down to the database. A hexagon points everything inward, and the core owns the interfaces.
- **"Driving versus driven ports?"** Driving: use cases the outside calls. Driven: what the core needs, such as storage, payments, notifications and the clock.
- **"What is an anti-corruption layer?"** A translating adapter that keeps a foreign model's formats out of your core.
- **"When is this overkill?"** A small CRUD service with few rules and no swappable parts.

## Worked example

Here is a different use case built as a hexagon: holding a bike for a rider for 10 minutes. The notification goes through **TextBee**, a made-up SMS provider with odd formats. Its client wants phone numbers like `+15550142`, and answers with number codes: `0` means sent.

```java run
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // composition root: build adapters, plug them into the core
        Clock clock = Clock.fixed(Instant.parse("2026-11-16T08:00:00Z"), ZoneOffset.UTC);
        BikeAvailability bikes = bikeId -> Set.of("B-1042", "B-2001").contains(bikeId);
        TextBeeClient textBee = (to, body) -> {
            System.out.println("TextBee -> " + to + ": " + body);
            return to.startsWith("+1") ? 0 : 21;
        };
        Map<String, String> phones = Map.of("rider-7", "+15550142", "rider-8", "0044-7700");
        RiderNotifier sms = new TextBeeNotifier(textBee, phones);
        HoldBikeUseCase holds = new HoldBikeService(bikes, sms, clock);

        System.out.println(holds.hold("rider-7", "B-1042"));
        System.out.println(holds.hold("rider-8", "B-2001"));
        try {
            holds.hold("rider-7", "B-9999");
        } catch (BikeUnavailableException e) {
            System.out.println("refused: " + e.getMessage());
        }
    }
}

// ---------- core: domain, ports and the use case. No adapter is named here. ----------
record Hold(String riderId, String bikeId, Instant until, boolean riderTold) {}

class BikeUnavailableException extends RuntimeException {
    BikeUnavailableException(String bikeId) {
        super(bikeId + " cannot be held");
    }
}

interface HoldBikeUseCase {                   // driving port
    Hold hold(String riderId, String bikeId);
}

interface BikeAvailability {                  // driven port
    boolean isFree(String bikeId);
}

interface RiderNotifier {                     // driven port: true when the message went out
    boolean bikeHeld(String riderId, String bikeId, Instant until);
}

class HoldBikeService implements HoldBikeUseCase {
    static final Duration HOLD_TIME = Duration.ofMinutes(10);
    private final BikeAvailability bikes;
    private final RiderNotifier notifier;
    private final Clock clock;

    HoldBikeService(BikeAvailability bikes, RiderNotifier notifier, Clock clock) {
        this.bikes = bikes;
        this.notifier = notifier;
        this.clock = clock;
    }

    public Hold hold(String riderId, String bikeId) {
        if (!bikes.isFree(bikeId)) {
            throw new BikeUnavailableException(bikeId);
        }
        Instant until = clock.instant().plus(HOLD_TIME);
        boolean told = notifier.bikeHeld(riderId, bikeId, until);
        return new Hold(riderId, bikeId, until, told);
    }
}

// ---------- outside: TextBee's library and the adapter that translates for it ----------
interface TextBeeClient {
    int send(String phoneNumber, String text);  // 0 = sent, 21 = bad number, others = errors
}

class TextBeeNotifier implements RiderNotifier {
    private final TextBeeClient client;
    private final Map<String, String> phoneByRider;

    TextBeeNotifier(TextBeeClient client, Map<String, String> phoneByRider) {
        this.client = client;
        this.phoneByRider = new HashMap<>(phoneByRider);
    }

    public boolean bikeHeld(String riderId, String bikeId, Instant until) {
        String phone = phoneByRider.get(riderId);
        if (phone == null) {
            return false;
        }
        String time = until.atZone(ZoneOffset.UTC).toLocalTime().toString();
        int code = client.send(phone, "Spoke: " + bikeId + " is yours until " + time + " UTC.");
        return code == 0;
    }
}
```

It prints:

```text
TextBee -> +15550142: Spoke: B-1042 is yours until 08:10 UTC.
Hold[riderId=rider-7, bikeId=B-1042, until=2026-11-16T08:10:00Z, riderTold=true]
TextBee -> 0044-7700: Spoke: B-2001 is yours until 08:10 UTC.
Hold[riderId=rider-8, bikeId=B-2001, until=2026-11-16T08:10:00Z, riderTold=false]
refused: B-9999 cannot be held
```

Step by step:

1. **The core** (`Hold`, the exception, three ports, `HoldBikeService`) mentions no SMS, no phone numbers and no TextBee. It says "tell the rider the bike is held until this instant".
2. **The driving port** `HoldBikeUseCase` is what a controller, a kiosk or a test would call. Here `main` calls it.
3. **The clock** is a driven port. The fixed clock makes the output the same on every run: 08:00 plus 10 minutes is 08:10.
4. **The adapter** `TextBeeNotifier` does all the translation. It turns the rider id into a phone number, the instant into readable text, and TextBee's codes into `true` or `false`.
5. **rider-8** has a number TextBee rejects with code `21`. The core just learns "not told". It could show the hold in the app instead.
6. **The composition root** at the top of `main` is the only code that knows both sides.

To move from TextBee to another SMS provider, you write one new adapter and change one line in `main`. `HoldBikeService` and its tests stay exactly as they are.

## Try it

This core charges a fare through a port. Two adapters are ready. Run it, then play with the plugs.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        RecordingPayments recorder = new RecordingPayments();
        FareCore core = new FareCore(recorder);
        System.out.println(core.endRide("rider-7", "EBIKE", 12));
        System.out.println(core.endRide("rider-8", "CLASSIC", 20));
        System.out.println("charges seen: " + recorder.charges);
    }
}

// core
enum PaymentResult { PAID, DECLINED, FAILED }

interface PaymentPort {
    PaymentResult charge(String riderId, long amountCents);
}

class FareCore {
    private final PaymentPort payments;

    FareCore(PaymentPort payments) {
        this.payments = payments;
    }

    String endRide(String riderId, String bikeType, int minutes) {
        long perMinute = switch (bikeType) {
            case "EBIKE" -> 30;
            case "CARGO" -> 25;
            default -> 15;
        };
        long fare = 100 + perMinute * minutes;
        return riderId + " owes " + fare + " cents: " + payments.charge(riderId, fare);
    }
}

// adapters
class RecordingPayments implements PaymentPort {
    final List<String> charges = new ArrayList<>();

    public PaymentResult charge(String riderId, long amountCents) {
        charges.add(riderId + "=" + amountCents);
        return PaymentResult.PAID;
    }
}

class AlwaysDeclines implements PaymentPort {
    public PaymentResult charge(String riderId, long amountCents) {
        return PaymentResult.DECLINED;
    }
}
```

1. Plug in `new AlwaysDeclines()` instead of the recorder. Which class did you change, and which did you not touch?
2. Write a third adapter, `PayRiverStyle`, that prints the amount as PayRiver text with `java.math.BigDecimal.valueOf(amountCents, 2).toPlainString()`, then returns `PAID`.
3. Try `String.format(java.util.Locale.GERMANY, "%.2f", amountCents / 100.0)` in your adapter. What would PayRiver receive?

## Recap

- Layers still leave the core depending on storage and providers. A hexagon points every dependency inward, towards the business rules.
- The core owns its ports. Driving ports are the use cases the world calls. Driven ports are what the core needs: storage, payments, notifications, the clock.
- Adapters on the outside connect one technology to one port, and map between outside models and domain models. An anti-corruption layer keeps a foreign API's odd formats out.
- A core with no framework, SQL or provider imports is fast to test with fakes and cheap to change. Clean and Onion architecture are the same idea with other names.
- The price is more interfaces and mapping. For a small CRUD service, it is overkill.

## Common mistakes

**1. The core still mentions the provider.**

```java nocompile
public class EndRideService implements EndRideUseCase {
    private static boolean wasPaid(PayRiverResponse response) {   // PayRiver inside the core
        return response != null && "ok".equals(response.status());
    }
}
```

```text
The core must depend only on its own ports. PayRiver's formats and status codes belong in PayRiverPaymentAdapter. Found: [EndRideService mentions jq.m09.hexagonal.PayRiverResponse]
```

That is the hexagon check from the practice. Even one helper method ties the core to PayRiver. Fix: move every PayRiver word into the adapter, and let the core see only `PaymentResult`.

**2. A port shaped like the provider.**

```java nocompile
public interface PaymentPort {
    PayRiverResponse createCharge(PayRiverChargeRequest request);   // PayRiver's types in the port
}
```

It compiles and it looks like a port, but it is PayRiver's library with a new name. Symptom: the next provider switch changes the port, so it changes the core. Fix: design the port in Spoke's words, `PaymentResult charge(String riderId, String rentalId, long amountCents)`, and translate in the adapter.

**3. Money through a double or the default locale.**

```java nocompile
String amount = String.valueOf(amountCents / 100.0);          // 460 -> "4.6"
String amount = String.format("%.2f", amountCents / 100.0);   // "4,60" on a German computer
```

```text
4.6
4,60
4.60
```

Those are the three lines from the money animation: the double, the German format, and `BigDecimal`. Fix: `BigDecimal.valueOf(amountCents, 2).toPlainString()`, which gives `"4.60"` everywhere.

**4. Letting the provider's exception escape.**

```java nocompile
public PaymentResult charge(String riderId, String rentalId, long amountCents) {
    return toResult(client.createCharge(request));   // PayRiverException flies straight into the core
}
```

```text
PayRiverException escaped from the adapter. Catch it and return PaymentResult.FAILED, so PayRiver's exception never reaches the core.
```

To handle it, the core would have to `catch (PayRiverException e)`, and now it depends on PayRiver again. Fix: catch it in the adapter and return `PaymentResult.FAILED`, a word the core already knows.

**5. A hexagon for a tiny CRUD service.**

```java nocompile
interface StationNamePort { String name(String stationId); }      // one adapter, forever
class StationNameAdapter implements StationNamePort { ... }
class StationNameMapper { ... }
class StationNameUseCaseImpl implements StationNameUseCase { ... }
```

Symptom: renaming one field touches six files, and new teammates cannot find where anything happens. Fix: for a service that only stores and shows records, a plain layered app is enough. Add ports where something really will be swapped or tested apart.

## Hook

A pilot never trusts memory before take-off. Even after 10,000 flights, they read the same checklist, line by line. Checklists catch the one thing a tired, clever person forgets.

Spoke is about to fly somewhere new. Bramford, a second city, has said yes. In this ticket you get the checklist a professional uses to build a service from an empty folder to launch day. Every line links a ticket you already finished. Then you build the first real piece: a rentals core that knows each city's stations, fares and clock.

## Concept

### From an empty folder to a launched service

Senior engineers do not start by typing a controller. They walk the same path every time, and they can explain each step. Here is that path. Keep this table: it is your checklist for every service you build.

| # | Step | You learned it in |
|---|---|---|
| 1 | Requirements and non-goals | [Object-oriented design](/t/module-06-craft/topic-13-object-oriented-design), [System design](/t/module-09-architecture/topic-06-system-design) |
| 2 | A one-page design doc | [Writing documentation](/t/module-10-professional/topic-03-documentation) |
| 3 | Plan and estimate | [Estimation and planning](/t/module-10-professional/topic-04-estimation) |
| 4 | API contract first | [REST design](/t/module-08-backend/topic-02-rest-design), [API docs and errors](/t/module-08-backend/topic-12-api-docs-errors) |
| 5 | Data model and migrations | [Database design](/t/module-08-backend/topic-14-database-design) |
| 6 | Core domain and use cases | [Hexagonal architecture](/t/module-09-architecture/topic-02-hexagonal-architecture) |
| 7 | Validation and the error model | [Validation](/t/module-08-backend/topic-06-validation) |
| 8 | Idempotency | [REST design](/t/module-08-backend/topic-02-rest-design) |
| 9 | Tests | [JUnit](/t/module-06-craft/topic-08-junit), [Layered architecture](/t/module-09-architecture/topic-01-layered-architecture) |
| 10 | Security | [Spring Security](/t/module-08-backend/topic-11-spring-security) |
| 11 | Observability | [Logs, metrics, traces](/t/module-09-architecture/topic-08-observability) |
| 12 | Resilience | [Resilience](/t/module-09-architecture/topic-09-resilience) |
| 13 | Packaging and deployment | [Docker](/t/module-07-tooling/topic-05-docker-basics), [Kubernetes](/t/module-09-architecture/topic-10-kubernetes) |
| 14 | Launch checklist | [Shipping to production](/t/module-07-tooling/topic-07-shipping-to-production) |
| 15 | After launch | [Observability](/t/module-09-architecture/topic-08-observability), [Code reviews](/t/module-10-professional/topic-01-code-reviews) |

The order matters. Cheap decisions come first, and expensive ones last. Changing a sentence in a design doc costs a minute. Changing a database table that holds a million rows costs a week.

### Step 1: Requirements and non-goals

First, write down what Bramford needs, in plain words. Split it in two, as you did in system design.

- **Functional:** riders start and end rentals at five Bramford stations. Fares are 120 cents to unlock, then CLASSIC 15 and EBIKE 35 cents a minute. The map shows free bikes and docks.
- **Non-functional:** every request under 200 ms at the 99th percentile. Rentals survive a crash. A rider is never charged twice.

Then write the **non-goals**, the things you will not do.

```callout jargon
A **non-goal** is something a project deliberately does not do, written down so nobody expects it. "No cargo bikes in Bramford" is a non-goal. So is "no memberships at launch".
```

Non-goals protect the 1 February launch date. Without them, every meeting adds "small" extras, and the launch date slips. Ask the product manager to agree to them in writing.

### Step 2: A one-page design doc

Next, write a short design doc, as in [Writing documentation](/t/module-10-professional/topic-03-documentation). One page is enough for this service:

- **Context:** Bramford's council approved five stations.
- **Goals and non-goals:** from step 1.
- **Design:** the API, the tables, the core and its ports, with one diagram.
- **Alternatives:** "a copy of the Riverton service per city" was rejected, because every fix would need two copies.
- **Risks:** time zones, a new payment region, launch-day traffic.
- **Rollout:** a city switch, a canary, a rollback plan.

The doc is for your teammates' review before you code. A reviewer who spots "Bramford uses London time" in the doc saves you a week of bug hunting later.

### Step 3: Plan, estimate, and a walking skeleton

Now split the work into tasks and estimate them, as in the [Bramford plan](/t/module-10-professional/topic-04-estimation). Add a buffer for the unknowns.

Order the tasks so something runs end to end early.

```callout jargon
A **walking skeleton** is the thinnest version of a system that works from end to end: one request goes in, through every layer, and an answer comes back. You thicken it later.
```

For Bramford, the skeleton is `POST /rentals` with one hard-coded rule, saved to a real table and deployed to staging. It flushes out build, deploy and wiring problems in week one, not in week six.

### Step 4: The API contract first

Agree the API before you build it. The rider app team can then build against the contract while you build the server.

| Request | Answer |
|---|---|
| `POST /rentals` with `riderId`, `bikeId`, `stationId` | `201 Created`, `Location: /rentals/rental-17` |
| `GET /rentals/{id}` | `200` and the rental |
| `POST /rentals/{id}/end` with `stationId` | `200` and the ended rental, with `fareCents` |
| `GET /stations/{id}/availability` | `200` and the free bikes and docks |

Write it down as an OpenAPI file, as in [API docs and errors](/t/module-08-backend/topic-12-api-docs-errors). Include the error answers too. A contract that lists only happy paths is half a contract.

### Step 5: Data model and migrations

Next, the tables. Design them from the questions the service must answer, as in [Database design](/t/module-08-backend/topic-14-database-design).

```sql
CREATE TABLE rentals (
    id              VARCHAR(40) PRIMARY KEY,
    city_id         VARCHAR(40) NOT NULL,
    rider_id        VARCHAR(40) NOT NULL,
    bike_id         VARCHAR(20) NOT NULL,
    started_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at        TIMESTAMP WITH TIME ZONE,
    fare_cents      BIGINT NOT NULL,
    idempotency_key VARCHAR(64),
    active_bike_id  VARCHAR(20),
    CONSTRAINT one_active_rental_per_bike UNIQUE (active_bike_id)
);
```

Three choices to notice:

- **Times are stored with a time zone**, as instants. You convert to a city's local time only when you show or report them.
- **Money is a `BIGINT` of cents**, never a decimal of dollars.
- **`active_bike_id`** holds the bike id while a ride is on, and `NULL` after. In H2 and PostgreSQL, a unique column allows many NULLs, so the database refuses two active rentals of one bike.

Ship the table as a numbered migration, and never edit one that has already run.

### Step 6: The core, with ports and adapters

Now the code. Build the core first, framework-free, as in [Hexagonal architecture](/t/module-09-architecture/topic-02-hexagonal-architecture). The core holds the rules: which station, which bike, what it costs. Everything technical plugs in through ports.

```animate
title: A Bramford rental travels through the hexagon
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱", row: 1, col: 0 }
  - { id: web, label: RentalController, emoji: "🌐", row: 1, col: 1 }
  - { id: core, label: RentalService (Bramford), emoji: "⬡", row: 1, col: 3, value: "CityConfig" }
  - { id: db, label: JDBC adapter, emoji: "🗄️", row: 0, col: 5 }
  - { id: fleet, label: Bike inventory, emoji: "🚲", row: 1, col: 5 }
  - { id: events, label: Event publisher, emoji: "📣", row: 2, col: 5 }
steps:
  - say: A rider in Bramford taps Unlock on BR-2003 at Castle Hill.
    send: { from: app, to: web, label: "POST /rentals" }
  - say: The controller is a driving adapter. It turns HTTP into one call on the core's use case.
    send: { from: web, to: core, label: "startRental(rider-7, BR-2003, castle-hill)" }
  - say: The core checks its rules with data from Bramford's CityConfig. Castle Hill is a Bramford station.
    focus: [core]
  - say: It asks the inventory port about the bike. BR-2003 is a Bramford EBIKE, docked right there.
    send: [{ from: core, to: fleet, label: "findById" }, { from: fleet, to: core, label: "AVAILABLE", back: true }]
  - say: It saves the rental through the repository port. Only the adapter knows this is SQL.
    send: { from: core, to: db, label: "save(rental-17)" }
  - say: When the ride ends, the core prices it and publishes RideEnded. Billing and reports listen.
    send: { from: core, to: events, label: "RideEnded 820 cents" }
  - say: The controller turns the result into 201 Created. The core never saw HTTP, SQL or JSON.
    send: { from: web, to: app, label: "201 Created", back: true }
```

The core's driving port is `RentalUseCases`: start, end, find, availability. Its driven ports are `RentalRepository`, `BikeInventory`, `EventPublisher` and a `Clock`. Tests drive the same port with in-memory adapters, and run in milliseconds.

### Many cities: configuration, not if-statements

The tempting way to add Bramford is an `if`:

```java nocompile
long perMinute;
if (cityId.equals("bramford")) {
    perMinute = type == BikeType.EBIKE ? 35 : 15;
} else {
    perMinute = type == BikeType.EBIKE ? 30 : type == BikeType.CARGO ? 25 : 15;
}
```

It works for two cities. With ten, every rule grows a ladder of `if`s, and each new city means editing, re-testing and redeploying the rules.

The professional way: put everything that differs between cities into **data**. A `CityConfig` holds the id, time zone, unlock price, per-minute prices, stations and docks. One `RentalService` serves one city, built with that city's config. Riverton and Bramford run the same code with different values.

```callout tip
Gojo says: when a new customer, city or country would make you copy an `if`, ask "what is the data here?" Then the next city is a config review, not a code change.
```

### Syntax: a city as a record

```java nocompile
public record CityConfig(String id, String name, ZoneId zone, long unlockCents,
                         Map<BikeType, Long> perMinuteCents, Map<String, String> stations,
                         int docksPerStation) {
    public boolean offers(BikeType type) { return perMinuteCents.containsKey(type); }
    public boolean hasStation(String stationId) { return stations.containsKey(stationId); }
}
```

A record is a good fit: a config never changes after it is built. "Bramford has no CARGO" is not a special rule either. CARGO is simply missing from Bramford's `perMinuteCents`, so `offers(CARGO)` is false.

In production the values come from a config file or a table, reviewed like code. The core does not care where they came from.

### Money stays in whole cents

Every fare in Spoke is a `long` of cents. Here is why, from a real run:

```java run
public class Main {
    public static void main(String[] args) {
        double dollars = 1.20 + 0.35 * 12;
        System.out.println(dollars);
        long cents = 120 + 35 * 12;
        System.out.println(cents);
        System.out.println(0.1 + 0.2);
    }
}
```

It prints `5.3999999999999995`, then `540`, then `0.30000000000000004`. A `double` cannot hold most decimal fractions exactly. Whole cents are exact, so sums, refunds and reports always add up.

Rounding needs one rule, written down and used everywhere. Spoke counts **whole minutes, rounded down**, with `Duration.toMinutes()`, exactly as the end-ride core did in module 9. A ride of 12 min 59 s pays for 12 minutes. If billing rounded up while the app rounded down, riders would see one price and pay another.

### Time zones: each city has its own calendar

A ride happens at one **instant**, the same moment everywhere on Earth. But the calendar date depends on where you stand. 04:30 UTC on 18 November is 23:30 on 17 November in Riverton.

So the core follows two rules:

- **Measure with instants.** A ride's length is `Duration.between(start, end)`. It never depends on a time zone.
- **Show and report in the city's zone.** A ride's **service date** is the local day it started, in that city's `ZoneId`.

```callout jargon
**DST** (daylight saving time) moves clocks forward an hour in spring and back an hour in autumn. A **service date** is the business day a ride belongs to, in the city's own time zone.
```

DST is where local-time maths breaks. On 1 November 2026, Riverton's clocks went back an hour:

```animate
title: A 20-minute ride on the night the clocks go back
kind: code
code: |
  java.time.ZoneId newYork = java.time.ZoneId.of("America/New_York");
  java.time.Instant start = java.time.Instant.parse("2026-11-01T05:50:00Z");
  java.time.Instant end = start.plusSeconds(20 * 60);
  System.out.println(start.atZone(newYork).toLocalTime());
  System.out.println(end.atZone(newYork).toLocalTime());
  System.out.println(java.time.Duration.between(start, end).toMinutes());
steps:
  - line: 1
    vars: { newYork: "America/New_York" }
    say: Riverton's time zone. On this night, at 02:00, its clocks jump back to 01:00.
  - line: 2
    vars: { start: "05:50Z" }
    say: The ride starts at 05:50 UTC, a single moment on Earth.
  - line: 3
    vars: { end: "06:10Z" }
    say: It ends 20 real minutes later, at 06:10 UTC.
  - line: 4
    out: "01:50"
    say: On Riverton's wall clocks, the ride started at 01:50.
  - line: 5
    out: "01:10"
    say: And it ended at 01:10! The clocks went back an hour in between. Local times would say minus 40 minutes.
  - line: 6
    out: "20"
    say: Duration between the two instants is 20 minutes, the truth. Measure with instants, show in local time.
```

### Syntax: an Instant on a city's calendar

```java nocompile
LocalDate serviceDate = rental.startedAt().atZone(city.zone()).toLocalDate();
```

`atZone(zone)` puts an `Instant` on that zone's clock, as a `ZonedDateTime`. `toLocalDate()` keeps only the date. Never use `ZoneId.systemDefault()` in a server: it is whatever the machine was set to, and Spoke's servers run in UTC.

A Bramford ride from 23:50 to 00:10 belongs to the day it started. Its fare uses the 20 real minutes. Its service date is the 17th, not the 18th.

### Step 7: Validation and the error model

Check a request's shape at the edge with Bean Validation, as in [Validation](/t/module-08-backend/topic-06-validation). Then let the core check the business rules, and throw domain exceptions.

In the core, all of them form one sealed family, `RentalException`. The web adapter maps each one to a status and a Problem Details answer, in one advice class:

| Error | Status | Why that status |
|---|---|---|
| a blank `bikeId` | 400 `invalid-request` | the request's shape is wrong |
| unknown rental, station or bike | 404 | it does not exist in this city |
| bike already out, rider already riding, ride already ended | 409 | it clashes with the current state; later it may work |
| CARGO in Bramford, an idempotency key reused | 422 | well formed, but it can never be done |

The 400 keeps module 8's shape: type `https://api.spoke.example/problems/invalid-request`, title "Invalid request", and an `errors` map from each bad field to its message. Write this table into the contract. Clients code against `type` values like `https://api.spoke.example/problems/bike-unavailable`, so they must never change.

### Step 8: Idempotency keys on POST /rentals

Phones lose answers in tunnels. The app retries, and a careless server starts two rentals. So `POST /rentals` takes an `Idempotency-Key` header, as in [REST design](/t/module-08-backend/topic-02-rest-design).

The core's rule, checked **before** anything else:

- Same rider, same key, same bike and station: return the first rental. Change nothing.
- Same rider, same key, different body: that is an app bug. Refuse it with `IdempotencyKeyReused`.
- Keys belong to one rider. Two riders may send the same key text by chance.

Store the key on the rental, with a unique constraint on `(rider_id, idempotency_key)`. Then even two retries racing on two servers cannot both win.

### Step 9: Tests shaped like a pyramid

A service needs many fast tests and a few slow ones. Picture a pyramid:

- **The wide base: unit tests on the core.** Plain JUnit with in-memory adapters and a fixed `Clock`. Hundreds of them, in a second. Every fare, every rule, every time zone.
- **The middle: slice tests on adapters.** A **slice test** starts only one slice of the app, such as the web layer with MockMvc, or the JDBC adapter against a real database engine.
- **The tip: a few end-to-end tests.** The whole service, from HTTP to the database, for the main journeys.

```callout jargon
An **end-to-end test** checks a whole journey through the running system, such as "start a rental, end it, see the fare". It is slow and catches wiring bugs. A **slice test** checks one adapter with just enough of the framework around it.
```

Upside-down pyramids, with mostly end-to-end tests, are slow and flaky. When one fails, nobody knows which part broke.

### Step 10: Security and secrets

Bramford riders log in, so every rental request carries a token. Two questions, as in [Spring Security](/t/module-08-backend/topic-11-spring-security):

- **Authentication:** who is this? The token says rider-7.
- **Authorization:** may rider-7 do this? Only rider-7 may end rider-7's rental.

Take the rider id from the verified token, never from the request body. Otherwise anyone can rent bikes on someone else's card.

Keep secrets, such as the database password and the payment API key, out of the code and the repository. Inject them from the environment or a vault at deploy time. Never log them.

### Step 11: Observability

On launch night you must see what the service is doing, as in [Logs, metrics, traces](/t/module-09-architecture/topic-08-observability).

- **Logs with ids:** every line carries the request id, rental id and city. Then one grep finds one rider's whole story.
- **RED metrics** for every endpoint: **R**ate (requests a second), **E**rrors (how many fail) and **D**uration (p50, p95, p99). Tag them with the city.
- **Health checks:** liveness says "the process is alive". Readiness says "I can serve riders now". Bramford's server is not ready until its stations are loaded.

A dashboard with RED per city tells you in seconds whether Bramford is healthy, or whether Riverton noticed the launch.

### Step 12: Resilience on the payment port

The payment provider is the riskiest outside call, so wrap its port, as in [Resilience](/t/module-09-architecture/topic-09-resilience):

- **A timeout** on every call, so a slow provider cannot hold your threads.
- **Retries** only for safe failures, with backoff and jitter, and the same idempotency key on each try.
- **A circuit breaker** that stops calling a provider that keeps failing, and lets rides end anyway with the charge queued for later.
- **A rate limit per rider** on `POST /rentals`, so one broken app cannot flood the service.

These live in adapters and wrappers around ports. The core still just calls `charge(...)`.

### Step 13: Packaging and deployment

Package the service once, as an image, as in [Docker](/t/module-07-tooling/topic-05-docker-basics). The same image goes to staging and production. Only configuration differs.

On [Kubernetes](/t/module-09-architecture/topic-10-kubernetes), point the probes at your health checks. The readiness probe keeps riders away from a pod that is still loading. Set memory requests and limits, and run at least two pods.

A **rolling update** then replaces pods one at a time. Each new pod gets traffic only once its readiness probe says yes. If new pods never become ready, the rollout stops, and the old ones keep serving.

### Step 14: The launch checklist

Launch is not "merge and hope". Walk a written checklist, as in [Shipping to production](/t/module-07-tooling/topic-07-shipping-to-production):

- **A city switch.** Bramford is behind a feature flag. The code ships days early, switched off. Launch is flipping it on.
- **A canary.** Turn Bramford on for staff riders first, then 5% of riders, then everyone, watching the numbers at each step.
- **Dashboards** with RED per city, and alerts on symptoms riders feel.
- **A runbook:** what to do when rentals fail, payments time out, or a station shows wrong numbers.
- **A rollback plan:** flip the switch off first. Redeploy the old image second. Know who decides, and when.

```callout jargon
A **runbook** is a written set of steps for handling a known problem in production, so whoever is on call at 3 a.m. does not have to invent them.
```

### Step 15: After launch

Launch day is the start. For the next weeks:

- **On-call:** someone watches Bramford's alerts, with the runbook to hand.
- **Postmortems:** when something breaks, write a blameless postmortem. Fix the system, not the person.
- **Iterate:** the non-goals come back as the next plan. Memberships, more stations, maybe cargo bikes.

Measure what you promised in step 1. Was p99 under 200 ms? Did anyone pay twice? The answers feed the next design doc.

### In an interview

"Tell me about a project you built end to end" is a classic SDE 2 question. Interviewers want to hear the checklist, in your own words, with numbers.

A strong answer, in about three minutes:

1. **The problem and your role:** "Spoke launched in a second city. I designed and built the rentals core and its service."
2. **The key decisions, with trade-offs:** "I put city differences in config, not `if`s. I kept money in cents and measured rides with instants, so DST could not break fares."
3. **The hard part:** "Double rentals under retries. Idempotency keys per rider, plus a unique constraint in the database."
4. **How you knew it worked:** "A test pyramid, a canary, RED dashboards per city."
5. **What you would change:** "I would load city configs from a table, so a new city needs no deploy."

Expect follow-ups such as "what would break at ten cities?" or "how would you roll it back?" Answer from the checklist, and say "I don't know, but I'd find out by..." when you don't.

## Worked example

Here is a different part of the core in action: pricing and dating rides for each city's daily report. The same config record drives both cities.

```java run
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public class Main {
    enum BikeType { CLASSIC, EBIKE, CARGO }

    // One city's differences, as data. A new city is a new value, not a new if-statement.
    record CityConfig(String id, ZoneId zone, long unlockCents, Map<BikeType, Long> perMinuteCents) {
        boolean offers(BikeType type) {
            return perMinuteCents.containsKey(type);
        }

        long fareCents(BikeType type, Duration ride) {
            if (!offers(type)) {
                throw new IllegalArgumentException(id + " does not offer " + type);
            }
            return unlockCents + perMinuteCents.get(type) * ride.toMinutes();   // whole minutes, rounded down
        }
    }

    record Ride(String rentalId, CityConfig city, BikeType type, Instant start, Instant end) {}

    public static void main(String[] args) {
        CityConfig riverton = new CityConfig("riverton", ZoneId.of("America/New_York"), 100,
                Map.of(BikeType.CLASSIC, 15L, BikeType.EBIKE, 30L, BikeType.CARGO, 25L));
        CityConfig bramford = new CityConfig("bramford", ZoneId.of("Europe/London"), 120,
                Map.of(BikeType.CLASSIC, 15L, BikeType.EBIKE, 35L));

        List<Ride> rides = List.of(
                new Ride("rental-17", bramford, BikeType.EBIKE,
                        Instant.parse("2026-11-17T23:50:00Z"), Instant.parse("2026-11-18T00:10:30Z")),
                new Ride("rental-18", riverton, BikeType.CLASSIC,
                        Instant.parse("2026-11-18T04:30:00Z"), Instant.parse("2026-11-18T04:42:59Z")),
                new Ride("rental-19", bramford, BikeType.CLASSIC,
                        Instant.parse("2026-07-14T23:30:00Z"), Instant.parse("2026-07-14T23:39:10Z")));

        for (Ride ride : rides) {
            Duration length = Duration.between(ride.start(), ride.end());
            long fare = ride.city().fareCents(ride.type(), length);
            LocalDate utcDay = ride.start().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate localDay = ride.start().atZone(ride.city().zone()).toLocalDate();
            System.out.println(ride.rentalId() + " " + ride.city().id() + ": " + length.toMinutes() + " min, "
                    + fare + " cents, UTC day " + utcDay + ", local day " + localDay);
        }
        try {
            bramford.fareCents(BikeType.CARGO, Duration.ofMinutes(10));
        } catch (IllegalArgumentException e) {
            System.out.println("refused: " + e.getMessage());
        }
    }
}
```

It prints:

```text
rental-17 bramford: 20 min, 820 cents, UTC day 2026-11-17, local day 2026-11-17
rental-18 riverton: 12 min, 280 cents, UTC day 2026-11-18, local day 2026-11-17
rental-19 bramford: 9 min, 255 cents, UTC day 2026-07-14, local day 2026-07-15
refused: bramford does not offer CARGO
```

Step by step:

1. **One record, two cities.** `fareCents` has no `if` on the city. Bramford's 120 and 35 and Riverton's 100 and 30 are just values.
2. **rental-17** crosses midnight in Bramford. `Duration.between` gives 20 real minutes: 120 + 35 × 20 = 820 cents. In November, London time equals UTC, so both days agree.
3. **rental-18** starts at 04:30 UTC. In Riverton that is 23:30 the evening before. A report by UTC day would put it on the wrong day.
4. **rental-19** is a July ride in Bramford. Britain is on summer time, one hour ahead of UTC, so 23:30 UTC is 00:30 the next day locally.
5. **Rounding** is the same everywhere: 12 min 59 s is 12 minutes (rental-18), and 9 min 10 s is 9 (rental-19).
6. **CARGO** is refused because it is missing from Bramford's prices, not because of a special rule.

The work is one loop over the rides, so it takes O(n) time for n rides, and O(1) extra space besides the output.

## Try it

Two cities, one fare rule. Run it, then change the data, not the code.

```java run
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class Main {
    enum BikeType { CLASSIC, EBIKE, CARGO }

    record CityConfig(String id, long unlockCents, Map<BikeType, Long> perMinuteCents) {
        long fareCents(BikeType type, Duration ride) {
            Long perMinute = perMinuteCents.get(type);
            if (perMinute == null) {
                throw new IllegalArgumentException(id + " does not offer " + type);
            }
            return unlockCents + perMinute * ride.toMinutes();
        }
    }

    public static void main(String[] args) {
        List<CityConfig> cities = List.of(
                new CityConfig("riverton", 100, Map.of(BikeType.CLASSIC, 15L, BikeType.EBIKE, 30L, BikeType.CARGO, 25L)),
                new CityConfig("bramford", 120, Map.of(BikeType.CLASSIC, 15L, BikeType.EBIKE, 35L)));
        Duration ride = Duration.ofMinutes(12).plusSeconds(40);
        for (CityConfig city : cities) {
            System.out.println(city.id() + " EBIKE 12 min 40 s: " + city.fareCents(BikeType.EBIKE, ride) + " cents");
        }
    }
}
```

1. Add a third city to the list, with its own unlock price and only CLASSIC bikes. Ask it for an EBIKE fare. What happens?
2. Change the rule to round **up**: use `(ride.toSeconds() + 59) / 60` instead of `ride.toMinutes()`. How much more does each 12 min 40 s ride cost? Why must billing and the app agree on this rule?
3. Rewrite the fares as `double` dollars, such as `1.20` and `0.35`, and print `1.20 + 0.35 * 12`. Is it still exact?

## Recap

- A professional builds every service along one checklist, from requirements and non-goals to launch and on-call. Each step is a ticket you already finished.
- Cheap decisions come first. Write the contract and the tables before the code, and get them reviewed.
- Keep the core framework-free behind ports. Put everything that differs between cities into a config, not into `if`s, so the next city is a data change.
- Money stays in whole cents with one rounding rule. Measure rides with instants, and date them in the city's own `ZoneId`.
- Launch behind a city switch with a canary, dashboards, a runbook and a rollback plan, then run on-call and postmortems after.

## Common mistakes

**1. An `if` for each city.**

```java nocompile
if (city.id().equals("bramford") && type == BikeType.EBIKE) {
    return 120 + 35 * minutes;
}
```

```text
FareCalculator should not name a city. Read prices, stations and the zone from CityConfig instead of writing if (city is Bramford) ==> expected: <true> but was: <false>
```

That is the hexagon check from the practice. The `if` gives the right fare today, but the price now lives in two places. Fix: read every price from the city's config.

**2. Dating a ride in UTC, or the server's zone.**

```java nocompile
LocalDate serviceDate = LocalDate.ofInstant(rental.startedAt(), ZoneOffset.UTC);
```

```text
04:30 UTC is 23:30 on 17 November in New York. Use the city's zone: startedAt.atZone(city.zone()) ==> expected: <2026-11-17> but was: <2026-11-18>
```

The late-night Riverton ride lands on the wrong day, so the daily report is wrong. Fix: `rental.startedAt().atZone(city.zone()).toLocalDate()`.

**3. Money in a `double`.**

```java nocompile
double fare = 1.20 + 0.35 * 12;
```

```text
5.3999999999999995
```

That is the real output from the money card. Totals drift by fractions of a cent, and reports stop adding up. Fix: a `long` of cents, `120 + 35 * 12`, which is exactly 540.

**4. Idempotency keys shared by all riders.**

```java nocompile
.filter(r -> idempotencyKey.equals(r.idempotencyKey()))   // forgot the rider
```

```text
jq.m10.capstone.RentalException$IdempotencyKeyReused: Idempotency key tap-7c1e was already used for a different request
```

rider-8 happened to pick the same key text as rider-7, and was refused. Worse, with a matching body, rider-8 would get rider-7's rental back. Fix: match on the rider **and** the key, and make the unique constraint `(rider_id, idempotency_key)`.

**5. Building before agreeing.**

Symptom: three weeks in, the app team expects `bike_id` in the JSON, you send `bikeId`. Billing expects whole minutes rounded up, the core rounds down. Nobody wrote it down, so everybody guessed. Fix: write the API contract, the error table and the rounding rule into the design doc first, and get them reviewed before the code.

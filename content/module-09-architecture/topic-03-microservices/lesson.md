## Hook

A busy restaurant has one big kitchen. The cooks share one fridge, one menu and one till. When the grill cook is ill, someone else covers. A food court is different: ten stalls, each with its own kitchen, staff and till. Each stall can change its menu without asking the others. But a burger from one stall and a drink from another means two queues and two payments. And one stall may be closed.

Spoke is one big kitchen today. An investor asked Jiraiya why it is not a food court like "everyone else". In this ticket you learn what microservices are, what they cost, and how to decide, so you can answer with numbers.

## Concept

### The monolith, and what is good about it

```callout jargon
A **monolith** is one application, built and deployed as one unit, usually with one database. All its parts run in the same process and call each other as plain Java methods.
```

Spoke's API is a monolith: one Spring Boot jar holds rentals, payments, fleet and riders, with one PostgreSQL database. "Monolith" is not an insult. It has real strengths:

- **Fast, safe calls.** A method call takes nanoseconds and never gets lost on a network.
- **One transaction.** Ending a ride and saving its fare commit together, as in [Transactions](/t/module-08-backend/topic-10-transactions).
- **Simple to run.** One build, one deploy, one log, one dashboard.
- **Easy to debug.** One stack trace shows the whole path of a request.
- **Easy to change across parts.** Renaming a method updates every caller in one commit.

The trouble starts with size. Twenty teams in one codebase step on each other. One slow test suite blocks everyone's release. A bug in a report can crash the process that unlocks bikes.

### The modular monolith

A **modular monolith** keeps one deployable app, but splits the code into modules with strong walls. Spoke's would have four: Rentals, Payments, Fleet and Riders.

The rules:

- Each module has a small public API, such as a `PaymentsApi` interface. Everything else is package-private.
- A module never touches another module's classes or tables directly. It asks through the public API.
- Each module owns its own tables. Payments' `payment` table is Payments' business alone.

Each module can be shaped like a hexagon from [Hexagonal architecture](/t/module-09-architecture/topic-02-hexagonal-architecture), with its own ports and adapters. Tools can check the walls: Maven modules, or Java's own module system. A test library such as ArchUnit fails the build when Rentals imports a Payments internal class.

The trap: walls nobody checks. One "quick" join across modules' tables, and the modules are welded together again.

### Microservices, defined

```callout jargon
A **microservice** is a small application that does one business job, owns its own data, and is deployed on its own. Microservices talk to each other only over the network. A **deploy** is putting a new version of an application into production.
```

In a microservice design, Rentals, Payments, Fleet and Riders become four separate applications. Each has its own repository or folder, its own build, its own database and, ideally, its own team.

The key word is **independent**. If you cannot change and deploy Payments without also deploying Rentals, you do not really have microservices. You have a monolith with network cables inside.

### What you gain

Microservices exist because big companies hit real limits. Here is what they buy, with Spoke examples.

| Benefit | What it means | Spoke example |
|---|---|---|
| **Independent deploys** | Ship one service without the others | Fix a Payments bug at noon without restarting unlocks |
| **Independent scaling** | Run more copies of just the busy part | Bike GPS pings: up to 2,000 bikes ÷ 10 s = 200 a second. Rides: 5,000 an hour ≈ 1.4 a second. Scale ping handling alone |
| **Team autonomy** | Each team owns its service end to end | A Fleet team picks its own release days |
| **Fault isolation** | One service failing does not crash the others | A memory leak in Riders' photo upload cannot kill Rentals |
| **Technology choice** | Each service can use a different language or database | Telemetry could store pings in a time-series database |

The scaling row is real arithmetic. Pings arrive about 140 times as often as rides start. In a monolith, adding servers for pings also copies rentals, payments and riders onto each one.

### What you pay

Every benefit has a price.

- **Network calls fail, and they are slow.** A method call takes a few nanoseconds. A call to another service in the same data centre takes around half a millisecond, plus turning objects into JSON and back. That is roughly 100,000 times slower, and it can time out.
- **Distributed data.** Each service has its own database, so no single transaction covers "charge and unlock". You need new patterns, like the saga below.
- **Operations work.** Four services mean four pipelines, four dashboards, four sets of alerts, and four things to upgrade.
- **Harder debugging.** One rider's tap passes through several services. You need traces that follow it, which a later ticket on observability covers.
- **Consistency.** Copies of data in different services disagree for a moment after each change.

```callout warning
Microservices do not make code simpler. They move complexity out of the code and into the network and the operations team. Make that trade only when the gains are real for you.
```

### The fallacies of distributed computing

In the 1990s, engineers at Sun Microsystems listed eight false beliefs that newcomers to networked systems hold. They still catch teams today.

| The false belief | The truth, at Spoke |
|---|---|
| The network is reliable | Calls get lost. Payments may never hear "charge rider-7" |
| Latency is zero | Every hop adds time. Five hops in a row add up |
| Bandwidth is infinite | Sending every GPS ping as big JSON fills the pipe |
| The network is secure | Anyone on the network may listen. Encrypt with TLS, even inside |
| Topology does not change | Servers come and go. Addresses change every deploy |
| There is one administrator | Cloud, network and database teams each control a piece |
| Transport cost is zero | Turning objects to JSON and back costs CPU, and cloud traffic costs money |
| The network is homogeneous | Phones, docks and servers speak different versions and protocols |

```callout jargon
A **fallacy** is a belief that sounds right but is false.
```

Every remote call needs an answer to "what if this is slow, lost, or answered twice?". That means timeouts, retries with idempotency keys, as in [REST design](/t/module-08-backend/topic-02-rest-design), and later, circuit breakers.

### Availability multiplies

```callout jargon
**Availability** is the share of time a service works, such as 99.9%. Over a 30-day month, 99.9% allows 0.1% × 43,200 minutes = 43.2 minutes of downtime.
```

Say unlocking a bike passes through five services in a row: the gateway, Rentals, Riders, Fleet and Payments. Each is up 99.9% of the time. The unlock only works when **all five** work. So multiply:

0.999 × 0.999 × 0.999 × 0.999 × 0.999 = 0.999⁵ ≈ 0.99501, which is **99.5%**.

That is about 215.6 minutes down a month: five times as much, about 3.6 hours. Each service looks great alone, yet the chain is much worse. The Try it lets you play with these numbers.

Two things help. **Shorter chains**: fewer services on the path of one request. **Spare copies**: take two copies of a service, where either can answer. If their failures are unrelated, both are down only 0.001 × 0.001 of the time. That "if" is the catch: two copies on one machine, or with the same bug, fail together.

### Conway's law and team size

In the late 1960s, Melvin Conway noticed a pattern, now called **Conway's law**. A system's design ends up copying the way its builders' teams talk to each other. Three teams building a compiler tend to produce a three-part compiler.

So service boundaries are really team boundaries. Each service should be owned by one team that can change it without meetings. Amazon's rule of thumb is a **two-pizza team**: small enough that two pizzas feed it, about six to ten people.

Companies also use it backwards, the **inverse Conway manoeuvre**: shape the teams first, and the architecture follows.

At Spoke, Tsunade, Kakashi and you are one small team. Four services for one team means four times the operations work, and no team autonomy gained. Conway's law says: one team, one deployable app, for now.

### Drawing boundaries: bounded contexts

Where should one service end and the next begin? Split by **business capability**, never by technical layer. A "database service" or a "validation service" is touched by every change.

Domain-Driven Design, a method by Eric Evans, calls each area a **bounded context**. It is a part of the business where every word has one clear meaning. The same word can mean different things in different contexts.

| Context | Owns | What "bike" means here |
|---|---|---|
| **Rentals** | rentals, ride state, fares | something a rider has for a while |
| **Payments** | charges, refunds, cards, monthly rider bills | nothing: it only sees amounts and payment ids |
| **Fleet** | bikes, docks, stations, repairs | a machine with a battery and brakes |
| **Riders** | accounts, memberships, login | something in a rider's ride history |

Two tests tell you a boundary is good:

- **High cohesion inside.** Things that change together live together. Fares and rentals change together, so both live in Rentals.
- **Loose coupling between.** One service can change its insides without the others noticing. If every Rentals change also needs a Payments change, the line is in the wrong place.

```callout jargon
**Cohesion** is how closely the parts of one module belong together. **Coupling** is how much one module depends on another. Aim for high cohesion and loose coupling.
```

A **chatty** boundary, with ten calls back and forth to finish one job, is a sign the line cuts through one capability.

### Database per service

Each service owns its data, and **no other service reads or writes its tables**. Payments asks Rentals for a rental through Rentals' API, never with a `SELECT` on the `rental` table.

A shared database looks easier. It is a trap:

- **Hidden coupling.** Rentals renames a column, and Payments breaks in production. Nothing in Payments' code changed.
- **No independent deploys.** Every schema change needs every team's agreement.
- **One bottleneck.** A heavy Analytics query slows down unlocks.
- **No clear owner.** Who decides what `rental.status` may hold?

So how does Payments know a rider's fare? Three ways: ask Rentals' API when needed, receive events and keep its own copy, or include the fare in the event itself. Some data is duplicated across services on purpose. That is normal, and the next ticket shows how events keep the copies in step.

### Sync calls and async events

Services talk in two ways.

**Synchronous request and response.** The caller waits for an answer.

- **REST** over HTTP with JSON, as in module 8. Easy to read and debug, and understood everywhere.
- **gRPC**, made by Google. It sends compact binary messages, defined in **Protocol Buffers** schema files, over HTTP/2. It is faster and strictly typed, which suits busy calls between services.

**Asynchronous events.** The sender publishes "this happened" and carries on. Any service that cares reacts later. The sender does not wait, and does not even know who listens.

| | Sync call | Async event |
|---|---|---|
| Caller waits? | yes | no |
| Both must be up? | yes | no: the event waits in a broker |
| Good for | questions that need an answer now: "is B-1042 free?" | news: "rental-17 ended" |
| Risk | long chains, availability multiplies | the reader is a little behind |

Use sync calls for questions the rider is waiting on. Use events for telling others what happened. A later ticket covers messaging in depth.

### Why not one big transaction?

Unlocking a bike touches Fleet's database (reserve the bike) and Payments' database (charge 100 cents). In the monolith, one transaction covered both. Across services, can we still have one?

There is a protocol for it: **two-phase commit** (**2PC**). A coordinator asks every database "can you commit?" (phase 1, **prepare**). Each locks its rows and votes yes or no. If all vote yes, the coordinator says "commit" (phase 2). Otherwise everyone rolls back.

Modern microservices avoid it:

- **It blocks.** If the coordinator dies after "prepare", every database waits with rows locked, maybe for minutes.
- **Everyone must be up.** One slow or down participant stalls them all. Availability multiplies again.
- **It is slow.** Extra round trips and long-held locks under rush-hour load.
- **Many tools do not support it.** Brokers, most cloud databases and payment providers offer no 2PC.

### Sagas: a chain of local transactions

```callout jargon
A **saga** is a sequence of local transactions, one per service. If a step fails, the saga runs **compensating actions** that undo the effects of the steps already done, in reverse order.
```

Each step commits on its own, straight away. There is no global rollback, so each step that changes something needs a matching "undo" step. Spoke's unlock saga:

| Step | Service | Action | Compensation if a later step fails |
|---|---|---|---|
| 1 | Fleet | reserve B-1042 for rider-7 | release the reservation |
| 2 | Payments | charge the 100 cent unlock fee | refund the 100 cents |
| 3 | Fleet | open the dock | (none: it is the last step) |

```animate
title: The unlock saga, when the dock jams
kind: scene
nodes:
  - { id: saga, label: Unlock saga, emoji: "🧭", value: started }
  - { id: fleet, label: Fleet, emoji: "🚲", value: "B-1042 free", col: 2 }
  - { id: payments, label: Payments, emoji: "💳", value: "no charge", row: 1, col: 1 }
  - { id: dock, label: Dock 4, emoji: "🔒", value: locked, row: 1, col: 3 }
steps:
  - say: rider-7 taps Unlock on B-1042. The saga starts, with three steps to run.
    focus: [saga]
  - say: Step 1. Fleet reserves the bike in its own database and commits.
    send: [{ from: saga, to: fleet, label: "reserve B-1042" }, { from: fleet, to: saga, label: reserved, back: true }]
    set: { fleet: "B-1042 reserved", saga: "step 1 done" }
  - say: Step 2. Payments charges 100 cents in its own database and commits. Two services have now changed.
    send: [{ from: saga, to: payments, label: "charge 100" }, { from: payments, to: saga, label: "pay-301", back: true }]
    set: { payments: "pay-301 charged", saga: "step 2 done" }
  - say: Step 3. Fleet tells dock 4 to open, but the lock is jammed. The step fails.
    send: [{ from: saga, to: dock, label: "open" }, { from: dock, to: saga, label: "jammed", back: true }]
    set: { saga: "step 3 failed" }
  - say: There is no rollback across services. So the saga runs compensations, newest first. Payments refunds the 100 cents.
    send: { from: saga, to: payments, label: "refund pay-301" }
    set: { payments: "pay-301 refunded", saga: compensating }
  - say: Then Fleet releases the reservation, so another rider can take B-1042.
    send: { from: saga, to: fleet, label: "release B-1042" }
    set: { fleet: "B-1042 free", saga: cancelled }
  - say: Everything is back to a correct state. But for a few seconds, rider-7 had a real charge and then a real refund, both visible.
    focus: [payments]
```

A compensation is not an undo button. It is a **new** action with its own effect: a refund appears on the rider's statement next to the charge. Some actions cannot be compensated at all. An email cannot be unsent; you can only send a second one.

### Choreography or orchestration

A saga can be run in two ways.

**Choreography**: no boss. Each service listens for events and reacts. Fleet publishes `BikeReserved`. Payments hears it, charges, and publishes `PaymentTaken`. Fleet hears that and opens the dock. On `DockFailed`, Payments refunds and Fleet releases.

**Orchestration**: one coordinator, the **orchestrator**, tells each service what to do, step by step, and decides the compensations. It is often its own small service, or part of the service that owns the process, here Rentals.

| | Choreography | Orchestration |
|---|---|---|
| Who knows the whole flow | nobody: it is spread across services | the orchestrator |
| Coupling | services only know events | the orchestrator knows every service |
| Easy to follow and change | hard once there are many steps | easy: the flow is in one place |
| Best for | two or three simple steps | longer flows with many failure paths |

Spoke would orchestrate unlocks from Rentals: the steps and their compensations stay readable in one class. Tools such as Temporal or Camunda run orchestrated sagas and remember where each one stopped, even across crashes.

### Saga traps

Sagas bring their own failure modes. Plan for each one.

- **Compensations must not fail for good.** Retry them until they succeed, so a refund that hits a timeout is tried again.
- **Every step and compensation must be idempotent.** A retried "refund pay-301" must not refund twice. Use the payment id as an idempotency key.
- **Put the risky, hard-to-undo step last.** The step after which the saga can no longer turn back is called the **pivot**. Opening the dock is last because a rider riding away cannot be undone.
- **Others see the middle.** Between steps, a reader sees "charged but not unlocked". There is no isolation, the I of ACID. Show clear states such as `UNLOCKING`, and never send "Ride started" before the last step succeeds.
- **Write the saga's state down.** Say the orchestrator crashes after step 2. On restart, it must know that it still owes step 3 or the compensations.

### Front doors and phone books: gateway and discovery

With many services, two questions appear. How does the rider app find the right service? And how do services find each other?

An **API gateway** is the single front door for outside clients. The app always calls `api.spoke.example`. The gateway sends `/rentals/**` to Rentals and `/riders/**` to Riders. It also does the jobs every service would otherwise repeat:

- checking the rider's login token
- rate limiting each app, as promised in [REST design](/t/module-08-backend/topic-02-rest-design)
- ending TLS, like the load balancer in [Networking](/t/module-08-backend/topic-13-networking)
- combining a few calls into one answer for a slow phone connection

Examples are Spring Cloud Gateway, Kong and cloud API gateways. Two traps: putting business rules in the gateway, and running only one copy, so it becomes a single point of failure.

**Service discovery** is the phone book. Service copies start and stop all day, each with a new address. So each copy registers itself, with health checks, in a **service registry** such as Consul or Netflix's Eureka. Callers ask the registry, or a load balancer that reads it, for a healthy address. Kubernetes, in a later ticket, gives every service a stable DNS name instead.

```callout jargon
A **single point of failure** is one part whose failure stops the whole system.
```

### Changing an API without breaking callers

In a monolith, renaming a method updates every caller in one commit. Across services, callers deploy on their own schedule. During a rolling deploy, old and new versions even run side by side. So every change must work with callers that have not changed yet.

The rules from [API docs, versioning and error design](/t/module-08-backend/topic-12-api-docs-errors) apply between services too. Add optional fields freely. Never rename, remove or change the meaning of one. Readers ignore fields they do not know: a **tolerant reader**.

For a needed breaking change, use **expand and contract**:

1. **Expand**: add the new field or endpoint beside the old one. Send both.
2. **Migrate**: move every caller to the new one, one deploy at a time.
3. **Contract**: once nobody uses the old one, remove it.

If the old shape must stay a long time, run versions side by side: `/api/v1/rentals` and `/api/v2/rentals`.

How do you know you did not break a caller? **Contract tests**. Each consumer writes down what it relies on: "GET /api/v1/rentals/{id} returns `riderId` as a string and `fareCents` as a number". The provider's build runs every consumer's contract, and fails before a breaking change ships. Pact and Spring Cloud Contract are the best-known tools. Because consumers write the contracts, this is called **consumer-driven contract testing**.

### The distributed monolith

The worst result is a **distributed monolith**: services that are split on paper, but still tightly coupled. You get every cost of microservices and none of the gains. Warning signs:

- Services must be deployed together, in a set order.
- Two or more services share one database.
- One rider tap makes a long chain of sync calls, Rentals to Riders to Fleet to Payments.
- A change to one feature needs pull requests in four repositories.
- When one service is down, all of them are useless.

The cure is usually fewer, bigger services, cut along bounded contexts.

### The strangler fig

How do you move from a monolith to services without a risky "big rewrite"? With the **strangler fig** pattern, named by Martin Fowler after a vine that grows around a tree until it replaces it.

1. Put a router, often the API gateway, in front of the monolith. At first it sends every request to the monolith.
2. Build one new service for one capability, such as Payments.
3. Switch the router to send `/payments/**` to the new service. Keep the monolith's code as a fallback for a while.
4. Delete that part of the monolith.
5. Repeat, one capability at a time, until the monolith is gone, or small enough to keep.

Each step is small, can be rolled back, and delivers value. The usual first service to pull out is the one with the clearest boundary and the strongest reason, such as very different scaling.

### How to decide

Most teams should **start with a modular monolith** and split a module out only for a real, measured reason:

- **Scaling**: one part needs far more capacity than the rest, like pings versus rides.
- **Teams**: several teams keep blocking each other in one codebase and one release.
- **Isolation**: one risky part keeps taking down important ones.
- **Technology**: one part truly needs a different runtime or database.
- **Compliance**: card data must live in a separate, locked-down system.

"Everyone else does it", "it is modern" and "it will be cleaner" are not reasons. Splitting is cheap to start and expensive to undo. In 2023 Amazon's Prime Video team even merged a monitoring tool's services back into one application, and reported a 90% lower cost.

So here is Spoke's answer for the investor. Spoke has one small team, so Conway's law says one deployable app. It keeps a modular monolith with four walled modules: Rentals, Payments, Fleet and Riders. Pings arrive about 140 times as often as rides start. So bike telemetry is the first candidate to split out, when its load demands it. With clean modules, that split is a strangler-fig step, not a rewrite.

### In an interview

Microservices come up in system design rounds and in "tell me about a design decision" questions. Spot them when you hear "scale independently", "many teams", "split the monolith", "consistency across services", or "a step in the middle failed".

- **Monolith or microservices for this product?** Weigh team size, scaling needs and operations cost. Defaulting to a modular monolith, with reasons, is a strong answer.
- **How do you split this system?** Bounded contexts and business capabilities, a database per service, high cohesion and loose coupling.
- **Book a trip with a flight, a hotel and a car**: a saga with compensations. Compare choreography and orchestration, and say why not two-phase commit.
- **Five services at 99.9% in a chain**: multiply, about 99.5%. Then shorten the chain, add spare copies, or go async.
- **Change an API used by other teams**: backward-compatible changes, expand and contract, versioning, and contract tests.

## Worked example

Here is Spoke's unlock saga as a tiny orchestrator in plain Java. Each step pairs an action with its compensation. The services are only print statements, so you can see the order of events.

```java run
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Main {
    /** One saga step: a local action in one service, and the compensating action that undoes its effect. */
    record Step(String name, Runnable action, Runnable compensation) {}

    public static void main(String[] args) {
        System.out.println("== rider-7 unlocks B-1042 at dock 4");
        runSaga(unlock("rider-7", "B-1042", "pay-301", 4, false));
        System.out.println("== rider-9 unlocks B-2001 at dock 6, which is jammed");
        runSaga(unlock("rider-9", "B-2001", "pay-302", 6, true));
    }

    static List<Step> unlock(String riderId, String bikeId, String paymentId, int dock, boolean jammed) {
        return List.of(
                new Step("reserve bike",
                        () -> System.out.println("  Fleet:    " + bikeId + " reserved for " + riderId),
                        () -> System.out.println("  Fleet:    " + bikeId + " released")),
                new Step("charge unlock fee",
                        () -> System.out.println("  Payments: " + paymentId + " charged 100 cents"),
                        () -> System.out.println("  Payments: " + paymentId + " refunded 100 cents")),
                new Step("open dock",
                        () -> {
                            if (jammed) {
                                throw new IllegalStateException("dock " + dock + " did not open");
                            }
                            System.out.println("  Fleet:    dock " + dock + " opened");
                        },
                        () -> { }));                          // the last step: nothing after it can fail
    }

    static void runSaga(List<Step> steps) {
        Deque<Step> done = new ArrayDeque<>();
        for (Step step : steps) {
            try {
                step.action().run();
                done.push(step);                          // newest on top
            } catch (RuntimeException failure) {
                System.out.println("  FAILED " + step.name() + ": " + failure.getMessage());
                while (!done.isEmpty()) {
                    done.pop().compensation().run();      // undo in reverse order
                }
                System.out.println("  result: unlock cancelled, every finished step undone");
                return;
            }
        }
        System.out.println("  result: ride started");
    }
}
```

It prints:

```text
== rider-7 unlocks B-1042 at dock 4
  Fleet:    B-1042 reserved for rider-7
  Payments: pay-301 charged 100 cents
  Fleet:    dock 4 opened
  result: ride started
== rider-9 unlocks B-2001 at dock 6, which is jammed
  Fleet:    B-2001 reserved for rider-9
  Payments: pay-302 charged 100 cents
  FAILED open dock: dock 6 did not open
  Payments: pay-302 refunded 100 cents
  Fleet:    B-2001 released
  result: unlock cancelled, every finished step undone
```

Line by line:

1. `Step` pairs each action with its compensation. Writing them side by side makes a missing "undo" easy to spot in review.
2. `runSaga` runs the steps in order. After each success, it pushes the step onto a stack, `done`.
3. When a step throws, the loop pops the stack, so compensations run newest first: refund, then release. Undoing in reverse order matters. Releasing the bike before refunding could let another rider take it while rider-9's charge still stands.
4. The failed step itself is not compensated: it never took effect.
5. Opening the dock is last, the pivot. Once the dock is open and the rider rides away, there is nothing to undo.

The work costs O(n) time for n steps, with O(n) extra space for the stack of finished steps.

A real orchestrator adds three things this toy skips. It saves its progress after every step, so a crash can resume. It retries failed compensations until they succeed. And each call carries an idempotency key, such as the payment id, so a retried step never charges twice.

## Try it

This is the availability arithmetic from earlier. Change the numbers and predict each answer before you run.

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        double each = 0.999;        // each service is up 99.9% of the time
        int chainLength = 5;        // services one request passes through, one after another
        int copies = 2;             // copies of one service, where any one copy can answer

        double chain = Math.pow(each, chainLength);
        double spare = 1 - Math.pow(1 - each, copies);

        report("one service", each);
        report("chain of " + chainLength, chain);
        report(copies + " copies", spare);
    }

    static void report(String label, double up) {
        double minutesPerMonth = 30 * 24 * 60;
        System.out.println(String.format(Locale.ROOT, "%-12s %.4f%% up, %6.2f minutes down a month",
                label + ":", up * 100, (1 - up) * minutesPerMonth));
    }
}
```

1. Make the chain 10 services long. How many hours a month is the unlock down now?
2. Set `each` to 0.99, "two nines". What happens to a chain of 5?
3. Put the chain back to 5 and `each` to 0.999. Now find, by trying values: how long can the chain get before downtime passes 6 hours a month?

## Recap

- A monolith is one deployable app. It gives fast in-process calls, one transaction, and simple operations. A modular monolith adds walls between modules that own their own tables.
- Microservices are small services, each owning its data and deployed on its own. They buy independent deploys, scaling, team autonomy and fault isolation, at the cost of network failures, distributed data and much more operations work.
- Availability multiplies along a chain of sync calls: five services at 99.9% give about 99.5%. Conway's law ties services to teams.
- Draw boundaries along bounded contexts, with a database per service. Across services, use sagas with compensating actions instead of two-phase commit, run by choreography or an orchestrator.
- Change APIs backward-compatibly, checked by contract tests. Avoid the distributed monolith, migrate with the strangler fig, and start with a modular monolith until a real reason to split appears.

## Common mistakes

**1. Splitting by technical layer.**

```text
rider-app -> web-service -> logic-service -> data-service -> database
```

Symptom: every new feature, such as "add CARGO bikes", needs changes and deploys in all three services, in the right order. Each request also pays three network hops. Fix: split by business capability, such as Rentals, Payments, Fleet and Riders, so one feature usually lives in one service.

**2. Sharing one database between services.**

```sql
-- in Payments' code, reading Rentals' table directly
SELECT fare_cents FROM rental WHERE id = 'rental-17';
```

Symptom: Rentals renames `fare_cents` to `fare_total_cents` in a migration, and Payments fails in production the same minute. Payments' code never changed, so nobody saw it coming. Fix: Payments asks Rentals' API, or keeps its own copy from events. Each table has exactly one owning service.

**3. Long chains of sync calls with no timeouts.**

```java nocompile
HttpClient client = HttpClient.newHttpClient();   // no connect timeout
HttpRequest request = HttpRequest.newBuilder(URI.create("http://fleet/bikes/B-1042")).build();   // no request timeout
```

Symptom: Fleet hangs, so every Rentals thread waiting on Fleet hangs too. Then the gateway's threads, waiting on Rentals, hang. One slow service freezes the whole chain. Fix: a timeout on every remote call (`HttpClient.newBuilder().connectTimeout(...)` and `HttpRequest.Builder.timeout(...)`), shorter chains, and events where nobody needs to wait.

**4. A saga step with no compensation.**

```java nocompile
reserveBike(bikeId, riderId);   // committed in Fleet
chargeUnlockFee(riderId);       // committed in Payments
openDock(dockId);               // throws: dock jammed, and nothing undoes the two steps above
```

Symptom: rider-9 is charged 100 cents and B-2001 stays reserved for a ride that never started. Itachi gets the support ticket. Fix: give every step that changes something a compensation, run them in reverse order on failure, and retry each until it succeeds.

**5. A breaking API change in one deploy.**

```json
{"rentalId": "rental-17", "riderId": "rider-7", "fare": 700}
```

Rentals renamed `fareCents` to `fare`. Payments still reads `record RentalView(String rentalId, String riderId, long fareCents)` with Jackson 3, and every read fails:

```text
tools.jackson.databind.exc.MismatchedInputException: Cannot map `null` into type `long` (set `DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES` to 'false' to allow)
```

Fix: expand and contract. Send `fare` beside `fareCents`, move every caller over, and only then remove `fareCents`. A contract test from Payments would have failed Rentals' build before it shipped.

**6. Splitting before there is a reason.**

```text
team: 3 engineers
services: rentals, payments, fleet, riders, notifications, pricing, gateway
```

Symptom: most of the week goes to pipelines, dashboards and version upgrades, and a simple feature needs four coordinated deploys. Fix: a modular monolith with strong walls. Split one module out when scaling, team or isolation needs prove it.

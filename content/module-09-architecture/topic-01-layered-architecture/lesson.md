## Hook

Picture a café with one worker. She takes orders, cooks, washes plates and runs the till. With three tables, that works. With fifty, everything waits on her, and nobody else can help, because only she knows where anything is.

Spoke's `RentalController` is that worker. It reads HTTP, checks the rules, keeps the rentals, and sends the whole rental back as JSON, payment token and all. Every change touches it, and every change risks breaking the rest. In this ticket you give each job its own place.

## Concept

### What architecture is

**Architecture** is the shape of your code: which big parts exist, what each part is for, and which parts may talk to which. It is the set of decisions that are expensive to change later.

```callout jargon
**Architecture**: the big structure of a program, its parts and the rules for how they connect. **Coupling**: how much one part depends on the details of another. **Cohesion**: how well the things inside one part belong together.
```

Good architecture aims for **low coupling** and **high cohesion**. Each part does one kind of work, and parts know as little as possible about each other's insides.

You already met the small version of this in [SOLID](/t/module-06-craft/topic-02-solid): one class, one reason to change. Architecture applies the same idea to whole groups of classes.

### Why it matters: the cost of change

Code is read and changed far more often than it is written. So the real cost of a design is the cost of the **next** change.

Spoke wants three things this month:

- the kiosk at Harbor should start rentals too, not only the app;
- the rentals table moves from H2 to PostgreSQL;
- the JSON must stop showing payment tokens.

In the god controller, all three changes land in one file, tangled together. The kiosk change risks breaking the app. The database change risks breaking the JSON. With layers, each change touches one layer only.

```callout warning
Architecture is a trade. Too little, and every change is scary. Too much, and a simple feature crosses ten files. Pick the least structure that keeps the next change cheap.
```

### The god controller

Here is a shortened version of the controller from Tsunade's ticket:

```java nocompile
@RestController
public class RentalController {
    private final Map<String, Rental> rentals = new LinkedHashMap<>();   // storage

    @PostMapping("/rentals")
    public ResponseEntity<?> start(@RequestBody StartRentalRequest request) {
        for (Rental rental : rentals.values()) {                          // business rule
            if (rental.isActive() && rental.getBikeId().equals(request.bikeId())) {
                return ResponseEntity.status(409).body("Bike is busy");   // error format
            }
        }
        Rental rental = new Rental(...);
        rentals.put(rental.getId(), rental);
        return ResponseEntity.created(...).body(rental);                  // the entity becomes JSON
    }
}
```

```callout jargon
A **god class** is a class that does far too many jobs. It knows everything and everyone depends on it, so it becomes hard to change, test or understand.
```

Four jobs live in one class: HTTP, rules, storage and error formats. The kiosk cannot reuse the rule without copying it. A test of the rule must send fake HTTP. And `body(rental)` sends every field of `Rental` to the phone.

### Four layers, one job each

A **layered architecture** splits the code into horizontal layers. Each layer has one job, and talks only to the layer below.

| Layer | Its one job | In Spoke |
|---|---|---|
| **Web** (presentation) | Turn HTTP into method calls, and results back into HTTP | `RentalController`, `StartRentalRequest`, `ApiErrors` |
| **Service** (application) | Run one use case: load, check rules, save, in one transaction | `RentalService.start`, `find`, `end`, and its result record `RentalResponse` |
| **Domain** | The business ideas and their rules, in plain Java | `Rental`, `RentalStatus`, `BikeUnavailableException` |
| **Persistence** (data access) | Store and load data | `RentalRepository`, `InMemoryRentalRepository` |

```callout jargon
A **layer** is a group of classes with one kind of job. A **use case** is one thing a user can do with the system, such as "start a rental". A **service** method usually runs one use case.
```

The domain is a little special. Every layer may use the domain classes, because they are the shared language of the business.

### A request travels through the layers

```animate
title: POST /rentals through the layers
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱" }
  - { id: web, label: RentalController, emoji: "🌐", col: 1 }
  - { id: svc, label: RentalService, emoji: "⚙️", col: 2 }
  - { id: repo, label: RentalRepository, emoji: "🗂️", col: 3 }
  - { id: db, label: Database, emoji: "🗄️", col: 4 }
  - { id: dom, label: Rental, emoji: "🚲", row: 1, col: 2 }
steps:
  - say: The rider taps Unlock on B-1042. The app sends JSON to the web layer.
    send: { from: app, to: web, label: "POST /rentals" }
  - say: The controller checks the JSON's shape, then turns it into one plain method call.
    send: { from: web, to: svc, label: "start(rider-7, B-1042, ...)" }
  - say: The service asks storage whether B-1042 is already out. Only the repository speaks SQL.
    send: [{ from: svc, to: repo, label: "findActiveByBikeId" }, { from: repo, to: db, label: "SELECT" }]
  - say: The bike is free. The service creates a Rental, the domain object that knows its own rules.
    send: { from: svc, to: dom, label: "new Rental(...)" }
  - say: The service saves the rental through the repository.
    send: [{ from: svc, to: repo, label: "save" }, { from: repo, to: db, label: "INSERT" }]
  - say: The service hands back a RentalResponse, a small record without the payment token.
    send: { from: svc, to: web, label: "RentalResponse", back: true }
  - say: The controller wraps it in HTTP, 201 Created plus a Location header. Each box only called the box below it.
    send: { from: web, to: app, label: "201 + Location", back: true }
```

### The dependency rule: only downward

The one rule that makes layers work: **a layer may depend on the layers below it, never on the layers above.**

```text
Web          →  may use Service, Domain
Service      →  may use its own DTOs, Persistence (its interface), Domain
Persistence  →  may use Domain
Domain       →  uses nothing from the other layers
```

Why downward? The lower layers are the stable heart of the system. Web screens change weekly. The rule "a bike can be out on one rental at a time" barely changes in years. Stable code should never depend on code that changes often.

This also gives you reuse. The kiosk controller, a nightly job and a test can all call the same `RentalService.start`. None of them needs HTTP to reach the rule.

```callout tip
Gojo says: when you are unsure where code belongs, ask "would the kiosk need this too?" If yes, it is below the web layer.
```

### Trap: skipping a layer

A shortcut that looks harmless: the controller calls the repository directly.

```java nocompile
@PostMapping("/kiosk/rentals")
public ResponseEntity<RentalResponse> startAtKiosk(@RequestBody StartRentalRequest request) {
    Rental rental = new Rental(repository.nextId(), ...);   // skipped the service
    repository.save(rental);                                // skipped the busy-bike rule
    return ResponseEntity.ok(RentalResponse.from(rental));
}
```

The busy-bike check lives in `RentalService.start`. This kiosk endpoint never runs it, so two riders can now rent B-1042 at once. The rule exists, but one door goes around it.

Reading through a repository with no rules at all, such as an admin list, is a gentler case. Some teams allow it. Writing around the service almost always ends in a broken rule.

### Trap: HTTP leaking down into the service

The opposite mistake: the service starts speaking HTTP.

```java nocompile
@Service
public class RentalService {
    public ResponseEntity<RentalResponse> start(HttpServletRequest request) {   // web types in the service
        String bikeId = request.getParameter("bikeId");
        if (busy(bikeId)) {
            return ResponseEntity.status(409).build();
        }
        // ...
    }
}
```

Now nothing but an HTTP request can call it. A nightly job, a message consumer from a later ticket, or the kiosk's own protocol all have to fake a servlet request. A plain unit test needs a mock request. And the caller must read status codes to learn what happened.

The fix: the service takes plain values, returns a domain object or a DTO, and **throws a domain exception** when a rule fails. The web layer decides what HTTP status that means.

### DTOs and entities are different things

An **entity** is the object the system stores. It has an identity, internal data and rules. A **DTO** (Data Transfer Object) is a simple record that holds exactly what one request or response needs.

```callout jargon
**Entity**: an object with an identity that the system stores, such as a `Rental`. **DTO**: a plain record shaped for one API message, such as `RentalResponse`. **Mapping**: copying the right fields from one to the other.
```

Never send an entity as JSON, or bind JSON straight into one. You saw why in [JPA and Hibernate](/t/module-08-backend/topic-09-jpa-hibernate):

- **Internal fields leak.** Jackson writes every getter. The payment token, fraud scores and audit columns all reach the phone.
- **Lazy loading misfires.** Jackson touches lazy links, which runs hidden queries or throws `LazyInitializationException`.
- **The API gets glued to the table.** Rename a column, and every app that reads the JSON breaks.
- **Mass assignment.** Bind a request into the entity, and a client can send `"status": "ENDED"` and have it saved.

### Syntax: request and response records

```java nocompile
public record StartRentalRequest(@NotBlank String riderId, @NotBlank String bikeId,
                                 @NotBlank String stationId, @NotBlank String paymentToken) {}

public record RentalResponse(String id, String riderId, String bikeId, String stationId, String status) {
    public static RentalResponse from(Rental rental) {
        return new RentalResponse(rental.getId(), rental.getRiderId(), rental.getBikeId(),
                rental.getStationId(), rental.getStatus().name());
    }
}
```

The request record is the shape the client may send. The response record is the shape the client may see. A static `from` method keeps the mapping in one place, next to the DTO.

The payment token comes **in** with the request, because the app needs to hand it over. It never goes **out**: `RentalResponse` simply has no field for it.

```java run
public class Main {
    public static void main(String[] args) {
        Rental entity = new Rental("rental-17", "rider-7", "B-1042", "harbor", "tok_visa_4242");
        System.out.println("entity:   " + entity);
        System.out.println("response: " + RentalResponse.from(entity));
    }
}

record Rental(String id, String riderId, String bikeId, String stationId, String paymentToken) {}

record RentalResponse(String id, String riderId, String bikeId, String stationId) {
    static RentalResponse from(Rental rental) {
        return new RentalResponse(rental.id(), rental.riderId(), rental.bikeId(), rental.stationId());
    }
}
```

Where should the mapping run? Either the service returns DTOs, or the controller maps. With JPA, map inside the service, while the transaction is still open, so lazy links can still load.

In this ticket the service returns `RentalResponse`, so that record belongs to the service layer: it is the service's API. The web layer only turns it into JSON. That keeps the arrows pointing down.

### Where validation lives

There are two kinds of "is this request OK?", and they live in different layers.

| Kind | Example | Where | How |
|---|---|---|---|
| **Shape** | `bikeId` is missing or blank | the edge, in the web layer | Bean Validation: `@NotBlank` on the request record, `@Valid` on the parameter |
| **Business rule** | B-1042 is already out | the domain or the service | plain Java, throwing a domain exception |

```callout jargon
**Bean Validation** is the standard set of annotations, such as `@NotBlank` and `@Positive`, that check an object's fields. Spring runs them when a parameter has `@Valid`. You used it in [Validation](/t/module-08-backend/topic-06-validation).
```

Shape checks need nothing but the request, so they run first and fail fast with `400`. Business rules need data from storage, so they run in the service and fail with `409` or `422`.

The trap is mixing them. An annotation like `@BikeAvailable` that queries the database hides a business rule at the edge. The kiosk, which skips that annotation, also skips the rule.

### Where @Transactional lives

`@Transactional` goes on **service** methods. A service method runs one use case. A use case is one unit of work: all of it happens, or none of it does. You learned the annotation in [Transactions](/t/module-08-backend/topic-10-transactions).

- **On a repository method only**: too small. `start` checks the bike, then saves. Two separate transactions leave a gap between them.
- **On the controller**: too big. The transaction, and its database connection, stays open while JSON is written and sent over a slow phone network.
- **On the service method**: just right. It covers the check and the save, and nothing else.

```java nocompile
@Service
public class RentalService {
    @Transactional
    public RentalResponse start(String riderId, String bikeId, String stationId, String paymentToken) {
        // check, create, save, map: one transaction
    }
}
```

Keep slow calls, like a payment provider over the network, outside the transaction where you can. A long transaction holds locks and a pooled connection while it waits.

```callout warning
A transaction makes `start` all-or-nothing. It does not make it race-free. At `READ COMMITTED`, two requests at the same moment can both run `findActiveByBikeId`, both see nothing, and both save. The real guard is in the database: a unique index or a row lock, as in [Concurrent writes](/t/module-08-backend/topic-15-concurrent-writes).
```

### Domain exceptions, mapped in one place

The service throws exceptions named in business words: `BikeUnavailableException`, `RentalNotFoundException`. They extend `RuntimeException` and know nothing about HTTP.

One class in the web layer, annotated `@RestControllerAdvice`, turns each one into an HTTP answer. It sends **Problem Details**, the standard JSON error format from [Validation](/t/module-08-backend/topic-06-validation).

```animate
title: A busy bike becomes a 409
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱" }
  - { id: web, label: RentalController, emoji: "🌐", col: 1 }
  - { id: svc, label: RentalService, emoji: "⚙️", col: 2 }
  - { id: adv, label: ApiErrors, emoji: "🧭", row: 1, col: 1 }
steps:
  - say: A second rider asks for B-1042, which is already out.
    send: { from: app, to: web, label: "POST /rentals" }
  - say: The controller calls the service as usual. It has no if-statements about errors.
    send: { from: web, to: svc, label: "start(...)" }
  - say: The service finds an active rental for B-1042 and throws a domain exception. It says what went wrong, not which status code to send.
    send: { from: svc, to: web, label: "BikeUnavailableException", back: true }
  - say: Spring sees the exception and hands it to the one advice class.
    send: { from: web, to: adv, label: "exception" }
  - say: ApiErrors maps it to 409 Conflict with a Problem Details body. Every controller gets the same error format for free.
    send: { from: adv, to: app, label: "409 problem+json", back: true }
```

### Syntax: @RestControllerAdvice with ProblemDetail

```java nocompile
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(BikeUnavailableException.class)
    public ProblemDetail bikeUnavailable(BikeUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://api.spoke.example/problems/bike-unavailable"));
        problem.setTitle("Bike unavailable");
        problem.setProperty("bikeId", ex.bikeId());
        return problem;
    }
}
```

One method per domain exception. Spring sends a returned `ProblemDetail` as `application/problem+json`, with the status from inside it.

Two traps. A `try/catch` in every controller method copies the mapping everywhere, and the copies drift apart. And `@ResponseStatus(HttpStatus.CONFLICT)` on the exception class puts a web annotation inside the domain layer, breaking the dependency rule.

### Anemic or rich domain model

Where does the rule "a rental ends only once" live? There are two styles.

```callout jargon
An **anemic domain model** has classes with only fields, getters and setters. All the rules sit in services. A **rich domain model** puts each rule inside the class whose data it guards.
```

```java run
public class Main {
    public static void main(String[] args) {
        Rental rental = new Rental("rental-17");
        rental.end();
        System.out.println("rental-17 is " + rental.status());
        try {
            rental.end();
        } catch (IllegalStateException e) {
            System.out.println("second end refused: " + e.getMessage());
        }
    }
}

class Rental {
    private final String id;
    private String status = "ACTIVE";

    Rental(String id) {
        this.id = id;
    }

    void end() {
        if (status.equals("ENDED")) {
            throw new IllegalStateException(id + " has already ended");
        }
        status = "ENDED";
    }

    String status() {
        return status;
    }
}
```

This `Rental` is rich: it has no `setStatus`, so no caller can skip the check. In an anemic model, every service that ends rentals must remember `if (status == ENDED) throw ...`. The one that forgets is the bug.

Martin Fowler, who named the anemic model, calls it an anti-pattern for complex rules. For a thin create-read-update-delete app with few rules, anemic classes are fine and simpler.

### Package by layer or by feature

How you arrange folders shows your architecture to every reader.

```text
package by layer                    package by feature
jq.spoke.controller                 jq.spoke.rental
    RentalController                    RentalController
    StationController                   RentalService
jq.spoke.service                        RentalRepository
    RentalService                       Rental
    StationService                  jq.spoke.station
jq.spoke.repository                     StationController
    RentalRepository                    StationService
    StationRepository                   ...
```

**By layer** is what most tutorials show. It is easy to start. But one feature is spread over many folders, and every class must be `public` so other packages can see it.

**By feature** keeps everything about rentals together. Classes can be package-private, so the station code cannot even reach `RentalRepository`. Big codebases lean this way. Inside each feature, the layers still exist.

### Testing each layer

Layers let you test each job on its own, which is faster and points straight at the broken part.

| Layer | Test | Speed |
|---|---|---|
| Domain | plain JUnit on `Rental.end()` | microseconds |
| Service | plain JUnit with a fake repository | milliseconds |
| Web | `@WebMvcTest` slice with a `@MockitoBean` service | about a second |
| Persistence | `@DataJpaTest` on a real database | seconds |
| All together | a few `@SpringBootTest` end-to-end tests | seconds |

A **fake** is a small working stand-in, such as an in-memory repository. It works because the service depends on the `RentalRepository` interface, not on a database class.

```java run
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        RentalService service = new RentalService(new FakeRentalRepository());
        System.out.println(service.start("B-1042"));
        try {
            service.start("B-1042");
        } catch (IllegalStateException e) {
            System.out.println("refused: " + e.getMessage());
        }
    }
}

interface RentalRepository {
    Optional<String> findActiveRentalOf(String bikeId);
    void save(String rentalId, String bikeId);
}

class FakeRentalRepository implements RentalRepository {
    private final Map<String, String> activeByBike = new HashMap<>();

    public Optional<String> findActiveRentalOf(String bikeId) {
        return Optional.ofNullable(activeByBike.get(bikeId));
    }

    public void save(String rentalId, String bikeId) {
        activeByBike.put(bikeId, rentalId);
    }
}

class RentalService {
    private final RentalRepository rentals;
    private int count = 0;

    RentalService(RentalRepository rentals) {
        this.rentals = rentals;
    }

    String start(String bikeId) {
        if (rentals.findActiveRentalOf(bikeId).isPresent()) {
            throw new IllegalStateException(bikeId + " is already out");
        }
        String id = "rental-" + (++count);
        rentals.save(id, bikeId);
        return "started " + id + " on " + bikeId;
    }
}
```

No Spring, no HTTP, no database, and the rule is fully tested. Keep most tests at this level, and only a few end to end. This shape is often called the **test pyramid**.

### The costs, and when thin is fine

Layers are not free.

- **Pass-through layers.** `find(id)` in the controller calls `find(id)` in the service, which calls `findById(id)` in the repository. Three methods do one thing.
- **Boilerplate.** A request record, a response record and a mapper for every endpoint. **Boilerplate** is code you must write but that adds no new idea.
- **More files to open** for a small change.

```callout jargon
**Boilerplate** is repeated code that carries no new logic, like getters or copy-this-field mappings. A **pass-through** method only forwards its call to the next layer.
```

For a small internal tool with no business rules, a controller plus a repository can be enough. Add the service layer when the first real rule, transaction or second caller appears. Being able to say **why** you chose the lighter shape matters more than the shape itself.

### In an interview

Layering appears in system design rounds and in "how would you structure this service?" questions. It also hides inside code reviews: spot the god class, the leaking entity, or the service that returns HTTP.

- **"Walk me through your service's structure."** Name the four layers, each one's job, and the downward dependency rule.
- **"Why not return the JPA entity from the controller?"** Internal fields, lazy loading, mass assignment, and an API glued to the table.
- **"Where do validation and transactions go?"** Shape at the edge with Bean Validation, rules in the domain or service, `@Transactional` on service methods.
- **"How do you turn errors into HTTP?"** Domain exceptions plus one `@RestControllerAdvice` that sends Problem Details.
- **"Package by layer or by feature?"** By feature for larger systems, because it keeps a feature together and lets you hide its insides.

## Worked example

Here is a layered version of a different endpoint: riders reporting a broken bike. It has all four layers, the DTOs and the error advice. MockMvc sends it four requests without starting a server.

```java run
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

public class Main {
    public static void main(String[] args) throws Exception {
        // Hide Spring's log lines, so you only see the answers.
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.ERROR);
        RepairService service = new RepairService(new InMemoryRepairRepository());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RepairController(service))
                .setControllerAdvice(new ApiErrors())
                .build();

        String report = """
                {"bikeId":"B-1042","problem":"flat tyre","reporterPhone":"555-0142"}""";
        show(mvc, post("/repairs").contentType(MediaType.APPLICATION_JSON).content(report));
        show(mvc, post("/repairs").contentType(MediaType.APPLICATION_JSON).content(report));
        show(mvc, get("/repairs/repair-9"));
        show(mvc, post("/repairs").contentType(MediaType.APPLICATION_JSON).content("{\"bikeId\":\"B-2001\"}"));
    }

    static void show(MockMvc mvc, MockHttpServletRequestBuilder request) throws Exception {
        var response = mvc.perform(request).andReturn().getResponse();
        System.out.println(response.getStatus() + " " + response.getContentAsString());
    }
}

// ---------- web layer: HTTP in, HTTP out ----------
record ReportRepairRequest(@NotBlank String bikeId, @NotBlank String problem, @NotBlank String reporterPhone) {}

record RepairResponse(String id, String bikeId, String problem, String status) {
    static RepairResponse from(Repair repair) {
        return new RepairResponse(repair.id(), repair.bikeId(), repair.problem(), repair.status());
    }
}

@RestController
@RequestMapping("/repairs")
class RepairController {
    private final RepairService repairs;

    RepairController(RepairService repairs) {
        this.repairs = repairs;
    }

    @PostMapping
    ResponseEntity<RepairResponse> report(@Valid @RequestBody ReportRepairRequest request) {
        RepairResponse repair = repairs.report(request.bikeId(), request.problem(), request.reporterPhone());
        return ResponseEntity.created(URI.create("/repairs/" + repair.id())).body(repair);
    }

    @GetMapping("/{id}")
    RepairResponse find(@PathVariable String id) {
        return repairs.find(id);
    }
}

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(RepairAlreadyOpenException.class)
    ProblemDetail alreadyOpen(RepairAlreadyOpenException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Repair already open");
        return problem;
    }

    @ExceptionHandler(RepairNotFoundException.class)
    ProblemDetail notFound(RepairNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Repair not found");
        return problem;
    }
}

// ---------- service layer: one method per use case ----------
class RepairService {
    private final RepairRepository repairs;

    RepairService(RepairRepository repairs) {
        this.repairs = repairs;
    }

    RepairResponse report(String bikeId, String problem, String reporterPhone) {
        if (repairs.findOpenByBike(bikeId).isPresent()) {
            throw new RepairAlreadyOpenException(bikeId);
        }
        Repair repair = new Repair(repairs.nextId(), bikeId, problem, reporterPhone, "OPEN");
        repairs.save(repair);
        return RepairResponse.from(repair);
    }

    RepairResponse find(String id) {
        return repairs.findById(id).map(RepairResponse::from).orElseThrow(() -> new RepairNotFoundException(id));
    }
}

// ---------- domain: business words, no framework ----------
record Repair(String id, String bikeId, String problem, String reporterPhone, String status) {}

class RepairAlreadyOpenException extends RuntimeException {
    RepairAlreadyOpenException(String bikeId) {
        super("Bike " + bikeId + " already has an open repair.");
    }
}

class RepairNotFoundException extends RuntimeException {
    RepairNotFoundException(String id) {
        super("No repair with id " + id + ".");
    }
}

// ---------- persistence layer: an interface and one implementation ----------
interface RepairRepository {
    String nextId();
    void save(Repair repair);
    Optional<Repair> findById(String id);
    Optional<Repair> findOpenByBike(String bikeId);
}

class InMemoryRepairRepository implements RepairRepository {
    private final Map<String, Repair> rows = new LinkedHashMap<>();
    private int last = 0;

    public String nextId() {
        return "repair-" + (++last);
    }

    public void save(Repair repair) {
        rows.put(repair.id(), repair);
    }

    public Optional<Repair> findById(String id) {
        return Optional.ofNullable(rows.get(id));
    }

    public Optional<Repair> findOpenByBike(String bikeId) {
        return rows.values().stream().filter(r -> r.bikeId().equals(bikeId) && r.status().equals("OPEN")).findFirst();
    }
}
```

It prints:

```text
201 {"id":"repair-1","bikeId":"B-1042","problem":"flat tyre","status":"OPEN"}
409 {"detail":"Bike B-1042 already has an open repair.","instance":"/repairs","status":409,"title":"Repair already open"}
404 {"detail":"No repair with id repair-9.","instance":"/repairs/repair-9","status":404,"title":"Repair not found"}
400 
```

Step by step:

1. **The first report.** The controller checks the shape with `@Valid`, then makes one call to `report`. The service checks the rule, builds a `Repair`, saves it, and maps it to a `RepairResponse`. The JSON has no `reporterPhone`: the rider's phone number stays inside.
2. **The same bike again.** The service throws `RepairAlreadyOpenException`. The controller has no error code at all. `ApiErrors` turns the exception into `409` with a Problem Details body.
3. **An unknown id.** Same path, different exception, `404`.
4. **A request with only a bikeId.** `@Valid` stops it at the edge with `400`, before the service runs. In a full Spring Boot app, a handler like the one in [Validation](/t/module-08-backend/topic-06-validation) would also give it a Problem Details body.

To move from memory to PostgreSQL, you would write a `JdbcRepairRepository` and change nothing else. That is the payoff of the dependency rule.

## Try it

This tiny layered app has no Spring, so you can see the layers plainly. Run it, then break the rules on purpose.

```java run
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        RentalRepository repository = new RentalRepository();
        RentalService service = new RentalService(repository);
        KioskController kiosk = new KioskController(service);

        System.out.println(kiosk.unlock("rider-7", "B-1042"));
        System.out.println(kiosk.unlock("rider-8", "B-1042"));
        System.out.println(kiosk.unlock("rider-8", "B-2001"));
    }
}

// web layer: turns a kiosk tap into a call, and the result into screen text
class KioskController {
    private final RentalService service;

    KioskController(RentalService service) {
        this.service = service;
    }

    String unlock(String riderId, String bikeId) {
        try {
            return "screen: ride started, " + service.start(riderId, bikeId);
        } catch (IllegalStateException e) {
            return "screen: sorry, " + e.getMessage();
        }
    }
}

// service layer: the rule
class RentalService {
    private final RentalRepository repository;

    RentalService(RentalRepository repository) {
        this.repository = repository;
    }

    String start(String riderId, String bikeId) {
        if (repository.isOut(bikeId)) {
            throw new IllegalStateException(bikeId + " is already out");
        }
        return repository.save(riderId, bikeId);
    }
}

// persistence layer: storage only
class RentalRepository {
    private final Map<String, String> riderByBike = new HashMap<>();
    private int count = 0;

    boolean isOut(String bikeId) {
        return riderByBike.containsKey(bikeId);
    }

    String save(String riderId, String bikeId) {
        riderByBike.put(bikeId, riderId);
        return "rental-" + (++count);
    }
}
```

1. Make `KioskController` skip the service: give it the repository and call `repository.save` directly. What happens to the second unlock of B-1042?
2. Put it back. Then change `start` to return the screen text itself, `"screen: sorry, ..."`. Why would a phone app now be unhappy calling the same service?
3. Add a second controller, `AppController`, that returns `"201 rental-1"` style text, using the same service. How many copies of the busy-bike rule do you need?

## Recap

- Architecture is the shape of the code: its big parts and which may talk to which. Good shape keeps the next change cheap.
- Layers split the work: web (HTTP), service (use cases, transactions), domain (business ideas and rules), persistence (storage). Dependencies point downward only.
- Send DTOs, never entities: internal fields, lazy loading and table changes must not reach the JSON.
- Shape checks live at the edge with Bean Validation. Business rules live in the domain or service, and fail with domain exceptions that one `@RestControllerAdvice` maps to Problem Details.
- Layers cost boilerplate and pass-through calls. Test each layer on its own, and keep the structure as light as the problem allows.

## Common mistakes

**1. Returning the entity from the controller.**

```java nocompile
@GetMapping("/rentals/{id}")
public Rental find(@PathVariable String id) {
    return repository.findById(id).orElseThrow();
}
```

```text
{"id":"rental-1","riderId":"rider-7","bikeId":"B-1042","stationId":"harbor","paymentToken":"tok_visa_4242","active":true,"status":"ACTIVE"}
```

That is the god controller from the practice, answering `GET /rentals/rental-1`. Jackson wrote every getter, so the payment token and the internal `active` flag went to the phone. Fix: return a `RentalResponse` record built with `RentalResponse.from(rental)`. A field the record does not have can never leak.

**2. Returning ResponseEntity from the service.**

```java nocompile
@Service
public class RentalService {
    public ResponseEntity<RentalResponse> start(String riderId, String bikeId) {
        if (busy(bikeId)) {
            return ResponseEntity.status(409).build();
        }
        // ...
    }
}
```

```text
The service layer must not know about HTTP. Return domain objects or DTOs and throw domain exceptions; the controller and ApiErrors deal with HTTP. Found: [start returns org.springframework.http.ResponseEntity<jq.m09.layered.RentalResponse>]
```

That is the layering check from this ticket's practice. The nightly job and the kiosk now have to read HTTP status codes. Fix: return the `RentalResponse`, throw `BikeUnavailableException` for a busy bike, and let `ApiErrors` choose `409`.

**3. A second door around the rules.**

```java nocompile
@PostMapping("/kiosk/rentals")
public RentalResponse startAtKiosk(@RequestBody StartRentalRequest request) {
    Rental rental = new Rental(repository.nextId(), request.riderId(), request.bikeId(), ...);
    repository.save(rental);
    return RentalResponse.from(rental);
}
```

```text
screen: ride started, rental-1
screen: ride started, rental-2
```

That is the Try it program with the kiosk calling the repository: two riders got B-1042. Fix: every entry point calls `RentalService.start`. Only the service talks to the repository.

**4. Putting HTTP annotations on domain exceptions.**

```java nocompile
@ResponseStatus(HttpStatus.CONFLICT)
public class BikeUnavailableException extends RuntimeException { ... }
```

It works, and that is why it spreads. But the domain now imports `org.springframework.web`, and the status lives far from the other error formats. A message consumer that throws the same exception gets an annotation that means nothing to it. Fix: keep the exception plain, and map it in the one `@RestControllerAdvice`.

**5. @Transactional in the wrong layer.**

```java nocompile
@RestController
@Transactional            // the whole request, including writing JSON, is one transaction
public class RentalController { ... }
```

Symptom: under load, the connection pool runs dry, because each connection stays busy while slow phones download their JSON. The opposite mistake, `@Transactional` only on repository methods, splits the check and the save into two transactions. Fix: put `@Transactional` on the public service method that runs the use case.

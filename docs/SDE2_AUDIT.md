# SDE 2 audit of the written modules (00–08)

Written 2026-11-18. Nine reviewers, one per module, read every lesson, quiz and practice brief and judged them against what an SDE 2 at a big tech firm must know, for a learner who starts with no computer science.

This file is the tracker for the fixes. Tick a line when it is done and checked.

## Verdict

- The beginner layer is accurate and well paced in every module. Code runs, errors are real, quizzes have one answer.
- The gap is depth. Most modules stop at the happy path. What an SDE 2 is judged on is missing: interview patterns (module 04), concurrency failure modes (05), database internals and locking (08), low-level design (06), undoing and shipping safely (07), and how values really behave (01–03).
- Money is a `double` in modules 01–05 lessons, against the course rule of whole cents in a `long`, and the switch is never explained.

## How fixes are made

- **Existing tickets:** new `###` subsections and corrected lines in `lesson.md`, new quiz questions where a new concept needs checking. Existing `###` headings are never renamed (saved places in a lesson are matched by heading). Practice code of existing tickets is not changed: the learner's `src/` must keep compiling against the tests.
- **New tickets:** new topic folders with new ids. They are placed with `order` (the catalog sorts by `order`, then id, and the ticket gate follows that order). A new topic before the boss moves the boss's prerequisite to it. Keys continue the module's `SPK-MMTT` numbering.
- **Money:** practice signatures that return `double` stay. Lessons say why real billing uses whole cents, and teach `long` cents and `BigDecimal`.

## Phase 1: modules 00–03, lesson fixes only

### Module 00
- [x] topic-01: bits, bytes, binary and hex; the operating system and processes; how fast each part is; swap
- [x] topic-02: interpreter and JIT, warm-up, "compiled or interpreted? both"
- [x] topic-04: PATH and JAVA_HOME, `which java`, several JDKs; JRE is a legacy term; vendors and LTS; jargon callout for repository, build tool, library, Maven
- [x] topic-05: the file name rule is for a **public** class
- [x] topic-06: `git diff` (and `--staged`), `git restore`, `git show`, `.gitignore`; fix "Older versions of Git call it master" with `init.defaultBranch`; explain `>>`; commit message convention

### Module 01
- [x] topic-02: overflow, `Math.addExact`, int-times-int before widening; all 8 primitives; rounding, `Math.round/floor/ceil`, casts cut toward zero; local `var`; double is wrong for money (callout)
- [x] topic-03: edge cases (divide by zero, `Infinity`, `NaN`, `%` with negatives, `Math.floorMod`); prefix vs postfix `++`; `%` is remainder, not modulo; bitwise operators
- [x] topic-04: validate input with `hasNextInt` or read-then-parse
- [x] topic-05: short-circuit `&&` `||`; ternary operator; money callout
- [x] topic-06: `-1` as an error signal for a price (callout)
- [x] topic-09: Java passes a copy
- [x] topic-10: two names, one array; `Arrays.toString/equals/copyOf/fill`
- [x] topic-11: the string pool and why `==` sometimes works; `"Harbor".equals(x)`; chars are numbers; `Locale.ROOT`
- [x] topic-12: money in cents, not dollars (`0.1 + 0.2`), `BigDecimal` preview; locale-safe formatting
- [x] one definition of "object" across topics 04, 11, 12; fill-blank quiz questions with two valid answers

### Module 02
- [x] topic-02: constructors check their rules (`Objects.requireNonNull`)
- [x] topic-03: the four access levels; getters that leak; behaviour, not setters; silent validation vs throwing
- [x] topic-04: when not to use static; a `public static final int[]` is not constant; money callout
- [x] topic-05: `final` classes and methods; constructors calling overridable methods; JDK 25 note on `super(...)`; define "signature"
- [x] topic-06: what is chosen at compile time (overloads, static hiding, fields)
- [x] topic-07: fix the `Vehicle`/`ClassicBike` reference
- [x] topic-08: static and private interface methods, default-method clashes, constants; abstract class vs interface
- [x] topic-09: why inheritance breaks (fragile base class)
- [x] topic-10: the equals rules, `instanceof` vs `getClass()`, mutable keys, `Objects.equals`
- [x] topic-11: immutable classes and defensive copies; final reference vs immutable object; throw in the compact constructor
- [x] topic-12: `EnumMap`, `EnumSet`, per-constant bodies, enums implementing interfaces, never store `ordinal()`
- [x] nested classes: static nested vs inner (topic-04)

### Module 03
- [x] topic-02: `List.copyOf` vs unmodifiable views, `Arrays.asList`, `List.of` rejects nulls
- [x] topic-03 / 06: sorted collections use `compareTo`; mutable keys
- [x] topic-04: `merge`, `computeIfAbsent`, `putIfAbsent`, multimaps; `NavigableMap`
- [x] topic-05: wildcards and PECS; type erasure and raw types
- [x] topic-07 / 08: rules for real services (multi-catch, `Error`, log or rethrow, `IllegalStateException` vs `IllegalArgumentException`, `requireNonNull`); suppressed exceptions; `return` in `finally`
- [x] topic-10: large files, safe writes with `ATOMIC_MOVE`; the `Files.exists` race
- [x] topic-11: `Instant`, `ZoneId`, `ZonedDateTime`, daylight saving time; fix "Local is fine"; `ChronoUnit.between` vs `getDays()`
- [x] topic-12: effectively final, the four method-reference kinds; remove subtraction comparators
- [x] topic-13: checked exceptions inside lambdas; money as `double`
- [x] topic-14: `anyMatch/allMatch/noneMatch/min/max`, `IntStream.range`, `boxed`, `toSet`
- [x] topic-15: when not to use a stream, side effects, parallel streams, `reduce` on strings
- [x] topic-16: `orElseThrow`, `flatMap`, `ifPresentOrElse`, the `isPresent`+`get` anti-pattern
- [x] `BigDecimal` (new subsection, topic-11 or a money card in topic-08)

## Phase 2: module 04, data structures and algorithms

Fixes in existing topics:
- [ ] topic-01: amortised cost; two inputs and hidden loops; bound vs case; O(2^n), O(n!)
- [ ] topic-02: how deep Java can recurse, explicit stack; fib growth ~1.6^n consistent with topic-12
- [ ] topic-03: lower and upper bound; binary search on the answer; rotated array; duplicates and insertion point
- [ ] topic-05: quicksort stack O(n) worst case; Dual-Pivot Quicksort vs TimSort; quickselect
- [ ] topic-06: reversing a list (trace); fast and slow pointers, dummy head, merge two lists; ArrayDeque beats LinkedList
- [ ] topic-07: the `Deque` interface; monotonic stack and deque
- [ ] topic-08: how Java's HashMap really works (treeified buckets, power-of-two tables); hash patterns; LRU cache
- [ ] topic-09: level-order traversal, pass values up, validate a BST, lowest common ancestor, delete, `TreeMap` navigation
- [ ] topic-10: top-k with a size-k heap, k-way merge, two heaps for the median; heapify in O(n); comparator overflow
- [ ] topic-11: program to the interface
- [ ] topic-12: explain the `waysToDock` and week-pass recurrences

New tickets (before the boss):
- [ ] topic-13 Two pointers, sliding window and prefix sums
- [ ] topic-14 Backtracking
- [ ] topic-15 Directed graphs: topological sort, cycles and grids
- [ ] topic-16 Weighted shortest paths: Dijkstra
- [ ] topic-17 Dynamic programming patterns
- [ ] topic-18 Greedy algorithms and intervals
- [ ] topic-19 Tries and union-find
- [ ] topic-20 Bit manipulation

## Phase 3: module 05, advanced Java

- [ ] topic-01: processes, threads, cores, context switches; interruption
- [ ] topic-02: deadlock's four conditions, finding a deadlock with a thread dump, `tryLock`, livelock and starvation, private lock objects
- [ ] topic-03: production executors (`ThreadPoolExecutor`, bounded queues, rejection, sizing, named threads), `submit` swallows exceptions, shutdown sequence, timeouts
- [ ] topic-04: which thread runs a stage, `allOf` failure, `orTimeout`, `cancel` does not interrupt
- [ ] topic-05: pinning fixed in JDK 24, file I/O; limit with a `Semaphore`
- [ ] topic-06: ConcurrentHashMap pitfalls
- [ ] topic-07: diagnosing OutOfMemoryError, heap dumps, container memory
- [ ] topic-08: fix "G1 does most of that work while your program runs"; GC tuning basics
- [ ] topic-09: class initialisation is thread-safe; `NoSuchMethodError` from version conflicts
- [ ] money as `double` in topics 08, 09, 11, 12 (callouts)
- New tickets:
  - [ ] topic-14 The Java Memory Model: visibility, `volatile`, happens-before, safe publication (placed after topic-02)
  - [ ] topic-15 Coordinating threads: latches, semaphores, barriers, read-write locks (placed after topic-06)

## Phase 4: module 06, craft

- [ ] topic-02: Liskov contract rules (Rectangle/Square)
- [ ] topic-05: State, Template Method, Chain of Responsibility, Proxy; when not to use a pattern
- [ ] topic-06: more code smells
- [ ] topic-07: `Optional` is not for fields; `..>` arrow
- [ ] topic-08: unit, integration and end-to-end tests; coverage and mutation testing; more parameterized sources
- [ ] topic-09: choosing a test double, a hand-written fake; void methods and strict stubbing
- [ ] topic-10: testing randomness
- [ ] topic-11: debugging in production; JDK 15 note; `... N more`
- [ ] topic-12: `{}` still evaluates arguments; declined payment is INFO; log once at the boundary; per-package levels; clear the MDC
- New ticket:
  - [ ] topic-13 Object-oriented design from requirements (placed after topic-07)

## Phase 5: module 07, tooling

- [ ] topic-01: dependency conflicts, `dependency:tree`, exclusions, BOMs, semantic versioning, fat jars
- [ ] topic-02: Gradle's "highest wins"
- [ ] topic-03: commit messages, `git clone`, SSH keys
- [ ] topic-04: pin actions by SHA; secret masking limits; unit vs integration tests in CI; dependency scanning
- [ ] topic-05: config from the environment, secrets never in images, non-root user, JVM memory in containers, `docker compose`
- New tickets:
  - [ ] topic-06 Undo and recover with Git (placed after topic-03)
  - [ ] topic-07 Shipping to production: environments, canaries, rollbacks, feature flags (placed after topic-04)
  - [ ] topic-08 The shell on call: pipes, grep, logs, processes, ports, curl, ssh (placed first)

## Phase 6: module 08, backend

- [ ] topic-01: timeouts in `SpokeClient`, JSON with Jackson; idempotent means same state; HTTP/2 and HTTP/3; 429
- [ ] topic-02: idempotent end, Idempotency-Key races; timeouts, retries, backoff with jitter; cursor pagination
- [ ] topic-03: application.properties, profiles, environment variables
- [ ] topic-07: `TIMESTAMP WITH TIME ZONE`
- [ ] topic-08: connection pools
- [ ] topic-09: persistence context, dirty checking, `LazyInitializationException`, open-in-view, DTOs vs entities, Testcontainers, `save` and `merge`
- [ ] topic-10: rollback does not undo remote calls, `REQUIRES_NEW`
- [ ] topic-11: sessions vs JWT, XSS, CORS, object-level authorization, keeping secrets out of code
- New tickets:
  - [ ] topic-13 Networking under HTTP: IP, DNS, TCP, TLS (placed first)
  - [ ] topic-14 Database design and performance: indexes, EXPLAIN, constraints, normalization, migrations (after topic-07)
  - [ ] topic-15 Concurrent writes: isolation anomalies, optimistic and pessimistic locking (after topic-10)

## Phase 7: planned modules 09 and 10

Add to the curriculum before they are written:
- [ ] module 09: rate limiting (token bucket), circuit breakers, Kubernetes basics
- [ ] module 10: a low-level design round in interview prep

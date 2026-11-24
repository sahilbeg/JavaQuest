# Curriculum

Generated from one topic list together with `build_state.json`. Topic IDs are stable: `<module>/topic-NN-<slug>`.

## Rules

- Each module unlocks when the previous module's boss is beaten. Module 0 is open from the start.
- Inside a module, each topic needs the topic before it. Extra cross-module prerequisites are listed per topic.
- Every topic follows: Hook -> Concept -> Worked example -> Try it -> Quiz -> Coding challenge -> Recap and common mistakes.
- Practice `code` means a JUnit exercise in `practice/`. Practice `quiz` means quiz plus a guided activity (tool topics, theory topics). Mastery for `quiz` topics is the quiz alone.
- Every module ends with a boss challenge, shown as an incident (`INC-MM`), and a mini-project, shown as a feature (`FEAT-MM`). The story for each is in `docs/STORY.md`.
- Every topic is shown as a ticket keyed `SPK-MMTT`, for example `SPK-0105`. Tickets added after a module was first built keep the next free key, so a key can be out of step with the `#` column (for example SPK-0813 comes first in module 08). Modules inserted later carry a letter in their id and the next free numbers in their keys: `module-09a-platform` is SPK-11TT, INC-11 and FEAT-11, and `module-09b-data` is SPK-12TT, INC-12 and FEAT-12. They sit between modules 09 and 10.

## module-00-foundations: How computers and programs work (project: Day one)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-00-foundations/topic-01-how-computers-work` | How computers work (CPU, memory, storage) | quiz | none |
| 2 | `module-00-foundations/topic-02-what-is-a-program` | What a program is: source, compiler, bytecode, JVM | quiz | `module-00-foundations/topic-01-how-computers-work` |
| 3 | `module-00-foundations/topic-03-terminal-basics` | The terminal: files, folders, commands | quiz | `module-00-foundations/topic-02-what-is-a-program` |
| 4 | `module-00-foundations/topic-04-setup-jdk-ide` | Installing the JDK and an IDE | quiz | `module-00-foundations/topic-03-terminal-basics` |
| 5 | `module-00-foundations/topic-05-hello-world` | Your first program: Hello, World | code | `module-00-foundations/topic-04-setup-jdk-ide` |
| 6 | `module-00-foundations/topic-06-git-basics` | Git basics: init, add, commit, log | quiz | `module-00-foundations/topic-05-hello-world` |
| boss | `module-00-foundations/boss` | INC-00: The kiosk welcome screen is broken on launch day | code | `module-00-foundations/topic-06-git-basics` |
| mini-project | `module-00-foundations/mini-project` | FEAT-00: A printed welcome card for new riders | code | `module-00-foundations/topic-06-git-basics` |

## module-01-basics: Java basics (project: The fare kiosk)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-01-basics/topic-01-variables` | Variables: boxes with labels | code | `module-00-foundations/boss` |
| 2 | `module-01-basics/topic-02-primitive-types` | Primitive types and casting | code | `module-01-basics/topic-01-variables` |
| 3 | `module-01-basics/topic-03-operators` | Operators and expressions | code | `module-01-basics/topic-02-primitive-types` |
| 4 | `module-01-basics/topic-04-console-io` | Input and output with the console | code | `module-01-basics/topic-03-operators` |
| 5 | `module-01-basics/topic-05-if-else` | Decisions: if, else, boolean logic | code | `module-01-basics/topic-04-console-io` |
| 6 | `module-01-basics/topic-06-switch` | Switch statements and switch expressions | code | `module-01-basics/topic-05-if-else` |
| 7 | `module-01-basics/topic-07-while-loops` | Repeating: while and do-while | code | `module-01-basics/topic-06-switch` |
| 8 | `module-01-basics/topic-08-for-loops` | Counting loops: for, break, continue | code | `module-01-basics/topic-07-while-loops` |
| 9 | `module-01-basics/topic-09-methods` | Methods: parameters, return values, scope | code | `module-01-basics/topic-08-for-loops` |
| 10 | `module-01-basics/topic-10-arrays` | Arrays and 2D arrays | code | `module-01-basics/topic-09-methods` |
| 11 | `module-01-basics/topic-11-strings` | Strings and their methods | code | `module-01-basics/topic-10-arrays` |
| 12 | `module-01-basics/topic-12-stringbuilder-formatting` | StringBuilder and text formatting | code | `module-01-basics/topic-11-strings` |
| boss | `module-01-basics/boss` | INC-01: The kiosk charges the wrong fare after midnight | code | `module-01-basics/topic-12-stringbuilder-formatting` |
| mini-project | `module-01-basics/mini-project` | FEAT-01: Ride receipt printer | code | `module-01-basics/topic-12-stringbuilder-formatting` |

## module-02-oop: Object-Oriented Programming (project: Modelling the fleet)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-02-oop/topic-01-classes-objects` | Classes and objects: blueprints and houses | code | `module-01-basics/boss` |
| 2 | `module-02-oop/topic-02-constructors` | Constructors and this | code | `module-02-oop/topic-01-classes-objects` |
| 3 | `module-02-oop/topic-03-encapsulation` | Encapsulation: private fields and getters | code | `module-02-oop/topic-02-constructors` |
| 4 | `module-02-oop/topic-04-static-members` | Static fields and methods | code | `module-02-oop/topic-03-encapsulation` |
| 5 | `module-02-oop/topic-05-inheritance` | Inheritance and super | code | `module-02-oop/topic-04-static-members` |
| 6 | `module-02-oop/topic-06-polymorphism` | Polymorphism and overriding | code | `module-02-oop/topic-05-inheritance` |
| 7 | `module-02-oop/topic-07-abstract-classes` | Abstract classes | code | `module-02-oop/topic-06-polymorphism` |
| 8 | `module-02-oop/topic-08-interfaces` | Interfaces and default methods | code | `module-02-oop/topic-07-abstract-classes` |
| 9 | `module-02-oop/topic-09-composition` | Composition over inheritance | code | `module-02-oop/topic-08-interfaces` |
| 10 | `module-02-oop/topic-10-equals-hashcode-tostring` | equals, hashCode and toString | code | `module-02-oop/topic-09-composition` |
| 11 | `module-02-oop/topic-11-records` | Records | code | `module-02-oop/topic-10-equals-hashcode-tostring` |
| 12 | `module-02-oop/topic-12-enums` | Enums with fields and methods | code | `module-02-oop/topic-11-records` |
| boss | `module-02-oop/boss` | INC-02: E-bikes are billed as normal bikes | code | `module-02-oop/topic-12-enums` |
| mini-project | `module-02-oop/mini-project` | FEAT-02: The membership system | code | `module-02-oop/topic-12-enums` |

## module-03-core-java: Core Java (project: Ride data)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-03-core-java/topic-01-collections-overview` | The collections framework map | code | `module-02-oop/boss` |
| 2 | `module-03-core-java/topic-02-lists` | List, ArrayList, LinkedList | code | `module-03-core-java/topic-01-collections-overview` |
| 3 | `module-03-core-java/topic-03-sets` | Set, HashSet, TreeSet | code | `module-03-core-java/topic-02-lists` |
| 4 | `module-03-core-java/topic-04-maps` | Map, HashMap, TreeMap | code | `module-03-core-java/topic-03-sets` |
| 5 | `module-03-core-java/topic-05-generics` | Generics and bounded types | code | `module-03-core-java/topic-04-maps`, `module-02-oop/topic-08-interfaces` |
| 6 | `module-03-core-java/topic-06-comparators-iterators` | Comparable, Comparator, Iterator | code | `module-03-core-java/topic-05-generics` |
| 7 | `module-03-core-java/topic-07-exceptions` | Exceptions: try, catch, finally | code | `module-03-core-java/topic-06-comparators-iterators` |
| 8 | `module-03-core-java/topic-08-custom-exceptions` | Checked vs unchecked, custom exceptions, try-with-resources | code | `module-03-core-java/topic-07-exceptions` |
| 9 | `module-03-core-java/topic-09-file-io` | Reading and writing files (java.io) | code | `module-03-core-java/topic-08-custom-exceptions` |
| 10 | `module-03-core-java/topic-10-nio-files` | NIO: Path and Files | code | `module-03-core-java/topic-09-file-io` |
| 11 | `module-03-core-java/topic-11-date-time` | java.time: dates, times, durations | code | `module-03-core-java/topic-10-nio-files` |
| 12 | `module-03-core-java/topic-12-lambdas` | Lambdas and method references | code | `module-03-core-java/topic-11-date-time` |
| 13 | `module-03-core-java/topic-13-functional-interfaces` | Functional interfaces | code | `module-03-core-java/topic-12-lambdas` |
| 14 | `module-03-core-java/topic-14-streams-basics` | Streams: filter, map, collect | code | `module-03-core-java/topic-13-functional-interfaces` |
| 15 | `module-03-core-java/topic-15-streams-advanced` | Streams: grouping, reducing, flatMap | code | `module-03-core-java/topic-14-streams-basics` |
| 16 | `module-03-core-java/topic-16-optional` | Optional | code | `module-03-core-java/topic-15-streams-advanced` |
| boss | `module-03-core-java/boss` | INC-03: The monthly ride report is wrong | code | `module-03-core-java/topic-16-optional` |
| mini-project | `module-03-core-java/mini-project` | FEAT-03: Ride-log analytics | code | `module-03-core-java/topic-16-optional` |

## module-04-dsa: Data structures and algorithms (project: Moving bikes around)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-04-dsa/topic-01-big-o` | Complexity and Big-O | code | `module-03-core-java/boss` |
| 2 | `module-04-dsa/topic-02-recursion` | Recursion | code | `module-04-dsa/topic-01-big-o` |
| 3 | `module-04-dsa/topic-03-searching` | Linear and binary search | code | `module-04-dsa/topic-02-recursion` |
| 4 | `module-04-dsa/topic-04-simple-sorts` | Bubble, selection, insertion sort | code | `module-04-dsa/topic-03-searching` |
| 5 | `module-04-dsa/topic-05-merge-quick-sort` | Merge sort and quicksort | code | `module-04-dsa/topic-04-simple-sorts` |
| 6 | `module-04-dsa/topic-06-linked-lists` | Build a linked list | code | `module-04-dsa/topic-05-merge-quick-sort` |
| 7 | `module-04-dsa/topic-07-stacks-queues` | Stacks and queues | code | `module-04-dsa/topic-06-linked-lists` |
| 8 | `module-04-dsa/topic-08-hash-tables` | Build a hash table | code | `module-04-dsa/topic-07-stacks-queues`, `module-02-oop/topic-10-equals-hashcode-tostring` |
| 9 | `module-04-dsa/topic-09-trees-bst` | Binary trees and BSTs | code | `module-04-dsa/topic-08-hash-tables` |
| 10 | `module-04-dsa/topic-10-heaps` | Heaps and priority queues | code | `module-04-dsa/topic-09-trees-bst` |
| 11 | `module-04-dsa/topic-11-graphs` | Graphs: BFS and DFS | code | `module-04-dsa/topic-10-heaps` |
| 12 | `module-04-dsa/topic-12-dynamic-programming` | Dynamic programming basics | code | `module-04-dsa/topic-11-graphs` |
| 13 | `module-04-dsa/topic-13-two-pointers` | Two pointers, sliding windows and prefix sums | code | `module-04-dsa/topic-12-dynamic-programming` |
| 14 | `module-04-dsa/topic-14-backtracking` | Backtracking: trying every choice | code | `module-04-dsa/topic-13-two-pointers` |
| 15 | `module-04-dsa/topic-15-topological-sort` | Directed graphs: order, cycles and grids | code | `module-04-dsa/topic-14-backtracking` |
| 16 | `module-04-dsa/topic-16-dijkstra` | Weighted shortest paths: Dijkstra | code | `module-04-dsa/topic-15-topological-sort` |
| 17 | `module-04-dsa/topic-17-dp-patterns` | Dynamic programming patterns | code | `module-04-dsa/topic-16-dijkstra` |
| 18 | `module-04-dsa/topic-18-greedy-intervals` | Greedy choices and intervals | code | `module-04-dsa/topic-17-dp-patterns` |
| 19 | `module-04-dsa/topic-19-tries-union-find` | Tries and union-find | code | `module-04-dsa/topic-18-greedy-intervals` |
| 20 | `module-04-dsa/topic-20-bit-manipulation` | Bit manipulation | code | `module-04-dsa/topic-19-tries-union-find` |
| boss | `module-04-dsa/boss` | INC-04: The route planner sends riders to full stations | code | `module-04-dsa/topic-20-bit-manipulation` |
| mini-project | `module-04-dsa/mini-project` | FEAT-04: Rebalancing-truck planner | code | `module-04-dsa/topic-20-bit-manipulation` |

## module-05-advanced-java: Advanced Java (project: Rush hour)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-05-advanced-java/topic-01-threads` | Threads and Runnable | code | `module-04-dsa/boss` |
| 2 | `module-05-advanced-java/topic-02-synchronization` | Race conditions, synchronized, locks | code | `module-05-advanced-java/topic-01-threads` |
| 3 | `module-05-advanced-java/topic-03-executors` | Executors and thread pools | code | `module-05-advanced-java/topic-02-synchronization` |
| 4 | `module-05-advanced-java/topic-04-completable-future` | CompletableFuture | code | `module-05-advanced-java/topic-03-executors`, `module-03-core-java/topic-12-lambdas` |
| 5 | `module-05-advanced-java/topic-05-virtual-threads` | Virtual threads | code | `module-05-advanced-java/topic-04-completable-future` |
| 6 | `module-05-advanced-java/topic-06-concurrent-collections` | Concurrent collections and atomics | code | `module-05-advanced-java/topic-05-virtual-threads` |
| 7 | `module-05-advanced-java/topic-07-jvm-memory` | JVM memory: stack, heap, metaspace | quiz | `module-05-advanced-java/topic-06-concurrent-collections` |
| 8 | `module-05-advanced-java/topic-08-garbage-collection` | Garbage collection | quiz | `module-05-advanced-java/topic-07-jvm-memory` |
| 9 | `module-05-advanced-java/topic-09-classloading` | Class loading | quiz | `module-05-advanced-java/topic-08-garbage-collection` |
| 10 | `module-05-advanced-java/topic-10-reflection` | Reflection | code | `module-05-advanced-java/topic-09-classloading` |
| 11 | `module-05-advanced-java/topic-11-annotations` | Annotations and a tiny processor | code | `module-05-advanced-java/topic-10-reflection` |
| 12 | `module-05-advanced-java/topic-12-sealed-types` | Sealed types | code | `module-05-advanced-java/topic-11-annotations` |
| 13 | `module-05-advanced-java/topic-13-pattern-matching` | Pattern matching for instanceof, switch and records | code | `module-05-advanced-java/topic-12-sealed-types` |
| 14 | `module-05-advanced-java/topic-14-memory-model` | The Java Memory Model: visibility and volatile | code | `module-05-advanced-java/topic-13-pattern-matching` |
| 15 | `module-05-advanced-java/topic-15-coordinating-threads` | Coordinating threads: latches, semaphores and barriers | code | `module-05-advanced-java/topic-14-memory-model` |
| boss | `module-05-advanced-java/boss` | INC-05: Double unlock at rush hour | code | `module-05-advanced-java/topic-15-coordinating-threads` |
| mini-project | `module-05-advanced-java/mini-project` | FEAT-05: Bike telemetry ingester | code | `module-05-advanced-java/topic-15-coordinating-threads` |

## module-06-craft: Engineering craft (project: Paying off debt)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-06-craft/topic-01-clean-code` | Clean code: names, functions, comments | code | `module-05-advanced-java/boss` |
| 2 | `module-06-craft/topic-02-solid` | SOLID principles | code | `module-06-craft/topic-01-clean-code` |
| 3 | `module-06-craft/topic-03-creational-patterns` | Creational patterns: factory, builder, singleton | code | `module-06-craft/topic-02-solid` |
| 4 | `module-06-craft/topic-04-structural-patterns` | Structural patterns: adapter, decorator, facade | code | `module-06-craft/topic-03-creational-patterns` |
| 5 | `module-06-craft/topic-05-behavioral-patterns` | Behavioral patterns: strategy, observer, command | code | `module-06-craft/topic-04-structural-patterns` |
| 6 | `module-06-craft/topic-06-refactoring` | Refactoring safely | code | `module-06-craft/topic-05-behavioral-patterns` |
| 7 | `module-06-craft/topic-07-uml-basics` | UML class and sequence diagrams | quiz | `module-06-craft/topic-06-refactoring` |
| 8 | `module-06-craft/topic-08-junit` | Testing with JUnit 5 | code | `module-06-craft/topic-07-uml-basics` |
| 9 | `module-06-craft/topic-09-mockito` | Mocks with Mockito | code | `module-06-craft/topic-08-junit`, `module-02-oop/topic-08-interfaces` |
| 10 | `module-06-craft/topic-10-tdd` | Test-driven development | code | `module-06-craft/topic-09-mockito` |
| 11 | `module-06-craft/topic-11-debugging` | Debugging with breakpoints and stack traces | code | `module-06-craft/topic-10-tdd` |
| 12 | `module-06-craft/topic-12-logging` | Logging with SLF4J | code | `module-06-craft/topic-11-debugging` |
| 13 | `module-06-craft/topic-13-object-oriented-design` | Object-oriented design from requirements | code | `module-06-craft/topic-12-logging` |
| boss | `module-06-craft/boss` | INC-06: Refactor the legacy billing module safely | code | `module-06-craft/topic-13-object-oriented-design` |
| mini-project | `module-06-craft/mini-project` | FEAT-06: Pluggable promo engine | code | `module-06-craft/topic-13-object-oriented-design` |

## module-07-tooling: Build and tooling (project: Shipping safely)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-07-tooling/topic-01-maven` | Maven: POM, lifecycle, dependencies | quiz | `module-06-craft/boss` |
| 2 | `module-07-tooling/topic-02-gradle` | Gradle basics | quiz | `module-07-tooling/topic-01-maven` |
| 3 | `module-07-tooling/topic-03-git-workflow` | Git branches, merges, rebases, pull requests | quiz | `module-07-tooling/topic-02-gradle` |
| 4 | `module-07-tooling/topic-04-ci-basics` | Continuous integration basics | quiz | `module-07-tooling/topic-03-git-workflow` |
| 5 | `module-07-tooling/topic-05-docker-basics` | Docker basics: images and containers | quiz | `module-07-tooling/topic-04-ci-basics` |
| 6 | `module-07-tooling/topic-06-git-undo` | Undo and recover with Git | quiz | `module-07-tooling/topic-05-docker-basics` |
| 7 | `module-07-tooling/topic-07-shipping-to-production` | Shipping to production: deploys, rollbacks and flags | quiz | `module-07-tooling/topic-06-git-undo` |
| 8 | `module-07-tooling/topic-08-shell-on-call` | The shell on call: logs, processes and servers | quiz | `module-07-tooling/topic-07-shipping-to-production` |
| boss | `module-07-tooling/boss` | INC-07: The release pipeline is red | code | `module-07-tooling/topic-08-shell-on-call` |
| mini-project | `module-07-tooling/mini-project` | FEAT-07: Package the kiosk as a runnable jar | code | `module-07-tooling/topic-08-shell-on-call` |

## module-08-backend: Backend engineering (project: The Spoke API)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-08-backend/topic-13-networking` | Networking under HTTP: IP, DNS, TCP and TLS | quiz | `module-07-tooling/boss` |
| 2 | `module-08-backend/topic-01-http` | How HTTP works | quiz | `module-08-backend/topic-13-networking` |
| 3 | `module-08-backend/topic-02-rest-design` | REST and API design | quiz | `module-08-backend/topic-01-http` |
| 4 | `module-08-backend/topic-03-spring-boot-intro` | Spring Boot: your first app | code | `module-08-backend/topic-02-rest-design` |
| 5 | `module-08-backend/topic-04-dependency-injection` | Dependency injection and beans | code | `module-08-backend/topic-03-spring-boot-intro`, `module-06-craft/topic-02-solid` |
| 6 | `module-08-backend/topic-05-spring-mvc` | Controllers and request mapping | code | `module-08-backend/topic-04-dependency-injection` |
| 7 | `module-08-backend/topic-06-validation` | Validation and error responses | code | `module-08-backend/topic-05-spring-mvc` |
| 8 | `module-08-backend/topic-07-sql-basics` | SQL basics | code | `module-08-backend/topic-06-validation` |
| 9 | `module-08-backend/topic-14-database-design` | Database design and performance: indexes and migrations | code | `module-08-backend/topic-07-sql-basics` |
| 10 | `module-08-backend/topic-08-jdbc` | JDBC | code | `module-08-backend/topic-14-database-design` |
| 11 | `module-08-backend/topic-09-jpa-hibernate` | JPA and Hibernate | code | `module-08-backend/topic-08-jdbc`, `module-08-backend/topic-07-sql-basics` |
| 12 | `module-08-backend/topic-10-transactions` | Transactions | code | `module-08-backend/topic-09-jpa-hibernate` |
| 13 | `module-08-backend/topic-15-concurrent-writes` | Concurrent writes: lost updates and locking | code | `module-08-backend/topic-10-transactions` |
| 14 | `module-08-backend/topic-11-spring-security` | Security basics with Spring Security | code | `module-08-backend/topic-15-concurrent-writes` |
| 15 | `module-08-backend/topic-12-api-docs-errors` | API docs, versioning and error design | code | `module-08-backend/topic-11-spring-security` |
| boss | `module-08-backend/boss` | INC-08: Half-finished rentals after a crash | code | `module-08-backend/topic-12-api-docs-errors` |
| mini-project | `module-08-backend/mini-project` | FEAT-08: The station availability API | code | `module-08-backend/topic-12-api-docs-errors` |

## module-09-architecture: Architecture (project: Growing up)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-09-architecture/topic-01-layered-architecture` | Layered architecture | code | `module-08-backend/boss` |
| 2 | `module-09-architecture/topic-02-hexagonal-architecture` | Hexagonal architecture: ports and adapters | code | `module-09-architecture/topic-01-layered-architecture`, `module-06-craft/topic-02-solid` |
| 3 | `module-09-architecture/topic-03-microservices` | Microservices: when and why | quiz | `module-09-architecture/topic-02-hexagonal-architecture` |
| 4 | `module-09-architecture/topic-04-messaging` | Messaging, events and the outbox pattern | code | `module-09-architecture/topic-03-microservices` |
| 5 | `module-09-architecture/topic-05-caching` | Caching | code | `module-09-architecture/topic-04-messaging` |
| 6 | `module-09-architecture/topic-06-system-design` | System design basics | quiz | `module-09-architecture/topic-05-caching` |
| 7 | `module-09-architecture/topic-07-performance-profiling` | Performance and profiling | code | `module-09-architecture/topic-06-system-design` |
| 8 | `module-09-architecture/topic-08-observability` | Logs, metrics, traces and health checks | quiz | `module-09-architecture/topic-07-performance-profiling` |
| 9 | `module-09-architecture/topic-09-resilience` | Resilience: rate limits, retries and circuit breakers | code | `module-09-architecture/topic-08-observability` |
| 10 | `module-09-architecture/topic-10-kubernetes` | Running on Kubernetes | quiz | `module-09-architecture/topic-09-resilience` |
| boss | `module-09-architecture/boss` | INC-09: The live map falls over at peak | code | `module-09-architecture/topic-10-kubernetes` |
| mini-project | `module-09-architecture/mini-project` | FEAT-09: Event-driven billing | code | `module-09-architecture/topic-10-kubernetes` |

## module-09a-platform: Platform and DevOps (project: Ship it everywhere)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-09a-platform/topic-01-dockerfiles` | Production Dockerfiles: layers, multi-stage builds and small, safe images | code | `module-09-architecture/boss` |
| 2 | `module-09a-platform/topic-02-docker-compose` | Docker Compose: running Spoke and its database locally | quiz | `module-09a-platform/topic-01-dockerfiles` |
| 3 | `module-09a-platform/topic-03-github-actions` | GitHub Actions in depth: workflows, caching, secrets and releases | code | `module-09a-platform/topic-02-docker-compose` |
| 4 | `module-09a-platform/topic-04-kubernetes-manifests` | Kubernetes manifests: Deployments, Services, probes and rollouts | code | `module-09a-platform/topic-03-github-actions` |
| 5 | `module-09a-platform/topic-05-helm-kustomize` | Helm and Kustomize: one app, many environments | quiz | `module-09a-platform/topic-04-kubernetes-manifests` |
| 6 | `module-09a-platform/topic-06-infrastructure-as-code` | Infrastructure as code: Terraform, state and GitOps | quiz | `module-09a-platform/topic-05-helm-kustomize` |
| 7 | `module-09a-platform/topic-07-config-and-secrets` | Configuration and secrets: twelve-factor apps and secret stores | code | `module-09a-platform/topic-06-infrastructure-as-code` |
| 8 | `module-09a-platform/topic-08-monitoring-stack` | The monitoring stack: Prometheus, PromQL, Grafana and alerts | quiz | `module-09a-platform/topic-07-config-and-secrets` |
| boss | `module-09a-platform/boss` | INC-11: The deploy that took Spoke down | code | `module-09a-platform/topic-08-monitoring-stack` |
| mini-project | `module-09a-platform/mini-project` | FEAT-11: One-click deploys | code | `module-09a-platform/topic-08-monitoring-stack` |

## module-09b-data: Data in depth (project: Know your data)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-09b-data/topic-01-advanced-sql` | Advanced SQL: joins, window functions and CTEs | code | `module-09a-platform/boss` |
| 2 | `module-09b-data/topic-02-index-internals` | How indexes work: B-trees, LSM trees and query plans | code | `module-09b-data/topic-01-advanced-sql` |
| 3 | `module-09b-data/topic-03-transaction-internals` | Inside transactions: MVCC, the write-ahead log and isolation | quiz | `module-09b-data/topic-02-index-internals` |
| 4 | `module-09b-data/topic-04-nosql-modeling` | NoSQL data modelling: key-value, document and wide-column | quiz | `module-09b-data/topic-03-transaction-internals` |
| 5 | `module-09b-data/topic-05-redis-patterns` | Redis patterns: caches, counters, leaderboards and locks | code | `module-09b-data/topic-04-nosql-modeling` |
| 6 | `module-09b-data/topic-06-replication-sharding` | Replication and sharding: lag, quorums and consistent hashing | code | `module-09b-data/topic-05-redis-patterns` |
| 7 | `module-09b-data/topic-07-data-pipelines` | Data pipelines: batch, streams and DAG schedulers like Airflow | code | `module-09b-data/topic-06-replication-sharding` |
| 8 | `module-09b-data/topic-08-postgres-in-production` | Postgres in production: pooling, partitions, JSONB and backups | quiz | `module-09b-data/topic-07-data-pipelines` |
| boss | `module-09b-data/boss` | INC-12: Month-end reports grind the database | code | `module-09b-data/topic-08-postgres-in-production` |
| mini-project | `module-09b-data/mini-project` | FEAT-12: The rider statements data mart | code | `module-09b-data/topic-08-postgres-in-production` |

## module-10-professional: Professional skills (project: Full-time offer)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-10-professional/topic-01-code-reviews` | Giving and receiving code reviews | quiz | `module-09b-data/boss` |
| 2 | `module-10-professional/topic-02-reading-codebases` | Reading large codebases | quiz | `module-10-professional/topic-01-code-reviews` |
| 3 | `module-10-professional/topic-03-documentation` | Writing documentation | quiz | `module-10-professional/topic-02-reading-codebases` |
| 4 | `module-10-professional/topic-04-estimation` | Estimation and planning | quiz | `module-10-professional/topic-03-documentation` |
| 5 | `module-10-professional/topic-05-interview-prep` | Interview prep: coding, design and behavioural | code | `module-10-professional/topic-04-estimation` |
| 6 | `module-10-professional/topic-06-capstone` | Capstone: full-stack Java service | code | `module-10-professional/topic-05-interview-prep` |
| boss | `module-10-professional/boss` | INC-10: The mock interview loop | code | `module-10-professional/topic-06-capstone` |
| mini-project | `module-10-professional/mini-project` | CAPSTONE: Launch Spoke in Bramford | code | `module-10-professional/topic-06-capstone` |

## Totals

- Modules: 13
- Lessons: 149
- Graded units including bosses and mini-projects: 175

# Web courses: the web pool

These modules live in `tracks/web/` and make up the course `frontend-react` in `courses.yaml` (later web courses reuse web-00 to web-07). The world is Dhaaga & Co. (`docs/STORY.md`). The same rules apply as above, except:

- Ticket keys are `WEB-MMTT` (module and topic number). Incidents are `INC-MM` and features `FEAT-MM`, with `MM` the web module number; react-10's feature is `CAPSTONE`.
- Practice `code` means a Vitest exercise in `tracks/web/practice/` (`runner: vitest`). Pages add `preview: true`.
- `web-00-how-the-web-works` is open from the start of the Frontend course.

## web-00-how-the-web-works: How the web works (project: A visiting card online)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `web-00-how-the-web-works/topic-01-how-a-page-arrives` | How a page reaches your phone: browsers, servers, URLs | quiz | none |
| 2 | `web-00-how-the-web-works/topic-02-html-css-js` | HTML, CSS and JavaScript: three languages, three jobs | quiz | `web-00-how-the-web-works/topic-01-how-a-page-arrives` |
| 3 | `web-00-how-the-web-works/topic-03-devtools` | The browser's DevTools: Elements, Console and Network | quiz | `web-00-how-the-web-works/topic-02-html-css-js` |
| 4 | `web-00-how-the-web-works/topic-04-editor-and-files` | VS Code, files and folders for a website | quiz | `web-00-how-the-web-works/topic-03-devtools` |
| 5 | `web-00-how-the-web-works/topic-05-first-page` | Your first web page | code | `web-00-how-the-web-works/topic-04-editor-and-files` |
| 6 | `web-00-how-the-web-works/topic-06-first-javascript` | Your first JavaScript: console.log | code | `web-00-how-the-web-works/topic-05-first-page` |
| 7 | `web-00-how-the-web-works/topic-07-git-basics` | Git basics: save points for your code | quiz | `web-00-how-the-web-works/topic-06-first-javascript` |
| boss | `web-00-how-the-web-works/boss` | INC-00: The ₹ sign shows as garbage on Raju's phone | code | `web-00-how-the-web-works/topic-07-git-basics` |
| mini-project | `web-00-how-the-web-works/mini-project` | FEAT-00: A one-page visiting card with the UPI QR | code | `web-00-how-the-web-works/topic-07-git-basics` |

## web-01-html: HTML (project: The catalogue page)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `web-01-html/topic-01-elements-and-tags` | Elements, tags and attributes | code | `web-00-how-the-web-works/boss` |
| 2 | `web-01-html/topic-02-text-and-headings` | Headings, paragraphs and text | code | `web-01-html/topic-01-elements-and-tags` |
| 3 | `web-01-html/topic-03-links-and-images` | Links and images | code | `web-01-html/topic-02-text-and-headings` |
| 4 | `web-01-html/topic-04-lists` | Lists | code | `web-01-html/topic-03-links-and-images` |
| 5 | `web-01-html/topic-05-tables` | Tables for the price list | code | `web-01-html/topic-04-lists` |
| 6 | `web-01-html/topic-06-semantic-layout` | Page structure: header, nav, main, section, footer | code | `web-01-html/topic-05-tables` |
| 7 | `web-01-html/topic-07-forms` | Forms: sending what the customer types | code | `web-01-html/topic-06-semantic-layout` |
| 8 | `web-01-html/topic-08-form-inputs-and-validation` | Input types and built-in validation | code | `web-01-html/topic-07-forms` |
| 9 | `web-01-html/topic-09-accessibility` | Accessibility: pages everyone can use | code | `web-01-html/topic-08-form-inputs-and-validation` |
| 10 | `web-01-html/topic-10-head-and-meta` | The head: title, meta tags and the viewport | code | `web-01-html/topic-09-accessibility` |
| boss | `web-01-html/boss` | INC-01: A blind customer can't place an order | code | `web-01-html/topic-10-head-and-meta` |
| mini-project | `web-01-html/mini-project` | FEAT-01: The custom-order request form | code | `web-01-html/topic-10-head-and-meta` |

## web-02-css: CSS (project: Making it ours)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `web-02-css/topic-01-selectors-and-rules` | CSS rules and selectors | code | `web-01-html/boss` |
| 2 | `web-02-css/topic-02-colours-and-units` | Colours and units | code | `web-02-css/topic-01-selectors-and-rules` |
| 3 | `web-02-css/topic-03-box-model` | The box model: margin, border, padding | code | `web-02-css/topic-02-colours-and-units` |
| 4 | `web-02-css/topic-04-typography` | Fonts and text | code | `web-02-css/topic-03-box-model` |
| 5 | `web-02-css/topic-05-cascade-and-specificity` | The cascade and specificity: which rule wins | code | `web-02-css/topic-04-typography` |
| 6 | `web-02-css/topic-06-display-and-position` | Display and position | code | `web-02-css/topic-05-cascade-and-specificity` |
| 7 | `web-02-css/topic-07-flexbox` | Flexbox: rows and columns | code | `web-02-css/topic-06-display-and-position` |
| 8 | `web-02-css/topic-08-grid` | Grid: the product grid | code | `web-02-css/topic-07-flexbox` |
| 9 | `web-02-css/topic-09-responsive-design` | Responsive design: phone first | code | `web-02-css/topic-08-grid` |
| 10 | `web-02-css/topic-10-variables-and-states` | Custom properties, hover and focus | code | `web-02-css/topic-09-responsive-design` |
| boss | `web-02-css/boss` | INC-02: The catalogue breaks on small Android phones | code | `web-02-css/topic-10-variables-and-states` |
| mini-project | `web-02-css/mini-project` | FEAT-02: A festive Diwali theme | code | `web-02-css/topic-10-variables-and-states` |

## web-03-javascript-basics: JavaScript basics (project: The price calculator)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `web-03-javascript-basics/topic-01-variables` | Variables: let and const | code | `web-02-css/boss` |
| 2 | `web-03-javascript-basics/topic-02-numbers-and-paise` | Numbers, maths and why prices are in paise | code | `web-03-javascript-basics/topic-01-variables` |
| 3 | `web-03-javascript-basics/topic-03-strings` | Strings and template literals | code | `web-03-javascript-basics/topic-02-numbers-and-paise` |
| 4 | `web-03-javascript-basics/topic-04-booleans-and-comparison` | Booleans and comparison: === and its friends | code | `web-03-javascript-basics/topic-03-strings` |
| 5 | `web-03-javascript-basics/topic-05-if-else` | Decisions: if, else and logical operators | code | `web-03-javascript-basics/topic-04-booleans-and-comparison` |
| 6 | `web-03-javascript-basics/topic-06-switch-and-ternary` | switch and the ternary operator | code | `web-03-javascript-basics/topic-05-if-else` |
| 7 | `web-03-javascript-basics/topic-07-loops` | Loops: for, while and for...of | code | `web-03-javascript-basics/topic-06-switch-and-ternary` |
| 8 | `web-03-javascript-basics/topic-08-functions` | Functions: parameters and return values | code | `web-03-javascript-basics/topic-07-loops` |
| 9 | `web-03-javascript-basics/topic-09-arrays` | Arrays | code | `web-03-javascript-basics/topic-08-functions` |
| 10 | `web-03-javascript-basics/topic-10-objects` | Objects | code | `web-03-javascript-basics/topic-09-arrays` |
| 11 | `web-03-javascript-basics/topic-11-formatting-money` | Showing rupees: toFixed and Intl.NumberFormat | code | `web-03-javascript-basics/topic-10-objects` |
| boss | `web-03-javascript-basics/boss` | INC-03: The cart total is off by a few paise | code | `web-03-javascript-basics/topic-11-formatting-money` |
| mini-project | `web-03-javascript-basics/mini-project` | FEAT-03: The price and delivery calculator | code | `web-03-javascript-basics/topic-11-formatting-money` |

## web-04-javascript-in-depth: JavaScript in depth (project: The catalogue engine)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `web-04-javascript-in-depth/topic-01-scope-and-closures` | Scope and closures | code | `web-03-javascript-basics/boss` |
| 2 | `web-04-javascript-in-depth/topic-02-functions-as-values` | Functions as values: callbacks and arrow functions | code | `web-04-javascript-in-depth/topic-01-scope-and-closures` |
| 3 | `web-04-javascript-in-depth/topic-03-array-methods` | Array methods: map, filter, reduce, find and sort | code | `web-04-javascript-in-depth/topic-02-functions-as-values` |
| 4 | `web-04-javascript-in-depth/topic-04-destructuring-and-spread` | Destructuring, spread and rest | code | `web-04-javascript-in-depth/topic-03-array-methods` |
| 5 | `web-04-javascript-in-depth/topic-05-objects-in-depth` | Objects in depth: methods, this and copying | code | `web-04-javascript-in-depth/topic-04-destructuring-and-spread` |
| 6 | `web-04-javascript-in-depth/topic-06-classes` | Classes: a Cart with methods | code | `web-04-javascript-in-depth/topic-05-objects-in-depth` |
| 7 | `web-04-javascript-in-depth/topic-07-modules` | Modules: import and export | code | `web-04-javascript-in-depth/topic-06-classes` |
| 8 | `web-04-javascript-in-depth/topic-08-errors` | Errors: throw, try and catch | code | `web-04-javascript-in-depth/topic-07-modules` |
| 9 | `web-04-javascript-in-depth/topic-09-json` | JSON: the catalogue as data | code | `web-04-javascript-in-depth/topic-08-errors` |
| 10 | `web-04-javascript-in-depth/topic-10-dates` | Dates and times: opening hours | code | `web-04-javascript-in-depth/topic-09-json` |
| 11 | `web-04-javascript-in-depth/topic-11-maps-and-sets` | Map and Set | code | `web-04-javascript-in-depth/topic-10-dates` |
| boss | `web-04-javascript-in-depth/boss` | INC-04: The Diwali discount is applied twice | code | `web-04-javascript-in-depth/topic-11-maps-and-sets` |
| mini-project | `web-04-javascript-in-depth/mini-project` | FEAT-04: The catalogue filter engine | code | `web-04-javascript-in-depth/topic-11-maps-and-sets` |

## web-05-dom-and-events: The DOM and events (project: A working cart)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `web-05-dom-and-events/topic-01-the-dom` | The DOM: the page as a tree of objects | code | `web-04-javascript-in-depth/boss` |
| 2 | `web-05-dom-and-events/topic-02-selecting-elements` | Finding elements: querySelector | code | `web-05-dom-and-events/topic-01-the-dom` |
| 3 | `web-05-dom-and-events/topic-03-changing-the-page` | Changing text, attributes and classes | code | `web-05-dom-and-events/topic-02-selecting-elements` |
| 4 | `web-05-dom-and-events/topic-04-creating-elements` | Creating and removing elements | code | `web-05-dom-and-events/topic-03-changing-the-page` |
| 5 | `web-05-dom-and-events/topic-05-events` | Events: click and friends | code | `web-05-dom-and-events/topic-04-creating-elements` |
| 6 | `web-05-dom-and-events/topic-06-event-delegation` | Event delegation: one listener for many buttons | code | `web-05-dom-and-events/topic-05-events` |
| 7 | `web-05-dom-and-events/topic-07-forms-and-input` | Forms and input events | code | `web-05-dom-and-events/topic-06-event-delegation` |
| 8 | `web-05-dom-and-events/topic-08-rendering-from-data` | Drawing the page from data | code | `web-05-dom-and-events/topic-07-forms-and-input` |
| 9 | `web-05-dom-and-events/topic-09-local-storage` | Remembering things: localStorage | code | `web-05-dom-and-events/topic-08-rendering-from-data` |
| 10 | `web-05-dom-and-events/topic-10-state-and-render` | State and render: keeping the page in sync | code | `web-05-dom-and-events/topic-09-local-storage` |
| boss | `web-05-dom-and-events/boss` | INC-05: The cart empties on refresh | code | `web-05-dom-and-events/topic-10-state-and-render` |
| mini-project | `web-05-dom-and-events/mini-project` | FEAT-05: The cart drawer | code | `web-05-dom-and-events/topic-10-state-and-render` |

## web-06-async-javascript: Async JavaScript (project: Live stock)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `web-06-async-javascript/topic-01-sync-and-async` | Waiting without freezing: the event loop | quiz | `web-05-dom-and-events/boss` |
| 2 | `web-06-async-javascript/topic-02-timers-and-callbacks` | Timers and callbacks | code | `web-06-async-javascript/topic-01-sync-and-async` |
| 3 | `web-06-async-javascript/topic-03-promises` | Promises | code | `web-06-async-javascript/topic-02-timers-and-callbacks` |
| 4 | `web-06-async-javascript/topic-04-async-await` | async and await | code | `web-06-async-javascript/topic-03-promises` |
| 5 | `web-06-async-javascript/topic-05-http-and-fetch` | HTTP and fetch | code | `web-06-async-javascript/topic-04-async-await` |
| 6 | `web-06-async-javascript/topic-06-json-apis` | Talking to a JSON API | code | `web-06-async-javascript/topic-05-http-and-fetch` |
| 7 | `web-06-async-javascript/topic-07-loading-and-errors` | Loading and error states | code | `web-06-async-javascript/topic-06-json-apis` |
| 8 | `web-06-async-javascript/topic-08-parallel-requests` | Many requests at once: Promise.all | code | `web-06-async-javascript/topic-07-loading-and-errors` |
| 9 | `web-06-async-javascript/topic-09-races-and-abort` | Race conditions and AbortController | code | `web-06-async-javascript/topic-08-parallel-requests` |
| 10 | `web-06-async-javascript/topic-10-debounce` | Debounce: searching as you type | code | `web-06-async-javascript/topic-09-races-and-abort` |
| boss | `web-06-async-javascript/boss` | INC-06: Slow 3G shows the wrong stock | code | `web-06-async-javascript/topic-10-debounce` |
| mini-project | `web-06-async-javascript/mini-project` | FEAT-06: Live stock badges | code | `web-06-async-javascript/topic-10-debounce` |

## web-07-tooling: Tooling (project: A real project)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `web-07-tooling/topic-01-npm-and-packages` | npm and packages | quiz | `web-06-async-javascript/boss` |
| 2 | `web-07-tooling/topic-02-vite` | Vite: a dev server and a build | quiz | `web-07-tooling/topic-01-npm-and-packages` |
| 3 | `web-07-tooling/topic-03-eslint-and-prettier` | ESLint and Prettier | quiz | `web-07-tooling/topic-02-vite` |
| 4 | `web-07-tooling/topic-04-git-branches` | Git branches and merging | quiz | `web-07-tooling/topic-03-eslint-and-prettier` |
| 5 | `web-07-tooling/topic-05-github-pull-requests` | GitHub and pull requests | quiz | `web-07-tooling/topic-04-git-branches` |
| 6 | `web-07-tooling/topic-06-debugging` | Debugging with breakpoints | quiz | `web-07-tooling/topic-05-github-pull-requests` |
| 7 | `web-07-tooling/topic-07-unit-tests` | Writing tests with Vitest | code | `web-07-tooling/topic-06-debugging` |
| 8 | `web-07-tooling/topic-08-testing-the-dom` | Testing the page | code | `web-07-tooling/topic-07-unit-tests` |
| 9 | `web-07-tooling/topic-09-environment-config` | Configuration and environment variables | code | `web-07-tooling/topic-08-testing-the-dom` |
| 10 | `web-07-tooling/topic-10-production-builds` | Production builds and caching | quiz | `web-07-tooling/topic-09-environment-config` |
| boss | `web-07-tooling/boss` | INC-07: The build breaks only on deploy | code | `web-07-tooling/topic-10-production-builds` |
| mini-project | `web-07-tooling/mini-project` | FEAT-07: A tested utilities package | code | `web-07-tooling/topic-10-production-builds` |

## react-08-react-fundamentals: React fundamentals (project: The store in React)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `react-08-react-fundamentals/topic-01-why-react` | Why React: components and the virtual DOM | quiz | `web-07-tooling/boss` |
| 2 | `react-08-react-fundamentals/topic-02-components-and-jsx` | Components and JSX | code | `react-08-react-fundamentals/topic-01-why-react` |
| 3 | `react-08-react-fundamentals/topic-03-props` | Props: passing data down | code | `react-08-react-fundamentals/topic-02-components-and-jsx` |
| 4 | `react-08-react-fundamentals/topic-04-state` | State with useState | code | `react-08-react-fundamentals/topic-03-props` |
| 5 | `react-08-react-fundamentals/topic-05-events` | Handling events | code | `react-08-react-fundamentals/topic-04-state` |
| 6 | `react-08-react-fundamentals/topic-06-conditional-rendering` | Showing things conditionally | code | `react-08-react-fundamentals/topic-05-events` |
| 7 | `react-08-react-fundamentals/topic-07-lists-and-keys` | Lists and keys | code | `react-08-react-fundamentals/topic-06-conditional-rendering` |
| 8 | `react-08-react-fundamentals/topic-08-forms` | Forms in React | code | `react-08-react-fundamentals/topic-07-lists-and-keys` |
| 9 | `react-08-react-fundamentals/topic-09-lifting-state` | Lifting state up | code | `react-08-react-fundamentals/topic-08-forms` |
| 10 | `react-08-react-fundamentals/topic-10-styling` | Styling components | code | `react-08-react-fundamentals/topic-09-lifting-state` |
| boss | `react-08-react-fundamentals/boss` | INC-08: Removing one item removes another | code | `react-08-react-fundamentals/topic-10-styling` |
| mini-project | `react-08-react-fundamentals/mini-project` | FEAT-08: The React catalogue | code | `react-08-react-fundamentals/topic-10-styling` |

## react-09-react-in-depth: React in depth (project: Checkout)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `react-09-react-in-depth/topic-01-effects` | Effects with useEffect | code | `react-08-react-fundamentals/boss` |
| 2 | `react-09-react-in-depth/topic-02-fetching-data` | Fetching data in a component | code | `react-09-react-in-depth/topic-01-effects` |
| 3 | `react-09-react-in-depth/topic-03-custom-hooks` | Custom hooks: useCart | code | `react-09-react-in-depth/topic-02-fetching-data` |
| 4 | `react-09-react-in-depth/topic-04-context` | Context: sharing state without passing props | code | `react-09-react-in-depth/topic-03-custom-hooks` |
| 5 | `react-09-react-in-depth/topic-05-reducers` | useReducer for the cart | code | `react-09-react-in-depth/topic-04-context` |
| 6 | `react-09-react-in-depth/topic-06-routing` | Pages and routing: one app, many pages | code | `react-09-react-in-depth/topic-05-reducers` |
| 7 | `react-09-react-in-depth/topic-07-form-validation` | Forms with validation | code | `react-09-react-in-depth/topic-06-routing` |
| 8 | `react-09-react-in-depth/topic-08-refs` | Refs: focus and measuring | code | `react-09-react-in-depth/topic-07-form-validation` |
| 9 | `react-09-react-in-depth/topic-09-performance` | Performance: memo, useMemo and useCallback | code | `react-09-react-in-depth/topic-08-refs` |
| 10 | `react-09-react-in-depth/topic-10-error-boundaries` | When things break: error boundaries | code | `react-09-react-in-depth/topic-09-performance` |
| boss | `react-09-react-in-depth/boss` | INC-09: A render loop floods the API during the sale | code | `react-09-react-in-depth/topic-10-error-boundaries` |
| mini-project | `react-09-react-in-depth/mini-project` | FEAT-09: Checkout with a UPI step | code | `react-09-react-in-depth/topic-10-error-boundaries` |

## react-10-professional-frontend: Professional frontend (project: Launch day)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `react-10-professional-frontend/topic-01-testing-components` | Testing components like a user | code | `react-09-react-in-depth/boss` |
| 2 | `react-10-professional-frontend/topic-02-accessibility-in-depth` | Accessibility in depth: keyboard, focus and ARIA | code | `react-10-professional-frontend/topic-01-testing-components` |
| 3 | `react-10-professional-frontend/topic-03-performance` | Fast pages: images, lazy loading and Web Vitals | code | `react-10-professional-frontend/topic-02-accessibility-in-depth` |
| 4 | `react-10-professional-frontend/topic-04-security` | Security: XSS and safe rendering | code | `react-10-professional-frontend/topic-03-performance` |
| 5 | `react-10-professional-frontend/topic-05-seo-and-sharing` | Search engines and sharing links | quiz | `react-10-professional-frontend/topic-04-security` |
| 6 | `react-10-professional-frontend/topic-06-deployment` | Deploying a frontend | quiz | `react-10-professional-frontend/topic-05-seo-and-sharing` |
| 7 | `react-10-professional-frontend/topic-07-code-reviews` | Code reviews for frontend work | quiz | `react-10-professional-frontend/topic-06-deployment` |
| 8 | `react-10-professional-frontend/topic-08-big-codebases` | Finding your way in a big frontend codebase | quiz | `react-10-professional-frontend/topic-07-code-reviews` |
| 9 | `react-10-professional-frontend/topic-09-interviews` | Frontend interviews | quiz | `react-10-professional-frontend/topic-08-big-codebases` |
| boss | `react-10-professional-frontend/boss` | INC-10: The mock frontend interview loop | code | `react-10-professional-frontend/topic-09-interviews` |
| mini-project | `react-10-professional-frontend/mini-project` | CAPSTONE: Launch the dhaaga.in online store | code | `react-10-professional-frontend/topic-09-interviews` |

## Web totals

- Modules: 11
- Lessons: 108
- Graded units including incidents and features: 130

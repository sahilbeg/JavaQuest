# Curriculum

Generated from one topic list together with `build_state.json`. Topic IDs are stable: `<module>/topic-NN-<slug>`.

## Rules

- Each module unlocks when the previous module's boss is beaten. Module 0 is open from the start.
- Inside a module, each topic needs the topic before it. Extra cross-module prerequisites are listed per topic.
- Every topic follows: Hook -> Concept -> Worked example -> Try it -> Quiz -> Coding challenge -> Recap and common mistakes.
- Practice `code` means a JUnit exercise in `practice/`. Practice `quiz` means quiz plus a guided activity (tool topics, theory topics). Mastery for `quiz` topics is the quiz alone.
- Every module ends with a boss challenge, shown as an incident (`INC-MM`), and a mini-project, shown as a feature (`FEAT-MM`). The story for each is in `docs/STORY.md`.
- Every topic is shown as a ticket keyed `SPK-MMTT`, for example `SPK-0105`.

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
| 1 | `module-08-backend/topic-01-http` | How HTTP works | quiz | `module-07-tooling/boss` |
| 2 | `module-08-backend/topic-02-rest-design` | REST and API design | quiz | `module-08-backend/topic-01-http` |
| 3 | `module-08-backend/topic-03-spring-boot-intro` | Spring Boot: your first app | code | `module-08-backend/topic-02-rest-design` |
| 4 | `module-08-backend/topic-04-dependency-injection` | Dependency injection and beans | code | `module-08-backend/topic-03-spring-boot-intro`, `module-06-craft/topic-02-solid` |
| 5 | `module-08-backend/topic-05-spring-mvc` | Controllers and request mapping | code | `module-08-backend/topic-04-dependency-injection` |
| 6 | `module-08-backend/topic-06-validation` | Validation and error responses | code | `module-08-backend/topic-05-spring-mvc` |
| 7 | `module-08-backend/topic-07-sql-basics` | SQL basics | code | `module-08-backend/topic-06-validation` |
| 8 | `module-08-backend/topic-08-jdbc` | JDBC | code | `module-08-backend/topic-07-sql-basics` |
| 9 | `module-08-backend/topic-09-jpa-hibernate` | JPA and Hibernate | code | `module-08-backend/topic-08-jdbc`, `module-08-backend/topic-07-sql-basics` |
| 10 | `module-08-backend/topic-10-transactions` | Transactions | code | `module-08-backend/topic-09-jpa-hibernate` |
| 11 | `module-08-backend/topic-11-spring-security` | Security basics with Spring Security | code | `module-08-backend/topic-10-transactions` |
| 12 | `module-08-backend/topic-12-api-docs-errors` | API docs, versioning and error design | code | `module-08-backend/topic-11-spring-security` |
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

## module-10-professional: Professional skills (project: Full-time offer)

| # | Topic ID | Title | Practice | Prerequisites |
|---|---|---|---|---|
| 1 | `module-10-professional/topic-01-code-reviews` | Giving and receiving code reviews | quiz | `module-09-architecture/boss` |
| 2 | `module-10-professional/topic-02-reading-codebases` | Reading large codebases | quiz | `module-10-professional/topic-01-code-reviews` |
| 3 | `module-10-professional/topic-03-documentation` | Writing documentation | quiz | `module-10-professional/topic-02-reading-codebases` |
| 4 | `module-10-professional/topic-04-estimation` | Estimation and planning | quiz | `module-10-professional/topic-03-documentation` |
| 5 | `module-10-professional/topic-05-interview-prep` | Interview prep: coding, design and behavioural | code | `module-10-professional/topic-04-estimation` |
| 6 | `module-10-professional/topic-06-capstone` | Capstone: full-stack Java service | code | `module-10-professional/topic-05-interview-prep` |
| boss | `module-10-professional/boss` | INC-10: The mock interview loop | code | `module-10-professional/topic-06-capstone` |
| mini-project | `module-10-professional/mini-project` | CAPSTONE: Capstone: launch Spoke in Bramford | code | `module-10-professional/topic-06-capstone` |

## Totals

- Modules: 11
- Lessons: 114
- Graded units including bosses and mini-projects: 136

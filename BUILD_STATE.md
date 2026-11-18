# Build state

Human-readable mirror of `build_state.json`. The JSON file wins if they disagree.

- **Last updated:** 2026-11-18T16:48:03+05:30
- **Phase:** PHASE 1: build
- **Milestone:** M6
- **Resume here:** M5 is committed: the capstone has a brief, six staged check classes and a self-review rubric; the app has the /offer completion screen, a launch dialog and the Full-time offer kudos. Next: M6 release hardening (fresh-clone test on macOS, README; Windows deferred). Modules 0 to 10 still need your read-through to reach done.
- **Next task:** M6: fresh-clone test on macOS (one command start) and a README with setup, troubleshooting and limits

## Completed tasks

- M0-01 architecture
- M0-02 curriculum
- M0-03 content spec
- M0-04 gamification
- M0-05 roadmap
- M0-06 state files
- M0-07 plan approved
- M1-01 Maven skeleton
- M1-02 run.sh
- M1-03 content catalog and sample topic
- M1-04 practice runner
- M1-05 runner timing measured
- M1-06 file watcher and SSE
- M1-07 practice verifier
- M1-08 M1 tests
- M2-00 Spoke story, ticket format, PRODUCT.md
- M2-A backend: lesson rendering, quiz draw and grading, source read/write, run main and snippets, plain-language compiler help
- M2-B frontend: board, ticket page, KB, onboarding check, Fix editor, Byte chat, design review fixes, DESIGN.md
- M2-C content checker (exec:java@check-content)
- M3-01 H2 + Flyway progress store; progress survives restarts
- M3-02 mastery, hint gating, solution unlock, reset to skeleton
- M3-03 native file watcher: save to checks in under 1 s
- M2 follow-ups: fill-blank prompt, spacing check on real pages, Impeccable v4.8.2
- M3-04 XP ledger and job-title levels
- M3-05 streaks with freezes
- M3-06 kudos
- M3-07 spaced repetition and weak list
- M3-08 career dashboard, review page, promotion and toasts
- M3-09 export and import progress
- M3-10 incident tiers, feature XP, Boss/Gold/Java Pro kudos, quiz-less units, catalog ordered by meta order
- M4-00 tests run on a temporary repo copy; catalog keeps topic order (was hash order)
- M4-01 keyboard and screen reader fixes
- M4-02 contrast fixes, Lighthouse 100 in both themes
- M4-03 error and empty states, connection banner
- M4-04 server shutdown with open event streams
- C00 module 0: 7 new units written, reviewed (22 findings fixed), content checks, quiz keys and practice verified
- C01 module 1: 12 topics, INC-01, FEAT-01; 2 reviews (34 findings) fixed; all checks green
- C02 module 2 OOP: 12 topics, INC-02, FEAT-02; 2 reviews (33 findings) fixed; all checks green
- C03: module 3 Core Java (16 topics), INC-03 and FEAT-03; java run crash block type; reviewed and fixed
- C04: module 4 DSA (12 topics), INC-04 and FEAT-04; built-by-hand and timeout guards in tests; Depot added to STORY.md
- C05: module 5 Advanced Java (13 topics, 3 quiz-only), INC-05 and FEAT-05; runner honours practice.timeoutSeconds
- C06: module 6 Engineering craft (12 topics, 1 quiz-only), INC-06 and FEAT-06; test-writing practice checked with mutants
- State fix: C03 to C06 milestones and their topics set to ui_wired (content checks pass); 4 quiz-only topics moved from planned to ui_wired
- C07: module 7 Build and tooling (5 quiz-only topics), INC-07 and FEAT-07; reviewed (13 findings fixed); jar build checked as reproducible across time zones; all checks green
- C08: module 8 Backend engineering (2 quiz topics, 10 Spring/SQL/JDBC code topics), INC-08 and FEAT-08; 3 reviews (about 45 findings) fixed; runner compiles with -parameters; all checks green
- UX overhaul (user feedback): playful redesign, journey map, mastery gating with author mode, lesson player with short cards, animation engine (code and scene, checked by check-content), first-visit walkthroughs; 56 animations added to modules 0 to 2 and reviewed
- SDE2 audit (docs/SDE2_AUDIT.md): modules 0 to 8 deepened to SDE 2 level; 17 new tickets (M04 13-20, M05 14-15, M06 13, M07 06-08, M08 13-15); module 9 gains resilience and Kubernetes topics; every module independently reviewed; all checks green
- C09: module 9 Architecture (4 quiz topics, 6 code topics: layered (Spring), hexagonal, messaging + transactional outbox (H2), caching, performance, resilience), INC-09 (live map cache: key, stampede, stale-if-error) and FEAT-09 (event-driven billing on a partitioned log); 3 independent reviews (about 40 findings) fixed; all checks green
- C10: module 10 Professional skills (4 quiz topics: code reviews, reading codebases, documentation, estimation; interview prep and the city-aware capstone core as code), INC-10 (mock interview loop: edge cases, O(n) follow-up, concurrent-safe extensible design) and CAPSTONE (Spring Boot Bramford launch in six staged test classes); Java Pro kudos moved to the capstone; 2 independent reviews (about 30 findings) fixed; all checks green
- M5: capstone rubric (lesson card and README), /offer completion screen (Tsunade's offer letter, printable), launch dialog on closing the capstone, Full-time offer kudos, kudos earnedOn dates; kudos toasts name the teammate; tested in a throwaway author app

## Decisions

- Helper: Satoru Gojo (was Byte the owl); team: Tsunade, Kakashi, Jiraiya, Itachi (see docs/STORY.md)
- Skip ahead: removed (user feedback, 2026-11-16). Tickets open one at a time when the one before is mastered; ./run.sh --unlock-all is author mode
- Platform: macOS only for now; Windows deferred
- Page style: textbook order with W3Schools-style pages (Syntax box, Try it Yourself, prev/next, sidebar)
- Spring Boot 4.1.2, JUnit 6.1.3 (Jupiter API)
- Content formats are Java records that check themselves; no JSON Schema files
- JDK WatchService kept: measured under 2 s on macOS
- Verify practice with: ./mvnw -q -pl tools dependency:properties exec:java
- Story: you are an intern at Spoke, a bike-sharing startup (docs/STORY.md)
- UI looks like Spoke's internal workplace tools; topics are tickets, bosses are incidents
- Content check with: ./mvnw -q -pl tools dependency:properties exec:java@check-content
- Each practice topic has a committed skeleton/ copy; verification and Reset use it
- File watcher: directory-watcher (native FSEvents), measured 0.36 to 0.39 s save to start
- Incident tiers come from test class names ending BronzeTest, SilverTest, GoldTest
- Animations: every topic has at least one ```animate block; modules 0 to 2 done in the UX round, modules 3 to 8 to follow, new modules include them from C09
- Tickets added after a module was built get the next free SPK key; the order a ticket appears in comes from meta.yaml order, not the key or folder number

## Open decisions

- A real VoiceOver pass needs a person
- Modules 0 to 10 read-through by you before marking them done

## Blockers

- None

## Milestones

| Milestone | Status |
|---|---|
| C00 | ui_wired |
| C01 | ui_wired |
| C02 | ui_wired |
| C03 | ui_wired |
| C04 | ui_wired |
| C05 | ui_wired |
| C06 | ui_wired |
| C07 | ui_wired |
| C08 | ui_wired |
| C09 | ui_wired |
| C10 | ui_wired |
| M0 | done |
| M1 | done |
| M2 | done |
| M3 | done |
| M4 | done |
| M5 | done |
| M6 | in_progress |

## Modules

| Module | Project | Units | Status |
|---|---|---|---|
| module-00-foundations | Day one | 8 | ui_wired |
| module-01-basics | The fare kiosk | 14 | ui_wired |
| module-02-oop | Modelling the fleet | 14 | ui_wired |
| module-03-core-java | Ride data | 18 | ui_wired |
| module-04-dsa | Moving bikes around | 22 | ui_wired |
| module-05-advanced-java | Rush hour | 17 | ui_wired |
| module-06-craft | Paying off debt | 15 | ui_wired |
| module-07-tooling | Shipping safely | 10 | ui_wired |
| module-08-backend | The Spoke API | 17 | ui_wired |
| module-09-architecture | Growing up | 12 | ui_wired |
| module-10-professional | Full-time offer | 8 | ui_wired |

## Topics past `planned`

| Topic | Status | Solution passes | Skeleton fails |
|---|---|---|---|
| module-00-foundations/topic-01-how-computers-work | ui_wired | None | None |
| module-00-foundations/topic-02-what-is-a-program | ui_wired | None | None |
| module-00-foundations/topic-03-terminal-basics | ui_wired | None | None |
| module-00-foundations/topic-04-setup-jdk-ide | ui_wired | None | None |
| module-00-foundations/topic-05-hello-world | ui_wired | True | True |
| module-00-foundations/topic-06-git-basics | ui_wired | None | None |
| module-00-foundations/boss | ui_wired | True | True |
| module-00-foundations/mini-project | ui_wired | True | True |
| module-01-basics/topic-01-variables | ui_wired | True | True |
| module-01-basics/topic-02-primitive-types | ui_wired | True | True |
| module-01-basics/topic-03-operators | ui_wired | True | True |
| module-01-basics/topic-04-console-io | ui_wired | True | True |
| module-01-basics/topic-05-if-else | ui_wired | True | True |
| module-01-basics/topic-06-switch | ui_wired | True | True |
| module-01-basics/topic-07-while-loops | ui_wired | True | True |
| module-01-basics/topic-08-for-loops | ui_wired | True | True |
| module-01-basics/topic-09-methods | ui_wired | True | True |
| module-01-basics/topic-10-arrays | ui_wired | True | True |
| module-01-basics/topic-11-strings | ui_wired | True | True |
| module-01-basics/topic-12-stringbuilder-formatting | ui_wired | True | True |
| module-01-basics/boss | ui_wired | True | True |
| module-01-basics/mini-project | ui_wired | True | True |
| module-02-oop/topic-01-classes-objects | ui_wired | True | True |
| module-02-oop/topic-02-constructors | ui_wired | True | True |
| module-02-oop/topic-03-encapsulation | ui_wired | True | True |
| module-02-oop/topic-04-static-members | ui_wired | True | True |
| module-02-oop/topic-05-inheritance | ui_wired | True | True |
| module-02-oop/topic-06-polymorphism | ui_wired | True | True |
| module-02-oop/topic-07-abstract-classes | ui_wired | True | True |
| module-02-oop/topic-08-interfaces | ui_wired | True | True |
| module-02-oop/topic-09-composition | ui_wired | True | True |
| module-02-oop/topic-10-equals-hashcode-tostring | ui_wired | True | True |
| module-02-oop/topic-11-records | ui_wired | True | True |
| module-02-oop/topic-12-enums | ui_wired | True | True |
| module-02-oop/boss | ui_wired | True | True |
| module-02-oop/mini-project | ui_wired | True | True |
| module-03-core-java/topic-01-collections-overview | ui_wired | True | True |
| module-03-core-java/topic-02-lists | ui_wired | True | True |
| module-03-core-java/topic-03-sets | ui_wired | True | True |
| module-03-core-java/topic-04-maps | ui_wired | True | True |
| module-03-core-java/topic-05-generics | ui_wired | True | True |
| module-03-core-java/topic-06-comparators-iterators | ui_wired | True | True |
| module-03-core-java/topic-07-exceptions | ui_wired | True | True |
| module-03-core-java/topic-08-custom-exceptions | ui_wired | True | True |
| module-03-core-java/topic-09-file-io | ui_wired | True | True |
| module-03-core-java/topic-10-nio-files | ui_wired | True | True |
| module-03-core-java/topic-11-date-time | ui_wired | True | True |
| module-03-core-java/topic-12-lambdas | ui_wired | True | True |
| module-03-core-java/topic-13-functional-interfaces | ui_wired | True | True |
| module-03-core-java/topic-14-streams-basics | ui_wired | True | True |
| module-03-core-java/topic-15-streams-advanced | ui_wired | True | True |
| module-03-core-java/topic-16-optional | ui_wired | True | True |
| module-03-core-java/boss | ui_wired | True | True |
| module-03-core-java/mini-project | ui_wired | True | True |
| module-04-dsa/topic-01-big-o | ui_wired | True | True |
| module-04-dsa/topic-02-recursion | ui_wired | True | True |
| module-04-dsa/topic-03-searching | ui_wired | True | True |
| module-04-dsa/topic-04-simple-sorts | ui_wired | True | True |
| module-04-dsa/topic-05-merge-quick-sort | ui_wired | True | True |
| module-04-dsa/topic-06-linked-lists | ui_wired | True | True |
| module-04-dsa/topic-07-stacks-queues | ui_wired | True | True |
| module-04-dsa/topic-08-hash-tables | ui_wired | True | True |
| module-04-dsa/topic-09-trees-bst | ui_wired | True | True |
| module-04-dsa/topic-10-heaps | ui_wired | True | True |
| module-04-dsa/topic-11-graphs | ui_wired | True | True |
| module-04-dsa/topic-12-dynamic-programming | ui_wired | True | True |
| module-04-dsa/topic-13-two-pointers | ui_wired | True | True |
| module-04-dsa/topic-14-backtracking | ui_wired | True | True |
| module-04-dsa/topic-15-topological-sort | ui_wired | True | True |
| module-04-dsa/topic-16-dijkstra | ui_wired | True | True |
| module-04-dsa/topic-17-dp-patterns | ui_wired | True | True |
| module-04-dsa/topic-18-greedy-intervals | ui_wired | True | True |
| module-04-dsa/topic-19-tries-union-find | ui_wired | True | True |
| module-04-dsa/topic-20-bit-manipulation | ui_wired | True | True |
| module-04-dsa/boss | ui_wired | True | True |
| module-04-dsa/mini-project | ui_wired | True | True |
| module-05-advanced-java/topic-01-threads | ui_wired | True | True |
| module-05-advanced-java/topic-02-synchronization | ui_wired | True | True |
| module-05-advanced-java/topic-03-executors | ui_wired | True | True |
| module-05-advanced-java/topic-04-completable-future | ui_wired | True | True |
| module-05-advanced-java/topic-05-virtual-threads | ui_wired | True | True |
| module-05-advanced-java/topic-06-concurrent-collections | ui_wired | True | True |
| module-05-advanced-java/topic-07-jvm-memory | ui_wired | None | None |
| module-05-advanced-java/topic-08-garbage-collection | ui_wired | None | None |
| module-05-advanced-java/topic-09-classloading | ui_wired | None | None |
| module-05-advanced-java/topic-10-reflection | ui_wired | True | True |
| module-05-advanced-java/topic-11-annotations | ui_wired | True | True |
| module-05-advanced-java/topic-12-sealed-types | ui_wired | True | True |
| module-05-advanced-java/topic-13-pattern-matching | ui_wired | True | True |
| module-05-advanced-java/topic-14-memory-model | ui_wired | True | True |
| module-05-advanced-java/topic-15-coordinating-threads | ui_wired | True | True |
| module-05-advanced-java/boss | ui_wired | True | True |
| module-05-advanced-java/mini-project | ui_wired | True | True |
| module-06-craft/topic-01-clean-code | ui_wired | True | True |
| module-06-craft/topic-02-solid | ui_wired | True | True |
| module-06-craft/topic-03-creational-patterns | ui_wired | True | True |
| module-06-craft/topic-04-structural-patterns | ui_wired | True | True |
| module-06-craft/topic-05-behavioral-patterns | ui_wired | True | True |
| module-06-craft/topic-06-refactoring | ui_wired | True | True |
| module-06-craft/topic-07-uml-basics | ui_wired | None | None |
| module-06-craft/topic-08-junit | ui_wired | True | True |
| module-06-craft/topic-09-mockito | ui_wired | True | True |
| module-06-craft/topic-10-tdd | ui_wired | True | True |
| module-06-craft/topic-11-debugging | ui_wired | True | True |
| module-06-craft/topic-12-logging | ui_wired | True | True |
| module-06-craft/topic-13-object-oriented-design | ui_wired | True | True |
| module-06-craft/boss | ui_wired | True | True |
| module-06-craft/mini-project | ui_wired | True | True |
| module-07-tooling/topic-01-maven | ui_wired | None | None |
| module-07-tooling/topic-02-gradle | ui_wired | None | None |
| module-07-tooling/topic-03-git-workflow | ui_wired | None | None |
| module-07-tooling/topic-04-ci-basics | ui_wired | None | None |
| module-07-tooling/topic-05-docker-basics | ui_wired | None | None |
| module-07-tooling/topic-06-git-undo | ui_wired | None | None |
| module-07-tooling/topic-07-shipping-to-production | ui_wired | None | None |
| module-07-tooling/topic-08-shell-on-call | ui_wired | None | None |
| module-07-tooling/boss | ui_wired | True | True |
| module-07-tooling/mini-project | ui_wired | True | True |
| module-08-backend/topic-13-networking | ui_wired | None | None |
| module-08-backend/topic-01-http | ui_wired | None | None |
| module-08-backend/topic-02-rest-design | ui_wired | None | None |
| module-08-backend/topic-03-spring-boot-intro | ui_wired | True | True |
| module-08-backend/topic-04-dependency-injection | ui_wired | True | True |
| module-08-backend/topic-05-spring-mvc | ui_wired | True | True |
| module-08-backend/topic-06-validation | ui_wired | True | True |
| module-08-backend/topic-07-sql-basics | ui_wired | True | True |
| module-08-backend/topic-14-database-design | ui_wired | True | True |
| module-08-backend/topic-08-jdbc | ui_wired | True | True |
| module-08-backend/topic-09-jpa-hibernate | ui_wired | True | True |
| module-08-backend/topic-10-transactions | ui_wired | True | True |
| module-08-backend/topic-15-concurrent-writes | ui_wired | True | True |
| module-08-backend/topic-11-spring-security | ui_wired | True | True |
| module-08-backend/topic-12-api-docs-errors | ui_wired | True | True |
| module-08-backend/boss | ui_wired | True | True |
| module-08-backend/mini-project | ui_wired | True | True |
| module-09-architecture/topic-01-layered-architecture | ui_wired | True | True |
| module-09-architecture/topic-02-hexagonal-architecture | ui_wired | True | True |
| module-09-architecture/topic-03-microservices | ui_wired | None | None |
| module-09-architecture/topic-04-messaging | ui_wired | True | True |
| module-09-architecture/topic-05-caching | ui_wired | True | True |
| module-09-architecture/topic-06-system-design | ui_wired | None | None |
| module-09-architecture/topic-07-performance-profiling | ui_wired | True | True |
| module-09-architecture/topic-08-observability | ui_wired | None | None |
| module-09-architecture/topic-09-resilience | ui_wired | True | True |
| module-09-architecture/topic-10-kubernetes | ui_wired | None | None |
| module-09-architecture/boss | ui_wired | True | True |
| module-09-architecture/mini-project | ui_wired | True | True |
| module-10-professional/topic-01-code-reviews | ui_wired | None | None |
| module-10-professional/topic-02-reading-codebases | ui_wired | None | None |
| module-10-professional/topic-03-documentation | ui_wired | None | None |
| module-10-professional/topic-04-estimation | ui_wired | None | None |
| module-10-professional/topic-05-interview-prep | ui_wired | True | True |
| module-10-professional/topic-06-capstone | ui_wired | True | True |
| module-10-professional/boss | ui_wired | True | True |
| module-10-professional/mini-project | ui_wired | True | True |

## Commands

- Start the app: `./run.sh`
- Run all tests: `./mvnw -q install`
- Verify practice topics: `./mvnw -q -pl tools dependency:properties exec:java@verify-practice`
- Check content: `./mvnw -q -pl tools dependency:properties exec:java@check-content`

## Session protocol

1. Read this file, `build_state.json` and `docs/ROADMAP.md`.
2. Report where work stopped, then continue from **Next task**.
3. Skip any topic whose files exist and whose status is past `planned`.
4. After each task: update both state files, then commit.

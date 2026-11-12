# Build state

Human-readable mirror of `build_state.json`. The JSON file wins if they disagree.

- **Last updated:** 2026-11-12T17:03:59+05:30
- **Phase:** PHASE 1: build
- **Milestone:** C05
- **Resume here:** C04 is committed at status ui_wired. Next: C05 (Advanced Java, 13 topics + INC-05 + FEAT-05). Modules 0 to 4 still need your read-through to reach done.
- **Next task:** C05-01: module 5 topic 01 threads

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

## Decisions

- Mascot: Byte the owl
- Skip ahead: kept
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

## Blockers

- None

## Milestones

| Milestone | Status |
|---|---|
| C00 | ui_wired |
| C01 | ui_wired |
| C02 | ui_wired |
| C03 | done |
| C04 | done |
| C05 | in_progress |
| C06 | planned |
| C07 | planned |
| C08 | planned |
| C09 | planned |
| C10 | planned |
| M0 | done |
| M1 | done |
| M2 | done |
| M3 | done |
| M4 | done |
| M5 | planned |
| M6 | planned |

## Modules

| Module | Project | Units | Status |
|---|---|---|---|
| module-00-foundations | Day one | 8 | ui_wired |
| module-01-basics | The fare kiosk | 14 | ui_wired |
| module-02-oop | Modelling the fleet | 14 | ui_wired |
| module-03-core-java | Ride data | 18 | ui_wired |
| module-04-dsa | Moving bikes around | 14 | ui_wired |
| module-05-advanced-java | Rush hour | 15 | planned |
| module-06-craft | Paying off debt | 14 | planned |
| module-07-tooling | Shipping safely | 7 | planned |
| module-08-backend | The Spoke API | 14 | planned |
| module-09-architecture | Growing up | 10 | planned |
| module-10-professional | Full-time offer | 8 | planned |

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

# Build state

Human-readable mirror of `build_state.json`. The JSON file wins if they disagree.

- **Last updated:** 2026-11-12T16:26:01+05:30
- **Phase:** PHASE 1: build
- **Milestone:** C00
- **Resume here:** C00 content is committed at status ui_wired. Next: your read-through of module 0 in the browser, then C01 (Java basics, 12 topics + INC-01 + FEAT-01).
- **Next task:** C00-review: learner read-through of module 0; then C01-01 variables

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
| C01 | planned |
| C02 | planned |
| C03 | planned |
| C04 | planned |
| C05 | planned |
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
| module-01-basics | The fare kiosk | 14 | planned |
| module-02-oop | Modelling the fleet | 14 | planned |
| module-03-core-java | Ride data | 18 | planned |
| module-04-dsa | Moving bikes around | 14 | planned |
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

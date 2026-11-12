# Build state

Human-readable mirror of `build_state.json`. The JSON file wins if they disagree.

- **Last updated:** 2026-11-12T14:43:13+05:30
- **Phase:** PHASE 1: build
- **Milestone:** M2
- **Resume here:** Story and ticket format are committed. Next: record the UI direction contract, then build the page shell (M2-01).
- **Next task:** M2-01: page shell in Spoke's workplace-tool look, light and dark themes, responsive layout

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

## Blockers

- None

## Milestones

| Milestone | Status |
|---|---|
| C00 | planned |
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
| M2 | in_progress |
| M3 | planned |
| M4 | planned |
| M5 | planned |
| M6 | planned |

## Modules

| Module | Project | Units | Status |
|---|---|---|---|
| module-00-foundations | Day one | 8 | in_progress |
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
| module-00-foundations/topic-05-hello-world | tests_verified | True | True |

## Commands

- Start the app: `./run.sh`
- Run all tests: `./mvnw -q install`
- Verify practice topics: `./mvnw -q -pl tools dependency:properties exec:java`

## Session protocol

1. Read this file, `build_state.json` and `docs/ROADMAP.md`.
2. Report where work stopped, then continue from **Next task**.
3. Skip any topic whose files exist and whose status is past `planned`.
4. After each task: update both state files, then commit.

# Build state

Human-readable mirror of `build_state.json`. The JSON file wins if they disagree.

- **Last updated:** 2026-11-12T13:26:32+05:30
- **Phase:** PHASE 1: build
- **Milestone:** M2
- **Resume here:** M1 is done and committed. Start M2 task M2-01: page shell with light and dark themes.
- **Next task:** M2-01: page shell, light and dark themes, responsive layout

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

## Decisions

- Mascot: Byte the owl
- Skip ahead: kept
- Platform: macOS only for now; Windows deferred
- Page style: textbook order with W3Schools-style pages (Syntax box, Try it Yourself, prev/next, sidebar)
- Spring Boot 4.1.2, JUnit 6.1.3 (Jupiter API)
- Content formats are Java records that check themselves; no JSON Schema files
- JDK WatchService kept: measured under 2 s on macOS
- Verify practice with: ./mvnw -q -pl tools dependency:properties exec:java

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

| Module | Units | Status |
|---|---|---|
| module-00-foundations | 8 | in_progress |
| module-01-basics | 14 | planned |
| module-02-oop | 14 | planned |
| module-03-core-java | 18 | planned |
| module-04-dsa | 14 | planned |
| module-05-advanced-java | 15 | planned |
| module-06-craft | 14 | planned |
| module-07-tooling | 7 | planned |
| module-08-backend | 14 | planned |
| module-09-architecture | 10 | planned |
| module-10-professional | 8 | planned |

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

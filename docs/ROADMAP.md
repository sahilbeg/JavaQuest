# Roadmap

Each milestone ends with: project checks green, both state files updated, one commit titled `<milestone>: <what>`. Stop after each milestone.

## M0: Planning (done)

- [x] ARCHITECTURE.md, CURRICULUM.md, CONTENT_SPEC.md, GAMIFICATION.md, ROADMAP.md
- [x] BUILD_STATE.md and build_state.json
- [ ] Plan approved by you

## M1: App skeleton and practice runner

- [ ] M1-01 Parent POM, Maven Wrapper, modules `app`, `tools`, and `practice/pom.xml`
- [ ] M1-02 `run.sh` and `run.bat`: build if the JAR is stale, start on port 2026, bound to 127.0.0.1
- [ ] M1-03 Content catalog with JSON Schema validation, plus one sample topic (module 0 hello-world)
- [ ] M1-04 Practice runner: compile, run JUnit in a child JVM, enforce limits, parse XML reports
- [ ] M1-05 Measure child JVM start and test time. Set the timeout from the measurement.
- [ ] M1-06 File watcher and SSE push. Measure macOS latency. Pick the watcher implementation.
- [ ] M1-07 `tools` verifier: solution passes, skeleton fails, writes results into build_state.json
- [ ] M1-08 Tests for the runner and the verifier

## M2: Lesson, editor and quiz UI

- [ ] Page shell, light and dark themes, responsive layout
- [ ] Lesson rendering: Markdown, callouts, Mermaid, runnable snippets
- [ ] Monaco editor with Run, Submit, and IDE-change reload
- [ ] All 5 quiz types, keyboard accessible, with variants and seeds
- [ ] Readable compiler and test feedback
- [ ] Content checker: schema, links, snippet compilation, predict-output answers

## M3: Progress and gamification

- [ ] H2 and Flyway schema
- [ ] Mastery, hints, solution unlock rules
- [ ] XP ledger, levels, streaks, badges
- [ ] Spaced repetition and weak-topic list
- [ ] Dashboard, world map, "Continue where you left off"
- [ ] Mascot art (SVG) and celebrations, reduced-motion aware
- [ ] Export and import progress

## M4: Accessibility and polish pass

- [ ] Keyboard and screen reader audit
- [ ] Contrast check in both themes
- [ ] Error and empty states

## C00 to C10: Content, one module per milestone

For each module, in CURRICULUM.md order:

- [ ] Check which topic files already exist and skip them
- [ ] `module.yaml`, then per topic: meta.yaml, lesson.md, quiz.yaml (status `content_written`)
- [ ] Practice skeleton, tests and solution (status `practice_written`)
- [ ] Verifier run: solution passes, skeleton fails (status `tests_verified`)
- [ ] Content checker passes and the topic appears on the map (status `ui_wired`)
- [ ] Manual read-through in the browser (status `done`)
- [ ] Boss and mini-project
- [ ] Commit `C<NN>: content for <module>`

Large modules (12 or more topics) may split into two commits, one per half. The state files are updated after each half.

## M5: Capstone

- [ ] Capstone brief, staged tests, rubric
- [ ] Final badge and completion screen

## M6: Release hardening

- [ ] Fresh-clone test on macOS and Windows: one command start
- [ ] README with setup, troubleshooting and limits

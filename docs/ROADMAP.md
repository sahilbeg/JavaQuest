# Roadmap

Each milestone ends with: project checks green, both state files updated, one commit titled `<milestone>: <what>`. Stop after each milestone.

## M0: Planning (done)

- [x] ARCHITECTURE.md, CURRICULUM.md, CONTENT_SPEC.md, GAMIFICATION.md, ROADMAP.md
- [x] BUILD_STATE.md and build_state.json
- [x] Plan approved: mascot Byte, Skip ahead kept, macOS only for now, textbook and W3Schools page style

## M1: App skeleton and practice runner (done)

- [x] M1-01 Parent POM, Maven Wrapper, modules `runner`, `app`, `tools`, and `practice/pom.xml`
- [x] M1-02 `run.sh`: build if the JAR is stale, start on port 2026, bound to 127.0.0.1
- [x] M1-03 Content catalog that checks every file, plus one sample topic (module 0 hello-world)
- [x] M1-04 Practice runner: compile, run JUnit in a child JVM, enforce limits, parse XML reports
- [x] M1-05 Measured: 0.30 to 0.49 s per submit. The 10 s limit stays.
- [x] M1-06 File watcher and SSE push. Measured: under 2 s from save to test start. JDK watcher kept.
- [x] M1-07 `tools` verifier: solution passes, skeleton fails, writes results into build_state.json
- [x] M1-08 Tests for the runner, catalog, path mapping, status ladder and HTTP endpoints

## M2: Lesson, editor and quiz UI (done)

- [x] Story pivot: Spoke workplace, tickets, `docs/STORY.md`, ticket block in `meta.yaml`
- [x] Page shell, light and dark themes, responsive layout (`DESIGN.md`)
- [x] Lesson rendering: Markdown, callouts, Mermaid, runnable snippets with "Try it yourself"
- [x] Monaco editor with Save, Run, Run checks, and reload when the IDE changes the file
- [x] All 5 quiz types, keyboard accessible, with variants and seeds
- [x] Readable compiler and test feedback. The launcher summary is stripped from the learner's output.
- [x] Content checker: prerequisites exist, Java blocks compile, snippets run, predict-output answers
- [ ] "Reset to skeleton" with a pristine copy (moved to M3)

## M3: Progress and gamification (done)

- [x] "Reset to skeleton" with a pristine `skeleton/` copy
- [x] Hint gating (hint 2 after 2 failed runs, hint 3 after 4) and solution unlock
- [x] H2 and Flyway schema
- [x] Mastery rules, mastery percent, progress survives restarts
- [x] Native file watcher (save to checks under 1 s)
- [x] XP ledger, job-title levels, streaks with freezes, kudos
- [x] Spaced repetition (SM-2) and weak-topic list
- [x] Career dashboard, ticket board, "Continue where you left off"
- [x] Byte SVG, dock animation, toasts, promotion dialog, reduced-motion aware
- [x] Export and import progress

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

- [ ] Fresh-clone test on macOS: one command start
- [ ] Windows support (`run.bat`) is deferred until requested
- [ ] README with setup, troubleshooting and limits

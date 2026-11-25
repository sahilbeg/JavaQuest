# Instructions for AI coding assistants

These rules are for any assistant working in this repository: GitHub Copilot, Claude Code, Cursor, Codex and others. The person you are helping is a **learner** in this app as well as a contributor. Their learning must never be disturbed.

## Two apps, two jobs

| | Learner app | Tester app |
|---|---|---|
| Command | `./run.sh` | `./run.sh --tester` |
| Address | http://localhost:2026 | http://localhost:3004 |
| Progress | The learner's own: `~/.cadence` (or `~/.javaquest` in older copies) | `~/.cadence-tester` |
| Ticket code | The learner's own: `practice/*/*/src/`, `tracks/*/practice/*/*/src/` | `~/.cadence-tester/code` |

- **Test and preview only with the tester app** (`./run.sh --tester`, port 3004), or `./run.sh --author` (port 2027). Both have their own progress and code, and read your changes to lessons, tests and skeletons straight from the working folder.
- **Never** run checks, submit code, take quizzes or open lessons against the learner app on port 2026.
- **Never** stop or restart the learner app (the `java` process on port 2026) unless the person asks.
- **Never** use `./run.sh --unlock-all`: it opens everything against the learner's real progress.
- **Never** delete, reset, import into or write to `~/.cadence`, `~/.javaquest` or their `backups/`.

## The learner's code

- A ticket's `src/` folder (`practice/<module>/<ticket>/src/` and `tracks/<track>/practice/<module>/<ticket>/src/`) is the learner's own work. Never edit, overwrite, reset, `git checkout`, `git restore` or `git stash` an existing ticket's `src/`.
- Fix a ticket's starting code in `skeleton/` and its answer in `solution/`. Create `src/` only for a new ticket, as an exact copy of its `skeleton/`.
- The pre-commit hook refuses commits that change an existing `src/`. Never bypass it with `--no-verify` or `ALLOW_SRC_CHANGES=1`.
- Stage files by explicit path. Never `git add -A` or `git add .`.

## Other rules

- Ticket ids never change: progress is keyed by them. Never rename or delete a released ticket or module folder.
- Don't add, change or commit anything in `avatars/`. Don't download character art.
- Don't commit or push unless the person asks.

## Checks before a pull request

```bash
./mvnw -q install                                                     # platform build and tests
./mvnw -q -pl tools dependency:properties exec:java@check-content     # every lesson and quiz
./mvnw -q -pl tools dependency:properties exec:java@verify-practice   # every solution passes, every skeleton fails
```

## Where to read more

- [CONTRIBUTING.md](CONTRIBUTING.md): the contributor workflow.
- [CLAUDE.md](CLAUDE.md): the full build guide. It covers how a content module is built, the house rules for lessons, quizzes, skeletons and tests, and the build tracker (`build_state.json`).
- `docs/CONTENT_SPEC.md`, `docs/STORY.md`, `docs/CURRICULUM.md` and `docs/WEB_COURSE_BRIEF.md`: content formats, the story's fixed facts, every unit, and how web tickets are tested.

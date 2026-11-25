# Contributing to Cadence

You can learn with Cadence and improve it at the same time. This guide shows how to do both without ever losing your own progress or your own code.

## 1. Fork, clone and start: one command

1. Fork the repository on GitHub.
2. Clone your fork and start it:

```bash
git clone https://github.com/<your-github-name>/JavaQuest.git ~/Cadence && ~/Cadence/run.sh
```

`run.sh` checks your laptop (Java 21+, and Node 20+ for the Frontend courses), builds everything, installs the web test tools, switches on the commit guard, and opens http://localhost:2026. See the [README](README.md#quick-start-one-command) if a check fails.

3. Add the original repository as `upstream`, so you can stay up to date:

```bash
git -C ~/Cadence remote add upstream https://github.com/sahilbeg/JavaQuest.git
```

## 2. The two apps: yours, and the tester's

Run Cadence twice from the same folder, side by side:

| | Learner app | Tester app |
|---|---|---|
| Start it with | `./run.sh` | `./run.sh --tester` |
| Address | http://localhost:2026 | http://localhost:3004 |
| Progress | **Yours**, in `~/.cadence` (`~/.javaquest` for copies made before the rename) | The tester's, in `~/.cadence-tester` |
| Ticket code | **Yours**: each ticket's `src/` folder in this repository | A copy in `~/.cadence-tester/code`, made from each skeleton |
| Tickets | Open one at a time | Every ticket of every course is open |
| Use it to | Learn. Your XP, level, streak and kudos are tracked here. | Check your branch before you open a pull request |

The rule is simple: **learn on 2026, test on 3004.**

- Your learner app is the only place your progress is recorded. Keep it running while you learn.
- The tester app reads the lessons, quizzes, tests and skeletons from your working folder, so it shows your changes as you make them. It never writes to your progress database or to your `src/` folders.
- After you change a ticket's skeleton, press **Reset** on that ticket in the tester app to load the new starting code.
- To start the tester from a clean slate, stop it and delete `~/.cadence-tester`.
- `./run.sh --author` (port 2027, `~/.cadence-author`) works the same way. Use it when you write lessons, if you'd like a third, separate copy.
- Don't use `./run.sh --unlock-all` for testing. It opens every ticket against your **own** progress and code.

Rebuilding is safe while both apps run: `run.sh` starts a copy of the app's jar. Restart an app to see a new build.

## 3. Make a change

```bash
git switch -c my-change
```

Then:

1. Make your change. Where things live:

   | Folder | What's in it |
   |---|---|
   | `content/`, `practice/` | The Java course: lessons (`lesson.md`), quizzes (`quiz.yaml`), ticket data (`meta.yaml`), and each code ticket's `README.md`, `skeleton/`, `src/`, `test/` and `solution/` |
   | `tracks/web/content/`, `tracks/web/practice/` | The Frontend courses, in the same shape |
   | `courses.yaml` | Every course: its goal, stack, modules and job-title ladder |
   | `app/`, `runner/`, `content-model/`, `tools/` | The web app, the test runners, the content model and the content checkers |
   | `docs/` | The curriculum, content spec, story, gamification rules and architecture |

2. Try it in the tester app: `./run.sh --tester`, then open http://localhost:3004.
3. Run the checks for what you changed:

```bash
./mvnw -q install                                                     # build, and run the platform tests
./mvnw -q -pl tools dependency:properties exec:java@check-content     # check every lesson and quiz (about 2 min)
./mvnw -q -pl tools dependency:properties exec:java@verify-practice   # every solution passes and every skeleton fails (about 5 min)
```

To check only some tickets, add `-Djq.only=<id prefix>`, for example `-Djq.only=web-03` to `check-content`.

4. Stage your files **by path**, commit, push to your fork, and open a pull request against `main`:

```bash
git add content/module-03-core-java tracks/web/content/web-05-dom-and-events
git commit -m "Short summary of the change"
git push -u origin my-change
```

The pull request template has a short checklist.

## 4. Rules that protect every learner

These hold for people and for AI coding assistants alike (see [AGENTS.md](AGENTS.md)):

- **Never commit a ticket's `src/` code.** `src/` is where each learner writes their own fixes, so yours stays on your laptop. The commit guard (`.githooks/pre-commit`) refuses commits that change an existing ticket's `src/`, for both the Java and the web courses. Fix a ticket's starting code in its `skeleton/` instead, and its reference answer in `solution/`. A new ticket adds `src/` once, as an exact copy of its `skeleton/`. Never bypass the guard with `--no-verify` or `ALLOW_SRC_CHANGES=1`.
- **Stage files by path.** Avoid `git add -A` and `git add .`. Your own fixes show up as changed `src/` files in `git status`; leave them unstaged.
- **Ticket ids never change.** Progress is keyed by them. Never rename or delete a ticket or module folder that has been released. Add new ones instead.
- **Never touch a learner's progress.** Don't delete, reset or write to `~/.cadence` or `~/.javaquest`, and don't run tests or submit code against the learner app on port 2026. Use the tester app.
- **Team pictures stay private.** `avatars/` holds each learner's own pictures, and git ignores them. Don't add any.
- **Tests must pass anywhere.** No network, no real ports (use MockMvc for Spring, jsdom for the web), files only in temporary folders, nothing that depends on the time of day.

## 5. Writing lessons and tickets

Read these before you write content:

- [`docs/CONTENT_SPEC.md`](docs/CONTENT_SPEC.md): the file formats, animations and quiz rules.
- [`docs/STORY.md`](docs/STORY.md): the world (Dhaaga & Co.) and the facts every lesson and test agrees on: prices in paise, item ids, payment modes.
- [`docs/CURRICULUM.md`](docs/CURRICULUM.md): every unit, with its id, title and prerequisites.
- [`docs/WEB_COURSE_BRIEF.md`](docs/WEB_COURSE_BRIEF.md): how the web tickets are written and tested.
- [`CLAUDE.md`](CLAUDE.md), "Rules learned while building": the house style. Explain as if to a 15-year-old: short sentences, and every jargon word defined.

A code ticket's skeleton compiles, has numbered `// TODO n:` lines, and fails its tests with clear messages. Its solution passes them. `verify-practice` checks both.

## 6. Staying up to date

```bash
git switch main
git pull --ff-only upstream main
./run.sh
```

A pull only changes tracked files, so your `src/` work stays put unless the same ticket's `src/` changed upstream, which the rules above prevent.

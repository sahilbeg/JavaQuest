# JavaQuest

Learn Java from zero to a professional, job-ready engineer, through a story. You join **Spoke**, a bike-sharing startup, as an intern. Every lesson is a ticket from a teammate, and every fix is checked by real JUnit tests. By the end you launch Spoke in a second city and get a full-time offer.

- **11 modules, 155 units:** from "what is a program" to data structures and algorithms, concurrency, Spring Boot, databases, system design, observability, Kubernetes and the interview loop.
- **Lessons** play as short cards with animations. **Onboarding checks** (quizzes) need 80% to pass.
- **Fixes** are real Java you write in the browser or in your own IDE. The app runs the tests and shows what failed.
- **Incidents** (production bugs with bronze, silver and gold tiers) and **features** close each module. The **capstone** is a Spring Boot service you build in six stages.
- XP, levels, job titles, streaks, kudos and spaced-repetition reviews keep you going.

Everything runs on your own laptop. There is no account and nothing is sent anywhere.

## Quick start

You need **macOS**, **JDK 21 or newer**, a web browser, and an internet connection for the first start.

```bash
git clone https://github.com/sahilbeg/JavaQuest.git
cd JavaQuest
./run.sh
```

Then open **http://localhost:2026**.

The first start builds the app and downloads its libraries, so it can take a few minutes. Later starts take a few seconds. Leave the terminal open while you learn, and press `Ctrl+C` to stop.

## Installing Java

Check what you have:

```bash
java -version
```

The first line must say version 21 or higher. If it doesn't, install a JDK. One way on macOS, with [Homebrew](https://brew.sh):

```bash
brew install --cask temurin@21
```

You can also download Temurin 21 from [adoptium.net](https://adoptium.net). If several Java versions are installed, point your terminal at 21:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
```

Add that line to `~/.zshrc` to keep it.

## How you learn

1. Open the next ticket on the course page. Tickets open one at a time, in order.
2. Read the knowledge-base page (the lesson), card by card.
3. Pass the onboarding check (the quiz) with 80% or more.
4. For a code ticket, fix the code until every check is green. Gojo, the team's helper, gives up to three hints. The reference solution opens once your checks pass, or after all the hints and one more try.
5. Closed tickets come back as short reviews, a day later and then less often.

The **Career** page shows your level, streak, kudos, reviews due and weak spots. **Notes** collects the syntax boxes and quick reference from every ticket you've finished.

### Working in your own IDE

Your code lives in `practice/<module>/<ticket>/src/`. Open the `practice` folder in VS Code or IntelliJ IDEA as a Maven project (`practice/pom.xml`). When you save a file, JavaQuest notices and runs that ticket's checks, the same as the **Run checks** button. Each code ticket's page also shows how to open its files in VS Code.

`Reset` on a ticket's page puts its starting code back. It replaces your `src/` files for that ticket, so use it with care.

## Your progress

- Progress is stored in `~/.javaquest/progress.mv.db` (an H2 database).
- Once a day, when you start it, JavaQuest copies that file to `~/.javaquest/backups/`, keeping the last 7 days.
- **Career → Download backup** saves a JSON file. **Restore from backup** replaces your progress with one. Use them to move to a new laptop.
- Your code is in the repository's `practice/` folders. Commit or copy it if you want to keep it.

## Commands

| Command | What it does |
|---|---|
| `./run.sh` | Learn. http://localhost:2026, your progress in `~/.javaquest`. |
| `./run.sh --server.port=2030` | The same, on another port. |
| `./run.sh --author` | Every ticket open, on http://localhost:2027, with a separate database in `~/.javaquest-author`. For writing and reviewing content. Your learning is untouched. |
| `./run.sh --javaquest.data-dir=<folder>` | Keep progress in another folder, for example a throwaway one. |

`./run.sh --unlock-all` also opens every ticket, but against **your own** progress. Avoid it unless you mean to skip ahead.

## Troubleshooting

**"JavaQuest needs Java 21 or newer"** or **"Java was not found".** Install JDK 21 and point `JAVA_HOME` at it, as shown in [Installing Java](#installing-java). Then run `./run.sh` again.

**"Port 2026 is already in use".** JavaQuest is probably already running in another terminal: open http://localhost:2026. To run a second copy, use another port, for example `./run.sh --server.port=2030`.

**The first start fails while downloading.** The first build needs the internet to fetch Maven and the libraries. Check your connection (and any company proxy), then run `./run.sh` again. It carries on where it stopped.

**The page says "Lost the connection to JavaQuest" or "Something went wrong".** The app stopped. Look at the terminal for the error, then start it again with `./run.sh`.

**Checks time out.** Spring tickets start a whole application, so their first run is slower. Most plain tickets stop a run after 10 seconds, so a timeout there usually means an endless loop. Look for a `while` loop that never ends.

**Saving in the IDE doesn't run the checks.** Make sure you edit files under `practice/<module>/<ticket>/src/`, not `skeleton/` or `solution/`. You can always press **Run checks** in the browser.

**A commit is refused with "Unstage those files".** The repository has a guard that stops commits from changing a ticket's `src/` folder (your own code), so the course stays clean for other learners. Commit your code somewhere else, or keep it locally.

**You want an earlier day's progress back.** Stop JavaQuest. Copy a file from `~/.javaquest/backups/` over `~/.javaquest/progress.mv.db`, then start it again.

## Limits

- **macOS only, for now.** `run.sh` is a bash script, and saving in your IDE uses macOS file events. Linux may work but is untested. Windows isn't supported yet.
- **One learner per progress folder.** There are no accounts and no sync between laptops. Use backups to move.
- **Local only.** The app listens on `127.0.0.1` and refuses other host names, so other computers can't reach it.
- **Your code runs on your laptop**, in a separate Java process with a time limit (usually 10 seconds, at least 60 for Spring tickets) and a memory limit (256 MB, 512 MB for Spring). It is not a sandbox: only run code you understand.
- **Dates use your laptop's clock and time zone:** streaks, the daily limit and review days.
- **English only.**
- Team pictures are not included. You can add your own: see [`avatars/README.md`](avatars/README.md).

## For contributors

The course is content plus a small Spring Boot app:

| Folder | What's in it |
|---|---|
| `content/` | Lessons (`lesson.md`), quizzes (`quiz.yaml`) and ticket data (`meta.yaml`), one folder per ticket |
| `practice/` | Each code ticket's `README.md`, `skeleton/`, `src/` (the learner's copy), `test/` and `solution/` |
| `app/`, `runner/`, `content-model/`, `tools/` | The web app, the test runner, the content model and the content checkers |
| `docs/` | The curriculum, content spec, story, gamification rules, architecture and roadmap |

Useful commands:

```bash
./mvnw -q install                                                     # build and run the platform tests
./mvnw -q -pl tools dependency:properties exec:java@check-content     # check every lesson and quiz
./mvnw -q -pl tools dependency:properties exec:java@verify-practice   # every solution passes, every skeleton fails
```

Start with [`docs/CONTENT_SPEC.md`](docs/CONTENT_SPEC.md) and [`CLAUDE.md`](CLAUDE.md), which hold the house rules for writing tickets. `run.sh` switches on the commit guard (`.githooks/pre-commit`) the first time it runs in a clone.

# Cadence

Become a software engineer through a story. Every lesson is a ticket from a teammate, and every fix you write is checked by real tests. Cadence runs on your own laptop: there is no account, and nothing is sent anywhere.

Pick a goal and a stack when you start, and you get your own path and your own job-title ladder:

| Course | Stack | What you build |
|---|---|---|
| **Software engineering** | Java | 13 modules and 175 units, from "what is a program" to algorithms, concurrency, Spring Boot, system design, DevOps, databases and the interview loop. You intern at Spoke, a bike-sharing startup, and finish with a full-time offer. |
| **Frontend** | JavaScript + React | 11 modules: how the web works, HTML, CSS, JavaScript, the DOM, async code, tooling, then React, ending with the online store for Dhaaga & Co., a crochet stall in Pune. |
| **Frontend** | Plain JavaScript | The same web basics, then components, single-page apps and production work, with no framework. |
| **Frontend** | React + TypeScript | The web basics, then TypeScript, then typed React. |
| **Frontend** | Next.js | The web basics and React, then the Next.js app router, server components, data, and production. |

Backend and full-stack courses are on the way. In every course:

- **Lessons** play as short cards with animations. **Onboarding checks** (quizzes) need 80% to pass.
- **Fixes** are real code you write in the browser or in your own editor. Java is checked with JUnit; JavaScript, React, TypeScript and Next.js with Vitest.
- **Incidents** (production bugs, with bronze, silver and gold tiers) and **features** close each module.
- XP, levels, job titles, streaks, kudos and spaced-repetition reviews keep you going.

## Quick start: one command

You need a **Mac**, [Git](https://git-scm.com) and an internet connection for the first start. Paste this into Terminal:

```bash
git clone https://github.com/sahilbeg/JavaQuest.git ~/Cadence && ~/Cadence/run.sh
```

If you forked the repository, use your fork's address instead:

```bash
git clone https://github.com/<your-github-name>/JavaQuest.git ~/Cadence && ~/Cadence/run.sh
```

That one command downloads Cadence into `~/Cadence`, checks your laptop, builds the app, starts it, and opens **http://localhost:2026**. The first start downloads about 400 MB of tools and libraries and builds the app, so it takes a few minutes. Later starts take seconds: run `~/Cadence/run.sh`. Leave the terminal open while you learn, and press `Ctrl+C` to stop.

The repository is private for now, so GitHub asks you to sign in. Ask its owner for access first.

### What you need, and what `run.sh` checks

| Check | If something is wrong |
|---|---|
| macOS | On Windows it stops. Linux gets a warning, because it is untested. |
| Java 21 or newer | Always needed: Cadence itself is a Java app. If no JDK 21 is installed, `run.sh` offers to install Temurin 21 with Homebrew, or tells you how. |
| Node 20 or newer | Needed for the Frontend courses, which check your code with Node. The Java course works without it. Install it with `brew install node`. |
| The port (2026) | If Cadence is already running there, it just opens it. If another program uses the port, it suggests another one. |
| Disk space and internet | Checked on the first start only, because that's when it downloads. |

To see the checks without starting anything, run `./run.sh --check`. To see every option, run `./run.sh --help`.

## Two ways to run Cadence: learning and testing

Everyone who uses this repository is a learner. Many are also contributors. Cadence keeps the two apart, so working on Cadence can never cost you your progress:

| | **Learn** | **Test your changes** |
|---|---|---|
| Command | `./run.sh` | `./run.sh --tester` |
| Address | http://localhost:2026 | http://localhost:3004 |
| Tickets | Open one at a time, as you progress | Every ticket of every course is open |
| Progress | Yours, in `~/.cadence`, backed up daily | The tester's own, in `~/.cadence-tester` |
| Your code | `practice/.../src/` and `tracks/web/practice/.../src/` in this folder | A separate copy in `~/.cadence-tester/code` |
| Use it for | Learning: your XP, level and streak count here | Trying a branch before a pull request |

Both can run at the same time from the same folder. Nothing you do on port 3004 changes your progress or your code. There is also `./run.sh --author` (port 2027, `~/.cadence-author`) for writing and reviewing lessons, which works the same way.

To start the tester from scratch, stop it and delete `~/.cadence-tester`. To pick up a changed skeleton in the tester, press **Reset** on that ticket.

Want to contribute? Read [CONTRIBUTING.md](CONTRIBUTING.md).

## How you learn

1. Open the next ticket on the course page. Tickets open one at a time, in order.
2. Read the knowledge-base page (the lesson), card by card.
3. Pass the onboarding check (the quiz) with 80% or more.
4. For a code ticket, fix the code until every check is green. The team's helper gives up to three hints. The reference solution opens once your checks pass, or after all the hints and one more try.
5. Closed tickets come back as short reviews, a day later and then less often.

The **Career** page shows your level, streak, kudos, reviews due and weak spots. **Notes** collects the syntax boxes and quick reference from every ticket you've finished. **Switch course** on the course page takes you back to the picker; each course keeps its own XP and ladder.

### Working in your own editor

Your code lives in this folder:

- Java: `practice/<module>/<ticket>/src/`. Open the `practice` folder in VS Code or IntelliJ IDEA as a Maven project (`practice/pom.xml`).
- Frontend: `tracks/web/practice/<module>/<ticket>/src/`.

When you save a file, Cadence notices and runs that ticket's checks, the same as the **Run checks** button.

**Reset** on a ticket's page puts its starting code back. It replaces your `src/` files for that ticket, so use it with care.

## Your progress

- Progress is stored in `~/.cadence/progress.mv.db` (an H2 database). If you started before the project was renamed from JavaQuest, it stays in `~/.javaquest`, and `run.sh` keeps using it.
- Once a day, when you start it, Cadence copies that file to `~/.cadence/backups/`, keeping the last 7 days.
- **Career → Download backup** saves a JSON file. **Restore from backup** replaces your progress with one. Use them to move to a new laptop.
- Your code is in this folder's `src/` folders. A commit guard stops it from going into the repository (see [CONTRIBUTING.md](CONTRIBUTING.md)), so copy it elsewhere if you want to keep it.

## Commands

| Command | What it does |
|---|---|
| `./run.sh` | Learn. Check the laptop, build if needed, start, and open http://localhost:2026. Your progress is in `~/.cadence`. |
| `./run.sh --tester` | Test your changes. Every ticket open, on http://localhost:3004, with its own progress and code in `~/.cadence-tester`. |
| `./run.sh --author` | Write and review lessons. Every ticket open, on http://localhost:2027, with its own progress and code in `~/.cadence-author`. |
| `./run.sh --check` | Only check the laptop, and say what to fix. |
| `./run.sh --help` | Show every option. |
| `./run.sh --no-browser` | Start without opening the browser. |
| `./run.sh --server.port=2030` | Start on another port. |
| `./run.sh --cadence.data-dir=<folder>` | Keep progress in another folder, for example a throwaway one. |

`./run.sh --unlock-all` also opens every ticket, but against **your own** progress and code. Avoid it unless you mean to skip ahead.

## Troubleshooting

**"Cadence needs Java 21 or newer".** Let `run.sh` install it with Homebrew when it asks, or install it yourself: `brew install --cask temurin@21`, or download Temurin 21 from [adoptium.net](https://adoptium.net). If several versions are installed, add `export JAVA_HOME="$(/usr/libexec/java_home -v 21)"` to `~/.zshrc`.

**"Node 20 or newer isn't installed".** The Frontend courses need it to check your code. Run `brew install node`, then start Cadence again.

**"Port 2026 is used by another program".** Start on another port, for example `./run.sh --server.port=2030`. (If Cadence itself is already running there, `run.sh` just opens it.)

**"The build failed" on the first start.** The first build needs the internet to fetch Maven, the libraries and the Node tools. Check your connection (and any company proxy), then run `./run.sh` again. It carries on where it stopped.

**The page says "Lost the connection to Cadence" or "Something went wrong".** The app stopped. Look at the terminal for the error, then start it again.

**Checks time out.** Spring tickets start a whole application, so their first run is slower. Most other tickets stop after 10 seconds, so a timeout there usually means an endless loop.

**Saving in the editor doesn't run the checks.** Make sure you edit files under a ticket's `src/` folder, not `skeleton/` or `solution/`. You can always press **Run checks** in the browser.

**"Git is not installed".** On a Mac, run `xcode-select --install`, then try again.

**A commit is refused with "Unstage those files".** The commit guard stops commits from changing a ticket's `src/` folder, which is a learner's own code. See [CONTRIBUTING.md](CONTRIBUTING.md).

**You want an earlier day's progress back.** Stop Cadence. Copy a file from `~/.cadence/backups/` over `~/.cadence/progress.mv.db`, then start it again.

## Limits

- **macOS only, for now.** `run.sh` is a bash script, and saving in your editor uses macOS file events. Linux may work but is untested. Windows isn't supported yet.
- **One learner per progress folder.** There are no accounts and no sync between laptops. Use backups to move.
- **Local only.** The app listens on `127.0.0.1` and refuses other host names, so other computers can't reach it.
- **Your code runs on your laptop**, in a separate process with a time limit and a memory limit. It is not a sandbox: only run code you understand.
- **Dates use your laptop's clock and time zone:** streaks, the daily limit and review days.
- **English only.**
- Team pictures are not included. You can add your own: see [`avatars/README.md`](avatars/README.md).

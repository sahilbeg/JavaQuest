# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

One learner: a new CS student who has never written software. They study alone at home on a laptop, in short sessions, with their IDE open next to the browser. Their job is to go from zero to professional Java engineer, one topic at a time.

## Product Purpose

JavaQuest teaches Java from first principles to professional backend and architecture skills. It renders pre-written lessons, quizzes and coding exercises stored in the repo, grades the learner's code with real JUnit tests, and tracks mastery. Success means the learner masters each topic (quiz at 80% or more plus passing tests) and keeps coming back.

## Positioning

The practice code lives in the learner's own repo and IDE. Saving a file in the IDE grades it live in the browser. Every exercise is checked by real JUnit tests on a real JDK, on the learner's machine, offline, with no account.

## Operating Context

- Started with `./run.sh`, served at http://localhost:2026, macOS only for now.
- The learner switches between the browser and an IDE editing `practice/`.
- Sessions are short: 5 to 10 minute lessons, one quiz, one exercise.
- Tickets open one at a time, in order: the next one opens when the current one is mastered.
- An optional daily limit (always in the top bar, off by default) paces the course: after that many new tickets in a day, the next one opens the following day. Finished tickets and reviews stay open for revisiting.

## Capabilities and Constraints

- Content is written once and committed. Nothing is generated at runtime.
- Order follows a textbook. A lesson plays as short cards, one idea each, after an overview of what the ticket teaches. Animations show code running and data moving, so learners read less.
- Every page has a first-visit walkthrough with Byte.
- Story: the learner is an intern at Spoke, a bike-sharing startup. Topics are tickets, bosses are incidents, mini-projects are features, levels are job titles. See `docs/STORY.md`.
- Game layer: XP, job titles, streaks, kudos, the ticket board, incidents. Rules are in `docs/GAMIFICATION.md`.
- The interface is a modern developer tool for adults, dark first (see `DESIGN.md`): a course outline, a lesson player, and Byte as a small helper mark. The Spoke story stays in the tickets, teammates and career ladder.
- Must work offline. All browser libraries are vendored.
- Spring Boot server with plain ES modules. No Node build step.

## Brand Commitments

- Name: JavaQuest. The fictional company inside it is Spoke, in the city of Riverton.
- Mascot: Byte, a small owl and the team's chat helper. Byte encourages and never blames.
- Learning happens through real-world cases: every concept is taught as the way to solve a concrete Spoke problem.
- Voice: explain like the reader is 15 and has never coded. Short sentences, analogies, no unexplained jargon.

## Evidence on Hand

- One complete topic: `content/module-00-foundations/topic-05-hello-world/`.
- No mascot artwork exists yet.

## Product Principles

- Learning comes first. The game layer makes progress feel fun and visible, and never blocks reading or coding.
- A mix of game and textbook: rewarding and playful, and still easy to understand. The game is the job: real problems, a team, a career ladder.
- Feedback is kind and specific. Every failure says what to try next.
- The learner's IDE is a first-class editor, equal to the browser editor.

## Accessibility & Inclusion

WCAG 2.2 AA in light and dark themes. Full keyboard use, including drag-and-order questions. Honour `prefers-reduced-motion`.

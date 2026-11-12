# Design

The visual system for JavaQuest, written from the built app after M2. The direction contract lives in `.impeccable/surfaces/app-src-main-resources-static-index-html.md`. Tokens live in `app/src/main/resources/static/css/app.css`.

## World

JavaQuest looks like the internal tools of Spoke, the bike-sharing startup in `docs/STORY.md`. Topics are tickets, lessons are KB pages, quizzes are onboarding checks, tests are checks. Byte the owl lives in a chat panel.

## Colour

Restrained: aluminium neutrals and one cobalt for action and focus. Status colours appear only on checks and ticket states.

| Token | Light | Dark | Use |
|---|---|---|---|
| `--ground` | oklch(96.6% 0.005 250) | oklch(17.5% 0.012 262) | Page |
| `--surface` | oklch(99.5% 0.002 250) | oklch(21.5% 0.014 262) | Cards, panels, top bar |
| `--ink` / `--ink-soft` | oklch(23% 0.02 262) / 42% | oklch(94%) / 74% | Text |
| `--cobalt` | oklch(48% 0.2 264) | oklch(72% 0.14 264) | Primary action, focus, current item |
| `--pass` / `--fail` / `--running` | green / red / amber | lighter versions | Checks and ticket state only |
| `--tm-*` | one hue per teammate | lighter versions | Avatars |

Theme follows the system until the learner picks one with the top-bar switch (stored in `localStorage` as `jq-theme`).

## Type

- UI and reading: Atkinson Hyperlegible Next, 400 and 700, self-hosted. It was chosen for legibility for beginners.
- Code, ticket keys and data: JetBrains Mono, 400 and 700, self-hosted.
- KB pages: 1.06rem, line height 1.65, measure 72ch.

## Components

- **Ticket card**: key, title, teammate avatar, state chip.
- **Continue strip**: the next open ticket at large size, with the project's dock row. When every ticket is closed it says so instead.
- **Dock row**: one post per ticket on a rail. A closed ticket docks a bike icon, animated once.
- **Ticket page**: project rail, ticket header with the requester, tabs (KB page, Onboarding check, Fix), Byte chat panel.
- **Callouts**: tinted block with a short label (Tip, Careful, Think of it like, New word). No coloured side borders.
- **Syntax box**: the code block right after a "Syntax:" heading gets a cobalt-tinted ground.
- **Checks list**: one row per JUnit test, pass or fail icon, the test's message, and a plain-language line for compiler errors.
- **Icons**: Lucide, one stroke weight, from `vendor/icons/icons.svg`.

## Motion

One authored moment: a bike docking into its slot when a ticket closes. Chat messages fade in. All motion is off under `prefers-reduced-motion`.

## Layout

Ticket page columns: 220px rail, flexible main, 320px chat. Under 1180px the chat moves below the main column and a "Chat with Byte" button jumps to it. Under 820px the rail collapses into a disclosure.

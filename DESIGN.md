# Design

The visual system for JavaQuest, rebuilt in November 2026 after user feedback: make it modern and playful, show an overview first, open tickets one at a time, teach with animations instead of long reading, and walk new users through each page. Tokens live in `app/src/main/resources/static/css/app.css`.

## Direction

A playful learning app, in the spirit of Duolingo or Brilliant, that keeps the Spoke story. The learner is still an intern at Spoke and tickets still come from teammates. The look is now friendly and game-like rather than an internal ticket tool.

- **Overview first, depth on demand.** The home page is a journey map of projects. Only the current project is open. A lesson starts with an overview card, and the detail comes one card at a time.
- **One thing at a time.** One ticket, one card, one quiz question, one walkthrough step.
- **Show, don't tell.** Animations show code running and data moving, with a one-line caption per step.
- **Celebrate progress.** XP floats up, confetti marks a closed ticket, a promotion or a new level, and Byte cheers.

## Colour

A warm cream page, white cards, and four accents. Each accent has one job.

| Token | Light | Dark | Use |
|---|---|---|---|
| `--bg` | cream oklch(97.6% 0.013 85) | navy oklch(18.5% 0.025 272) | Page, with two soft colour glows |
| `--surface` | white | oklch(23.5% 0.03 272) | Cards, top bar, bubbles |
| `--ink` / `--ink-soft` | oklch(24%) / 44% | oklch(95%) / 79% | Text |
| `--primary` | Spoke green oklch(50% 0.14 158) | oklch(77% 0.15 154) | Main action, done, progress |
| `--amber` | oklch(80% 0.15 78) | | XP, stars, Byte, variable boxes |
| `--violet` | oklch(52% 0.2 292) | oklch(76% 0.13 292) | Animations, objects and references |
| `--sky` | oklch(53% 0.13 240) | | Links, selected answers, focus ring |
| `--coral` | oklch(53% 0.19 27) | | Failing checks, incidents, mistakes |
| `--owl-*` | | | Byte's body, belly, eyes and beak |

Text on every surface meets 4.5:1. White text sits only on the darker light-theme primary; in dark mode the primary is light and its text is dark. The theme follows the system until the learner picks one (stored in `localStorage` as `jq-theme`).

## Type and shape

- Atkinson Hyperlegible Next 400 and 700 for UI and reading, chosen for beginner legibility. JetBrains Mono for code.
- Lesson text: 1.06rem, line height 1.7, measure 70ch.
- Radii: 28px hero cards, 22px cards, 14px buttons and inputs, full pills for chips and stats.
- Buttons are "pressable": a 3px bottom edge in a darker shade, which collapses when pressed.

## Byte

`js/byte.js` draws Byte as an illustrated owl (body, belly, eye rings, wings, tufts, feet), in sizes `sm`, `md`, `lg` and `hero`. Moods: idle, thinking, cheering, encouraging and surprised change the eyes and beak. Byte blinks, and waves when greeting. `byteSays()` puts Byte beside a speech bubble.

## Pages

- **Journey** (`js/journey.js`, home): Byte's greeting, an "Up next" card for the one open ticket, a review strip when reviews are due, and the path. Each project is a stop: done (check badge, tickets one click away), current (its tickets as stepping stones: done, up next with a pulse, or locked), or locked (its summary only).
- **Lesson player** (`js/lesson.js`): a sticky bar with a segmented progress bar, one card on stage, and Back and Next at the bottom (the arrow keys work too). Steps: ticket, learn cards, mistakes, recap, check, code, done. Byte lives in a floating helper button that opens the chat drawer with hints.
- **Locked ticket**: Byte thinking, what to finish first, and a button to the current ticket.
- **Career** and **Review** keep their content, restyled as cards.

## Components

- **Stepping stone**: a 52px circle, with states done, current, open and locked; incidents and features get a coral or violet rim. A tooltip shows the ticket title.
- **Animation** (`js/animate.js`): a violet-framed figure with a Watch overlay, the stage, Byte's caption and the controls (restart, back, play/pause, next, dots, step count). Code animations show the code with a moving line pointer, memory (amber variable boxes, violet object cards with arrows) and a console. Scene animations show boxes that glow when focused and tokens that fly between them.
- **Walkthrough** (`js/tour.js`): the page dims, a spotlight with an amber ring glides to each target, and a bubble shows Byte with a title, one or two sentences, and Back, Next and Skip. It plays once per page; the `?` button replays it. Escape closes it and focus is trapped in the bubble.
- **Quiz**: one question per view with a dot pager; answers get a verdict and a link back to the card that teaches them.
- **Checks**: one row per JUnit test, sliding in one after another; incident tiers as bronze, silver and gold pills.
- **Callouts**: tinted cards with an emoji label (💡 tip, ⚠️ careful, 🧩 think of it like, 📖 new word).

## Motion

Spring easing for pops and presses, ease-out for slides. Cards slide in from the side you are moving towards, and progress fills animate. Confetti (`js/celebrate.js`) is a two-second canvas burst. Under `prefers-reduced-motion` every animation and transition is effectively off, animations never auto-play, and walkthrough spotlights jump instead of gliding.

## Layout

The top bar is sticky and blurred. The journey and career pages are 1040px wide. Lesson cards are 820px wide, and 1180px for the code step. At 900px and below, hero grids stack and the code animation stacks its panels. At 640px and below, the top bar shows icons only, stops shrink, and the player bar hides the title.

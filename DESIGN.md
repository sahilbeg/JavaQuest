# Design

JavaQuest looks like a modern developer tool for adults. The direction was set in November 2026 from user feedback ("modern and adult, not old or childish") and references: Linear, Vercel's Geist, Raycast, Brilliant and master.dev. Tokens live in `app/src/main/resources/static/css/app.css`.

## Principles

- **Quiet and precise.** Neutral greys, hairline borders, small radii, one accent colour. The content is the colour.
- **Overview first.** The course page is a syllabus; a lesson opens on what it will teach, then one card at a time.
- **One thing at a time.** One ticket, one card, one quiz question, one walkthrough step.
- **Show, don't tell.** Animations show code running and data moving, with a one-line caption per step.
- **Progress without noise.** XP, level and streak are small stats; success is a soft glow and a check mark, not confetti.

## Colour

Dark by default; the light theme is one click away and remembered in `localStorage` as `jq-theme`.

| Token | Dark | Light | Use |
|---|---|---|---|
| `--bg` | oklch(14.6% 0.004 286) | oklch(98.5%) | Page, with a faint indigo glow at the top |
| `--surface` / `--surface-2` | 18.4% / 21.2% | white / 97.6% | Cards, rows, panels |
| `--line` / `--line-strong` | 26.5% / 33% | 91.6% / 86.5% | Hairline borders, tracks |
| `--ink` / `--ink-soft` / `--ink-faint` | 95.5% / 73% / 60% | 21% / 44% / 52% | Text |
| `--button` | near-white | near-black | Primary buttons (high-contrast neutral, as in Vercel and Raycast) |
| `--accent` | indigo oklch(70% 0.15 277) | oklch(51% 0.21 277) | Progress, current item, focus, links, references in animations |
| `--green`, `--amber`, `--red`, `--blue` | | | Status only: done, in progress or XP, failing or incident, to do |

Every text colour meets 4.5:1 on its surface in both themes.

## Type

- Inter (variable, self-hosted, OFL) for UI and reading, with tight tracking on headings (−0.02 to −0.03em).
- JetBrains Mono for code, and for small uppercase labels: eyebrows, keys, counts, panel names.
- Body 15px; lesson text 16px with line height 1.75 and a 68ch measure.

## Team pictures

A teammate's avatar is a small circle with their initials on their colour. When the learner puts their own picture in `avatars/` (for example `tsunade.png`; see `avatars/README.md`), the circle shows it instead, cropped to a circle with a thin ring in their colour. The pictures are the learner's, stay on their laptop (git ignores them), and are served by `/api/team/avatars`. PNG, JPEG, WebP and GIF only, up to 2 MB; SVG is refused.

## Shape and depth

Radii: 6px for inputs and small boxes, 8px for buttons and rows, 12px for cards. Borders do most of the work; shadows are soft and mostly used for floating elements (cards, drawer, toasts, walkthrough).

## Gojo, the helper

Gojo (Satoru Gojo), the team's helper, is a small round mark (`js/byte.js`; the file keeps its old name). With the learner's own `gojo.jpg` in `avatars/` it shows that picture; without one, a simple monochrome mark (spiky hair and a blindfold) that draws in `currentColor`. It appears in the logo, the Hints button, chat messages, animation captions and walkthrough bubbles. No moods.

## Pages

Every page has a real address that can be bookmarked and reloaded: `/` (course), `/t/<ticket id>` (lesson, with `?step=` for the card), `/notes`, `/notes/<ticket id>`, `/review/<ticket id>` and `/career`. The server answers each with the one page (`PageController`), and links inside the app open without a reload. Old `/#/...` links still work.


- **Course** (`js/journey.js`, home): greeting and overall progress; an "Up next" card for the one open ticket; a review strip when reviews are due; the course outline as a numbered syllabus. Each module row shows its status, a thin progress bar and a count. The current module is open, showing its tickets as rows (done, up next, locked; incident and feature tags). Done modules fold away, and locked modules show their summary only.
- **Lesson player** (`js/lesson.js`): a sticky bar with a segmented progress bar, one card on stage, Back and Next at the bottom (arrow keys too), and a Hints button that opens Gojo's drawer. Steps: ticket, learn cards, mistakes, recap, check, code, done.
- **Done for today**: when the daily limit is reached, the "Up next" card becomes a rest card (green top edge) naming the ticket that opens tomorrow, and that ticket's row shows an amber clock and "Tomorrow".
- **Continue** (the ▷ tab in the top nav, `/api/resume`): back to the exact card of the unfinished ticket the learner last had open, from any page. The lesson player saves each card it shows with the progress (`lesson_position`: card, furthest card, total and card title), so a rebuild, a hard reload or another browser opens the same screen; a card is found again by its title when a rebuild adds or removes cards. The course page's "Up next" card says "You were on card 5 of 17: ..." and its button reads "Continue at card 5". The tab hides while that ticket is on screen and when nothing is unfinished.
- **Notes** (`js/notes.js`, `/notes`): quick reference for every finished ticket, grouped by module, with a filter. Each ticket's page (`/notes/<id>`) has bordered sections: Key points, Syntax, Words to know and Common mistakes, plus "Open the full lesson", print, and previous/next. The ticket's last card links to it.
- **Career** and **Review**: the same cards and lists.

## Components

- **Animation** (`js/animate.js`): a bordered panel with a mono header, a Watch overlay, the stage, the caption and compact controls. Code animations show code with a highlighted line, memory (variable boxes, object cards, indigo reference arrows) and a console. Scenes show boxes, links and travelling tokens.
- **Walkthrough** (`js/tour.js`): the page dims, a spotlight with a thin indigo edge moves to each target, and a small bubble explains it. It plays once per page; the `?` button replays it.
- **Callouts**: bordered panels with a coloured dot and a mono label (Tip, Careful, Think of it like, New word).
- **Daily limit** (`js/daily.js`): always on screen, in the sticky top bar on every page. A switch turns it on, a stepper sets tickets per day, and a mono status shows today's count ("2/3 today", amber "Done today · 3/3"). Changes save at once. The status has a fixed width, so the + and − buttons never move between clicks.
- **Read aloud** (`js/speech.js`): a small pill with Listen and a gear, at the top right of every reading card (ticket, learn cards, mistakes, recap) and on each Notes section, plus "Listen to all" on a notes page. It uses the browser's own speech (Web Speech API), reads paragraph by paragraph with an indigo highlight, skips code and animations, and opens collapsed mistakes as it reaches them. The gear opens a panel with the voice (English voices plus the browser's language, no joke voices), speed (0.5× to 2×) and "Try it". Both are saved with the progress (`/api/settings/voice`, the `player` row), so they survive reloads, restarts and other browsers and are in backups; `localStorage` (`jq-voice`, `jq-rate`) only gives a quick start. The voice is matched by name, and reading waits up to two seconds for the browser's voice list. Reading stops on a new card or page.
- **Checks**: a joined list of JUnit results, with incident tiers as bronze, silver and gold pills.

## Motion

Short fades and 6–14px slides with an ease-out curve. Cards slide in from the side you move towards, and bars fill smoothly. Success is a soft green glow. Under `prefers-reduced-motion` everything is effectively instant and animations never auto-play.

## Layout

The top bar is 56px, sticky and blurred. Below 1280px it drops the XP bar, title, "a day" and the nav labels (the icons stay); below 760px the wordmark text. At 820px and below it is 100px tall, with the daily limit on its own second row. The course and career pages are 960px wide. Lesson cards are 760px, and 1160px for the code step. At 900px and below, cards stack and module rows drop their bars. At 640px and below, the top bar shows icons only and rows hide their keys.

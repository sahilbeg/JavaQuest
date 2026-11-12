---
version: 1
slug: "app-src-main-resources-static-index-html"
primary_target: "app/src/main/resources/static/index.html"
related_targets: []
---

## Scope

Whole JavaQuest web app: ticket board (home), ticket page (KB lesson, onboarding check, the fix editor, Byte chat), incidents later. Mode: Operate, with the KB page in Read mode.

## Audience and job

One beginner learner at home on a laptop, IDE open beside the browser, short evening sessions. Job: pick up the next ticket, read the KB page, pass the check, fix the code until the checks go green.

## Constraints

Offline, vendored libraries, plain ES modules, WCAG 2.2 AA in light and dark, keyboard complete, reduced motion honoured. Story words from docs/STORY.md.

## Direction contract

THESIS: JavaQuest is Spoke's own internal tooling, used on your first job. It refuses the course-catalog grid and the cartoon learning path; the unit of progress is a ticket you close.

OWN-WORLD: Neutral aluminium greys of a bike frame, one Spoke cobalt for action and focus, and status colours reserved for checks only: green pass, red fail, amber running. Ticket keys and code in monospace. Teammates are initial avatars, each with its own tint. Byte is a small flat owl drawn in SVG. Project progress is a docking station: one dock slot per ticket, and a closed ticket docks a bike.

STORY: The learner sees what is assigned and why it matters to riders. They read the KB page, pass the onboarding check, fix the code, and watch the checks turn green, from the browser or their IDE.

FIRST VIEWPORT: Board. Top bar with the Spoke wordmark and a theme switch. A "Continue" strip showing the current ticket at large size: key, title, teammate, and a dock row of the project's slots. Below it, project columns of ticket cards. On a ticket page: the ticket header across the top, tabs for KB, Check and Fix on the left, the Byte chat panel on the right.

FORM: Real workplace tools (user-pinned; replaces roll position 3). Seed key 845057fd.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance

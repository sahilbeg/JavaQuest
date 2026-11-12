# Story

JavaQuest is told as one story. You join **Spoke**, a bike-sharing startup in the city of Riverton, as a software intern. Every topic is a real problem the company has. You learn the concept, then fix the problem. By the end you are the engineer who launches Spoke in a second city.

## The company

Spoke rents bikes from docking stations around Riverton. Riders unlock a bike at one station, ride, and dock it at another. Spoke charges by the minute and sells memberships. The company starts small, with a few stations and one kiosk, and grows with every module.

The first stations are Riverside, Old Mill, Harbor and Uptown, with 12 docks each. Depot opens in module 4 as the fifth station. Bike ids look like B-1042.

## Cast

| Character | Role | What they bring you |
|---|---|---|
| **Byte** | The team's owl mascot, who lives in team chat as a helper bot | Explains concepts, gives hints, cheers. Never blames. |
| **Maya Okafor** | Lead engineer, your manager | Assigns most tickets. Reviews your code. |
| **Theo Lindqvist** | Operations engineer | Raises incidents when something breaks in production. |
| **Priya Raman** | Product manager | Asks for features. Explains what riders need. |
| **Sam Ortiz** | Rider support | Forwards rider complaints that turn into bug tickets. |

Characters speak only in tickets, chat and incidents. A lesson may name the teammate who sent the ticket for context, but teammates never do the teaching. The teaching itself stays plain text.

## Words used in the app

| Game idea | Word in the app |
|---|---|
| Topic | Ticket, keyed `SPK-MMTT` (module and topic number, for example `SPK-0105`) |
| Module | Project, shown on the board as a column of tickets |
| Lesson | Knowledge base page ("KB") |
| Quiz | Onboarding check |
| Coding exercise | The fix, graded by checks (the tests) |
| Boss challenge | Incident, keyed `INC-MM` |
| Mini-project | Feature, keyed `FEAT-MM` |
| Level title | Job title |
| Badge | Kudos |
| Hint | A message from Byte in chat |

## Arc by module

| Module | Project | Example tickets | Incident (boss) | Feature (mini-project) |
|---|---|---|---|---|
| 00 Foundations | Day one | Set up your laptop, make the kiosk say hello, first commit | INC-00: the kiosk welcome screen is broken on launch day | FEAT-00: a printed welcome card for new riders |
| 01 Basics | The fare kiosk | Store a ride's minutes, compute the fare, read the rider's choice, loop over docks, format a receipt | INC-01: the kiosk charges the wrong fare after midnight | FEAT-01: ride receipt printer |
| 02 OOP | Modelling the fleet | Bike, EBike and Station classes, membership types, ride records | INC-02: e-bikes are billed as normal bikes | FEAT-02: the membership system |
| 03 Core Java | Ride data | Lists of rides, a station map, a typed repository, dock errors, import ride logs, ride durations, streams for reports | INC-03: the monthly ride report is wrong | FEAT-03: ride-log analytics |
| 04 DSA | Moving bikes around | Sort rides, search by time, nearest free dock, station graph routes, cheapest pass | INC-04: the route planner sends riders to full stations | FEAT-04: rebalancing-truck planner |
| 05 Advanced Java | Rush hour | Two riders unlock one bike, thread pools for bike pings, payment and unlock in parallel, ride events as sealed types | INC-05: double unlock at rush hour | FEAT-05: bike telemetry ingester |
| 06 Craft | Paying off debt | Clean up the pricing code, strategy pattern for promos, tests, a mocked payment gateway, logging | INC-06: refactor the legacy billing module safely | FEAT-06: pluggable promo engine |
| 07 Tooling | Shipping safely | Fix the build, branch and merge, CI checks, a Docker image | INC-07: the release pipeline is red | FEAT-07: package the kiosk as a runnable jar |
| 08 Backend | The Spoke API | HTTP, REST design, Spring Boot rentals API, validation, SQL, JPA, transactions for pay-and-unlock, rider login | INC-08: half-finished rentals after a crash | FEAT-08: the station availability API |
| 09 Architecture | Growing up | Layers, ports and adapters, ride events, caching station status, scaling, observability | INC-09: the live map falls over at peak | FEAT-09: event-driven billing |
| 10 Professional | Full-time offer | Review teammates' pull requests, find your way in a big codebase, write docs, estimate, interview | INC-10: the mock interview loop | Capstone: launch Spoke in Bramford |

## Career ladder

Levels map to job titles every 5 levels: Intern, Junior Developer, Developer, Software Engineer, Senior Engineer, Staff Engineer, Architect, Principal Engineer.

## Writing rules for story text

- A ticket is 2 to 4 sentences, in the character's voice, and names a concrete rider or station problem.
- Ticket text never teaches. Teaching happens in the KB page.
- Use plain words a 15-year-old knows. No company jargon without a one-line explanation.

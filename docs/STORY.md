# Story: Dhaaga & Co.

> Approved on 24 November 2026. It replaces the Spoke story, which is kept in `docs/archive/STORY-spoke.md` for Java modules 00 and 01. Platform support (per-track cast, ticket keys, ladders and the Shabaash name) arrives with the multi-track work; until then, the app still shows the Spoke names.

Cadence is told as one story, like a game. You join **Dhaaga & Co.**, a crochet business in Pune, as its first tech hire. *Dhaaga* means thread. It starts as one table at a Sunday market. Every ticket is a real problem the business has. You learn the idea, then fix the problem, and the business grows a step with every project you ship. By the end, Dhaaga is a national brand, and you're the engineer who built it.

The same world is shared by every track. Each track builds a different part of the same business:

| Track | What you build for Dhaaga |
|---|---|
| Software engineering (Java) | The billing, stock and orders system behind the counter, then its API and platform |
| Frontend (JavaScript + React) | The website: from a one-page visiting card to the full online store |
| Backend (later) | The orders API, payments and delivery services |
| Full stack (later) | The website and the API together |

## The business

**Meera Kulkarni** is a retired school teacher from Pune who has crocheted all her life. Her friends kept asking for her coasters and little amigurumi animals, so she set up a table at the **Sunday haat on FC Road**. People loved it. Now the orders come on WhatsApp faster than she can write them down. Payments arrive by UPI with no record of what was paid for, and the yarn stock lives in a notebook.

That's where you come in.

### How the business grows: the game arc

The business grows one stage at a time. Each stage is a project name in the app, so you can see it growing as you learn.

1. **The table:** one table at the Sunday haat, with cash and a UPI QR code.
2. **The stall:** a proper stall with a counter, a price board and a stock box.
3. **The WhatsApp catalogue:** orders and photos come in on WhatsApp, and deliveries go out across Pune.
4. **The shop:** a small shop in Aundh, open all week.
5. **The website:** dhaaga.in, the catalogue online.
6. **The online store:** a cart, UPI checkout and delivery anywhere in India.
7. **The Diwali rush:** ten times the orders in three weeks.
8. **Two cities:** a second shop in Bengaluru.
9. **The brand:** a national D2C brand with a warehouse and a team.

## Cast

The ids in `ticket.from` stay `maya`, `theo`, `priya` and `sam`, as they did when the cast last changed. Content, kudos and progress never change when names do. Only the names shown in the app change (`TEAM` in `dom.js`). Spoke tickets in modules 00 and 01 keep the Spoke names: the cast shown follows the course and module.

The learner can still drop their own pictures into `avatars/`; the file names follow the new names. A designer joins for the Frontend track, which needs one new id.

| Character | Role at Dhaaga | id | What they bring you |
|---|---|---|---|
| **Golu** | The team's helper: a crocheted elephant who sits on the counter and lives in team chat | (helper) | Explains, gives hints and cheers. Warm, patient, a little funny. Says "Shabaash!" when you get it right. Never blames. |
| **Kavya Iyer** (she) | Lead engineer and your manager | `maya` | Assigns most tickets and reviews your code. Hers is the solution you compare with. |
| **Arjun Mehta** (he) | Operations: deliveries, stock and keeping things running | `theo` | Raises incidents when something breaks, usually on a sale day. |
| **Meera Kulkarni** (she) | The founder, "Meera Aunty" | `priya` | Asks for features in plain words: what customers need, not how to build it. |
| **Raju Pawar** (he) | Runs the counter and customer support | `sam` | Forwards customer complaints that turn into bug tickets. |
| **Sana Shaikh** (she) | Designer (Frontend track) | `sana` (new) | Sends designs and screens to build, and notices when something looks or reads wrong. |

Characters speak only in tickets, chat and incidents. They never teach. The teaching stays plain text, as it is today.

## Facts every lesson and test agrees on

This section works like today's fare rules: every lesson, quiz and test uses these same facts.

- **Money is stored in paise, as whole numbers (`long` in Java, an integer in JavaScript):** ₹1 = 100 paise. It's shown as ₹ with two decimals, for example `₹1,299.00`. Lessons explain why there are no floating-point prices.
- **The price list:**

  | Code | Item | Price |
  |---|---|---|
  | `CST` | Coaster set (4) | ₹150 |
  | `KEY` | Keychain | ₹120 |
  | `AMI` | Amigurumi (small toy) | ₹450 |
  | `BAG` | Tote bag | ₹899 |
  | `BTY` | Baby booties | ₹350 |

- **Ids:**
  - Item ids look like `DC-1042`. `DC-1042` is an amigurumi elephant, the most famous one, modelled on Golu.
  - Order ids look like `order-17`.
  - Customer phone numbers are clearly fake, for example `98765 43210`.
- **Where Dhaaga sells:** the FC Road stall, the Aundh shop, the Koregaon Park pop-up (Diwali only), WhatsApp, and later the website.
- **Payment modes:** `CASH`, `UPI`, `CARD`.
- **Extras:**
  - Gift wrap: ₹30.
  - A name stitched on: ₹50.
  - Delivery inside Pune: ₹60, free above ₹999.
  - The Diwali offer: 10% off orders above ₹1,500.
- **Opening hours:**
  - The stall: Saturday and Sunday, 10:00–20:00.
  - The shop: Tuesday–Sunday, 11:00–21:00.
- **Yarn stock:** counted in grams per colour. The colours are Marigold, Indigo, Rani pink, Mint and Off-white. A small amigurumi uses about 40 g.

## Words used in the app

| Game idea | Word in the app |
|---|---|
| Topic | Ticket. Java course: `DHG-MMTT`; web course: `WEB-MMTT` |
| Module | Project, named after the business stage |
| Lesson | Knowledge base page ("KB") |
| Quiz | Onboarding check |
| Coding exercise | The fix, graded by checks (the tests) |
| Boss challenge | Incident, `INC-MM`; usually a sale day gone wrong |
| Mini-project | Feature, `FEAT-MM` |
| Level title | Job title, a ladder per track |
| Badge | **Shabaash** (instead of Kudos) |
| Hint | A message from Golu in chat |

## Arc by module: Software engineering (Java)

**Where the switch happens.** Modules 00 and 01 are written about Spoke, and the learner's code for them uses bikes and fares. They stay as they are: the learner's **first job**, an internship at Spoke. The story continues: Meera Aunty's niece saw the Spoke kiosk the learner built and showed it to her, and Dhaaga hires them. Nothing already finished becomes wrong, and from module 02 on, everything is Dhaaga.

| Module | Project (business stage) | Example tickets | Incident | Feature |
|---|---|---|---|---|
| 00–01 | Your Spoke internship (unchanged) | as today | as today | as today |
| 02 OOP | The stall: modelling the stall | `Item`, `Amigurumi` and `ToteBag` classes, `Customer`, an `Order` record, a `PaymentMode` enum | INC-02: tote bags billed at the coaster price | FEAT-02: a loyalty card (every 10th purchase free) |
| 03 Core Java | The WhatsApp catalogue: order data | lists of orders, a map of items, a yarn stock store, out-of-stock errors, importing WhatsApp order logs, order dates, streams for the weekly sales report | INC-03: the Diwali sales report counts orders twice | FEAT-03: sales analytics |
| 04 DSA | Deliveries across Pune | sorting orders, searching by date, the nearest pickup point, a route graph across Pune areas, the cheapest courier mix | INC-04: the delivery plan crosses the city twice | FEAT-04: the Diwali batch-delivery planner |
| 05 Advanced Java | The Diwali rush | two customers buy the last elephant, a thread pool for order messages, UPI payment and stock hold in parallel, order events as sealed types | INC-05: the last `DC-1042` is sold twice | FEAT-05: the live stock feed from both shops |
| 06 Craft | Paying off debt | cleaning up the pricing code, a strategy pattern for festival offers, tests, a mocked UPI gateway, logging | INC-06: refactor the old billing code safely | FEAT-06: a pluggable offers engine |
| 07 Tooling | Shipping safely | fixing the build, branching and merging, CI checks, a Docker image | INC-07: the release pipeline is red on sale day | FEAT-07: package the billing app as a runnable jar |
| 08 Backend | The Dhaaga API | HTTP, REST design, a Spring Boot orders API, validation, SQL, JPA, transactions for pay-and-reserve, customer login | INC-08: half-paid orders after a crash | FEAT-08: the stock availability API |
| 09 Architecture | Two cities | layers, ports and adapters, order events, caching stock, scaling, observability | INC-09: the store falls over at the Diwali sale peak | FEAT-09: event-driven invoices |
| 09a Platform and DevOps | Ship it everywhere | Docker, Compose, GitHub Actions, Kubernetes, Helm, Terraform, secrets, Prometheus | INC-11: the deploy that took checkout down | FEAT-11: one-click deploys |
| 09b Data in depth | Know your sales | advanced SQL, indexes, MVCC, NoSQL, Redis, replication, data pipelines, Postgres in production | INC-12: month-end reports grind the database | FEAT-12: the sales data mart |
| 10 Professional | Full-time offer | reviewing pull requests, a big codebase, docs, estimates, interviews | INC-10: the mock interview loop | Capstone: launch Dhaaga in Bengaluru |

## Arc by module: Frontend (JavaScript + React)

| Module | Project (business stage) | Example tickets | Incident | Feature |
|---|---|---|---|---|
| web-00 How the web works | A visiting card online | how a page reaches a phone, DevTools, VS Code, the first HTML page | INC-00: the ₹ sign shows as garbage on Raju's phone (character encoding) | FEAT-00: a one-page visiting card with the UPI QR |
| web-01 HTML | The catalogue page | page structure, product lists, an item page, the custom-order form, alt text for photos | INC-01: a blind customer can't place an order with a screen reader | FEAT-01: the custom-order request form |
| web-02 CSS | Making it ours | Sana's brand colours (Marigold and Indigo), the box model, a flexbox product grid, grid layout, phone-first design, CSS variables | INC-02: the catalogue breaks on small Android phones | FEAT-02: a festive Diwali theme |
| web-03 JavaScript basics | The price calculator | values, paise maths, free delivery above ₹999, loops over the cart, functions, arrays, objects, strings | INC-03: the cart total is off by a few paise (floating point) | FEAT-03: the price and delivery calculator |
| web-04 JavaScript in depth | The catalogue engine | scope and closures, a `Cart` class, modules, errors, `filter` by colour, destructuring, `catalogue.json` | INC-04: the Diwali discount is applied twice | FEAT-04: the catalogue filter engine |
| web-05 The DOM and events | A working cart | drawing the catalogue from JSON, add-to-cart buttons, form checks, event delegation, saving the cart | INC-05: the cart empties on refresh | FEAT-05: the cart drawer |
| web-06 Async JavaScript | Live stock | `fetch`, loading and error states, `async`/`await`, the PIN-code delivery check | INC-06: slow 3G shows the wrong stock (a race) | FEAT-06: live stock badges |
| web-07 Tooling | A real project | npm, Vite, ESLint and Prettier, Git and GitHub, debugging, Vitest | INC-07: the build breaks only on deploy | FEAT-07: a tested utilities package |
| react-08 React fundamentals | The store in React | components, JSX, props, state, events, lists and keys, forms | INC-08: removing one item removes another (keys) | FEAT-08: the React catalogue |
| react-09 React in depth | Checkout | effects, fetching data, a `useCart` hook, context, a reducer, React Router, performance | INC-09: a render loop floods the API during the sale | FEAT-09: checkout with a UPI step |
| react-10 Professional frontend | Launch day | testing with React Testing Library, accessibility, image performance, XSS in reviews, deploying, PR reviews | INC-10: the mock frontend interview loop | Capstone: launch the dhaaga.in online store |

## Career ladders

The ladder steps up every 5 levels, as now, with one ladder per track:

- **Software engineering (Java):** unchanged. Intern, Junior Developer, Developer, Software Engineer, Senior Engineer, Staff Engineer, Architect, Principal Engineer.
- **Frontend:** Frontend Intern, Junior Frontend Developer, Frontend Developer, Frontend Engineer, Senior Frontend Engineer, Staff Frontend Engineer, Frontend Architect, Principal Frontend Engineer.
- **Backend and Full stack (later):** the same steps, with "Backend" or "Full-Stack".

## Writing rules for story text

- A ticket is 2 to 4 sentences, in the character's voice, and names a concrete customer, item or day. For example: "Raju here. Mrs. Joshi paid ₹899 by UPI for a tote bag, but the receipt says ₹150."
- Ticket text never teaches. Teaching happens on the KB page.
- Use plain Indian English a 15-year-old knows. A Hindi or Marathi word is fine when everyone in India knows it (haat, bhaiya, shabaash, Diwali). Never use one in place of a technical term.
- Show money as ₹ in text and as paise in code.
- No stereotypes. Customers come from everywhere: students, grandparents, office workers, NRIs ordering gifts.
- Festivals stay light (the Diwali rush, Rakhi gifts). Nothing religious beyond the date on the calendar.

## Decisions

- **Names:** Dhaaga & Co., Pune, FC Road, Golu the crocheted elephant, Kavya, Arjun, Meera Aunty, Raju and Sana, as written above.
- **The switch:** modules 00 and 01 stay as your Spoke internship, unchanged. The story moves to Dhaaga from module 02.
- **Badges** are called **Shabaash** instead of Kudos. Kudos ids stay the same, so earned badges carry over.
- **Ticket keys:** `DHG-MMTT` for the Java course from module 02 on, and `WEB-MMTT` for the web course. Keys are display-only; topic ids never change.
- **Pictures:** the new cast shows initials until the learner adds new pictures to `avatars/`, named after the new cast.

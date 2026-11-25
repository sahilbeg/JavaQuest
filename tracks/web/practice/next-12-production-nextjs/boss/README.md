# INC-12: The mock Next.js interview loop

Arjun runs your mock loop. He hands you a previous candidate's answers to three rounds. Each one worked on the candidate's single quick try. Each one failed the round. Fix them the way an interviewer expects.

The catalogue (`src/lib/catalogue.js`, don't change it) has five items, in this order:

| id | name | paise | stock |
|---|---|---|---|
| DC-1042 | Golu the elephant | 45000 | 7 |
| DC-1101 | Marigold coaster set | 15000 | 12 |
| DC-1205 | Indigo keychain | 12000 | 0 |
| DC-1301 | Tote bag | 89900 | 2 |
| DC-1402 | Baby booties | 35000 | 4 |

Each item also has a `costPaise`, which customers must never see.

## Bronze: the API round (`src/app/api/search/route.js`, TODO 1)

`GET /api/search?q=<words>&max=<paise>` answers `{ results: [...] }`.

- `q` matches part of the item's name, **ignoring case** and **spaces around it**. No `q`, or an empty one, matches everything.
- `max` keeps items whose price is **at most** `max`. **No `max` means no limit.**
- A `max` that is there but isn't a whole number of paise (`"abc"`, `"12.5"`, `"-1"`, `""`) answers status **400** with `{ error: "max must be a whole number of paise" }`.
- Results go **cheapest first**, and each is only `{ id, name, paise }`.

The candidate's answer has five bugs: it matches case exactly, it doesn't trim, a missing `max` becomes `Number(null)`, which is 0, so nothing is found, a bad `max` quietly answers 200 with nothing, and the results are in catalogue order with every field, `costPaise` included.

## Silver: the server action round (`src/app/cart/actions.js`, TODOs 2–3)

`addToCart(prevState, formData)` is used with `useActionState`. The state is `{ lines: [{ id, qty }], error: "" }`, and the form sends `id` and `qty`.

- An unknown `id`: return `{ lines: prevState.lines, error: "Unknown item" }`.
- A `qty` that isn't a whole number from 1 to 5: return the old lines with `error: "Pick a quantity from 1 to 5"`.
- If the cart's quantity of that item plus `qty` is more than its stock: return the old lines with `error: "Only 7 left"` (the real stock number), or `"Sold out"` when the stock is 0.
- Otherwise add `qty`, **as a number**, to the item's existing line, or add a new line at the end. Return `{ lines, error: "" }`.
- Never change `prevState`: build new arrays and objects.

## Gold: the Next.js round (`src/app/page.jsx`, TODOs 4–6)

The home page lists the catalogue, **two items per page**, in catalogue order: page 1 has Golu and the coaster set, page 2 the keychain and the tote bag, page 3 the booties.

- `searchParams` is a **Promise**. Await it, then read `page`.
- No `page` means page 1. `page` exactly `"1"` calls `redirect("/")`, so page 1 has one address.
- Anything else that isn't a whole number from 1 to the last page calls `notFound()`: `"0"`, `"4"`, `"abc"`, `"2.5"`, or two values like `["2", "3"]`.
- Each item is a `<li>` with a link to `/items/<id>` named by the item's name.
- In the `<nav aria-label="Pages">`: a **Previous** link only after page 1 (page 2's goes to `/`, page 3's to `/?page=2`), the text **Page 2 of 3**, and a **Next** link only before the last page.
- `generateMetadata`: page 1 gives `{ title: "Catalogue", alternates: { canonical: "/" } }`; page 3 gives `{ title: "Catalogue, page 3", alternates: { canonical: "/?page=3" } }`; a bad page gives `{ title: "Page not found", robots: { index: false } }`.

The checks mock `next/navigation`: `notFound()` throws `NEXT_NOT_FOUND`, and `redirect("/")` throws `NEXT_REDIRECT /`.

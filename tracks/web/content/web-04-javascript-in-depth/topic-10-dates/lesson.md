## Hook

The Aundh shop has a little sign on the door that flips between Open and Closed. Raju flips it by hand. The website has to flip its own sign, and to do that it must know what day it is, what time it is, and the opening hours.

Dates look simple until you code them. Months start at 0, Sunday is day 0, and the clock keeps moving while you test. This lesson shows the traps, and how to step around them.

## Concept

### Syntax: making a Date

```js
const when = new Date(2026, 9, 31, 10, 30);
```

A `Date` is one moment in time. With numbers, the order is year, month, day, hours, minutes. This one is Saturday 31 October 2026 at 10:30, in the local time of the computer running it.

```callout jargon
A **Date** object holds one exact moment. Inside, it is a **timestamp**: the number of milliseconds since 1 January 1970, midnight UTC.
```

You can leave off the time: `new Date(2026, 10, 8)` is midnight at the start of 8 November.

### Months count from 0

The month number is an index, like an array index. January is 0, so October is 9 and December is 11. Days of the month start at 1, as normal.

```js
const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
// index:         0      1      2      3      4      5      6      7      8      9     10     11
```

```callout tip
When you write a date in code, say it out loud as "month index". `new Date(2026, 10, 8)` is month index 10, which is November.
```

### Syntax: reading the parts

```js
date.getFullYear(); date.getMonth(); date.getDate(); date.getDay(); date.getHours(); date.getMinutes();
```

Each `get` method reads one part, in local time. Two names are easy to mix up. `getDate()` is the day of the month, 1 to 31. `getDay()` is the day of the week, 0 to 6, and 0 is Sunday.

```js run
const diwali = new Date(2026, 10, 8, 18, 30);
const DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
console.log(diwali.getFullYear());
console.log(diwali.getMonth());
console.log(diwali.getDate());
console.log(DAYS[diwali.getDay()]);
console.log(diwali.getHours() + ":" + diwali.getMinutes());
```

It prints `2026`, `10` (November), `8`, `Sun` and `18:30`.

### Dates roll over by themselves

Give a `Date` a day that doesn't exist, and it rolls into the next month. That sounds odd, but it makes adding days easy. Watch 31 October become 1 November.

```animate
title: Day 32 of October is 1 November
kind: code
language: js
code: |
  const sat = new Date(2026, 9, 31, 10, 30);
  console.log(sat.getMonth(), sat.getDate(), sat.getDay());
  const next = new Date(2026, 9, 32, 10, 30);
  console.log(next.getMonth(), next.getDate(), next.getDay());
steps:
  - line: 1
    vars: { sat: "Sat 31 Oct 2026, 10:30" }
    say: Month index 9 is October. The 31st of October 2026 is a Saturday.
  - line: 2
    out: "9 31 6"
    say: Month 9, date 31, weekday 6. Weekday 6 means Saturday, because Sunday is 0.
  - line: 3
    vars: { next: "Sun 1 Nov 2026, 10:30" }
    say: October has no 32nd day. The Date rolls over to the next day, 1 November.
  - line: 4
    out: "10 1 0"
    say: Month 10 is November, date 1, weekday 0, a Sunday. Adding 1 to the day did the right thing.
```

### Syntax: adding days

```js
const later = new Date(date.getFullYear(), date.getMonth(), date.getDate() + days);
```

Build a new date from the old one's parts, with `days` added to the day of the month. Rolling over months and years is handled for you, even 31 December plus 1.

There is also `date.setDate(...)`, which changes a date in place. A `Date` is an object, so any other code holding the same date sees the change too. Making a new date is safer.

### Comparing dates

`<` and `>` compare two dates by their timestamps, so "is this before that?" just works. But `===` asks whether they are the same object, like any object.

```js run
const opens = new Date(2026, 10, 3, 11, 0);
const arrived = new Date(2026, 10, 3, 10, 45);
const sameTime = new Date(2026, 10, 3, 11, 0);
console.log(arrived < opens);
console.log(opens === sameTime);
console.log(opens.getTime() === sameTime.getTime());
console.log((opens - arrived) / 60000 + " minutes to wait");
```

It prints `true`, `false`, `true`, then `15 minutes to wait`. `getTime()` gives the timestamp as a plain number, so `===` works on it. Subtracting two dates gives the gap in milliseconds; 60000 milliseconds make a minute.

### Times of day as minutes

To check opening hours, turn a time into minutes after midnight. Then one comparison does the job, even for times like 10:59.

```js
const minutes = date.getHours() * 60 + date.getMinutes();
const open = minutes >= 11 * 60 && minutes < 21 * 60;
```

Note the `<` at the end. At 21:00 exactly, the shop has closed.

### Showing a date to people

Build the text yourself from the parts. `padStart(2, "0")` adds a leading zero, so 9:05 shows as `09:05`.

```js run
function timeLabel(date) {
  const hh = String(date.getHours()).padStart(2, "0");
  const mm = String(date.getMinutes()).padStart(2, "0");
  return hh + ":" + mm;
}
const MONTHS = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
const pickup = new Date(2026, 10, 8, 9, 5);
console.log(pickup.getDate() + " " + MONTHS[pickup.getMonth()] + " " + pickup.getFullYear() + ", " + timeLabel(pickup));
```

It prints `8 November 2026, 09:05`. Methods like `toLocaleDateString()` also exist, but their output depends on the computer's language settings, so it can differ between your laptop and a customer's phone.

### Never read the clock inside your logic

`new Date()` with no arguments means "right now". Code that calls it inside a check gives a different answer every time you run it, which makes it impossible to test.

```js
// Hard to test: the answer depends on when it runs.
function shopOpenNow() {
  return isShopOpen(new Date());
}
```

So write functions that take the date as a parameter, like `isShopOpen(date)`. Only the page asks the clock, and hands the answer in. A test can then pass in a Monday at noon whenever it likes.

```callout jargon
**Local time** is the time where the computer is, such as India Standard Time for Dhaaga. **UTC** is the world's reference time, which `toISOString()` shows. One moment has a different clock time in each time zone.
```

## Worked example

Say the Koregaon Park pop-up runs from 1 to 8 November 2026, from 16:00 to 22:00 each day. Here is a check for it.

```js run
const POPUP_FIRST = new Date(2026, 10, 1);
const POPUP_END = new Date(2026, 10, 9);

function isPopUpOpen(date) {
  const inDates = date >= POPUP_FIRST && date < POPUP_END;
  const minutes = date.getHours() * 60 + date.getMinutes();
  return inDates && minutes >= 16 * 60 && minutes < 22 * 60;
}

function label(date) {
  const mm = String(date.getMinutes()).padStart(2, "0");
  return date.getDate() + "/" + (date.getMonth() + 1) + " " + date.getHours() + ":" + mm;
}

const checks = [
  new Date(2026, 9, 31, 18, 0),
  new Date(2026, 10, 1, 15, 59),
  new Date(2026, 10, 1, 16, 0),
  new Date(2026, 10, 8, 21, 59),
  new Date(2026, 10, 9, 18, 0),
];
for (const when of checks) {
  console.log(label(when) + " " + (isPopUpOpen(when) ? "open" : "closed"));
}
```

Line by line:

1. `POPUP_END` is midnight at the start of 9 November, the moment after the last day ends. `< POPUP_END` includes all of 8 November.
2. `isPopUpOpen` takes the date as a parameter. It never reads the clock, so every check below gives the same answer every time.
3. The time becomes minutes after midnight, and is compared with 16:00 and 22:00.
4. `label` adds 1 to the month index, because people count months from 1.
5. It prints `31/10 18:00 closed`, `1/11 15:59 closed`, `1/11 16:00 open`, `8/11 21:59 open` and `9/11 18:00 closed`.

## Try it

Add a check for 8 November at 22:00 and predict the answer. Then change the pop-up to also close on 4 November, a Wednesday, using `getDay()`.

```js run
const DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const when = new Date(2026, 10, 4, 17, 0);
console.log(DAYS[when.getDay()] + " " + when.getDate());
```

## Recap

- `new Date(year, monthIndex, day, hours, minutes)` makes a local date. Month indexes start at 0, so October is 9.
- `getDate()` is the day of the month. `getDay()` is the weekday, with Sunday as 0.
- Dates roll over: day 32 of October is 1 November. Build a new date with `getDate() + days` to add days safely.
- Compare dates with `<` and `>`, or compare `getTime()` values. `===` only checks for the same object.
- Pass dates into your functions instead of calling `new Date()` inside them, so they can be tested.

## Common mistakes

**1. Treating the month as a normal number.**

```js run
const wanted = new Date(2026, 10, 31);
console.log(wanted.getMonth(), wanted.getDate());
```

It prints `11 1`: 1 December. Month index 10 is November, and November has no 31st, so it rolled over. Fix: 31 October is `new Date(2026, 9, 31)`.

**2. Mixing up getDay and getDate.**

```js run
const sat = new Date(2026, 9, 31);
console.log(sat.getDay(), sat.getDate());
```

It prints `6 31`. `getDay()` is the weekday (6, Saturday), and `getDate()` is the day of the month. Fix: use `getDay()` for opening days, and `getDate()` for the calendar date.

**3. Changing a date that someone else is holding.**

```js run
const orderDay = new Date(2026, 10, 6, 12, 0);
const deliveryDay = orderDay;
deliveryDay.setDate(deliveryDay.getDate() + 3);
console.log(orderDay.getDate());
```

It prints `9`. Both names point to one `Date`, and `setDate` changed it, so the order date moved too. Fix: build a new date for the delivery, `new Date(orderDay.getFullYear(), orderDay.getMonth(), orderDay.getDate() + 3)`.

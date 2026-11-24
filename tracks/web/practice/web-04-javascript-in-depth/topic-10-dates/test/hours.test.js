import { describe, it, expect } from "vitest";
import { isShopOpen, isStallOpen, addDays, formatDay } from "../src/hours.js";

// Every date is built in local time with an explicit month index (9 is October, 10 is November).
// Fri 30 Oct, Sat 31 Oct, Sun 1 Nov, Mon 2 Nov, Tue 3 Nov, Wed 4 Nov 2026.
const at = (month, day, hours, minutes) => new Date(2026, month, day, hours, minutes);

describe("isShopOpen", () => {
  it("is closed all day on Monday", () => {
    expect(isShopOpen(at(10, 2, 12, 0)), "Monday 2 November at noon: the shop is closed on Mondays").toBe(false);
    expect(isShopOpen(at(10, 2, 11, 0)), "Monday 2 November at 11:00: still closed").toBe(false);
  });

  it("opens at 11:00 on Tuesday", () => {
    expect(isShopOpen(at(10, 3, 10, 59)), "Tuesday at 10:59 is one minute too early").toBe(false);
    expect(isShopOpen(at(10, 3, 11, 0)), "Tuesday at 11:00 the shop opens").toBe(true);
  });

  it("is open in the middle of the day from Tuesday to Sunday", () => {
    expect(isShopOpen(at(10, 4, 15, 30)), "Wednesday at 15:30 is open").toBe(true);
    expect(isShopOpen(at(9, 31, 12, 0)), "Saturday 31 October at noon is open").toBe(true);
    expect(isShopOpen(at(10, 1, 20, 59)), "Sunday at 20:59 is still open").toBe(true);
  });

  it("closes at 21:00", () => {
    expect(isShopOpen(at(10, 1, 21, 0)), "Sunday at 21:00 the shop has closed").toBe(false);
    expect(isShopOpen(at(10, 1, 21, 15)), "Sunday at 21:15 is closed (Raju's complaint)").toBe(false);
  });
});

describe("isStallOpen", () => {
  it("is open on Saturday and Sunday from 10:00", () => {
    expect(isStallOpen(at(9, 31, 10, 0)), "Saturday 31 October at 10:00 the stall opens").toBe(true);
    expect(isStallOpen(at(10, 1, 12, 0)), "Sunday 1 November at noon is open").toBe(true);
    expect(isStallOpen(at(9, 31, 19, 59)), "Saturday at 19:59 is still open").toBe(true);
  });

  it("is closed before 10:00 and from 20:00", () => {
    expect(isStallOpen(at(9, 31, 9, 59)), "Saturday at 9:59 is too early").toBe(false);
    expect(isStallOpen(at(10, 1, 20, 0)), "Sunday at 20:00 the stall has closed").toBe(false);
  });

  it("is closed on weekdays", () => {
    expect(isStallOpen(at(9, 30, 12, 0)), "Friday 30 October: the stall only opens at weekends").toBe(false);
    expect(isStallOpen(at(10, 3, 12, 0)), "Tuesday 3 November: closed").toBe(false);
  });
});

describe("addDays", () => {
  it("moves into the next month when it has to", () => {
    const next = addDays(at(9, 31, 10, 30), 1);
    expect(next.getMonth(), "one day after 31 October is in November (month index 10)").toBe(10);
    expect(next.getDate(), "one day after 31 October is the 1st").toBe(1);
    expect(next.getHours() * 60 + next.getMinutes(), "the time of day should stay 10:30").toBe(630);
  });

  it("moves into the next year when it has to", () => {
    const next = addDays(new Date(2026, 11, 31, 18, 0), 1);
    expect(next.getFullYear(), "one day after 31 December 2026 is in 2027").toBe(2027);
    expect(next.getMonth(), "it should be January, month index 0").toBe(0);
    expect(next.getDate(), "it should be the 1st").toBe(1);
  });

  it("goes back in time with a negative number", () => {
    const before = addDays(at(10, 1, 11, 0), -1);
    expect(before.getMonth() + "/" + before.getDate(), "one day before 1 November is 31 October").toBe("9/31");
  });

  it("returns a new date and leaves the old one alone", () => {
    const saturday = at(9, 31, 10, 30);
    const week = addDays(saturday, 7);
    expect(week === saturday, "addDays should return a new Date object").toBe(false);
    expect(saturday.getDate(), "the date passed in must still be the 31st").toBe(31);
    expect(week.getMonth() + "/" + week.getDate(), "a week after 31 October is 7 November").toBe("10/7");
  });
});

describe("formatDay", () => {
  it("shows the short weekday, the date and the short month", () => {
    expect(formatDay(at(9, 31, 10, 30)), "31 October 2026 is a Saturday").toBe("Sat 31 Oct");
    expect(formatDay(at(10, 2, 9, 0)), "2 November 2026 is a Monday").toBe("Mon 2 Nov");
    expect(formatDay(at(10, 8, 18, 0)), "Diwali, 8 November 2026, is a Sunday").toBe("Sun 8 Nov");
  });

  it("works for January", () => {
    expect(formatDay(new Date(2027, 0, 1, 12, 0)), "1 January 2027 is a Friday").toBe("Fri 1 Jan");
  });
});

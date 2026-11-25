import { describe, it, expect, beforeEach, vi } from "vitest";
import { createStarRating } from "../src/starRating.js";

let root;
let onChange;

beforeEach(() => {
  document.body.innerHTML = `<div id="rating"></div>`;
  root = document.querySelector("#rating");
  onChange = vi.fn();
});

const make = (value = 0) => createStarRating(root, { label: "Rate Golu the elephant", value, onChange });
const stars = () => [...root.querySelectorAll(".star")];
const star = (n) => stars()[n - 1];
const checked = () => stars().map((s) => s.getAttribute("aria-checked"));
const tabStops = () => stars().map((s) => s.getAttribute("tabindex"));

/** Presses a key on whatever has focus, like a keyboard does. */
function press(key) {
  const event = new KeyboardEvent("keydown", { key, bubbles: true, cancelable: true });
  (document.activeElement ?? document.body).dispatchEvent(event);
  return event;
}

describe("Silver: the star rating", () => {
  it("is a labelled radio group of five named stars", () => {
    make();
    expect(root.getAttribute("role"), "the container should have role=\"radiogroup\"").toBe("radiogroup");
    expect(root.getAttribute("aria-label"), "the group should be named by its label").toBe("Rate Golu the elephant");
    expect(stars().map((s) => s.getAttribute("role")), "each star should have role=\"radio\"").toEqual(["radio", "radio", "radio", "radio", "radio"]);
    expect(stars().map((s) => s.getAttribute("aria-label")), "each star needs a name a screen reader can read").toEqual(["1 star", "2 stars", "3 stars", "4 stars", "5 stars"]);
  });

  it("checks only the chosen star", () => {
    make(3);
    expect(checked(), "with 3 chosen, only the third star should have aria-checked=\"true\"").toEqual(["false", "false", "true", "false", "false"]);
  });

  it("has exactly one Tab stop, on the chosen star", () => {
    make(3);
    expect(tabStops(), "the chosen star should have tabindex 0 and every other star -1").toEqual(["-1", "-1", "0", "-1", "-1"]);
  });

  it("puts the Tab stop on the first star when nothing is chosen", () => {
    make();
    expect(tabStops(), "with no rating yet, Tab should land on the first star").toEqual(["0", "-1", "-1", "-1", "-1"]);
    expect(checked(), "with no rating yet, no star is checked").toEqual(["false", "false", "false", "false", "false"]);
  });

  it("moves the check and the Tab stop when a star is clicked", () => {
    make();
    star(4).click();
    expect(checked(), "after clicking the fourth star, only it should be checked").toEqual(["false", "false", "false", "true", "false"]);
    expect(tabStops(), "the Tab stop should move to the fourth star").toEqual(["-1", "-1", "-1", "0", "-1"]);
    expect(onChange.mock.calls, "onChange should be told 4, once").toEqual([[4]]);
  });

  it("ArrowRight chooses and focuses the next star", () => {
    const rating = make(2);
    star(2).focus();
    const event = press("ArrowRight");
    expect(rating.getValue(), "ArrowRight from 2 stars should choose 3").toBe(3);
    expect(document.activeElement === star(3), "focus should move to the third star with the choice").toBe(true);
    expect(checked()[2], "the third star should now be checked").toBe("true");
    expect(event.defaultPrevented, "the arrow key should not also scroll the page: call preventDefault").toBe(true);
  });

  it("ArrowLeft goes back, and both arrows wrap around the ends", () => {
    const rating = make(1);
    star(1).focus();
    press("ArrowLeft");
    expect(rating.getValue(), "ArrowLeft from 1 star should wrap to 5").toBe(5);
    expect(document.activeElement === star(5), "focus should wrap to the fifth star").toBe(true);
    press("ArrowRight");
    expect(rating.getValue(), "ArrowRight from 5 stars should wrap to 1").toBe(1);
    expect(document.activeElement === star(1), "focus should wrap back to the first star").toBe(true);
  });

  it("ArrowDown and ArrowUp work like Right and Left, as in any radio group", () => {
    const rating = make(3);
    star(3).focus();
    press("ArrowDown");
    expect(rating.getValue(), "ArrowDown from 3 should choose 4, like ArrowRight").toBe(4);
    press("ArrowUp");
    expect(rating.getValue(), "ArrowUp from 4 should choose 3, like ArrowLeft").toBe(3);
  });

  it("Space chooses the focused star when nothing is chosen yet", () => {
    const rating = make();
    star(1).focus();
    press(" ");
    expect(rating.getValue(), "Space on the first star should choose 1").toBe(1);
    expect(onChange.mock.calls, "onChange should be told 1").toEqual([[1]]);
  });

  it("tells the page once per change, and ignores keys that change nothing", () => {
    make(2);
    star(2).focus();
    press(" ");
    press("a");
    expect(onChange.mock.calls.length, "Space on the star that's already chosen, and the letter a, should not call onChange").toBe(0);
    press("ArrowRight");
    expect(onChange.mock.calls, "ArrowRight should call onChange once, with 3").toEqual([[3]]);
  });
});

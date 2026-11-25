import { describe, it, expect, beforeEach, vi } from "vitest";
import { matchNames, statusMessage, createSuggestions } from "../src/suggest.js";

const NAMES = [
  "Golu the elephant",
  "Marigold coaster set",
  "Indigo coaster set",
  "Mint keychain",
  "Rani pink tote bag",
  "Off-white baby booties",
];

let input;
let listbox;
let status;
let onPick;

beforeEach(() => {
  document.body.innerHTML = `
    <label for="search">Search the shop</label>
    <input id="search" type="text" role="combobox" aria-autocomplete="list" aria-expanded="false" aria-controls="suggestions" autocomplete="off">
    <ul id="suggestions" role="listbox" aria-label="Suggestions" hidden></ul>
    <p id="search-status" role="status"></p>`;
  input = document.querySelector("#search");
  listbox = document.querySelector("#suggestions");
  status = document.querySelector("#search-status");
  onPick = vi.fn();
  createSuggestions({ input, listbox, status, names: NAMES, onPick });
  input.focus();
});

function type(text) {
  input.value = text;
  input.dispatchEvent(new Event("input", { bubbles: true }));
}

function press(key) {
  const event = new KeyboardEvent("keydown", { key, bubbles: true, cancelable: true });
  input.dispatchEvent(event);
  return event;
}

const options = () => [...listbox.querySelectorAll('[role="option"]')];
const optionTexts = () => options().map((option) => option.textContent);
const active = () => input.getAttribute("aria-activedescendant");

describe("matchNames", () => {
  it("finds names that contain the words, whatever the capitals", () => {
    expect(matchNames(NAMES, "COASTER"), "COASTER should match both coaster sets").toEqual(["Marigold coaster set", "Indigo coaster set"]);
  });

  it("ignores spaces around the words", () => {
    expect(matchNames(NAMES, "  golu "), "'  golu ' should match Golu the elephant").toEqual(["Golu the elephant"]);
  });

  it("suggests nothing for a blank box", () => {
    expect(matchNames(NAMES, "   "), "only spaces should give no suggestions, not every name").toEqual([]);
  });
});

describe("statusMessage", () => {
  it("says nothing for a blank box", () => {
    expect(statusMessage("  ", 0), "a blank box should give an empty status").toBe("");
  });

  it("counts the suggestions in words", () => {
    expect(statusMessage("coaster", 2), "two matches should say 2 suggestions").toBe("2 suggestions");
    expect(statusMessage("golu", 1), "one match should say 1 suggestion, not 1 suggestions").toBe("1 suggestion");
    expect(statusMessage("zebra", 0), "typed words with no matches should say No suggestions").toBe("No suggestions");
  });
});

describe("typing in the search box", () => {
  it("opens the list with one option per match", () => {
    type("coaster");
    expect(listbox.hidden, "the listbox should show when there are matches").toBe(false);
    expect(input.getAttribute("aria-expanded"), "the input's aria-expanded should be \"true\" when the list is open").toBe("true");
    expect(optionTexts(), "the options should be the two coaster sets").toEqual(["Marigold coaster set", "Indigo coaster set"]);
    expect(options().map((option) => option.id), "options should have the ids suggestions-0 and suggestions-1").toEqual(["suggestions-0", "suggestions-1"]);
    expect(status.textContent, "the status line should announce 2 suggestions").toBe("2 suggestions");
  });

  it("starts with no option active", () => {
    type("coaster");
    expect(active(), "right after typing, the input should have no aria-activedescendant").toBeNull();
    expect(options().map((option) => option.getAttribute("aria-selected")), "no option should be selected yet").toEqual(["false", "false"]);
  });

  it("closes the list when nothing matches", () => {
    type("coaster");
    type("zebra");
    expect(listbox.hidden, "the listbox should hide when nothing matches").toBe(true);
    expect(input.getAttribute("aria-expanded"), "aria-expanded should go back to \"false\"").toBe("false");
    expect(options().length, "there should be no options left").toBe(0);
    expect(status.textContent, "the status line should say No suggestions").toBe("No suggestions");
  });
});

describe("the arrow keys", () => {
  it("ArrowDown makes the first option active, with focus kept in the input", () => {
    type("coaster");
    press("ArrowDown");
    expect(active(), "aria-activedescendant should point at suggestions-0").toBe("suggestions-0");
    expect(options().map((option) => option.getAttribute("aria-selected")), "only the first option should be selected").toEqual(["true", "false"]);
    expect(document.activeElement === input, "focus must stay in the search box, so the customer can keep typing").toBe(true);
  });

  it("ArrowDown moves on, and wraps from the last option to the first", () => {
    type("coaster");
    press("ArrowDown");
    press("ArrowDown");
    expect(active(), "the second ArrowDown should make suggestions-1 active").toBe("suggestions-1");
    press("ArrowDown");
    expect(active(), "ArrowDown on the last option should wrap to suggestions-0").toBe("suggestions-0");
  });

  it("ArrowUp from nothing goes to the last option, then up", () => {
    type("coaster");
    press("ArrowUp");
    expect(active(), "ArrowUp with nothing active should go to the last option, suggestions-1").toBe("suggestions-1");
    press("ArrowUp");
    expect(active(), "the next ArrowUp should go to suggestions-0").toBe("suggestions-0");
  });

  it("stops the arrows from moving the text cursor while the list is open", () => {
    type("coaster");
    expect(press("ArrowDown").defaultPrevented, "ArrowDown should call event.preventDefault() when the list is open").toBe(true);
    expect(press("ArrowUp").defaultPrevented, "ArrowUp should call event.preventDefault() when the list is open").toBe(true);
  });

  it("does nothing when the list is closed", () => {
    press("ArrowDown");
    expect(active(), "with no suggestions, ArrowDown should not make anything active").toBeNull();
    expect(listbox.hidden, "with no suggestions, the list should stay hidden").toBe(true);
  });
});

describe("choosing and closing", () => {
  it("Enter picks the active option", () => {
    type("coaster");
    press("ArrowDown");
    press("ArrowDown");
    press("Enter");
    expect(input.value, "the search box should now hold the picked name").toBe("Indigo coaster set");
    expect(onPick.mock.calls, "onPick should be called once, with Indigo coaster set").toEqual([["Indigo coaster set"]]);
    expect(listbox.hidden, "picking should close the list").toBe(true);
    expect(input.getAttribute("aria-expanded"), "aria-expanded should be \"false\" after picking").toBe("false");
    expect(active(), "nothing should be active after picking").toBeNull();
  });

  it("Enter with no active option picks nothing", () => {
    type("coaster");
    press("Enter");
    expect(onPick.mock.calls.length, "Enter with no active option should not call onPick").toBe(0);
    expect(listbox.hidden, "the list should stay open").toBe(false);
  });

  it("Escape closes the list and keeps what was typed", () => {
    type("golu");
    press("ArrowDown");
    press("Escape");
    expect(listbox.hidden, "Escape should close the list").toBe(true);
    expect(input.getAttribute("aria-expanded"), "aria-expanded should be \"false\" after Escape").toBe("false");
    expect(active(), "aria-activedescendant should be removed after Escape").toBeNull();
    expect(input.value, "Escape should keep the customer's typing").toBe("golu");
    expect(onPick.mock.calls.length, "Escape should not pick anything").toBe(0);
  });

  it("a click on an option picks it", () => {
    type("mint");
    options()[0]?.click();
    expect(input.value, "clicking Mint keychain should put it in the search box").toBe("Mint keychain");
    expect(onPick.mock.calls, "onPick should be called with Mint keychain").toEqual([["Mint keychain"]]);
  });
});

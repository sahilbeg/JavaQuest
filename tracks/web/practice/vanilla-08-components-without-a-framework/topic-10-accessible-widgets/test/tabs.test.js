import { describe, it, expect, beforeEach } from "vitest";
import { setupTabs } from "../src/tabs.js";

beforeEach(() => {
  document.body.innerHTML = `
    <div class="tabs" id="item-tabs">
      <div class="tabs__list" aria-label="About Golu the elephant">
        <button type="button" id="tab-details" data-panel="panel-details">Details</button>
        <button type="button" id="tab-care" data-panel="panel-care">Care</button>
        <button type="button" id="tab-delivery" data-panel="panel-delivery">Delivery</button>
      </div>
      <section id="panel-details"><p>Crocheted in Indigo, about 15 cm tall.</p></section>
      <section id="panel-care"><p>Hand wash in cold water.</p></section>
      <section id="panel-delivery"><p>₹60 inside Pune, free above ₹999.</p></section>
    </div>`;
  setupTabs(document.querySelector("#item-tabs"));
});

const NAMES = ["details", "care", "delivery"];
const tab = (name) => document.querySelector(`#tab-${name}`);
const panel = (name) => document.querySelector(`#panel-${name}`);
const selectedTabs = () => NAMES.filter((name) => tab(name).getAttribute("aria-selected") === "true");
const visiblePanels = () => NAMES.filter((name) => !panel(name).hidden);
const tabindexes = () => NAMES.map((name) => tab(name).getAttribute("tabindex"));

/** Presses a key on whatever has focus, like a keyboard would. Returns false when preventDefault was called. */
function press(key) {
  return document.activeElement.dispatchEvent(new KeyboardEvent("keydown", { key, bubbles: true, cancelable: true }));
}

describe("setupTabs", () => {
  it("gives the list, the tabs and the panels their roles", () => {
    expect(document.querySelector(".tabs__list").getAttribute("role"), ".tabs__list should have role=\"tablist\"").toBe("tablist");
    expect(NAMES.map((name) => tab(name).getAttribute("role")), "every tab button should have role=\"tab\"").toEqual(["tab", "tab", "tab"]);
    expect(NAMES.map((name) => panel(name).getAttribute("role")), "every panel should have role=\"tabpanel\"").toEqual(["tabpanel", "tabpanel", "tabpanel"]);
  });

  it("links each tab to its panel and back", () => {
    expect(tab("care").getAttribute("aria-controls"), "the Care tab's aria-controls should be panel-care").toBe("panel-care");
    expect(panel("care").getAttribute("aria-labelledby"), "the Care panel's aria-labelledby should be tab-care").toBe("tab-care");
    expect(panel("delivery").getAttribute("tabindex"), "every panel should have tabindex=\"0\"").toBe("0");
  });

  it("starts on the first tab", () => {
    expect(NAMES.map((name) => tab(name).getAttribute("aria-selected")), "aria-selected should be \"true\" on Details and \"false\" on the others").toEqual(["true", "false", "false"]);
    expect(visiblePanels(), "only the Details panel should be shown").toEqual(["details"]);
  });

  it("keeps only the selected tab in the Tab order", () => {
    expect(tabindexes(), "the selected tab should have tabindex 0 and the others -1").toEqual(["0", "-1", "-1"]);
  });

  it("selects a tab when it is clicked", () => {
    tab("care").click();
    expect(selectedTabs(), "after a click on Care, only Care should be selected").toEqual(["care"]);
    expect(visiblePanels(), "after a click on Care, only the Care panel should show").toEqual(["care"]);
    expect(tabindexes(), "tabindex 0 should move to Care").toEqual(["-1", "0", "-1"]);
  });

  it("moves right with the right arrow, selecting and focusing", () => {
    tab("details").focus();
    press("ArrowRight");
    expect(document.activeElement, "the right arrow on Details should move focus to Care").toBe(tab("care"));
    expect(selectedTabs(), "Care should now be selected").toEqual(["care"]);
    expect(visiblePanels(), "the Care panel should now show").toEqual(["care"]);
  });

  it("wraps round at both ends", () => {
    tab("delivery").click();
    tab("delivery").focus();
    press("ArrowRight");
    expect(document.activeElement, "the right arrow on the last tab should wrap to Details").toBe(tab("details"));
    press("ArrowLeft");
    expect(document.activeElement, "the left arrow on the first tab should wrap to Delivery").toBe(tab("delivery"));
    expect(selectedTabs(), "Delivery should be selected after wrapping back").toEqual(["delivery"]);
  });

  it("jumps with Home and End", () => {
    tab("details").focus();
    press("End");
    expect(document.activeElement, "End should move focus to the last tab, Delivery").toBe(tab("delivery"));
    press("Home");
    expect(document.activeElement, "Home should move focus to the first tab, Details").toBe(tab("details"));
    expect(selectedTabs(), "Details should be selected after Home").toEqual(["details"]);
  });

  it("stops the page scrolling for the keys it handles, and ignores the rest", () => {
    tab("details").focus();
    expect(press("ArrowRight"), "the right arrow should call preventDefault").toBe(false);
    expect(press("a"), "the key a should not call preventDefault").toBe(true);
    expect(selectedTabs(), "the key a should change nothing").toEqual(["care"]);
    expect(document.activeElement, "the key a should leave focus where it was").toBe(tab("care"));
  });
});

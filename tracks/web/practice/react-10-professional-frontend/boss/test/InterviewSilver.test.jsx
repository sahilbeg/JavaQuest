import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Tabs } from "../src/Tabs.jsx";

afterEach(cleanup);

const TABS = [
  { id: "description", label: "Description", content: "Golu is crocheted in Indigo cotton." },
  { id: "care", label: "Care", content: "Hand wash in cold water." },
  { id: "delivery", label: "Delivery", content: "₹60 inside Pune, free above ₹999." },
];

function setup() {
  const user = userEvent.setup();
  render(<Tabs label="About Golu" tabs={TABS} />);
  const tabs = screen.queryAllByRole("tab");
  expect(tabs.length, "there should be three elements with role=\"tab\", one per tab (TODO 2)").toBe(3);
  return { user, tabs };
}

function selectedNames(tabs) {
  return tabs.filter((tab) => tab.getAttribute("aria-selected") === "true").map((tab) => tab.textContent);
}

function visiblePanel(name) {
  const panels = screen.queryAllByRole("tabpanel");
  expect(panels.length, "exactly one tab panel should be visible; hide the others with the hidden attribute (TODO 3)").toBe(1);
  const panel = screen.queryByRole("tabpanel", { name });
  expect(panel, `the visible panel should be named by its tab, "${name}", through aria-labelledby (TODO 3)`).not.toBeNull();
  return panel;
}

describe("Round 2: Tabs", () => {
  it("puts the tabs in a tab list named by the label prop", () => {
    setup();
    expect(screen.queryByRole("tablist", { name: "About Golu" }), "the tabs should sit in a role=\"tablist\" with aria-label=\"About Golu\" (TODO 2)").not.toBeNull();
  });

  it("selects the first tab and shows only its panel", () => {
    const { tabs } = setup();
    expect(selectedNames(tabs), "only Description should have aria-selected=\"true\" at the start").toEqual(["Description"]);
    expect(visiblePanel("Description").textContent, "the Description panel should show Golu's description").toBe("Golu is crocheted in Indigo cotton.");
  });

  it("switches tab and panel on a click", async () => {
    const { user, tabs } = setup();
    await user.click(tabs[1]);
    expect(selectedNames(tabs), "after clicking Care, only Care should be selected").toEqual(["Care"]);
    expect(visiblePanel("Care").textContent, "the Care panel should show the care instructions").toBe("Hand wash in cold water.");
  });

  it("lets Tab stop only on the selected tab, then go to its panel", async () => {
    const { user, tabs } = setup();
    await user.tab();
    expect(document.activeElement === tabs[0], "the first Tab should land on the selected tab, Description").toBe(true);
    await user.tab();
    expect(document.activeElement === screen.queryByRole("tabpanel", { name: "Description" }), "the next Tab should skip the other tabs and land on the panel: roving tabIndex, 0 on the selected tab and -1 on the rest, and tabIndex 0 on the panel").toBe(true);
  });

  it("moves with the arrow keys, wrapping round at both ends", async () => {
    const { user, tabs } = setup();
    await user.tab();
    await user.keyboard("{ArrowRight}");
    expect(selectedNames(tabs), "ArrowRight from Description should select Care (TODO 4)").toEqual(["Care"]);
    expect(document.activeElement === tabs[1], "ArrowRight should also move focus to Care").toBe(true);
    await user.keyboard("{ArrowRight}");
    await user.keyboard("{ArrowRight}");
    expect(selectedNames(tabs), "ArrowRight from the last tab should wrap round to Description").toEqual(["Description"]);
    await user.keyboard("{ArrowLeft}");
    expect(selectedNames(tabs), "ArrowLeft from the first tab should wrap round to Delivery").toEqual(["Delivery"]);
    expect(document.activeElement === tabs[2], "focus should follow the selection to Delivery").toBe(true);
    expect(visiblePanel("Delivery").textContent, "the Delivery panel should be the one showing").toBe("₹60 inside Pune, free above ₹999.");
  });

  it("jumps to the last and first tab with End and Home", async () => {
    const { user, tabs } = setup();
    await user.tab();
    await user.keyboard("{End}");
    expect(selectedNames(tabs), "End should select the last tab, Delivery (TODO 4)").toEqual(["Delivery"]);
    expect(document.activeElement === tabs[2], "End should move focus to Delivery").toBe(true);
    await user.keyboard("{Home}");
    expect(selectedNames(tabs), "Home should select the first tab, Description").toEqual(["Description"]);
    expect(document.activeElement === tabs[0], "Home should move focus to Description").toBe(true);
  });
});

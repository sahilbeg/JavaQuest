import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Disclosure } from "../src/Disclosure.jsx";

afterEach(cleanup);

function renderHelp() {
  render(
    <Disclosure title="Delivery and returns">
      <p>Delivery inside Pune costs ₹60. It's free above ₹999.</p>
    </Disclosure>
  );
}

function titleButton() {
  const button = screen.queryByRole("button", { name: "Delivery and returns" });
  expect(button, "the title should be a button named Delivery and returns (TODO 1)").not.toBeNull();
  return button;
}

function panelOf(button) {
  const id = button.getAttribute("aria-controls");
  expect(Boolean(id), "the button needs aria-controls, set to the panel's id (TODO 1)").toBe(true);
  const panel = document.getElementById(id);
  expect(panel, `aria-controls points at "${id}", but nothing has that id. Always render the panel with that id (TODO 2)`).not.toBeNull();
  return panel;
}

describe("Disclosure", () => {
  it("makes the title a real button", () => {
    renderHelp();
    const button = titleButton();
    expect(button.tagName, "use a real <button>: it gets focus, Enter and Space for free").toBe("BUTTON");
  });

  it("starts closed, with a hidden panel that aria-controls points to", () => {
    renderHelp();
    const button = titleButton();
    expect(button.getAttribute("aria-expanded"), "a closed disclosure has aria-expanded=\"false\"").toBe("false");
    const panel = panelOf(button);
    expect(panel.hidden, "the closed panel should have the hidden attribute").toBe(true);
  });

  it("opens and closes when clicked", async () => {
    const user = userEvent.setup();
    renderHelp();
    const button = titleButton();
    await user.click(button);
    expect(button.getAttribute("aria-expanded"), "after one click, aria-expanded should be \"true\"").toBe("true");
    const panel = panelOf(button);
    expect(panel.hidden, "the open panel should not be hidden").toBe(false);
    expect(panel.textContent.includes("Delivery inside Pune costs ₹60."), "the open panel should show the answer").toBe(true);
    await user.click(button);
    expect(button.getAttribute("aria-expanded"), "a second click should close it again: aria-expanded=\"false\"").toBe("false");
    expect(panel.hidden, "after the second click the panel should be hidden again").toBe(true);
  });

  it("works with only a keyboard: Tab, then Enter or Space", async () => {
    const user = userEvent.setup();
    renderHelp();
    const button = titleButton();
    await user.tab();
    expect(document.activeElement === button, "pressing Tab should move focus to the title button").toBe(true);
    await user.keyboard("{Enter}");
    expect(button.getAttribute("aria-expanded"), "Enter on the focused title should open it").toBe("true");
    await user.keyboard(" ");
    expect(button.getAttribute("aria-expanded"), "Space on the focused title should close it").toBe("false");
  });
});

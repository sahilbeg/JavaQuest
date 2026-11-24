import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { RemoveItemButton } from "../src/RemoveItemButton.jsx";

afterEach(cleanup);

function setup() {
  const user = userEvent.setup();
  const onRemove = vi.fn();
  render(<RemoveItemButton itemName="Golu the elephant" onRemove={onRemove} />);
  return { user, onRemove };
}

function opener() {
  const button = screen.queryByRole("button", { name: "Remove Golu the elephant" });
  expect(button, "the Remove button should be named \"Remove Golu the elephant\", so a screen reader says which item it removes (TODO 5)").not.toBeNull();
  return button;
}

function dialog() {
  return screen.queryByRole("dialog", { name: "Remove Golu the elephant?" });
}

async function openDialog(user) {
  const button = opener();
  await user.click(button);
  expect(dialog(), "pressing Remove should open a role=\"dialog\" named by its heading, \"Remove Golu the elephant?\" (TODO 6)").not.toBeNull();
  return button;
}

describe("RemoveItemButton", () => {
  it("names the Remove button after the item, and shows no dialog yet", () => {
    setup();
    opener();
    expect(screen.queryByRole("dialog"), "no dialog should show before you press Remove").toBeNull();
  });

  it("opens a modal dialog named by its heading", async () => {
    const { user } = setup();
    await openDialog(user);
    expect(dialog().getAttribute("aria-modal"), "the dialog needs aria-modal=\"true\" (TODO 6)").toBe("true");
  });

  it("moves focus to Cancel when the dialog opens", async () => {
    const { user } = setup();
    await openDialog(user);
    const cancel = screen.queryByRole("button", { name: "Cancel" });
    expect(document.activeElement === cancel, "when the dialog opens, focus should move to Cancel, the safe choice (TODO 3)").toBe(true);
  });

  it("closes on Escape and puts focus back on the Remove button", async () => {
    const { user, onRemove } = setup();
    const button = await openDialog(user);
    await user.keyboard("{Escape}");
    expect(dialog(), "Escape should close the dialog. Its onKeyDown only hears keys while focus is inside it, so do TODO 3 too (TODO 6)").toBeNull();
    expect(document.activeElement === button, "after the dialog closes, focus should go back to the Remove button that opened it (TODO 4)").toBe(true);
    expect(onRemove.mock.calls.length, "Escape must not remove the item").toBe(0);
  });

  it("closes on Cancel without removing, and puts focus back", async () => {
    const { user, onRemove } = setup();
    const button = await openDialog(user);
    await user.click(screen.queryByRole("button", { name: "Cancel" }));
    expect(dialog(), "Cancel should close the dialog").toBeNull();
    expect(onRemove.mock.calls.length, "Cancel must not remove the item").toBe(0);
    expect(document.activeElement === button, "after Cancel, focus should go back to the Remove button (TODO 4)").toBe(true);
  });

  it("removes the item once when you press Yes, remove", async () => {
    const { user, onRemove } = setup();
    await openDialog(user);
    await user.click(screen.queryByRole("button", { name: "Yes, remove" }));
    expect(onRemove.mock.calls.length, "Yes, remove should call onRemove exactly once").toBe(1);
    expect(dialog(), "the dialog should close after removing").toBeNull();
  });
});

import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup, act } from "@testing-library/react";
import { QuickView } from "../src/QuickView.tsx";

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

/** Presses a key the way a real keyboard would: the event goes to window. */
function press(key: string) {
  act(() => {
    window.dispatchEvent(new KeyboardEvent("keydown", { key }));
  });
}

describe("QuickView", () => {
  it("shows the item's name and price when it is open", () => {
    render(<QuickView name="Golu the elephant" paise={45000} open={true} onClose={() => {}} />);
    expect(screen.queryByRole("dialog", { name: "Golu the elephant" }), "an open quick view should show a dialog named Golu the elephant").not.toBeNull();
    expect(screen.queryByText("₹450.00"), "45000 paise should show as ₹450.00").not.toBeNull();
  });

  it("shows nothing when it is closed", () => {
    render(<QuickView name="Golu the elephant" paise={45000} open={false} onClose={() => {}} />);
    expect(screen.queryByRole("dialog"), "a closed quick view should not be on the page").toBeNull();
  });

  it("calls onClose once when you press Escape", () => {
    const onClose = vi.fn();
    render(<QuickView name="Golu the elephant" paise={45000} open={true} onClose={onClose} />);
    press("Escape");
    expect(onClose.mock.calls.length, "pressing Escape while the quick view is open should call onClose once").toBe(1);
  });

  it("ignores other keys", () => {
    const onClose = vi.fn();
    render(<QuickView name="Golu the elephant" paise={45000} open={true} onClose={onClose} />);
    press("Enter");
    press("a");
    expect(onClose.mock.calls.length, "only Escape should close the quick view").toBe(0);
  });

  it("doesn't listen while it is closed", () => {
    const onClose = vi.fn();
    render(<QuickView name="Golu the elephant" paise={45000} open={false} onClose={onClose} />);
    press("Escape");
    expect(onClose.mock.calls.length, "a closed quick view should not react to Escape").toBe(0);
  });

  it("stops listening when it closes", () => {
    const onClose = vi.fn();
    const removed = vi.spyOn(window, "removeEventListener");
    const { rerender } = render(<QuickView name="Golu the elephant" paise={45000} open={true} onClose={onClose} />);
    rerender(<QuickView name="Golu the elephant" paise={45000} open={false} onClose={onClose} />);
    press("Escape");
    expect(onClose.mock.calls.length, "after the quick view closes, Escape should do nothing").toBe(0);
    const keydownRemovals = removed.mock.calls.filter(([type]) => type === "keydown");
    expect(keydownRemovals.length > 0, "the cleanup should call window.removeEventListener(\"keydown\", handleKey)").toBe(true);
  });

  it("stops listening when it leaves the page", () => {
    const onClose = vi.fn();
    const { unmount } = render(<QuickView name="Golu the elephant" paise={45000} open={true} onClose={onClose} />);
    unmount();
    press("Escape");
    expect(onClose.mock.calls.length, "a quick view that was removed should not call onClose").toBe(0);
  });
});

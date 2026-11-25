import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup, act } from "@testing-library/react";
import { CartNudge } from "../src/CartNudge.tsx";

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  cleanup();
  vi.useRealTimers();
});

/** Moves the fake clock forward, letting React update the page as it goes. */
function wait(ms: number) {
  act(() => {
    vi.advanceTimersByTime(ms);
  });
}

/** The reminder's text, or null when there is no reminder on the page. */
function reminder() {
  return screen.queryByRole("status")?.textContent ?? null;
}

describe("CartNudge", () => {
  it("shows nothing straight away", () => {
    render(<CartNudge count={2} delayMs={5000} />);
    expect(reminder(), "the reminder should wait before it appears").toBeNull();
  });

  it("shows the reminder after the wait", () => {
    render(<CartNudge count={2} delayMs={5000} />);
    wait(5000);
    expect(reminder(), "after 5000 ms the reminder should say Still thinking? Your 2 items are saved in the cart.").toBe(
      "Still thinking? Your 2 items are saved in the cart.",
    );
  });

  it("doesn't show the reminder a moment too early", () => {
    render(<CartNudge count={1} delayMs={5000} />);
    wait(4999);
    expect(reminder(), "after 4999 ms there should be no reminder yet").toBeNull();
    wait(1);
    expect(reminder(), "at 5000 ms the reminder should say Your 1 item is saved").toBe("Still thinking? Your 1 item is saved in the cart.");
  });

  it("starts the wait again when the cart changes", () => {
    const { rerender } = render(<CartNudge count={2} delayMs={5000} />);
    wait(3000);
    rerender(<CartNudge count={3} delayMs={5000} />);
    wait(3000);
    expect(reminder(), "the cart changed 3000 ms ago, so the reminder should still wait (clear the old timer in the cleanup)").toBeNull();
    wait(2000);
    expect(reminder(), "5000 ms after the change, the reminder should show the new count, 3").toBe(
      "Still thinking? Your 3 items are saved in the cart.",
    );
  });

  it("never reminds about an empty cart", () => {
    render(<CartNudge count={0} delayMs={5000} />);
    wait(10000);
    expect(reminder(), "an empty cart should never show the reminder").toBeNull();
  });

  it("stops its timer when it leaves the page", () => {
    const before = vi.getTimerCount();
    const { unmount } = render(<CartNudge count={2} delayMs={5000} />);
    expect(vi.getTimerCount(), "the reminder should start one timer when the cart has items").toBe(before + 1);
    unmount();
    expect(vi.getTimerCount(), "the cleanup should clear the timer when the reminder is removed").toBe(before);
  });
});

import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup, act } from "@testing-library/react";
import { SaleCountdown } from "../src/SaleCountdown.jsx";

beforeEach(() => {
  vi.useFakeTimers();
});

/** Moves the fake clock forward, letting React update the page as it goes. */
function wait(ms) {
  act(() => {
    vi.advanceTimersByTime(ms);
  });
}

afterEach(() => {
  cleanup();
  vi.useRealTimers();
});

describe("SaleCountdown", () => {
  it("starts at the number of seconds it was given", () => {
    render(<SaleCountdown seconds={3} />);
    expect(screen.queryByText("Sale ends in 3 s"), "the banner should first say Sale ends in 3 s").not.toBeNull();
  });

  it("takes one second off every second", () => {
    render(<SaleCountdown seconds={3} />);
    wait(1000);
    expect(screen.queryByText("Sale ends in 2 s"), "after 1 second the banner should say Sale ends in 2 s").not.toBeNull();
    wait(1000);
    expect(
      screen.queryByText("Sale ends in 1 s"),
      "after 2 seconds it should say Sale ends in 1 s (if it is stuck at 2, use the updater form setLeft((l) => ...))",
    ).not.toBeNull();
  });

  it("says the sale is over at zero and stays there", () => {
    render(<SaleCountdown seconds={3} />);
    wait(3000);
    expect(screen.queryByText("The Diwali sale is over."), "after 3 seconds the banner should say The Diwali sale is over.").not.toBeNull();
    wait(2000);
    expect(screen.queryByText("The Diwali sale is over."), "the countdown should stop at zero, not go below it").not.toBeNull();
  });

  it("stops its timer when the banner leaves the page", () => {
    const before = vi.getTimerCount();
    const { unmount } = render(<SaleCountdown seconds={30} />);
    expect(vi.getTimerCount(), "the banner should start one timer after it appears").toBe(before + 1);
    unmount();
    expect(vi.getTimerCount(), "the cleanup should stop the timer when the banner is removed").toBe(before);
  });
});

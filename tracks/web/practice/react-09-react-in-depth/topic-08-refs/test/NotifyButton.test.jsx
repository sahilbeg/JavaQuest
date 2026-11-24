import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup, act } from "@testing-library/react";
import { NotifyButton } from "../src/NotifyButton.jsx";

const MESSAGE = "We'll message you on WhatsApp when it's back.";

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  cleanup();
  vi.useRealTimers();
});

function tap() {
  act(() => {
    screen.getByRole("button", { name: "Notify me" }).click();
  });
}

function wait(ms) {
  act(() => {
    vi.advanceTimersByTime(ms);
  });
}

describe("NotifyButton", () => {
  it("shows the message for 3 seconds after a tap", () => {
    render(<NotifyButton />);
    tap();
    expect(screen.queryByText(MESSAGE), "the message should show straight after the tap").not.toBeNull();
    wait(2999);
    expect(screen.queryByText(MESSAGE), "the message should still show just before 3 seconds").not.toBeNull();
    wait(1);
    expect(screen.queryByText(MESSAGE), "the message should be gone after 3 seconds").toBeNull();
  });

  it("gives a second tap a full 3 seconds", () => {
    render(<NotifyButton />);
    tap();
    wait(2000);
    tap();
    wait(2000);
    expect(
      screen.queryByText(MESSAGE),
      "2 seconds after the second tap the message should still show: cancel the first tap's timer",
    ).not.toBeNull();
    wait(1000);
    expect(screen.queryByText(MESSAGE), "3 seconds after the second tap the message should be gone").toBeNull();
  });

  it("cancels its timer when it leaves the page", () => {
    const before = vi.getTimerCount();
    const { unmount } = render(<NotifyButton />);
    tap();
    expect(vi.getTimerCount(), "a tap should start one timer").toBe(before + 1);
    unmount();
    expect(vi.getTimerCount(), "removing the button should cancel its waiting timer").toBe(before);
  });
});

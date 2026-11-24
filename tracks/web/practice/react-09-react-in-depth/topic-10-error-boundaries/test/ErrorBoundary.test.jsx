import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ErrorBoundary } from "../src/ErrorBoundary.jsx";

beforeEach(() => {
  // React logs every error a boundary catches. Keep the test output quiet.
  vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

function Bomb() {
  throw new Error("The reviews service sent bad data");
}

let broken = true;
function Flaky() {
  if (broken) throw new Error("Not ready yet");
  return <p>Reviews loaded</p>;
}

describe("ErrorBoundary", () => {
  it("shows its children when nothing breaks", () => {
    render(
      <ErrorBoundary>
        <p>All good</p>
      </ErrorBoundary>,
    );
    expect(screen.queryByText("All good"), "with no error the boundary should just show its children").not.toBeNull();
  });

  it("shows the fallback when a child throws while rendering", () => {
    render(
      <ErrorBoundary fallback="Reviews are resting.">
        <Bomb />
      </ErrorBoundary>,
    );
    expect(screen.queryByText("Reviews are resting."), "a crash inside should show the fallback text instead").not.toBeNull();
    expect(screen.queryByRole("alert"), "the fallback should be inside an element with role=\"alert\"").not.toBeNull();
  });

  it("uses a default message when no fallback is given", () => {
    render(
      <ErrorBoundary>
        <Bomb />
      </ErrorBoundary>,
    );
    expect(screen.queryByText("Something went wrong."), "without a fallback prop the message should be Something went wrong.").not.toBeNull();
  });

  it("keeps everything outside the boundary on the page", () => {
    render(
      <>
        <h1>Golu the elephant</h1>
        <ErrorBoundary>
          <Bomb />
        </ErrorBoundary>
      </>,
    );
    expect(screen.queryByRole("heading", { name: "Golu the elephant" }), "a crash inside the boundary must not remove the heading outside it").not.toBeNull();
  });

  it("tells onError about the error", () => {
    const onError = vi.fn();
    render(
      <ErrorBoundary onError={onError}>
        <Bomb />
      </ErrorBoundary>,
    );
    expect(onError.mock.calls.length, "componentDidCatch should call onError once").toBe(1);
    expect(onError.mock.calls[0][0]?.message, "onError should get the error that was thrown").toBe("The reviews service sent bad data");
  });

  it("renders the children again when you press Try again", async () => {
    const user = userEvent.setup();
    broken = true;
    render(
      <ErrorBoundary>
        <Flaky />
      </ErrorBoundary>,
    );
    broken = false;
    await user.click(screen.getByRole("button", { name: "Try again" }));
    expect(screen.queryByText("Reviews loaded"), "after Try again, the fixed children should show").not.toBeNull();
    expect(screen.queryByRole("alert"), "the fallback should be gone after a successful try").toBeNull();
  });
});

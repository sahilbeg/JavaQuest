import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ErrorBoundary, toError } from "../src/ErrorBoundary.tsx";

beforeEach(() => {
  // React logs every error a boundary catches. Keep the test output quiet.
  vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

function Bomb(): never {
  throw new Error("The suggestions service sent bad data");
}

function StringBomb(): never {
  throw "stock API down";
}

let broken = true;
function Flaky() {
  if (broken) throw new Error("Not ready yet");
  return <p>Suggestions loaded</p>;
}

/** A fallback that shows the error's message and a Try again button. */
function showMessage(error: Error, reset: () => void) {
  return (
    <div role="alert">
      <p>{error instanceof Error ? `Caught: ${error.message}` : "Caught something that is not an Error"}</p>
      <button onClick={reset}>Try again</button>
    </div>
  );
}

describe("toError", () => {
  it("keeps a real Error as it is", () => {
    const error = new TypeError("name is missing");
    expect(toError(error), "an Error should come back as the very same object").toBe(error);
  });

  it("turns other thrown values into an Error", () => {
    const fromString = toError("stock API down");
    expect(fromString instanceof Error, "a thrown string should become an Error").toBe(true);
    expect(fromString.message, "the string should be the message").toBe("stock API down");
    expect(toError(404).message, "a thrown number should become the message 404").toBe("404");
  });
});

describe("ErrorBoundary", () => {
  it("shows its children when nothing breaks", () => {
    render(
      <ErrorBoundary fallback={showMessage}>
        <p>All good</p>
      </ErrorBoundary>,
    );
    expect(screen.queryByText("All good"), "with no error the boundary should just show its children").not.toBeNull();
  });

  it("shows the fallback, with the error, when a child throws while rendering", () => {
    render(
      <ErrorBoundary fallback={showMessage}>
        <Bomb />
      </ErrorBoundary>,
    );
    expect(
      screen.queryByText("Caught: The suggestions service sent bad data"),
      "a crash inside should show the fallback, which gets the error (add getDerivedStateFromError and use fallback in render)",
    ).not.toBeNull();
  });

  it("gives the fallback a real Error even when a string is thrown", () => {
    render(
      <ErrorBoundary fallback={showMessage}>
        <StringBomb />
      </ErrorBoundary>,
    );
    expect(screen.queryByText("Caught: stock API down"), "a thrown string should reach the fallback as an Error (use toError)").not.toBeNull();
  });

  it("keeps everything outside the boundary on the page", () => {
    render(
      <>
        <h1>Golu the elephant</h1>
        <ErrorBoundary fallback={showMessage}>
          <Bomb />
        </ErrorBoundary>
      </>,
    );
    expect(screen.queryByRole("heading", { name: "Golu the elephant" }), "a crash inside the boundary must not remove the heading outside it").not.toBeNull();
  });

  it("tells onError about the error, once", () => {
    const onError = vi.fn();
    render(
      <ErrorBoundary fallback={showMessage} onError={onError}>
        <Bomb />
      </ErrorBoundary>,
    );
    expect(onError.mock.calls.length, "componentDidCatch should call onError once").toBe(1);
    expect(onError.mock.calls[0]?.[0]?.message, "onError should get the error that was thrown").toBe("The suggestions service sent bad data");
  });

  it("renders the children again when you press Try again", async () => {
    const user = userEvent.setup();
    broken = true;
    render(
      <ErrorBoundary fallback={showMessage}>
        <Flaky />
      </ErrorBoundary>,
    );
    broken = false;
    await user.click(screen.getByRole("button", { name: "Try again" }));
    expect(screen.queryByText("Suggestions loaded"), "after Try again, the fixed children should show").not.toBeNull();
    expect(screen.queryByRole("alert"), "the fallback should be gone after a successful try").toBeNull();
  });

  it("clears the error when resetKey changes", () => {
    broken = true;
    const { rerender } = render(
      <ErrorBoundary fallback={showMessage} resetKey="DC-1042">
        <Flaky />
      </ErrorBoundary>,
    );
    broken = false;
    rerender(
      <ErrorBoundary fallback={showMessage} resetKey="DC-1042">
        <Flaky />
      </ErrorBoundary>,
    );
    expect(screen.queryByRole("alert"), "with the same resetKey, the fallback should stay").not.toBeNull();
    rerender(
      <ErrorBoundary fallback={showMessage} resetKey="DC-2001">
        <Flaky />
      </ErrorBoundary>,
    );
    expect(screen.queryByText("Suggestions loaded"), "a new resetKey should clear the error and show the children (componentDidUpdate)").not.toBeNull();
  });
});

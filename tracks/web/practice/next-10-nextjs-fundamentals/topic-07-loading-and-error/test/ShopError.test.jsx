import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ShopError from "../src/app/items/error.jsx";
import { source, startsWithUseClient } from "./source.js";

afterEach(cleanup);

const failure = () => new Error("connect ECONNREFUSED 10.0.0.5:5432");

describe("ShopError", () => {
  it("is a client component", () => {
    expect(startsWithUseClient(source("app/items/error.jsx")), "error.jsx should start with the line \"use client\";").toBe(true);
  });

  it("says sorry in plain words", () => {
    render(<ShopError error={failure()} retry={vi.fn()} />);
    const alert = screen.queryByRole("alert");
    expect(alert, "the error page should be a <div role=\"alert\">").not.toBeNull();
    expect(alert.querySelector("h2")?.textContent, "the alert's <h2> should say We couldn't load the shop.").toBe("We couldn't load the shop.");
    expect(screen.queryByText("Please try again in a moment."), "the alert should say Please try again in a moment.").not.toBeNull();
  });

  it("never shows the technical error to customers", () => {
    const { container } = render(<ShopError error={failure()} retry={vi.fn()} />);
    expect(
      container.textContent.includes("ECONNREFUSED"),
      "the page must not show the error's message (here it mentions ECONNREFUSED and a server address)",
    ).toBe(false);
  });

  it("calls retry once when Try again is pressed", async () => {
    const user = userEvent.setup();
    const retry = vi.fn();
    render(<ShopError error={failure()} retry={retry} />);
    const button = screen.queryByRole("button", { name: "Try again" });
    expect(button, "there should be a button that says Try again").not.toBeNull();
    expect(retry.mock.calls.length, "retry should not be called before the button is pressed").toBe(0);
    await user.click(button);
    expect(retry.mock.calls.length, "pressing Try again should call retry() once").toBe(1);
  });
});

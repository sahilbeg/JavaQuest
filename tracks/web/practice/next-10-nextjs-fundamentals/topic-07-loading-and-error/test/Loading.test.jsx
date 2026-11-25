import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import Loading from "../src/app/items/loading.jsx";

afterEach(cleanup);

describe("Loading", () => {
  it("tells customers the shop is loading", () => {
    render(<Loading />);
    expect(screen.queryByText("Loading the shop..."), "loading.jsx should say Loading the shop...").not.toBeNull();
  });

  it("is announced politely by screen readers", () => {
    render(<Loading />);
    expect(
      screen.queryByRole("status")?.textContent,
      "the message should be in an element with role=\"status\", like <p role=\"status\">",
    ).toBe("Loading the shop...");
  });
});

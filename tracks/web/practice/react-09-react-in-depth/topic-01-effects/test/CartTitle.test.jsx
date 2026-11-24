import { describe, it, expect, afterEach, beforeEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { CartTitle } from "../src/CartTitle.jsx";

beforeEach(() => {
  document.title = "";
});

afterEach(cleanup);

describe("CartTitle", () => {
  it("puts the number of items in the tab title", () => {
    render(<CartTitle count={3} />);
    expect(document.title, "with 3 items the tab should say (3) Dhaaga & Co.").toBe("(3) Dhaaga & Co.");
  });

  it("shows just the shop name when the cart is empty", () => {
    render(<CartTitle count={0} />);
    expect(document.title, "an empty cart should leave the tab as Dhaaga & Co., without (0)").toBe("Dhaaga & Co.");
  });

  it("updates the title when the count changes", () => {
    const { rerender } = render(<CartTitle count={1} />);
    rerender(<CartTitle count={4} />);
    expect(document.title, "after the count goes from 1 to 4 the tab should say (4) Dhaaga & Co.").toBe("(4) Dhaaga & Co.");
  });

  it("leaves the title alone when it renders again with the same count", () => {
    const { rerender } = render(<CartTitle count={2} />);
    document.title = "Someone else's title";
    rerender(<CartTitle count={2} />);
    expect(document.title, "the effect should run again only when count changes, so list count in its dependency array").toBe(
      "Someone else's title",
    );
  });
});

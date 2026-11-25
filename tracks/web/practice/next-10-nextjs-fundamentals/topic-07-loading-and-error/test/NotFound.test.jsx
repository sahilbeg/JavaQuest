import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import ItemNotFound from "../src/app/items/not-found.jsx";

afterEach(cleanup);

describe("ItemNotFound", () => {
  it("says the piece couldn't be found", () => {
    render(<ItemNotFound />);
    expect(
      screen.queryByRole("heading", { level: 1 })?.textContent,
      "not-found.jsx should have the <h1> We couldn't find that piece.",
    ).toBe("We couldn't find that piece.");
  });

  it("links back to the shop", () => {
    render(<ItemNotFound />);
    const link = screen.queryByRole("link", { name: "Back to the shop" });
    expect(link, "there should be a link that says Back to the shop").not.toBeNull();
    expect(link.getAttribute("href"), "Back to the shop should go to /items").toBe("/items");
  });
});

import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { ItemPage } from "../src/ItemPage.jsx";

const ASHA = { id: "r1", author: "Asha", stars: 5, text: "So soft! My niece won't let go of him." };
const NO_TEXT = { id: "r2", author: "Vikram", stars: 4, text: null };

beforeEach(() => {
  // React logs every error a boundary catches. Keep the test output quiet.
  vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe("Golu's page", () => {
  it("shows the reviews when they are all fine", () => {
    render(<ItemPage reviews={[ASHA]} />);
    expect(screen.queryByText(/So soft!/), "Asha's review should be on the page").not.toBeNull();
  });

  it("keeps the page working when a review breaks", () => {
    render(<ItemPage reviews={[ASHA, NO_TEXT]} />);
    expect(screen.queryByRole("heading", { name: "Golu the elephant" }), "the heading must stay when a review crashes").not.toBeNull();
    expect(screen.queryByRole("button", { name: "Add to cart" }), "customers must still be able to add Golu to the cart").not.toBeNull();
    expect(
      screen.queryByText("Reviews are taking a break. You can still buy Golu."),
      "the reviews should be replaced by the fallback message",
    ).not.toBeNull();
  });
});

import { describe, it, expect, afterEach, vi } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { ProductCard } from "../src/ProductCard.jsx";

const GOLU = { name: "Golu the elephant", paise: 45000 };

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

/** jsdom has no real layout, so pretend every element is this many pixels wide. */
function pretendWidth(width) {
  return vi.spyOn(HTMLElement.prototype, "getBoundingClientRect").mockReturnValue({
    width,
    height: 200,
    top: 0,
    left: 0,
    right: width,
    bottom: 200,
    x: 0,
    y: 0,
    toJSON() {
      return {};
    },
  });
}

describe("ProductCard", () => {
  it("stacks when it is narrower than 400 pixels", () => {
    pretendWidth(320);
    const { container } = render(<ProductCard item={GOLU} />);
    expect(container.querySelector("article").getAttribute("data-layout"), "a 320px card should have data-layout=\"stack\"").toBe(
      "stack",
    );
  });

  it("stays in a row at 400 pixels or wider", () => {
    pretendWidth(400);
    const { container } = render(<ProductCard item={GOLU} />);
    expect(container.querySelector("article").getAttribute("data-layout"), "a 400px card should have data-layout=\"row\"").toBe("row");
  });

  it("measures the card element itself", () => {
    const measure = pretendWidth(320);
    const { container } = render(<ProductCard item={GOLU} />);
    const article = container.querySelector("article");
    expect(
      measure.mock.contexts.includes(article),
      "getBoundingClientRect should be called on the <article> through its ref",
    ).toBe(true);
  });
});

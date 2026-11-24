import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { ProductCard, Badge, FeaturedProducts } from "../src/ProductCard.jsx";

afterEach(cleanup);

/** The text of the first element matching the selector, or null when there is none. */
function textOf(container, selector) {
  const element = container.querySelector(selector);
  return element === null ? null : element.textContent;
}

describe("ProductCard", () => {
  it("shows the product's name in the heading", () => {
    const { container } = render(<ProductCard name="Golu the elephant" paise={45000} colour="Indigo" />);
    expect(textOf(container, "article.product-card h3"), "the <h3> should show the name prop").toBe("Golu the elephant");
  });

  it("shows the price in rupees", () => {
    const { container } = render(<ProductCard name="Golu the elephant" paise={45000} colour="Indigo" />);
    expect(textOf(container, "p.price"), "45000 paise should show as ₹450.00 in p.price").toBe("₹450.00");
  });

  it("works out the price from the paise prop, not a fixed number", () => {
    const { container } = render(<ProductCard name="Indigo tote bag" paise={89900} colour="Indigo" />);
    expect(textOf(container, "p.price"), "89900 paise should show as ₹899.00").toBe("₹899.00");
  });

  it("shows the colour it is given", () => {
    const { container } = render(<ProductCard name="Mint keychain" paise={12000} colour="Mint" />);
    expect(textOf(container, "p.colour"), "p.colour should say Colour: Mint").toBe("Colour: Mint");
  });

  it("says Off-white when no colour is given", () => {
    const { container } = render(<ProductCard name="Coaster set" paise={15000} />);
    expect(textOf(container, "p.colour"), "without a colour prop, p.colour should say Colour: Off-white").toBe("Colour: Off-white");
  });
});

describe("Badge", () => {
  it("shows whatever is between its tags inside span.badge", () => {
    const { container } = render(<Badge>Only 2 left</Badge>);
    expect(textOf(container, "span.badge"), "<Badge>Only 2 left</Badge> should show Only 2 left").toBe("Only 2 left");
  });
});

describe("FeaturedProducts", () => {
  it("shows three product cards", () => {
    const { container } = render(<FeaturedProducts />);
    const cards = container.querySelectorAll("article.product-card");
    expect(cards.length, "FeaturedProducts should show three ProductCards").toBe(3);
  });

  it("shows Golu, the coaster set and the tote bag, in that order", () => {
    const { container } = render(<FeaturedProducts />);
    const names = [...container.querySelectorAll("article.product-card h3")].map((h3) => h3.textContent);
    expect(names.join(", "), "the cards should be in the order from the README").toBe(
      "Golu the elephant, Marigold coaster set, Indigo tote bag",
    );
  });

  it("gives each card its own price and colour", () => {
    const { container } = render(<FeaturedProducts />);
    const prices = [...container.querySelectorAll("article.product-card p.price")].map((p) => p.textContent);
    const colours = [...container.querySelectorAll("article.product-card p.colour")].map((p) => p.textContent);
    expect(prices.join(", "), "the prices should be ₹450.00, ₹150.00 and ₹899.00").toBe("₹450.00, ₹150.00, ₹899.00");
    expect(colours.join(", "), "the colours should be Indigo, Marigold and Indigo").toBe(
      "Colour: Indigo, Colour: Marigold, Colour: Indigo",
    );
  });

  it("keeps the New badge in the heading", () => {
    const { container } = render(<FeaturedProducts />);
    expect(textOf(container, "h2 span.badge"), "the <h2> should show a badge that says New").toBe("New");
  });
});

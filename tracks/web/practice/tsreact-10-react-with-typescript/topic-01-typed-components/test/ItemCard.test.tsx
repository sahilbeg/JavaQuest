import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { ItemCard } from "../src/ItemCard.tsx";

afterEach(cleanup);

describe("ItemCard", () => {
  it("is an article with the item-card class", () => {
    const { container } = render(<ItemCard name="Golu the elephant" paise={45000} colour="Indigo" />);
    const card = container.firstElementChild;
    expect(card?.tagName, "the card should be an <article>").toBe("ARTICLE");
    expect(card?.className, "the article should have className item-card").toBe("item-card");
  });

  it("shows the name in a heading", () => {
    const { container } = render(<ItemCard name="Golu the elephant" paise={45000} colour="Indigo" />);
    expect(container.querySelector("h3")?.textContent, "the <h3> should hold the item's name").toBe("Golu the elephant");
  });

  it("shows the price in rupees, not paise", () => {
    const { container } = render(<ItemCard name="Golu the elephant" paise={45000} colour="Indigo" />);
    expect(container.querySelector(".price")?.textContent, "45000 paise should show as ₹450.00").toBe("₹450.00");
  });

  it("shows a bigger price with a comma", () => {
    const { container } = render(<ItemCard name="Two Indigo tote bags" paise={179800} colour="Indigo" />);
    expect(container.querySelector(".price")?.textContent, "179800 paise should show as ₹1,798.00").toBe("₹1,798.00");
  });

  it("shows the colour", () => {
    const { container } = render(<ItemCard name="Mint keychain" paise={12000} colour="Mint" />);
    expect(container.querySelector(".colour")?.textContent, "the colour line should say Mint").toBe("Mint");
  });

  it("shows the Handmade badge when no badge is passed", () => {
    const { container } = render(<ItemCard name="Golu the elephant" paise={45000} colour="Indigo" />);
    const badge = container.querySelector("span.badge");
    expect(badge, "the card should have a <span className=\"badge\">").not.toBeNull();
    expect(badge?.textContent, "with no badge prop, the badge should say Handmade").toBe("Handmade");
  });

  it("shows the badge it is given", () => {
    const props = { name: "Marigold coaster set", paise: 15000, colour: "Marigold", badge: "Diwali special" } as const;
    const { container } = render(<ItemCard {...props} />);
    expect(container.querySelector("span.badge")?.textContent, "badge=\"Diwali special\" should show Diwali special").toBe("Diwali special");
  });
});

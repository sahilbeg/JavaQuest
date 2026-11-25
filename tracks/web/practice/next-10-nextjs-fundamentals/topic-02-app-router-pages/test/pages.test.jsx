import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import HomePage from "../src/app/page.jsx";
import AboutPage from "../src/app/about/page.jsx";
import VisitPage from "../src/app/visit/page.jsx";

afterEach(cleanup);

describe("The pages", () => {
  it("keeps the home page's heading", () => {
    render(<HomePage />);
    expect(
      screen.queryByRole("heading", { level: 1, name: "Handmade in Pune" }),
      "the home page should still have the <h1> Handmade in Pune",
    ).not.toBeNull();
  });

  it("gives the About page the heading Our story", () => {
    render(<AboutPage />);
    expect(
      screen.queryByRole("heading", { level: 1, name: "Our story" }),
      "src/app/about/page.jsx should return an <h1> that says Our story",
    ).not.toBeNull();
  });

  it("tells Meera Kulkarni's story in a paragraph on the About page", () => {
    const { container } = render(<AboutPage />);
    const story = [...container.querySelectorAll("p")].find((p) => p.textContent.includes("Meera Kulkarni"));
    expect(story, "the About page should have a <p> that names Meera Kulkarni").toBeDefined();
  });

  it("gives the Visit us page the heading Visit us", () => {
    render(<VisitPage />);
    expect(
      screen.queryByRole("heading", { level: 1, name: "Visit us" }),
      "src/app/visit/page.jsx should return an <h1> that says Visit us",
    ).not.toBeNull();
  });

  it("lists the stall and the shop with their opening hours", () => {
    const { container } = render(<VisitPage />);
    const lines = [...container.querySelectorAll("ul li")].map((li) => li.textContent.trim());
    expect(lines, "the Visit us page should have a <ul> with the stall's line, then the shop's line, exactly as in README.md").toEqual([
      "FC Road stall: Saturday and Sunday, 10:00 to 20:00",
      "Aundh shop: Tuesday to Sunday, 11:00 to 21:00",
    ]);
  });
});

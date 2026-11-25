import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import HomePage from "../src/app/page.jsx";
import { source, startsWithUseClient } from "./source.js";

afterEach(cleanup);

/** The <li> whose <h2> is the given name, or undefined. */
function row(container, name) {
  return [...container.querySelectorAll("li")].find((li) => li.querySelector("h2")?.textContent === name);
}

describe("HomePage", () => {
  it("is an async server component", () => {
    expect(HomePage.constructor.name, "HomePage should be an async function, so it can await getItems()").toBe("AsyncFunction");
    expect(startsWithUseClient(source("app/page.jsx")), "page.jsx should stay a server component, with no \"use client\" line").toBe(false);
  });

  it("keeps the heading Handmade in Pune", async () => {
    render(await HomePage());
    expect(screen.queryByRole("heading", { level: 1, name: "Handmade in Pune" }), "the page should keep its <h1>").not.toBeNull();
  });

  it("lists every item in the catalogue", async () => {
    const { container } = render(await HomePage());
    expect(container.querySelectorAll("ul > li").length, "the catalogue has 6 items, so the <ul> should have 6 <li>s").toBe(6);
  });

  it("shows each item's name in an <h2> and its price in rupees", async () => {
    const { container } = render(await HomePage());
    const golu = row(container, "Golu the elephant");
    expect(golu, "there should be an <li> whose <h2> says Golu the elephant").toBeDefined();
    expect(golu.querySelector("p")?.textContent, "Golu costs 45000 paise, so his row should show ₹450.00").toBe("₹450.00");
    const tote = row(container, "Indigo tote bag");
    expect(tote?.querySelector("p")?.textContent, "the tote bag costs 89900 paise, so its row should show ₹899.00").toBe("₹899.00");
  });

  it("puts an Add to cart button in every row", async () => {
    const { container } = render(await HomePage());
    const golu = row(container, "Golu the elephant");
    expect(golu?.querySelector("button")?.textContent, "Golu's row should have an Add to cart button").toBe("Add to cart");
    expect(screen.queryAllByRole("button", { name: "Add to cart" }).length, "each of the 6 rows should have its own Add to cart button").toBe(6);
  });
});

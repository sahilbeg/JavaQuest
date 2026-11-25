import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import RootLayout from "../src/app/layout.jsx";
import { bodyContent } from "./layout.js";

afterEach(cleanup);

function renderWithPage() {
  return render(<>{bodyContent(RootLayout({ children: <p>This is the page</p> }))}</>);
}

const comesBefore = (a, b) => Boolean(a.compareDocumentPosition(b) & Node.DOCUMENT_POSITION_FOLLOWING);

describe("RootLayout", () => {
  it("writes the <html> element with lang en", () => {
    const tree = RootLayout({ children: <p>This is the page</p> });
    expect(tree?.type, "the root layout should return an <html> element").toBe("html");
    expect(tree?.props?.lang, "the <html> element should keep lang=\"en\"").toBe("en");
  });

  it("has a header with the shop's name and the delivery banner", () => {
    const { container } = renderWithPage();
    const header = container.querySelector("header");
    expect(header, "inside <body>, the root layout should have a <header>").not.toBeNull();
    const lines = [...header.querySelectorAll("p")].map((p) => p.textContent.trim());
    expect(lines, "the <header> should hold <p>Dhaaga & Co.</p> and <p>Free delivery in Pune above ₹999</p>").toEqual([
      "Dhaaga & Co.",
      "Free delivery in Pune above ₹999",
    ]);
  });

  it("puts the page inside <main>", () => {
    const { container } = renderWithPage();
    const main = container.querySelector("main");
    expect(main, "inside <body>, the root layout should have a <main>").not.toBeNull();
    expect(main.textContent.includes("This is the page"), "the page ({children}) should be inside <main>").toBe(true);
  });

  it("ends with a footer that says Handmade in Pune, after the page", () => {
    const { container } = renderWithPage();
    const footer = container.querySelector("footer");
    expect(footer, "inside <body>, the root layout should have a <footer>").not.toBeNull();
    expect(footer.textContent.trim(), "the <footer> should say Handmade in Pune").toBe("Handmade in Pune");
    const main = container.querySelector("main");
    expect(main !== null && comesBefore(main, footer), "the <footer> should come after <main>").toBe(true);
  });

  it("shows the header before the page", () => {
    const { container } = renderWithPage();
    const header = container.querySelector("header");
    const main = container.querySelector("main");
    expect(header !== null && main !== null && comesBefore(header, main), "the <header> should come before <main>").toBe(true);
  });
});

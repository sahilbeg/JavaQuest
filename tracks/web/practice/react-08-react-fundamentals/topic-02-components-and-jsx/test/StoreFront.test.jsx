import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { StoreHeader, OpeningHours, StoreFront, TAGLINE } from "../src/StoreFront.jsx";

afterEach(cleanup);

describe("StoreHeader", () => {
  it("shows the shop name in the main heading", () => {
    render(<StoreHeader />);
    const h1 = screen.queryByRole("heading", { level: 1 });
    expect(h1, "StoreHeader should show an <h1>").not.toBeNull();
    expect(h1.textContent, "the <h1> should say Dhaaga & Co.").toBe("Dhaaga & Co.");
  });

  it("is a header element with the class store-header", () => {
    const { container } = render(<StoreHeader />);
    const header = container.querySelector("header");
    expect(header, "StoreHeader should return a <header> element").not.toBeNull();
    expect(header.className, "the <header> needs className=\"store-header\"").toBe("store-header");
  });

  it("shows the tagline from the TAGLINE constant", () => {
    render(<StoreHeader />);
    const tagline = screen.queryByText(TAGLINE);
    expect(tagline, "a paragraph should show Handmade crochet from Pune").not.toBeNull();
    expect(tagline.tagName, "the tagline should be in a <p>").toBe("P");
  });

  it("shows Golu's photo with a helpful alt text", () => {
    render(<StoreHeader />);
    const photo = screen.queryByAltText("Golu the crocheted elephant");
    expect(photo, "an <img> with alt=\"Golu the crocheted elephant\" should be in the header").not.toBeNull();
    expect(photo.getAttribute("src"), "the photo's src should be golu.png").toBe("golu.png");
  });

  it("keeps the heading, tagline and photo inside the header", () => {
    const { container } = render(<StoreHeader />);
    const header = container.querySelector("header");
    expect(header, "StoreHeader should return a <header> element").not.toBeNull();
    expect(header.querySelector("h1"), "the <h1> should be inside the <header>").not.toBeNull();
    expect(header.querySelector("p"), "the tagline <p> should be inside the <header>").not.toBeNull();
    expect(header.querySelector("img"), "the photo should be inside the <header>").not.toBeNull();
  });
});

describe("OpeningHours", () => {
  it("lists the stall and then the shop", () => {
    const { container } = render(<OpeningHours />);
    const items = [...container.querySelectorAll("ul.hours li")].map((li) => li.textContent);
    expect(items.length, "there should be two <li> items inside <ul className=\"hours\">").toBe(2);
    expect(items[0], "the first item should give the stall's hours").toBe("FC Road stall: Sat and Sun, 10:00 to 20:00");
    expect(items[1], "the second item should give the shop's hours").toBe("Aundh shop: Tue to Sun, 11:00 to 21:00");
  });
});

describe("StoreFront", () => {
  it("puts the header and then the opening hours inside <main>", () => {
    const { container } = render(<StoreFront />);
    const main = container.querySelector("main");
    expect(main, "StoreFront should return a <main> element").not.toBeNull();
    expect(main.querySelector("header.store-header"), "<main> should hold <StoreHeader />").not.toBeNull();
    expect(main.querySelector("ul.hours"), "<main> should hold <OpeningHours />").not.toBeNull();
    const tags = [...main.children].map((child) => child.tagName);
    expect(tags.join(", "), "inside <main>, the header should come first and the list second").toBe("HEADER, UL");
  });
});

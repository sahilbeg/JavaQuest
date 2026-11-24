import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

/** Only the text written straight inside an element, not the text of the elements inside it. */
const ownText = (el) =>
  Array.from(el.childNodes)
    .filter((node) => node.nodeType === 3)
    .map((node) => node.textContent)
    .join(" ")
    .replace(/\s+/g, " ")
    .trim();

/** The li elements that sit straight inside a list, not the ones in a nested list. */
const items = (list) => Array.from(list.children).filter((child) => child.tagName === "LI");

const PRODUCTS = ["Coaster set (4)", "Keychain", "Amigurumi (small toy)", "Tote bag", "Baby booties"];
const COLOURS = ["Marigold", "Indigo", "Rani pink", "Mint", "Off-white"];
const STEPS = ["Pick an item from the catalogue.", "Send us its name on WhatsApp.", "Pay by UPI and we start stitching."];

describe("The catalogue's lists", () => {
  it("puts the five products in an unordered list with the id products", () => {
    const list = page().getElementById("products");
    expect(list, 'there should be an element with id="products"').not.toBeNull();
    expect(list.tagName, 'the element with id="products" should be a <ul>, because the products have no order').toBe("UL");
    const names = items(list).map(ownText);
    expect(names, "the list should have one <li> per product, in the same order as before").toEqual(PRODUCTS);
  });

  it("uses no br inside the product list", () => {
    const list = page().getElementById("products");
    expect(list, 'there should be an element with id="products"').not.toBeNull();
    expect(list.querySelectorAll("br").length, "each <li> starts on its own line, so the <br> tags can go").toBe(0);
  });

  it("nests the five colours inside the amigurumi item", () => {
    const list = page().getElementById("products");
    expect(list, 'there should be an element with id="products"').not.toBeNull();
    const amigurumi = items(list).find((li) => ownText(li) === "Amigurumi (small toy)");
    expect(amigurumi, "there should be an <li> for Amigurumi (small toy)").toBeDefined();
    const inner = amigurumi.querySelector("ul");
    expect(inner, "the amigurumi <li> should hold a nested <ul>, before its </li>").not.toBeNull();
    expect(items(inner).map(text), "the nested list should have the five colours, one <li> each").toEqual(COLOURS);
  });

  it("puts the order steps in an ordered list", () => {
    const steps = page().querySelector("ol");
    expect(steps, "the How to order steps should be an <ol>, because their order matters").not.toBeNull();
    expect(items(steps).map(text), "the <ol> should have the three steps, one <li> each, with no numbers typed in").toEqual(STEPS);
  });
});

import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

/** The th and td cells of one table row. */
const cells = (row) => Array.from(row.children).filter((c) => c.tagName === "TH" || c.tagName === "TD");

const PRICES = [
  ["CST", "Coaster set (4)", "₹150.00"],
  ["KEY", "Keychain", "₹120.00"],
  ["AMI", "Amigurumi (small toy)", "₹450.00"],
  ["BAG", "Tote bag", "₹899.00"],
  ["BTY", "Baby booties", "₹350.00"],
];

/** The price table, or null when the page has none. */
const table = () => page().querySelector("table");

describe("The price table", () => {
  it("has a caption that says Our prices, as its first child", () => {
    const t = table();
    expect(t, "the page should have a <table>").not.toBeNull();
    const caption = t.querySelector("caption");
    expect(caption, "the table should have a <caption>").not.toBeNull();
    expect(text(caption), "the caption should say Our prices").toBe("Our prices");
    expect(t.firstElementChild, "the <caption> must be the first thing inside <table>").toBe(caption);
  });

  it("has a thead with three column titles, each a th with scope col", () => {
    const t = table();
    expect(t, "the page should have a <table>").not.toBeNull();
    const rows = t.querySelectorAll("thead tr");
    expect(rows.length, "the <thead> should hold exactly one row of titles").toBe(1);
    const titles = cells(rows[0]);
    expect(titles.map(text), "the title row should say Code, Item, Price").toEqual(["Code", "Item", "Price"]);
    for (const title of titles) {
      expect(title.tagName, `the title ${text(title)} should be a <th>, not a <td>`).toBe("TH");
      expect(title.getAttribute("scope"), `the title ${text(title)} should have scope="col"`).toBe("col");
    }
  });

  it("has one row per item in the tbody, with code, name and price", () => {
    const t = table();
    expect(t, "the page should have a <table>").not.toBeNull();
    const rows = Array.from(t.querySelectorAll("tbody tr"));
    expect(rows.length, "the <tbody> should have five rows, one per item").toBe(5);
    rows.forEach((row, i) => {
      expect(cells(row).map(text), `row ${i + 1} should be ${PRICES[i].join(", ")}`).toEqual(PRICES[i]);
    });
  });

  it("makes each item code a row header with scope row", () => {
    const t = table();
    expect(t, "the page should have a <table>").not.toBeNull();
    for (const row of t.querySelectorAll("tbody tr")) {
      const first = cells(row)[0];
      expect(first?.tagName, `the code ${first ? text(first) : ""} titles its row, so it should be a <th>`).toBe("TH");
      expect(first.getAttribute("scope"), `the code ${text(first)} should have scope="row"`).toBe("row");
    }
  });
});

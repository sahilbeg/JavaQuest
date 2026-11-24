import { describe, it, expect } from "vitest";
import { page } from "./html.js";

describe("The Diwali switch", () => {
  it("turns the Diwali theme on with a class on <body>", () => {
    expect(page().body.classList.contains("theme-diwali"), 'the <body> tag should be <body class="theme-diwali">').toBe(true);
  });

  it("puts the theme class only on <body>", () => {
    const others = [...page().querySelectorAll(".theme-diwali")].filter((element) => element.tagName !== "BODY");
    expect(others.length, "only <body> should carry theme-diwali, so the whole page switches together").toBe(0);
  });
});

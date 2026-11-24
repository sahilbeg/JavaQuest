import { describe, it, expect } from "vitest";
import { value } from "./css.js";

describe("The catalogue's first styles", () => {
  it("centres every h1 with a type selector", () => {
    expect(value("h1", "text-align"), "add a rule h1 { text-align: center; }").toBe("center");
  });

  it("makes every price bold with a class selector", () => {
    expect(value(".price", "font-weight") ?? "", "add .price { font-weight: bold; } (with a dot before price)").toMatch(/^(bold|700)$/);
  });

  it("makes the Visit us footer italic with an id selector", () => {
    expect(value("#visit", "font-style"), "add #visit { font-style: italic; } (with a hash before visit)").toBe("italic");
  });

  it("colours every h2 and h3 dark blue", () => {
    expect(value("h2", "color"), "h2 should get color: darkblue (try the grouped rule h2, h3)").toBe("darkblue");
    expect(value("h3", "color"), "h3 should get color: darkblue (try the grouped rule h2, h3)").toBe("darkblue");
  });

  it("colours only the links inside the nav dark red", () => {
    expect(value("nav a", "color"), "add nav a { color: darkred; } (a space between nav and a)").toBe("darkred");
    expect(value("a", "color"), "don't colour every link: the footer's WhatsApp link should keep its own colour").toBeUndefined();
  });
});

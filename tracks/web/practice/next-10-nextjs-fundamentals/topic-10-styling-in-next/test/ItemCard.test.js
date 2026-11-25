import { describe, it, expect } from "vitest";
import { source, value, withoutComments } from "./css.js";
import { cardClass } from "../src/app/items/cardClass.js";

const MODULE = "app/items/ItemCard.module.css";

describe("ItemCard.module.css", () => {
  it("gives the card a marigold border, round corners and padding", () => {
    expect(value(MODULE, ".card", "border"), ".card should have border: 2px solid var(--marigold)").toBe("2px solid var(--marigold)");
    expect(value(MODULE, ".card", "border-radius"), ".card should have border-radius: 8px").toBe("8px");
    expect(value(MODULE, ".card", "padding"), ".card should have padding: 1rem").toBe("1rem");
  });

  it("makes the price bold and indigo", () => {
    expect(value(MODULE, ".price", "color"), ".price should have color: var(--indigo)").toBe("var(--indigo)");
    expect(value(MODULE, ".price", "font-weight"), ".price should have font-weight: 700").toBe("700");
  });

  it("fades sold-out cards", () => {
    expect(/^(0\.5|\.5)$/.test(value(MODULE, ".soldOut", "opacity") ?? ""), ".soldOut should have opacity: 0.5").toBe(true);
  });
});

describe("cardClass", () => {
  const styles = { card: "ItemCard_card__x7y8", soldOut: "ItemCard_soldOut__k9q1" };

  it("gives a card with stock only the card class", () => {
    expect(cardClass(styles, { name: "Golu the elephant", stock: 7 }), "Golu has 7 left, so only styles.card").toBe("ItemCard_card__x7y8");
  });

  it("adds the sold-out class when none are left", () => {
    expect(
      cardClass(styles, { name: "Mint keychain", stock: 0 }),
      "the Mint keychain is sold out, so styles.card and styles.soldOut joined with one space",
    ).toBe("ItemCard_card__x7y8 ItemCard_soldOut__k9q1");
  });
});

describe("ItemCard.jsx", () => {
  const code = withoutComments(source("app/items/ItemCard.jsx"));

  it("uses the module's class names instead of plain text", () => {
    expect(/className="/.test(code), "ItemCard should not use plain text class names like className=\"card\"").toBe(false);
  });

  it("puts cardClass(styles, item) on the card and styles.price on the price", () => {
    expect(
      /className=\{\s*cardClass\(\s*styles\s*,\s*item\s*\)\s*\}/.test(code),
      "the <article> should have className={cardClass(styles, item)}",
    ).toBe(true);
    expect(/className=\{\s*styles\.price\s*\}/.test(code), "the price should have className={styles.price}").toBe(true);
  });
});

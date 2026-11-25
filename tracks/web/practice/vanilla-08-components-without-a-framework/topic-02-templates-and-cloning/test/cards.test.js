import { describe, it, expect, beforeEach } from "vitest";
import { cardFromTemplate, renderCards } from "../src/cards.js";

beforeEach(() => {
  document.body.innerHTML = `
    <template id="product-card">
      <li class="product">
        <img class="photo" alt="">
        <h3 class="name"></h3>
        <p class="price"></p>
        <button class="add" type="button">Add to cart</button>
      </li>
    </template>
    <ul id="catalogue"></ul>`;
});

const template = () => document.querySelector("#product-card");
const list = () => document.querySelector("#catalogue");

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, photo: "photos/DC-1042.jpg" };
const coasters = { id: "DC-1001", name: "Marigold coaster set", paise: 15000, photo: "photos/DC-1001.jpg" };
const keychain = { id: "DC-1013", name: "Rani pink keychain", paise: 12000, photo: "photos/DC-1013.jpg" };

describe("cardFromTemplate", () => {
  it("returns a copy of the template's li.product", () => {
    const card = cardFromTemplate(template(), golu);
    expect(card.tagName, "the card should be the template's <li>").toBe("LI");
    expect(card.classList.contains("product"), "the card should keep the template's class product").toBe(true);
    expect(card.querySelector("button.add")?.textContent, "the card should hold the template's Add to cart button").toBe("Add to cart");
  });

  it("fills the id, name and price", () => {
    const card = cardFromTemplate(template(), golu);
    expect(card.dataset.id, "the card's data-id should be DC-1042").toBe("DC-1042");
    expect(card.querySelector(".name")?.textContent, ".name should say Golu the elephant").toBe("Golu the elephant");
    expect(card.querySelector(".price")?.textContent, "45000 paise should show as ₹450.00 in .price").toBe("₹450.00");
  });

  it("sets the photo and its alt text", () => {
    const photo = cardFromTemplate(template(), golu).querySelector(".photo");
    expect(photo?.getAttribute("src"), "the photo's src should be the product's photo").toBe("photos/DC-1042.jpg");
    expect(photo?.getAttribute("alt"), "the photo's alt should be the product's name").toBe("Golu the elephant");
  });

  it("labels the button for screen readers", () => {
    const button = cardFromTemplate(template(), coasters).querySelector(".add");
    expect(button?.getAttribute("aria-label"), "the button should be labelled Add Marigold coaster set to cart").toBe("Add Marigold coaster set to cart");
  });

  it("makes a separate card each time and leaves the template blank", () => {
    const first = cardFromTemplate(template(), golu);
    const second = cardFromTemplate(template(), coasters);
    expect(first === second, "two calls should give two different cards").toBe(false);
    expect(first.querySelector(".name")?.textContent, "the first card should still say Golu after the second is made").toBe("Golu the elephant");
    expect(second.querySelector(".name")?.textContent, "the second card should say Marigold coaster set").toBe("Marigold coaster set");
    expect(template().content.querySelector(".name")?.textContent, "the template itself must stay blank: fill the copy").toBe("");
    expect(template().content.querySelector(".product")?.dataset.id, "the template's li must not get a data-id").toBeUndefined();
  });

  it("keeps a name with tags as plain text", () => {
    const card = cardFromTemplate(template(), { ...golu, name: "<b>Golu</b>" });
    expect(card.querySelector(".name")?.textContent, "the name should show exactly as written").toBe("<b>Golu</b>");
    expect(card.querySelector("b"), "a name must never turn into HTML").toBeNull();
  });
});

describe("renderCards", () => {
  it("draws one card per product, in order", () => {
    renderCards(list(), template(), [golu, coasters, keychain]);
    const ids = [...list().querySelectorAll("li.product")].map((card) => card.dataset.id);
    expect(ids, "the list should hold Golu, the coasters and the keychain, in that order").toEqual(["DC-1042", "DC-1001", "DC-1013"]);
  });

  it("replaces the old cards when it draws again", () => {
    renderCards(list(), template(), [golu, coasters, keychain]);
    renderCards(list(), template(), [keychain]);
    expect(list().children.length, "drawing one product after three should leave exactly one card").toBe(1);
    expect(list().querySelector(".name")?.textContent, "the one card left should be the keychain").toBe("Rani pink keychain");
  });
});

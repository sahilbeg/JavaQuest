import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { ProductGrid } from "../src/ProductGrid.jsx";

afterEach(cleanup);

const PRODUCTS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1102", name: "Indigo coaster set", paise: 15000 },
  { id: "DC-1201", name: "Mint keychain", paise: 12000 },
  { id: "DC-1301", name: "Rani pink tote bag", paise: 89900 },
  { id: "DC-1401", name: "Off-white baby booties", paise: 35000 },
];

function photos() {
  render(<ProductGrid products={PRODUCTS} />);
  const imgs = [...document.querySelectorAll("img")];
  expect(imgs.length, "the grid should draw one <img> per product: 6").toBe(6);
  return imgs;
}

describe("ProductGrid photos", () => {
  it("describe each product in their alt text", () => {
    photos().forEach((img, i) => {
      expect(img.getAttribute("alt"), `photo ${i + 1} should have the product's name as alt text: ${PRODUCTS[i].name}`).toBe(PRODUCTS[i].name);
    });
  });

  it("keep their space with width 400 and height 400, so the page doesn't jump", () => {
    photos().forEach((img, i) => {
      expect(img.getAttribute("width"), `photo ${i + 1} (${PRODUCTS[i].name}) needs width="400"`).toBe("400");
      expect(img.getAttribute("height"), `photo ${i + 1} (${PRODUCTS[i].name}) needs height="400"`).toBe("400");
    });
  });

  it("load the first four straight away and the rest lazily", () => {
    photos().forEach((img, i) => {
      if (i < 4) {
        expect(img.getAttribute("loading"), `photo ${i + 1} is on the first screen, so it must not be lazy: use loading="eager"`).toBe("eager");
      } else {
        expect(img.getAttribute("loading"), `photo ${i + 1} is below the first screen, so it should have loading="lazy"`).toBe("lazy");
      }
    });
  });

  it("use the small file as src and offer both sizes through srcset and sizes", () => {
    photos().forEach((img, i) => {
      const id = PRODUCTS[i].id;
      expect(img.getAttribute("src"), `photo ${i + 1} should use the 400-pixel file as its src, not the 1600-pixel one`).toBe(`/img/${id}-400.webp`);
      expect(img.getAttribute("srcset"), `photo ${i + 1} should list both sizes in srcSet`).toBe(`/img/${id}-400.webp 400w, /img/${id}-800.webp 800w`);
      expect(img.getAttribute("sizes"), `photo ${i + 1} should say how wide it shows: sizes="(min-width: 600px) 400px, 100vw"`).toBe("(min-width: 600px) 400px, 100vw");
    });
  });
});

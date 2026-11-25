import { describe, it, expect, beforeEach } from "vitest";
import { renderList, equalHeights, createRenderScheduler } from "../src/render.js";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
const coasters = { id: "DC-2000", name: "Marigold coaster set", paise: 15000 };
const booties = { id: "DC-2440", name: "Off-white baby booties", paise: 35000 };

let list;

beforeEach(() => {
  document.body.innerHTML = `<ul id="products"></ul>`;
  list = document.querySelector("#products");
});

/** Runs work, and counts how many separate changes it made to the target's children. */
function changesTo(target, work) {
  const observer = new MutationObserver(() => {});
  observer.observe(target, { childList: true });
  work();
  const records = observer.takeRecords();
  observer.disconnect();
  return records.length;
}

const ids = () => [...list.querySelectorAll("li.product")].map((li) => li.dataset.id);

describe("renderList", () => {
  it("draws one card per product, in order", () => {
    renderList(list, [golu, coasters, booties]);
    expect(ids(), "the list should hold Golu, the coaster set and the booties, in that order").toEqual(["DC-1042", "DC-2000", "DC-2440"]);
    expect(list.querySelector('[data-id="DC-1042"] .price')?.textContent, "Golu's card should show ₹450.00").toBe("₹450.00");
  });

  it("replaces the old cards instead of adding to them", () => {
    renderList(list, [golu, coasters]);
    renderList(list, [booties]);
    expect(ids(), "after the second draw, only the booties should be left").toEqual(["DC-2440"]);
  });

  it("empties the list when nothing matches", () => {
    renderList(list, [golu]);
    renderList(list, []);
    expect(list.children.length, "drawing no products should leave no cards").toBe(0);
  });

  it("puts a whole list of cards into the page as one change", () => {
    const changes = changesTo(list, () => renderList(list, [golu, coasters, booties]));
    expect(changes, "three cards should go in as one change (one DocumentFragment), not one change per card").toBe(1);
  });

  it("swaps an old list for a new one as one change", () => {
    renderList(list, [golu, coasters]);
    const changes = changesTo(list, () => renderList(list, [booties, golu]));
    expect(changes, "removing the old cards and adding the new ones should be one change (list.replaceChildren)").toBe(1);
  });
});

/** Cards that report their natural height, and count reads made while any card still had a height set. */
function makeCards(naturalHeights) {
  const log = { reads: 0, readsMixedWithWrites: 0 };
  const cards = naturalHeights.map(() => {
    const card = document.createElement("li");
    card.className = "product";
    card.style.height = "80px";
    list.append(card);
    return card;
  });
  cards.forEach((card, index) => {
    Object.defineProperty(card, "offsetHeight", {
      configurable: true,
      get() {
        log.reads += 1;
        if (cards.some((other) => other.style.height !== "")) {
          log.readsMixedWithWrites += 1;
        }
        return naturalHeights[index];
      },
    });
  });
  return { cards, log };
}

describe("equalHeights", () => {
  it("makes every card as tall as the tallest one", () => {
    const { cards } = makeCards([120, 180, 150]);
    equalHeights(cards);
    expect(cards.map((card) => card.style.height), "every card should be 180px, the tallest card's height").toEqual(["180px", "180px", "180px"]);
  });

  it("measures each card once", () => {
    const { cards, log } = makeCards([120, 180, 150]);
    equalHeights(cards);
    expect(log.reads, "three cards need three reads of offsetHeight").toBe(3);
  });

  it("clears every height before it measures any card", () => {
    const { cards, log } = makeCards([120, 180, 150]);
    equalHeights(cards);
    expect(log.readsMixedWithWrites, "a card was measured while another card still had a height set; clear them all first, then read them all, so the browser lays out only once").toBe(0);
  });

  it("does nothing for an empty grid", () => {
    expect(() => equalHeights([]), "an empty array of cards should not throw").not.toThrow();
  });
});

/** A pretend frame queue: raf stores callbacks, runNext runs the oldest one. */
function fakeFrames() {
  const waiting = [];
  return {
    raf: (callback) => {
      waiting.push(callback);
      return waiting.length;
    },
    count: () => waiting.length,
    runNext: () => {
      const callback = waiting.shift();
      if (callback) {
        callback(16);
      }
    },
  };
}

describe("createRenderScheduler", () => {
  it("waits for the next frame instead of drawing straight away", () => {
    const frames = fakeFrames();
    const draws = [];
    const schedule = createRenderScheduler((state) => draws.push(state), frames.raf);
    schedule({ search: "g" });
    expect(draws, "nothing should be drawn until the frame comes").toEqual([]);
    expect(frames.count(), "schedule should ask raf for one frame").toBe(1);
  });

  it("draws once per frame, with the newest state", () => {
    const frames = fakeFrames();
    const draws = [];
    const schedule = createRenderScheduler((state) => draws.push(state), frames.raf);
    schedule({ search: "g" });
    schedule({ search: "go" });
    schedule({ search: "golu" });
    expect(frames.count(), "three calls before the frame should ask for only one frame").toBe(1);
    frames.runNext();
    expect(draws, "the frame should draw once, with the newest search").toEqual([{ search: "golu" }]);
  });

  it("asks for a new frame once the last one has drawn", () => {
    const frames = fakeFrames();
    const draws = [];
    const schedule = createRenderScheduler((state) => draws.push(state), frames.raf);
    schedule({ search: "golu" });
    frames.runNext();
    schedule({ search: "golu " });
    expect(frames.count(), "after a frame has drawn, the next call should ask for a new frame").toBe(1);
    frames.runNext();
    expect(draws, "two frames should make two draws").toEqual([{ search: "golu" }, { search: "golu " }]);
  });
});

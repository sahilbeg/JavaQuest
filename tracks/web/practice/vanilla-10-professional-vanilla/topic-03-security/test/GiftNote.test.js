import { describe, it, expect, beforeEach } from "vitest";
import { escapeHtml, packingSlipHtml, showGiftNote, nextPage } from "../src/giftNote.js";

const ATTACK = `Happy Diwali! <img src="x" onerror="window.hacked = true">`;
const kindNote = { to: "Asha & Ravi", message: "Happy Diwali! Love, from all of us.", from: "Meera" };
const attackNote = { to: "Raju", message: ATTACK, from: "A stranger" };

/** Turns an HTML string into elements inside a box, the way the printer page would. */
function parse(html) {
  const box = document.createElement("div");
  box.innerHTML = html;
  return box;
}

describe("escapeHtml", () => {
  it("turns every special character into an entity", () => {
    expect(escapeHtml(`<b>"Hi" & 'bye'</b>`), "< > \" & and ' should all become entities").toBe("&lt;b&gt;&quot;Hi&quot; &amp; &#39;bye&#39;&lt;/b&gt;");
  });

  it("escapes & first, so no entity is escaped twice", () => {
    expect(escapeHtml("Tom & Jerry <3"), "Tom & Jerry <3 should become Tom &amp; Jerry &lt;3, not &amp;lt;3").toBe("Tom &amp; Jerry &lt;3");
  });

  it("leaves ordinary text alone", () => {
    expect(escapeHtml("Happy Diwali, Aai!"), "text with no special characters should not change").toBe("Happy Diwali, Aai!");
  });
});

describe("packingSlipHtml", () => {
  it("shows a kind note exactly as it was written", () => {
    const slip = parse(packingSlipHtml(kindNote));
    expect(slip.querySelector(".to")?.textContent, "the slip should say For Asha & Ravi").toBe("For Asha & Ravi");
    expect(slip.querySelector(".message")?.textContent, "the message should be unchanged").toBe("Happy Diwali! Love, from all of us.");
    expect(slip.querySelector(".from")?.textContent, "the slip should say From Meera").toBe("From Meera");
  });

  it("never turns the message into an element", () => {
    const slip = parse(packingSlipHtml(attackNote));
    expect(slip.querySelector("img"), "the <img> in the message must not become a real image; escape the message").toBeNull();
    expect(slip.querySelector(".message")?.textContent, "the slip should show the message's characters exactly as typed").toBe(ATTACK);
  });

  it("escapes the names as well as the message", () => {
    const slip = parse(packingSlipHtml({ to: "</p><script>window.hacked = true</script>", message: "Hi", from: "<b>Sneha</b>" }));
    expect(slip.querySelector("script"), "a <script> in the To name must stay text; escape note.to").toBeNull();
    expect(slip.querySelector("b"), "a <b> in the From name must stay text; escape note.from").toBeNull();
    expect(slip.querySelector(".from")?.textContent, "the From line should show the name's characters").toBe("From <b>Sneha</b>");
  });
});

describe("showGiftNote", () => {
  let card;

  beforeEach(() => {
    document.body.innerHTML = `<div id="card"><p class="to"></p><p class="message"></p><p class="from"></p></div>`;
    card = document.querySelector("#card");
  });

  it("fills in the card", () => {
    showGiftNote(card, kindNote);
    expect(card.querySelector(".to").textContent, "the card should say For Asha & Ravi").toBe("For Asha & Ravi");
    expect(card.querySelector(".message").textContent, "the card should show the message").toBe("Happy Diwali! Love, from all of us.");
    expect(card.querySelector(".from").textContent, "the card should say From Meera").toBe("From Meera");
  });

  it("shows tags in a note as plain characters", () => {
    showGiftNote(card, attackNote);
    expect(card.querySelector("img"), "the note must not create an <img> on the page; use textContent").toBeNull();
    expect(card.querySelector(".message").textContent, "the card should show the angle brackets as text").toBe(ATTACK);
  });

  it("doesn't show entities to the customer", () => {
    showGiftNote(card, { to: "Tom & Jerry", message: "5 < 6", from: "Golu" });
    expect(card.querySelector(".to").textContent, "the card should say Tom & Jerry, not Tom &amp; Jerry").toBe("For Tom & Jerry");
    expect(card.querySelector(".message").textContent, "the card should say 5 < 6").toBe("5 < 6");
  });
});

describe("nextPage", () => {
  it("follows a path on this site", () => {
    expect(nextPage("?next=/cart"), "?next=/cart should go to /cart").toBe("/cart");
    expect(nextPage("?next=%2Forders%2Forder-17"), "an encoded path should be decoded to /orders/order-17").toBe("/orders/order-17");
  });

  it("goes home when there is no next", () => {
    expect(nextPage(""), "no ?next= should go to /").toBe("/");
    expect(nextPage("?from=whatsapp"), "a search without next should go to /").toBe("/");
  });

  it("refuses javascript: addresses", () => {
    expect(nextPage("?next=javascript:alert(document.cookie)"), "javascript: would run code, so go to / instead").toBe("/");
    expect(nextPage("?next=%20javascript:alert(1)"), "a space before javascript: doesn't make it a path; go to /").toBe("/");
  });

  it("refuses other websites", () => {
    expect(nextPage("?next=https://dhaaga-offers.example/login"), "a full address to another site should go to /").toBe("/");
    expect(nextPage("?next=//dhaaga-offers.example"), "// starts another website, so go to /").toBe("/");
    expect(nextPage("?next=/%5Cdhaaga-offers.example"), "/\\ is read like // by browsers, so go to /").toBe("/");
  });
});

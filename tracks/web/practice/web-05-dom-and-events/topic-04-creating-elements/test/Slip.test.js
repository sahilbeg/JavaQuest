import { describe, it, expect, beforeEach } from "vitest";
import { slipLine, addLine, removeLine, clearSlip } from "../src/slip.js";

beforeEach(() => {
  document.body.innerHTML = `
    <ul id="slip">
      <li class="line" data-code="CST"><span class="name">Coaster set (4)</span> × <span class="qty">1</span></li>
      <li class="line" data-code="BTY"><span class="name">Baby booties</span> × <span class="qty">2</span></li>
    </ul>
    <ul id="empty-slip"></ul>
    <ul id="saved">
      <li class="line" data-code="AMI"><span class="name">Amigurumi</span> × <span class="qty">1</span></li>
    </ul>`;
});

const slip = () => document.querySelector("#slip");
const emptySlip = () => document.querySelector("#empty-slip");
const codes = (list) => [...list.children].map((li) => li.dataset.code);
const KEYCHAIN = { code: "KEY", name: "Keychain", qty: 2 };

describe("slipLine", () => {
  it("makes an <li> with the class line and the item's code", () => {
    const li = slipLine(KEYCHAIN);
    expect(li?.tagName, "slipLine should return an <li> element").toBe("LI");
    expect(li?.classList.contains("line"), "the <li> should have the class line").toBe(true);
    expect(li?.dataset.code, "the <li> should have data-code=\"KEY\"").toBe("KEY");
  });

  it("shows the name and the quantity in their own spans", () => {
    const li = slipLine(KEYCHAIN);
    expect(li?.querySelector("span.name")?.textContent, "the .name span should say Keychain").toBe("Keychain");
    expect(li?.querySelector("span.qty")?.textContent, "the .qty span should say 2").toBe("2");
  });

  it("adds a Remove button labelled for screen readers", () => {
    const button = slipLine(KEYCHAIN)?.querySelector("button.remove");
    expect(button?.textContent, "the button should say Remove").toBe("Remove");
    expect(button?.getAttribute("type"), "the button should have type=\"button\"").toBe("button");
    expect(button?.getAttribute("aria-label"), "the button's aria-label should be Remove Keychain").toBe("Remove Keychain");
  });

  it("does not put the line on the page by itself", () => {
    const li = slipLine(KEYCHAIN);
    expect(li !== null && li !== undefined && !li.isConnected, "slipLine should return a detached line; adding it is addLine's job").toBe(true);
    expect(slip().children.length, "the slip should still have its 2 lines").toBe(2);
  });

  it("shows tags in a name as plain text", () => {
    const li = slipLine({ code: "BAG", name: "Tote <b>bag</b>", qty: 1 });
    expect(li?.querySelector(".name")?.textContent, "the name should appear exactly as given").toBe("Tote <b>bag</b>");
    expect(li?.querySelector("b"), "no real <b> element should appear: use textContent").toBe(null);
  });
});

describe("addLine", () => {
  it("adds the new line at the end of the slip", () => {
    addLine(slip(), KEYCHAIN);
    expect(codes(slip()), "the keychain line should come after the two lines already there").toEqual(["CST", "BTY", "KEY"]);
  });

  it("returns the line it added", () => {
    const line = addLine(slip(), KEYCHAIN);
    expect(line !== null && line === slip().lastElementChild, "addLine should return the same <li> it put on the slip").toBe(true);
  });

  it("keeps lines in the order they were added", () => {
    addLine(emptySlip(), KEYCHAIN);
    addLine(emptySlip(), { code: "AMI", name: "Amigurumi", qty: 1 });
    expect(codes(emptySlip()), "Keychain was added first, then Amigurumi").toEqual(["KEY", "AMI"]);
  });
});

describe("removeLine", () => {
  it("removes the booties line and says it did", () => {
    expect(removeLine(slip(), "BTY"), "removing a line that is there should return true").toBe(true);
    expect(codes(slip()), "only the coaster set line should be left").toEqual(["CST"]);
  });

  it("returns false and changes nothing for a code that is not on the slip", () => {
    expect(removeLine(slip(), "BAG"), "there is no BAG line, so the answer is false").toBe(false);
    expect(codes(slip()), "both lines should still be there").toEqual(["CST", "BTY"]);
  });

  it("only removes lines from the slip it is given", () => {
    expect(removeLine(slip(), "AMI"), "the amigurumi line is in #saved, not on the slip, so the answer is false").toBe(false);
    expect(document.querySelectorAll('#saved [data-code="AMI"]').length, "the line in #saved must stay").toBe(1);
  });
});

describe("clearSlip", () => {
  it("removes every line but keeps the slip", () => {
    clearSlip(slip());
    expect(slip()?.children.length, "the slip should have no lines left").toBe(0);
    expect(document.querySelector("#saved li") !== null, "lines in other lists must stay").toBe(true);
  });
});

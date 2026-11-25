import { describe, it, expect } from "vitest";
import { pinText, contactLine, itemCount, pickupDate, giftNote } from "../src/delivery.ts";

describe("Delivery details, narrowed before use", () => {
  it("writes a PIN code given as a number or as text", () => {
    expect(pinText(411001), "the number 411001 should become the text \"411001\"").toBe("411001");
    expect(pinText("411 001"), "\"411 001\" should lose its space: \"411001\"").toBe("411001");
    expect(pinText("411045"), "\"411045\" has no spaces and stays as it is").toBe("411045");
  });

  it("writes a contact line for a phone or an email", () => {
    expect(contactLine({ phone: "98765 43210" }), "a phone contact should read \"Phone: 98765 43210\"").toBe("Phone: 98765 43210");
    expect(contactLine({ email: "asha@example.com" }), "an email contact should read \"Email: asha@example.com\"").toBe(
      "Email: asha@example.com",
    );
  });

  it("counts the items in a list or a single code", () => {
    expect(itemCount(["CST", "KEY", "AMI"]), "a list of three codes counts 3").toBe(3);
    expect(itemCount("BAG"), "a single code on its own counts 1, not its 3 letters").toBe(1);
    expect(itemCount([]), "an empty list counts 0").toBe(0);
  });

  it("writes a pickup date from a Date or from text", () => {
    expect(pickupDate(new Date("2026-11-08T10:30:00Z")), "the Date for 8 November 2026 should become \"2026-11-08\"").toBe("2026-11-08");
    expect(pickupDate("2026-11-14"), "text is already a date and should stay \"2026-11-14\"").toBe("2026-11-14");
  });

  it("writes a gift note, or says there is none", () => {
    expect(giftNote("  Happy Diwali, Asha!  "), "the note should be trimmed: \"Note: Happy Diwali, Asha!\"").toBe("Note: Happy Diwali, Asha!");
    expect(giftNote(null), "null means no note was written: \"No note\"").toBe("No note");
    expect(giftNote("   "), "a note of only spaces is no note: \"No note\"").toBe("No note");
  });
});

import { describe, it, expect } from "vitest";
import { itemLabel, readyMessage, cleanCode, initials } from "../src/labels.js";

describe("Labels and messages", () => {
  it("builds an item label from a code and a name", () => {
    expect(itemLabel("AMI", "Amigurumi"), 'itemLabel("AMI", "Amigurumi") should be "AMI: Amigurumi"').toBe("AMI: Amigurumi");
    expect(itemLabel("BAG", "Tote bag"), 'itemLabel("BAG", "Tote bag") should be "BAG: Tote bag"').toBe("BAG: Tote bag");
  });

  it("writes the WhatsApp message for an order that is ready", () => {
    expect(
      readyMessage("Asha", "order-17"),
      'readyMessage("Asha", "order-17") should be "Hi Asha, order-17 is ready at the FC Road stall."',
    ).toBe("Hi Asha, order-17 is ready at the FC Road stall.");
    expect(
      readyMessage("Mr. Joshi", "order-42"),
      'readyMessage("Mr. Joshi", "order-42") should be "Hi Mr. Joshi, order-42 is ready at the FC Road stall."',
    ).toBe("Hi Mr. Joshi, order-42 is ready at the FC Road stall.");
  });

  it("cleans up an item code that a customer typed", () => {
    expect(cleanCode("  ami "), 'cleanCode("  ami ") should drop the spaces and give "AMI"').toBe("AMI");
    expect(cleanCode("Bag"), 'cleanCode("Bag") should give "BAG"').toBe("BAG");
    expect(cleanCode("CST"), 'cleanCode("CST") is already clean, so it stays "CST"').toBe("CST");
  });

  it("works out the initials to stitch on baby booties", () => {
    expect(initials("riya", "fernandes"), 'initials("riya", "fernandes") should be "RF"').toBe("RF");
    expect(initials("Arjun", "Mehta"), 'initials("Arjun", "Mehta") should be "AM"').toBe("AM");
  });
});

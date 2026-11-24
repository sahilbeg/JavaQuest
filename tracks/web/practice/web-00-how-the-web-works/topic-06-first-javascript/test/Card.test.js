import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { welcomeLine, printCard } from "../src/card.js";

const WELCOME = "Namaste! Welcome to Dhaaga & Co.";
const MAKES = "Handmade crochet from Pune.";
const STALL = "Stall: FC Road, Saturday and Sunday, 10:00 to 20:00";

let log;

beforeEach(() => {
  log = vi.spyOn(console, "log").mockImplementation(() => {});
});

afterEach(() => {
  log.mockRestore();
});

/** Runs printCard() and returns each line it printed, as text. */
function printedLines() {
  printCard();
  return log.mock.calls.map((args) => args.join(" "));
}

describe("welcomeLine", () => {
  it("returns the welcome text exactly", () => {
    expect(welcomeLine(), `welcomeLine() should return ${WELCOME}`).toBe(WELCOME);
  });
});

describe("printCard", () => {
  it("prints exactly three lines", () => {
    expect(printedLines().length, "printCard() should call console.log three times, once per line").toBe(3);
  });

  it("prints the welcome line first", () => {
    expect(printedLines()[0], `the first line should be ${WELCOME}`).toBe(WELCOME);
  });

  it("prints what Dhaaga makes second", () => {
    expect(printedLines()[1], `the second line should be ${MAKES}`).toBe(MAKES);
  });

  it("prints where and when to find the stall third", () => {
    expect(printedLines()[2], `the third line should be ${STALL}`).toBe(STALL);
  });
});

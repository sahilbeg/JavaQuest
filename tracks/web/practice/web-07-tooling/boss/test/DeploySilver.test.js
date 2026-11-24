import { describe, it, expect } from "vitest";
import { requireEnv } from "../src/env.js";

const NEEDED = ["VITE_API_URL", "VITE_UPI_ID"];

/** The message of the error fn throws, or null when it does not throw. */
function errorMessage(fn) {
  try {
    fn();
    return null;
  } catch (error) {
    return error.message;
  }
}

describe("requireEnv", () => {
  it("returns the values that are there", () => {
    const env = { MODE: "production", VITE_API_URL: "https://api.dhaaga.in", VITE_UPI_ID: "dhaaga@upi" };
    expect(requireEnv(env, NEEDED), "only the names asked for, with their values").toEqual({
      VITE_API_URL: "https://api.dhaaga.in",
      VITE_UPI_ID: "dhaaga@upi",
    });
  });

  it("trims spaces around values", () => {
    const env = { VITE_API_URL: " https://api.dhaaga.in ", VITE_UPI_ID: "dhaaga@upi  " };
    expect(requireEnv(env, NEEDED).VITE_UPI_ID, "\"dhaaga@upi  \" should become \"dhaaga@upi\"").toBe("dhaaga@upi");
  });

  it("refuses to go on when the UPI ID never reached the server", () => {
    const env = { MODE: "production", VITE_API_URL: "https://api.dhaaga.in" };
    const message = errorMessage(() => requireEnv(env, NEEDED));
    expect(message, "a missing VITE_UPI_ID should throw, not give pa=undefined").not.toBeNull();
    expect(message ?? "", "the error should name VITE_UPI_ID").toContain("VITE_UPI_ID");
  });

  it("counts a blank value as missing", () => {
    const env = { VITE_API_URL: "https://api.dhaaga.in", VITE_UPI_ID: "   " };
    expect(errorMessage(() => requireEnv(env, NEEDED)) ?? "", "a blank VITE_UPI_ID is as bad as none").toContain(
      "VITE_UPI_ID",
    );
  });

  it("names every missing value at once, in the order asked", () => {
    const message = errorMessage(() => requireEnv({ MODE: "production" }, NEEDED));
    expect(message ?? "", "both missing names should be listed together").toContain("VITE_API_URL, VITE_UPI_ID");
  });
});

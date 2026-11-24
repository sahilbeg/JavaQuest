import { describe, it, expect } from "vitest";
import { loadConfig } from "../src/config.js";

const live = {
  MODE: "production",
  VITE_API_URL: "https://api.dhaaga.in",
  VITE_FREE_DELIVERY_ABOVE: "99900",
  VITE_SHOW_DIWALI: "false",
};

describe("loadConfig defaults", () => {
  it("fills in safe defaults on a laptop with no settings", () => {
    expect(loadConfig({ MODE: "development" }), "development with no settings should use every default").toEqual({
      mode: "development",
      apiUrl: "http://localhost:3000",
      freeDeliveryAbovePaise: 99900,
      showDiwaliOffer: false,
    });
  });

  it("treats a missing MODE as development", () => {
    expect(loadConfig({}).mode, "an empty env should give mode development").toBe("development");
  });

  it("treats a blank setting like a missing one", () => {
    const config = loadConfig({ MODE: "development", VITE_API_URL: "   ", VITE_SHOW_DIWALI: "" });
    expect(config.apiUrl, "a blank VITE_API_URL should fall back to http://localhost:3000").toBe("http://localhost:3000");
    expect(config.showDiwaliOffer, "a blank VITE_SHOW_DIWALI should mean no banner").toBe(false);
  });
});

describe("loadConfig reads the environment", () => {
  it("uses the live API address in production", () => {
    expect(loadConfig(live).apiUrl, "production should use VITE_API_URL").toBe("https://api.dhaaga.in");
  });

  it("removes a trailing slash from the API address", () => {
    const config = loadConfig({ ...live, VITE_API_URL: "https://staging-api.dhaaga.in/" });
    expect(config.apiUrl, "https://staging-api.dhaaga.in/ should lose its last /").toBe("https://staging-api.dhaaga.in");
  });

  it("turns the free-delivery limit into a number", () => {
    const config = loadConfig({ ...live, VITE_FREE_DELIVERY_ABOVE: "149900" });
    expect(config.freeDeliveryAbovePaise, "\"149900\" should become the number 149900").toBe(149900);
    expect(typeof config.freeDeliveryAbovePaise, "the limit should be a number, not text").toBe("number");
  });

  it("turns \"true\" into true", () => {
    expect(loadConfig({ ...live, VITE_SHOW_DIWALI: "true" }).showDiwaliOffer, "\"true\" should switch the banner on").toBe(true);
  });

  it("turns \"false\" into false, even though the text \"false\" is truthy", () => {
    expect(loadConfig(live).showDiwaliOffer, "\"false\" should keep the banner off").toBe(false);
  });

  it("ignores spaces around values", () => {
    const config = loadConfig({ ...live, VITE_FREE_DELIVERY_ABOVE: " 99900 ", VITE_SHOW_DIWALI: " true " });
    expect(config.freeDeliveryAbovePaise, "\" 99900 \" should read as 99900").toBe(99900);
    expect(config.showDiwaliOffer, "\" true \" should read as true").toBe(true);
  });
});

describe("loadConfig refuses bad settings", () => {
  it("refuses to start in production without an API address", () => {
    const { VITE_API_URL, ...noAddress } = live;
    expect(() => loadConfig(noAddress), "production with no VITE_API_URL should throw an Error naming it").toThrow(
      /VITE_API_URL/,
    );
  });

  it("refuses a blank API address in production", () => {
    expect(() => loadConfig({ ...live, VITE_API_URL: "  " }), "a blank VITE_API_URL in production should throw").toThrow(
      /VITE_API_URL/,
    );
  });

  it("refuses a free-delivery limit that is not whole paise", () => {
    for (const bad of ["₹999", "99.9", "-5", "lots"]) {
      expect(
        () => loadConfig({ ...live, VITE_FREE_DELIVERY_ABOVE: bad }),
        `VITE_FREE_DELIVERY_ABOVE="${bad}" should throw an Error naming the setting`,
      ).toThrow(/VITE_FREE_DELIVERY_ABOVE/);
    }
  });

  it("refuses a Diwali flag that is not true or false", () => {
    for (const bad of ["yes", "ture", "1"]) {
      expect(
        () => loadConfig({ ...live, VITE_SHOW_DIWALI: bad }),
        `VITE_SHOW_DIWALI="${bad}" should throw an Error naming the setting`,
      ).toThrow(/VITE_SHOW_DIWALI/);
    }
  });
});

describe("the finished config", () => {
  it("is frozen, so no code can change a setting later", () => {
    expect(Object.isFrozen(loadConfig(live)), "loadConfig should return Object.freeze(...)").toBe(true);
  });
});

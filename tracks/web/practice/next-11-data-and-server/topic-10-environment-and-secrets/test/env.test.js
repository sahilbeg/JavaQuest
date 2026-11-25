import { describe, it, expect } from "vitest";
import { readServerConfig, publicConfig, leakedSecrets } from "../src/lib/env.js";

const GOOD = {
  UPI_GATEWAY_KEY: "test_gateway_key",
  ORDERS_DB_URL: "postgres://localhost/dhaaga_dev",
  NEXT_PUBLIC_SITE_URL: "https://staging.dhaaga.in",
  NEXT_PUBLIC_UPI_ID: "dhaaga.test@upi",
};

describe("readServerConfig", () => {
  it("gives the server's settings", () => {
    expect(readServerConfig(GOOD), "with both set, it should return { upiKey, dbUrl }").toEqual({
      upiKey: "test_gateway_key",
      dbUrl: "postgres://localhost/dhaaga_dev",
    });
  });

  it("fails fast, naming a missing setting", () => {
    const { UPI_GATEWAY_KEY, ...withoutKey } = GOOD;
    expect(() => readServerConfig(withoutKey), "a missing UPI_GATEWAY_KEY should throw, naming it").toThrow(
      "Missing environment variables: UPI_GATEWAY_KEY.",
    );
  });

  it("treats an empty value as missing, and names every missing setting at once", () => {
    expect(() => readServerConfig({ UPI_GATEWAY_KEY: "" }), "both settings are missing or empty, so both should be named").toThrow(
      "Missing environment variables: UPI_GATEWAY_KEY, ORDERS_DB_URL.",
    );
  });
});

describe("publicConfig", () => {
  it("reads only the public settings", () => {
    expect(publicConfig(GOOD), "it should return { siteUrl, upiId } from the NEXT_PUBLIC_ variables").toEqual({
      siteUrl: "https://staging.dhaaga.in",
      upiId: "dhaaga.test@upi",
    });
  });

  it("uses the shop's defaults when they're not set", () => {
    expect(publicConfig({}), "with nothing set, use https://dhaaga.in and dhaaga@upi").toEqual({ siteUrl: "https://dhaaga.in", upiId: "dhaaga@upi" });
  });
});

describe("leakedSecrets", () => {
  it("finds public names that look secret, sorted", () => {
    const env = {
      ...GOOD,
      NEXT_PUBLIC_UPI_GATEWAY_KEY: "oops",
      NEXT_PUBLIC_ADMIN_PASSWORD: "oops",
      NEXT_PUBLIC_MAPS_TOKEN: "hmm",
      WEBHOOK_SECRET: "fine, it's not public",
    };
    expect(leakedSecrets(env), "three NEXT_PUBLIC_ names contain KEY, PASSWORD or TOKEN").toEqual([
      "NEXT_PUBLIC_ADMIN_PASSWORD",
      "NEXT_PUBLIC_MAPS_TOKEN",
      "NEXT_PUBLIC_UPI_GATEWAY_KEY",
    ]);
  });

  it("finds nothing in safe settings", () => {
    expect(leakedSecrets(GOOD), "none of the normal settings is a leak").toEqual([]);
  });
});

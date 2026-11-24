// Kavya's pre-deploy check. It should fail on every laptop exactly where the deploy server would.
import { resolveImport } from "./imports.js";
import { requireEnv } from "./env.js";
import { assetUrl } from "./assets.js";

// What the deploy server has: the committed files, and the settings it was given.
const serverFiles = ["index.html", "src/main.js", "src/cart.js", "src/lib/money.js", "public/images/golu.png"];
const serverEnv = { MODE: "production", VITE_API_URL: "https://api.dhaaga.in" };

function stage(name, work) {
  try {
    console.log(`${name}: GREEN (${work()})`);
  } catch (error) {
    console.log(`${name}: RED (${error.message})`);
  }
}

stage("imports", () => resolveImport("src/main.js", "./Cart.js", serverFiles));
stage("environment", () => Object.keys(requireEnv(serverEnv, ["VITE_API_URL", "VITE_UPI_ID"])).join(", "));
stage("images", () => assetUrl("/shop/", "images\\golu.png"));

// Builds a ticket's page for the in-app preview.
// Usage: node tools/preview.mjs <src folder> <out folder>
// Copies every file from src. If there is a main.js or main.jsx, it is bundled (with React and any CSS it imports)
// into out/main.js and out/main.css. Without an index.html, a plain page with <div id="root"> is written.
import { build } from "esbuild";
import { cp, mkdir, rm, stat, writeFile } from "node:fs/promises";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const [src, out] = process.argv.slice(2);
const workspace = join(dirname(fileURLToPath(import.meta.url)), "..");
const exists = (path) => stat(path).then(() => true, () => false);

await rm(out, { recursive: true, force: true });
await mkdir(out, { recursive: true });
await cp(src, out, { recursive: true });

const ENTRIES = ["main.tsx", "main.ts", "main.jsx", "main.js"];
let entry = null;
for (const name of ENTRIES) if (!entry && (await exists(join(src, name)))) entry = name;
if (entry) {
  await rm(join(out, entry), { force: true });
  const result = await build({
    entryPoints: [join(src, entry)],
    bundle: true,
    outfile: join(out, "main.js"),
    format: "esm",
    jsx: "automatic",
    loader: { ".js": "jsx", ".ts": "ts", ".tsx": "tsx" },
    nodePaths: [join(workspace, "node_modules")],
    define: { "process.env.NODE_ENV": '"development"' },
    logLevel: "silent",
  }).catch((failure) => ({ errors: failure.errors }));
  if (result.errors?.length) {
    process.stdout.write(JSON.stringify(result.errors.map((e) => ({ file: e.location?.file ?? entry, line: e.location?.line ?? 0, message: e.text }))));
    process.exit(1);
  }
}
if (!(await exists(join(out, "index.html")))) {
  const css = (await exists(join(out, "main.css"))) ? '\n  <link rel="stylesheet" href="main.css">' : "";
  await writeFile(join(out, "index.html"), `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Preview</title>${css}
</head>
<body>
  <div id="root"></div>${entry ? '\n  <script type="module" src="main.js"></script>' : ""}
</body>
</html>
`);
}
process.stdout.write("[]");

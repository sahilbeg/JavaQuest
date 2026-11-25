// Syntax-checks every .js/.jsx/.mjs file under a folder, like javac does for Java before the tests run.
// Usage: node tools/check.mjs <folder>. Prints a JSON list of { file, line, message } and exits 1 when there are errors.
import { transform } from "esbuild";
import { readdir, readFile } from "node:fs/promises";
import { join, relative, extname } from "node:path";

const root = process.argv[2];
const LOADERS = { ".js": "jsx", ".jsx": "jsx", ".mjs": "js", ".ts": "ts", ".tsx": "tsx" };

async function files(dir) {
  const out = [];
  for (const entry of await readdir(dir, { withFileTypes: true })) {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) out.push(...(await files(path)));
    else if (LOADERS[extname(entry.name)]) out.push(path);
  }
  return out;
}

const errors = [];
for (const file of await files(root)) {
  try {
    await transform(await readFile(file, "utf8"), { loader: LOADERS[extname(file)], jsx: "automatic", sourcefile: file, logLevel: "silent" });
  } catch (failure) {
    for (const e of failure.errors ?? [{ text: String(failure) }]) {
      errors.push({ file: relative(root, file), line: e.location?.line ?? 0, message: e.text });
    }
  }
}
process.stdout.write(JSON.stringify(errors));
process.exit(errors.length ? 1 : 0);

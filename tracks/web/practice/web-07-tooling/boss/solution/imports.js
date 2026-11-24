/** Finished: the path an import points at. joinPath("src/components/Drawer.js", "../cart.js") is "src/cart.js". */
export function joinPath(fromFile, specifier) {
  const parts = fromFile.split("/").slice(0, -1);
  for (const part of specifier.split("/")) {
    if (part === "." || part === "") continue;
    if (part === "..") parts.pop();
    else parts.push(part);
  }
  return parts.join("/");
}

/**
 * The file an import in fromFile points at, from the list of files the deploy server has.
 * resolveImport("src/main.js", "./cart.js", ["src/main.js", "src/cart.js"]) is "src/cart.js".
 */
export function resolveImport(fromFile, specifier, files) {
  const wanted = joinPath(fromFile, specifier);

  const found = files.find((file) => file === wanted);
  if (found !== undefined) {
    return found;
  }
  const nearly = files.find((file) => file.toLowerCase() === wanted.toLowerCase());
  const hint = nearly === undefined ? "" : ` (did you mean ${nearly}?)`;
  throw new Error(`Cannot find "${specifier}" from ${fromFile}${hint}`);
}

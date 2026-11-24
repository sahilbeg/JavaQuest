# INC-07: The build breaks only on deploy

The deploy of dhaaga.in is red in three places, and all three pass on Kavya's Mac. The pre-deploy check in `src/main.js` is as forgiving as her Mac. Make its three functions as strict as the Linux deploy server. Press Run to see the check's report.

## Bronze: `src/imports.js` (resolves the incident)

`resolveImport(fromFile, specifier, files)` turns an import like `"./cart.js"` in `src/main.js` into a file path from `files`.

- The finished `joinPath(fromFile, specifier)` already works out the wanted path, like `src/cart.js`.
- Return the file from `files` that equals it exactly, capitals included.
- If none does, throw an `Error` whose message starts `Cannot find`. If a file matches when capitals are ignored, add `did you mean <that file>?` to the message, for example `did you mean src/cart.js?`.

## Silver: `src/env.js`

`requireEnv(env, names)` returns `{ name: trimmed value }` for each name asked for, and no other keys.

- A value that is missing or blank counts as missing. The finished `read(env, name)` helps.
- If any are missing, throw one `Error` naming every missing one, in the order asked, separated by `, ` (for example `VITE_API_URL, VITE_UPI_ID`).

## Gold: `src/assets.js`

`assetUrl(base, path)` joins the site's base, like `"/"` or `"/shop/"`, and an image path from the catalogue.

- Every `\` becomes `/`.
- A leading `./` or `/` on the path is dropped, and the base gets exactly one `/` at its end.
- A path from one computer throws an `Error` whose message contains `one computer`: a drive letter (`C:\...`, `D:/...`), a path starting `/Users/` or `/home/`, or anything starting `file:`.

| base | path | result |
|---|---|---|
| `"/"` | `"images/golu.png"` | `"/images/golu.png"` |
| `"/shop/"` | `"images\\diwali\\banner.png"` | `"/shop/images/diwali/banner.png"` |
| `"/shop"` | `"./images/golu.png"` | `"/shop/images/golu.png"` |

The checks are split into `DeployBronze`, `DeploySilver` and `DeployGold` test files.

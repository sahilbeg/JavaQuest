// The Diwali catalogue: Golu, plus every item in every yarn colour. Prices are in paise.
const COLOURS = ["Marigold", "Indigo", "Rani pink", "Mint", "Off-white"];
const KINDS = [
  { name: "coaster set", paise: 15000 },
  { name: "keychain", paise: 12000 },
  { name: "amigurumi bunny", paise: 45000 },
  { name: "tote bag", paise: 89900 },
  { name: "baby booties", paise: 35000 },
];

export const PRODUCTS = [{ id: "DC-1042", name: "Golu the elephant", paise: 45000 }];

KINDS.forEach((kind, k) => {
  COLOURS.forEach((colour, c) => {
    PRODUCTS.push({ id: `DC-2${k}${c}0`, name: `${colour} ${kind.name}`, paise: kind.paise });
  });
});

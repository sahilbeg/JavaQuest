import { listItems } from "../lib/catalogue.js";

// Next.js serves this list at /sitemap.xml, so search engines find every page.
export default async function sitemap() {
  const items = await listItems();
  return [
    { url: "https://dhaaga.in/", changeFrequency: "daily", priority: 1 },
    ...items.map((item) => ({ url: `https://dhaaga.in/items/${item.id}`, changeFrequency: "weekly", priority: 0.8 })),
  ];
}

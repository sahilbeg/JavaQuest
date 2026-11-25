// Next.js serves this at /robots.txt: the rules for search engine crawlers.
export default function robots() {
  return {
    rules: { userAgent: "*", allow: "/", disallow: ["/cart", "/checkout"] },
    sitemap: "https://dhaaga.in/sitemap.xml",
  };
}

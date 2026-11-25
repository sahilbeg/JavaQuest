// Next.js serves this at /robots.txt: the rules for search engine crawlers.
export default function robots() {
  // TODO 4 (also): this was copied from the preview site and blocks every page.
  // Allow "/", disallow ["/cart", "/checkout"], and point to "https://dhaaga.in/sitemap.xml".
  return {
    rules: { userAgent: "*", disallow: "/" },
  };
}

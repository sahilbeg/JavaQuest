// TODO 1: export the site's metadata: metadataBase, a title with a default and a template,
// a description and openGraph (the exact values are in README.md).
export const metadata = {};

/** The root layout: it wraps every page on dhaaga.in. */
export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <header>
          <p>Dhaaga & Co.</p>
          <p>Free delivery in Pune above ₹999</p>
        </header>
        <main>{children}</main>
        <footer>Handmade in Pune</footer>
      </body>
    </html>
  );
}

import "./globals.css";

/** The root layout: it wraps every page on dhaaga.in, and brings in the site-wide styles once. */
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

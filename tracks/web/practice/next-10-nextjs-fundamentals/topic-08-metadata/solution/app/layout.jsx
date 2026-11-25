export const metadata = {
  metadataBase: new URL("https://dhaaga.in"),
  title: {
    default: "Dhaaga & Co.",
    template: "%s | Dhaaga & Co.",
  },
  description: "Handmade crochet from Pune: coasters, keychains, toys, bags and booties.",
  openGraph: { siteName: "Dhaaga & Co.", locale: "en_IN", type: "website" },
};

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

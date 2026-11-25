import Link from "next/link";

export const metadata = {
  metadataBase: new URL("https://dhaaga.in"),
  title: { default: "Dhaaga & Co.", template: "%s | Dhaaga & Co." },
  description: "Handmade crochet from Pune, delivered across India.",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en-IN">
      <body>
        <a href="#main">Skip to content</a>
        <header>
          <nav aria-label="Main">
            <Link href="/">Shop</Link> <Link href="/cart">Cart</Link>
          </nav>
        </header>
        <main id="main">{children}</main>
      </body>
    </html>
  );
}

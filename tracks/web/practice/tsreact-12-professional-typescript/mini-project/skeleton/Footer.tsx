/** The bottom of every page: who we are, when we're open, and how to reach us. */
export function Footer() {
  return (
    <footer className="site-footer">
      <section id="about" aria-label="About us">
        <p>{"Dhaaga & Co."}: crochet made by hand in Pune, since the Sunday haat on FC Road.</p>
      </section>
      <section id="contact" aria-label="Contact">
        <p>The shop: Aundh, Pune. Open Tuesday to Sunday, 11:00 to 21:00.</p>
        <p>
          WhatsApp: <a href="https://wa.me/919876543210">98765 43210</a>
        </p>
        <p>We accept UPI, cards and cash.</p>
      </section>
    </footer>
  );
}

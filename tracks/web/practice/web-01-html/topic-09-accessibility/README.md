# Accessibility: pages everyone can use

Sana listened to the catalogue page with a screen reader and found four problems. Fix them in `index.html`.

1. **TODO 1:** the yarn border in Our story is only decoration. Give it an empty alt: `alt=""`.
2. **TODO 2:** the "click here" link to `story.html` makes no sense alone. Put the meaning inside the link, for example `<p><a href="story.html">Read how Meera Aunty started</a></p>`. The link's own text must mention Meera Aunty.
3. **TODO 3:** wrap the Hindi word `धागा` in `<span lang="hi">...</span>`, so it's read with a Hindi voice.
4. **TODO 4:** the WhatsApp icon in the footer is the only thing in its link. Give it an alt that says where the link goes, such as `Chat with us on WhatsApp`.

The last check makes sure every `<img>` on the page has an `alt` attribute, even an empty one.

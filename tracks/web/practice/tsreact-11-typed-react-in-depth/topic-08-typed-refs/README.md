# Typed refs

Make Sana's six PIN code boxes move the focus by themselves, both ways.

Change `src/PincodeBoxes.tsx`. The boxes are labelled `Digit 1` to `Digit 6`, and the digits are kept in state already.

1. **TODO 1:** keep the six input elements in one ref, `useRef<(HTMLInputElement | null)[]>([])`, and give each input a callback ref that stores it: `ref={(el) => { boxes.current[i] = el; }}`.
2. **TODO 2:** after the first render, put the focus in `Digit 1`.
3. **TODO 3:** typing a digit moves the focus to the next box. A letter is ignored (the box stays empty, and the focus stays).
4. **TODO 4:** when all six boxes hold a digit, call `onComplete` once with the six digits joined, like `"411007"`.
5. **TODO 5:** Backspace in an empty box (any box but the first) calls `event.preventDefault()` and moves the focus to the box before.

`src/main.tsx` shows the boxes in the preview, with the finished PIN code under them.

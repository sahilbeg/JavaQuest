# Refactoring safely

`InvoiceGenerator.generate` works, but it is one long method full of magic numbers and copy-paste. Clean it up without changing a single character of its output. Money is in `long` cents.

Three of the tests are a **golden master**. They compare the whole invoice text with the exact output the old code gives. They already pass on the starting code. That is the point: they pin today's behaviour, so you know at once if a change breaks it. Keep them green after every small step.

The other three tests check the new shape, and they fail until you build it.

1. **TODO 1:** turn `InvoiceLine` into a record: `public record InvoiceLine(String bikeId, String type, int minutes, long cents)`.
2. **TODO 2:** write `lines(rides, membership)`. It returns one `InvoiceLine` per ride, with the same cents that `generate` works out.
3. **TODO 3:** write `format(line)`. It returns one ride line exactly as `generate` prints it, such as `B-1042 | classic | 12 min | $2.80`, with no newline.
4. **TODO 4:** rewrite `generate` so it uses `lines` and `format`. Extract small helpers and name the magic numbers as you go. Run the tests after every step.

The tests check the extracted `lines()` and `format()` and the unchanged output. They cannot see whether `generate` uses `lines` and `format`, or whether the magic numbers are gone. Check those two things in your own review.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

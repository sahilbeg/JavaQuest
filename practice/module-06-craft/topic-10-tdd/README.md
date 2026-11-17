# Test-driven development

Shizune needs the group ride discount right on day one. You build it test first.

The rules: 1 or 2 riders pay the full fare. 3 or 4 riders get 20 percent off. 5 or more riders get 30 percent off. Every result rounds down to whole cents. A group size below 1 throws `IllegalArgumentException`. Money is a `long` number of cents.

This exercise has two parts, and you write both.

**Part 1: the checks.** Fill in `GroupDiscountChecks.java`. Each method takes any `GroupDiscount` and uses `Assertions` to check one rule.

1. **TODO 1:** a solo rider and a pair pay the full fare.
2. **TODO 2:** 3 and 4 riders pay 80 percent.
3. **TODO 3:** 5 riders and a bigger group pay 70 percent.
4. **TODO 4:** a fare that leaves a fraction of a cent rounds down. Try 1234 cents.
5. **TODO 5:** a group of 0 and a negative group throw `IllegalArgumentException`.

**Part 2: the code.** Fill in `Groups.java`, one rule at a time, until your checks pass.

6. **TODO 6:** throw `IllegalArgumentException` when the group size is below 1.
7. **TODO 7:** apply 30 percent off for 5 or more and 20 percent off for 3 or 4.
8. **TODO 8:** return the full fare for 1 or 2 riders, and round down.

The hidden tests run your checks against a correct discount, against your `Groups`, and against four buggy discounts. Your checks must pass on the correct ones and catch every buggy one. A buggy discount counts as caught only when one of your assertions fails. Any other exception does not count. A separate test checks `Groups` directly.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

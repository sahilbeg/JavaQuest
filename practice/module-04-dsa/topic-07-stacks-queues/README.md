# Stacks and queues

Fix the kiosk's undo and the repair line. Build three small classes in `jq.m04.stacksqueues`.

**ActionStack** is a stack of kiosk actions. Build it yourself on a `String[]`. Do not use `ArrayList`, `ArrayDeque` or any other `java.util` collection inside it.

1. **TODO 1:** `push(action)` stores the action on top. When the array is full, make a new array twice as long and copy the items across first.
2. **TODO 2:** `pop()` removes and returns the newest action. On an empty stack it throws `IllegalStateException`.
3. **TODO 3:** `peek()` returns the newest action without removing it, or `null` when the stack is empty.
4. **TODO 4:** `isEmpty()` and `size()` report how many actions are stored.

**RepairQueue** is the line of broken bikes. Use the `ArrayDeque<String>` field it already has.

5. **TODO 5:** `report(bikeId)` adds the bike at the back of the line.
6. **TODO 6:** `next()` removes and returns the bike at the front, or `null` when nobody is waiting. `waiting()` returns how many bikes are in line.

**Brackets** checks text such as a kiosk config line.

7. **TODO 7:** `Brackets.balanced(text)` returns `true` when every `(`, `[` and `{` is closed by its partner in the right order. Use an `ArrayDeque<Character>` as a stack. Skip every other character.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

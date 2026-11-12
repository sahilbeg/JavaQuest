# Virtual threads

Make `MarathonPayments.java` pay every marathon rider at once, each on their own virtual thread. The method `pay(int rider)` is already written. It stands in for the slow payment service: it waits 5 ms and returns `true`.

1. **TODO 1 to 3:** in `payAll(int riders)`, open `Executors.newVirtualThreadPerTaskExecutor()` with try-with-resources. Submit one task per rider that calls `pay(rider)`. Count every `true` with an `AtomicInteger`, and return the count after the block.
2. **TODO 4 and 5:** in `ranOnVirtualThread(Runnable task)`, start a virtual thread that runs `task` and records `Thread.currentThread().isVirtual()`. Join it and return what it recorded.
3. **TODO 6 and 7:** in `payNamed(List<String> riderIds)`, submit one task per id that calls `pay(0)` and returns `"paid " + id`. Keep the futures in a list and read them in order, so the lines match the input order.
4. **TODO 8 and 9:** in `countTaps(int taps)`, submit `taps` virtual-thread tasks. Each task adds 1 to an `AtomicInteger` and does nothing else. Return the count after the block.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

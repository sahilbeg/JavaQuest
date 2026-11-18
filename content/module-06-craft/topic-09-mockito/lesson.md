## Hook

A film crew needs a shot of a car crash. They do not wreck a real star in a real car. They use a stunt double who looks the part, does exactly what the script says, and reports what happened.

Kakashi's unlock tests have no stunt doubles. Each run charges a real card and clicks open a real lock at Harbor. In this topic you learn to swap the payment provider and the lock for stand-ins. Your tests then run in a second, cost nothing, and need no bike in the room.

## Concept

### Test doubles

A **test double** is any object that stands in for a real one during a test. The name comes from the stunt double. There are five common kinds:

- A **dummy** fills a parameter and is never used. Think of a cardboard box passed in because the constructor needs something.
- A **stub** gives back canned answers. "When asked to charge, say pay-1."
- A **fake** has a real but simple working version, such as a payment gateway that stores charges in a `HashMap`.
- A **mock** records every call made on it, so the test can check those calls afterwards.
- A **spy** wraps a real object. Calls go to the real code, and the spy records them too.

```callout jargon
**Mockito** is a library that builds stubs, mocks and spies for you at runtime. You hand it an interface. It hands back an object that implements it, with every method doing nothing until you say otherwise.
```

Here is the code you are testing. It depends on three interfaces.

```java nocompile
interface PaymentGateway { String charge(String riderId, long cents); void refund(String paymentId); }
interface BikeLock { boolean open(String bikeId); }
interface Receipts { void send(String riderId, String paymentId); }
```

### Syntax: mock()

```java nocompile
BikeLock lock = mock(BikeLock.class);
```

An unstubbed mock returns a "nothing" value: `null` for most objects, empty for lists and `Optional`, `false` for `boolean`, `0` for numbers.

```java run
import static org.mockito.Mockito.mock;

public class Main {
    public static void main(String[] args) {
        BikeLock lock = mock(BikeLock.class);
        PaymentGateway gateway = mock(PaymentGateway.class);
        System.out.println(lock.open("B-1042"));
        System.out.println(gateway.charge("rider-7", 100));
    }
}

interface BikeLock { boolean open(String bikeId); }
interface PaymentGateway { String charge(String riderId, long cents); }
```

### Syntax: when(...).thenReturn and thenThrow

**Stubbing** means telling a mock what to answer.

```java nocompile
when(gateway.charge("rider-7", 100)).thenReturn("pay-1");
when(lock.open("B-1042")).thenReturn(false);
when(gateway.charge("rider-9", 100)).thenThrow(new PaymentDeclinedException("card declined"));
```

- `when(...)` names the call.
- `thenReturn(...)` gives the answer for that exact call.
- `thenThrow(...)` makes that call throw instead. This is how you test a declined card without a real bank.

### Syntax: doThrow for void methods

`when(...)` needs a return value to wrap, so it cannot hold a `void` call like `refund`. For void methods, Mockito flips the order: the answer first, then the mock, then the call.

```java nocompile
doThrow(new IllegalStateException("bank is down")).when(gateway).refund("pay-3");
```

```java run
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class Main {
    public static void main(String[] args) {
        PaymentGateway gateway = mock(PaymentGateway.class);
        doThrow(new IllegalStateException("bank is down")).when(gateway).refund("pay-3");

        gateway.refund("pay-1");
        System.out.println("pay-1 refunded quietly");
        try {
            gateway.refund("pay-3");
        } catch (IllegalStateException e) {
            System.out.println("pay-3 failed: " + e.getMessage());
        }
    }
}

interface PaymentGateway { String charge(String riderId, long cents); void refund(String paymentId); }
```

An unstubbed void method on a mock does nothing, so `pay-1` passes quietly. Only the stubbed call throws. Use this to test what your code does when a refund fails.

### Syntax: verify, times and never

**Verification** means checking afterwards that a call happened, or did not.

```java nocompile
verify(lock).open("B-1042");                       // called exactly once with B-1042
verify(gateway, times(1)).charge("rider-7", 100);  // same as above, said out loud
verify(receipts, never()).send(anyString(), anyString());
```

If the check fails, Mockito throws an error that says which call it wanted and which calls it saw.

### Syntax: argument matchers

A **matcher** accepts a whole family of arguments. `any()` and `anyString()` accept any value. `eq(x)` accepts only `x`.

```java nocompile
when(gateway.charge(anyString(), anyLong())).thenReturn("pay-1");
verify(gateway).charge(eq("rider-7"), eq(100L));
```

```callout warning
In one call, use matchers for every argument or for none. charge(anyString(), 100) is not allowed. Write charge(anyString(), eq(100L)) instead.
```

### Syntax: ArgumentCaptor

An **ArgumentCaptor** grabs the value that was passed to a mock, so you can look at it.

```java nocompile
ArgumentCaptor<String> paymentId = ArgumentCaptor.forClass(String.class);
verify(receipts).send(eq("rider-7"), paymentId.capture());
assertEquals("pay-42", paymentId.getValue());
```

### Syntax: @ExtendWith and @Mock

In a JUnit test class, Mockito can build the mocks for you.

```java nocompile
@ExtendWith(MockitoExtension.class)
class UnlockServiceTest {
    @Mock PaymentGateway gateway;
    @Mock BikeLock lock;
    @Mock Receipts receipts;
}
```

- `@ExtendWith(MockitoExtension.class)` plugs Mockito into JUnit.
- Each `@Mock` field gets a fresh mock before every test.

### Strict stubs: unused stubbing fails the test

`MockitoExtension` is strict. If you stub a call that your test never makes, the test fails, even when every assertion passed.

```java nocompile
when(gateway.charge("rider-7", 100)).thenReturn("pay-1");
when(lock.open("B-1042")).thenReturn(true);   // never used below

assertEquals("pay-1", gateway.charge("rider-7", 100));
```

```
org.mockito.exceptions.misusing.UnnecessaryStubbingException: 
Unnecessary stubbings detected.
Clean & maintainable test code requires zero unnecessary code.
Following stubbings are unnecessary (click to navigate to relevant line of code):
  1. -> at StrictTest.chargesTheRider(StrictTest.java:17)
Please remove unnecessary stubbings or use 'lenient' strictness. More info: javadoc for UnnecessaryStubbingException class.
```

This is a friend, not an enemy. An unused stub often means the code took a path you did not expect, or the test is copy-pasted clutter. Fix: delete the stub. Use `lenient().when(...)` only for a shared stub that some tests truly skip.

Strict stubs catch one more slip. If the code calls a stubbed method with different arguments, such as `charge("rider-8", 100)` instead of `"rider-7"`, the test fails with `PotentialStubbingProblem`. That usually means a typo in the test or a bug in the code.

### Mock interfaces you own

Mock the interfaces your own team wrote, such as `PaymentGateway`. Do not mock the bank's library directly. Your interface is small and describes what Spoke needs. The bank's library is large and can change under you.

### The danger of over-mocking

A test that mocks everything checks only that your code calls what it calls today. Rename one private step and the test breaks, though riders see no change. Mock the slow or costly edges: cards, locks, networks. Use real objects for plain logic like a fare rule or a record.

```callout tip
Gojo says: if a test has more when lines than assert lines, ask whether it still tests behaviour.
```

### Choosing a test double

Pick the simplest double that does the job, in this order:

1. **A real object**, when it is fast and has no side effects, like a fare rule.
2. **A fake**, when the real thing is slow or costly, but you want real behaviour.
3. **A stub**, when you only need a canned answer.
4. **A mock**, when the call itself is the result, such as "a refund was sent".

```callout jargon
**State verification** checks the result afterwards: a return value or a fake's contents. **Interaction verification** checks which calls happened, with `verify`. Prefer state; it breaks less often when the code inside changes.
```

```java run
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // a stub: canned answer, no logic
        BikeLock alwaysStuck = bikeId -> false;
        System.out.println("stub says open? " + alwaysStuck.open("B-1042"));

        // a fake: a small working version
        FakeGateway fake = new FakeGateway();
        String id = fake.charge("rider-7", 100);
        fake.refund(id);
        System.out.println("fake balance for " + id + ": " + fake.charges.get(id));

        // a spy: real code runs, and the calls are recorded
        FakeGateway spied = spy(new FakeGateway());
        spied.charge("rider-9", 250);
        verify(spied).charge("rider-9", 250);
        System.out.println("spy saw the charge, real code stored " + spied.charges);
    }
}

interface BikeLock { boolean open(String bikeId); }
interface PaymentGateway { String charge(String riderId, long cents); void refund(String paymentId); }

class FakeGateway implements PaymentGateway {
    final Map<String, Long> charges = new HashMap<>();

    public String charge(String riderId, long cents) {
        String id = "pay-" + (charges.size() + 1);
        charges.put(id, cents);
        return id;
    }

    public void refund(String paymentId) {
        charges.put(paymentId, 0L);
    }
}
```

A hand-written stub can be a lambda. The fake keeps charges in a `HashMap`, so a test can check its contents (state). The spy runs the real fake and also records calls (interaction).

## Worked example

Here is one hidden test from your practice task. Read it from top to bottom.

```java nocompile
@Test
@DisplayName("A stuck lock refunds the payment and sends no receipt")
void lockFailureRefunds() {
    when(gateway.charge(anyString(), anyLong())).thenReturn("pay-3");
    when(lock.open("B-1042")).thenReturn(false);

    String result = service.unlock("rider-7", "B-1042");

    assertEquals("lock failed", result);
    verify(gateway).refund("pay-3");
    verify(receipts, never()).send(anyString(), anyString());
}
```

- Lines 4 and 5 arrange: any charge succeeds with `pay-3`, and the lock stays shut.
- Line 7 acts: it calls your `unlock` once.
- Line 9 asserts on the returned text.
- Line 10 verifies the refund used the same payment id the charge gave back.
- Line 11 verifies no receipt went out.

Another hidden test checks order with `InOrder`. The charge must happen before the lock opens.

```java nocompile
InOrder order = inOrder(gateway, lock);
order.verify(gateway).charge(eq("rider-7"), eq(100L));
order.verify(lock).open("B-1042");
```

The program below runs the same story in a plain `main`, so you can watch each step.

```java run
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Main {
    public static void main(String[] args) {
        PaymentGateway gateway = mock(PaymentGateway.class);
        BikeLock lock = mock(BikeLock.class);
        when(gateway.charge(anyString(), anyLong())).thenReturn("pay-3");
        when(lock.open("B-1042")).thenReturn(false);

        String paymentId = gateway.charge("rider-7", 100);
        if (!lock.open("B-1042")) {
            gateway.refund(paymentId);
            System.out.println("lock failed, refunded " + paymentId);
        }

        verify(gateway).refund("pay-3");
        verify(lock, never()).open("B-2001");
        System.out.println("all verifications passed");
    }
}

interface PaymentGateway { String charge(String riderId, long cents); void refund(String paymentId); }
interface BikeLock { boolean open(String bikeId); }
```

## Try it

Run the program and read the captured payment id. Then change `thenReturn("pay-42")` to `thenReturn("pay-99")` and run it again. Last, change `eq("rider-7")` in the `verify` line to `eq("rider-8")`. The verify now throws and names the call it wanted.

```java run
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

public class Main {
    public static void main(String[] args) {
        PaymentGateway gateway = mock(PaymentGateway.class);
        Receipts receipts = mock(Receipts.class);
        when(gateway.charge(anyString(), anyLong())).thenReturn("pay-42");

        String paymentId = gateway.charge("rider-7", 100);
        receipts.send("rider-7", paymentId);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(receipts).send(eq("rider-7"), captor.capture());
        System.out.println("receipt carried " + captor.getValue());
    }
}

interface PaymentGateway { String charge(String riderId, long cents); }
interface Receipts { void send(String riderId, String paymentId); }
```

## Recap

- A test double stands in for a real object. Dummies fill gaps, stubs answer, fakes work simply, mocks record, spies wrap.
- `when(...).thenReturn(...)` and `thenThrow(...)` script a mock's answers. `doThrow(...).when(mock)` does it for void methods.
- `verify` with `times` and `never` checks which calls happened. `InOrder` checks their order.
- Matchers like `any()` and `eq()` go on every argument or none. `ArgumentCaptor` grabs a passed value.
- Prefer real objects, then fakes, then stubs, then mocks. Mock only the costly edges you own, or the tests only mirror your code.

## Common mistakes

**1. Mixing a matcher with a plain value.**

```java nocompile
when(gateway.charge(anyString(), 100)).thenReturn("pay-1");
```

```
org.mockito.exceptions.misusing.InvalidUseOfMatchersException:
Invalid use of argument matchers!
2 matchers expected, 1 recorded:
```

Fix: wrap the plain value in `eq`, `charge(anyString(), eq(100L))`, or use plain values for both.

**2. Stubbing a void method with when.**

```java nocompile
when(receipts.send("rider-7", "pay-1")).thenReturn(null);
```

```
error: 'void' type not allowed here
```

`send` returns nothing, so there is nothing to stub. Fix: you usually do not stub it at all. Check it afterwards with `verify(receipts).send("rider-7", "pay-1")`. To make a void method throw, use `doThrow(...).when(receipts).send(...)`.

**3. Putting the call inside verify.**

```java nocompile
verify(lock.open("B-1042"));
```

```
org.mockito.exceptions.misusing.NotAMockException:
Argument passed to verify() is of type Boolean and is not a mock!
```

`lock.open(...)` runs first and passes `false` to `verify`. Fix: pass the mock, then call the method on the result, `verify(lock).open("B-1042")`.

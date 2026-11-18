## Hook

Picture the Spoke workshop. The head mechanic writes a repair guide in English for fixing a bike lock. Spoke has stations in many towns, and some mechanics don't read English.

So the guide goes to a translator first. The translator checks it for mistakes, then turns it into a picture manual. Every mechanic at every station can follow pictures. Write the guide once, and any station can use it.

Java works the same way. The ticket's question about the "build" is really a question about that translator.

## Concept

A **program** is a list of instructions for a computer. The computer follows them in order, exactly as written.

```animate
title: A program runs top to bottom
kind: code
code: |
  System.out.println("Tap your card");
  System.out.println("Unlock bike B-1042");
  System.out.println("Ride away!");
steps:
  - line: 1
    out: Tap your card
    say: The computer starts at the first instruction and does exactly what it says. It prints one line.
  - line: 2
    out: Unlock bike B-1042
    say: Then it moves to the next line down. It never skips ahead or guesses.
  - line: 3
    out: Ride away!
    say: The last instruction runs, and nothing is left, so the program ends. Swap two lines and the output swaps too.
```

```callout jargon
Source code is the text of a program that a person writes. Java source code lives in files that end in `.java`.
```

### The compiler: javac

A computer can't run source code straight away. Its CPU only understands tiny number codes. So Java uses two helpers.

The first helper is the **compiler**. A compiler is a program that checks your source code and translates it. Java's compiler is called `javac`. It reads `Hello.java` and writes a new file called `Hello.class`.

The `.class` file holds **bytecode**. Bytecode is a set of simple instructions that people don't read. It is the picture manual from the hook.

### The JVM: runs the bytecode

The second helper is the **JVM**, the Java Virtual Machine. The JVM is a program that reads bytecode and runs it on your computer. Windows, Mac and Linux each have their own JVM. They all read the same bytecode. That is why people say Java is "write once, run anywhere".

This whole step from `.java` to `.class` is the **build** that the ticket asks about. Watch one file go through both helpers:

```animate
title: From Hello.java to the screen
kind: scene
nodes:
  - { id: src, label: Hello.java, emoji: "📝", value: source code, row: 0, col: 0 }
  - { id: javac, label: javac compiler, emoji: "🔍", row: 0, col: 1 }
  - { id: cls, label: Hello.class, emoji: "📦", value: bytecode, row: 0, col: 2 }
  - { id: mac, label: JVM on a Mac, emoji: "💻", row: 1, col: 1 }
  - { id: win, label: JVM on Windows, emoji: "🖥️", row: 1, col: 3 }
steps:
  - say: You write source code in Hello.java. It is plain text that people can read, but the CPU cannot.
    focus: [src]
  - say: You run javac Hello.java. The compiler reads every line and checks it for mistakes.
    send: { from: src, to: javac, label: Hello.java }
    set: { javac: checking... }
  - say: No mistakes, so javac writes Hello.class. It holds bytecode, the picture manual for the JVM.
    show: [cls]
    send: { from: javac, to: cls, label: bytecode }
    set: { javac: done }
  - say: You run java Hello. The JVM on your Mac loads the bytecode and runs it, and Hello appears on the screen.
    show: [mac]
    send: { from: cls, to: mac, label: Hello.class }
    set: { mac: "prints: Hello" }
  - say: Copy the same Hello.class to a Windows laptop, and its own JVM reads the same bytecode. Write once, run anywhere.
    show: [win]
    send: { from: cls, to: win, label: Hello.class }
    set: { win: "prints: Hello" }
```

```callout tip
Shortcut: `java Hello.java` compiles in memory and runs in one step. It makes no `.class` file. This course uses `javac`, then `java`, so you can see both helpers at work.
```

### Syntax: compile

```bash
javac Hello.java
```

- `javac` is the compiler.
- You give it the file name, with `.java` at the end.
- If the code is fine, `Hello.class` appears in the same folder.

### Syntax: run

```bash
java Hello
```

- `java` starts the JVM.
- You give it the class name only. Leave off `.class`.
- The JVM loads `Hello.class` and runs it.

### Inside the JVM: interpreter and JIT

How does the JVM run bytecode? It uses two helpers of its own, one after the other.

First comes the **interpreter**. It reads the bytecode one instruction at a time and does what each one says. It starts at once, but it is slow. It is like a mechanic checking the picture manual before every single turn of the wrench.

Meanwhile, the JVM counts how often each piece of code runs. Code that runs again and again is called **hot**. The **JIT compiler** turns hot code into machine code for your exact CPU. From then on, that code runs at full speed.

```callout jargon
**Machine code** is the CPU's own number codes, the only thing a CPU can run directly. **JIT** stands for just-in-time. The JIT compiles while your program is already running, just in time for the next call.
```

```animate
title: Hot code gets faster
kind: scene
nodes:
  - { id: cls, label: Fare.class, emoji: "📦", value: bytecode }
  - { id: interp, label: Interpreter, emoji: "📖", value: idle }
  - { id: jit, label: JIT compiler, emoji: "⚡", value: idle }
  - { id: cpu, label: CPU, emoji: "🧠", value: waiting }
steps:
  - say: The JVM loads the bytecode for fareFor(), the piece of code that works out a rider's fare.
    focus: [cls]
  - say: The first rider ends a ride. The interpreter reads fareFor() step by step and tells the CPU what to do. It works, but slowly.
    send: [{ from: cls, to: interp, label: bytecode }, { from: interp, to: cpu, label: one step at a time }]
    set: { interp: "runs: 1", cpu: slow }
  - say: Riders keep coming, and fareFor() runs thousands of times. The JVM notices. This is hot code.
    set: { interp: "hot!" }
    focus: [interp]
  - say: The JIT compiler takes the hot bytecode and turns it into machine code for this exact CPU.
    send: { from: interp, to: jit, label: "hot: fareFor()" }
    set: { jit: compiling }
  - say: Now fareFor() runs as machine code, straight on the CPU. Every fare after this is much faster.
    send: { from: jit, to: cpu, label: machine code }
    set: { jit: done, cpu: fast }
  - say: Code that runs only once, such as start-up, stays in the interpreter. Compiling it would cost more time than it saves.
    focus: [interp]
```

When you check your Java version in a later ticket, the last line says `mixed mode`. That means the interpreter and the JIT are working together.

### Warm-up: why the first requests are slow

When a Java server starts, none of its code is hot yet. The first requests run in the interpreter, so they are slower. After a while, the JIT has compiled the busy code, and the server speeds up. This is called **warm-up**.

Teams plan for it. Before a fresh server takes all its traffic, they often send it practice requests, or let traffic in slowly. Then real riders never wait on a cold server.

```callout tip
A classic interview question: "Is Java compiled or interpreted?" The answer is both. `javac` compiles source code to bytecode. Then the JVM interprets the bytecode, and the JIT compiles the hot parts to machine code.
```

### Compile errors and run-time errors

Errors can show up at two different times. A **compile error** happens when `javac` finds a mistake, such as a missing semicolon. No `.class` file is made, so nothing runs. A **run-time error** happens later, while the JVM is running your program. The code was valid, but something went wrong during the run.

```animate
title: Where each kind of error stops you
kind: scene
nodes:
  - { id: src, label: Hello.java, emoji: "📝", value: "missing ;" }
  - { id: javac, label: javac compiler, emoji: "🔍" }
  - { id: cls, label: Hello.class, emoji: "📦", value: bytecode }
  - { id: jvm, label: JVM, emoji: "⚙️", value: waiting }
steps:
  - say: Line 3 of Hello.java is missing its semicolon. You run javac to build it.
    focus: [src]
    send: { from: src, to: javac, label: Hello.java }
  - say: javac finds the mistake and stops. This is a compile error, caught before anything runs.
    set: { javac: "error: ';' expected" }
    focus: [javac]
  - say: No Hello.class is made, so the JVM has nothing to run. Nothing prints at all.
    focus: [jvm]
    set: { jvm: nothing to run }
  - say: You add the semicolon and compile again. Now javac is happy and writes Hello.class.
    set: { src: fixed, javac: ok }
    show: [cls]
    send: [{ from: src, to: javac, label: Hello.java }, { from: javac, to: cls, label: bytecode }]
  - say: The JVM runs the bytecode. A run-time error can only happen now, during the run, such as dividing a fare by zero.
    send: { from: cls, to: jvm, label: run }
    set: { jvm: running }
```

```callout tip
A compile error is good news. The compiler caught the mistake before any rider saw it on a kiosk.
```

## Worked example

Here is a complete program. Press **Run** to see it work.

```java run
public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Spoke!");
        System.out.println("Your bike is unlocked.");
    }
}
```

When you press Run, the app does this for you:

1. It saves your text as `Main.java`. This is the source code.
2. It runs `javac Main.java`. The compiler checks the code and makes `Main.class`.
3. It runs `java Main`. The JVM loads the bytecode.
4. The JVM starts at `main` and runs the two print lines in order.
5. You see two lines of output.

## Try it

Press **Run** on the program in the worked example. Check that both lines appear.

Now change the text inside the quotes. Try `"Ride safe, Riverton!"`, then press **Run** again. The app compiles your new source code, so the output changes.

Next, delete one semicolon and press **Run**. You get a compile error, and nothing prints. Put the semicolon back to fix it.

```callout warning
Changing a `.java` file does nothing until you compile it again. The JVM runs the `.class` file it finds, even if it is older than your `.java` file.
```

## Recap

- A program is a list of instructions. Source code is the text you write in a `.java` file.
- `javac` is the compiler. It checks your code and turns it into bytecode in a `.class` file.
- The JVM runs bytecode, so the same `.class` file works on any computer with a JVM. It interprets bytecode first, then the JIT turns hot code into fast machine code.
- Compile with `javac Hello.java`. Run with `java Hello`.
- Compile errors stop the build. Run-time errors happen while the program runs.

## Common mistakes

**The file name doesn't match the class name.**

You save `public class Hello` in a file called `hello.java`. `javac` says: `class Hello is public, should be declared in a file named Hello.java`. Rename the file to `Hello.java`, with the same capital letters.

**Adding .class to the run command.**

You type `java Hello.class`. Java says: `Could not find or load main class Hello.class`. Give the class name only: `java Hello`.

**Forgetting to compile after an edit.**

You edit `Hello.java` and run `java Hello` again. The old output still appears. The JVM ran the old `Hello.class`. Run `javac Hello.java` first, then `java Hello`.

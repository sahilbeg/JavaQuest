## Hook

Picture the Spoke repair workshop near Riverside Station. A mechanic stands at a workbench. Behind them is a big storage shed full of parts.

When a broken bike rolls in, the mechanic fetches the right parts from the shed. They lay the parts out on the workbench. Then they fix the bike, piece by piece.

A computer works the same way. Every kiosk, laptop and phone has a mechanic, a workbench and a shed inside. Once you know these three parts, the code you write later will make much more sense.

## Concept

A computer has three main parts that do the work. It also has ways to take things in and send things out.

### The CPU: the mechanic

The **CPU** (Central Processing Unit) is the chip that follows instructions. It adds numbers, compares values and decides what to do next.

A CPU is very fast. It can do billions of tiny steps every second. Still, it only does what the instructions tell it.

```callout jargon
A core is one worker inside the CPU. A CPU with 8 cores is like a workshop with 8 mechanics working at once.
```

### Memory (RAM): the workbench

**Memory**, also called **RAM** (Random Access Memory), holds what the computer is using right now. Open apps and their data sit here.

RAM is fast, so the CPU can grab things from it quickly. RAM is also temporary. When the power goes off, everything in RAM disappears.

A bigger workbench lets the mechanic work on more bikes at once. More RAM lets you keep more apps open without slowing down.

### Storage: the shed

**Storage** keeps your files and apps for the long term. On a Mac this is an SSD (Solid State Drive), a chip that keeps data without power.

Storage is much bigger than RAM, but slower. When you open an app, the computer copies it from storage into RAM. Then the CPU can work with it.

```callout warning
Saving a file means copying it from RAM to storage. If the power cuts out before you save, your latest changes are gone.
```

### Input and output

**Input** is anything that goes into the computer. The kiosk touchscreen, a card reader and a keyboard are input devices.

**Output** is anything that comes out. The kiosk screen, a receipt printer and speakers are output devices.

### How they work together

Here is what happens when a rider taps "Unlock bike" on a kiosk:

```animate
title: A rider taps "Unlock bike"
kind: scene
nodes:
  - { id: screen, label: Touchscreen, emoji: "👆", col: 0 }
  - { id: cpu, label: CPU, emoji: "🧠", value: "waiting", col: 1 }
  - { id: ram, label: Memory (RAM), emoji: "🧰", col: 2 }
  - { id: disk, label: Storage, emoji: "🗄️", value: "kiosk app", col: 3 }
steps:
  - say: Before any rider arrives, the kiosk starts up. The app is saved in storage, like a tool kept in the shed.
    focus: [disk]
  - say: The app is copied from storage into memory, so it is on the workbench and ready to use.
    send: { from: disk, to: ram, label: kiosk app }
    set: { ram: "kiosk app" }
  - say: A rider taps Unlock bike. The touchscreen is an input, so the tap travels to the CPU.
    focus: [screen]
    send: { from: screen, to: cpu, label: "tap: unlock" }
    set: { cpu: "got a tap" }
  - say: The CPU asks memory what the app says to do with an Unlock tap.
    focus: [cpu]
    send: { from: cpu, to: ram, label: "what next?" }
  - say: Memory answers with the app's instructions. The CPU follows them, very fast.
    send: { from: ram, to: cpu, label: "unlock bike 12", back: true }
    set: { cpu: "unlocking 12" }
  - say: The CPU sends the result to the screen, an output. The rider sees it and rides away.
    send: { from: cpu, to: screen, label: "Bike 12 unlocked", back: true }
    set: { screen: "Bike 12 unlocked", cpu: "done" }
```

```callout analogy
Shed, workbench, mechanic. Storage keeps things, RAM holds what you need now, and the CPU does the actual work.
```

### Everything is numbers: bits and bytes

Inside a computer, everything is stored as numbers: text, photos, fares and apps. Each number is built from tiny switches that are either off or on.

```callout jargon
A **bit** is one switch: 0 for off, 1 for on. A **byte** is a group of 8 bits. Counting with only 0 and 1 is called **binary**.
```

In normal counting, each place is worth ten times the one to its right. In binary, each place is worth two times. So the places are worth 8, 4, 2 and 1. Watch four bits count:

```animate
title: Four bits count up
kind: scene
nodes:
  - { id: b8, label: "8s place", emoji: "💡", value: "0", row: 0, col: 0 }
  - { id: b4, label: "4s place", emoji: "💡", value: "0", row: 0, col: 1 }
  - { id: b2, label: "2s place", emoji: "💡", value: "0", row: 0, col: 2 }
  - { id: b1, label: "1s place", emoji: "💡", value: "0", row: 0, col: 3 }
  - { id: total, label: The number, emoji: "🔢", value: "0", row: 1, col: 1 }
steps:
  - say: Four bits, all off. 0000 means the number 0.
    focus: [b8, b4, b2, b1]
  - say: Turn on the 1s switch. 0001 means 1.
    set: { b1: "1", total: "1" }
  - say: To reach 2, the 1s switch turns off and the 2s switch turns on. It is like 9 rolling over to 10.
    set: { b1: "0", b2: "1", total: "2" }
    focus: [b2]
  - say: 0101 means 4 + 1, which is 5. Add up the places whose switch is on.
    set: { b4: "1", b2: "0", b1: "1", total: "5" }
    focus: [b4, b1, total]
  - say: All four on, 1111, is 8 + 4 + 2 + 1 = 15. So four bits give 16 numbers, from 0 to 15.
    set: { b8: "1", b2: "1", total: "15" }
    focus: [total]
```

Each extra bit doubles how many numbers fit. With n bits you get 2 to the power n values. One byte, 8 bits, holds 256 values: 0 to 255. Later, Java's number types work the same way, just with more bits.

Long binary numbers are hard to read, so programmers often write them in **hexadecimal**, or "hex". Hex uses 16 digits: 0 to 9, then a to f for 10 to 15. One hex digit stands for exactly 4 bits, so one byte is two hex digits. The byte 1111 1111 is `ff` in hex, which is 255.

```callout tip
You will meet hex soon. Git gives every saved change an id like `b61c49e`. That is a hex number.
```

### The operating system: the shop manager

The **operating system**, or OS, is the program that runs the whole computer. Your Mac runs macOS. Spoke's servers run Linux.

The OS is the shop manager. It decides which app gets the CPU next. It hands out space in RAM and keeps the files in storage in order. Apps never grab the hardware themselves. They ask the OS.

```callout jargon
A **process** is a program that is running right now. The OS gives each process its own space in RAM, and stops one process from touching another's space.
```

When a process ends, it hands the OS a small number called its **exit code**. 0 means "everything went fine". Any other number, such as 1, means something went wrong. Build tools and servers read this number to know whether a step worked.

### How fast is each part?

The CPU is so fast that it spends much of its time waiting for data. How long it waits depends on where the data lives.

```callout jargon
A **cache** is a small, very fast memory inside the CPU. It keeps copies of the data the CPU used most recently. A nanosecond (ns) is a billionth of a second. A microsecond (µs) is a millionth, and a millisecond (ms) is a thousandth.
```

These are rough sizes, not exact numbers. The last column stretches time so you can feel the gaps.

| Where the data is | Time to fetch it | If 1 ns were 1 second |
|---|---|---|
| CPU cache | about 1 ns | 1 second |
| RAM | about 100 ns | about 2 minutes |
| SSD storage | about 100 µs | about 1 day |
| A server across the world | about 100 ms | about 3 years |

Each step down is far slower. So fast programs avoid trips to storage and across the network. Keeping a copy of data close by is called **caching**, and later tickets use this idea a lot.

### When RAM runs out: swap

What happens when the workbench is full? The OS moves parts of apps you are not using out to storage. This is called **swap**.

When you switch back to that app, the OS must fetch it from the SSD. That is about 1,000 times slower than RAM. This is why a computer with too many apps open feels sluggish. Activity Monitor's Memory tab shows it as **Swap Used**.

## Worked example

Follow the kiosk at Riverside Station from the moment it turns on.

1. A worker presses the power button. That is input.
2. The kiosk app sits in storage, where it stayed safe all night.
3. The computer copies the app from storage into RAM.
4. A rider taps "Rent a bike". The touchscreen sends that input to the CPU.
5. The CPU runs the app's instructions from RAM. It checks which dock has a free bike.
6. The screen shows "Take bike from dock 4". The printer prints a receipt. Both are output.
7. At night the kiosk shuts down. RAM goes blank, but the app is still in storage.

Next morning, the whole process starts again from step 3.

## Try it

Find the three parts inside your own Mac.

1. Click the Apple menu in the top left corner of the screen.
2. Choose **About This Mac**.
3. Look for the **Chip** or **Processor** line. That is your CPU.
4. Look for the **Memory** line. That number, such as 16 GB, is your RAM.
5. Click **More Info**, then scroll to **Storage**. That shows the size of your SSD.
6. Now open **Activity Monitor** from Applications, then Utilities.
7. Click the **CPU** tab and the **Memory** tab. Watch the numbers change as you open and close an app.

```callout tip
GB means gigabyte, about a billion bytes. A byte is 8 bits, enough to store one simple letter of text.
```

Compare your numbers. Your storage size should be many times bigger than your memory size.

## Recap

- The **CPU** follows instructions and does the actual work.
- **Memory (RAM)** holds what is in use right now. It is fast and forgets when power goes off. **Storage** keeps files for the long term. It is big and slower.
- Apps move from storage into RAM, the CPU runs them, and **input** and **output** devices carry data in and out.
- Everything is stored in **bits**. 8 bits make a **byte**, and n bits give 2 to the power n values.
- The **operating system** runs each program as a **process**. Cache, RAM, SSD and network are each far slower than the one before.

## Common mistakes

**"Memory and storage are the same thing."**

They are two different parts. RAM holds what you use right now and empties when power goes off. Storage keeps your files even when the computer is off.

**"More storage makes my computer faster."**

A bigger shed holds more parts, but the mechanic works at the same speed. Speed depends on the CPU, on having enough RAM, and on how fast the storage is. A faster SSD helps apps open quickly. A bigger one does not.

**"A closed app is still in RAM."**

When you quit an app, the computer frees its space in RAM. The app stays in storage, ready to load again next time.

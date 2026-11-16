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
GB means gigabyte, about a billion bytes. A byte is a tiny piece of data, roughly one letter of text.
```

Compare your numbers. Your storage size should be many times bigger than your memory size.

## Recap

- The **CPU** follows instructions and does the actual work.
- **Memory (RAM)** holds what is in use right now. It is fast and forgets when power goes off.
- **Storage** keeps files and apps for the long term. It is big and slower.
- **Input** devices send data in. **Output** devices show or send results out.
- Apps move from storage into RAM, and the CPU runs them from there.

## Common mistakes

**"Memory and storage are the same thing."**

They are two different parts. RAM holds what you use right now and empties when power goes off. Storage keeps your files even when the computer is off.

**"More storage makes my computer faster."**

A bigger shed holds more parts, but the mechanic works at the same speed. Speed mostly depends on the CPU and on having enough RAM.

**"A closed app is still in RAM."**

When you quit an app, the computer frees its space in RAM. The app stays in storage, ready to load again next time.

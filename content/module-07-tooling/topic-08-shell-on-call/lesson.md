## Hook

A bike mechanic on a call-out does not bring the whole workshop. They bring a small bag: a pump, a multitool, a chain breaker. With those few tools they can fix most bikes at the roadside.

Being **on call** is the software version. When production breaks at 7 am, you may have only a terminal on a server. No IDE, no dashboard, no mouse. A dozen small commands, joined together, answer almost every question: what is failing, how often, where, and since when.

Itachi's riders at Harbor cannot pay. The answer is somewhere in a 400-line log. Let's pack your bag.

## Concept

### Pipes: small tools, joined up

```callout jargon
**On call** means you are the person who answers when production breaks, often for a week at a time. A **log** is a file where a program writes one line for each thing that happens. A **pipe**, written `|`, sends the output of one command straight into the next command.
```

Each shell tool does one small job. `grep` picks lines, `sort` sorts them, `wc -l` counts them. A pipe joins them into a little assembly line:

```bash
grep ERROR spoke.log | wc -l
```

`grep` finds the error lines and, instead of printing them, hands them to `wc -l`, which counts them. You never see the lines in between, only the final answer.

Build pipelines one step at a time. Run the first command, look at its output, then add `|` and the next step. When something looks wrong, you know exactly which step did it.

A pipe carries only normal output. Error messages and Java stack traces travel on a second stream, called **standard error**. Add `2>&1` to send them down the pipe too: `java App.java 2>&1 | grep Exception`.

### The log you will read

Spoke's API writes one line per rental event. Every line has seven **fields**, separated by single spaces:

```text
2026-11-17 08:00:53 ERROR DockTimeout rental-3 B-1048 Harbor
```

| Field | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| Meaning | date | time | level | event | rental | bike | station |

The **level** says how serious a line is: `INFO` for normal events, `WARN` for warnings, `ERROR` for failures. In this log, station names have no spaces, so Old Mill is written `OldMill`.

### Syntax: look at a file with head, tail and less

```bash
head -n 3 spoke.log
tail -n 2 spoke.log
tail -f spoke.log
less spoke.log
```

- `head -n 3` prints the first 3 lines. `tail -n 2` prints the last 2.
- `tail -f` ("follow") keeps the file open and prints new lines the moment the program writes them. It is how you watch a live server. Press Ctrl+C to stop.
- `less` opens a file one screen at a time, without loading it all. Space goes forward, `b` goes back, `/Harbor` searches and `n` jumps to the next match, `G` jumps to the end, and `q` quits.

```text
% head -n 3 spoke.log
2026-11-17 08:00:17 INFO RentalEnded rental-1 B-1045 Depot
2026-11-17 08:00:52 INFO RentalStarted rental-2 B-1029 Uptown
2026-11-17 08:00:53 ERROR DockTimeout rental-3 B-1048 Harbor
```

```callout warning
Never `cat` a huge log on a server. A 2 GB file floods your terminal for minutes. Use `less`, `head`, `tail` or `grep` instead.
```

### Syntax: grep finds lines

```bash
grep ERROR spoke.log
grep -i error spoke.log
grep -v INFO spoke.log
grep -n B-1042 spoke.log
grep -c ERROR spoke.log
grep -E "StationOffline|FareMismatch" spoke.log
grep -rn 8080 src
```

`grep` prints every line that contains a pattern. Its options:

| Option | Meaning |
|---|---|
| `-i` | ignore case: `error` also matches `ERROR` and `Error` |
| `-v` | invert: print lines that do NOT match |
| `-n` | show the line number in front of each match |
| `-c` | print only how many lines matched |
| `-E` | extended patterns, where a vertical bar means "or" |
| `-r` | search every file in a folder and its subfolders |

Case matters, and so does what else contains your word:

```text
% grep -c ERROR spoke.log
49
% grep -ci error spoke.log
64
```

The extra 15 are `WARN RetryAfterError` lines. `-i` matched "Error" inside them. Pick your pattern with care, then check a few matching lines by eye.

`-r` is how you search a whole project, with file names and line numbers:

```text
% grep -rn 8080 src
src/app.properties:1:server.port=8080
src/main/java/App.java:1:class App { int port = 8080; }
```

### Counting: awk, cut, sort, uniq -c and wc -l

These tools turn lines into answers.

```bash
awk '{print $4}' spoke.log
cut -d' ' -f2 spoke.log
sort
sort -rn
uniq -c
wc -l spoke.log
```

- `awk '{print $4}'` prints field 4 of each line. awk splits on any run of spaces.
- `cut -d' ' -f2` also prints field 2, split on exactly one space. `cut -d: -f1` splits on colons instead.
- `sort` sorts lines alphabetically. `sort -rn` sorts by number (`-n`), biggest first (`-r`).
- `uniq -c` squashes repeated lines next to each other into one, with a count in front.
- `wc -l` counts lines. On a Mac it pads the number with spaces.

Put together, they answer the classic on-call question, "what are the top 5 errors?":

```text
% grep ERROR spoke.log | awk '{print $4}' | sort | uniq -c | sort -rn | head -n 5
  23 PaymentDeclined
  13 DockTimeout
   7 BikeNotFound
   5 StationOffline
   1 FareMismatch
```

On Linux, `uniq -c` pads the counts a little differently. The numbers are the same.

```animate
title: The top-errors pipeline, one step at a time
kind: scene
nodes:
  - { id: log, label: spoke.log, emoji: "📄", value: 400 lines, row: 0, col: 0 }
  - { id: grep, label: grep ERROR, emoji: "🔎", row: 0, col: 1 }
  - { id: awk, label: "awk '{print $4}'", emoji: "✂️", row: 0, col: 2 }
  - { id: sort, label: sort, emoji: "🗂️", row: 0, col: 3 }
  - { id: uniq, label: uniq -c, emoji: "🧮", row: 1, col: 3 }
  - { id: rank, label: sort -rn, emoji: "🏆", row: 1, col: 2 }
  - { id: head, label: head -n 5, emoji: "🖥️", row: 1, col: 1 }
steps:
  - say: The log holds 400 lines. grep keeps only the 49 lines that contain ERROR.
    send: { from: log, to: grep, label: 400 lines }
    set: { grep: 49 lines }
  - say: awk keeps just field 4 of each line, the error type. Now it is 49 short names.
    send: { from: grep, to: awk, label: 49 lines }
    set: { awk: "DockTimeout, PaymentDeclined, ..." }
  - say: sort puts equal names next to each other. That matters for the next step.
    send: { from: awk, to: sort, label: 49 names }
    set: { sort: "BikeNotFound x7, then DockTimeout x13, ..." }
  - say: uniq -c squashes each group of neighbours into one line with a count in front.
    send: { from: sort, to: uniq, label: grouped }
    set: { uniq: 5 lines with counts }
  - say: sort -rn sorts by that number, biggest first. -n means numeric, -r means reversed.
    send: { from: uniq, to: rank, label: 5 counts }
    set: { rank: "23 PaymentDeclined first" }
  - say: head -n 5 keeps the top five and prints them. Six small tools, one answer.
    send: { from: rank, to: head, label: ranked }
    set: { head: "23 PaymentDeclined, 13 DockTimeout, ..." }
```

### Syntax: find files

```bash
find logs -name "*.log"
find logs -size +100M
find logs -name "*.log" -mtime +7
```

`find` walks a folder tree and lists the files that match. `-name "*.log"` matches by name; the quotes stop the shell from expanding `*` itself. `-size +100M` finds files bigger than 100 MB. `-mtime +7` finds files last changed more than 7 days ago.

```text
% find logs -name "*.log"
logs/2026-10/api-02.log
logs/2026-10/api-01.log
logs/2026-11/api-02.log
logs/2026-11/api-01.log
```

### A throwaway server to practise on

The next few cards use this tiny Spoke server. It answers two web addresses on port 8080, and prints a message when it shuts down cleanly. Save it as `StationServer.java` in `~/spoke-practice/oncall` and start it with `java StationServer.java &`.

```java nocompile
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class StationServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/stations/Harbor", exchange ->
                reply(exchange, 200, "{\"station\":\"Harbor\",\"freeDocks\":7}"));
        server.createContext("/rentals", exchange -> {
            if (!exchange.getRequestMethod().equals("POST")) {
                reply(exchange, 405, "{\"error\":\"use POST\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            reply(exchange, 201, "{\"id\":\"rental-17\",\"request\":" + body + "}");
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("Stopping: closing open rentals cleanly")));
        server.start();
        System.out.println("Station server on port 8080, pid " + ProcessHandle.current().pid());
    }

    static void reply(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
```

`HttpServer` is a small web server built into the JDK. You will meet real ones in module 8. The **shutdown hook** is code Java runs when the program is asked to stop.

### Processes: ps and kill

```callout jargon
A **process** is one running program. Each has a number, its **PID** (process id). A **signal** is a short message the system sends to a process, such as "please stop".
```

```bash
ps aux | grep StationServer
kill 87349
kill -9 87407
```

`ps aux` lists every process on the machine, one per line. Piping it into `grep` finds yours:

```text
% ps aux | grep StationServer
you    87171   0.1  0.0 435300400   1248   ??  S     5:12AM   0:00.00 grep StationServer
you    87136   0.0  0.5 441783728 102960   ??  SN    5:12AM   0:00.51 /usr/bin/java StationServer.java
```

The second column is the PID. The first line is the `grep` command itself, which matched its own words. Ignore it.

`kill <pid>` sends **SIGTERM**, the polite "please stop". A Java program gets the chance to clean up: finish requests, save state, close files. `kill -9 <pid>` sends **SIGKILL**. The system ends the process instantly, with no cleanup at all.

The station server prints a message from its shutdown hook. Watch the difference:

```text
% java StationServer.java &
[1] 87349
Station server on port 8080, pid 87349
% kill 87349
Stopping: closing open rentals cleanly
[1]  + exit 143   java StationServer.java
% java StationServer.java &
[1] 87407
Station server on port 8080, pid 87407
% kill -9 87407
[1]  + killed     java StationServer.java
```

The `&` at the end starts a program in the background, so you get the prompt back. After `kill -9`, the cleanup message never printed. Open rentals could be left half-saved.

```callout warning
`kill -9` is the last resort. Send a plain `kill` first and give the process a few seconds. Only if it ignores you, use `-9`.
```

Is a Java process frozen rather than busy? Before you kill it, take a **thread dump**. `jstack <pid>` or `kill -3 <pid>` prints what every thread is doing. It often shows exactly why it froze.

`top` shows live processes sorted by CPU use, like a task manager. Press `q` to quit. `htop` is a friendlier version that some servers have.

### Exit codes and $?

Every program ends with an **exit code**: a number that says how it went. `0` means success. Anything else means failure. The shell keeps the last one in `$?`.

```text
% java StationServer.java
Exception in thread "main" java.net.BindException: Address already in use
	at java.base/sun.nio.ch.Net.bind0(Native Method)
% echo $?
1
% curl -i http://localhost:8081/stations/Harbor
curl: (7) Failed to connect to localhost port 8081 after 0 ms: Couldn't connect to server
% echo $?
7
```

An uncaught exception ends a Java program with exit code 1. `System.exit(3)` would end it with 3. A process stopped by a signal gets 128 plus the signal's number: SIGTERM is 15, so 143, and SIGKILL is 9, so 137. CI uses exit codes too: any step that ends with a non-zero code turns the build red.

`grep` follows the same rule: exit code 0 if it found a match, 1 if not. That makes it handy in scripts.

### Ports: "Address already in use"

```callout jargon
A **port** is a numbered door on a computer that a server listens at, such as 8080. Only one process can listen at a port at a time.
```

When a server will not start because of `Address already in use`, something else already owns the port. Often it is an old copy of your own app that never stopped. `lsof -i :8080` ("list open files") shows who:

```text
% lsof -i :8080
COMMAND   PID USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
java    87136  you    5u  IPv6 0x1cf24756f353954e      0t0  TCP *:http-alt (LISTEN)
% lsof -t -i :8080
87136
```

`http-alt` is the usual name for port 8080; add `-P` to see the number instead. `-t` prints only the PID, ready for `kill`. Check what the process is before you stop it. It might be a teammate's app or a database.

Without `sudo`, `lsof` only shows your own processes. On a server, run `sudo lsof -i :8080`. Many Linux servers also have `ss -ltnp`, which lists every listening port with its process.

### Syntax: talk to a server with curl

```bash
curl -i http://localhost:8080/stations/Harbor
curl -i -X POST -H 'Content-Type: application/json' -d '{"bike":"B-1042","station":"Harbor"}' http://localhost:8080/rentals
```

`curl` sends an HTTP request from the terminal and prints the answer. You will use it constantly in module 8.

| Option | Meaning |
|---|---|
| `-i` | also print the response's status line and headers |
| `-X POST` | use the POST method instead of the default GET |
| `-H '...'` | add a request header |
| `-d '...'` | send this text as the request body |
| `-s` | silent: hide the progress meter and error messages (add `-S` to still show errors) |

```text
% curl -i http://localhost:8080/stations/Harbor
HTTP/1.1 200 OK
Date: Wed, 18 Nov 2026 05:13:02 GMT
Content-type: application/json
Content-length: 34

{"station":"Harbor","freeDocks":7}
```

The first line is the **status**: `200 OK` means it worked. The POST answers `HTTP/1.1 201 Created`, with the new rental in the body. When something misbehaves, `curl -i` tells you in seconds whether the server itself is fine.

### Environment variables and PATH

You met `PATH` and `JAVA_HOME` when you set up Java. Both are environment variables: named settings the shell hands to every program it starts.

```bash
echo $PATH
env
export SPOKE_ENV=staging
SPOKE_ENV=production java Env.java
```

- `echo $PATH` prints one variable. `env` prints all of them.
- `NAME=value` alone only sets a **shell variable**. Programs you start do not see it.
- `export NAME=value` makes it an environment variable, so every program you start from now on sees it.
- `NAME=value command` sets it for that one command only.

```java
public class Env {
    public static void main(String[] args) {
        System.out.println("SPOKE_ENV is " + System.getenv("SPOKE_ENV"));
    }
}
```

```text
% SPOKE_ENV=staging
% java Env.java
SPOKE_ENV is null
% export SPOKE_ENV=staging
% java Env.java
SPOKE_ENV is staging
% SPOKE_ENV=production java Env.java
SPOKE_ENV is production
```

An `export` lasts until you close that terminal window. To keep one for good, add the line to `~/.zshrc`, as you did for `JAVA_HOME`.

### Permissions: chmod +x

`ls -l` starts each line with ten characters, such as `-rw-r--r--`. After the first, they come in three groups: what the owner, the group and everyone else may do. `r` is read, `w` is write and `x` is execute, meaning "may run as a program".

```text
% ./deploy.sh
zsh: permission denied: ./deploy.sh
% ls -l deploy.sh | cut -c1-10
-rw-r--r--
% chmod +x deploy.sh
% ls -l deploy.sh | cut -c1-10
-rwxr-xr-x
% ./deploy.sh
deploying kiosk
```

`chmod +x` adds the execute permission. You will meet this on CI. Its shell is bash, so the failing line ends in `./mvnw: Permission denied`. The wrapper was committed without its `x`. Run `git update-index --chmod=+x mvnw`, then commit, to store the permission in Git.

### Disks: df and du

A full disk breaks servers in strange ways: logs stop, databases refuse writes, deploys fail.

- `df -h` shows how full each disk is. `-h` means human-readable sizes, like `50G`.
- `du -sh <folder>` shows how much space one folder uses. `-s` means one total, not every subfolder.

Here is an example from a Linux server, for illustration only:

```text
Filesystem      Size  Used Avail Use% Mounted on
/dev/sda1        50G   50G     0 100% /
```

Then hunt for the culprit with `du` and `find`:

```text
% du -sh logs/*
322M	logs/2026-10
9.0M	logs/2026-11
% find logs -size +100M
logs/2026-10/api-01.log
```

Old logs are the classic cause. The real fix is **log rotation**: a tool that starts a new log file every day and deletes old ones.

### Remote servers: ssh and scp

Production servers live in a data center, not on your desk. You reach them with **ssh**, the "secure shell": it opens a terminal on the remote machine, over an encrypted connection.

```bash
ssh intern@api-1.spoke.example
scp intern@api-1.spoke.example:/var/log/spoke/api.log .
```

- `ssh user@host` logs you in as `user` on `host`. Every command after that runs on the server, until you type `exit`.
- `scp` copies files over the same connection, as `scp from to`. This one copies the server's log into your current folder, `.`.

These addresses are made up, so the commands are only examples. Real teams log in with **SSH keys**, not passwords. `ssh-keygen` makes a pair: a private key that never leaves your laptop, and a public key you give to the server. Keys cannot be guessed like passwords, and one key can be removed when someone leaves the team.

### In an interview

On-call skills show up in interviews in three ways.

- **Shell puzzles.** "Find the 10 most common IP addresses in this access log" is a classic. The answer is the pipeline you just learned: `awk`, `sort`, `uniq -c`, `sort -rn`, `head`.
- **Debugging questions.** "Your service will not start: Address already in use. What do you do?" Use `lsof -i :port`, check what the process is, then stop it politely.
- **Concept questions.** "What is the difference between SIGTERM and SIGKILL?" "A server's disk is full. How do you find out why?" "What does exit code 137 mean?" Killed by signal 9.

In behavioural rounds, "tell me about a production issue you debugged" goes well when you can describe exactly how you narrowed it down.

## Worked example

Ticket SPK-0708. Kakashi copied this morning's API log to your laptop as `spoke.log`. Itachi asks three things: what is failing, where, and since when.

**Step 1: get a feel for it.**

```text
% wc -l spoke.log
     400 spoke.log
% tail -n 2 spoke.log
2026-11-17 11:29:55 INFO RentalStarted rental-399 B-1047 OldMill
2026-11-17 11:30:38 WARN SlowResponse rental-400 B-1007 Riverside
```

400 lines, from just after 08:00 to 11:30.

**Step 2: how many errors?**

```text
% grep -c ERROR spoke.log
49
% grep -v INFO spoke.log | wc -l
      75
```

49 errors, and 75 lines that are not plain `INFO`, warnings included. Roughly one rental in eight hit an error.

**Step 3: what is failing?**

```text
% grep ERROR spoke.log | awk '{print $4}' | sort | uniq -c | sort -rn | head -n 5
  23 PaymentDeclined
  13 DockTimeout
   7 BikeNotFound
   5 StationOffline
   1 FareMismatch
```

Payments are the biggest problem by far, just as Itachi said.

**Step 4: where?** Swap field 4 for field 7, the station:

```text
% grep ERROR spoke.log | awk '{print $7}' | sort | uniq -c | sort -rn
  25 Harbor
  10 Riverside
   9 Depot
   3 Uptown
   2 OldMill
% grep 'ERROR PaymentDeclined' spoke.log | grep -c Harbor
17
```

17 of the 23 declined payments happened at Harbor. Something at Harbor is wrong, not payments everywhere.

**Step 5: since when?** Field 2 is the time. `cut -d: -f1` keeps just the hour:

```text
% grep ERROR spoke.log | cut -d' ' -f2 | cut -d: -f1 | sort | uniq -c
  16 08
  10 09
  18 10
   5 11
```

Errors started in the first hour of the log. So the trouble began before 09:00, not after a later deploy.

**Step 6: one real example.** Itachi mentioned a rider on B-1042:

```text
% grep -n B-1042 spoke.log | grep ERROR
133:2026-11-17 09:10:15 ERROR PaymentDeclined rental-133 B-1042 Harbor
```

Line 133, rental-133, at Harbor. You reply to Itachi: "Payment failures, mostly at Harbor, since about 08:00. Example: rental-133 on B-1042 at 09:10." Kakashi now knows exactly where to look: Harbor's card reader.

## Try it

Make your own copy of the log and answer questions with pipelines. Save this as `MakeLog.java` in a new folder, `~/spoke-practice/oncall`:

```java
import java.util.Random;

public class MakeLog {
    public static void main(String[] args) {
        Random random = new Random(7);
        String[] stations = {"Riverside", "Old Mill", "Harbor", "Uptown", "Depot"};
        String[] errors = {"PaymentDeclined", "PaymentDeclined", "PaymentDeclined", "PaymentDeclined",
                "PaymentDeclined", "DockTimeout", "DockTimeout", "DockTimeout", "BikeNotFound",
                "BikeNotFound", "StationOffline", "FareMismatch"};
        int seconds = 8 * 3600;
        for (int rental = 1; rental <= 400; rental++) {
            seconds += 1 + random.nextInt(60);
            String time = String.format("%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
            String station = stations[random.nextInt(stations.length)].replace(" ", "");
            String bike = "B-" + (1000 + random.nextInt(60));
            int roll = random.nextInt(100);
            String level;
            String event;
            if (roll < 12) {
                level = "ERROR";
                event = errors[random.nextInt(errors.length)];
                if (event.startsWith("Payment") && random.nextInt(3) > 0) {
                    station = "Harbor";
                }
            } else if (roll < 20) {
                level = "WARN";
                event = random.nextBoolean() ? "SlowResponse" : "RetryAfterError";
            } else {
                level = "INFO";
                event = random.nextBoolean() ? "RentalStarted" : "RentalEnded";
            }
            System.out.println("2026-11-17 " + time + " " + level + " " + event
                    + " rental-" + rental + " " + bike + " " + station);
        }
    }
}
```

`new Random(7)` starts from a fixed **seed**, so every run makes exactly the same "random" log. Your answers will match the ones in this lesson.

```bash
cd ~/spoke-practice/oncall
java MakeLog.java > spoke.log
head -n 3 spoke.log
```

The `>` sends the program's output into `spoke.log`, as you learned in your first week. Now answer each question with one pipeline. Build each one a step at a time.

1. How many lines are there? (`wc -l spoke.log` → 400)
2. How many lines are `WARN`? Use `grep -c`. (26)
3. Which bike ids failed at Uptown? Start from `grep ERROR spoke.log | grep Uptown`, then keep field 6.
4. How many `StationOffline` or `FareMismatch` errors? Try `grep -E "StationOffline|FareMismatch" spoke.log | wc -l`. (6)
5. Count the lines per level with `cut -d' ' -f3 spoke.log | sort | uniq -c`. (49 ERROR, 325 INFO, 26 WARN)
6. Drop the first `sort` from the top-errors pipeline and run it. Why do the counts fall apart?
7. Bonus: how many different bikes had at least one error? Start from `grep ERROR spoke.log`, keep field 6 with `awk '{print $6}'`, then `sort -u` (sort, keep each line once), then `wc -l`. (28)

## Recap

- A pipe `|` feeds one command's output into the next. Build pipelines one step at a time, checking each output.
- `grep` finds lines (`-i`, `-v`, `-n`, `-c`, `-E`, `-r`), and `head`, `tail -f` and `less` let you look at big logs safely.
- `awk '{print $N}' | sort | uniq -c | sort -rn | head` counts and ranks anything in a log.
- `ps aux | grep`, `kill` before `kill -9`, exit codes in `$?`, and `lsof -i :8080` for "Address already in use". `curl -i` checks a server from the terminal.
- `export` sets environment variables, and `chmod +x` fixes "Permission denied". `df -h` and `du -sh` find a full disk, and `ssh` and `scp` reach servers with keys.

## Common mistakes

**1. uniq -c without sort first.**

```text
% grep ERROR spoke.log | awk '{print $4}' | uniq -c | head -n 4
   1 DockTimeout
   2 PaymentDeclined
   1 BikeNotFound
   2 StationOffline
```

What you see: the same name over and over with tiny counts. `uniq` only merges lines that sit next to each other. Fix: always `sort` before `uniq -c`.

**2. Reaching for kill -9 first.**

```text
% kill -9 87407
[1]  + killed     java StationServer.java
```

What you see: the process is gone, but its shutdown code never ran. Open rentals may be half-written, and temporary files are left behind. Fix: run plain `kill <pid>` first and wait a few seconds. Use `-9` only if the process ignores it.

**3. Killing whatever owns the port.**

```bash
kill $(lsof -t -i :5432)
```

What you see: your app starts, but now the database is down. Port 5432 belonged to it, not to your old app. Fix: run `lsof -i :<port>` first and read the COMMAND column. Only stop a process you know is yours. Or start your app on another port.

**4. Setting a variable without export.**

```text
% SPOKE_ENV=staging
% java Env.java
SPOKE_ENV is null
```

What you see: the shell knows the value, but the program you start does not. Fix: `export SPOKE_ENV=staging`, or put it in front of the one command: `SPOKE_ENV=staging java Env.java`.

**5. Trusting a grep pattern without looking.**

```text
% grep -ci error spoke.log
64
```

What you see: 64 "errors", when only 49 lines are real `ERROR` lines. The other 15 are warnings with "Error" inside a word. Fix: match exactly what you mean, like `grep -c ERROR` or `grep -c " ERROR "`. Then look at a few matching lines before you trust a count.

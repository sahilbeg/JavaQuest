## Hook

You post a letter to a friend. You write their street address, not just their name. The post office reads the address, picks a route, and a van carries it. If the letter gets lost, nobody tells you. You only notice when no reply comes.

The rider app is about to send letters to the Spoke server, thousands of them a minute. The next ticket teaches HTTP, the language of those letters. This ticket is about the post office underneath: how a name becomes an address, how a connection is opened, and how it is kept private. That is also where Kakashi's missing seconds are hiding.

## Concept

```callout jargon
A **network** is a group of computers joined so they can send each other data. The **internet** is a network of networks. Data travels in small chunks called **packets**, each one up to about 1,500 bytes.
```

### The journey of one request

Say the rider app asks for `https://api.spoke.example/rentals`. Before any rental data moves, four things happen in order. Watch them.

```animate
title: One request, from name to answer
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱", value: "knows a name" }
  - { id: dns, label: DNS resolver, emoji: "📖" }
  - { id: api, label: Spoke API, emoji: "🖥️", value: idle }
steps:
  - say: The app only knows the name api.spoke.example. Computers find each other by number, so it must look the number up first.
    focus: [app]
  - say: Step 1, DNS. The app asks a resolver, which works like a phone book, for the server's address.
    send: { from: app, to: dns, label: "api.spoke.example?" }
  - say: The resolver answers with an IP address. Now the app knows where to go.
    send: { from: dns, to: app, label: "203.0.113.10", back: true }
    set: { app: "knows 203.0.113.10" }
  - say: Step 2, TCP. The app and server shake hands to open a connection. That costs one full round trip before any data moves.
    send: [{ from: app, to: api, label: SYN }, { from: api, to: app, label: SYN-ACK, back: true }, { from: app, to: api, label: ACK }]
    set: { api: connected }
  - say: Step 3, TLS. The server shows its certificate. The app checks it really is Spoke, and both agree on secret keys.
    send: [{ from: app, to: api, label: "hello" }, { from: api, to: app, label: "certificate + keys", back: true }]
    set: { api: "encrypted tunnel" }
  - say: Step 4, HTTP. Only now does the real request travel, scrambled inside the tunnel.
    send: { from: app, to: api, label: "GET /rentals" }
  - say: The server answers in 20 ms. Most of the wait was the three steps before it.
    send: { from: api, to: app, label: "200 OK + JSON", back: true }
  - say: The next request reuses this open connection, so it skips DNS, TCP and TLS. That is why reuse matters so much.
    focus: [app, api]
```

`api.spoke.example` is made up. Names ending in `.example` are reserved for examples, so they never lead anywhere real. Addresses starting `203.0.113.` are reserved for examples too.

### IP addresses: a number for every computer

```callout jargon
An **IP address** (Internet Protocol address) is the number that finds one computer on a network. **IPv4** addresses look like `203.0.113.10`: four numbers from 0 to 255. **IPv6** addresses are longer, like `2606:4700:83b5::ff98`, because the world ran out of IPv4 ones.
```

Every packet carries two IP addresses: where it came from and where it is going. Routers along the way read the "to" address and pass the packet one hop closer, like post offices.

```callout jargon
A **router** is a box that forwards packets between networks. Your home Wi-Fi box is one. It joins your home network to your internet provider's network.
```

### Special addresses: loopback, any, private, public

A few addresses have special meanings.

| Address | Name | Means |
|---|---|---|
| `127.0.0.1` (and `::1` in IPv6) | loopback | this same computer. The name `localhost` points here. |
| `0.0.0.0` | any | "every network card of this computer", used when a server starts listening |
| `10.x.x.x`, `192.168.x.x`, `172.16.x.x` to `172.31.x.x` | private | only inside one home, office or data centre, never on the open internet |
| everything else, like `1.1.1.1` | public | reachable across the internet |

Java's `InetAddress` can tell them apart. A written-out address needs no lookup, so this runs anywhere.

```java run
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        String[] addresses = {"127.0.0.1", "0.0.0.0", "192.168.1.20", "10.0.0.7", "1.1.1.1", "::1"};
        for (String text : addresses) {
            InetAddress ip = InetAddress.getByName(text);   // a written-out address: no DNS lookup happens
            String kind;
            if (ip.isLoopbackAddress()) {
                kind = "loopback: this same computer";
            } else if (ip.isAnyLocalAddress()) {
                kind = "any: every network card of this computer";
            } else if (ip.isSiteLocalAddress()) {
                kind = "private: only inside a home or office network";
            } else {
                kind = "public: reachable across the internet";
            }
            System.out.println(text + " -> " + kind);
        }
    }
}
```

Your laptop at home has a private address, like `192.168.1.8`. Your Wi-Fi box shares one public address for the whole house. Spoke's servers will sit on private addresses too, behind one public front door.

```callout jargon
**NAT** (Network Address Translation) is how one public address is shared. Your Wi-Fi box swaps each outgoing packet's private address for its public one, and remembers which device asked. When the answer comes back, it swaps the address back and passes it on.
```

NAT is why a server on the internet sees your house, not your laptop. It is also why nobody outside can start a connection to your laptop: the Wi-Fi box has no note saying who should get it.

### Ports: doors on one computer

One computer runs many programs that talk over the network. A **port** number, from 1 to 65535, says which program a packet is for. An IP address finds the building; the port finds the flat.

| Port | Usually used by |
|---|---|
| 80 | HTTP |
| 443 | HTTPS |
| 5432 | PostgreSQL, a database |
| 8080 | a Spring Boot app while you develop it |

A server **listens** on a port: it asks the operating system to hand it every connection that arrives there. Only one program can listen on a given address and port at a time.

```callout jargon
A **socket** is one end of a network connection, as a program sees it: an IP address plus a port. A connection joins two sockets, such as `192.168.1.8:61112` on the phone and `203.0.113.10:443` on the server.
```

On a Mac or Linux, `lsof` lists who is listening. Here a small test server listens on port 8765 (trimmed real output; your numbers will differ):

```bash
lsof -nP -iTCP:8765 -sTCP:LISTEN
```

```text
COMMAND   PID   TYPE  NODE NAME
Python  59178   IPv4   TCP 127.0.0.1:8765 (LISTEN)
```

`127.0.0.1:8765` means it only accepts connections from this same computer.

### DNS: names into numbers

People remember names. Computers route by numbers. **DNS** turns one into the other.

```callout jargon
**DNS** (Domain Name System) is the internet's phone book: it turns a name like `api.spoke.example` into an IP address. A **resolver** is the server that does the looking up for you, usually run by your internet provider or your Wi-Fi box.
```

No single computer knows every name. So the resolver asks in steps, from the top of the name down. Pretend `.example` is a real ending, like `.com`.

```animate
title: The resolver looks up api.spoke.example
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱" }
  - { id: resolver, label: Resolver, emoji: "📖", value: "cache: empty" }
  - { id: root, label: Root servers, emoji: "🌍", row: 1, col: 0 }
  - { id: tld, label: ".example servers", emoji: "🗂️", row: 1, col: 1 }
  - { id: spoke, label: "Spoke's name server", emoji: "🚲", row: 1, col: 2 }
steps:
  - say: The app asks its resolver for api.spoke.example. The resolver has never seen this name.
    send: { from: app, to: resolver, label: "api.spoke.example?" }
  - say: The root servers do not know it, but they know who handles every name ending in .example.
    send: [{ from: resolver, to: root, label: "api.spoke.example?" }, { from: root, to: resolver, label: "ask .example servers", back: true }]
  - say: The .example servers point to the name server Spoke runs for its own names.
    send: [{ from: resolver, to: tld, label: "api.spoke.example?" }, { from: tld, to: resolver, label: "ask Spoke's server", back: true }]
  - say: Spoke's name server knows the answer, and says how long it may be kept.
    send: [{ from: resolver, to: spoke, label: "api.spoke.example?" }, { from: spoke, to: resolver, label: "203.0.113.10, keep 300 s", back: true }]
  - say: The resolver saves the answer in its cache, then hands it to the app.
    set: { resolver: "cache: 203.0.113.10" }
    send: { from: resolver, to: app, label: "203.0.113.10", back: true }
  - say: The next rider who asks within 300 seconds gets the answer straight from the cache. No more trips.
    focus: [resolver]
```

### DNS records and TTL

A DNS answer is made of **records**. Three types matter most:

| Record | Holds | Example |
|---|---|---|
| `A` | an IPv4 address | `api.spoke.example` → `203.0.113.10` |
| `AAAA` | an IPv6 address | `api.spoke.example` → `2001:db8::10` |
| `CNAME` | another name to look up instead | `www.spoke.example` → `spoke.example` |

```callout jargon
**TTL** (time to live) is how many seconds a DNS answer may be kept in a cache. A **cache** is a saved copy of an answer, kept so you need not ask again.
```

The `dig` command asks DNS directly. Here is a real run for `example.com` and `www.github.com`. Your addresses and TTLs will differ, because they change over time and by place.

```bash
dig +noall +answer example.com A
dig +noall +answer www.github.com
```

```text
example.com.		284	IN	A	104.20.23.154
example.com.		284	IN	A	172.66.147.243
www.github.com.		254	IN	CNAME	github.com.
github.com.		15	IN	A	4.186.157.192
```

Read each line as: name, TTL in seconds, `IN` (internet), record type, value. `example.com` has two addresses, so clients can use either. `www.github.com` is a CNAME: "look up `github.com` instead".

TTL is a trade-off. A long TTL means fewer lookups. A short TTL means a change of address reaches everyone sooner. Before moving a server, teams lower the TTL a day ahead.

Java keeps its own DNS cache too, inside the JVM, and it ignores the TTL. By default it keeps a found address for 30 seconds and a failed lookup for 10. The security property `networkaddress.cache.ttl` changes the first number. A JVM that caches forever keeps calling an old address long after DNS has moved on.

### TCP: a reliable, ordered stream

Packets can get lost, copied, or arrive out of order. Your program does not want to deal with that. **TCP** deals with it for you.

```callout jargon
**TCP** (Transmission Control Protocol) turns unreliable packets into a reliable stream of bytes. It numbers every byte, resends anything lost, and puts everything back in order.
```

TCP's promise: bytes arrive complete and in the order they were sent, or you get an error. The receiver sends back **acknowledgements**, little "got it up to here" notes. Anything not acknowledged in time is sent again.

HTTP/1.1, HTTP/2 and database connections all ride on TCP. That is why your Java code reads a neat stream and never sees a packet.

### The three-way handshake

A TCP connection must be opened before data flows. That opening is the **three-way handshake**:

1. The client sends **SYN** ("let's talk, my numbers start here").
2. The server answers **SYN-ACK** ("got it, and my numbers start here").
3. The client sends **ACK** ("got yours"). It can send its first data right behind it.

So a new connection costs one full **round trip** before the first request can leave.

```callout jargon
A **round trip** is a message going there and the reply coming back. The time it takes is the **RTT** (round-trip time). From a phone to a server in another country, 80 ms is normal.
```

### UDP: fast and forgetful

**UDP** (User Datagram Protocol) is TCP's simpler cousin. It sends single packets with no handshake, no resending and no ordering. A lost packet stays lost.

That sounds bad, but it suits data where late is useless. Live video calls, online games and DNS lookups use UDP. A bike's GPS ping every few seconds could too: if one ping is lost, the next one is fresher anyway.

| | TCP | UDP |
|---|---|---|
| Handshake first | yes, one round trip | no |
| Lost data | sent again | gone |
| Order | kept | not kept |
| Good for | web pages, APIs, databases | calls, games, DNS, live pings |

### TLS: a private, checked tunnel

On café Wi-Fi, anyone nearby can read plain TCP packets. **TLS** wraps the TCP stream so nobody in the middle can read it or change it.

```callout jargon
**TLS** (Transport Layer Security) encrypts a connection and proves who the server is. **Encrypt** means scramble with a secret key, so only the two ends can unscramble it. **HTTPS** is HTTP sent inside TLS, on port 443.
```

TLS gives three things:

- **Privacy:** snoopers see scrambled bytes.
- **Integrity:** a changed byte is detected, so nobody can quietly edit a fare.
- **Identity:** the app knows it is talking to the real Spoke, not an impostor.

### Certificates and the chain of trust

How does the app know the server is really Spoke? The server shows a **certificate**.

```callout jargon
A **certificate** is a small file that says "this key belongs to `api.spoke.example`", signed by someone trusted. That someone is a **certificate authority** (CA), a company whose job is checking who owns a name.
```

A signature can only be checked if you already trust the signer. Your phone and your JDK ship with a list of over a hundred trusted root CAs. So the check runs like a chain:

1. Spoke's certificate is signed by an **intermediate** CA.
2. The intermediate's certificate is signed by a **root** CA.
3. The root is in the phone's trusted list. Chain complete.

The app also checks that the name matches and that today's date is inside the certificate's dates. Here is the certificate part of a real `curl -v https://example.com` (trimmed; the dates change every few months):

```text
*  subject: CN=example.com
*  start date: Sep 26 22:49:11 2026 GMT
*  expire date: Dec 25 22:56:35 2026 GMT
*  subjectAltName: host "example.com" matched cert's "example.com"
*  issuer: C=US; O=SSL Corporation; CN=Cloudflare TLS Issuing ECC CA 3
*  SSL certificate verify ok.
```

`subject` is who the certificate is for. `issuer` is the CA that signed it. "verify ok" means the chain, the name and the dates all passed.

```callout analogy
A certificate is like a passport. You do not trust it because the traveller says so. You trust it because a government you trust stamped it, and the photo matches the face in front of you.
```

### The TLS handshake

Before any HTTP flows, the two sides run a **TLS handshake**. In TLS 1.3, the version in use today, it takes one round trip:

1. The app says hello, lists what it supports, and sends its half of a key.
2. The server replies with its half of the key and its certificate, already encrypted.
3. Both sides now share a secret key that never crossed the wire. Everything after is encrypted.

The older TLS 1.2 took two round trips. Here is the handshake in a real `curl -v` run (trimmed):

```text
* Connected to example.com (2606:4700:83b5:72db:f276:42a:ef6b:ff98) port 443
* ALPN: curl offers h2,http/1.1
* (304) (OUT), TLS handshake, Client hello (1):
* (304) (IN), TLS handshake, Server hello (2):
* (304) (IN), TLS handshake, Certificate (11):
* (304) (IN), TLS handshake, CERT verify (15):
* (304) (IN), TLS handshake, Finished (20):
* (304) (OUT), TLS handshake, Finished (20):
* SSL connection using TLSv1.3 / AEAD-CHACHA20-POLY1305-SHA256 / [blank] / UNDEF
* ALPN: server accepted h2
```

`ALPN` is the two sides agreeing on the HTTP version inside the tunnel. Here they picked `h2`, which is HTTP/2.

### What HTTPS protects, and what it does not

```callout jargon
The next ticket teaches HTTP itself. For now, a request has a **method**, like `GET` for "read", and a **path**, like `/rentals/rental-17`. It has **headers**, short notes such as `Authorization`, and a **body**, the data. The `Authorization` header carries the rider's **token**, a secret that proves who they are. The answer starts with a **status**, such as `200 OK`.
```

HTTPS hides the method, path, headers and body. A snooper cannot see `/rentals/rental-17`, the rider's token, or the fare.

It does **not** hide everything:

- The server's **IP address** is on every packet.
- The **host name** usually leaks, in the DNS lookup and in the TLS hello.
- **When** you talk and **how much** you send are visible.

And it only guards the trip. It does not stop a bug in Spoke's code, a stolen password, or malware on the phone. Once data arrives, keeping it safe is your job.

### Keep-alive and connection pools

You saw that a new HTTPS connection costs DNS, TCP and TLS before the first request. So reuse the connection for the next request instead of closing it. That is **keep-alive**, and HTTP/1.1 does it by default.

```callout jargon
A **connection pool** is a set of open connections kept ready for reuse. A request borrows one, uses it, and gives it back open, so the next request skips the handshakes.
```

This toy pool shows the idea. It only "opens" a connection when none is free.

```java run
import java.util.ArrayDeque;
import java.util.Deque;

public class Main {
    record Connection(int id) {}

    static class Pool {
        private final Deque<Connection> idle = new ArrayDeque<>();
        private int opened = 0;

        Connection borrow() {
            if (!idle.isEmpty()) {
                return idle.pop();                 // reuse: no handshake
            }
            opened++;
            System.out.println("  handshake: open connection " + opened);
            return new Connection(opened);
        }

        void giveBack(Connection connection) {
            idle.push(connection);                 // keep it open for the next request
        }
    }

    public static void main(String[] args) {
        Pool pool = new Pool();
        String[] paths = {"/stations", "/bikes/B-1042", "/rentals/rental-17", "/stations/harbor"};
        for (String path : paths) {
            Connection connection = pool.borrow();
            System.out.println("GET " + path + " on connection " + connection.id());
            pool.giveBack(connection);
        }
        System.out.println("connections opened: " + pool.opened);
    }
}
```

Four requests, one handshake. Borrowing and returning are O(1) time, because a deque adds and removes at its end in constant time. The pool uses O(c) space for c open connections. Real pools also cap c, and close connections that sit idle too long.

Database drivers use pools too. Spring Boot's default database pool is called HikariCP.

### Syntax: one shared HttpClient

```java
import java.net.http.HttpClient;
import java.time.Duration;

class SpokeHttp {
    // One client for the whole app. It keeps a pool of open connections inside.
    static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
}
```

Java's `HttpClient` keeps its own connection pool. Make one client and share it. A new client per request starts with an empty pool, so every request pays every handshake again.

### HTTP/1.1, HTTP/2 and HTTP/3

The three versions carry the same methods, paths, headers and bodies. They differ in how they use connections.

- **HTTP/1.1:** one request at a time per connection. The next waits until the answer arrives. Browsers open about six connections to work around it.
- **HTTP/2:** many requests share one connection at the same moment. This is **multiplexing**. It also squeezes repeated headers.
- **HTTP/3:** like HTTP/2, but it runs on **QUIC**, a protocol built on UDP. QUIC sets up the connection and encryption together, often in one round trip.

```callout jargon
**Multiplexing** means several conversations share one line at once, taking turns in small slices. **Head-of-line blocking** is when one lost or slow item holds up everything queued behind it.
```

Why HTTP/3? In HTTP/2, all requests share one TCP stream. One lost packet makes TCP hold back every later byte until the resend arrives, even bytes for other requests. QUIC keeps each request's stream separate, so a loss only delays its own request.

Six station photos on one connection, 80 ms away:

```java run
public class Main {
    public static void main(String[] args) {
        int rttMillis = 80;
        int images = 6;   // six station photos on one screen

        int oneAtATime = images * rttMillis;   // HTTP/1.1 on one connection: ask, wait, ask, wait...
        int multiplexed = 1 * rttMillis;       // HTTP/2: all six requests go out together

        System.out.println("HTTP/1.1, one connection: " + oneAtATime + " ms");
        System.out.println("HTTP/2, one connection:   " + multiplexed + " ms");
    }
}
```

This ignores the time to send the bytes themselves. For small files on a fast line that is tiny, as the next card shows.

### Latency and bandwidth

Two different numbers describe "how fast" a network is.

```callout jargon
**Latency** is how long one bit takes to get there, in milliseconds. **Bandwidth** is how many bits can flow per second, in megabits per second (Mbit/s).
```

```callout analogy
Latency is how long the motorway is. Bandwidth is how many lanes it has. More lanes let more cars through per hour, but each car still drives the whole length.
```

Time for one transfer is roughly latency plus size divided by bandwidth.

```java run
import java.util.Locale;

public class Main {
    static double millis(double latencyMs, double megabitsPerSecond, double kilobytes) {
        double bits = kilobytes * 1000 * 8;
        double sendMs = bits / (megabitsPerSecond * 1_000_000) * 1000;
        return latencyMs + sendMs;
    }

    static void show(String label, double ms) {
        System.out.println(label + String.format(Locale.ROOT, "%.1f ms", ms));
    }

    public static void main(String[] args) {
        show("2 KB JSON,   80 ms away,  50 Mbit/s: ", millis(80, 50, 2));
        show("2 KB JSON,   80 ms away, 500 Mbit/s: ", millis(80, 500, 2));
        show("2 KB JSON,   10 ms away,  50 Mbit/s: ", millis(10, 50, 2));
        show("50 MB maps,  80 ms away,  50 Mbit/s: ", millis(80, 50, 50_000));
        show("50 MB maps,  80 ms away, 500 Mbit/s: ", millis(80, 500, 50_000));
    }
}
```

For small API answers, latency is almost everything. Ten times the bandwidth saves nothing. Moving the server closer, or making fewer round trips, is what helps. For big downloads, bandwidth wins.

### A round-trip budget

Now you can explain Kakashi's missing seconds. Count the round trips, then multiply by the RTT.

```java run
public class Main {
    public static void main(String[] args) {
        int rttMillis = 80;      // one round trip from the rider's phone to Spoke and back
        int serverMillis = 20;   // time the server spends thinking

        int dns = 1, tcp = 1, tls = 1, http = 1;   // round trips for each step
        int fresh = (dns + tcp + tls + http) * rttMillis + serverMillis;
        int reused = http * rttMillis + serverMillis;

        System.out.println("new connection:    " + fresh + " ms");
        System.out.println("reused connection: " + reused + " ms");

        int requests = 10;
        System.out.println("10 requests, new connection each: " + requests * fresh + " ms");
        System.out.println("10 requests, one kept-alive conn: " + (fresh + (requests - 1) * reused) + " ms");
    }
}
```

The server's 20 ms is the smallest part. An app that opens a new connection for each of ten calls spends 3.4 seconds. One kept-alive connection cuts that to 1.24 seconds. Doing the calls in parallel over HTTP/2 cuts it further.

The math is simple: total time is about (round trips) × RTT + server time. Each saved round trip saves a whole RTT.

### Load balancers and reverse proxies

Big services do not point DNS straight at one server. They put a **load balancer** in front.

```callout jargon
A **reverse proxy** is a server that takes requests from the internet and passes them on to servers behind it. A **load balancer** is a reverse proxy that spreads requests across many copies of a server.
```

The load balancer holds Spoke's public address and certificate. It usually ends the TLS tunnel itself, then forwards plain HTTP over the private network. It checks which servers are healthy and skips the broken ones. A later module covers scaling with it.

### When it breaks: four errors

Each network error points at one step of the journey. Read it before you guess.

| Error | Step that failed | Usual cause |
|---|---|---|
| `UnknownHostException` | DNS | a typo in the name, no internet, or the name does not exist |
| `ConnectException: Connection refused` | TCP | the computer answered, but nothing listens on that port |
| `Connect timed out` | TCP | no answer at all: wrong address, a firewall dropping packets, or the machine is down |
| `SSLHandshakeException` | TLS | the certificate expired, has the wrong name, or is signed by nobody trusted |

These are from real Java runs on a Mac. The words after a colon can differ on other systems.

```text
java.net.UnknownHostException: api.spoke.invalid: nodename nor servname provided, or not known
java.net.ConnectException: Connection refused
java.net.SocketTimeoutException: Connect timed out
java.net.http.HttpConnectTimeoutException: HTTP connect timed out
javax.net.ssl.SSLHandshakeException: (certificate_expired) PKIX path validation failed: java.security.cert.CertPathValidatorException: validity check failed
javax.net.ssl.SSLHandshakeException: (certificate_unknown) No subject alternative DNS name matching wrong.host.badssl.com found.
```

Refused is fast: the other computer said "no" at once. A connect timeout is slow: nobody said anything, so you waited the whole limit. A timeout *after* connecting is different again: the server got the request but is too slow to answer.

```callout tip
Java's `HttpClient` sometimes throws a bare `java.net.ConnectException` with no message for a refused connection. Read the exception's type, not only its text.
```

### In an interview

"What happens when you type a URL and press Enter?" is one of the most asked interview questions. Answer it as the journey: DNS lookup with caching, the TCP handshake, and the TLS handshake with its certificate check. Then the HTTP request, the response, and reuse of the connection.

Spot this topic when a question mentions latency, "the first request is slow", timeouts, or choosing a protocol. Classic questions:

- What happens when you type a URL into a browser?
- TCP versus UDP: which would you use for a video call, a payment, a game?
- Why is HTTP/2 faster than HTTP/1.1, and why does HTTP/3 use UDP?
- A service call times out. How do you find out why? Check DNS (`dig`), then the port (`nc -vz`), then TLS and HTTP (`curl -v`).
- How does HTTPS know a server is real? Explain certificates and the chain of trust.

## Worked example

Find where time goes, the way Kakashi would, with tools on your own laptop. First, a tiny web server. Python ships one. In an empty folder, run:

```bash
echo "<h1>Harbor</h1>" > index.html
python3 -m http.server 8765 --bind 127.0.0.1
```

In a second terminal, ask it for the page with `curl -v`. The `-v` means **verbose**: show every step. Here is a real run, trimmed:

```bash
curl -v http://localhost:8765/
```

```text
* Host localhost:8765 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8765...
* connect to ::1 port 8765 from ::1 port 60341 failed: Connection refused
*   Trying 127.0.0.1:8765...
* Connected to localhost (127.0.0.1) port 8765
> GET / HTTP/1.1
> Host: localhost:8765
> User-Agent: curl/8.7.1
> Accept: */*
>
< HTTP/1.0 200 OK
< Server: SimpleHTTP/0.6 Python/3.9.6
< Content-type: text/html
< Content-Length: 16
<
* Closing connection
<h1>Harbor</h1>
```

Read it top to bottom. It is the whole journey.

1. **DNS.** `localhost` resolved to two addresses: `::1` (IPv6) and `127.0.0.1` (IPv4). For `localhost` no resolver is asked; the computer knows it.
2. **TCP, first try.** curl tried IPv6 first. Connection refused: nothing listens on `::1`, because the server was started with `--bind 127.0.0.1`.
3. **TCP, second try.** It tried IPv4 and connected. Clients try every address they get.
4. **HTTP.** Lines starting `>` are the request curl sent. Lines starting `<` are the response.
5. **Close.** This little server speaks HTTP/1.0, which closes after each answer. So a second request would need a new handshake.

Now stop the server with Ctrl+C, and probe the port again:

```bash
nc -vz 127.0.0.1 8765
curl http://127.0.0.1:8765/
```

```text
nc: connectx to 127.0.0.1 port 8765 (tcp) failed: Connection refused
curl: (7) Failed to connect to 127.0.0.1 port 8765 after 0 ms: Couldn't connect to server
```

`nc -vz` only tries to open a TCP connection, then hangs up. It is the quickest way to ask "is anyone listening?". Failing "after 0 ms" tells you it was refused, not timed out.

Last, time a real HTTPS request. curl can print how long each step took. A real run against `example.com` (your times will differ):

```bash
curl -s -o /dev/null -w "dns: %{time_namelookup}s\ntcp: %{time_connect}s\ntls: %{time_appconnect}s\nfirst byte: %{time_starttransfer}s\n" https://example.com/
```

```text
dns: 0.013686s
tcp: 0.038644s
tls: 0.075577s
first byte: 0.111448s
```

Each number is the time since the start. So DNS took 14 ms. The TCP handshake took about 25 ms more, and TLS about 37 ms more. The first byte of the answer came about 36 ms after that. Three of the four steps happen before the server even sees the request.

Ask for the same page twice in one command, and curl keeps the connection open between them. Trimmed output of `curl -v --http1.1 https://example.com/ https://example.com/`:

```text
* Connected to example.com (2606:4700:83b5:72db:f276:42a:ef6b:ff98) port 443
* Connection #0 to host example.com left intact
* Re-using existing connection with host example.com
* Connection #0 to host example.com left intact
```

"Re-using existing connection" is keep-alive at work: the second request skipped DNS, TCP and TLS.

## Try it

This is the round-trip budget from above. Change the numbers and predict each answer before you run.

```java run
public class Main {
    public static void main(String[] args) {
        int rttMillis = 80;      // phone to server and back
        int serverMillis = 20;   // server thinking time
        int requests = 10;       // calls the app makes when it opens

        int handshakeTrips = 3;  // DNS + TCP + TLS 1.3
        int fresh = (handshakeTrips + 1) * rttMillis + serverMillis;
        int reused = rttMillis + serverMillis;

        System.out.println("new connection every time: " + requests * fresh + " ms");
        System.out.println("one kept-alive connection: " + (fresh + (requests - 1) * reused) + " ms");
    }
}
```

1. The rider is near the server, so `rttMillis` is 10. Which saves more now, keep-alive or a faster server?
2. Use TLS 1.2 by making `handshakeTrips` 4. How much slower is the first request?
3. The DNS answer is cached on the phone. Make `handshakeTrips` 2.

On your own laptop, try `dig +noall +answer github.com`, then run it again a few seconds later. Watch the TTL count down.

## Recap

- DNS turns a name into an IP address, and caches the answer for its TTL. A records hold IPv4, AAAA hold IPv6, and CNAME points to another name.
- An IP address finds a computer and a port finds the program on it. `127.0.0.1` is this computer, and a server bound to `0.0.0.0` listens on every network card.
- TCP gives a reliable, ordered stream but costs a handshake round trip. UDP skips the handshake and the resends.
- TLS encrypts the connection and checks the server's certificate against a chain of trust. HTTPS hides paths, headers and bodies, but not the IP address or the host name.
- Total time is about round trips × RTT, so reuse connections with keep-alive and pools. Read each network error by the step that failed.

## Common mistakes

**1. A server that listens only on 127.0.0.1.**

```bash
python3 -m http.server 8765 --bind 127.0.0.1
curl http://192.168.1.8:8765/
```

```text
curl: (7) Failed to connect to 192.168.1.8 port 8765 after 4 ms: Couldn't connect to server
```

From the same laptop, `http://127.0.0.1:8765/` works. From its network address, or from another machine, it is refused. Loopback only accepts connections from the same computer. Fix: listen on `0.0.0.0` when other machines must reach the server, as in a container or behind a load balancer. `lsof` then shows `TCP *:8765 (LISTEN)`.

**2. A new HttpClient for every request.**

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class StationsClient {
    String fetch() throws Exception {
        HttpClient client = HttpClient.newHttpClient();   // a fresh, empty pool every call
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.spoke.example/stations")).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
```

Nothing crashes. But each call pays DNS, TCP and TLS again: 340 ms instead of 100 ms in the budget above. Fix: create one `HttpClient` when the app starts, and share it, as in the Syntax card.

**3. Turning off certificate checks to "fix" a certificate error.**

```text
javax.net.ssl.SSLHandshakeException: (certificate_unknown) PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```

This means the certificate is not signed by anyone your JDK trusts, such as a self-signed test certificate. Online you will find code that "trusts all certificates". It makes the error vanish, and makes HTTPS worthless: any impostor is now accepted. Fix: get a certificate from a real CA, or add your test CA to a trust store used only in tests.

**4. No connect timeout.**

```bash
curl http://10.255.255.1/
```

```text
curl: (28) Failed to connect to 10.255.255.1 port 80 after 75008 ms: Couldn't connect to server
```

That address never answers. With no limit of its own, curl waited 75 seconds, until the Mac's system limit gave up. A rider would have closed the app long before. With `curl --connect-timeout 2` the same call fails `after 2006 ms`. Fix: always set a connect timeout, like `.connectTimeout(Duration.ofSeconds(2))` on `HttpClient`, plus a timeout on each request. Then show the rider a "Try again" button.

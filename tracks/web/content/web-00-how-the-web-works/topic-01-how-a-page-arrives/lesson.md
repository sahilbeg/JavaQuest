## Hook

It's Sunday at the FC Road stall. A college student picks up a coaster set and asks, "How much is this?" The person behind the table smiles and says, "₹150."

A question goes one way. An answer comes back. That's the whole deal.

The web works exactly like this. Your phone asks a question, and a computer somewhere far away answers it. Every page you have ever opened reached you this way. Once you can picture that trip, the rest of this course will make much more sense.

## Concept

Opening a page takes four helpers: a browser that asks, a server that answers, an address that says where to go, and the internet that carries everything in between.

### The browser: the one who asks

A **browser** is the app you use to open web pages. Chrome, Safari, Firefox and Edge are all browsers.

A browser has two jobs. First, it asks other computers for pages. Then it reads what comes back and draws it on your screen.

```callout jargon
A **browser** is an app that fetches web pages and draws them. A **client** is any program that asks another program for something. Your browser is a client.
```

### The server: the one who answers

A **server** is a computer that stays switched on, connected to the internet, waiting for questions. It keeps a website's files, and it sends them to anyone who asks.

A server is not a special kind of machine. It is usually an ordinary computer in a big building full of computers, called a **data centre**.

```callout analogy
The browser is the customer at the stall. The server is the person behind the counter, who knows every price and hands over what you ask for.
```

### The internet: the roads in between

The **internet** is a giant network of computers joined together by cables, Wi-Fi and mobile towers. It carries messages between your phone and the server.

Big messages are cut into small pieces called **packets**. Each packet travels on its own, and the pieces are put back together at the other end.

```callout jargon
A **network** is a group of computers that can send messages to each other. The **internet** is the network of all those networks, joined into one.
```

### URLs: the address of a page

Every page has an address, called a **URL**. Here is one, split into its parts:

| Part | In `https://dhaaga.in/prices` | What it says |
|---|---|---|
| Scheme | `https://` | Which rules to use when talking |
| Domain | `dhaaga.in` | Which server to ask |
| Path | `/prices` | Which page on that server |

A path of just `/` means the home page. So `https://dhaaga.in/` asks for Dhaaga's front page.

```callout jargon
**URL** stands for Uniform Resource Locator, a fancy name for "web address". A **domain** is the name of a website, like `dhaaga.in` or `wikipedia.org`.
```

### DNS: turning names into numbers

Computers on the internet don't find each other by name. They use numbers, called **IP addresses**, such as `203.0.113.10`.

People are bad at remembering numbers, so we use names. **DNS** is the internet's phone book. Your phone asks DNS, "What number is `dhaaga.in`?" DNS answers with the IP address. Then your phone can reach the server.

Your phone remembers the answer for a while, so it doesn't need to ask DNS again for every page.

```callout jargon
An **IP address** is the number that finds one computer on the internet. **DNS** (Domain Name System) turns a domain name into an IP address.
```

### HTTP: the language of asking and answering

Browsers and servers talk using a set of rules called **HTTP**. The browser sends a **request**. The server sends back a **response**.

A request names what it wants. `GET` means "please send me this":

```text
GET /prices HTTP/1.1
Host: dhaaga.in
```

The response starts with a status line, then some extra details called **headers**, then the page itself:

```text
HTTP/1.1 200 OK
Content-Type: text/html

<!doctype html>
<html lang="en">
...
```

```callout jargon
**HTTP** (HyperText Transfer Protocol) is the set of rules for asking and answering on the web. A **request** asks for something. A **response** answers it. **Headers** are extra lines of details, like "this is an HTML page".
```

### Status codes: how did it go?

Every response starts with a three-digit **status code**. It tells the browser how things went.

| Code | Meaning | When you see it |
|---|---|---|
| `200 OK` | Here it is | The page exists and came back |
| `301 Moved Permanently` | It lives somewhere else now | An old address points to a new one |
| `404 Not Found` | There's no such page | A typo in the path, or a deleted page |
| `500 Internal Server Error` | The server broke | A bug in the server's own code |

The first digit tells you the family. 2 means success. 3 means "go somewhere else". 4 means the request asked for something wrong. 5 means the server itself failed.

### HTTPS: the sealed envelope

The `s` in `https` stands for **secure**. With HTTPS, the request and response are scrambled on the way, so nobody in between can read them. Not the café's Wi-Fi, and not anyone snooping on the network.

```callout jargon
**Encryption** scrambles a message so only the sender and the receiver can read it. HTTPS is HTTP with encryption added.
```

Browsers show a small padlock or a settings icon next to an HTTPS address. A page that asks for a password or payment details must always use HTTPS.

### The whole trip

Watch a customer scan the QR code at the stall and open Dhaaga's page:

```animate
title: A page travels to a phone
kind: scene
nodes:
  - { id: phone, label: Phone browser, emoji: "📱", value: "blank", col: 0 }
  - { id: dns, label: DNS, emoji: "📒", row: 1, col: 1 }
  - { id: server, label: Dhaaga's server, emoji: "🖥️", value: "waiting", col: 2 }
steps:
  - say: A customer scans the QR code at the stall. The phone's browser now holds the URL https://dhaaga.in/.
    focus: [phone]
    set: { phone: "https://dhaaga.in/" }
  - say: The browser doesn't know where dhaaga.in lives. So it asks DNS, the internet's phone book.
    send: { from: phone, to: dns, label: "where is dhaaga.in?" }
  - say: DNS answers with the server's number, its IP address.
    send: { from: dns, to: phone, label: "203.0.113.10", back: true }
  - say: Now the browser sends an HTTP request to that number. GET / means "please send the home page".
    send: { from: phone, to: server, label: "GET /" }
    set: { server: "finding index.html" }
  - say: The server finds the page's file and sends it back with status 200, which means OK.
    send: { from: server, to: phone, label: "200 OK + index.html", back: true }
    set: { server: "waiting", phone: "reading HTML" }
  - say: The page mentions a photo of a coaster. The browser asks for that file too, with a second request.
    send: [{ from: phone, to: server, label: "GET /coaster.jpg" }, { from: server, to: phone, label: "200 OK + photo", back: true }]
  - say: Every piece has arrived. The browser draws the page, and the customer sees Dhaaga on their phone.
    set: { phone: "page drawn" }
    focus: [phone]
```

### One page, many requests

A page is rarely one file. The HTML arrives first. It mentions other files: a stylesheet for colours, some JavaScript, and photos. The browser asks for each one with its own request.

That's why a page can half-work. If one photo is missing, that one request gets a `404`, and you see a broken image. The rest of the page still loads fine.

## Worked example

Mrs. Joshi wants to check prices before she visits the stall. She types `https://dhaaga.in/prices` into her phone.

1. Her browser splits the URL. The scheme is `https://`, the domain is `dhaaga.in`, and the path is `/prices`.
2. The browser asks DNS for the IP address of `dhaaga.in` and gets `203.0.113.10`.
3. It opens a secure HTTPS connection to that server.
4. It sends the request `GET /prices`.
5. The server finds the prices page and answers `200 OK`, with the HTML.
6. The HTML mentions a stylesheet and two photos. The browser sends three more requests, and each one comes back `200 OK`.
7. The browser draws the page. Mrs. Joshi sees that a tote bag costs ₹899.

The next day she types `https://dhaaga.in/prises` by mistake. Steps 1 to 4 happen just the same. But the server has no page at `/prises`, so it answers `404 Not Found`. Nothing is broken. She asked for a page that doesn't exist.

## Try it

Look at real URLs and responses in your own browser.

1. Open any website you use often.
2. Click the address bar and look at the whole URL. Find the scheme, the domain and the path.
3. Look for the padlock or settings icon next to the address. That means the page came over HTTPS.
4. Click a link on the site and watch the path change, while the domain stays the same.
5. Now add `/this-page-does-not-exist` to the end of the domain and press Enter.

Most sites show a "Page not found" message. Behind the scenes, the server answered with status `404`.

```callout tip
In the next ticket you'll meet the browser's own toolbox, which shows every request and its status code. For now, just notice the URL changing as you click.
```

## Recap

- A **browser** asks for pages, and a **server** answers with the files.
- A **URL** has a scheme (`https://`), a domain (`dhaaga.in`) and a path (`/prices`).
- **DNS** turns a domain name into an **IP address**, so the browser can find the server.
- Browsers and servers talk in **HTTP**: a request, then a response with a **status code** like `200` or `404`.
- **HTTPS** encrypts the trip, and one page usually needs many requests.

## Common mistakes

**1. "The internet and the web are the same thing."**

The internet is the network of cables, Wi-Fi and towers. The web is one thing that runs on it: pages linked to pages, sent over HTTP. WhatsApp messages and email also travel over the internet, but they aren't web pages.

**2. "A 404 means the internet is down."**

```text
HTTP/1.1 404 Not Found
```

If you got a 404, the server is up and it answered you. It just has no page at that path. Check the URL for typos, such as `/prises` instead of `/prices`.

**3. Putting a space in a URL.**

```text
https://dhaaga.in/price list
```

URLs can't hold spaces. The browser may treat the text as a search, or turn the space into `%20`, giving a path the server doesn't know. Website paths use dashes instead: `/price-list`.

**4. Typing payment details on a page without HTTPS.**

```text
http://dhaaga-offers.example/pay
```

That address starts with `http://`, not `https://`. Anything typed there travels unscrambled, so people on the same network could read it. Only enter private details when the address starts with `https://`.

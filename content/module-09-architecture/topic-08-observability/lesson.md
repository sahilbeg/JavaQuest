## Hook

A smoke alarm does not tell you the kitchen is on fire because it understands cooking. It just measures smoke, all the time, and screams when the number is too high. Without one, you find out when the neighbours call.

Last night Spoke had no smoke alarm. For 40 minutes riders could not end their rides, and the team learned about it from angry tweets. In this ticket you give Spoke alarms, dashboards and a trail of clues. They tell you something is wrong, and where, before riders do.

## Concept

### Monitoring vs observability

You already debug on your laptop with breakpoints, as in [debugging](/t/module-06-craft/topic-11-debugging). In production you cannot pause a server that 100,000 riders depend on. You can only read what it sends out.

```callout jargon
**Monitoring** means watching numbers you chose in advance and alerting when they cross a line, such as "errors above 1%". **Observability** is how well you can work out what is happening inside a system from its outputs, including for questions nobody thought of before.
```

Monitoring answers known questions: "is the error rate high?". Observability answers new ones: "why do only Harbor riders on Android fail to end rides since 21:40?". You need both. Monitoring wakes you up. Observability lets you find the cause without shipping new code first.

### Three signals: logs, metrics and traces

Production code sends out three kinds of signal. Each answers a different question.

| Signal | What it is | Answers | Cost |
|---|---|---|---|
| **Metrics** | numbers counted over time, such as requests per second | *Is* something wrong, and how much? | cheap: a few numbers per minute |
| **Traces** | the path of one request through every service, with timings | *Where* is it slow or failing? | medium: usually sampled |
| **Logs** | text records of single events, with details | *Why* did this request fail? | expensive: every line is stored |

A typical hunt uses all three in order. A metric alert says end-ride errors jumped. A trace shows the time goes in the payment call. The logs of that trace show the exact exception.

The trick is joining them up. Every log line and every trace carries the same **trace id**, so you can jump from one signal to the next.

### Logs in production: levels, volume and cost

You learned levels and `{}` placeholders in [logging](/t/module-06-craft/topic-12-logging). In production, three more rules matter.

- **ERROR should mean "a human must act".** If a page of ERRORs is normal, nobody reads them, and the real one gets lost.
- **Log an error once, where it is handled.** Logging the same exception at every layer triples the noise.
- **Change levels without a redeploy.** Spring Boot Actuator's `/actuator/loggers` endpoint can switch one package to DEBUG for ten minutes, then back.

Logs cost real money. Log tools charge for every gigabyte stored and searched. Say someone logs one INFO line per GPS ping. [System design](/t/module-09-architecture/topic-06-system-design) worked out about 2.4 million pings a day in Riverton. At 300 bytes a line, that is 720 MB a day, for lines nobody reads.

So log **events that matter** (a ride ended, a payment failed), not every loop pass. Keep logs for a fixed time, such as 30 days. For very chatty paths, keep only a sample, such as 1 line in 100.

### Structured logs: JSON, not sentences

A plain log line is a sentence for humans: `ride rental-17 ended for rider-7 after 12 min, fare 460`. To find every ride over 400 cents, a tool has to guess where the numbers are.

A **structured log** writes each line as key-value pairs, usually one JSON object per line:

```json
{"time":"2026-11-17T21:42:07.112Z","level":"INFO","logger":"jq.spoke.rentals.RentalService","message":"ride ended","rentalId":"rental-17","riderId":"rider-7","minutes":12,"fareCents":460,"traceId":"4bf92f3577b34da6a3ce929d0e0e4736"}
```

Now a log tool can answer `fareCents > 400 AND level = "ERROR"` exactly, count by `riderId`, or show every line of one trace.

```java nocompile
log.atInfo()
   .addKeyValue("rentalId", rentalId)
   .addKeyValue("fareCents", fareCents)
   .log("ride ended");
```

SLF4J 2's fluent API attaches keys to one line. Spring Boot 3.4 and later can print every line as JSON with one setting, `logging.structured.format.console=ecs`. ECS is one common JSON layout; `logstash` is another.

### Correlation ids with MDC

One rider's request touches many log lines, on many threads and many servers. A **correlation id** ties them together.

```callout jargon
A **correlation id** (or request id) is a unique label given to one request when it arrives. Every log line written while handling it carries that label, so you can pull out the whole story with one search.
```

You met **MDC** in the logging ticket. In a real service, a servlet filter puts the id in the MDC when a request starts. It removes it when the request ends. The log pattern prints it with `%X{requestId}`. If the caller sent an id, reuse it, so the trail continues across services.

The trap: MDC lives on one thread. Work handed to a thread pool does not see it.

```java run
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.MDC;

public class Main {
    // Prints what a log pattern with %thread and %X{requestId} would show.
    static void logLine(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] requestId=" + MDC.get("requestId") + " " + message);
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor(task -> new Thread(task, "payments"));
        try (MDC.MDCCloseable id = MDC.putCloseable("requestId", "req-8f3a")) {
            logLine("POST /rentals/rental-17/end");

            pool.submit(() -> logLine("charging card")).get();      // another thread: no MDC

            Map<String, String> context = MDC.getCopyOfContextMap();   // copy it across
            pool.submit(() -> {
                MDC.setContextMap(context);
                try {
                    logLine("charging card");
                } finally {
                    MDC.clear();                                       // pool threads are reused
                }
            }).get();
        } finally {
            pool.shutdown();
        }
    }
}
```

```text
[main] requestId=req-8f3a POST /rentals/rental-17/end
[payments] requestId=null charging card
[payments] requestId=req-8f3a charging card
```

The second line lost its id, so a search for `req-8f3a` would miss the payment step entirely. Copy the context into the task, and clear it in `finally`. Tracing libraries do this copying for you.

### What never goes in a log

Logs are copied to many machines, kept for weeks and read by many people. Treat every line as if it might leak.

- **Secrets:** passwords, API keys, `Authorization` headers, session tokens, JWTs. Anyone holding a token can act as that rider.
- **Payment data:** card numbers and security codes. Mask the card to its last 4 digits.
- **Personal data:** email, phone number, home address, and a rider's exact GPS trail, which shows where they live. Privacy laws such as GDPR let riders ask you to delete their data, and that includes copies in logs.

Log the **id** instead: `rider-7`, not their email. Watch for leaks you did not write yourself: a whole request object logged with `toString()`, or a URL whose query string holds a token.

```callout warning
Once a secret is in a log, you cannot pull it back from every copy. Rotate the secret, meaning replace it with a new one, and treat the old one as stolen.
```

### Metrics: cheap numbers over time

A **metric** is a named number measured over and over, such as "requests to end a ride, per second". Each measurement is stored with its time, which makes a **time series**.

Metrics are cheap because they are numbers, not text. Ten thousand requests a second still make one counter that is read every 15 seconds. That is why alerts and dashboards are built on metrics, not logs.

They lose detail on purpose. A metric can say "57 end-ride requests failed this minute". It cannot say which riders. That is the logs' job.

### Counter, gauge, timer and histogram

Monitoring libraries offer a few kinds of metric.

```callout jargon
A **counter** only goes up, such as total rides ended. A **gauge** is a value right now that goes up and down, such as bikes docked at Harbor. A **timer** records how long things took, both how many and how long in total. A **histogram** counts values into buckets, such as 0-100 ms, 100-250 ms, 250-500 ms.
```

| Spoke metric | Kind | Why that kind |
|---|---|---|
| `spoke.rides.ended` | counter | you want "rides per minute", the rate of increase |
| `spoke.bikes.available` | gauge | it rises and falls as bikes dock and leave |
| `http.server.requests` | timer with histogram | you want request rate and latency percentiles |
| `spoke.payment.charge` | timer with histogram | payment latency is a common cause of slow end-ride |

Trap: never use a gauge for something you count. A gauge of "errors so far" read every 15 seconds misses a burst that came and went in between. A counter never forgets, and the monitoring system works out its rate.

### Why percentiles need histograms

You saw in [performance](/t/module-09-architecture/topic-07-performance-profiling) that p99 matters more than the average. But Spoke runs six API servers. Each could compute its own p99, yet you cannot average six p99s: the result means nothing.

Histograms fix this. Each server counts how many requests landed in each bucket. Counts *can* be added: 9,312 fast requests on server A plus 8,977 on server B. The monitoring system adds the buckets from all servers, then estimates p99 from the total.

The estimate is only as precise as the buckets. If the buckets are 250 ms and 500 ms, it only knows p99 is somewhere between them. Pick bucket edges near your targets, such as one at 300 ms if that is the budget.

### Labels and the cardinality explosion

Metrics can carry **labels** (Micrometer calls them **tags**), such as `uri`, `method` and `status`. Each distinct combination of label values is its own time series.

```callout jargon
**Cardinality** is how many different values something can take. A label with high cardinality, such as a rider id, multiplies the number of time series.
```

Do the maths for `http.server.requests`. Spoke has about 20 URI templates, 4 methods and 8 status codes: 20 × 4 × 8 = 640 series. Fine.

Now someone adds a `riderId` label "to debug one rider". 640 × 100,000 riders = 64 million series. The metrics database runs out of memory and the dashboards die, often during the incident you needed them for.

Rules:

- Labels have a small, fixed set of values: status, method, bike type, station id (300 at most).
- Use the URI **template** `/rentals/{id}/end`, never the real path `/rentals/rental-17/end`. Spring does this for you.
- Per-rider questions go to logs and traces, which are built for many unique values.

### RED, USE and the four golden signals

Three short checklists tell you which metrics to have before an incident, not after.

- **RED**, for every service or endpoint: **R**ate (requests per second), **E**rrors (failed requests per second), **D**uration (latency percentiles).
- **USE**, for every resource, such as CPU, memory or a connection pool: **U**tilisation (how busy), **S**aturation (work queued), **E**rrors.
- **The four golden signals**, from Google's SRE book: **latency**, **traffic**, **errors** and **saturation**. It is RED plus saturation.

```callout jargon
**Saturation** means work waiting because a resource is full, such as requests queued for a database connection. It often warns you *before* latency rises.
```

For last night: RED on `POST /rentals/{id}/end` would have shown errors leaping. USE on the payment connection pool would have shown it 100% busy with a long queue.

### Syntax: Micrometer

**Micrometer** is the metrics facade for Java, the way SLF4J is for logging. Your code records metrics through Micrometer, and a registry sends them to Prometheus, Datadog or another system. Spring Boot Actuator sets it up and already times every HTTP request.

```java nocompile
Counter ridesEnded = Counter.builder("spoke.rides.ended")
        .tag("bike_type", "EBIKE")              // a small, fixed set of values
        .register(registry);
ridesEnded.increment();

Timer charge = Timer.builder("spoke.payment.charge")
        .publishPercentileHistogram()           // send buckets, so p99 works across servers
        .register(registry);
charge.record(() -> payments.charge("rider-7", 460));

Gauge.builder("spoke.bikes.available", fleet, Fleet::availableCount)
        .register(registry);                    // read from fleet each time it is scraped
```

- `registry` is a `MeterRegistry`, which Spring injects into your beans.
- A dotted name becomes `spoke_rides_ended_total` in Prometheus, which adds `_total` to counters.
- A gauge does not store a number. It reads the live value whenever metrics are collected.

### Prometheus scrapes, Grafana draws

**Prometheus** is a popular open-source metrics database. It uses a **pull** model: every 15 seconds or so, it calls each server's `/actuator/prometheus` endpoint and reads the current numbers.

```text
http_server_requests_seconds_count{method="POST",status="200",uri="/rentals/{id}/end"} 48211
http_server_requests_seconds_count{method="POST",status="503",uri="/rentals/{id}/end"} 114
spoke_bikes_available{station="harbor"} 7
```

Pull means a dead server is noticed at once, because the scrape fails. Push systems, where servers send metrics out, suit short jobs that end before anyone could pull.

You ask Prometheus questions in **PromQL**. Error ratio of ending rides over the last 5 minutes:

```text
sum(rate(http_server_requests_seconds_count{uri="/rentals/{id}/end",status=~"5.."}[5m]))
  / sum(rate(http_server_requests_seconds_count{uri="/rentals/{id}/end"}[5m]))
```

p99 latency, from the histogram buckets of every server:

```text
histogram_quantile(0.99, sum by (le) (rate(http_server_requests_seconds_bucket{uri="/rentals/{id}/end"}[5m])))
```

**Grafana** draws these queries as dashboards. A good service dashboard has one row per endpoint with its RED graphs, and one row for saturation: thread pools, connection pools, GC.

### Traces and spans

Ending a ride crosses several hops: the rider app, the Spoke API, the payment provider, the database. A log line on each server shows one piece. A **trace** shows the whole journey.

```callout jargon
A **trace** is the record of one request across every service it touched, under one **trace id**. A **span** is one timed step inside it, such as "charge card, 1,840 ms". Each span knows its **parent span**, the step that called it, so the spans form a tree.
```

A trace viewer draws the tree as a **waterfall**: one bar per span, indented under its parent, as long as it took.

```text
POST /rentals/rental-17/end         [=========================================] 2,010 ms
  RentalService.endRide              [========================================]  1,995 ms
    SELECT rental                     [=]                                          12 ms
    PaymentClient.charge                 [==================================]    1,840 ms
    UPDATE rental                                                          [=]     9 ms
```

The slow hop jumps out: 1,840 of 2,010 ms is the payment call. Logs alone would show five lines with five timestamps, and you would do the subtraction by hand.

### Syntax: the traceparent header

How does the payment service know it is part of the same trace? The caller sends the ids along in an HTTP header. The W3C standard header is `traceparent`:

```text
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
```

- `00`: the format version.
- `4bf92f35...4736`: the **trace id**, 32 hex characters, the same on every hop.
- `00f067aa0ba902b7`: the **parent id**, 16 hex characters, the span that made this call.
- `01`: flags. `01` means "sampled": this trace is being recorded.

Passing the header on at every hop is called **propagation**. If one service drops it, the trace breaks into two unrelated halves.

**OpenTelemetry** is the open standard and set of libraries for traces, metrics and logs. In Spring Boot, Micrometer Tracing with an OpenTelemetry bridge creates spans for incoming and outgoing HTTP calls. It also puts `traceId` and `spanId` into the MDC, so every log line carries them.

```animate
title: One trace id travels with the request
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱" }
  - { id: api, label: Spoke API, emoji: "🖥️", value: "no trace yet" }
  - { id: pay, label: Payment provider, emoji: "💳", row: 1, col: 2 }
  - { id: db, label: Database, emoji: "🗄️", row: 1, col: 0 }
  - { id: col, label: Trace collector, emoji: "🔭", value: "0 spans", row: 0, col: 4 }
steps:
  - say: The rider taps End ride. The request has no trace yet.
    send: { from: app, to: api, label: "POST /rentals/rental-17/end" }
  - say: The API starts trace 4bf9... and opens its first span for the whole request.
    set: { api: "trace 4bf9, span a1" }
  - say: The database call becomes a child span of a1. It takes 12 ms.
    send: [{ from: api, to: db, label: "SELECT rental" }, { from: db, to: api, label: "12 ms", back: true }]
  - say: The payment call sends traceparent with trace 4bf9 and parent a1, so the provider's span joins the same trace.
    send: { from: api, to: pay, label: "traceparent: 00-4bf9...-a1...-01" }
    set: { pay: "trace 4bf9, span b7" }
  - say: The provider is slow today. Its span lasts 1,840 ms.
    send: { from: pay, to: api, label: "1,840 ms", back: true }
  - say: Each service reports its spans. The collector joins them by trace id into one waterfall.
    send: [{ from: api, to: col, label: "3 spans" }, { from: pay, to: col, label: "1 span" }]
    set: { col: "4 spans, 1 trace" }
```

### Sampling: head vs tail

Recording every span of every request costs a lot of storage. So tracing systems keep only some traces. This is **sampling**.

- **Head sampling** decides at the first service, before anything happens: "keep 10% of traces". It is cheap and simple. The decision travels in the `01` flag. But it throws away 90% of the rare slow and failed requests too.
- **Tail sampling** waits until the trace is complete, then decides. For example: "keep every error, every trace over 1 s, and 1% of the rest". It keeps exactly the interesting ones, but a collector must hold every span in memory until the trace ends.

Many teams combine them. They head-sample normal traffic at a low rate. A tail-sampling collector, such as the OpenTelemetry Collector, keeps all errors and slow traces.

### Health checks with Spring Boot Actuator

Module 7 promised a health endpoint that load balancers and deploy tools can ask "are you OK?". **Spring Boot Actuator** gives you `/actuator/health`.

```json
{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```

- Actuator runs a **health indicator** for each thing it finds: `db` checks the database connection, `diskSpace` checks free disk, and so on.
- The overall status is `UP` only if every component is `UP`. Then the endpoint answers 200. `DOWN` or `OUT_OF_SERVICE` answers 503.
- By default, the JSON shows only `{"status":"UP"}`. `management.endpoint.health.show-components=always` lists the components; `show-details` adds details such as free disk space.
- You can write your own indicator: a class that implements `HealthIndicator` and returns `Health.up()` or `Health.down()`.

### Liveness vs readiness

A server can be in three states: working, alive but not ready, or stuck. So there are two different questions, with two endpoints.

```callout jargon
**Liveness** asks "is this process broken beyond repair?". If not, the platform restarts it. **Readiness** asks "should this instance get traffic right now?". If not, the load balancer skips it, but nothing restarts.
```

- `/actuator/health/liveness`: only the app's own state, for example a deadlock or a fatal startup error.
- `/actuator/health/readiness`: is it warmed up and able to serve? It goes `OUT_OF_SERVICE` during startup and shutdown.

Spring Boot exposes these groups when it runs on Kubernetes, or when you set `management.endpoint.health.probes.enabled=true`. A later ticket on Kubernetes wires them to the platform.

**Liveness must never check the database.** Imagine the database has a 2-minute blip. Every Spoke instance fails liveness at once. Kubernetes restarts all of them together: a **restart storm**. Restarting cannot fix the database. Worse, every fresh instance then hits the recovering database with new connections and cold caches at the same moment.

Readiness can include a dependency, with care: `management.endpoint.health.group.readiness.include=readinessState,db`. But if *every* instance shares that database, all of them go unready together, and riders get nothing. Often it is better to stay ready and answer fast `503` Problem Details for the calls that need the database.

### Syntax: exposing actuator endpoints safely

```properties
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.probes.enabled=true
management.server.port=8081
```

- `exposure.include` lists which endpoints are reachable over HTTP. By default it is only `health`.
- `management.server.port` serves Actuator on a separate port, which the firewall keeps inside Spoke's network.

Some endpoints are dangerous in public. `/actuator/heapdump` downloads the whole memory, secrets and all. `/actuator/env` shows configuration. `/actuator/loggers` lets anyone change log levels. Never use `include=*` on a public port. If an endpoint must be reachable, protect it with Spring Security, the same way as any other private URL. Leave `health` open, because load balancers need it.

### SLI, SLO, SLA and error budgets

How good is "good enough"? Three terms make it measurable.

```callout jargon
An **SLI** (service level indicator) is a measured ratio, such as "share of end-ride requests answered without a 5xx in under 2 s". An **SLO** (objective) is the target for it, such as "99.9% over 30 days". An **SLA** (agreement) is a promise in a contract, with money back if broken.
```

The SLA is set looser than the SLO, so the team gets a warning before money is lost. Spoke has no SLA with riders, but it does have SLOs.

An SLO of 99.9% means 0.1% is allowed to fail. That allowance is the **error budget**.

- 30 days = 30 × 24 × 60 = 43,200 minutes.
- 0.1% of 43,200 = **43.2 minutes** of full outage per month.
- Last night's 40 minutes used 40 / 43.2 ≈ **93%** of the month's budget, in one evening.

99.99% allows only 4.3 minutes a month. Each extra nine costs far more engineering. So pick the SLO riders really need, not the highest one.

The budget also steers decisions. Budget left: ship features. Budget spent: pause risky releases and fix reliability.

### Burn rate and alerting on symptoms

**Burn rate** is how fast you are spending the budget. A burn rate of 1 uses exactly the whole budget in 30 days. A burn rate of 10 uses it in 3 days.

burn rate = current error rate / allowed error rate. At 0.5% errors with a 0.1% allowance, the burn rate is 5.

**Alert on symptoms, not causes.** A symptom is what riders feel: end-ride requests failing or slow. A cause is something like "CPU at 90%" or "one pod restarted". Causes often do no harm, so their alerts teach people to ignore pages.

A good **page**, an alert that wakes someone, is:

- **Urgent:** riders are hurting now, or soon will be. A common rule pages when the burn rate is over 14.4 for both the last hour and the last 5 minutes. That pace spends 2% of the month's budget in one hour.
- **Actionable:** there is something a human can do. Slower burns open a ticket for working hours instead.
- **Linked to a runbook:** a short page saying what the alert means and which dashboard to open. It lists the first safe steps, such as "roll back the last deploy".

```callout jargon
A **runbook** is a step-by-step guide for one alert or task, written in calm daylight for the person reading it half-asleep at 3 a.m.
```

### On-call and blameless postmortems

**On-call** means one engineer at a time carries the pager for a week. You practised the tools in the [on-call ticket](/t/module-07-tooling/topic-08-shell-on-call). Observability decides whether that week is calm or awful.

After an incident, the team writes a **postmortem**: what happened and how to stop it happening again.

- A **timeline**: 21:40 errors begin, 21:55 the first tweet, 22:05 the team starts, 22:20 fixed.
- **Impact**: how many riders, for how long, and how much error budget.
- **Causes**, found by asking "why?" several times, until you reach something the team can change.
- **Action items**, each with an owner and a date: "add a burn-rate alert on end-ride", "add a payment timeout".

```callout jargon
**Blameless** means the postmortem looks for what in the system let the mistake happen, not for who to punish. People who fear blame hide details, and then nobody learns.
```

"Someone deployed a bad config" is not a cause. "A config change could reach production with no check, and no alert fired for 40 minutes" is one you can fix.

### In an interview

Observability comes up in system design rounds ("how would you know it is working?") and in behavioural rounds ("tell me about an incident"). Spot it when the question says monitor, alert, debug in production, on-call, SLA, or "it is slow for some users".

- **"How do you monitor this design?"** RED per endpoint and USE for pools and queues. Add traces across services, structured logs with a trace id, and SLO alerts.
- **"Users say it is slow, but the dashboard looks fine."** Look at p99, not the average, and slice by endpoint and region. Then open a slow trace and find the slow span.
- **"Liveness vs readiness?"** Restart vs stop sending traffic. Never put a shared dependency in liveness.
- **"What is an error budget?"** 1 − SLO. Compute it in minutes, and explain burn-rate alerts.
- **"Tell me about an incident"**: timeline, impact, root cause, what you changed, blameless.

## Worked example

Replay last night with the tools from this ticket. Riders could not end rides from 21:40 to 22:20, and the team learned about it from tweets.

**Step 1: decide what "working" means.** The SLI is the share of `POST /rentals/{id}/end` requests answered without a 5xx in under 2 seconds. The SLO is 99.9% over 30 days.

**Step 2: what the alert would have seen.** In the first minute, 114 of 120 end-ride requests failed. This program does the burn-rate maths.

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        long requests = 120;          // POST /rentals/{id}/end, in one minute of the incident
        long failed = 114;            // answered 5xx, or took longer than 2 s
        double sloTarget = 0.999;     // 99.9% of requests must be good

        double errorRate = (double) failed / requests;
        double allowed = 1 - sloTarget;
        double burnRate = errorRate / allowed;
        double minutesToEmpty = 30 * 24 * 60 / burnRate;

        System.out.printf(Locale.ROOT, "error rate: %.1f%%%n", errorRate * 100);
        System.out.printf(Locale.ROOT, "burn rate: %.0f%n", burnRate);
        System.out.printf(Locale.ROOT, "30-day budget gone in: %.0f minutes%n", minutesToEmpty);
    }
}
```

```text
error rate: 95.0%
burn rate: 950
30-day budget gone in: 45 minutes
```

1. The error rate is 114 / 120 = 95%.
2. The SLO allows 0.1%, so the burn rate is 95 / 0.1 = 950, far over the 14.4 paging line.
3. At that pace the whole month's budget lasts 43,200 / 950 ≈ 45 minutes. A burn-rate alert would have paged within the first few minutes, not after 40.

**Step 3: the dashboard.** The on-call engineer opens the runbook's dashboard. RED for `POST /rentals/{id}/end`: rate normal, errors at 95%, p99 stuck near 30 seconds. Every other endpoint looks fine. USE for the payment connection pool: 100% busy, with dozens of requests queued behind 30-second payment calls. So the problem sits between end-ride and payments.

**Step 4: the trace.** One failed trace shows the waterfall: `SELECT rental` 12 ms, then `PaymentClient.charge` running for 30,000 ms until it timed out. The slow hop is the payment provider.

**Step 5: the logs.** A search for that trace id returns one ERROR line, structured, with `"exception":"PaymentTimeoutException"`, `"provider":"cardco"` and `"rentalId":"rental-17"`. No card number, no email: just ids.

**Step 6: fix, then write it up.** The provider was slow, and Spoke waited 30 seconds per call. That filled the connection pool, so every end-ride queued behind it. The quick fix is a short payment timeout. The postmortem's action items: the burn-rate alert, a runbook, and the resilience work in a later ticket (timeouts, retries and a circuit breaker).

## Try it

Work out an error budget. Run it, then try the changes below.

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        double slo = 0.999;                  // 99.9% of minutes must be good
        long minutesIn30Days = 30 * 24 * 60;
        double budget = minutesIn30Days * (1 - slo);
        long outage = 40;                    // last night: riders could not end rides

        System.out.printf(Locale.ROOT, "minutes in 30 days: %d%n", minutesIn30Days);
        System.out.printf(Locale.ROOT, "error budget: %.1f minutes%n", budget);
        System.out.printf(Locale.ROOT, "used by last night: %.1f%%%n", outage * 100 / budget);
    }
}
```

1. Change the SLO to 99.99%. How many minutes of outage does Spoke get per month now?
2. Change it to 99%. How many hours is that?
3. Add a burn rate: at 0.5% errors and a 99.9% SLO, how many days until the budget is gone?
4. Which SLO would you pick for ending a ride? And for the monthly ride report email? Why might they differ?

## Recap

- Metrics tell you *that* something is wrong, traces tell you *where*, and logs tell you *why*. Join them with one trace id.
- Write structured JSON logs with ids, carry a correlation id in the MDC across threads, and never log secrets or personal data.
- Use counters, gauges and histogram timers, with low-cardinality tags. Watch RED per endpoint and USE per resource. Prometheus scrapes, Grafana draws.
- Traces pass `traceparent` from hop to hop, and the waterfall shows the slow span. Sample with head or tail sampling.
- Liveness restarts, readiness removes from traffic, and liveness never checks the database. Alert on SLO burn rate, with a runbook, and write blameless postmortems.

## Common mistakes

**1. Putting a rider id in a metric tag.**

```java nocompile
Counter.builder("spoke.rides.ended")
       .tag("riderId", ride.riderId())   // 100,000 riders: 100,000 time series
       .register(registry)
       .increment();
```

Symptom: the metrics database's memory climbs every day, until scrapes time out and dashboards go blank. Fix: tags with a small, fixed set of values only, such as `bike_type`. Put the rider id in the log line or the trace instead.

**2. Checking the database in the liveness probe.**

```properties
management.endpoint.health.group.liveness.include=livenessState,db
```

Symptom: a short database blip fails liveness on every instance, and the platform restarts them all at once. The restarts fix nothing and hammer the database as it recovers. Fix: liveness only checks the process itself. If you must, put `db` in readiness, and think about what happens when every instance is unready.

**3. Losing the request id on another thread.**

```java nocompile
MDC.put("requestId", requestId);
executor.submit(() -> log.info("charging card"));   // runs on a pool thread with an empty MDC
```

Symptom: the payment log lines have no request id, so searching by request id hides the very step that failed. Fix: copy `MDC.getCopyOfContextMap()` into the task, set it at the start and clear it in `finally`, or let a tracing library wrap the executor.

**4. Alerting on causes instead of symptoms.**

```text
ALERT HighCpu  IF cpu_usage > 0.9 FOR 5m   # pages at 3 a.m., riders unaffected
```

Symptom: the pager goes off every night for nightly batch jobs. People start ignoring it, and then miss the real outage. Fix: page on what riders feel, such as the end-ride error-rate burn rate, and link every page to a runbook.

**5. Exposing every actuator endpoint publicly.**

```properties
management.endpoints.web.exposure.include=*
```

Symptom: anyone on the internet can download `/actuator/heapdump`, with tokens and personal data inside, or change log levels. Fix: expose only `health`, `info` and `prometheus`, serve them on a separate `management.server.port`, and protect anything else with Spring Security.

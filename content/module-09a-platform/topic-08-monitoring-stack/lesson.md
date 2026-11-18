## Hook

A smoke detector that measures smoke but has no alarm bell is just a number on a wall. Somebody would have to stand there and read it.

That is Spoke today. spoke-api already counts every request at `/actuator/prometheus`, as you set up in [Observability](/t/module-09-architecture/topic-08-observability). But nothing collects those numbers, no screen draws them, and no rule wakes anyone up. This ticket wires the alarm bell: Prometheus collects, PromQL asks questions, Grafana draws, and Alertmanager pages the right person.

## Concept

### From a number in Java to a page at 3 a.m.

The observability ticket taught the signals, RED and USE, SLOs and burn rate. This one builds the machine that uses them. Five parts, each with one job:

| Part | Its job |
|---|---|
| **Micrometer** in spoke-api | counts and times things, and serves them as text at `/actuator/prometheus` |
| **Prometheus** | fetches that text every 15 seconds from every Pod, and stores it as time series |
| **Rules** in Prometheus | pre-compute heavy queries, and decide when something is wrong |
| **Alertmanager** | groups alerts, picks the team, and sends the page or the chat message |
| **Grafana** | draws dashboards from PromQL queries |

```animate
title: One failing request becomes a page
kind: scene
nodes:
  - { id: api, label: spoke-api Pod, emoji: "🖥️", value: "errors: 114" }
  - { id: prom, label: Prometheus, emoji: "🗄️" }
  - { id: am, label: Alertmanager, emoji: "📣" }
  - { id: pager, label: On-call phone, emoji: "📱" }
  - { id: grafana, label: Grafana, emoji: "📊", row: 1, col: 1 }
steps:
  - say: spoke-api only counts. Its error counter sits at 114 and waits to be read.
    focus: [api]
  - say: Every 15 seconds Prometheus pulls the numbers. That is a scrape.
    send: { from: prom, to: api, label: "GET /actuator/prometheus" }
  - say: The Pod answers with plain text, one line per time series.
    send: { from: api, to: prom, label: "200 OK, text", back: true }
  - say: Prometheus checks its alert rules. The error ratio has been too high for 5 minutes, so the alert fires.
    set: { prom: "alert firing" }
    send: { from: prom, to: am, label: "SpokeApiErrorBudgetBurn" }
  - say: Alertmanager groups it, sees team=rides and severity=page, and pages the rides on-call.
    send: { from: am, to: pager, label: "page with runbook link" }
  - say: The engineer opens Grafana, which runs PromQL against Prometheus to draw the graphs.
    show: [grafana]
    send: { from: grafana, to: prom, label: "PromQL query" }
```

### Pull: targets, scrapes and up

Prometheus **pulls**. It keeps a list of **targets**, which are URLs such as `http://10.1.4.17:8081/actuator/prometheus`, and fetches each one on a fixed beat.

```callout jargon
A **scrape** is one fetch of a target's metrics page. The **scrape interval** is the time between two scrapes. A **target** is one endpoint Prometheus scrapes, usually one Pod.
```

Prometheus's built-in default interval is one minute. Most teams set 15 or 30 seconds. Shorter means sharper graphs, but more samples to store.

For every scrape, Prometheus also writes a series of its own, called `up`. It is `1` when the scrape worked and `0` when it failed. So `up == 0` lists every target that is down, or too broken to answer. That is the pull model's gift: a dead server shows up at once, because Prometheus notices the silence.

Push suits a short job that ends before anyone could pull, such as a nightly batch. It sends its numbers to a **Pushgateway**, which Prometheus then scrapes.

### Syntax: a scrape config

Prometheus reads `prometheus.yml`. The smallest config for two fixed servers:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s       # how often rules are checked
scrape_configs:
  - job_name: spoke-api
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["api-1.spoke.internal:8081", "api-2.spoke.internal:8081"]
```

- `job_name` becomes the label `job="spoke-api"` on every series from these targets.
- Each target gets the label `instance`, such as `instance="api-1.spoke.internal:8081"`.
- Port 8081 is Actuator's own port. Port 8080 serves riders.

### Finding targets on Kubernetes

Spoke runs 30 Pods, and they change names on every deploy. A fixed list is useless. Prometheus asks the Kubernetes API instead. This is **service discovery**.

```yaml
scrape_configs:
  - job_name: spoke-api
    metrics_path: /actuator/prometheus
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names: [spoke]
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_label_app]
        regex: spoke-api
        action: keep               # only Pods labelled app=spoke-api
      - source_labels: [__meta_kubernetes_pod_container_port_number]
        regex: "8081"
        action: keep               # only the Actuator port
      - source_labels: [__meta_kubernetes_pod_name]
        target_label: pod          # keep the Pod name as a label
      - source_labels: [__meta_kubernetes_namespace]
        target_label: namespace    # and the namespace
```

```callout jargon
**Relabelling** rewrites a target's labels before the scrape. Kubernetes hands Prometheus many `__meta_` labels about each Pod. Relabel rules keep some targets, drop others, and copy useful facts into real labels.
```

Many clusters run the **Prometheus Operator** instead. You then write a small `ServiceMonitor` object next to your Service, and the operator writes the scrape config for you:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: spoke-api
  namespace: spoke
spec:
  selector:
    matchLabels:
      app: spoke-api
  endpoints:
    - port: management             # the Service port named management, 8081
      path: /actuator/prometheus
      interval: 15s
```

### Storage and retention

Prometheus stores samples in its own **time series database** on local disk.

- New samples go to memory and to a **write-ahead log**, so a crash loses nothing.
- Every two hours, they are written out as a compressed **block**. A sample costs only one or two bytes on disk.
- Old blocks are deleted after the **retention** time, 15 days by default (`--storage.tsdb.retention.time`).

One Prometheus is one machine, with one disk. So:

- For **high availability**, run two identical Prometheus servers that scrape the same targets. Both send alerts, and Alertmanager removes the duplicates.
- For **long-term storage**, Prometheus sends its samples with `remote_write` to a system such as Thanos, Grafana Mimir, or a hosted service. That is where a year of history lives.

### The exposition format, line by line

The text at `/actuator/prometheus` follows the Prometheus **exposition format**. A trimmed example with Spoke's metrics:

```text
# HELP spoke_rides_ended_total Rides ended
# TYPE spoke_rides_ended_total counter
spoke_rides_ended_total{bike_type="EBIKE"} 48211
spoke_rides_ended_total{bike_type="CLASSIC"} 91377
# HELP spoke_bikes_available Bikes docked and ready to ride
# TYPE spoke_bikes_available gauge
spoke_bikes_available{station="harbor"} 7
spoke_bikes_available{station="uptown"} 12
```

Line by line:

1. `# HELP` gives the metric's name and a sentence for humans.
2. `# TYPE` says what kind it is: `counter`, `gauge`, `histogram`, `summary` or `untyped`.
3. Each other line is one **sample**: the name, the labels in braces, and the current value.

The name plus its exact set of labels is one **time series**. `spoke_rides_ended_total{bike_type="EBIKE"}` and `spoke_rides_ended_total{bike_type="CLASSIC"}` are two series.

Counters end in `_total` by convention, and Micrometer adds it for you. Base units are written into the name: `_seconds`, `_bytes`. Never milliseconds.

### Histograms on the wire

A histogram is not one line. It is a small family of series. Here is an unlock timer with buckets at 0.1 s, 0.3 s and 1 s:

```text
# TYPE spoke_unlock_seconds histogram
spoke_unlock_seconds_bucket{le="0.1"} 900
spoke_unlock_seconds_bucket{le="0.3"} 980
spoke_unlock_seconds_bucket{le="1.0"} 998
spoke_unlock_seconds_bucket{le="+Inf"} 1000
spoke_unlock_seconds_count 1000
spoke_unlock_seconds_sum 92.4
```

- `le` means "less than or equal". `le="0.3"` counts unlocks that took **0.3 s or less**, so the buckets are **cumulative**: each includes the ones before it.
- `le="+Inf"` counts everything, so it always equals `_count`.
- `_sum` is the total time of all unlocks. `_sum / _count` gives the **average**, 0.0924 s here.
- 900 of 1,000 unlocks took 0.1 s or less. 18 took between 0.3 s and 1 s.

Each bucket is its own series. Ten buckets on a metric with 640 label combinations make 6,400 series, before `_sum` and `_count`. Keep bucket lists short.

### Selectors and label matchers

PromQL starts by picking series. A name alone picks every series with that name. Braces narrow it down with **label matchers**:

```text
http_server_requests_seconds_count{job="spoke-api", uri="/rentals/{id}/end"}
http_server_requests_seconds_count{job="spoke-api", status=~"5.."}
http_server_requests_seconds_count{job="spoke-api", uri!~"/actuator/.*"}
```

| Matcher | Means |
|---|---|
| `=` | equals |
| `!=` | does not equal |
| `=~` | matches the regular expression |
| `!~` | does not match it |

Regular expressions must match the **whole** value. `status=~"5.."` matches `503`, but `status=~"5"` matches nothing.

The result is an **instant vector**: the latest value of each matching series, one number per series.

### Range vectors and rate()

A raw counter, such as 48,211 rides ended since the Pod started, is not useful on its own. You want "rides per second right now". That needs the counter's values over a stretch of time.

```text
rate(spoke_rides_ended_total{job="spoke-api"}[5m])
```

- `[5m]` turns the selector into a **range vector**: every sample of each series from the last five minutes.
- `rate()` gives the **per-second** average increase over that window, for each series.

```callout jargon
A **range vector** holds a list of samples per series, over a time window such as `[5m]`. Functions such as `rate()` turn it back into one number per series.
```

Pick a window at least **four times the scrape interval**. With 15 s scrapes, `[1m]` is the shortest safe window, and `[5m]` is the usual choice. A window with fewer than two samples gives no result at all, and graphs show gaps.

### rate, irate and increase

Three functions read counters. They answer different questions.

| Function | What it gives | Use it for |
|---|---|---|
| `rate(x[5m])` | average per-second increase over the whole window | alerts, recording rules, most graphs |
| `irate(x[5m])` | per-second increase between the **last two** samples only | fast-moving graphs, where spikes must show |
| `increase(x[1h])` | total increase over the window: `rate` times the window's seconds | "how many rides ended in the last hour" |

`irate` jumps around with every scrape, so an alert on it flaps. `rate` smooths the spikes out, which is right for alerts.

`increase()` can return a number like `2391.6`, though rides are whole. Prometheus extrapolates to the window's edges, because the first and last samples rarely sit exactly on them.

### Counter resets

A counter only goes up, until the Pod restarts. Then it starts again from 0. Prometheus spots this: a value lower than the one before means a **reset**.

```animate
title: rate() survives a restart
kind: code
code: |
  long[] samples = {100, 160, 220, 60, 120};
  long increase = 0;
  for (int i = 1; i < samples.length; i++) {
      long delta = samples[i] - samples[i - 1];
      if (delta < 0) delta = samples[i];
      increase += delta;
  }
  System.out.println(increase + " rides in 60 s = " + increase / 60.0 + " per second");
steps:
  - line: 1
    objects: { s: { type: "long[]", fields: { 0: 100, 1: 160, 2: 220, 3: 60, 4: 120 } } }
    vars: { samples: "@s" }
    say: Five scrapes of spoke_rides_ended_total, 15 s apart. The Pod restarted between 220 and 60.
  - line: 2
    vars: { increase: 0 }
    say: We add up how much the counter grew between neighbouring samples.
  - line: 4
    vars: { i: 1, delta: 60 }
    say: 100 to 160 is 60 rides.
  - line: 6
    vars: { increase: 60 }
    say: Add it up.
  - line: 6
    vars: { i: 2, delta: 60, increase: 120 }
    say: 160 to 220 is another 60.
  - line: 4
    vars: { i: 3, delta: -160 }
    say: 220 to 60 is negative. A counter never goes down on its own, so this is a reset.
  - line: 5
    vars: { delta: 60 }
    say: After a reset the counter started at 0, so the whole new value, 60, is the growth.
  - line: 6
    vars: { increase: 180 }
    say: Add the 60. Without the reset rule, we would have added -160.
  - line: 6
    vars: { i: 4, delta: 60, increase: 240 }
    say: 60 to 120 is 60 more.
  - line: 8
    vars: { i: ~, delta: ~ }
    out: "240 rides in 60 s = 4.0 per second"
    say: That is rate() in miniature. The real one also extrapolates to the edges of the window.
```

Two rules follow:

- **Never `rate()` a gauge.** A gauge going down is normal, but `rate()` would read it as a reset. Use `deriv()` or `delta()` for gauges.
- **Rate first, then sum.** `sum(rate(x[5m]))` is right. Summing counters from many Pods first hides each Pod's reset inside the total.

### Aggregation: sum by and without

Thirty Pods give thirty series. Usually you want one number, or one per endpoint. **Aggregation** combines series.

```text
sum by (uri) (rate(http_server_requests_seconds_count{job="spoke-api"}[5m]))
```

- `sum by (uri)` adds up all series that share a `uri`, and keeps **only** the `uri` label. Thirty Pods' series become one per endpoint.
- `sum without (instance, pod)` does the opposite: it removes the listed labels and keeps the rest.
- Other aggregators work the same way: `avg`, `max`, `min`, `count`, and `topk(5, ...)` for the five largest.

`by` is safer in dashboards, because you know exactly which labels come out. `without` is handy in recording rules, because labels you did not think of survive.

### The error ratio for spoke-api

Put the pieces together. The share of end-ride requests that failed with a 5xx, over five minutes, across every Pod:

```text
sum(rate(http_server_requests_seconds_count{job="spoke-api", uri="/rentals/{id}/end", status=~"5.."}[5m]))
/
sum(rate(http_server_requests_seconds_count{job="spoke-api", uri="/rentals/{id}/end"}[5m]))
```

Read it inside out. `rate` turns each Pod's counters into requests per second. `sum` adds the Pods up. Dividing failures by all requests gives a ratio, such as `0.004` for 0.4%.

Why a ratio and not a count? 50 errors a minute is a disaster at 3 a.m. and noise at 8 a.m. rush hour. A ratio means the same at any traffic level.

### Percentiles from histograms

`histogram_quantile` estimates a percentile from bucket counts:

```text
histogram_quantile(0.99,
  sum by (le) (rate(http_server_requests_seconds_bucket{job="spoke-api", uri="/rentals/{id}/end"}[5m])))
```

Read it inside out. `rate` per bucket, per Pod. `sum by (le)` adds the same bucket across all Pods, and keeps `le`. Then `histogram_quantile` finds the bucket where the 99th percentile falls.

- **Keep `le` in the `by`.** Without it, the buckets melt into one number and the function returns nothing useful. Want p99 per endpoint? Write `sum by (le, uri)`.
- **The answer is an estimate.** Inside a bucket, Prometheus assumes values are spread evenly. With buckets at 0.3 s and 1 s, a p99 of "0.69 s" only means "somewhere between 0.3 and 1".
- **Never average percentiles.** The mean of 30 Pods' p99s is not the fleet's p99. A quiet Pod with a great p99 drags the average down while busy Pods suffer. Add the buckets first, then estimate once.

### Recording rules

The p99 query reads thousands of samples. A dashboard with ten such panels, refreshed by twenty people, makes Prometheus slow. A **recording rule** runs the query once per evaluation and saves the answer as a new series.

```yaml
groups:
  - name: spoke-api-recording
    interval: 30s
    rules:
      - record: job:http_server_requests:rate5m
        expr: sum by (job) (rate(http_server_requests_seconds_count{job="spoke-api"}[5m]))
      - record: job:http_server_requests_errors:ratio_rate5m
        expr: |
          sum by (job) (rate(http_server_requests_seconds_count{job="spoke-api", status=~"5.."}[5m]))
          /
          sum by (job) (rate(http_server_requests_seconds_count{job="spoke-api"}[5m]))
```

The name follows the convention `level:metric:operations`. `job:` says it is aggregated per job, and `ratio_rate5m` says what was done to it. Dashboards and alerts then use the cheap new series.

### Alerting rules

An **alerting rule** is a PromQL condition with a name. Whenever the query returns a series, that series is an alert.

```yaml
groups:
  - name: spoke-api-alerts
    rules:
      - alert: SpokeApiHighErrorRatio
        expr: job:http_server_requests_errors:ratio_rate5m{job="spoke-api"} > 0.05
        for: 5m
        labels:
          severity: page
          team: rides
        annotations:
          summary: "spoke-api error ratio is {{ $value | humanizePercentage }}"
          runbook_url: "https://runbooks.spoke.example/spoke-api/high-error-ratio"
```

- **`expr`**: the condition. Here, more than 5% of requests failing.
- **`for: 5m`**: the condition must hold on every evaluation for five minutes. Until then the alert is **pending**; after that it is **firing**. One bad scrape does not wake anyone.
- **`labels`**: used for routing. `severity` and `team` decide who gets it, and how.
- **`annotations`**: text for the human. A summary, and a **runbook** link, always.

An alert with no runbook leaves a half-asleep engineer guessing. Write the runbook in daylight, as [Observability](/t/module-09-architecture/topic-08-observability) said.

### Alertmanager: grouping, routing, silences and inhibition

Prometheus decides **that** something is wrong. **Alertmanager** decides **who hears about it, and how**.

```yaml
route:
  receiver: platform-chat              # the fallback for anything not matched below
  group_by: [alertname, namespace]
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h
  routes:
    - matchers: ['team="rides"', 'severity="page"']
      receiver: rides-pager
    - matchers: ['team="rides"']
      receiver: rides-chat
inhibit_rules:
  - source_matchers: ['alertname="SpokeDatabaseDown"']
    target_matchers: ['team="rides"']
    equal: [namespace]
```

- **Grouping.** 30 Pods fail at once, so 30 alerts fire. `group_by` bundles them into **one** page. `group_wait` waits 30 s for more alerts to join the first message.
- **Routing.** The route tree is walked top-down, and the first matching child wins. A paging rides alert goes to the pager; other rides alerts go to chat.
- **Silences.** During planned database maintenance, someone adds a silence: matchers plus an end time. Matching alerts stay quiet, and the silence ends on its own.
- **Inhibition.** When the database is down, every API alert fires too. The inhibit rule mutes rides alerts in the same namespace while `SpokeDatabaseDown` fires. People see the cause, not 40 symptoms.

### A RED dashboard in Grafana

**Grafana** draws PromQL results as panels. A good service dashboard answers "are riders OK?" in five seconds. For spoke-api, one row per question:

| Row | Panel query, shortened | Unit |
|---|---|---|
| Rate | `sum by (uri) (rate(http_server_requests_seconds_count{job="spoke-api"}[$__rate_interval]))` | requests per second |
| Errors | 5xx rate divided by all requests, `sum by (uri)` | percent |
| Duration | `histogram_quantile(0.99, sum by (le, uri) (rate(..._bucket[$__rate_interval])))`, plus p50 | seconds |
| Saturation | `hikaricp_connections_active / hikaricp_connections_max`, heap used | percent |

A few habits make dashboards useful:

- **Variables.** A drop-down for `$namespace`, filled by `label_values(up{job="spoke-api"}, namespace)`. One dashboard then serves dev, staging and prod.
- **`$__rate_interval`.** Grafana picks a safe `rate` window from the scrape interval and the zoom level.
- **Deploy markers.** An annotation for every deploy. "Errors jumped right after 2.6.0" answers itself.
- **Dashboards as code.** Keep the JSON in git and load it automatically. A dashboard someone edited by hand at 3 a.m. is gone after the next reinstall.

### Multi-window burn-rate alerts

[Observability](/t/module-09-architecture/topic-08-observability) introduced the burn rate. Here is the full rule, with its numbers.

Spoke's end-ride SLO is 99.9% over 30 days, so the error budget is 0.1% of requests. A burn rate tells how fast that budget goes. The trick: decide **what share of the month's budget** may burn before someone is told.

burn rate = share of budget × (30 days ÷ window)

| Budget spent | Long window | Short window | Burn rate | Error ratio above | Action |
|---|---|---|---|---|---|
| 2% | 1 hour | 5 minutes | 14.4 | 1.44% | page |
| 5% | 6 hours | 30 minutes | 6 | 0.6% | page |
| 10% | 3 days | 6 hours | 1 | 0.1% | ticket |

Check the first row: 2% × (720 h ÷ 1 h) = 14.4. The error ratio threshold is 14.4 × 0.1% = 1.44%.

Why two windows per row? The **long** window proves the burn is real, not one bad minute. The **short** window, one twelfth as long, proves it is **still** happening. Without it, the alert keeps firing for an hour after the fix.

```text
(
  job:slo_errors_per_request:ratio_rate1h{job="spoke-api"} > (14.4 * 0.001)
and
  job:slo_errors_per_request:ratio_rate5m{job="spoke-api"} > (14.4 * 0.001)
)
```

How fast does it page? If every end-ride fails, the one-hour ratio passes 1.44% after 1.44% of an hour: about 52 seconds.

### Cardinality costs memory and money

Every series costs Prometheus memory while it is active, roughly a few kilobytes. Hosted services often bill per active series. So **cardinality**, the number of series, is the real price of a metric.

Count it before you add a label. `http_server_requests` with 20 URIs × 4 methods × 8 statuses is 640 combinations. As a histogram with 10 buckets, that is 6,400 series, per Pod. Times 30 Pods: 192,000. Add a `rider_id` label "for debugging", and it multiplies by 100,000.

Two PromQL queries find the worst offenders:

```text
topk(10, count by (__name__) ({__name__=~".+"}))
count(count by (uri) (http_server_requests_seconds_count))
```

The first lists the ten metric names with the most series. The second counts how many different `uri` values exist. A `uri` count in the thousands means raw paths are leaking in, such as `/rentals/rental-17/end`.

Fixes, cheapest first:

- Do not add the label. Per-rider questions belong in logs and traces.
- Fewer histogram buckets, and only on the timers you alert on.
- Drop a bad label or metric at scrape time with `metric_relabel_configs`, until the code is fixed.

### In an interview

Monitoring questions test whether you have been on call. Classic ones:

- **"Pull or push?"** Pull: Prometheus knows its targets and spots a dead one through `up == 0`. Push: for short batch jobs, through a Pushgateway.
- **"rate or irate?"** `rate` for alerts and recording rules, `irate` for spiky graphs. Window at least four scrapes long.
- **"How do you compute p99 across 30 servers?"** Histograms, `sum by (le)`, then `histogram_quantile`. Never average percentiles.
- **"How do you avoid alert fatigue?"** Alert on SLO burn rates, with `for`, grouping, inhibition, and a runbook on every page.
- **"Why did Prometheus run out of memory?"** Cardinality. Find it with `count by (__name__)`, then remove the label.
- **"How do you design the alerts for a new service?"** Define the SLO first. Then add multi-window burn-rate alerts: page on fast burns, ticket on slow ones.

In a system design round, add one line to your diagram: "Prometheus scrapes every service, burn-rate alerts go through Alertmanager to the owning team".

## Worked example

Kakashi's ticket asks for alerts that page someone. Here is the whole path for end-ride, built step by step.

**Step 1: the SLI as recording rules.** One rule per window, so alerts and dashboards stay cheap.

```yaml
groups:
  - name: spoke-api-slo
    rules:
      - record: job:slo_errors_per_request:ratio_rate5m
        expr: |
          sum by (job) (rate(http_server_requests_seconds_count{job="spoke-api", uri="/rentals/{id}/end", status=~"5.."}[5m]))
          /
          sum by (job) (rate(http_server_requests_seconds_count{job="spoke-api", uri="/rentals/{id}/end"}[5m]))
      # the same rule again for [30m], [1h], [6h], [3d], with matching names
```

**Step 2: the fast-burn page.** 2% of the month's budget in one hour.

```yaml
      - alert: SpokeEndRideFastBurn
        expr: |
          job:slo_errors_per_request:ratio_rate1h{job="spoke-api"} > (14.4 * 0.001)
          and
          job:slo_errors_per_request:ratio_rate5m{job="spoke-api"} > (14.4 * 0.001)
        for: 2m
        labels: { severity: page, team: rides }
        annotations:
          summary: "End-ride is burning the error budget 14x too fast"
          runbook_url: "https://runbooks.spoke.example/spoke-api/end-ride-burn"
```

**Step 3: the slower alerts.** The same shape with 6 over `[6h]` and `[30m]` for the second page. Then 1 over `[3d]` and `[6h]` with `severity: ticket`, which opens a ticket for working hours.

**Step 4: the route.** The Alertmanager tree from earlier already sends `team="rides", severity="page"` to the rides pager, and other rides alerts to chat.

**Step 5: the dashboard.** One Grafana row shows the SLI over 30 days and the budget left. The RED rows sit underneath, so whoever is paged sees the symptom and the likely cause together.

What happens on a bad night:

1. At 02:10 the payment provider slows down, and 20% of end-rides fail.
2. The 5-minute ratio passes 1.44% within a minute. The 1-hour ratio needs 1.44 ÷ 20 of an hour, about 4.3 minutes. At about 02:14, both are above the line, and the alert is pending.
3. At about 02:16, `for: 2m` has passed. The alert fires.
4. Alertmanager waits `group_wait` (30 s) for related alerts, then sends one page with the runbook link.
5. The engineer opens the dashboard: errors on `/rentals/{id}/end` only, and the payment pool saturated. The runbook says: switch on the payment fallback.

About seven minutes from the first failure to a page, and the page says what to do. A total outage pages faster, because the one-hour ratio climbs five times as fast.

## Try it

This program turns the three burn-rate rows into numbers for a 99.9% SLO.

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        double slo = 0.999;                       // 99.9% of end-ride requests succeed
        double budget = 1 - slo;                  // so 0.1% may fail
        double periodHours = 30 * 24;             // the SLO is measured over 30 days

        // { share of the month's budget, long window in hours }
        double[][] rules = { {0.02, 1}, {0.05, 6}, {0.10, 72} };
        for (double[] rule : rules) {
            double share = rule[0];
            double windowHours = rule[1];
            double burnRate = share * periodHours / windowHours;
            double errorRatio = burnRate * budget;
            double daysToEmpty = periodHours / burnRate / 24;
            System.out.printf(Locale.ROOT,
                    "%2.0f%% in %2.0fh: burn rate %4.1f, alert above %.2f%% errors, budget gone in %.1f days%n",
                    share * 100, windowHours, burnRate, errorRatio * 100, daysToEmpty);
        }
    }
}
```

1. Change the SLO to 99.5%. The burn rates stay the same. Why do the error thresholds change?
2. Change the SLO to 99.99%. What error ratio pages now? Is that a sensible line for a small service?
3. Add a row for 1% of the budget in 30 minutes. What burn rate does it need?

## Recap

- Micrometer serves metrics as text. Prometheus pulls them from every target on a beat, stores them as time series, and writes `up` for each scrape. On Kubernetes, it finds Pods through service discovery or a ServiceMonitor.
- The exposition format has `# HELP`, `# TYPE` and one line per series. Counters end in `_total`. Histograms are cumulative `_bucket{le="..."}` series plus `_sum` and `_count`.
- PromQL: select with label matchers, `rate()` over a range vector, then `sum by`. `rate` handles counter resets, `irate` is for spiky graphs, `increase` gives totals. Percentiles come from `histogram_quantile` over `sum by (le)`, never from averaging.
- Recording rules pre-compute heavy queries. Alerting rules add `for`, labels and a runbook link. Alertmanager groups, routes, silences and inhibits. Grafana draws a RED dashboard per service.
- Page on multi-window SLO burn rates, such as 14.4 over 1 hour and 5 minutes. Watch cardinality, because every label value is another series to pay for.

## Common mistakes

**1. Summing before rate.**

```text
rate(sum(spoke_rides_ended_total)[5m:])
```

Symptom: every deploy draws a huge false spike. When one Pod restarts, the sum drops a little. `rate` reads that as the whole total resetting to zero, and counts it all again as new rides. Fix: `sum(rate(spoke_rides_ended_total[5m]))`. Rate each series first, then add them up.

**2. Averaging percentiles.**

```text
avg(histogram_quantile(0.99, rate(http_server_requests_seconds_bucket{job="spoke-api"}[5m])))
```

Symptom: the dashboard shows a calm p99 while riders wait seconds. Quiet Pods with fast answers pull the average down. Fix: add the buckets across Pods first, `histogram_quantile(0.99, sum by (le) (rate(...[5m])))`.

**3. A window shorter than four scrapes.**

```text
rate(http_server_requests_seconds_count{job="spoke-api"}[20s])
```

Symptom: with 15 s scrapes, many windows hold only one sample, so the graph is full of gaps, and alerts flicker. Fix: at least four scrape intervals, such as `[1m]`, or `$__rate_interval` in Grafana.

**4. An alert with no for.**

```yaml
- alert: SpokeApiHighErrorRatio
  expr: job:http_server_requests_errors:ratio_rate5m > 0.05
```

Symptom: one bad scrape pages the on-call at 3 a.m., and the alert resolves before they open the laptop. After a week, people stop trusting the pager. Fix: add `for: 5m`, or better, a multi-window burn-rate rule.

**5. A rider id as a label.**

```java nocompile
Counter.builder("spoke.unlocks").tag("rider_id", riderId).register(registry).increment();
```

Symptom: series count climbs with every new rider until Prometheus runs out of memory. The dashboards die, often during the incident you needed them for. Fix: remove the label; ask per-rider questions in logs and traces. Drop it at scrape time with `metric_relabel_configs` until the code ships.

**6. No team label on an alert.**

```yaml
labels:
  severity: page
```

Symptom: the alert fires, matches no team's route, and lands with the fallback receiver: a busy chat channel nobody watches at night. Fix: give every alert a `team` label, and test the route tree whenever you add one.

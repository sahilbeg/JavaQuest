## Hook

Imagine three copies of a recipe book: one at home, one at a friend's, one at school. You fix a mistake in the home copy. Do you also remember the other two? After a year, the three books disagree, and nobody knows which one is right.

Spoke has three copies of its Kubernetes YAML: `dev/`, `staging/` and `prod/`. Each fix lands in one or two of them. Last week, staging had the new probe settings and prod did not. This ticket teaches the two standard tools for keeping **one** source and many environments: Kustomize and Helm.

## Concept

### The many-environments problem

Every environment runs the same app, but a few values differ:

| | dev | staging | prod |
|---|---|---|---|
| replicas | 1 | 2 | 6 |
| image tag | newest build | release candidate | released version |
| memory | 512Mi | 512Mi | 1Gi |
| SMS timeout | 5,000 ms | 5,000 ms | 2,000 ms |
| namespace | `spoke-dev` | `spoke-staging` | `spoke` |

Copying the whole folder per environment means every change is made three times, by hand. One gets missed. That gap is called **drift**.

```callout jargon
**Drift** is when copies that should match slowly stop matching, because changes reach some copies and not others. **Configuration drift** is the same thing for settings and YAML.
```

The fix is the same idea as a method in Java: write the shared part once, and pass in only what differs. The shared part should be most of the file. The differences should be short and easy to review.

### Two approaches: patch or template

There are two ways to keep one source:

- **Patch**: keep plain, complete YAML as the **base**. Each environment lists small changes on top. That is **Kustomize**.
- **Template**: write YAML with blanks in it, such as `replicas: {{ .Values.replicaCount }}`. Each environment fills in the blanks from a **values** file. That is **Helm**.

```callout analogy
A patch is like a printed form with corrections written on a sticky note. A template is a form with empty boxes that you fill in. Both give you a finished form; they differ in what you write by hand.
```

Both produce the same thing in the end: plain Kubernetes YAML, ready for `kubectl apply`. Keep that in mind. Whatever tool you use, the rendered YAML is what reaches the cluster, and it is what you should check.

```callout jargon
**Rendering** means turning the source (a base and patches, or templates and values) into the final YAML that will be applied.
```

### Kustomize: a base and overlays

Kustomize is built into kubectl: kubectl 1.34 ships Kustomize v5.7.1, and `kubectl version --client` shows the version yours has. No extra install is needed.

The layout:

```text
k8s/
  base/
    deployment.yaml
    service.yaml
    kustomization.yaml       # lists the files and shared settings
  overlays/
    dev/kustomization.yaml
    staging/kustomization.yaml
    prod/
      kustomization.yaml
      more-replicas.yaml     # a patch, only for prod
```

```callout jargon
A **base** is a folder of complete, shared YAML. An **overlay** is a folder that points at the base and lists changes for one environment. Every folder has a file called `kustomization.yaml`.
```

`kubectl kustomize overlays/prod` prints the rendered YAML and changes nothing. It runs offline, so it is safe to run anywhere, including CI.

```animate
title: One base, two environments
kind: scene
nodes:
  - { id: base, label: "base/", emoji: "📄", value: "replicas 2, tag 2.6.0-3f9c2b7a1d4e" }
  - { id: stg, label: "overlays/staging", emoji: "🩹", value: "replicas 1", row: 1, col: 0 }
  - { id: prod, label: "overlays/prod", emoji: "🩹", value: "replicas 6, tag 2.6.1-8e41d07c5a92", row: 1, col: 2 }
  - { id: kz, label: "kubectl kustomize", emoji: "⚙️", row: 2, col: 1 }
  - { id: out, label: "Rendered YAML", emoji: "📦", row: 3, col: 1 }
steps:
  - say: The base holds the complete Deployment, Service and ConfigMap. Both environments share it.
    focus: [base]
  - say: The staging overlay points at the base and changes only the replica count.
    send: { from: stg, to: base, label: "resources: ../../base" }
  - say: kubectl kustomize reads the overlay, loads the base, and applies the changes in order.
    send: [{ from: stg, to: kz, label: overlay }, { from: base, to: kz, label: base }]
  - say: Out comes plain YAML for staging. Nothing in base/ was edited.
    send: { from: kz, to: out, label: "staging YAML" }
    set: { out: "replicas 1, tag 2.6.0-3f9c2b7a1d4e" }
  - say: Prod does the same with its own changes. A fix in base/ reaches both on the next render.
    send: [{ from: prod, to: kz, label: overlay }, { from: kz, to: out, label: "prod YAML" }]
    set: { out: "replicas 6, tag 2.6.1-8e41d07c5a92" }
```

### Syntax: kustomization.yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:                    # files, or other kustomization folders, to include
  - deployment.yaml
  - service.yaml
configMapGenerator:           # make a ConfigMap from key=value pairs
  - name: spoke-api-config
    literals:
      - SPOKE_CITY=riverton
      - SMS_TIMEOUT_MS=2000
```

An overlay uses the same file, with the base as a resource and changes on top:

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: spoke              # put every object in this namespace
resources:
  - ../../base
images:                       # change an image's tag, without editing any YAML
  - name: ghcr.io/spoke/spoke-api
    newTag: 2.6.1-8e41d07c5a92
patches:                      # small edits to specific objects
  - path: more-replicas.yaml
```

The main fields: `resources`, `namespace`, `namePrefix`, `labels`, `images`, `patches`, `configMapGenerator` and `secretGenerator`. Older guides show `patchesStrategicMerge` and `patchesJson6902`. Both are deprecated: today one `patches:` list holds both kinds.

### Strategic merge patches

A **strategic merge patch** looks like a small piece of the object, with only the fields you want to change. Kustomize merges it in. Here is prod's `more-replicas.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spoke-api             # which object to patch
spec:
  replicas: 6
  template:
    spec:
      containers:
        - name: api           # which container: matched by name
          resources:
            requests: { cpu: 500m, memory: 1Gi }
            limits: { memory: 1Gi }
```

Real output of `kubectl kustomize overlays/prod`, the Deployment part:

```text
spec:
  replicas: 6
  ...
      containers:
      - envFrom:
        - configMapRef:
            name: spoke-api-config-d785hk4gkh
        image: ghcr.io/spoke/spoke-api:2.6.1-8e41d07c5a92
        name: api
        ports:
        - containerPort: 8080
          name: http
        resources:
          limits:
            memory: 1Gi
          requests:
            cpu: 500m
            memory: 1Gi
```

The patch said nothing about `envFrom` or `ports`, and they stayed. "Strategic" means Kubernetes-aware: lists like `containers` are merged **by name**, not replaced wholesale. Maps merge key by key, and a field set to `null` is deleted.

### JSON 6902 patches

Sometimes you need an exact operation: replace this one value, remove that one entry. A **JSON patch**, defined in the internet standard RFC 6902, is a list of operations, each with a path:

```yaml
patches:
  - target:
      kind: Deployment
      name: spoke-api
    patch: |-
      - op: replace
        path: /spec/replicas
        value: 1
```

- `op` is `add`, `remove`, `replace`, `move`, `copy` or `test`.
- `path` walks the object with `/`. List items are picked by **index**: `/spec/template/spec/containers/0/image`.

Index paths are precise but fragile. Point one at a container that is not there, and Kustomize stops. This is a real run, with only one container in the base:

```text
error: replace operation does not apply: doc is missing path: /spec/template/spec/containers/1/image: missing value
```

Rule of thumb: strategic merge for "set these fields", JSON patch for "remove exactly this" or for objects Kustomize does not know how to merge.

### images: changing the tag without touching YAML

The image tag changes on every release, so Kustomize gives it its own field:

```yaml
images:
  - name: ghcr.io/spoke/spoke-api       # the image name as it appears in the base
    newTag: 2.6.1-8e41d07c5a92                        # the tag to use instead
```

Kustomize finds every container whose image is `ghcr.io/spoke/spoke-api`, whatever its tag, and sets the new one. `newName` changes the repository, for example to a mirror. `digest` pins an exact build. A real render with `digest:` gave:

```text
        image: ghcr.io/spoke/spoke-api@sha256:3f9c2e1a7b8d4c6e5f0a1b2c3d4e5f60718293a4b5c6d7e8f9a0b1c2d3e4f5a6
```

Spoke's release tags carry the version and the first 12 hex characters of the commit, so `2.6.1-8e41d07c5a92` names one exact build. In a pipeline, the release job changes only this one line in `overlays/prod/kustomization.yaml` and commits it. The pull request diff then shows exactly which version goes to prod.

### configMapGenerator and the hash suffix

Look again at the rendered ConfigMap's name: `spoke-api-config-d785hk4gkh`. Kustomize adds a **hash** of the contents to the name, and rewrites every reference to it.

Real runs of the same base: with `SMS_TIMEOUT_MS=2000` the name was `spoke-api-config-d785hk4gkh`. Changed to `2500`, it became `spoke-api-config-d6k84m77hk`.

Why bother? Environment variables are read once, when a container starts. Editing a ConfigMap in place leaves running Pods with the old values. With the hash, new contents means a new name. The Deployment's Pod template now refers to that new name, so the template changed, and Kubernetes rolls out new Pods with the new settings. Rolling back the Deployment brings back the old name, and the old settings with it.

```callout jargon
A **hash suffix** is a short code computed from the contents and added to the name. Same contents, same suffix; any change, a different suffix.
```

Two side effects. Old ConfigMaps stay in the cluster until something prunes them. And `generatorOptions: { disableNameSuffixHash: true }` turns the suffix off, losing the automatic rollout. An overlay can change one key with `behavior: merge`, as staging does below.

### namePrefix, namespace and labels

The staging overlay, as first written:

```yaml
namespace: spoke-staging
namePrefix: staging-
commonLabels:
  env: staging
resources:
  - ../../base
configMapGenerator:
  - name: spoke-api-config
    behavior: merge
    literals:
      - SMS_TIMEOUT_MS=5000
```

The real render starts with a warning:

```text
# Warning: 'commonLabels' is deprecated. Please use 'labels' instead. Run 'kustomize edit fix' to update your Kustomization automatically.
```

And the Service comes out like this:

```text
metadata:
  labels:
    env: staging
  name: staging-spoke-api
  namespace: spoke-staging
spec:
  ...
  selector:
    app: spoke-api
    env: staging
```

`namePrefix` renamed every object, and every reference too. `commonLabels` put `env: staging` on everything, **selectors included**. On a new environment that is fine. On an existing Deployment it is a trap: its selector is immutable, so the apply is rejected.

The newer `labels:` field adds labels to metadata only, unless you ask for more:

```yaml
labels:
  - pairs: { env: staging }
    includeSelectors: false     # the default: leave selectors alone
```

### Applying: kubectl apply -k

```bash
kubectl kustomize overlays/prod             # render and print; no cluster needed
kubectl diff -k overlays/prod               # what would change in the live cluster
kubectl apply -k overlays/prod              # render and apply in one step
```

`-k` means "this is a kustomization folder", where `-f` means "these are YAML files". `diff` and `apply` need a cluster; `kubectl kustomize` does not. Many teams render to a file in CI, check it, and apply exactly that file, so what was reviewed is what ran.

### Helm: charts, templates and values

**Helm** is a package manager for Kubernetes. A package is called a **chart**:

```text
spoke-api/                  # the chart
  Chart.yaml                # name, chart version, app version, dependencies
  values.yaml               # default values for every blank
  values-staging.yaml       # per-environment values (a common convention)
  values-prod.yaml
  templates/
    deployment.yaml         # YAML with {{ }} blanks
    service.yaml
    _helpers.tpl            # shared snippets, such as the standard labels
```

```yaml
# Chart.yaml
apiVersion: v2
name: spoke-api
version: 1.4.0              # the chart's own version
appVersion: "2.6.1-8e41d07c5a92"         # the app version it deploys
```

```callout jargon
A **chart** is a folder of templates plus default values that Helm turns into Kubernetes YAML. A **release** is one installed copy of a chart in a cluster, with a name, such as `spoke-api` in namespace `spoke`.
```

Helm is a separate install, so this page shows charts and commands and describes Helm's output rather than showing a run. `helm create spoke-api` writes a starter chart with `Chart.yaml`, `values.yaml` and a `templates/` folder; the per-environment files are yours to add.

### Syntax: a Helm template

```yaml
# templates/deployment.yaml (part)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Release.Name }}
  labels:
    {{- include "spoke-api.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  template:
    spec:
      containers:
        - name: api
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          env:
            - name: SMS_TIMEOUT_MS
              value: {{ .Values.smsTimeoutMs | quote }}
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
```

Helm uses **Go templates**:

- `{{ .Values.x }}` inserts a value. `.Release.Name` is the release's name; `.Chart.AppVersion` comes from `Chart.yaml`.
- `|` passes a value through a function: `quote` adds quotes, `default` fills a gap, `toYaml` turns a map into YAML.
- `{{-` trims the whitespace before it. `nindent 12` starts a new line and indents every line by 12 spaces.
- `if` and `end` include a block only when a value is true.

Notice the `if` around `replicas`: when the HPA is on, the template leaves `replicas` out, as [Kubernetes manifests](/t/module-09a-platform/topic-04-kubernetes-manifests) taught.

### Values files per environment

```yaml
# values.yaml: the defaults
replicaCount: 2
image:
  repository: ghcr.io/spoke/spoke-api
  tag: ""                       # empty: use the chart's appVersion
smsTimeoutMs: 5000
autoscaling:
  enabled: false
resources:
  requests: { cpu: 250m, memory: 512Mi }
  limits: { memory: 512Mi }
```

```yaml
# values-prod.yaml: only what prod changes
replicaCount: 6
smsTimeoutMs: 2000
resources:
  requests: { cpu: 500m, memory: 1Gi }
  limits: { memory: 1Gi }
```

```bash
helm upgrade --install spoke-api ./spoke-api -n spoke -f values-prod.yaml --set image.tag=2.6.1-8e41d07c5a92
```

Who wins when several sources set the same value:

1. `values.yaml` in the chart: the defaults, weakest.
2. Each `-f` file, in the order given. A later file beats an earlier one.
3. `--set` on the command line: strongest.

Maps merge key by key, so `values-prod.yaml` lists only differences. Lists do not merge: a list in a later file replaces the whole list. Keep `--set` for the one value that changes on every deploy, the image tag, so everything else stays in reviewed files.

### Releases: upgrade --install, history and rollback

Helm remembers every change to a release as a numbered **revision**, stored as a Secret in the release's namespace.

```bash
helm upgrade --install spoke-api ./spoke-api -n spoke -f values-prod.yaml   # install, or upgrade if it exists
helm history spoke-api -n spoke                                             # every revision, with its status
helm rollback spoke-api 4 -n spoke                                          # back to revision 4's settings
helm uninstall spoke-api -n spoke
```

```animate
title: Three revisions of one release
kind: scene
nodes:
  - { id: ci, label: "CI pipeline", emoji: "🤖" }
  - { id: rel, label: "Release spoke-api", emoji: "📦", value: "none yet", col: 2 }
  - { id: hist, label: "History", emoji: "📜", value: "empty", row: 1, col: 2 }
steps:
  - say: The first deploy runs helm upgrade --install. No release exists, so Helm installs one.
    send: { from: ci, to: rel, label: "upgrade --install (2.6.0-3f9c2b7a1d4e)" }
    set: { rel: "2.6.0-3f9c2b7a1d4e", hist: "1: 2.6.0-3f9c2b7a1d4e" }
  - say: The next deploy runs the very same command. Now the release exists, so Helm upgrades it.
    send: { from: ci, to: rel, label: "upgrade --install (2.6.1-8e41d07c5a92)" }
    set: { rel: "2.6.1-8e41d07c5a92", hist: "1: 2.6.0-3f9c2b7a1d4e, 2: 2.6.1-8e41d07c5a92" }
  - say: Errors climb after 2.6.1-8e41d07c5a92. Someone runs helm rollback spoke-api 1.
    send: { from: ci, to: rel, label: "rollback to 1" }
  - say: Rollback does not erase history. It adds revision 3, with revision 1's settings.
    set: { rel: "2.6.0-3f9c2b7a1d4e", hist: "1: 2.6.0-3f9c2b7a1d4e, 2: 2.6.1-8e41d07c5a92, 3: copy of 1" }
    focus: [hist]
```

`upgrade --install` is the one command a pipeline needs: it works on the first deploy and every one after. Two cautions. A Helm rollback restores the chart's YAML, not your database: a migration that already ran stays run. And if the release's values live in git, fix git too, or the next deploy brings the bad version back.

### Chart dependencies

A chart can include other charts. In dev, Spoke wants its own Postgres; staging and prod use a managed database.

```yaml
# Chart.yaml
dependencies:
  - name: postgresql
    version: "16.4.1"
    repository: oci://ghcr.io/spoke/charts
    condition: postgresql.enabled      # only when values say so
```

```yaml
# values-dev.yaml
postgresql:
  enabled: true
```

`helm dependency update` downloads the listed charts into `charts/` and writes `Chart.lock` with the exact versions it chose. Commit `Chart.lock`, so every build uses the same versions. Values for a dependency go under its name, here `postgresql:`.

Teams also publish **library charts**: shared templates, such as standard labels or a standard Deployment, that many service charts depend on.

### Choosing: Kustomize, Helm, or both

| | Kustomize | Helm |
|---|---|---|
| Source | plain YAML plus patches | templates plus values |
| Learning curve | small: it is all YAML | Go templates, functions, scoping |
| Remembers releases | no: kubectl applies, git is the history | yes: revisions, `helm rollback` |
| Sharing with other teams | awkward | built for it: versioned charts in a registry |
| Typical use | your own apps, a few environments | third-party software, platform add-ons, many similar services |

Common answers in real teams:

- **Your own service, a few environments**: Kustomize is often enough, and easiest to review.
- **Installing someone else's software**, such as an ingress controller or a monitoring stack: Helm, because its makers publish a chart.
- **Both**: render a vendor's chart with `helm template`, then adjust the result with a Kustomize overlay. Kustomize can also call Helm itself through its `helmCharts` field, when run with `--enable-helm`.

GitOps tools, covered in the next ticket, understand both formats.

### Secrets across environments

Overlays and values files live in git. Secret values must not. Base64 in a Secret is not protection, as [Running on Kubernetes](/t/module-09-architecture/topic-10-kubernetes) showed. Two common tools:

- **Sealed Secrets**: the `kubeseal` command encrypts a Secret with a public key from a controller in the cluster. You commit the encrypted **SealedSecret**. Only that controller can decrypt it, back into a normal Secret.
- **External Secrets Operator**: you commit an **ExternalSecret** that only names a key in a secret store, such as AWS Secrets Manager or HashiCorp Vault. The operator fetches the value and creates the Secret, refreshing it on a timer.

Either way, each environment's overlay or values file points at its own secret, and the value itself never enters git. [Configuration and secrets](/t/module-09a-platform/topic-07-config-and-secrets) goes deeper.

### Traps: indentation, drift and un-rendered reviews

**Indentation in templates.** Helm writes text, not YAML objects. `toYaml .Values.resources | nindent 10` where 12 was needed puts `resources` at the wrong level. The template may still produce valid YAML that means something else, or fail only at install time.

**Values drift.** Five values files can drift just like five folders did. Keep defaults in `values.yaml`, and keep environment files short: only real differences.

**Reviewing un-rendered changes.** A one-line change to `_helpers.tpl` or the base can change every object in every environment. Reviewers reading the template cannot see that. The fix is to render in CI and review the rendered diff:

```bash
kubectl kustomize overlays/prod > rendered/prod.yaml       # Kustomize
helm template spoke-api ./spoke-api -f values-prod.yaml > rendered/prod.yaml   # Helm
```

Commit or attach the rendered files, and let the pull request show what really changes. Then run the checks from [Kubernetes manifests](/t/module-09a-platform/topic-04-kubernetes-manifests) on them: schema, policy, `kubectl diff`.

### In an interview

Platform and backend interviews ask how you would ship one service to many environments.

- **"How do you manage config for dev, staging and prod?"** One source plus per-environment differences: Kustomize overlays or Helm values. Secrets come from a store, never from git.
- **"Kustomize or Helm?"** Patches versus templates. Kustomize for your own app; Helm for packaging and for third-party software; both together is normal.
- **"A ConfigMap changed, but the Pods still use old values. Why?"** Env vars are read at start. Kustomize's hash suffix makes the change roll Pods out.
- **"How do you roll back a Helm release?"** `helm history`, then `helm rollback` to a revision, which creates a new revision. Mention that the database is not rolled back.
- **"How do you review a change to a template?"** Render it in CI and review the rendered diff.

## Worked example

Spoke moves from three copied folders to one base and three overlays. The dev overlay:

```yaml
# overlays/dev/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: spoke-dev
resources:
  - ../../base
labels:
  - pairs: { env: dev }
images:
  - name: ghcr.io/spoke/spoke-api
    newTag: 2.6.2-c47e9a0b3d18     # the newest build of main
patches:
  - target: { kind: Deployment, name: spoke-api }
    patch: |-
      - op: replace
        path: /spec/replicas
        value: 1
```

And prod, which needs the tag, more replicas and more memory:

```yaml
# overlays/prod/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: spoke
resources:
  - ../../base
images:
  - name: ghcr.io/spoke/spoke-api
    newTag: 2.6.1-8e41d07c5a92
patches:
  - path: more-replicas.yaml       # the strategic merge patch from the card above
```

Line by line:

1. **`resources: ../../base`**: every environment starts from the same Deployment, Service and ConfigMap. A probe fix in the base reaches all three on the next render.
2. **`namespace`**: each environment lands in its own namespace, with no edits to the base.
3. **`images`**: the one line the release pipeline changes. dev follows `main`; prod gets a reviewed version.
4. **`patches`**: dev uses a JSON patch for one exact value; prod uses a strategic merge patch for several fields of the `api` container.
5. **Nothing secret**: the database password comes from an ExternalSecret, which each environment points at its own key.

The CI job renders all three and runs the checks on each:

```bash
for env in dev staging prod; do
  kubectl kustomize "k8s/overlays/$env" > "rendered/$env.yaml"
done
```

The same set-up in Helm would be one chart, `values.yaml` for the base, and `values-dev.yaml`, `values-staging.yaml`, `values-prod.yaml` for the differences. Neither is wrong. Spoke picks Kustomize for spoke-api because the YAML stays plain and the reviewers already read Kubernetes YAML every day.

## Try it

This program merges an environment's changes into a base, the way a strategic merge patch merges maps. Maps merge key by key, `null` deletes, and anything else replaces.

```java run
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    // Builds a map from key, value, key, value...
    static Map<String, Object> map(Object... keysAndValues) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            m.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return m;
    }

    // Like a strategic merge patch on maps: maps merge key by key, null deletes, anything else replaces.
    @SuppressWarnings("unchecked")
    static Map<String, Object> merge(Map<String, Object> base, Map<String, Object> patch) {
        Map<String, Object> result = new LinkedHashMap<>(base);
        for (Map.Entry<String, Object> change : patch.entrySet()) {
            Object old = result.get(change.getKey());
            if (change.getValue() == null) {
                result.remove(change.getKey());
            } else if (old instanceof Map && change.getValue() instanceof Map) {
                result.put(change.getKey(), merge((Map<String, Object>) old, (Map<String, Object>) change.getValue()));
            } else {
                result.put(change.getKey(), change.getValue());
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Map<String, Object> base = map(
                "replicas", 2,
                "image", "ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e",
                "config", map("SPOKE_CITY", "riverton", "SMS_TIMEOUT_MS", "2000"),
                "debugPort", 5005);
        Map<String, Object> staging = map("replicas", 1);
        Map<String, Object> prod = map(
                "replicas", 6,
                "image", "ghcr.io/spoke/spoke-api:2.6.1-8e41d07c5a92",
                "config", map("SMS_TIMEOUT_MS", "3000"),
                "debugPort", null);

        System.out.println("base:    " + base);
        System.out.println("staging: " + merge(base, staging));
        System.out.println("prod:    " + merge(base, prod));
    }
}
```

1. Change the base's `replicas` to 3. Which environments change, and which keep their own number?
2. In prod, replace the `config` map with the text `"none"`. What happens to `SPOKE_CITY`, and why?
3. Add a `dev` patch that sets `debugPort` to 5006 and `SMS_TIMEOUT_MS` to `"5000"`. Print it.

## Recap

- Copying YAML per environment causes drift. Keep one source and write down only what differs per environment.
- Kustomize patches plain YAML: a base plus overlays. Strategic merge patches merge lists by name, JSON 6902 patches use exact paths, and `images:` sets tags. `kubectl kustomize` renders offline; `kubectl apply -k` applies.
- `configMapGenerator` adds a hash of the contents to the name. A config change then rolls out new Pods, and a rollback restores the old config.
- Helm templates YAML with Go templates and values files. `-f` files override `values.yaml` in order, and `--set` beats both. `helm upgrade --install` installs or upgrades; every change is a revision, and `helm rollback` adds a new one.
- Choose Kustomize for your own app, Helm for packaged and third-party software, or both. Keep secrets out of git with Sealed Secrets or External Secrets, and review the rendered YAML in CI, not just the templates.

## Common mistakes

**1. A patch that names the wrong container.**

```yaml
spec:
  template:
    spec:
      containers:
        - name: spoke-api       # the base's container is called api
          resources:
            limits: { memory: 1Gi }
```

Symptom: no error. A real render produced a **second** container, named `spoke-api`, with no image, next to the unchanged `api` with its old 512Mi limit. The apply then fails, because a container needs an image. Fix: copy the container's `name` from the base exactly. Render and read the output before you trust a patch.

**2. commonLabels on a running app.**

```yaml
commonLabels:
  env: staging
```

Symptom: the render shows `env: staging` inside `spec.selector`, and kubectl warns that `commonLabels` is deprecated. Applying it to an existing Deployment is rejected, because its selector is immutable. Fix: `labels:` with `pairs:`, which leaves selectors alone by default.

**3. Wrong indentation in a Helm template.**

```yaml
          resources:
            {{- toYaml .Values.resources | nindent 10 }}
```

Symptom: `requests` and `limits` land beside `resources` instead of inside it. Depending on the spot, the YAML fails to parse, or Kubernetes rejects unknown fields at install time. Fix: the number after `nindent` must equal the column where the inner keys belong, here 12. Render with `helm template` in CI to catch it before review.

**4. Secrets in a values file.**

```yaml
# values-prod.yaml
database:
  password: s3cret-prod-pass
```

Symptom: the password is now in git history, readable by anyone with the repo, forever. Fix: rotate the password at once. Then keep only a reference in git, an ExternalSecret or a SealedSecret, and let the cluster fetch or decrypt the value.

**5. Changing a ConfigMap and waiting for the Pods to notice.**

```yaml
configMapGenerator:
  - name: spoke-api-config
    literals: [SMS_TIMEOUT_MS=3000]
generatorOptions:
  disableNameSuffixHash: true
```

Symptom: the ConfigMap shows 3000, but the running Pods still time out at 2,000 ms. Environment variables were read when they started. Fix: keep the hash suffix, so a change gives a new name and a rollout. Without it, run `kubectl rollout restart deployment/spoke-api` yourself.

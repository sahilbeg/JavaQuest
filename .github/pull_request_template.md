## What changed

<!-- One or two sentences: what this pull request does, and why. -->

## How I checked it

- [ ] Tried it in the tester app (`./run.sh --tester`, http://localhost:3004), not in my learner app
- [ ] `./mvnw -q install` passes
- [ ] `check-content` passes (for lesson, quiz or course changes)
- [ ] `verify-practice` passes (for ticket code or test changes): every solution passes and every skeleton fails

## Learner safety

- [ ] No existing ticket's `src/` folder is changed (fixes go in `skeleton/` and `solution/`)
- [ ] No ticket or module id was renamed or removed
- [ ] Nothing from `avatars/` and no personal progress files are included

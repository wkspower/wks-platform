# Contributing to wks-platform

Thanks for contributing! This guide captures how we branch, commit, review, and
release so changes flow predictably. It applies to every change — features,
fixes, refactors, dependency bumps, and security patches alike.

## Branching model

- **`develop`** is the integration branch. Day-to-day work merges here.
- **`main`** is the stable branch.
- Cut a **feature branch** off `develop`, named by change type:
  `feat/...`, `fix/...`, `chore/...`, `docs/...`, `build/...`, `ci/...`,
  `refactor/...`, `security/...` (e.g. `fix/case-portal-task-details`,
  `build/upgrade-deps`, `security/resolve-ui-vulnerabilities`).

### Promoting `develop` → `main`

Releases are cut from a short-lived release branch so `develop` stays open for
ongoing (including breaking) work while a release stabilizes:

- Cut a **`release/x.y`** branch off `develop` to freeze the release scope.
- Only stabilization lands on `release/x.y` — bug fixes, docs, release-note
  polish. No new features. Merge each fix back into `develop` so it isn't lost.
- Validate the branch with **release candidates**: tag `vx.y.0-rc.1`,
  `vx.y.0-rc.2`, … and deploy those images. The `-rc.N` suffix is a SemVer
  pre-release, so it correctly sorts *before* the final `vx.y.0`.
- Tag the final **`vx.y.0`** from `release/x.y` once an RC is accepted — same
  code, final name (the tag drives the image build — see [Releases](#releases)).
- **Merge `release/x.y` into `main`.** This is a merge, not a fast-forward:
  `main` can carry patch commits (below) that aren't on the release branch, so
  the histories may have diverged.

### Out-of-band patches

Patches fix an already-released version, so they branch off **`main`** (the
released code), not `develop`:

- Branch off `main`, make the fix, and tag the next **PATCH** (`v1.4.10 →
  v1.4.11`). The tag triggers the image build.
- **Merge the patch back into `develop`** (and into any active `release/x.y`)
  so the fix isn't reintroduced as a regression in the next release.

## Commits — Conventional Commits

Commit subjects follow [Conventional Commits](https://www.conventionalcommits.org):
`type(scope): short imperative summary`. This keeps history scannable and makes
release notes derivable from history.

Types in use here: `feat`, `fix`, `docs`, `chore`, `build`, `ci`, `style`,
`refactor`, `test`, `perf`. Scope is the affected area, e.g. `case-portal`,
`java`, `portal`, `roadmap`, `deps`, `ci`.

Examples (from this repo's history):

```
feat(case-portal): show ad-hoc task details and add completion notes
build(deps): resolve critical and high frontend vulnerabilities
chore(java): SB 4.0 + Java 21 + Camunda 7.24 upgrade, drop Camunda 8
docs(roadmap): finalize v1.5.0 modernization release documentation
```

## Pull requests

- Open the PR against **`develop`**.
- PRs are **squash-merged**, so the **PR title becomes the squashed commit** —
  write it as a Conventional Commit. GitHub appends the PR number (`(#NNN)`)
  automatically; keep it.
- Link any issue the PR closes (`Closes #NNN`).
- CI (`build.yml`) must pass. Build and exercise the affected app before
  requesting review — see the per-app run instructions in the repo README.

- Every PR needs **at least one approving review** before it's merged.

## Releases

Releases are **tag-driven**. Pushing a tag triggers `.github/workflows/release.yml`,
which builds the services and publishes container images to
`ghcr.io/wkspower/<service>:<tag>` (storage-api, opa, c7-external-tasks,
case-engine-rest-api, …). **The tag name is the image tag**, so the tag *is* the
release version.

- Use **semantic version** tags: `vMAJOR.MINOR.PATCH` (e.g. `v1.4.14`, heading to
  `v1.5.0`).
- Patches and fixes increment **PATCH** on the released line:
  `v1.4.10 → v1.4.11` — no suffix. The next PATCH integer is always free, since
  `develop` only issues the next MINOR (see
  [Promoting `develop` → `main`](#promoting-develop--main)).
- The only suffix we use is `-rc.N` for **release candidates** of an upcoming
  version (`v1.5.0-rc.1`), validated before the final tag — see
  [Promoting `develop` → `main`](#promoting-develop--main).
- Cutting a release = creating and pushing the tag:

  ```bash
  git tag v1.5.0
  git push origin v1.5.0
  ```

### Changelog & release notes

We don't maintain a hand-written `CHANGELOG.md`. Because PR titles are
Conventional Commits, the squashed history **is** the changelog. For each release
(and each `-rc.N`) we **cut a GitHub Release** on the tag and use its *Generate
release notes* to produce the notes from the merged PR titles — which is exactly
why clean, well-typed PR titles matter.

## Security fixes

How to **report** a vulnerability, which versions get fixes, and our remediation
SLAs live in [`SECURITY.md`](./SECURITY.md). The release cadence and support
window are in the [Support & Release Policy](./apps/react/docs/docs/release-policy.md).
This section covers how a fix is **landed** once it's in hand.

Security patches follow the same flow above, with a few additions that exist
because **this repository is public**:

- **Link the advisory.** Reference the `GHSA-…`/`CVE-…` in the commit body and PR
  so the fix is traceable to what it remediates. For dependency bumps, name the
  package and the before→after version.
- **Mind disclosure.** Don't turn the PR/commit into an exploitation guide — state
  *what* is fixed without a step-by-step of how to attack the unpatched version,
  which would arm attackers during the window before users upgrade. Dependency
  CVEs are already public via the advisory databases (lower sensitivity); for a
  vulnerability in *our own* code, prefer a
  [GitHub Security Advisory](https://docs.github.com/en/code-security/security-advisories)
  and coordinated disclosure over a plain public PR.
- **Severity drives urgency.** A Critical/High (e.g. CVSS ≥ 7) may warrant an
  immediate patch release (`-patchN` tag) rather than waiting for the next normal
  release; lows can ride the regular cycle.
- **Dependabot closes itself.** Once the fix reaches the default branch,
  Dependabot auto-resolves the corresponding alert — no manual close needed. If
  you intentionally won't fix one, dismiss it in the GitHub UI **with a reason**
  rather than leaving it silently open.
- Maintainers: reproduce and verify fixes locally before pushing (build + run the
  affected module's tests, and confirm the advisory clears) — internal tooling
  exists for the local scan/verify loop.

## Communication

Releases are announced through the **GitHub Release** for the tag — its generated
notes are the announcement. Notable user-facing changes are also reflected in the
**documentation site** (`apps/react/docs`) so the published docs track the
released behaviour. Security fixes are communicated the same way (the GitHub
Release notes, alongside the published advisory).

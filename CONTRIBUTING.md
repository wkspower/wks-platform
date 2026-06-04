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

<!-- TODO(maintainers): confirm the develop → main promotion rule (when/how
     develop is merged or fast-forwarded into main) and document it here. -->

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

<!-- TODO(maintainers): document review requirements (required approvals,
     code owners) if/when branch protection is configured. -->

## Releases

Releases are **tag-driven**. Pushing a tag triggers `.github/workflows/release.yml`,
which builds the services and publishes container images to
`ghcr.io/wkspower/<service>:<tag>` (storage-api, opa, c7-external-tasks,
case-engine-rest-api, …). **The tag name is the image tag**, so the tag *is* the
release version.

- Use **semantic version** tags: `vMAJOR.MINOR.PATCH` (e.g. `v1.4.14`, heading to
  `v1.5.0`).
- For an out-of-band patch on an existing release, use a suffix:
  `v1.4.10-patch1`, `v1.4.7-fix1`.
- Cutting a release = creating and pushing the tag:

  ```bash
  git tag v1.5.0
  git push origin v1.5.0
  ```

### Changelog & release notes

We don't maintain a hand-written `CHANGELOG.md`. Because PR titles are
Conventional Commits, the squashed history **is** the changelog — generate the
release notes from it (GitHub's "Generate release notes" on the tag/Release works
well). This is exactly why clean, well-typed PR titles matter.

<!-- TODO(maintainers): if you adopt GitHub Releases, note the convention here
     (e.g. auto-generated notes, grouped by Conventional Commit type). -->

## Security fixes

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

<!-- TODO(maintainers): document where releases and security fixes are announced
     (team channel, mailing list, GitHub Releases) and who must be notified for a
     security release. -->

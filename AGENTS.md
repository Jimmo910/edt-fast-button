# Repository Guidelines

## Project Structure & Module Organization

This Maven/Tycho reactor builds a 1C:EDT Eclipse plug-in against `targets/default/` (EDT 2026.1). Production code is
under `bundles/ru.jimmo.edt.fastbutton.ui/src/`: `application` defines use cases and ports,
`infrastructure` contains JGit/Eclipse adapters, and `handlers`, `ui`, and `preferences` integrate with the workbench.
Tests are under `tests/`; `features/` contains the single installable feature, `repositories/` assembles its p2 site,
and `coverage/` enforces JaCoCo. Treat every `target/` directory as generated output.

## Build, Test, and Development Commands

Use JDK 17 and Maven 3.9.4+ from the repository root.

- `mvn clean verify` — run Checkstyle, tests, coverage, and build the p2 repository.
- `mvn verify` — verify while retaining prior build output.
- `.\tools\redeploy-edt.ps1 -EdtHome E:\edt-test -AcknowledgeTestInstallation` — install into an explicitly
  acknowledged test EDT; the script refuses Program Files installations.
- Wrapper launchers and scripts can exit 0 even on failure, so gate build success on the literal `BUILD SUCCESS` in
  the log, not the exit code.

The ZIP is under `repositories/ru.jimmo.edt.fastbutton.repository/target/`. EDT republishes its p2 channel in place;
if pinned units disappear, refresh `targets/default/default.target` against 2026.1 metadata.

## Architecture, Style & Safety

Keep `application` framework-independent and JGit/Eclipse details behind ports; use public EGit APIs only. UI-thread
code should only read workbench state: repository discovery and Git I/O belong in `SwitchAndUpdateBranchJob`.
Preserve its repository-wide scheduling rule and non-cancellable refresh after a worktree mutation. Reject dirty,
diverged, or unsupported states instead of resetting user work.

Write code, comments, and commits in English. Java uses four spaces, Allman braces, a 120-column limit, lowercase
packages, `PascalCase` types, `camelCase` members, and `UPPER_SNAKE_CASE` constants. Checkstyle and `.editorconfig`
enforce this. Externalize UI text through Eclipse NLS; keep English/Russian keys and placeholders identical, and use
`UpdateMessageResolver` for operation messages. Russian `.properties` values must be ASCII `\uXXXX` escapes (Eclipse
resource loaders read ISO-8859-1); generate the escapes programmatically rather than pasting Cyrillic. Pin GitHub
Actions to full commit SHAs.

Require-Bundle version ranges must be wide — `[X,X+2)` (for example `[6.8.0,8.0.0)`) or unbounded — never a
single-major range like `[6.8.0,7.0.0)`, which makes the bundle fail to resolve on the next EDT/EGit major and blocks
installation. Reuse platform commands (for example EGit's `org.eclipse.egit.ui.team.Branch`) by `commandId` rather
than reimplementing or redeclaring them, and guard each reused id with a contract test against the target platform;
never depend on EGit/JGit internal packages.

## Testing Guidelines

Tests use JUnit 4.13 in headless Tycho and temporary local JGit repositories. Name classes `*Test.java`; avoid network
and credentials. The 70% line-coverage gate covers application/Git code, job/progress orchestration, and
`UpdateMessageResolver`. Test success, dirty/diverged repositories, missing remotes, editor scoping, cancellation,
failures, and post-mutation refresh.

## Documentation, Releases & Pull Requests

`README.md` is Russian-primary and `README_EN.md` is its English counterpart. Keep `CHANGELOG.md` and
`CHANGELOG_RU.md` synchronized; `vX.Y.Z` releases extract the exact `## [X.Y.Z]` section from the Russian changelog.
Keep versions consistent across Maven, manifests, and feature metadata.

History uses short imperative subjects such as `Fix update-site duplication`; release commits use
`Prepare release X.Y.Z`. Keep commits focused. Pull requests need `## Summary` and `## Test plan`, behavior/safety
impact, linked issues, `mvn clean verify` results, and screenshots for UI changes. Never commit
credentials, workspace metadata, machine paths, or build output.

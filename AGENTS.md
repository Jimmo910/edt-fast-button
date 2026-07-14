# Repository Guidelines

## Project Structure & Module Organization

This is a Maven/Tycho reactor for a 1C:EDT Eclipse plug-in. `targets/default/` pins the EDT 2025.2 platform.
Production code is in `bundles/ru.jimmo.edt.fastbutton.ui/src/`: `application` contains use cases and ports,
`infrastructure` contains JGit/Eclipse adapters, and `handlers`, `ui`, and `preferences` contain workbench integration.
Tests live in `tests/ru.jimmo.edt.fastbutton.ui.tests/src/`. `features/` defines the single installable, localized p2
feature; `repositories/` assembles the update site and its category definition. `coverage/` generates and enforces
JaCoCo results.
Treat every `target/` directory as generated output.

## Build, Test, and Development Commands

Use JDK 17 and Maven 3.9.4 or newer from the repository root.

- `mvn clean verify` — compile, run Checkstyle and all tests, check coverage, and build the p2 repository.
- `mvn verify` — repeat verification without deleting previous outputs; useful for local iteration.
- `.\tools\redeploy-edt.ps1 -EdtHome E:\edt-test -AcknowledgeTestInstallation` — safely install through p2 into
  an explicitly acknowledged test EDT. The script refuses Program Files installations.

The distributable ZIP is under `repositories/ru.jimmo.edt.fastbutton.repository/target/`.

The pinned EDT p2 channel (`https://edt.1c.ru/downloads/releases/ruby/2025.2/`) is republished in place on EDT point
releases. If target resolution suddenly fails with missing installable-unit versions, refresh the pinned versions in
`targets/default/default.target` against the current channel content instead of debugging the build.

## Coding Style & Naming Conventions

Write code, comments, and commits in English. Java uses four spaces, Allman braces, and a 120-column limit. Use
lowercase packages, `PascalCase` types, `camelCase` members, and `UPPER_SNAKE_CASE` constants. Checkstyle and
`.editorconfig` enforce the baseline. Externalize UI text through Eclipse NLS and keep English/Russian keys and
placeholders identical. Do not introduce EGit internal APIs or cross the application/adapters boundary.

## Testing Guidelines

Tests use JUnit 4.13 and temporary local JGit repositories. Name classes `*Test.java`; avoid external network and
credential dependencies. Core application and Git adapter line coverage must remain at least 70%. Test safety paths
(dirty worktree, divergence, missing remote, cancellation) as well as successful updates.

## Commit & Pull Request Guidelines

No Git history is available in this checkout, so use short imperative subjects such as `Handle diverged branches`.
Keep commits focused. Pull requests must describe behavior and safety impact, link issues, report `mvn clean verify`,
and include screenshots for UI changes. Never commit credentials, workspace metadata, machine paths, or build output.

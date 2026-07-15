# Changelog

All notable changes to this project are documented here. The format follows Keep a Changelog, and releases use
Semantic Versioning.

## [Unreleased]

### Added

- A **Merge...** Git-project action that reuses EGit's standard merge dialog and icon.
- Per-button visibility toggles under **Window > Preferences > Fast Button** — every action can be shown or hidden
  (all shown by default).

## [0.1.3] - 2026-07-14

### Added

- Added a top-level **Switch to another branch...** Git-project action that reuses EGit's branch icon and standard
  dialog for selecting any branch, tag, or ref.

### Changed

- Broadened the JGit and EGit dependency ranges to `[6.8.0,8.0.0)` so the plug-in still installs on the next EDT major.

## [0.1.2] - 2026-07-14

### Fixed

- The update site no longer lists the plug-in three times in Install New Software; it offers a single feature under one category.

### Changed

- Documentation is Russian-first: README.md is now Russian and the English version moved to README_EN.md.
- GitHub release notes are now published in Russian from CHANGELOG_RU.md.

## [0.1.1] - 2026-07-14

### Changed

- Repository discovery and project mapping now run in the background job instead of the UI thread, keeping the
  workbench responsive on large workspaces.
- The background job orchestration is now covered by headless unit tests through a repository-resolver interface and
  a use-case factory seam.
- The success message now also appears in the status line when an editor is active, not only for views.
- Localized message formatting is centralized in one tested resolver; its coverage is now measured and enforced.
- SonarQube Cloud analysis is skipped for fork pull requests, whose runs cannot access the Sonar token, so external
  contributions are no longer blocked by the required check.
- GitHub Actions steps are pinned to commit SHAs.
- SonarQube Cloud now requires a real token, waits for the Quality Gate, and classifies the Tycho test bundle as test
  code for accurate issues and coverage.
- CI check names and Java 17 Dependabot constraints are explicit and suitable for branch protection.
- Eclipse lifecycle, repository ownership, handler results, and unsaved-editor traversal were clarified without
  changing user-visible behavior.

## [0.1.0] - 2026-07-13

### Added

- Workspace preference for the target branch, localized in English and Russian.
- Git-only EDT navigator command placed before the standard New group.
- Clean-worktree and unsaved-editor preconditions.
- Upstream fetch, safe checkout, local branch creation, and fast-forward-only updates.
- Explicit handling for missing remotes, missing branches, ahead and diverged histories, unsafe repository state,
  cancellation, and project refresh.
- JUnit integration tests, Checkstyle, JaCoCo coverage gate, CI, CodeQL, SonarQube Cloud integration, and automated
  attested p2 release archives.

[Unreleased]: https://github.com/Jimmo910/edt-fast-button/compare/v0.1.3...HEAD
[0.1.3]: https://github.com/Jimmo910/edt-fast-button/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/Jimmo910/edt-fast-button/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/Jimmo910/edt-fast-button/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/Jimmo910/edt-fast-button/releases/tag/v0.1.0

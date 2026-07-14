# Changelog

All notable changes to this project are documented here. The format follows Keep a Changelog, and releases use
Semantic Versioning.

## [Unreleased]

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

[Unreleased]: https://github.com/Jimmo910/edt-fast-button/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/Jimmo910/edt-fast-button/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/Jimmo910/edt-fast-button/releases/tag/v0.1.0

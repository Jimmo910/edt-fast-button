# Contributing

Thank you for improving Fast Button. For substantial behavior or extension-point changes, open an issue first so the
design and EDT compatibility can be discussed.

## Development setup

Use JDK 17 and Maven 3.9.4 or newer. Run the complete quality gate from the repository root:

```shell
mvn clean verify
```

The build downloads the pinned EDT 2025.2 target platform. Tests must use temporary local Git repositories and must
not depend on credentials or external services.

## Code expectations

- Keep business decisions in `application`; keep JGit and Eclipse dependencies in adapters.
- Preserve the non-destructive contract: no automatic merge, rebase, reset, force checkout, commit, or push.
- Add or update tests for every behavior change. Core application and Git adapter line coverage must remain at least
  70%.
- Externalize user-facing text to the English and Russian property bundles, preserving keys and placeholders.
- Follow the Java style enforced by Checkstyle and the repository `.editorconfig`.

## Pull requests

Use a short imperative title and keep commits focused. Describe the user-visible behavior and safety implications,
link related issues, and state that `mvn clean verify` passes. Include screenshots for preference-page, menu, or dialog
changes. Do not commit generated `target/` output, workspace metadata, credentials, or machine-specific paths.

By contributing, you agree that your contribution is provided under EPL-2.0.

## Maintainer releases

Release tags must match the Maven and feature base version. Creating and pushing an annotated tag such as `v0.1.0`
starts the release workflow. It runs the full quality gate, assigns a commit-based OSGi qualifier, produces the
installable p2 ZIP and SHA-256 checksum, creates a build-provenance attestation, and publishes a GitHub release. Never
upload a locally built archive as an official release asset.

SonarQube Cloud analysis is activated after importing the GitHub repository and adding its generated token as the
`SONAR_TOKEN` repository secret. Pull requests from forks intentionally run without that secret.

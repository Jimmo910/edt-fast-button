# Fast Button

[Русский](README.md) | [English](README_EN.md)

[![Build](https://github.com/Jimmo910/edt-fast-button/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Jimmo910/edt-fast-button/actions/workflows/build.yml)
[![CodeQL](https://github.com/Jimmo910/edt-fast-button/actions/workflows/codeql.yml/badge.svg?branch=main)](https://github.com/Jimmo910/edt-fast-button/security/code-scanning)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Jimmo910_edt-fast-button)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=bugs)](https://sonarcloud.io/summary/overall?id=Jimmo910_edt-fast-button)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Jimmo910_edt-fast-button)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=code_smells)](https://sonarcloud.io/summary/overall?id=Jimmo910_edt-fast-button)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Jimmo910_edt-fast-button)
[![Release](https://img.shields.io/github/v/release/Jimmo910/edt-fast-button?sort=semver)](https://github.com/Jimmo910/edt-fast-button/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Jimmo910/edt-fast-button/total)](https://github.com/Jimmo910/edt-fast-button/releases)
[![License: EPL-2.0](https://img.shields.io/badge/license-EPL--2.0-blue.svg)](LICENSE)
[![EDT 2026.1](https://img.shields.io/badge/1C%3AEDT-2026.1-orange.svg)](https://edt.1c.ru/)

Fast Button is an open-source plug-in that adds safe, repeatable project actions to 1C:Enterprise Development Tools
(EDT). Its UI is available in English and Russian.

## Features

The plug-in adds actions to the Git-project context menu in the EDT navigator. Each action appears only for
Git-connected projects and follows the EDT UI language (Russian or English). Any button can be hidden or shown under
**Window > Preferences > Fast Button** (all are shown by default).

- **Switch to the configured branch and update**
  - *What it does:* safely switches the project to a preconfigured branch and fast-forwards it from the remote in one
    click.
  - *How it works:* first rejects unsaved editors and any uncommitted changes (modified, staged, untracked); then
    fetches the configured upstream, `origin`, or the only unambiguous remote; creates or checks out the local branch;
    and updates it by fast-forward only. It never merges, rebases, resets, force-checks-out, commits, or pushes —
    diverged branches and unfinished Git operations are left for manual resolution.
  - *Configuration:* the target branch (default `main`) under **Window > Preferences > Fast Button**.
- **Switch to another branch…**
  - *What it does:* quickly checks out any local or remote-tracking branch, tag, or other ref.
  - *How it works:* opens EGit's standard branch-selection dialog (with the standard Git icon) and performs a regular
    checkout — without any automatic fetch or update from a remote.
  - *Configuration:* none.
- **Merge...**
  - *What it does:* merges a selected branch, tag, or ref into the current branch through EGit's standard machinery.
  - *How it works:* opens EGit's standard merge dialog (with the standard Git icon) and performs a regular merge
    (which may create a merge commit or report conflicts — that is EGit's own behaviour).
  - *Configuration:* none.

## Installation

1. Open the [latest release](https://github.com/Jimmo910/edt-fast-button/releases/latest).
2. Download `edt-fast-button-<version>.zip` — not GitHub's automatically generated source archives. Do not unpack it.
3. In EDT, open **Help > Install New Software**, then choose **Add > Archive** and select the downloaded ZIP.
4. Select **Fast Button**, complete the wizard, and restart EDT when prompted.

The current release target is EDT 2026.1. A release also contains a SHA-256 checksum. Its build provenance can be
verified with GitHub CLI:

```shell
gh attestation verify edt-fast-button-<version>.zip -R Jimmo910/edt-fast-button
```

## Build and test

Use JDK 17 and Maven 3.9.4 or newer:

```shell
mvn clean verify
```

The quality gate compiles the plug-in, runs JUnit and Checkstyle, checks core JaCoCo line coverage, and creates the p2
archive under `repositories/ru.jimmo.edt.fastbutton.repository/target/`.

For a disposable EDT installation on Windows, use the guarded p2 deployment script:

```powershell
.\tools\redeploy-edt.ps1 -EdtHome E:\edt-test -AcknowledgeTestInstallation
```

## Design and contributing

`application` contains framework-independent use cases and ports. `infrastructure/git` implements non-destructive
JGit operations, `infrastructure/repository` maps workspace projects to repositories, and `handlers`/`ui` adapt the
use case to Eclipse jobs and dialogs. Tests use temporary local repositories and require no external services.

See [CONTRIBUTING.md](CONTRIBUTING.md), [SECURITY.md](SECURITY.md), and [CHANGELOG.md](CHANGELOG.md). The project is
licensed under the [Eclipse Public License 2.0](LICENSE).

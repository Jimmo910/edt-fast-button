# Fast Button

[Русский](README.md) | [English](README_EN.md)

[![Build](https://github.com/Jimmo910/edt-fast-button/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Jimmo910/edt-fast-button/actions/workflows/build.yml)
[![CodeQL](https://github.com/Jimmo910/edt-fast-button/actions/workflows/codeql.yml/badge.svg?branch=main)](https://github.com/Jimmo910/edt-fast-button/security/code-scanning)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Jimmo910_edt-fast-button)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Jimmo910_edt-fast-button)
[![Release](https://img.shields.io/github/v/release/Jimmo910/edt-fast-button?sort=semver)](https://github.com/Jimmo910/edt-fast-button/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Jimmo910/edt-fast-button/total)](https://github.com/Jimmo910/edt-fast-button/releases)
[![License: EPL-2.0](https://img.shields.io/badge/license-EPL--2.0-blue.svg)](LICENSE)
[![EDT 2025.2](https://img.shields.io/badge/1C%3AEDT-2025.2-orange.svg)](https://edt.1c.ru/)

Fast Button is an open-source plug-in that adds safe, repeatable project actions to 1C:Enterprise Development Tools
(EDT). Its UI is available in English and Russian.

## Installation

1. Open the [latest release](https://github.com/Jimmo910/edt-fast-button/releases/latest).
2. Download `edt-fast-button-<version>.zip` — not GitHub's automatically generated source archives. Do not unpack it.
3. In EDT, open **Help > Install New Software**, then choose **Add > Archive** and select the downloaded ZIP.
4. Select **Fast Button**, complete the wizard, and restart EDT when prompted.

The current release target is EDT 2025.2. A release also contains a SHA-256 checksum. Its build provenance can be
verified with GitHub CLI:

```shell
gh attestation verify edt-fast-button-<version>.zip -R Jimmo910/edt-fast-button
```

## Switch and update a branch

Right-click a Git-connected project and choose **Switch to `<branch>` and update**. Configure the workspace-wide
target branch (default: `main`) under **Window > Preferences > Fast Button**.

The adjacent Git-icon **Switch to another branch...** action opens EGit's standard dialog for selecting any local
branch, remote-tracking branch, tag, or other ref. It performs a regular EGit checkout without automatically fetching
or updating from a remote.

Before changing anything, the command rejects unsaved editors and tracked, staged, or untracked repository changes.
It then fetches the configured upstream remote, `origin`, or the only unambiguous remote; creates or checks out the
target branch; and updates it only by fast-forward. The command is hidden for projects not shared with Git.

The plug-in never performs an automatic merge, rebase, reset, force checkout, commit, or push. Diverged branches and
repositories with unfinished Git operations are left unchanged for manual resolution.

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

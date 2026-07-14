# Быстрые кнопки

[Русский](README.md) | [English](README_EN.md)

[![Сборка](https://github.com/Jimmo910/edt-fast-button/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Jimmo910/edt-fast-button/actions/workflows/build.yml)
[![CodeQL](https://github.com/Jimmo910/edt-fast-button/actions/workflows/codeql.yml/badge.svg?branch=main)](https://github.com/Jimmo910/edt-fast-button/security/code-scanning)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Jimmo910_edt-fast-button)
[![Покрытие](https://sonarcloud.io/api/project_badges/measure?project=Jimmo910_edt-fast-button&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Jimmo910_edt-fast-button)
[![Релиз](https://img.shields.io/github/v/release/Jimmo910/edt-fast-button?sort=semver)](https://github.com/Jimmo910/edt-fast-button/releases/latest)
[![Загрузки](https://img.shields.io/github/downloads/Jimmo910/edt-fast-button/total)](https://github.com/Jimmo910/edt-fast-button/releases)
[![Лицензия: EPL-2.0](https://img.shields.io/badge/license-EPL--2.0-blue.svg)](LICENSE)
[![EDT 2025.2](https://img.shields.io/badge/1C%3AEDT-2025.2-orange.svg)](https://edt.1c.ru/)

«Быстрые кнопки» — открытый плагин с безопасными и повторяемыми действиями над проектами в 1С:Enterprise
Development Tools (EDT). Интерфейс локализован на русский и английский языки.

## Установка

1. Откройте [последний релиз](https://github.com/Jimmo910/edt-fast-button/releases/latest).
2. Скачайте `edt-fast-button-<версия>.zip`, а не автоматически созданный GitHub архив исходников. Не распаковывайте
   файл.
3. В EDT откройте **Справка > Установить новое ПО**, затем выберите **Добавить > Архив** и укажите скачанный ZIP.
4. Выберите **Быстрые кнопки**, завершите установку и перезапустите EDT по запросу.

Текущая целевая версия — EDT 2025.2. К релизу прикладывается контрольная сумма SHA-256. Происхождение сборки можно
проверить через GitHub CLI:

```shell
gh attestation verify edt-fast-button-<версия>.zip -R Jimmo910/edt-fast-button
```

## Переключение и обновление ветки

Нажмите правой кнопкой мыши по Git-проекту и выберите **Переключиться на `<ветка>` и обновить**. Целевая ветка для
рабочей области (по умолчанию `main`) настраивается в разделе **Окно > Параметры > Быстрые кнопки**.

Перед началом команда проверяет несохранённые редакторы, изменённые, staged и untracked-файлы. Затем она получает
изменения из настроенного upstream, `origin` или единственного однозначного remote, создаёт либо переключает локальную
ветку и обновляет её только через fast-forward. Для проектов без Git команда не отображается.

Плагин никогда автоматически не выполняет merge, rebase, reset, принудительное переключение, commit или push.
Разошедшиеся ветки и незавершённые Git-операции остаются без изменений для ручного разрешения.

## Сборка и тестирование

Нужны JDK 17 и Maven 3.9.4 или новее:

```shell
mvn clean verify
```

Команда компилирует плагин, запускает JUnit и Checkstyle, проверяет покрытие JaCoCo и создаёт p2-архив в
`repositories/ru.jimmo.edt.fastbutton.repository/target/`.

Архитектура и правила участия описаны в [CONTRIBUTING.md](CONTRIBUTING.md), изменения — в
[CHANGELOG.md](CHANGELOG.md), порядок сообщения об уязвимостях — в [SECURITY.md](SECURITY.md). Код распространяется
по лицензии [Eclipse Public License 2.0](LICENSE).

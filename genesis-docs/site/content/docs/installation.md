---
title: "Installation"
description: "How to install Genesis"
weight: 1
---

# Installation

## Requirements

- JDK 17 or later

## From Source

```bash
git clone https://github.com/genesis-ssg/genesis.git
cd genesis
./gradlew build
```

The CLI is available via Gradle:

```bash
./gradlew :genesis-cli:run --args="new my-site"
```

## From Release (planned)

Binary releases will be available via:

- GitHub Releases (fat JAR or native binary)
- Homebrew: `brew install genesis-ssg`
- Scoop (Windows): `scoop install genesis`

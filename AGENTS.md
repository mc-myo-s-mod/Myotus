# Repository Guidelines

## Project Structure & Module Organization
`src/main/java/me/myogoo/myotus` contains the mod code, split by responsibility: `api` for public extension points, `client` for UI and keybinds, `config` and `init` for setup, `mixin` for AE2/GuideME hooks, and `integration`/`util` for compatibility and shared helpers. Static assets live in `src/main/resources`, mod metadata templates live in `src/main/templates`, and generated data outputs should land in `src/generated/resources`. Ignore local runtime output under `run/`, `build/`, and `bin/`.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root:

- `./gradlew build` compiles the mod and produces jars.
- `./gradlew runClient` launches a local NeoForge client for manual testing.
- `./gradlew runServer` starts a dedicated server environment.
- `./gradlew runGameTestServer` runs registered GameTests and exits.
- `./gradlew runData` regenerates data-driven resources into `src/generated/resources`.
- `./gradlew publishToMavenLocal` publishes the artifact to your local Maven cache for downstream testing.

## Coding Style & Naming Conventions
Follow the existing Java style: 4-space indentation, braces on the same line, and one top-level class per file. Keep packages rooted under `me.myogoo.myotus`. Use `UpperCamelCase` for classes, `lowerCamelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants such as `MODID`. Favor descriptive names that match the feature area, for example `MyotusConfigScreen`, `ModIntegrationManagerImpl`, or `MEStorageScreenMixin`.

## Testing Guidelines
There is no committed `src/test` suite yet, so validate changes with targeted runtime checks. Use `runClient` for UI, keybinding, and integration behavior; use `runGameTestServer` when adding automated GameTests. Keep future tests close to the feature they cover and name them after the behavior under test, not generic helper names.

## Commit & Pull Request Guidelines
Recent history uses Conventional Commit prefixes such as `feat:`. Keep commit messages imperative and scoped, for example `fix: guard null config tab`. Pull requests should explain gameplay impact, list the mods or integrations affected, and include screenshots or short recordings for screen or widget changes. Link related issues and note any required data regeneration or manual test steps.

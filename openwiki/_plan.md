# Documentation Map

This wiki describes source in this checkout, not an independently versioned API specification.

| Page | Scope | Primary evidence |
| --- | --- | --- |
| [README](../README.md) | Version matrix and entry points | Module properties, settings, build scripts, `MyotusAPI` |
| [Quickstart](quickstart.md) | Addon compile/runtime setup | API JAR/publication tasks and loader metadata |
| [API](api.md) | Methods, examples, and limitations | `api/**`, integration manager, command registrar, menu helpers |
| [Architecture](architecture.md) | Lifecycle, sides, resources, version differences | Loader entrypoints, source sets, API interfaces |
| [Workflows](workflows.md) | Build/runtime commands and evidence boundaries | Gradle runs, JVM tests, GameTests |

When changing a public signature, loader version, resource merge rule, or lifecycle boundary, update its page and check all three loader implementations. Tie examples to an explicit version where Minecraft types differ. Verify relative links after moving source files. Do not substitute README claims for source inspection or imply a publication/runtime test occurred because a task exists.

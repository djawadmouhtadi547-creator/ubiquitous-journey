# Contributing

Thanks for helping improve NOVA Client.

## Development setup

- Use Java 17.
- Target Minecraft 1.20.1, Fabric Loader, and Fabric API versions pinned in `gradle.properties`.
- Run `./gradlew clean build` on Linux/macOS or `gradlew.bat clean build` on Windows before opening a pull request.
- Keep client-only behavior in the client source set. Add Mixin targets only when no suitable Fabric API exists, document the target, and test in the exact supported game version.

## Pull requests

- Describe user-visible behavior and manual test steps.
- Add tests for pure config / validation logic where practical.
- Do not bundle proprietary assets, tokens, private logs, or unrelated binaries.
- Update README / CHANGELOG when behavior or installation changes.
- Do not claim compatibility with third-party mods without testing it.

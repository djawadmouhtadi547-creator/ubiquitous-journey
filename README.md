# NOVA Client

**NOVA Client 1.2.0** is a free, open-source, client-side utility mod for **Minecraft Java Edition 1.20.1**, built with Fabric and Java 17. It provides a modular menu, configurable HUD widgets, local settings, and a small set of visual / quality-of-life modules.

> This repository is a source project. A locally tested runtime build has not been produced in every environment; use the build workflow or run the Gradle build before distributing a JAR.

## Included features

- **NOVA menu** — open with **Right Shift** by default; tabs for Dashboard, Modules, HUD Editor, Performance, Settings, and About.
- **Modular toggles** — Fullbright, Zoom, Toggle Sprint, Dynamic FPS, and Low Health Warning; each can be enabled from the UI or assigned a key.
- **Configurable HUD** — FPS, ping, coordinates, direction, biome, keystrokes, CPS, armor durability, status effects, local clock, player status, movement speed, memory use, frame-time average / 95th percentile, and a Target HUD showing the entity you are aiming at.
- **HUD editor** — drag widgets around the screen, enable/disable them, change scale and opacity, cycle text colors, use an alignment grid, and reset positions.
- **Local JSON settings** — defaults on first launch, basic validation, repair of missing fields, an attempt to preserve malformed files as `.broken-*`, and atomic saves.
- **Config import/export** — Settings exports to `config/nova-client-export.json`; place a JSON file with that name there to import it from the same screen.
- **Keybind editor** — menu key and module key rebinding; press Escape to cancel or Delete/Backspace to unbind. Menu controls do not trigger feature toggles while a screen is open.
- **English and Arabic strings** — Arabic rendering / shaping may depend on the Minecraft font renderer and resource pack.
- **GitHub Actions** — CI build + tests and tag-triggered Release attachment.

## Requirements

- Minecraft Java Edition **1.20.1**.
- Fabric Loader compatible with 1.20.1.
- Fabric API for 1.20.1.
- Java 17 to build from source.
- Internet access on first build to download the pinned Gradle distribution, Minecraft/Fabric dependencies, and Gradle plugins.

No account, API key, paid service, telemetry, advertising, or external client backend is required by this mod.

## Install the JAR

1. Install Minecraft Java Edition 1.20.1.
2. Install **Fabric Loader for Minecraft 1.20.1** and launch the Fabric profile once.
3. Download the built `nova-client-1.2.0.jar` from the GitHub Actions artifact or a GitHub Release (see below).
4. Put the mod JAR in your Minecraft `mods` directory, usually `%AppData%\.minecraft\mods` on Windows.
5. Ensure Fabric API for 1.20.1 is also in `mods`.
6. Launch the Fabric 1.20.1 profile. Press **Right Shift** in-game to open NOVA Client.
7. If it crashes, inspect `.minecraft\logs\latest.log` and the crash report before reporting an issue.

Do not put a `-sources.jar` in the `mods` folder; use the normal mod JAR.

## Build on Windows

1. Install a **64-bit JDK 17** (for example, Temurin 17).
2. Open Command Prompt or PowerShell in the repository folder.
3. Run:

   ```bat
   gradlew.bat --no-daemon clean build
   ```

The repository's bootstrap scripts download the pinned official **Gradle 8.8** distribution into your user Gradle cache the first time. The first build also needs an internet connection for dependencies. The main mod JAR should be at:

`build\libs\nova-client-1.2.0.jar`

Run tests alone with `gradlew.bat test`.

## Build on Linux / macOS

```sh
./gradlew --no-daemon clean build
```

## Get a JAR from GitHub Actions

1. Push the repository to GitHub.
2. Open **Actions → Build NOVA Client**.
3. Select a successful run on your branch.
4. In **Artifacts**, download `nova-client-<commit-sha>` and unzip it.
5. Copy the normal `nova-client-1.2.0.jar` into the Minecraft `mods` directory.

Pull requests run the build too, but cannot publish a GitHub Release. Normal build artifacts are retained for 14 days under the workflow's current settings.

## Create a GitHub Release

After committing and pushing your source, create and push a version tag:

```sh
git tag v1.2.0
git push origin v1.2.0
```

The `NOVA Client Release` workflow builds and tests first. Only if successful, it creates a GitHub Release and attaches the normal mod JAR. The workflow uses the repository-provided `GITHUB_TOKEN`; no personal access token needs to be stored in the code. If using `workflow_dispatch`, note that GitHub needs a tag ref for the release workflow to have a `GITHUB_REF_NAME` suitable for a release; the intended release path is a pushed `v*.*.*` tag.

## Settings and controls

- **Performance → MAX FPS / Balanced** — applies reversible vanilla video-option presets. Click **Restore previous video settings** to undo the preset.
- **Dynamic FPS** — while enabled in Modules, caps FPS to 30 when the game window is unfocused and restores the previous cap on focus.

- **Right Shift** — open / close the NOVA menu by default.
- **B** — Fullbright by default.
- **C** — Zoom by default.
- **R** — Toggle Sprint by default.
- Change these in **Settings → key rows**. Click a key value, then press a key. Escape cancels; Delete / Backspace unbinds.
- The **HUD Editor** lets you select a widget and use the panel to toggle it, reset position, adjust scale or opacity, cycle text and background colors, change horizontal alignment, apply a Balanced preset, or reset all positions. Press **M** to toggle menu visibility for the selected widget and **H** to hide or restore the editor panel so it does not obstruct a widget you need to drag.
- `config/nova-client.json` contains the current settings. `config/nova-client-export.json` is the import/export file.
- **Reset All** restores default settings. Keep a copy of the JSON file first if you want to keep a custom setup.

## Implemented limits

- **Fullbright** changes the client lightmap result; it does not reveal hidden ores or implement X-ray.
- **Zoom** sets the camera FOV while enabled. Its saved value is stored under `zoomFov` in the JSON config (15–60 degrees).
- **Toggle Sprint** requests sprinting while forward movement is held and vanilla conditions permit. Servers remain authoritative about movement.
- Ping shows `—` when no player-list latency is available (for example, some local contexts).
- CPS measures actual left/right mouse-button presses detected by the client; it does not automate clicks.
- Sodium, Iris, Mod Menu integration, crosshair skinning, damage indicators, screenshot notifications, and multi-profile config management have **not been implemented or certified** by this project. No compatibility claim is made for those mods.
- The HUD is refreshed on a short tick interval instead of recomputing all values for each rendered frame. Memory / clock values are informational estimates.

## Troubleshooting

- Confirm Minecraft and Fabric Loader both target **1.20.1**.
- Confirm Fabric API 1.20.1 is installed.
- Use Java 17 or newer to run; use Java 17 for builds.
- Delete nothing automatically: rename or back up `config/nova-client.json` if investigating settings problems.
- If a Mixin target changes in a different game patch or conflicts with another mod, attach `latest.log` and the crash report to an issue.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Please include the Minecraft version, Fabric Loader / Fabric API versions, reproducible steps, and relevant logs when filing bugs. Do not upload player logs containing private server addresses or tokens without reviewing them first.

## Security and privacy

See [SECURITY.md](SECURITY.md). NOVA Client has no network client code for collecting data. GitHub Actions downloads dependencies as part of building the project.

## License

MIT. See [LICENSE](LICENSE).

## New in 1.2.0

- **Mixin refmap generation** is configured in Gradle, with `sponge-mixin` as an annotation processor and the expected `nova_client-refmap.json` filename. Check the built JAR to confirm the refmap is present.
- **Target HUD** shows the name and health of the entity under the crosshair. It is a normal editable HUD widget and can be moved, resized, or disabled.
- **Low Health Warning** can be toggled in Modules. It shows one action-bar warning at 3 hearts or less and rearms after recovery to 4 hearts.
- Existing user settings are preserved; the new HUD widget and module receive defaults when the config is loaded.

## Performance features (1.1.0)

The **Performance** page offers two reversible vanilla-options presets:

- **MAX FPS** lowers view/simulation distance, disables clouds and entity shadows, minimizes particles and biome blending, selects Fast graphics, and raises the vanilla frame cap.
- **Balanced** makes gentler distance/effect changes.
- **Restore previous video settings** restores the options captured before the first NOVA preset.
- **Dynamic FPS** (Modules) temporarily caps FPS to 30 while the game window is unfocused and restores the previous cap when focus returns. This is a background power/heat saver; it does not increase FPS while actively playing.
- **Frame Time HUD** displays a rolling average and 95th-percentile frame time, which can help identify stutter that an FPS number alone can hide.

### About DLSS

NOVA Client does **not** implement NVIDIA DLSS. DLSS is a renderer-level temporal upscaling technology requiring compatible NVIDIA hardware and a supported integration; a Fabric client utility cannot enable genuine DLSS merely by changing a setting. The performance presets above tune supported vanilla video options and are not an upscaler. Results depend on the computer, world, resource packs, and other mods, so no fixed FPS gain is promised.

The MAX FPS preset changes graphics quality and render/simulation distances. Apply it only if you accept those visual trade-offs; use **Restore previous video settings** to undo it.

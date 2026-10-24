# Test and validation notes

## Automated checks

Run `./gradlew clean build` (or `gradlew.bat clean build` on Windows). This runs Java compilation, resource processing, the JUnit config validation tests, and builds the mod JAR.

The GitHub Actions workflow runs the same command and uploads the normal JAR as an artifact. The release workflow only publishes after a successful build and test task.

## Manual Minecraft smoke test

1. Use Minecraft Java 1.20.1 with a matching Fabric Loader and Fabric API.
2. Launch the Fabric profile and check `latest.log` for a successful `NOVA Client initialized` message.
3. Press Right Shift to open the NOVA screen; open Modules and toggle Fullbright, Zoom, and Toggle Sprint.
4. Check the HUD and open HUD Editor; drag a widget, change scale / opacity, hide the editor panel with H, and return to save.
5. Change keybinds and confirm the new choices persist after restart.
6. Export settings, modify or replace `nova-client-export.json`, import it, and verify the result.
7. Test in a local world and a multiplayer server where client-side cosmetic / QoL mods are permitted.

No manual game launch or screenshot capture is claimed by this repository scaffold until those steps are actually performed in a game environment.


## Performance additions (1.1.0)

- Open Performance, apply MAX FPS, check that video options change, then Restore previous video settings and verify every captured option returns.
- Apply Balanced and verify Restore previous video settings reverses it too.
- Enable Dynamic FPS, alt-tab to another window and verify the game cap changes to 30; focus the game again and verify the prior cap returns. Disable it while unfocused and verify restoration.
- Enable the Frame Time HUD widget and watch average / p95 values settle after several frames.
- Confirm NOVA does not display a fake DLSS toggle; performance notes distinguish vanilla option tuning from renderer-level upscaling.

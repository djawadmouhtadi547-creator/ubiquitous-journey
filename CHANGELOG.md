# Changelog

## 1.2.0
- Fixed Mixin refmap build configuration and pinned the expected `nova_client-refmap.json` name.
- Added Target HUD widget for the entity under the crosshair, including health for living entities.
- Added optional Low Health Warning module with an action-bar alert and a recovery-based reset to prevent spam.
- Updated in-game version labels, README, tests, and release notes for 1.2.0.

## 1.1.0
- Added a reversible MAX FPS preset and a Balanced performance preset.
- Added a frame-time HUD widget with rolling average and 95th-percentile samples.
- Added Dynamic FPS, which caps rendering while the game window is unfocused and restores the previous cap when focused.
- Added a Performance Center page and honest DLSS compatibility notes; NOVA does not claim to implement NVIDIA DLSS.

## 1.0.0 — Initial source release

- Added Fabric 1.20.1 client entrypoint and a custom NOVA settings screen.
- Added Fullbright, Zoom, and Toggle Sprint modules.
- Added HUD widgets with normalized positioning, scale, opacity, configurable text / background color, and a drag editor.
- Added local JSON settings, validation, backup of malformed config, and import/export.
- Added configurable keybinds with unbind/cancel behavior.
- Added English and Arabic translation files.
- Added Gradle build scripts, smoke-oriented config tests, CI artifacts, and tag-based GitHub Releases.

# Changelog

---

## [2.0.1] - 2026-08-16

### Fixed
- Crash on load with NeoForge and Forge: the `mod_compat_replace` structure processor was registered directly into the already-frozen vanilla registry during mod construction, which those loaders reject. It is now registered through the mod loading registration event. (Fabric was unaffected.)

---

## [2.0.0] - 2026-08-15

### Changed
- MVSI now enhances Moog's Voyager Structures instead of overwriting it: integrated variants generate alongside the vanilla structures, chosen at random based on which supported mods are installed, so worlds stay varied and nothing breaks when a mod is missing
- Supplementaries and Amendments are now optional — their variants appear when installed, and structures fall back to the vanilla versions when they are not
- Rebuilt as a multiloader mod for Fabric, NeoForge, and Forge

### Added
- Conditional structure variants for 80+ structures, gated on the installed mods
- `mod_compat_replace` structure processor that swaps blocks to modded equivalents only when that mod is present
- New integrated house variants: diorite tower, diorite & deepslate house, mud brick house, and two prismarine houses

---

## [1.0.2] - 2026-05-15

### Fixed
- Apostrophe in mod description caused a TOML parse error preventing the jar from loading (`Invalid entry separator 's' in inline table`); removed apostrophe from description text

---

## [1.0.1] - 2026-05-10

### Added
- Initial release of Moog's Voyager Structures Integrated
- Forked from Moog's Voyager Structures v5.0.9
- Replaced all structures with integrated versions using Supplementaries & Amendments blocks
- New mod ID `mvsintegrated` to coexist with the original MVS

### Changed
- Updated credits
- Updated icon.png

---

# Contributing

Thanks for taking the time to contribute! This is a Moog's structure mod and follows the same conventions as the rest of the family. Quick reference below.

## Setup

```bash
git clone <this-repo>
cd <this-repo>
./gradlew build
```

The build produces `build/libs/<ModName>-<mc>-<version>.jar`. Drop that into a Minecraft instance's `mods/` folder alongside `moogs-structure-lib` to test. There's no in-IDE run setup; datapack-style mods don't need one.

For the `scripts/` Python tooling:

```bash
pip install nbtlib
```

## Workflow

1. **Branch off the right base**: this repo uses MC-version branches (`1.20-datapack`, `1.21-datapack`). Make sure you branch off the one that matches the MC version you're targeting.
2. **Make your changes** (new structure, loot tweak, biome tag fix, etc.).
3. **Run the local checks**:
   ```bash
   .\validate.bat                  # full structure validator (Windows; see "Local validator" below)
   ./gradlew build                 # confirms the jar builds
   ```
4. **Update `CHANGELOG.md`**: add an entry under a new `## [VERSION] - YYYY-MM-DD` heading describing your change.
5. **Open a PR**. CI will rerun `nbt_check.py` and the structure validator automatically.

## Naming conventions

- Structure NBT filenames are `snake_case`, descriptive, with optional `_top`, `_side`, `_lower` suffixes for jigsaw pieces.
- Biome tag files live under `data/<mod_id>/tags/worldgen/biome/has_structure/` and use the form `<biome_category>_biomes.json`.
- Loot tables live under `data/<mod_id>/loot_tables/` (1.20) or `data/<mod_id>/loot_table/` (1.21).

## Biome tag pattern

Each `has_structure/<X>_biomes.json` should fan out to:

- `#minecraft:is_X`: vanilla equivalent (where one exists)
- `#c:<X>` (1.20 unprefixed) or `#c:is_<X>` (1.21 prefixed): Fabric/NeoForge convention, marked `required: false`
- `#forge:is_<X>`: Forge convention, marked `required: false` (1.20 only; 1.21 has migrated to `c:`)

Plus any explicit modded biome IDs (BYG, BiomesOPlenty, etc.) the mod targets.

## Licensing

This project is **dual-licensed**. See [`COPYING.md`](COPYING.md) for the full breakdown. The short version:

- Code, JSON, manifests, scripts → **LGPL-3.0**
- `.nbt` structure files → **All Rights Reserved (© FinnDog)**

When contributing **structure NBTs**, you grant FinnDog rights to redistribute them under the same ARR terms. **Don't redistribute existing NBTs from this repo** outside of pull requests back to the original.

## Local validator

`.\validate.bat` runs the same structure validator that CI runs. It expects the `MOOGS_VALIDATOR_PATH` environment variable to point at your local clone of [`moogs-structure-validator`](https://github.com/FinnSetchell/moogs-structure-validator)'s `validator.py`. One-time setup:

```bat
setx MOOGS_VALIDATOR_PATH "C:\path\to\moogs-structure-validator\validator.py"
```

Restart the terminal after running `setx` so the new value is visible. Then `.\validate.bat` works from the repo root.

## CI / tooling

- **Build** (`.github/workflows/build.yml`): runs on every push and PR
- **Validate** (`.github/workflows/validate.yml`): runs the structure validator on every push and PR (catches issues before release)
- **Release** (`.github/workflows/release.yml`): triggers on tags `<X.Y.Z>-<mc>` (e.g. `2.0.0-1.21`); builds, validates, publishes to Modrinth + CurseForge + Discord, creates a GitHub Release, and post-bumps the patch version
- **Dependabot**: weekly auto-bumps of GitHub Actions versions

## Releasing

```bash
# After your changes are merged into the version branch:
git tag 5.0.5-1.20      # format: <mod_version>-<minecraft_version>
git push origin 5.0.5-1.20
```

The release workflow handles everything from there, including the Discord review card. Bump `mod_version` in `gradle.properties` yourself before tagging.

## Questions

Discord is the fastest way to reach me. URL is in each mod's `gradle.properties` (`mod_discord=...`).

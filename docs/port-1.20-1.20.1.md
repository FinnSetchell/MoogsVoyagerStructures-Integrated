# MVSI port plan — 1.21.1 → 1.20–1.20.1

**Status:** planning, not started. **Branch:** `1.20-1.20.1` (forked from `1.21-datapack` at the 2.0.0 release).
**Concrete build target:** MC **1.20.1**, declared compat **1.20–1.20.1**.

Derived from a 4-agent research pass over MVS (1.20 branch), MSL (`1.20-1.20.4` branch),
the Modrinth API (dep availability), and MVSI's own port surface.

## Locked decisions

- **Loaders: Fabric + Forge only — NeoForge dropped.** Two independent blockers: MSL's 1.20 branch
  ships no NeoForge, and Amendments has no 1.20.1 NeoForge build. Every variant is gated on
  Amendments, so NeoForge could never satisfy the gate.
- **Build system: mirror MSL 1.20 — Architectury Loom** (`dev.architectury.loom`) for both loaders,
  Java 17, shadow. (Not the current fabric-loom + moddev + ForgeGradle split.)
- **NBT downgrade: scripted (nbtlib)** — deterministic, repeatable, verified per-file. Not a manual
  re-export of 85 modded structures.

## Viability (confirmed)

- Supplementaries `1.20-3.1.43`, Amendments `1.20-2.2.6`, Moonlight `1.20-2.16.34` all exist for
  1.20.1 on **Fabric and Forge**.
- All **77** pool-injection targets resolve: every `mvs:` pool we inject into exists on MVS's 1.20
  branch (145/145 of the 1.21 pools are present on 1.20).
- **Risk carried into Phase 0:** the 1.20.1 Supp/Amendments are much older lines (3.1.x vs 3.8.x).
  Blocks added after those versions load as air → per-structure palette audit required.

## Reference material

- MVS 1.20 format reference: `C:\Users\finn\IdeaProjects\MoogsVoyagerStructures` (branch `1.20-datapack`)
- MSL 1.20 toolchain + primitives: `C:\Users\finn\IdeaProjects\MoogsStructureLib` (branch `1.20-1.20.4`)
- MSL is consumed via `curse.maven:moogs-structure-lib-1337167:<fileId>` (project id reused; file ids change).

---

## Phase 0 — Go/no-go prerequisites — DONE (2026-08-16): both GO

- [x] **MSL 1.20 published — CONFIRMED.** CurseForge project `1337167` has 1.20-1.20.4 v3.0.2 builds:
      **`msl_fabric_file=8419670`**, **`msl_forge_file=8419671`** (no NeoForge). Published version 3.0.2
      → `structure_lib_version_range=[1.0.0,)` still covers it. Primitives all present on the
      `1.20-1.20.4` branch (`StructurePoolAccessor` accessors `moogs_structures_get{Raw}Templates`,
      `GeneralUtils.getAllDatapacksJSONElement`, pool-element codecs, `BlockAliasCompatCodec`).
- [x] **Block-palette parity — GO with 1 caveat.** 85 NBTs use 45 distinct modded blocks
      (40 Supplementaries + 5 Amendments). **43/45 exist on 1.20.1** (Supp 3.1.43 / Amendments 2.2.6).
      The 2 exceptions are only in **`houses/warped_house.nbt`**: `supplementaries:bunting_green`
      (`axis=z`) and `supplementaries:bunting_wall_green` (`facing=west`). 1.20.1 has buntings as a
      single unified `supplementaries:rope_buntings` (multipart blockstate; color stored in a block
      entity, not the block id). **Phase 4 special-case:** map both → `rope_buntings` with a green
      block-entity color (adding a block entity), or simplify warped_house's bunting decoration.

## Phase 1 — Build system (mirror MSL 1.20)

- [ ] Delete the `neoforge` subproject (module, `settings.gradle` include, Java classes,
      `neoforge.mods.toml`).
- [ ] Convert root `build.gradle` + `buildSrc` + `fabric`/`forge` `build.gradle` to Architectury Loom
      (both loaders) with shadow; drop `net.neoforged.moddev` and `net.minecraftforge.gradle`.
      Provide Mojmap to `common` via Loom (replaces the moddev/NeoForm path).
- [ ] `gradle.properties`: `java_version 21→17`, `minecraft_version→1.20.1`,
      `minecraft_version_range→1.20-1.20.1`, Forge `47.x`, Fabric API `+1.20.1`, MSL 1.20 file ids
      (drop `msl_neoforge_file`), remove `neo_form_version`/`neoforge_*`. Plugin versions from MSL:
      Loom `1.10-SNAPSHOT`, architectury-plugin `3.4-SNAPSHOT`, shadow `7.1.2`.
- [ ] Verify `collectJars` still collects the two loader jars into root `build/libs`.

## Phase 2 — Metadata & resources

- [ ] `common/src/main/resources/pack.mcmeta`: `pack_format 48 → 15` (single field).
- [ ] Fix the 3 **hard-coded** MC ranges (literals, not `${…}`):
      `fabric/src/main/resources/fabric.mod.json` `depends.minecraft` → `>=1.20 <1.20.2`;
      `forge/.../mods.toml` minecraft `versionRange → [1.20,1.20.2)`; delete `neoforge.mods.toml`.

## Phase 3 — Java code (small, scoped)

- [ ] **Fold in the 2.0.1 crash fix** (from `1.21-datapack`): register the `mod_compat_replace`
      processor via the mod-bus `RegisterEvent` on Forge (direct on Fabric), NOT a direct
      `Registry.register` during construction — Forge 1.20.1 freezes the vanilla registries before
      the mod constructor runs, same as NeoForge did on 1.21. `MVSIProcessors` becomes a data
      holder + `bootstrap(BiConsumer)`; `getType()` returns the constant; drop `MVSICommon.init()`.
- [ ] `ResourceLocation.fromNamespaceAndPath(…)` → `new ResourceLocation(…)` — 2 sites:
      `common/.../conditions/MvsiConditions.java:16`, `common/.../modinit/MVSIProcessors.java:35`.
- [ ] `MapCodec` → `Codec` for the structure-processor path (`ModCompatReplaceProcessor`,
      `MVSIProcessors`) — 1.20.1 `StructureProcessorType` expects a plain `Codec`.
- [ ] Verify the `RegistryOps.create(JsonOps.INSTANCE, access)` overload on 1.20.1
      (`PoolAdditionsMerger.java:62`). `registryOrThrow` + `Registries.TEMPLATE_POOL` are fine on 1.20.1.
- [ ] Delete the 3 NeoForge classes (`MVSINeoforge`, neoforge `PlatformHelperImpl`).

## Phase 4 — Data (85 NBTs, scripted downgrade)

- [ ] Rename `common/.../data/mvsintegrated/structure/` → `structures/` (plural). Pool-addition
      `location`s still resolve (they don't encode the dir) — no JSON edits.
- [ ] Scripted nbtlib downgrade:
  - 12 files with `components` items → old `Count`/`tag`:
    `beach_bar, small_pillager_tower, sunzi_gate, houses/deepslate_house, houses/desert_house,
    houses/diorite_and_deepslate_house, houses/diorite_tower, houses/house, houses/mud_brick_house_1,
    houses/prismarine_house_1, houses/prismarine_house_2, other_decoration/wooden_wheat_farm`.
  - 3 files with lowercase `attributes` entities → capital `Attributes` + UUID-based modifiers:
    `cathedral/base/base, houses/prismarine_house_2, houses/small_swamp_house`
    (`prismarine_house_2` needs both).
  - Re-stamp DataVersion `3955 → 3465` across all 85.
- [ ] Verify each downgraded file by re-reading with nbtlib; do not trust the script's summary.

## Phase 5 — Validate, test, publish

- [ ] `validator.json` → `mc_versions: ["1.20","1.20.1"]`. Validator handles the plural `structures/`
      dir; the 2 architectural fails (no template_pool/loot_table) persist; `validate` stays `false`.
- [ ] Build both jars; deploy to a 1.20.1 Fabric + Forge instance with MSL + MVS + Supplementaries +
      Amendments + Moonlight; confirm variants generate (`/place template mvsintegrated:supplementaries_amendments/<piece>`
      and worldgen).
- [ ] Publishing: add a **second target** to `.github/moogs-publish.yml` on the default branch —
      `branch: 1.20-1.20.1`, `loaders: [fabric, forge]`, `mc: {start: "1.20", end: "1.20.1"}`. Both
      branches are `mod-jar`, so one manifest covers both (no schema conflict). Add the 3 v2 caller
      workflows (checkout-composite shape) to this branch.

## Risks

1. **MSL 1.20 not published** (Phase 0) — blocks the whole port.
2. **Modded block palette gaps** on the older Supp/Amendments — may force per-structure swaps/drops.
3. **NBT attribute conversion fidelity** — the `attributes → Attributes` (UUID) downgrade is the
   trickiest surgery; verify strays/hat_stand spawn with correct attributes in-game.
4. **Bare-1.20 (non-.1) Forge loading** — building against 1.20.1/Forge 47 may not load on 1.20/Forge 46;
   validate if true bare-1.20 support is required, else treat the target as 1.20.1.

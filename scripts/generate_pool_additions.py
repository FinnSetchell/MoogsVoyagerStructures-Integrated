"""
Convert the remaining blind MVS structure overrides into conditional pool
additions, mirroring the hand-done `house` slice.

For every `.nbt` MVSI still ships under `data/mvs/structure/**` (which silently
shadows the matching MVS file on the classpath), this script:

  1. Parses every MVS template_pool JSON to learn which pool element references
     that structure (and with what projection / processors).
  2. Moves the override out of the `mvs:` namespace into `mvsintegrated:` so the
     MVS original becomes the pool's fallback again.
  3. Writes a `mvsi_pool_additions` file that injects the relocated piece into
     the same target pool, gated on supplementaries + amendments, mirroring the
     matched MVS element's projection and processors.

It is idempotent: re-running re-detects already-moved overrides as done, and
re-writes the additions files deterministically (sorted, one file per pool).

Run from anywhere:
    python scripts/generate_pool_additions.py
"""
from __future__ import annotations

import json
import shutil
from collections import defaultdict
from pathlib import Path

from _paths import RESOURCES_ROOT

# --- Tunables ---------------------------------------------------------------

# Favor the integrated variant when both mods are present (easy to retune).
DEFAULT_WEIGHT = 6

# The integration tier namespaces the NBTs so different mods' variants of the
# same piece don't collide: NBTs live under `.../structure/<INTEGRATION>/**` and
# the `location` carries the same segment. Pool-addition JSONs stay FLAT (one
# file per target pool); each mod's variant is a separate entry in that file's
# `additions` array, gated on its own CONDITIONS. So re-running for a new mod
# appends to the existing pool files rather than making a parallel tree.
# The tier name equals the mod-set in CONDITIONS — keep them in lockstep so the
# layout documents the gating. To scaffold a new integration, change both.
INTEGRATION = "supplementaries_amendments"

# Both mods drive the revamp blocks, so additions are gated on both.
CONDITIONS = [
    {"type": "mvsintegrated:mod_loaded", "modid": "supplementaries"},
    {"type": "mvsintegrated:mod_loaded", "modid": "amendments"},
]

# The already-migrated slice — never touch it.
ALREADY_MIGRATED = {"houses/house"}

# --- Repo locations ---------------------------------------------------------

# MVS base mod (the source of pool truth); read-only.
MVS_REPO = Path(r"C:\Users\finn\IdeaProjects\MoogsVoyagerStructures-1.21-datapack")
MVS_POOL_DIR = MVS_REPO / "src" / "main" / "resources" / "data" / "mvs" / "worldgen" / "template_pool"

# MVSI override layout (this repo).
MVS_OVERRIDE_DIR = RESOURCES_ROOT / "data" / "mvs" / "structure"
MVSI_STRUCTURE_DIR = RESOURCES_ROOT / "data" / "mvsintegrated" / "structure"
MVSI_ADDITIONS_DIR = RESOURCES_ROOT / "data" / "mvsintegrated" / "mvsi_pool_additions"


def _pool_id_for(path: Path) -> str:
    """data/mvs/worldgen/template_pool/houses/house/start_pool.json -> mvs:houses/house/start_pool"""
    rel = path.relative_to(MVS_POOL_DIR).as_posix()[: -len(".json")]
    return f"mvs:{rel}"


def parse_pools() -> dict[str, list[dict]]:
    """pool registry id -> list of element records (location/weight/projection/processors/...)."""
    pools: dict[str, list[dict]] = {}
    for jf in sorted(MVS_POOL_DIR.rglob("*.json")):
        data = json.loads(jf.read_text(encoding="utf-8"))
        pool_id = _pool_id_for(jf)
        elements = []
        for entry in data.get("elements", []):
            element = entry.get("element", {})
            elements.append(
                {
                    "location": element.get("location"),
                    "locations": element.get("locations") or {},
                    "weight": entry.get("weight", 1),
                    "projection": element.get("projection"),
                    "processors": element.get("processors"),
                    "element_type": element.get("element_type"),
                }
            )
        pools[pool_id] = elements
    return pools


def find_matches(pools: dict[str, list[dict]], override_location: str) -> list[tuple[str, dict]]:
    """Pool element(s) whose default `location` or any versioned `locations` value matches."""
    matches = []
    for pool_id, elements in pools.items():
        for el in elements:
            candidate_locations = set()
            if el["location"]:
                candidate_locations.add(el["location"])
            candidate_locations.update(el["locations"].values())
            if override_location in candidate_locations:
                matches.append((pool_id, el))
    return matches


def enumerate_overrides() -> list[Path]:
    return sorted(MVS_OVERRIDE_DIR.rglob("*.nbt"))


def main() -> None:
    pools = parse_pools()

    overrides = enumerate_overrides()
    migrated: list[str] = []
    unmatched: list[tuple[str, str]] = []
    versioned_cases: list[str] = []
    # target pool id -> list of (sort_key, addition dict)
    additions_by_pool: dict[str, list[tuple[str, dict]]] = defaultdict(list)

    for nbt in overrides:
        rel = nbt.relative_to(MVS_OVERRIDE_DIR).as_posix()[: -len(".nbt")]
        override_location = f"mvs:{rel}"

        if rel in ALREADY_MIGRATED:
            continue

        matches = find_matches(pools, override_location)
        if not matches:
            unmatched.append((rel, f"no MVS pool element references `{override_location}`"))
            continue

        # Move the nbt out of the mvs: override namespace (idempotent).
        dest = MVSI_STRUCTURE_DIR / INTEGRATION / f"{rel}.nbt"
        dest.parent.mkdir(parents=True, exist_ok=True)
        if nbt.exists():
            shutil.move(str(nbt), str(dest))

        for pool_id, el in matches:
            if el["element_type"] == "moogs_structures:versioned_single_pool_element":
                versioned_cases.append(f"{override_location} -> {pool_id}")

            # Ship a plain single_pool_element (1.21.1-only jar), but copy the
            # matched element's projection + processors verbatim so placement
            # stays identical to the MVS original.
            addition = {
                "element": {
                    "element_type": "minecraft:single_pool_element",
                    "location": f"mvsintegrated:{INTEGRATION}/{rel}",
                    "processors": el["processors"],
                    "projection": el["projection"],
                },
                "weight": DEFAULT_WEIGHT,
                "conditions": CONDITIONS,
            }
            additions_by_pool[pool_id].append((override_location, addition))

        migrated.append(rel)

    # Write one additions file per target pool. The pool id maps 1:1 to a file
    # path (mvs:houses/barn/start_pool -> houses/barn/start_pool.json).
    files_written = 0
    for pool_id, items in sorted(additions_by_pool.items()):
        rel_pool = pool_id.split(":", 1)[1]
        out = MVSI_ADDITIONS_DIR / f"{rel_pool}.json"
        out.parent.mkdir(parents=True, exist_ok=True)

        if out.exists():
            existing = json.loads(out.read_text(encoding="utf-8"))
            additions = existing.get("additions", [])
        else:
            additions = []

        # Append only additions not already present (idempotent on re-run).
        existing_locations = {a["element"]["location"] for a in additions}
        for _, addition in sorted(items, key=lambda kv: kv[0]):
            if addition["element"]["location"] not in existing_locations:
                additions.append(addition)
                existing_locations.add(addition["element"]["location"])

        doc = {"target_pool": pool_id, "additions": additions}
        out.write_text(json.dumps(doc, indent=2) + "\n", encoding="utf-8")
        files_written += 1

    # --- Report -------------------------------------------------------------
    print("=" * 70)
    print("Pool additions generation summary")
    print("=" * 70)
    print(f"Template pools parsed : {len(pools)}")
    print(f"Overrides found       : {len(overrides)} (excluding already-migrated house)")
    print(f"Migrated              : {len(migrated)}")
    print(f"Addition files written: {files_written}")
    print(f"Unmatched             : {len(unmatched)}")
    print(f"Versioned-pool cases  : {len(versioned_cases)}")

    if unmatched:
        print("\n--- MANUAL REVIEW (left untouched) ---")
        for rel, reason in unmatched:
            print(f"  mvs:structure/{rel}.nbt -- {reason}")

    if versioned_cases:
        print("\n--- Versioned pool matches (shipped as plain single_pool_element) ---")
        for line in versioned_cases:
            print(f"  {line}")


if __name__ == "__main__":
    main()

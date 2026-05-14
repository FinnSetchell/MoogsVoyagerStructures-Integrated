"""
Scan all NBT structure files and delete any that don't contain blocks or
items from Supplementaries or Amendments.  The check is a simple binary
search for the namespace prefixes inside the raw NBT data.
"""

import gzip
import os
from pathlib import Path
from _paths import PROJECT_ROOT

STRUCTURE_DIR = PROJECT_ROOT / "src" / "main" / "resources" / "data" / "mvs" / "structure"
MODDED_PREFIXES = [b"supplementaries:", b"amendments:"]


def has_modded_content(path: Path) -> bool:
    raw = path.read_bytes()
    try:
        data = gzip.decompress(raw)
    except (gzip.BadGzipFile, OSError):
        data = raw
    return any(prefix in data for prefix in MODDED_PREFIXES)


def main():
    deleted = []
    kept = []

    for nbt in sorted(STRUCTURE_DIR.rglob("*.nbt")):
        rel = nbt.relative_to(STRUCTURE_DIR)
        if has_modded_content(nbt):
            kept.append(rel)
        else:
            nbt.unlink()
            deleted.append(rel)

    print(f"\n=== Kept {len(kept)} files (contain Supplementaries/Amendments content) ===")
    for f in kept:
        print(f"  + {f}")

    print(f"\n=== Deleted {len(deleted)} files (vanilla only) ===")
    for f in deleted:
        print(f"  - {f}")

    # Clean up empty directories
    for dirpath, dirnames, filenames in os.walk(STRUCTURE_DIR, topdown=False):
        if not filenames and not dirnames:
            os.rmdir(dirpath)
            print(f"  (removed empty dir: {Path(dirpath).relative_to(STRUCTURE_DIR)})")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Fail when any non-excluded Go source file is below the minimum line coverage."""

from __future__ import annotations

import argparse
import sys
from collections import defaultdict
from pathlib import Path


EXCLUDE_PREFIXES = (
    "github.com/jinganix/webpb/plugin/cmd/",
    "github.com/jinganix/webpb/plugin/gen/",
    "github.com/jinganix/webpb/plugin/internal/testutil/",
)


def parse_profile(path: Path) -> dict[str, tuple[int, int]]:
    blocks: dict[str, tuple[int, int]] = {}
    with path.open(encoding="utf-8") as handle:
        handle.readline()
        for line in handle:
            parts = line.strip().split()
            if len(parts) < 3:
                continue
            key = parts[0]
            stmt_count = int(parts[1])
            covered = int(parts[2])
            if key in blocks:
                _, prev_covered = blocks[key]
                covered = max(prev_covered, covered)
            blocks[key] = (stmt_count, covered)

    by_file: dict[str, list[int]] = defaultdict(lambda: [0, 0])
    for key, (stmt_count, covered) in blocks.items():
        file_name = key.split(":", 1)[0]
        by_file[file_name][1] += stmt_count
        if covered > 0:
            by_file[file_name][0] += stmt_count
    return {file_name: (covered, total) for file_name, (covered, total) in by_file.items()}


def should_exclude(file_name: str) -> bool:
    return any(file_name.startswith(prefix) for prefix in EXCLUDE_PREFIXES)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("profile", type=Path)
    parser.add_argument("--min", type=float, default=90.0)
    args = parser.parse_args()

    if not args.profile.is_file():
        print(f"coverage profile not found: {args.profile}", file=sys.stderr)
        return 1

    by_file = parse_profile(args.profile)
    failures: list[tuple[float, str, int, int]] = []
    for file_name, (covered, total) in sorted(by_file.items()):
        if total == 0 or should_exclude(file_name):
            continue
        pct = 100.0 * covered / total
        if pct + 1e-9 < args.min:
            short = file_name.split("plugin/", 1)[-1]
            failures.append((pct, short, covered, total))

    if failures:
        print(f"Go file coverage below {args.min:g}%:", file=sys.stderr)
        for pct, short, covered, total in sorted(failures):
            print(f"  {pct:5.1f}% {short} ({covered}/{total})", file=sys.stderr)
        return 1

    checked = sum(
        1 for file_name, (_, total) in by_file.items() if total > 0 and not should_exclude(file_name)
    )
    print(f"Go file coverage OK ({checked} files >= {args.min:g}%)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

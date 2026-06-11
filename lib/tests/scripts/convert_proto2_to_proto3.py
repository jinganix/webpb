#!/usr/bin/env python3
"""Convert proto2 fixture trees to proto3 under test/proto3/."""

from __future__ import annotations

import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/proto/proto2"
DST = ROOT / "src/proto/proto3"

CASES = [
    "alias_skip",
    "auto_alias",
    "core_codegen",
    "enumeration",
    "errors",
    "generator_options",
    "imports",
    "message_extends",
]

GROUP_RE = re.compile(
    r"  required group (\w+) = (\d+) \{\n    required string test = 1;\n  \}\n",
)
DEFAULT_RE = re.compile(r" \[default = [^\]]+\]")


def convert_content(path: Path, text: str) -> str:
    text = text.replace('syntax = "proto2";', 'syntax = "proto3";')

    if path.name == "CoreMessages.proto":
        def replace_group(match: re.Match[str]) -> str:
            name = match.group(1)
            number = match.group(2)
            field_name = name[:1].lower() + name[1:]
            return (
                f"  message {name} {{\n"
                f"    string test = 1;\n"
                f"  }}\n\n"
                f"  {name} {field_name} = {number};\n"
            )

        text = GROUP_RE.sub(replace_group, text)

    text = re.sub(r"\brequired\s+", "", text)
    text = re.sub(r"\boptional\s+", "", text)
    text = DEFAULT_RE.sub("", text)
    return text


def main() -> None:
    if DST.exists():
        shutil.rmtree(DST)
    DST.mkdir()

    for case in CASES:
        src_case = SRC / case
        dst_case = DST / case
        for src_file in src_case.rglob("*.proto"):
            rel = src_file.relative_to(src_case)
            dst_file = dst_case / rel
            dst_file.parent.mkdir(parents=True, exist_ok=True)
            content = convert_content(src_file, src_file.read_text())
            dst_file.write_text(content)


if __name__ == "__main__":
    main()

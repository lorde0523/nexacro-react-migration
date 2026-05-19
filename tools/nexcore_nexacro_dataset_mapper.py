#!/usr/bin/env python3
"""
Nexcore BizUnit transactionId와 Nexacro transaction dataset을 대조해
Markdown 리포트와 DTO 후보 Java 파일을 생성하는 사이드 분석 도구.

수정 포인트:
- BIZUNIT_ID_TAGS: Nexcore에서 실제 비교해야 하는 ID 태그가 다르면 여기를 수정한다.
- TRANSACTION_ID_ARG_INDEX: Nexacro transaction(...)에서 서비스 ID가 몇 번째 인자인지 다르면 여기를 수정한다.
"""

from __future__ import annotations

import argparse
import csv
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable


# TODO: 실제 Nexcore 기준 ID가 transactionId가 아니라 methodId/serviceId라면 이 목록을 수정하세요.
BIZUNIT_ID_TAGS = ("transactionId",)

# TODO: Nexacro transaction("callbackId", "SERVICE_ID", ...) 기준입니다.
# 만약 첫 번째 인자("callbackId")가 실제 서비스 ID라면 0으로 바꾸세요.
TRANSACTION_ID_ARG_INDEX = 1

INPUT_DATASET_ARG_INDEX = 2
OUTPUT_DATASET_ARG_INDEX = 3


@dataclass(frozen=True)
class BizUnitMethod:
    match_id: str
    match_tag: str
    bizunit_id: str
    bizunit_name: str
    component_id: str
    method_id: str
    method_name: str
    transaction_id: str
    file_path: str


@dataclass(frozen=True)
class DatasetColumn:
    source_name: str
    java_name: str
    java_type: str
    nexacro_type: str
    size: str


@dataclass(frozen=True)
class Dataset:
    name: str
    columns: tuple[DatasetColumn, ...]


@dataclass(frozen=True)
class TransactionCall:
    name: str
    service_id: str
    input_datasets: dict[str, str]
    output_datasets: dict[str, str]


@dataclass(frozen=True)
class Screen:
    screen_id: str
    file_path: str
    datasets: dict[str, Dataset]
    transactions: tuple[TransactionCall, ...]


@dataclass
class DtoCandidate:
    class_name: str
    dataset_name: str
    direction: str
    fields: tuple[DatasetColumn, ...]


@dataclass
class MatchResult:
    screen: Screen
    transaction: TransactionCall
    bizunit_method: BizUnitMethod | None
    dto_candidates: list[DtoCandidate] = field(default_factory=list)


def parse_bizunits(root: Path) -> dict[str, BizUnitMethod]:
    methods: dict[str, BizUnitMethod] = {}
    for xml_path in iter_files(root, {".xml"}):
        try:
            tree = ET.parse(xml_path)
        except ET.ParseError:
            continue

        xml_root = tree.getroot()
        bizunits = [xml_root] if strip_namespace(xml_root.tag) == "bizUnit" else []
        bizunits.extend(tree.findall(".//bizUnit"))
        for bizunit in bizunits:
            bizunit_id = bizunit.attrib.get("id", "")
            bizunit_name = text_of(bizunit, "bizUnitName")
            component_id = text_of(bizunit, "componentId")
            for method in bizunit.findall(".//method"):
                method_id = text_of(method, "methodId")
                method_name = text_of(method, "methodName")
                transaction_id = text_of(method, "transactionId")
                for tag_name in BIZUNIT_ID_TAGS:
                    match_id = text_of(method, tag_name)
                    if not match_id:
                        continue
                    methods[match_id] = BizUnitMethod(
                        match_id=match_id,
                        match_tag=tag_name,
                        bizunit_id=bizunit_id,
                        bizunit_name=bizunit_name,
                        component_id=component_id,
                        method_id=method_id,
                        method_name=method_name,
                        transaction_id=transaction_id,
                        file_path=relative_path(root, xml_path),
                    )
    return methods


def parse_nexacro_screens(root: Path) -> list[Screen]:
    screens: list[Screen] = []
    for xfdl_path in iter_files(root, {".xfdl"}):
        xfdl_text = read_text(xfdl_path)
        screen_id = extract_screen_id(xfdl_text) or xfdl_path.stem
        xjs_path = xfdl_path.with_suffix(".xjs")
        script_text = xfdl_text
        if xjs_path.exists():
            script_text += "\n" + read_text(xjs_path)

        screens.append(
            Screen(
                screen_id=screen_id,
                file_path=relative_path(root, xfdl_path),
                datasets=parse_datasets(xfdl_text),
                transactions=parse_transactions(script_text),
            )
        )
    return screens


def parse_datasets(xfdl_text: str) -> dict[str, Dataset]:
    try:
        xml_root = ET.fromstring(xfdl_text)
    except ET.ParseError:
        return {}

    datasets: dict[str, Dataset] = {}
    for dataset_el in xml_root.findall(".//Dataset"):
        dataset_name = dataset_el.attrib.get("id", "")
        if not dataset_name:
            continue
        columns = []
        for column_el in dataset_el.findall(".//Column"):
            source_name = column_el.attrib.get("id", "")
            if not source_name:
                continue
            nexacro_type = column_el.attrib.get("type", "STRING")
            columns.append(
                DatasetColumn(
                    source_name=source_name,
                    java_name=to_camel_case(source_name),
                    java_type=to_java_type(nexacro_type),
                    nexacro_type=nexacro_type,
                    size=column_el.attrib.get("size", ""),
                )
            )
        datasets[dataset_name] = Dataset(dataset_name, tuple(columns))
    return datasets


def parse_transactions(script_text: str) -> tuple[TransactionCall, ...]:
    transactions: list[TransactionCall] = []
    for match in re.finditer(r"transaction\s*\((.*?)\)\s*;", script_text, re.IGNORECASE | re.DOTALL):
        args = split_js_args(match.group(1))
        if len(args) <= TRANSACTION_ID_ARG_INDEX:
            continue

        name = unquote(args[0]) if args else ""
        service_id = normalize_service_id(unquote(args[TRANSACTION_ID_ARG_INDEX]))
        input_arg = unquote(args[INPUT_DATASET_ARG_INDEX]) if len(args) > INPUT_DATASET_ARG_INDEX else ""
        output_arg = unquote(args[OUTPUT_DATASET_ARG_INDEX]) if len(args) > OUTPUT_DATASET_ARG_INDEX else ""
        transactions.append(
            TransactionCall(
                name=name,
                service_id=service_id,
                input_datasets=parse_dataset_map(input_arg),
                output_datasets=parse_dataset_map(output_arg),
            )
        )
    return tuple(transactions)


def build_matches(screens: Iterable[Screen], bizunit_methods: dict[str, BizUnitMethod]) -> list[MatchResult]:
    results: list[MatchResult] = []
    for screen in screens:
        for transaction in screen.transactions:
            method = bizunit_methods.get(transaction.service_id)
            result = MatchResult(screen=screen, transaction=transaction, bizunit_method=method)
            result.dto_candidates.extend(build_dto_candidates(screen, transaction))
            results.append(result)
    return results


def build_dto_candidates(screen: Screen, transaction: TransactionCall) -> list[DtoCandidate]:
    candidates: list[DtoCandidate] = []
    base_name = to_pascal_case(screen.screen_id) + to_pascal_case(transaction.name or transaction.service_id)
    for dataset_name in transaction.input_datasets.values():
        dataset = screen.datasets.get(dataset_name)
        if dataset:
            candidates.append(DtoCandidate(base_name + "Request", dataset.name, "Request", dataset.columns))
    for dataset_name in transaction.output_datasets.values():
        dataset = screen.datasets.get(dataset_name)
        if dataset:
            candidates.append(DtoCandidate(base_name + "Response", dataset.name, "Response", dataset.columns))
    return candidates


def write_markdown(results: list[MatchResult], output_path: Path) -> None:
    lines = [
        "# Nexcore-Nexacro Dataset DTO Mapping",
        "",
        f"- BizUnit 비교 태그: `{', '.join(BIZUNIT_ID_TAGS)}`",
        f"- Nexacro transaction 서비스 ID 인자 위치: `{TRANSACTION_ID_ARG_INDEX}`",
        "",
        "| 상태 | 화면 | transaction | serviceId | BizUnit | methodId | input dataset | output dataset | DTO 후보 |",
        "| --- | --- | --- | --- | --- | --- | --- | --- | --- |",
    ]
    for result in results:
        method = result.bizunit_method
        status = "MATCHED" if method else "NOT_FOUND"
        dto_names = "<br>".join(dto.class_name for dto in result.dto_candidates)
        lines.append(
            "| "
            + " | ".join(
                [
                    status,
                    escape_md(result.screen.screen_id),
                    escape_md(result.transaction.name),
                    escape_md(result.transaction.service_id),
                    escape_md(method.bizunit_id if method else ""),
                    escape_md(method.method_id if method else ""),
                    escape_md(", ".join(result.transaction.input_datasets.values())),
                    escape_md(", ".join(result.transaction.output_datasets.values())),
                    escape_md(dto_names),
                ]
            )
            + " |"
        )

    lines.extend(["", "## DTO 상세", ""])
    for result in results:
        for dto in result.dto_candidates:
            lines.append(f"### {dto.class_name}")
            lines.append("")
            lines.append(f"- 화면: `{result.screen.screen_id}`")
            lines.append(f"- serviceId: `{result.transaction.service_id}`")
            lines.append(f"- dataset: `{dto.dataset_name}`")
            lines.append(f"- direction: `{dto.direction}`")
            lines.append("")
            lines.append("| Java field | Java type | Nexacro column | Nexacro type | size |")
            lines.append("| --- | --- | --- | --- | --- |")
            for field_item in dto.fields:
                lines.append(
                    f"| {field_item.java_name} | {field_item.java_type} | "
                    f"{field_item.source_name} | {field_item.nexacro_type} | {field_item.size} |"
                )
            lines.append("")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(lines), encoding="utf-8")


def write_csv(results: list[MatchResult], output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8", newline="") as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(
            [
                "status",
                "screen_id",
                "screen_file",
                "transaction",
                "service_id",
                "bizunit_id",
                "method_id",
                "match_tag",
                "input_dataset",
                "output_dataset",
                "dto_candidates",
            ]
        )
        for result in results:
            method = result.bizunit_method
            writer.writerow(
                [
                    "MATCHED" if method else "NOT_FOUND",
                    result.screen.screen_id,
                    result.screen.file_path,
                    result.transaction.name,
                    result.transaction.service_id,
                    method.bizunit_id if method else "",
                    method.method_id if method else "",
                    method.match_tag if method else "",
                    ", ".join(result.transaction.input_datasets.values()),
                    ", ".join(result.transaction.output_datasets.values()),
                    ", ".join(dto.class_name for dto in result.dto_candidates),
                ]
            )


def write_dto_files(results: list[MatchResult], dto_dir: Path, package_name: str) -> None:
    dto_dir.mkdir(parents=True, exist_ok=True)
    seen: set[str] = set()
    for result in results:
        if result.bizunit_method is None:
            continue
        for dto in result.dto_candidates:
            if dto.class_name in seen:
                continue
            seen.add(dto.class_name)
            imports = sorted({field_item.java_type for field_item in dto.fields if "." in field_item.java_type})
            lines = [f"package {package_name};", ""]
            for import_name in imports:
                lines.append(f"import {import_name};")
            if imports:
                lines.append("")
            lines.append(f"public record {dto.class_name}(")
            field_lines = []
            for field_item in dto.fields:
                type_name = field_item.java_type.rsplit(".", 1)[-1]
                field_lines.append(f"        {type_name} {field_item.java_name}")
            lines.append(",\n".join(field_lines))
            lines.append(") {")
            lines.append("}")
            dto_path = dto_dir / f"{dto.class_name}.java"
            dto_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def iter_files(root: Path, suffixes: set[str]) -> Iterable[Path]:
    if not root.exists():
        raise FileNotFoundError(root)
    for path in sorted(root.rglob("*")):
        if path.is_file() and path.suffix.lower() in suffixes:
            yield path


def text_of(element: ET.Element, tag_name: str) -> str:
    found = element.find(tag_name)
    return (found.text or "").strip() if found is not None else ""


def strip_namespace(tag_name: str) -> str:
    return tag_name.rsplit("}", 1)[-1]


def read_text(path: Path) -> str:
    for encoding in ("utf-8-sig", "utf-8", "cp949", "euc-kr"):
        try:
            return path.read_text(encoding=encoding)
        except UnicodeDecodeError:
            continue
    return path.read_text(errors="ignore")


def extract_screen_id(xfdl_text: str) -> str:
    match = re.search(r"<Form\s+[^>]*id=[\"']([^\"']+)[\"']", xfdl_text, re.IGNORECASE)
    return match.group(1) if match else ""


def split_js_args(expression: str) -> list[str]:
    args: list[str] = []
    current: list[str] = []
    quote = ""
    depth = 0
    escaped = False
    for ch in expression:
        if escaped:
            current.append(ch)
            escaped = False
            continue
        if ch == "\\":
            current.append(ch)
            escaped = True
            continue
        if quote:
            current.append(ch)
            if ch == quote:
                quote = ""
            continue
        if ch in {"'", '"'}:
            quote = ch
            current.append(ch)
            continue
        if ch in "([{":
            depth += 1
        elif ch in ")]}" and depth > 0:
            depth -= 1
        if ch == "," and depth == 0:
            args.append("".join(current).strip())
            current = []
            continue
        current.append(ch)
    if current:
        args.append("".join(current).strip())
    return args


def parse_dataset_map(value: str) -> dict[str, str]:
    result: dict[str, str] = {}
    for token in value.split():
        if "=" not in token:
            continue
        left, right = token.split("=", 1)
        if left and right:
            result[left.strip()] = right.strip()
    return result


def normalize_service_id(value: str) -> str:
    service_id = value.strip()
    if "::" in service_id:
        service_id = service_id.split("::", 1)[1]
    return service_id


def unquote(value: str) -> str:
    value = value.strip()
    if len(value) >= 2 and value[0] == value[-1] and value[0] in {"'", '"'}:
        return value[1:-1]
    return value


def to_java_type(nexacro_type: str) -> str:
    normalized = (nexacro_type or "").upper()
    if normalized in {"INT", "INTEGER"}:
        return "Integer"
    if normalized in {"LONG", "BIGDECIMAL", "FLOAT", "DOUBLE", "DECIMAL"}:
        return "java.math.BigDecimal"
    if normalized in {"DATE", "DATETIME", "TIME"}:
        return "java.time.LocalDateTime"
    if normalized in {"BOOLEAN", "BOOL"}:
        return "Boolean"
    return "String"


def to_pascal_case(value: str) -> str:
    words = split_words(value)
    return "".join(word[:1].upper() + word[1:].lower() for word in words) or "Generated"


def to_camel_case(value: str) -> str:
    pascal = to_pascal_case(value)
    return pascal[:1].lower() + pascal[1:] if pascal else pascal


def split_words(value: str) -> list[str]:
    spaced = re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", value)
    return [word for word in re.split(r"[^A-Za-z0-9]+", spaced) if word]


def escape_md(value: str) -> str:
    return value.replace("|", "\\|").replace("\n", " ")


def relative_path(root: Path, path: Path) -> str:
    return path.relative_to(root).as_posix()


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Nexcore BizUnit transactionId와 Nexacro transaction dataset을 매핑합니다."
    )
    parser.add_argument("--nexacro-root", required=True, type=Path, help=".xfdl/.xjs 화면 소스 루트")
    parser.add_argument("--nexcore-root", required=True, type=Path, help="Nexcore BizUnit XML 소스 루트")
    parser.add_argument("--out", default=Path("build/migration-analysis"), type=Path, help="결과 출력 폴더")
    parser.add_argument("--package", default="com.lorde0523.migration.generated.dto", help="생성 DTO Java package")
    parser.add_argument("--write-dtos", action="store_true", help="매칭된 항목의 DTO .java 파일을 생성")
    return parser.parse_args(argv)


def main(argv: list[str]) -> int:
    args = parse_args(argv)
    bizunit_methods = parse_bizunits(args.nexcore_root)
    screens = parse_nexacro_screens(args.nexacro_root)
    results = build_matches(screens, bizunit_methods)

    args.out.mkdir(parents=True, exist_ok=True)
    write_markdown(results, args.out / "mapping-report.md")
    write_csv(results, args.out / "mapping-report.csv")
    if args.write_dtos:
        write_dto_files(results, args.out / "dto", args.package)

    matched = sum(1 for result in results if result.bizunit_method is not None)
    print(f"screens={len(screens)} transactions={len(results)} matched={matched} output={args.out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))

#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

invalid_methods="$(rg -n --pcre2 \
  '^\s*(?:public\s+|protected\s+|private\s+)?void\s+(?![a-z][A-Za-z0-9]*\s*\()[^\s(]+\s*\(' \
  --glob '**/src/test/**/*.java' || true)"

if [[ -n "${invalid_methods}" ]]; then
  echo "测试方法名必须使用 ASCII lowerCamelCase："
  echo "${invalid_methods}"
  exit 1
fi

echo "测试方法命名契约通过。"

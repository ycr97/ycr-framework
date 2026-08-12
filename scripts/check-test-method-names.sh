#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

if command -v rg >/dev/null 2>&1; then
  invalid_methods="$(rg -n --pcre2 \
    '^\s*(?:public\s+|protected\s+|private\s+)?void\s+(?![a-z][A-Za-z0-9]*\s*\()[^\s(]+\s*\(' \
    --glob '**/src/test/**/*.java' || true)"
else
  invalid_methods="$(find . -type f -path '*/src/test/*.java' -exec perl -ne '
    if (/^\s*(?:(?:public|protected|private)\s+)?void\s+([^\s(]+)\s*\(/ && $1 !~ /^[a-z][A-Za-z0-9]*$/) {
      print "$ARGV:$.:$_";
    }
    close ARGV if eof;
  ' {} +)"
fi

if [[ -n "${invalid_methods}" ]]; then
  echo "测试方法名必须使用 ASCII lowerCamelCase："
  echo "${invalid_methods}"
  exit 1
fi

echo "测试方法命名契约通过。"

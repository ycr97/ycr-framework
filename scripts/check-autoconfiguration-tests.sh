#!/usr/bin/env bash
set -euo pipefail

missing=0
while IFS= read -r imports_file; do
  while IFS= read -r class_name; do
    [[ -z "${class_name}" || "${class_name}" == \#* ]] && continue
    simple_name="${class_name##*.}"
    if ! rg --files | rg -q "/${simple_name}Test\\.java$"; then
      echo "缺少自动配置测试: ${class_name} (${imports_file})"
      missing=1
    fi
  done < "${imports_file}"
done < <(rg --files -g 'org.springframework.boot.autoconfigure.AutoConfiguration.imports')

exit "${missing}"

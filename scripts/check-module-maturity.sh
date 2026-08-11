#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

experimental_poms=(
  "ycr-starter-business/pom.xml"
  "ycr-starter-sdk/pom.xml"
  "ycr-starter-crud/pom.xml"
  "ycr-starter-ddd/pom.xml"
  "ycr-starter-ddd/ycr-starter-ddd-core/pom.xml"
  "ycr-starter-ddd/ycr-starter-ddd-extension/pom.xml"
  "ycr-starter-ddd/ycr-starter-ddd-statemachine/pom.xml"
)

for pom in "${experimental_poms[@]}"; do
  if ! rg -q '<ycr\.module\.maturity>experimental</ycr\.module\.maturity>' "${pom}"; then
    echo "实验性模块缺少 maturity 标记: ${pom}"
    exit 1
  fi
  if ! rg -q '<description>\[Experimental\]' "${pom}"; then
    echo "实验性模块 POM description 缺少标记: ${pom}"
    exit 1
  fi
done

while IFS= read -r pom; do
  case "${pom}" in
    ycr-framework-bom/pom.xml|ycr-starter-business/*|ycr-starter-sdk/*|ycr-starter-crud/*|ycr-starter-ddd/*)
      ;;
    *)
      echo "Stable 模块不得传递依赖 Experimental 模块: ${pom}"
      exit 1
      ;;
  esac
done < <(rg -l '<artifactId>ycr-starter-(crud|sdk|business|ddd[^<]*)</artifactId>' --glob 'pom.xml')

echo "模块成熟度与依赖边界契约通过。"

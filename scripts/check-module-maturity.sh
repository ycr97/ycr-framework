#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

experimental_poms=(
  "incubator/ycr-starter-business/pom.xml"
  "incubator/ycr-starter-sdk/pom.xml"
  "incubator/ycr-starter-crud/pom.xml"
  "incubator/ycr-starter-ddd/pom.xml"
  "incubator/ycr-starter-ddd/ycr-starter-ddd-core/pom.xml"
  "incubator/ycr-starter-ddd/ycr-starter-ddd-extension/pom.xml"
  "incubator/ycr-starter-ddd/ycr-starter-ddd-statemachine/pom.xml"
)

for pom in "${experimental_poms[@]}"; do
  if ! grep -Eq '<ycr\.module\.maturity>experimental</ycr\.module\.maturity>' "${pom}"; then
    echo "实验性模块缺少 maturity 标记: ${pom}"
    exit 1
  fi
  if ! grep -Eq '<description>\[Experimental\]' "${pom}"; then
    echo "实验性模块 POM description 缺少标记: ${pom}"
    exit 1
  fi
done

while IFS= read -r pom; do
  case "${pom}" in
    build/ycr-framework-bom/pom.xml|incubator/ycr-starter-business/*|incubator/ycr-starter-sdk/*|incubator/ycr-starter-crud/*|incubator/ycr-starter-ddd/*)
      ;;
    *)
      echo "Stable 模块不得传递依赖 Experimental 模块: ${pom}"
      exit 1
      ;;
  esac
done < <(find . -name pom.xml -type f -exec grep -El \
  '<artifactId>ycr-starter-(crud|sdk|business|ddd[^<]*)</artifactId>' {} + | sed 's#^\./##')

echo "模块成熟度与依赖边界契约通过。"

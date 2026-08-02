#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

revision="$(sed -n 's/^-Drevision=//p' .mvn/maven.config)"
if [[ -z "${revision}" ]]; then
  echo ".mvn/maven.config 缺少 -Drevision"
  exit 1
fi

for pom in pom.xml ycr-dependencies/pom.xml ycr-framework-bom/pom.xml; do
  declared="$(sed -n 's:.*<revision>\([^<]*\)</revision>.*:\1:p' "${pom}" | head -n 1)"
  if [[ "${declared}" != "${revision}" ]]; then
    echo "版本源不一致: ${pom}=${declared}, .mvn/maven.config=${revision}"
    exit 1
  fi
done

if [[ "${revision}" != *-SNAPSHOT ]]; then
  tag="v${revision}"
  tag_commit="$(git rev-list -n 1 "${tag}" 2>/dev/null || true)"
  if [[ -n "${tag_commit}" && "$(git rev-parse HEAD)" != "${tag_commit}" ]]; then
    echo "版本 ${revision} 已由 ${tag} 固定，当前 HEAD 不得继续复用该制品坐标"
    exit 1
  fi
fi

echo "版本一致性与不可变性契约通过。"

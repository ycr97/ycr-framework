#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

fail() {
  echo "模块边界检查失败: $*" >&2
  exit 1
}

project_artifact() {
  perl -0777 -ne 's#<parent>.*?</parent>##s; print $1 if /<artifactId>([^<]+)<\/artifactId>/' "$1"
}

parent_artifact() {
  perl -0777 -ne 'print $1 if /<parent>.*?<artifactId>([^<]+)<\/artifactId>.*?<\/parent>/s' "$1"
}

parent_relative_path() {
  perl -0777 -ne 'print $1 if /<parent>.*?<relativePath>([^<]+)<\/relativePath>.*?<\/parent>/s' "$1"
}

runtime_dependencies() {
  perl -0777 -ne '
    while (/<dependency>(.*?)<\/dependency>/sg) {
      my $dependency = $1;
      next if $dependency =~ /<scope>test<\/scope>/;
      print "$1\n" if $dependency =~ /<artifactId>([^<]+)<\/artifactId>/;
    }
  ' "$1"
}

module_paths=()
while IFS= read -r module; do
  module_paths+=("${module}")
done < <(sed -n '/<modules>/,/<\/modules>/p' pom.xml \
  | sed -n 's:.*<module>\([^<]*\)</module>.*:\1:p')

[[ "${#module_paths[@]}" -eq 36 ]] || fail "根 reactor 应包含 36 个一级模块，实际 ${#module_paths[@]} 个"

duplicates="$(printf '%s\n' "${module_paths[@]}" | sort | uniq -d)"
[[ -z "${duplicates}" ]] || fail "根 reactor 存在重复模块: ${duplicates}"

for module in "${module_paths[@]}"; do
  [[ -f "${module}/pom.xml" ]] || fail "根 reactor 模块不存在: ${module}"
  case "${module}" in
    build/*|foundation/*|platform/*|extensions/*|incubator/*) ;;
    *) fail "根 reactor 模块不在约定架构分区: ${module}" ;;
  esac
done

declared_direct="$(printf '%s\n' "${module_paths[@]}" | sort)"
actual_direct="$(find build foundation platform extensions incubator -mindepth 2 -maxdepth 2 -name pom.xml \
  | sed 's#/pom.xml$##' | sort)"
[[ "${declared_direct}" == "${actual_direct}" ]] || fail "根 reactor 与一级分区中的直接 Maven 模块不一致"

while IFS= read -r pom; do
  parent_artifact="$(parent_artifact "${pom}")"
  [[ -z "${parent_artifact}" ]] && continue

  relative_path="$(parent_relative_path "${pom}")"
  [[ -z "${relative_path}" ]] && relative_path="../pom.xml"
  parent_pom="$(cd "$(dirname "${pom}")" && realpath "${relative_path}")"
  [[ -f "${parent_pom}" ]] || fail "parent relativePath 不存在: ${pom} -> ${relative_path}"

  actual_parent="$(project_artifact "${parent_pom}")"
  [[ "${actual_parent}" == "${parent_artifact}" ]] \
    || fail "parent 坐标不匹配: ${pom} 声明 ${parent_artifact}，实际 ${actual_parent}"
done < <(find build foundation platform extensions incubator -name pom.xml -type f | sort)

module_layer() {
  local artifact="$1"
  local module

  for module in "${module_paths[@]}"; do
    if [[ "$(project_artifact "${module}/pom.xml")" == "${artifact}" ]]; then
      echo "${module%%/*}"
      return
    fi
  done
}

check_runtime_dependencies() {
  local pom="$1"
  local source_layer="$2"
  local dependency_artifact
  local dependency_layer

  while IFS= read -r dependency_artifact; do
    [[ -z "${dependency_artifact}" ]] && continue
    dependency_layer="$(module_layer "${dependency_artifact}")"
    [[ -z "${dependency_layer}" ]] && continue

    case "${source_layer}:${dependency_layer}" in
      foundation:platform|foundation:extensions|foundation:incubator|platform:extensions|platform:incubator)
        fail "非法生产依赖 ${pom}: ${source_layer} -> ${dependency_layer} (${dependency_artifact})"
        ;;
      build:*|incubator:*|extensions:*|foundation:foundation|platform:foundation|platform:platform)
        ;;
    esac
  done < <(runtime_dependencies "${pom}")
}

for layer in foundation platform extensions; do
  while IFS= read -r pom; do
    check_runtime_dependencies "${pom}" "${layer}"
  done < <(find "${layer}" -name pom.xml -type f | sort)
done

while IFS= read -r pom; do
  if runtime_dependencies "${pom}" \
    | grep -Eq '^ycr-starter-(business|crud|sdk|ddd($|-))'; then
    fail "Stable 模块不得生产依赖 Incubator: ${pom}"
  fi
done < <(find foundation platform extensions -name pom.xml -type f | sort)

bom_count="$(sed -n '/<dependencyManagement>/,/<\/dependencyManagement>/p' build/ycr-framework-bom/pom.xml \
  | sed -n 's:.*<artifactId>\(ycr-[^<]*\)</artifactId>.*:\1:p' | sort -u | wc -l | tr -d ' ')"
[[ "${bom_count}" -eq 39 ]] || fail "YCR BOM 应管理 39 个 YCR artifact，实际 ${bom_count} 个"

for artifact in ycr-starter-data-core ycr-starter-data-mp ycr-starter-mq-core ycr-starter-mq-rocketmq \
  ycr-starter-ddd-core ycr-starter-ddd-extension ycr-starter-ddd-statemachine; do
  grep -Fq "<artifactId>${artifact}</artifactId>" build/ycr-framework-bom/pom.xml \
    || fail "YCR BOM 缺少聚合子模块: ${artifact}"
done

echo "仓库拓扑、parent relativePath、生产依赖方向与 BOM 完整性契约通过。"

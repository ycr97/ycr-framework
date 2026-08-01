#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

"${repo_root}/scripts/check-test-method-names.sh"

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

contracts_file="scripts/autoconfiguration-side-effect-contracts.tsv"
while IFS='|' read -r class_name property default_case enabled_case; do
  [[ -z "${class_name}" || "${class_name}" == \#* ]] && continue
  simple_name="${class_name##*.}"
  prefix="${property%.*}"
  name="${property##*.}"
  source_file="$(rg --files -g "${simple_name}.java" | rg '/src/main/java/' | head -n 1)"
  test_file="$(rg --files -g "${simple_name}Test.java" | head -n 1)"

  if [[ -z "${source_file}" || -z "${test_file}" ]]; then
    echo "副作用自动配置契约缺少源码或测试: ${class_name}"
    missing=1
    continue
  fi
  if ! rg -q "@ConditionalOnProperty\\(prefix = \"${prefix}\", name = \"${name}\", havingValue = \"true\"\\)" "${source_file}"; then
    echo "副作用能力必须显式开启且不得 matchIfMissing: ${property} (${source_file})"
    missing=1
  fi
  if ! rg -Fq "${default_case}" "${test_file}"; then
    echo "缺少默认关闭语义用例: ${default_case} (${test_file})"
    missing=1
  fi
  if ! rg -Fq "${enabled_case}" "${test_file}"; then
    echo "缺少显式开启语义用例: ${enabled_case} (${test_file})"
    missing=1
  fi
done < "${contracts_file}"

if [[ "${missing}" -ne 0 ]]; then
  exit "${missing}"
fi

auth_dependency_tree="ycr-starter-auth-satoken/target/auth-satoken-compile-dependencies.txt"
mvn -q -pl ycr-starter-auth-satoken dependency:tree \
  -Dscope=compile \
  -DoutputFile="target/auth-satoken-compile-dependencies.txt"
if rg -q 'org\.springframework\.security:' "${auth_dependency_tree}"; then
  echo "auth-satoken 默认依赖路径不得包含 Spring Security："
  rg 'org\.springframework\.security:' "${auth_dependency_tree}"
  exit 1
fi
if rg -q 'cn\.dev33:sa-token-jwt:' "${auth_dependency_tree}"; then
  echo "auth-satoken 不得隐式启用未装配的 JWT 模式"
  exit 1
fi

echo "自动配置静态契约通过，执行全部 AutoConfiguration 行为测试..."
mvn -q -Dtest='*AutoConfigurationTest' -Dsurefire.failIfNoSpecifiedTests=false test

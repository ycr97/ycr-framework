#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

"${repo_root}/scripts/check-version-consistency.sh"
"${repo_root}/scripts/check-test-method-names.sh"
"${repo_root}/scripts/check-module-maturity.sh"

missing=0
while IFS= read -r imports_file; do
  while IFS= read -r class_name; do
    [[ -z "${class_name}" || "${class_name}" == \#* ]] && continue
    simple_name="${class_name##*.}"
    if [[ -z "$(find . -type f -name "${simple_name}Test.java" -print -quit)" ]]; then
      echo "缺少自动配置测试: ${class_name} (${imports_file})"
      missing=1
    fi
  done < "${imports_file}"
done < <(find . -type f -name 'org.springframework.boot.autoconfigure.AutoConfiguration.imports')

contracts_file="scripts/autoconfiguration-side-effect-contracts.tsv"
while IFS='|' read -r class_name property default_case enabled_case; do
  [[ -z "${class_name}" || "${class_name}" == \#* ]] && continue
  simple_name="${class_name##*.}"
  prefix="${property%.*}"
  name="${property##*.}"
  source_file="$(find . -type f -path '*/src/main/java/*' -name "${simple_name}.java" -print -quit)"
  test_file="$(find . -type f -name "${simple_name}Test.java" -print -quit)"

  if [[ -z "${source_file}" || -z "${test_file}" ]]; then
    echo "副作用自动配置契约缺少源码或测试: ${class_name}"
    missing=1
    continue
  fi
  if ! grep -Eq "@ConditionalOnProperty\\(prefix = \"${prefix}\", name = \"${name}\", havingValue = \"true\"\\)" "${source_file}"; then
    echo "副作用能力必须显式开启且不得 matchIfMissing: ${property} (${source_file})"
    missing=1
  fi
  if ! grep -Fq "${default_case}" "${test_file}"; then
    echo "缺少默认关闭语义用例: ${default_case} (${test_file})"
    missing=1
  fi
  if ! grep -Fq "${enabled_case}" "${test_file}"; then
    echo "缺少显式开启语义用例: ${enabled_case} (${test_file})"
    missing=1
  fi
done < "${contracts_file}"

if [[ "${missing}" -ne 0 ]]; then
  exit "${missing}"
fi

auth_dependency_tree="ycr-starter-auth-satoken/target/auth-satoken-compile-dependencies.txt"
mvn -q -pl ycr-starter-auth-satoken -am dependency:tree \
  -Dscope=compile \
  -DoutputFile="target/auth-satoken-compile-dependencies.txt"
if grep -Eq 'org\.springframework\.security:' "${auth_dependency_tree}"; then
  echo "auth-satoken 默认依赖路径不得包含 Spring Security："
  grep -E 'org\.springframework\.security:' "${auth_dependency_tree}"
  exit 1
fi
if grep -Eq 'cn\.dev33:sa-token-jwt:' "${auth_dependency_tree}"; then
  echo "auth-satoken 不得隐式启用未装配的 JWT 模式"
  exit 1
fi

oauth_dependency_tree="ycr-starter-auth-oauth2-resource-server/target/oauth2-resource-server-compile-dependencies.txt"
mvn -q -pl ycr-starter-auth-oauth2-resource-server -am dependency:tree \
  -Dscope=compile \
  -DoutputFile="target/oauth2-resource-server-compile-dependencies.txt"
if ! grep -Eq 'org\.springframework\.security:spring-security-oauth2-resource-server:' "${oauth_dependency_tree}"; then
  echo "oauth2-resource-server 编译依赖必须包含 spring-security-oauth2-resource-server"
  exit 1
fi
if ! grep -Eq 'org\.springframework\.security:spring-security-oauth2-jose:' "${oauth_dependency_tree}"; then
  echo "oauth2-resource-server 编译依赖必须包含 spring-security-oauth2-jose"
  exit 1
fi
if grep -Eq 'org\.springframework\.security:spring-security-oauth2-client:|org\.springframework\.security:spring-security-oauth2-authorization-server:' "${oauth_dependency_tree}"; then
  echo "oauth2-resource-server 不得包含 OAuth2 Client 或 Authorization Server"
  exit 1
fi

echo "自动配置静态契约通过，执行全部 AutoConfiguration 行为测试..."
mvn -q -Dtest='*AutoConfigurationTest' -Dsurefire.failIfNoSpecifiedTests=false test

oauth_metadata="ycr-starter-auth-oauth2-resource-server/target/classes/META-INF/spring-configuration-metadata.json"
if [[ ! -f "${oauth_metadata}" ]] || ! grep -Eq 'ycr\.auth\.oauth2\.resource-server' "${oauth_metadata}"; then
  echo "OAuth2 Resource Server 配置元数据缺失或未包含配置前缀"
  exit 1
fi

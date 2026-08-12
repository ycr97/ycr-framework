#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
revision="$(sed -n 's/^-Drevision=//p' "${repo_root}/.mvn/maven.config")"
smoke_dir="$(mktemp -d)"
trap 'rm -rf "${smoke_dir}"' EXIT

mkdir -p "${smoke_dir}/src/test/java/com/example"
sed "s/@YCR_VERSION@/${revision}/g" \
  "${repo_root}/scripts/external-consumer/pom.xml.template" > "${smoke_dir}/pom.xml"
cp "${repo_root}/scripts/external-consumer/StarterConsumptionTest.java" \
  "${smoke_dir}/src/test/java/com/example/StarterConsumptionTest.java"

mvn -q -f "${smoke_dir}/pom.xml" test

echo "仓库外 BOM 与 Starter 消费契约通过。"

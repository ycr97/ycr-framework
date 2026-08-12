#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

mvn -q org.apache.maven.plugins:maven-enforcer-plugin:3.5.0:enforce \
  -Drules=dependencyConvergence

echo "Maven 依赖收敛契约通过。"

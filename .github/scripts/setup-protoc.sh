#!/usr/bin/env bash
set -euo pipefail

readonly PROTOC_VERSION="${1:-27.3}"

case "$(uname -s)/$(uname -m)" in
  Linux/x86_64)
    asset="protoc-${PROTOC_VERSION}-linux-x86_64.zip"
    ;;
  Linux/aarch64 | Linux/arm64)
    asset="protoc-${PROTOC_VERSION}-linux-aarch_64.zip"
    ;;
  Darwin/x86_64)
    asset="protoc-${PROTOC_VERSION}-osx-x86_64.zip"
    ;;
  Darwin/arm64)
    asset="protoc-${PROTOC_VERSION}-osx-aarch_64.zip"
    ;;
  MINGW64_NT-* | MSYS_NT-*)
    asset="protoc-${PROTOC_VERSION}-win64.zip"
    ;;
  *)
    echo "Unsupported platform: $(uname -s)/$(uname -m)" >&2
    exit 1
    ;;
esac

readonly install_dir="${RUNNER_TEMP:-/tmp}/protoc"
readonly archive_path="${install_dir}/${asset}"
readonly url="https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOC_VERSION}/${asset}"

mkdir -p "${install_dir}"
curl -fsSL "${url}" -o "${archive_path}"
unzip -qo "${archive_path}" -d "${install_dir}"
echo "${install_dir}/bin" >> "${GITHUB_PATH}"
"${install_dir}/bin/protoc" --version

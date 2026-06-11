#!/usr/bin/env bash
set -euo pipefail

plugin_dir="${PLUGIN_DIR:-plugin}"
dist_dir="${DIST_DIR:-dist}"

mkdir -p "${dist_dir}"

platforms=(
  "darwin:arm64:darwin-arm64"
  "darwin:amd64:darwin-amd64"
  "linux:amd64:linux-amd64"
  "windows:amd64:windows-amd64"
)
plugins=(dump java ts)

pushd "${plugin_dir}" >/dev/null

for entry in "${platforms[@]}"; do
  IFS=':' read -r goos goarch platform <<< "${entry}"
  ext=""

  if [ "${goos}" = "windows" ]; then
    ext=".exe"
  fi

  for plugin in "${plugins[@]}"; do
    output="../${dist_dir}/webpb-protoc-${plugin}-${platform}${ext}"
    GOOS="${goos}" GOARCH="${goarch}" CGO_ENABLED=0 \
      go build -trimpath -ldflags="-s -w" \
      -o "${output}" \
      "./cmd/webpb-${plugin}"
  done
done

popd >/dev/null

(
  cd "${dist_dir}"
  shasum -a 256 webpb-protoc-* > SHA256SUMS.txt
)

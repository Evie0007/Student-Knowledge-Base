#!/bin/sh
# Maven Wrapper: runs Maven without installing it. Usage: ./mvnw clean javafx:run
set -e

dir=$(cd "$(dirname "$0")" && pwd)
url=$(sed -n 's/^[[:space:]]*distributionUrl[[:space:]]*=[[:space:]]*//p' "$dir/.mvn/wrapper/maven-wrapper.properties" | tr -d '\r')
name=$(basename "$url" .zip)                      # apache-maven-3.9.11-bin
base="$HOME/.m2/wrapper/dists/$name"
maven_home="$base/${name%-bin}"                   # apache-maven-3.9.11

if [ ! -x "$maven_home/bin/mvn" ]; then
  echo "Downloading Maven from $url ..." >&2
  mkdir -p "$base"
  curl -fsSL -o "$base/$name.zip" "$url"
  expected=$(curl -fsSL "$url.sha512" | awk '{print $1}')
  if command -v sha512sum >/dev/null 2>&1; then
    actual=$(sha512sum "$base/$name.zip" | awk '{print $1}')
  else
    actual=$(shasum -a 512 "$base/$name.zip" | awk '{print $1}')
  fi
  if [ "$actual" != "$expected" ]; then
    rm -f "$base/$name.zip"
    echo "Checksum mismatch for $url" >&2
    exit 1
  fi
  unzip -q -o "$base/$name.zip" -d "$base"
  rm -f "$base/$name.zip"
fi

exec "$maven_home/bin/mvn" "$@"

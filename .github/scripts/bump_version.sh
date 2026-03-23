#!/bin/bash
set -e

GRADLE_FILE="build.gradle.kts"

current_vn=$(grep -E '^[[:space:]]*version[[:space:]]*=[[:space:]]*"' "$GRADLE_FILE" | sed 's/.*"\(.*\)".*/\1/')

IFS='.' read -r major minor patch <<< "$current_vn"

new_vn="${major}.${minor}.$((patch + 1))"

sed -i "s/version = \"$current_vn\"/version = \"$new_vn\"/" "$GRADLE_FILE"

echo "version $current_vn → $new_vn"

[ -n "$GITHUB_ENV" ] && echo "NEW_VN=$new_vn" >> "$GITHUB_ENV"
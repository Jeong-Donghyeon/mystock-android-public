#!/bin/sh
input=$(cat)
[ "$(printf '%s' "$input" | jq -r '.stop_hook_active // false')" = "false" ] || exit 0
root="${CLAUDE_PROJECT_DIR:-.}"
marker="$root/build/verified"

changed=$(cd "$root" && git status --porcelain --untracked-files=all | cut -c4- | sed 's/.* -> //' \
    | grep -E '\.kts?$|/res/|/src/.*\.(xml|json)$' || true)
[ -n "$changed" ] || exit 0

stale=""
if [ ! -f "$marker" ]; then
    stale="$changed"
else
    for file in $changed; do
        [ -e "$root/$file" ] && [ "$root/$file" -nt "$marker" ] && stale="$stale $file"
    done
fi
[ -n "$stale" ] || exit 0

reason="코드를 바꾸고 마무리 검증을 통과하지 않았다. ./gradlew detekt -PdetektFix 뒤에 ./gradlew verifyAll을 돌리고 결과를 그대로 보고한다. 검증이 실패하면 실패한 그대로 보고하고 끝낸다. 검증 안 된 파일:$(printf ' %s' $stale)"
jq -n --arg reason "$reason" '{decision: "block", reason: $reason}'
exit 0

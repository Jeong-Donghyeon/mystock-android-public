#!/bin/sh
input=$(cat)
path=$(printf '%s' "$input" | jq -r '.tool_input.file_path // ""')
content=$(printf '%s' "$input" | jq -r '.tool_input.new_string // .tool_input.content // ""')

ask() {
    jq -n --arg reason "$1" \
        '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "ask", permissionDecisionReason: $reason}}'
    exit 0
}

case "$path" in
    */konsist/src/test/*|*/config/detekt/*|*/build-logic/*|"$CLAUDE_PROJECT_DIR"/build.gradle.kts|*/.claude/hooks/*|*/.claude/settings.json)
        ask "테스트·컨벤션·검사 규칙(${path#"$CLAUDE_PROJECT_DIR"/})을 바꾸는 것은 사용자 확인 뒤에만 한다."
        ;;
esac

if printf '%s' "$content" | grep -qE '@Ignore\b|@Suppress\('; then
    ask "테스트 비활성화나 규칙 억제(@Ignore, @Suppress)는 사용자 확인 뒤에만 한다."
fi

exit 0

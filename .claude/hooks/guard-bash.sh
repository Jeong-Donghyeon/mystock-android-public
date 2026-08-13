#!/bin/sh
input=$(cat)
command=$(printf '%s' "$input" | jq -r '.tool_input.command // ""')

decide() {
    jq -n --arg decision "$1" --arg reason "$2" \
        '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: $decision, permissionDecisionReason: $reason}}'
    exit 0
}

if printf '%s' "$command" | grep -qE 'gradlew?[^|;&]*\brecordRoborazzi'; then
    decide ask "recordRoborazzi는 골든을 덮어쓴다. 사용자 확인 뒤에만 돌린다."
fi

if printf '%s' "$command" | grep -qE '\b(rm|git rm|mv)\b[^|;&]*(Test[A-Za-z]*\.kt|src/test/)'; then
    decide ask "테스트·픽스처 파일을 지우거나 옮기는 것은 사용자 확인 뒤에만 한다."
fi

if printf '%s' "$command" | grep -qE '(>|\btee\b|\bsed\b[^|;&]*-i)' && printf '%s' "$command" | grep -qE '@Ignore\b|@Suppress\('; then
    decide ask "테스트 비활성화나 규칙 억제(@Ignore, @Suppress)를 Bash로 쓰는 것은 사용자 확인 뒤에만 한다."
fi

root=$(printf '%s' "${CLAUDE_PROJECT_DIR:-.}" | sed 's/[.[\*^$/]/\\&/g')
rules="konsist/src/test/|config/detekt/|build-logic/|\.claude/hooks/|\.claude/settings\.json|(^|[[:space:]]|\./|$root/)build\.gradle\.kts"
if printf '%s' "$command" | grep -qE "(\b(rm|git rm|mv|cp|tee)\b|\bsed\b[^|;&]*-i|>)[^|;&]*($rules)"; then
    decide ask "테스트·컨벤션·검사 규칙 파일을 Bash로 지우거나 쓰는 것은 사용자 확인 뒤에만 한다."
fi

exit 0

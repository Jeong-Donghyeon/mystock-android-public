# CLAUDE.md

프로젝트 소개·화면·모듈 구조는 `README.md`를 본다. 이 문서는 작업 규칙만 담는다.

화면·UI의 원천은 Design System 아티팩트 "MyStock Design"이며, 구현 규칙은 `docs/DESIGN.md`, 화면의 코드 구조는 `docs/SCREEN.md`를 따른다. 화면 구현·수정은 `/screen` 스킬을 사용한다. 데이터 층(`:core:*`)의 원천은 서버 계약(`docs/API.md`)이고, 코드 구조는 `docs/CORE.md`를 따르며, 구현·수정은 `/core` 스킬을 사용한다.

## 작업 방식

1. 검증 절의 테스트 작성 규칙대로 테스트를 먼저 쓰고, 실패하는 것을 확인한다.
2. 구현해서 바꾼 모듈의 테스트를 통과시킨다.
3. 코드를 바꾼 작업이면 검증 절대로 마무리 검증을 돌린다.
4. 돌린 명령과 결과를 그대로 보고한다. 돌리지 않은 검증을 통과했다고 쓰지 않는다.

- 요청한 범위만 바꾼다. 무관한 리팩터링, 의존성·버전 인상, 원천에 없는 화면·컴포넌트 추가는 하지 않는다.
- 내 변경과 무관한 기존 실패는 고치지 않고 보고한다.
- 규칙과 문서는 핵심만 최소한으로 적는다. 예시와 상세 설명을 늘리지 않는다.

## 검증

검사는 Gradle 태스크가 한다. 훅은 검사가 아니라 절차를 건너뛰는 것을 막는 게이트다.

### 흐름

| 시점 | 명령 | 조건 |
|---|---|---|
| 작업 중 | `./gradlew :<모듈>:test` | 바꾼 모듈만 |
| 작업 중 | `./gradlew :<모듈>:verifyRoborazziDebug` | 화면·컴포넌트·토큰을 바꿨을 때 |
| 작업 중 | `./gradlew :konsist:test` | 화면·데이터 층의 구조, 모듈 의존, `:design` 컴포넌트나 대응표를 건드렸을 때 |
| 마무리 | `./gradlew detekt -PdetektFix` 뒤 `./gradlew verifyAll` | 코드(`.kt`·`.kts`)·리소스·매니페스트·계약 픽스처를 바꾼 작업에서 1회. 문서만 바꿨으면 돌리지 않는다 |

- `:core:model`처럼 테스트가 없는 모듈을 바꿨으면 그것을 쓰는 모듈의 `test`로 확인한다.
- `verifyAll`은 아래 검사를 전부 돌리고 모두 통과했을 때만 `build/verified`를 남긴다.

```
verifyAll
├─ assembleDebug
├─ test                    JUnit4. Konsist 포함
├─ verifyRoborazziDebug ── checkScreenshotGoldens   mystock.screenshot을 붙인 모듈
├─ lint                    경고도 실패. 버전 갱신 검사와 :app의 MissingFirebaseInstanceTokenRefresh만 끈다
├─ detekt                  ktlint 포맷·Compose 규칙 포함. 모든 모듈·루트·build-logic의 .kts까지
└─ checkDesignTokens       :app·:feature:* (docs/DESIGN.md)
```

### 훅

`.claude/hooks/`의 스크립트가 하고 `.claude/settings.json`이 등록한다. 규칙 파일은 Konsist 테스트·detekt 설정·`build-logic`·루트 빌드 스크립트·훅·`settings.json`이다.

| 시점 | 막는 것 |
|---|---|
| Bash 실행 전 | `recordRoborazzi`, 테스트·픽스처 파일 삭제·이동, 규칙 파일 쓰기·삭제, `@Ignore`·`@Suppress` 쓰기는 확인 |
| 편집 전 | 규칙 파일 편집과 `@Ignore`·`@Suppress`는 확인 |
| 작업 종료 | `verifyAll`을 통과하지 않은 코드 변경이 있으면 종료 차단 |

### 테스트 작성

- 구현보다 먼저 쓴다. 스크린샷 테스트도 샘플 상태와 함께 먼저 쓴다.
- 무엇을 쓰는지는 `docs/DESIGN.md`·`docs/SCREEN.md`·`docs/CORE.md`의 테스트 표(딥링크는 `docs/API.md`, 모듈 의존은 `README.md`)가 정한다. 거기 없는 테스트 클래스는 만들지 않고, 클래스 안의 케이스는 표의 설명 범위 안에서 더한다.

### 스크린샷 골든

- 골든은 프로젝트 루트 `screenshot/`에 있고 gitignore 대상이다.
- record(`recordRoborazziDebug`)는 새로 쓴 테스트는 구현이 끝난 뒤에, 골든을 잃은 화면은 코드를 바꾸기 전에 돌린다.
- verify가 실패하면 `build/outputs/roborazzi/compare`를 본다. 바꾸지 않은 화면이 달라졌으면 코드 문제고, 의도한 변화면 보고한다.

### 실패

- 원인을 고친다. 단언 완화, `@Ignore`, 테스트·Konsist·detekt 규칙 삭제·완화로 통과시키는 것은 사용자 확인 뒤에만 한다.

## 핵심 규칙

- **앱에 매매 로직을 두지 않는다.** 매매 판단·실행·안전망은 전부 서버 책임이다.
- **전략 생성·편집 UI를 만들지 않는다.** 앱은 전략 조회, 매매 시작, 상태 제어만 한다.

## 스타일 · 커밋

- **주석은 코드만으로 알 수 없는 "왜"(값의 근거, 방어하는 상황, 외부 제약)에만 짧게 단다.** 무엇을 하는지 되풀이하는 주석은 달지 않는다.
- 다음은 주석이 아니라 여기에 남긴다.

| 무엇 | 어디에 |
|---|---|
| 동작이 왜 그래야 하는지 | 테스트 이름 |
| 화면·UI 결정 | 원천 시안, `docs/DESIGN.md` |
| 변경의 배경 | 커밋 본문 |

- Conventional Commits(`<type>: <제목>`), 제목·본문은 한국어. 본문은 변경 사항을 `-` 불릿으로 나열한다. 제목과 불릿은 명사형으로 끝낸다. 이 외에는 작성하지 않는다.

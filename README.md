# mystock-android

전략 기반 자동매매 시스템의 안드로이드 클라이언트. 매매 엔진과 API 서버는 앱 밖에 있고, 앱은 계좌 상태를 보여 주고 제어 명령을 보내는 얇은 클라이언트다. 서버 계약은 `docs/API.md`, 매매 상태의 전이 규칙과 화면 규칙은 디자인 원천(Design System 아티팩트 "MyStock Design")에 있다.

## 화면

| 화면 | 내용 |
|---|---|
| 홈 | 계좌 선택, 매매 상태·손익·보유 종목·주문 내역, 상태 제어(일시정지 · 재개 · 회수 · 청산), 주문 체결 알림 토글, 주기 갱신. 대기 계좌면 시작할 전략을 고른다 |
| 운용 성과 | 선택 계좌의 합산 손익·통계, 운용 내역 |
| 전략 목록 | 전략 카드(이름·버전·설명·시장·조건) |
| 전략 상세 | 계좌·버전 범위 칩, 그 범위의 합산 손익·통계, 운용 내역. 대기 홈에서 열면 시작 시트 |
| 버전 내역 | 버전별 운용 요약(횟수 · 평균 수익률)과 변경 내용 |
| 설정 | 알림 스위치, 키 인증, 오픈소스 라이선스, 버전 |

데이터는 서버에서 온다. 증권사 키를 인증하기 전에는 `:core:data`의 Fake가 샘플 데이터로 답한다. 인증·기기 등록·홈 조립 규칙은 `docs/CORE.md`에 있다.

## 구조

| 모듈 | 역할 |
|---|---|
| `:app` | 진입점, 내비게이션(Navigation 3), 딥링크, 스낵바 호스트, 푸시 수신(FCM) |
| `:design` | 디자인 토큰·컴포넌트·아이콘·카탈로그 |
| `:core:model` | 도메인 모델. 순수 Kotlin(JVM) |
| `:core:network` | Retrofit 클라이언트(Hilt), 리소스별 API 인터페이스, DTO, 토큰 저장·Bearer·재발급, 서버 계약 픽스처 |
| `:core:data` | 리포지토리 인터페이스와 Remote·Fake 구현, DTO→도메인 매퍼, Hilt 바인딩 |
| `:feature:common` | `:feature:*`가 공유하는 포맷·문구·운용 집계·도메인→`:design` enum 매핑·샘플 |
| `:feature:account` | 홈·운용 성과 화면 |
| `:feature:settings` | 설정 화면 |
| `:feature:strategy` | 전략 목록·상세·버전 내역 |
| `:konsist` | 모듈 의존 방향, 화면·데이터 층 구조, 매매 상태 enum, 디자인 대응표 컨벤션 테스트 |
| `build-logic` | 공통 빌드 설정 컨벤션 플러그인(`mystock.*`) |

- 의존은 `feature → data → network → model` 한 방향이고, `:feature:*`는 `:design`도 참조한다. `:feature:*`끼리는 `:feature:common`만 참조한다. 화면 간 연결은 `:app`의 내비게이션에서만 한다. — `ArchitectureTest`
- 화면은 Compose + MVI로 만든다.
- 스택: Kotlin, Jetpack Compose(Material 3), Hilt, Retrofit, kotlinx.serialization, Firebase Cloud Messaging. minSdk 28.

## 문서

- [CLAUDE.md](CLAUDE.md) — 작업 방식, 검증 규칙, 코드 스타일
- [docs/DESIGN.md](docs/DESIGN.md) — 디자인 원천을 Compose로 옮기는 규칙과 대응표
- [docs/SCREEN.md](docs/SCREEN.md) — 화면 하나의 코드 구조
- [docs/API.md](docs/API.md) — 서버 계약과 갱신 규칙
- [docs/CORE.md](docs/CORE.md) — 데이터 층의 코드 구조
- [.claude/skills/screen](.claude/skills/screen/SKILL.md) — 화면 작업 절차(`/screen` 스킬)
- [.claude/skills/core](.claude/skills/core/SKILL.md) — 데이터 층 작업 절차(`/core` 스킬)

## 빌드

서버 주소는 gitignore 대상인 `keystore.properties`의 `mystock.baseUrl`에서 읽고, 없으면 기본값으로 빌드된다. 평문 HTTP는 디버그 빌드에서만 열려 있다. Play 스토어 배포 없이 개인 설치용 APK로 쓴다.

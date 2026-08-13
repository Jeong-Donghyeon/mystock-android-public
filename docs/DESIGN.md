# 디자인

**원천은 Design System 아티팩트 "MyStock Design"이다** — https://claude.ai/artifact/YTyEUaP8GNsf3YWw9wMgcw. 파일은 전부 `project/` 아래다.
토큰 값(`tokens.json`)·화면 시안(`Screens`)·설계 원칙·도메인 색 매핑이 거기서 정해진다.
이 문서는 Android(Compose) 구현이 원천을 따르는 방법이다.
코드가 원천과 어긋나면 코드를 고친다. 원천의 값과 규칙을 여기 복사하지 않는다.

**구현한 원천 버전: 0.3.0** — 원천이 버전을 올리면 코드를 맞추고 이 숫자를 갱신한다.

## 원천 ↔ 코드 대응

| 원천 | 코드 |
|---|---|
| `tokens.json` `color` (`dark`·`light`) | `:design` `token/Colors.kt` (`darkColors`/`lightColors`) |
| `tokens.json` `type` 스타일 | `:design` `token/Typography.kt` |
| `tokens.json` `spacing`·`radius`·`icon`·`size` | `:design` `token/Dimens.kt` (`Spacing`/`Radius`/`IconSize`/`stroke`) |
| `[data-theme="light"]` 전환 | `:design` `MyStockTheme.kt` (`isSystemInDarkTheme()`), 프리뷰는 `PreviewThemes` |
| `fonts/PretendardVariable.ttf` (배포본 무수정, SIL OFL 1.1) | `:design` `res/font/pretendard.ttf`, 루트 `licenses/OFL-Pretendard.txt` |
| Screens 컴포넌트 | `:design` `component/*.kt`, 카탈로그는 `catalog/DesignSystemCatalog.kt` |
| Screens 아이콘 | `:design` `icon/MsIcons.kt` (SVG를 path로 옮김) |
| 도메인 → 토큰 매핑 표 | `:design` `model/UiEnums.kt`와 그 값을 받는 컴포넌트 |
| 매매 상태 | `AccountState` |
| 상태 칩 · 키 인증 칩 · 매수/매도 칩 · 분류 칩 | `StatusChip`·`KeyAuthChip`·`OrderSideChip`·`MsChip` |
| 등락 금액 · 퍼센트 | `PnlText`·`PnlPairText` |
| 금액 · 수치 텍스트 | `MsAmountText` |
| 확정 버튼 · 취소 버튼 | `MsPrimaryButton`·`MsSecondaryButton` |
| 스위치 | `MsToggle` |
| 배너 (사유 · 연결 끊김) | `MsBanner` |
| 바텀시트 | `MsBottomSheet` |
| 확인 다이얼로그 | `MsDialog` |
| 명령 오버레이 | `MsCommandOverlay` |
| 스낵바 | `MsSnackbarHost`·`MsSnackbar`·`SnackbarController` |
| 스켈레톤 블록 | `MsSkeleton`·`MsSkeletonText`·`MsSkeletonChip`·`MsQuickActionSkeleton` |
| 카드 | `MsCard` |
| 상태 카드 퀵 액션 | `MsQuickAction` |
| 보유 종목·주문 내역 목록 | `MsListCard` + `MsHoldingRow`·`MsOrderRow`·`MsListRow`·`MsListPnlRow` |
| 전략 카드 | `MsStrategyCard` (조건은 `MsCondition`) |
| 섹션 라벨 · 시트 안내문 | `MsSectionLabel`·`MsInfoNote` |
| 오류 화면 | `MsFailedContent` (`MsEmptyMessage`에 문구를 고정) |
| 운용 없음 카드 | `MsEmptyCard` |
| 시트 취소·확정 버튼 | `MsSheetActions` |
| 키 입력란 (라벨 + 필드) | `MsTextField` |
| 설정 행 (라벨 + 스위치·셰브런·값) | `MsSettingRow` |
| 뒤로 상단바 (제목 · 부제) | `MsBackTopBar` (부제가 늦게 오면 `reserveSubtitle`로 자리를 비워 둔다) |
| 계좌 상단바 (계좌 선택기 · 아이콘 액션) | `MsAccountTopBar` · `MsTopBarAction` (계좌명이 없으면 높이만 확보한다) |
| 통계 셀 (라벨 + 값) | `MsStat` |
| 합산 카드 (손익 + 통계 6칸) | `MsSummaryCard` |
| 운용 내역 행 | `MsRunRow` |
| 범위 칩 (계좌 드롭다운 · 버전) | `MsDropdownChip` · `MsChip(onClick)` |
| 전략 카드 상세 설명 | `MsStrategyCard(note)` |
| Screens 화면 | `:feature:*` |
| `--background` (첫 프레임) | `:app` `res/values{,-night}/colors.xml` |

## 코드 규칙

원천의 설계 원칙과 도메인 매핑을 Compose에서 이렇게 지킨다.

- 하드코딩 금지 → 화면은 `MyStockTheme` 토큰과 `:design` 컴포넌트만 쓴다. `checkDesignTokens`가 `:app`·`:feature:*`의 `Color(`·`sp`·`dp`를 잡고, 스펙 상수가 있는 `:design`은 검사하지 않는다
- 도메인 매핑과 비색상 신호 → `:design` 컴포넌트가 강제한다. 화면은 `success`·`priceUp` 같은 토큰을 직접 집지 않고, 도메인 enum도 `:design`의 것을 쓴다
- 옅은 배경 15% → `color.copy(alpha = 0.15f)`
- 그림자 없음 → `Surface`·`Card`는 elevation 0, 카드 분리는 `stroke` 두께의 `border`
- 시안에 있는 컴포넌트가 `:design`에 없으면 화면 안에 만들지 말고 `:design`에 추가한 뒤 위 대응표를 갱신한다. — `DesignConventionTest`
- 폰트는 배포본 그대로 쓴다. 서브셋 금지 (OFL 예약 이름)

## 테스트

컴포넌트의 모습은 카탈로그 스크린샷이 지킨다. 모달·호스트·전체 화면 컴포넌트(`MsBottomSheet`·`MsDialog`·`MsCommandOverlay`·`MsSnackbarHost`·`MsFailedContent`)는 정적 페이지에 못 넣어 화면 골든이 지킨다. 코드가 강제하는 표기·동작이 있을 때만 단위 테스트를 더한다.

| 테스트 | 무엇을 | 어디서 |
|---|---|---|
| `DesignSystemCatalogScreenshotTest` | 카탈로그 페이지마다 다크·라이트 한 장 | `:design` |
| `PnlFormatTest` | 등락 기호·부호·자릿수 표기 | `:design` |
| `SnackbarControllerTest` | 마지막 메시지로 덮어쓰기, 내려가면 다음 메시지가 이어받기 | `:design` |
| `MsBottomSheetTest` | 긴 내용도 끝까지 펼침 | `:design` |
| `MsCommandOverlayTest` | 별도 윈도우로 뜨고 뒤로가기로 닫히지 않음 | `:design` |
| `DesignConventionTest` | `:design` 공개 컴포넌트와 대응표가 서로 빠짐없다 | `:konsist` |

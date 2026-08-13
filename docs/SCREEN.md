# 화면 구조

화면·UI의 원천은 Design System 아티팩트 "MyStock Design"이고, 시안을 Compose로 옮기는 규칙은 [`docs/DESIGN.md`](DESIGN.md)가, 화면이 쓰는 데이터는 [`docs/CORE.md`](CORE.md)가 정한다. 이 문서는 그 화면을 담는 코드의 구조다.

**이 문서가 결정이다.** 바꾸려면 여기를 먼저 고치고 코드를 맞춘다.

## 파일

화면 하나는 패키지 하나(`<screen>`)다.

| 파일 | 내용 |
|---|---|
| `<Screen>Contract.kt` | `UiState`·`UiIntent`·`UiEffect`, 화면 전용 타입, 샘플 상태 |
| `<Screen>ViewModel.kt` | `ViewModel`, `Msg`, `reduce` |
| `<Screen>Screen.kt` | 상태 없는 화면 컴포저블과 프리뷰 |
| `<Screen>Route.kt` | ViewModel·이펙트·내비게이션 콜백 연결 |
| `navigation/<Feature>Keys.kt` · `navigation/<Feature>Entries.kt` | 모듈의 `NavKey`들과 `entry` 등록. 키가 하나면 `<Feature>Key.kt` |
| `src/test/…/TestRepositories.kt` | 모듈이 공유하는 `Test<X>Repository`와 `test…()` 픽스처 |

프리뷰는 시안이 보여주는 상태마다 하나씩, `sample<Case>State`를 받아 `MyStockTheme`으로 감싸고 `@PreviewThemes`로 단다.

시트·스켈레톤처럼 큰 조각은 같은 패키지에 파일을 나누고 같은 `onIntent`를 받는다. 조각 전용 표시 타입은 그 파일에 둔다. 모듈 안 여러 화면이 같이 쓰는 조각·샘플은 모듈 루트 패키지에 둔다. 포맷·도메인→`:design` enum 매핑처럼 모듈을 넘어 공유하는 순수 함수와 샘플은 `:feature:common`에 둔다.

## 타입

| 타입 | 형태 | 역할 |
|---|---|---|
| `<Screen>UiState` | `data class` | 화면 전체. 파생값은 계산 프로퍼티 |
| `<Screen>UiIntent` | `sealed interface` | 사용자 행동. 이름은 행동(`Refresh`·`Open…`·`Confirm…`·`Dismiss…`) |
| `<Screen>UiEffect` | `sealed interface` | 일회성 결과. `Navigate(target)`·`ShowMessage(@StringRes id)` 등 |
| `<Screen>NavTarget` | `sealed interface` | 이 화면에서 갈 수 있는 곳 |
| `<Screen>Content` | `sealed interface` | 로딩·실패·준비처럼 배타적인 화면 상태. `Boolean` 여러 개로 흉내 내지 않는다 |
| `<Screen>Sheet` | `sealed interface` | 한 번에 하나만 열리는 시트. `UiState.sheet: <Screen>Sheet?` |
| `<Screen>Msg` | `private sealed interface` | 상태를 바꾸는 사건. ViewModel 파일 밖으로 나가지 않는다 |
| `sample<Case>State` | `internal val` | Contract의 샘플 상태. 프리뷰·스크린샷이 공유 |

## 규칙

끝에 적힌 것이 어기면 잡는 검사다.

- `onIntent`는 Intent마다 `dispatch(Msg)`, 비동기 작업 시작, `emit(Effect)`만 한다. 현재 상태에서 뜻이 없는 Intent를 무시하는 가드와 결과에 따라 Effect를 고르는 분기는 여기 둔다.
- 한 갈래의 Intent가 많아지면 `UiIntent`도 하위 `sealed interface`로 묶고 `onIntent`는 그 갈래를 한 분기로 받아 넘긴다. 겹침(시트·다이얼로그)이 그렇다.
- 상태는 `dispatch` 안에서 `reduce`로만 바꾼다. `reduce`는 파일 최상위 순수 함수다. Msg가 많아지면 `Msg`를 하위 `sealed interface`로 묶고 `reduce`도 그만큼 나눈다. — `ScreenConventionTest`
- 리포지토리 관찰은 `uiState`에 구독자가 있는 동안만 한다.
- ViewModel은 리포지토리의 `Result`를 풀 뿐 예외를 잡지 않는다. 실패가 화면을 바꾸면 Msg, 안내만 하면 `ShowMessage`.
- 화면이 쓰는 파생값·순수 함수는 `UiState`의 계산 프로퍼티나 Contract에 둔다. ViewModel 파일에는 `ViewModel`·`Msg`·`reduce`만 있다. — `ScreenConventionTest`
- 내비게이션은 `UiIntent.Navigate(target)` → `UiEffect.Navigate(target)` → Route의 `on…` 콜백 → `:app`. feature는 백스택을 모른다. — `ArchitectureTest`
- 화면을 떠났다 돌아와도 살아야 하는 값은 `SavedStateHandle`에 둔다.
- 텍스트 입력은 `TextFieldState`를 `UiState`에 두고 화면이 직접 편집한다. UI 소유 객체라 `reduce` 대상이 아니며, ViewModel은 제출 Intent에서 값을 읽고 성공 시에만 비운다.
- 화면 문구는 모듈의 `strings.xml`에 두고 `stringResource`로 읽는다. 리소스는 `:app`에서 이름으로 병합되므로 화면 이름을 접두사로 붙이고(`home_`·`settings_`), 모듈을 넘어 공유하는 문구만 `:feature:common`에 `common_` 접두사로 둔다. — `ScreenConventionTest`
- 컴포저블 밖에서 고른 문구는 `@StringRes` id로 넘긴다. ViewModel은 `ShowMessage(id)`를 보내고 Route가 `getString`으로 푼다.

## 시그니처

```kotlin
@HiltViewModel
class <Screen>ViewModel @Inject constructor(…) : ViewModel() {
    val uiState: StateFlow<<Screen>UiState>
    val effects: Flow<<Screen>UiEffect>
    fun onIntent(intent: <Screen>UiIntent)
}

@Composable
fun <Screen>Screen(state: <Screen>UiState, onIntent: (<Screen>UiIntent) -> Unit, modifier: Modifier = Modifier)

@Composable
fun <Screen>Route(modifier: Modifier = Modifier, on…: (…) -> Unit = {}, viewModel: <Screen>ViewModel = hiltViewModel())
```

인자가 있는 화면은 `Route`가 첫 파라미터로 `<Screen>Key`(`NavKey`)를 받고, ViewModel은 `@AssistedInject`로 그 키를 받는다. `viewModel` 기본값은 `hiltViewModel<VM, VM.Factory>(creationCallback = { it.create(key) })`다.

`effects`는 `Channel`을 `receiveAsFlow`로 노출하고 Route가 `repeatOnLifecycle(STARTED)` 안에서 수집한다. 스낵바는 `LocalSnackbarHostState`를 `SnackbarController`로 감싸 `show`한다 — `showSnackbar`를 직접 부르면 메시지가 쌓인다.

## 테스트

| 테스트 | 무엇을 | 어디서 |
|---|---|---|
| `<Screen>ViewModelTest` | 초기 로드·실패와 Intent별 상태 전이 | `:feature:*` |
| `<Screen>ScreenshotTest` | 샘플 상태마다 한 장. 대표 상태는 최대 글자 배율로 한 장 더 | `:feature:*` |
| `<File>Test` | 포맷·계산 순수 함수 | `:feature:common` |
| `ScreenConventionTest` | 이 문서의 규칙 | `:konsist` |

# 데이터 층 구조

데이터 층은 서버 응답을 도메인으로 옮기고 이 기기의 로컬 상태를 드는 일만 한다.
서버 계약은 [`docs/API.md`](API.md)가, 화면이 데이터를 어떻게 쓰고 실패를 어떻게 보이는지는 [`docs/SCREEN.md`](SCREEN.md)가 정한다.

**이 문서가 결정이다.** 바꾸려면 여기를 먼저 고치고 코드를 맞춘다.

## 파일

| 모듈 | 파일 | 내용 |
|---|---|---|
| `:core:model` | `<Domain>.kt` | 도메인 모델·enum과 `ApiException` |
| `:core:network` | `<Resource>Api.kt` | Retrofit 인터페이스. 빈 응답을 푸는 확장 함수도 같은 파일에 |
| | `dto/<Resource>Dto.kt` | 리소스 하나가 주고받는 `@Serializable data class`들 |
| | `ApiJson.kt` | `ignoreUnknownKeys` |
| | `ApiErrorCallAdapterFactory.kt` | 2xx가 아닌 응답을 오류 본문의 `code`·`message`를 담은 `ApiException`으로 |
| | `auth/` | `AuthTokenStore`·`PreferencesTokenStore`·`AuthInterceptor`·`TokenAuthenticator`·`HttpLogRedaction`. 토큰은 이 기기의 것이라 백업에서 뺀다 |
| | `di/NetworkModule.kt` | OkHttp·Retrofit·`<Resource>Api` 제공. `AuthApi`(키·재발급)는 인증기 없는 별도 클라이언트를 쓰고, 로그아웃은 `SessionApi`로 인증 클라이언트를 탄다 |
| | `src/test/resources/contract/*.json` | 서버 계약 픽스처 복사본 |
| `:core:data` | `<X>Repository.kt` | 리포지토리 인터페이스. 화면이 쓰는 유일한 데이터 API. 폴링 주기 상수도 여기 |
| | `remote/Remote<X>Repository.kt` | 서버 구현 |
| | `fake/Fake<X>Repository.kt` | 미인증일 때 답하는 구현. `FakeBackend`에 위임한다 |
| | `fake/Fake*.kt` | 가짜 백엔드. `FakeBackend`가 `Mutex`로 감싸 소유한다 |
| | `mapper/<Resource>Mappers.kt` | `internal fun <X>Dto.toDomain(): <X>`. DTO→도메인 변환은 여기에만 |
| | `Served.kt` | `AuthTokenStore.serve(fake, remote)`, 관찰은 `serveFlow` |
| | `ApiResult.kt` | `apiResult { }` |
| | `HomeData.kt` | 홈이 병렬로 받는 리소스의 조립. 화면이 아니라 여기서 하고, 일부만 실패하면 받은 것으로 채워 `partial`로 알린다 |
| | `DeviceSettingsStore.kt` | 이 기기의 알림 스위치와 푸시 토큰. 백업에서 뺀다 |
| | `di/DataModule.kt` · `di/TimeModule.kt` · `di/ScopeModule.kt` | 인터페이스에 구현을 묶고 `Clock`과 `@ApplicationScope` `CoroutineScope`를 제공한다 |
| | `src/test/…/mapper/TestDtos.kt` | 매퍼 테스트가 공유하는 `test<X>Dto()` 픽스처 |

## 규칙

끝에 적힌 것이 어기면 잡는 검사다.

- `:core:model`은 직렬화·네트워크·Android를 모르고, `:core:data`는 Retrofit·OkHttp·직렬화를 직접 다루지 않는다. — `CoreConventionTest`
- DTO는 계약 그대로다. 기본값이 없어 계약에 있는 필드가 빠지면 디코딩이 실패한다 — null이 올 수 있는 필드도 키는 와야 한다. — `CoreConventionTest`·`ContractTest`
- `@Serializable`은 `dto` 패키지에만 붙는다. 도메인 모델과 로컬 상태는 직렬화 대상이 아니다. — `CoreConventionTest`
- API 함수는 전부 `suspend`다. 본문이 있으면 DTO를 돌려주고, 없으면 반환형이 없다. 있다 없다 하는 본문(204)은 `Response<Dto>`로 받아 같은 파일의 확장 함수가 nullable로 푼다. — `CoreConventionTest`
- 문자열 enum(`state`·`market`·`side` 등)은 DTO에서 `String`으로 받고 매퍼가 바꾼다. 모르는 값이면 실패다. — `<Resource>MappersTest`
- 리포지토리는 예외를 던지지 않는다. `suspend` 함수는 `Result<T>`, 폴링 관찰은 `Flow<Result<T>>`, 로컬 상태는 `StateFlow<T>`. — `CoreConventionTest`
- 폴링 주기는 데이터 층이 정한다. Fake와 서버가 같은 주기로 돌아야 인증 전후로 화면이 같게 움직인다.
- 실패는 셋이다. 서버가 거절하면 `ApiException(code, message)`, 통신이 안 되면 `IOException`, 계약이 어긋나면 `SerializationException`(디코딩)이나 매퍼의 `IllegalArgumentException`·`DateTimeParseException`(값 변환). — `ApiErrorCallAdapterTest`·`<Resource>MappersTest`
- 리포지토리 구현은 `internal` `@Singleton`이고 이름은 `Fake`·`Remote` 접두사다. 그 밖의 구현 클래스(`FakeBackend`·`DeviceSettingsStore` 등)는 이 접두사 규칙 밖이다. — `CoreConventionTest`
- 구현은 패키지로 가른다 — 서버는 `remote`, Fake는 `fake`, DTO→도메인 매퍼는 `mapper`. 계약과 어느 구현에도 매이지 않는 도구는 루트에 둔다. — `CoreConventionTest`
- Hilt 모듈은 `di` 패키지의 `internal` 선언이다. — `CoreConventionTest`
- 키를 인증해야 실제 계좌가 연결된다(원천). 미인증이면 `Remote<X>Repository`가 `serve`로 같은 계약의 `Fake<X>Repository`에 위임한다. 설정·기기 등록은 Fake가 없다. — `ServedTest`
- Fake는 미인증 화면에 보일 샘플을 만드는 것이 전부다. 명령은 서버가 할 상태 전이 검증까지 한다 — 미인증에서도 화면의 전이를 그대로 시험할 수 있어야 해서다. 매매 판단은 하지 않는다 — 주문은 화면에 보일 샘플을 만드는 무작위 생성이고 시세·조건을 보지 않는다.
- 인증은 `:core:network`가 맡는다. 토큰이 있으면 인증된 것이고, 서버가 재발급까지 거절하면 토큰이 지워져 `Settings.keyAuthenticated`가 false가 된다. 키 이름은 `/auth/keys`가 정하고 설정 화면 입력란 순서로 붙인다. — `TokenAuthenticatorTest` (토큰 수명만)
- 디버그 HTTP 로그는 본문까지 찍되 시크릿 필드(`keys`·`accessToken`·`refreshToken`)의 값은 가린다. `keys`는 키 이름이 바뀔 수 있어 객체째 가린다. — `HttpLogRedactionTest`
- 기기 등록(`POST /devices`)은 푸시 토큰과 알림 스위치를 함께 보낸다. 알림 스위치는 `DeviceSettingsStore`에 두고 미인증이면 로컬만 바꾼다. 인증 전에 받은 푸시 토큰은 두었다가 키 인증이 끝나면 보내고, 인증된 뒤 스위치를 바꾸면 다시 보낸다. 인증 해제는 등록 해제 뒤 로그아웃이다.

## 테스트

| 테스트 | 무엇을 | 어디서 |
|---|---|---|
| `ContractTest` | 계약 픽스처마다 DTO로 디코딩되고, 키가 빠지면 실패하며, `nullable.json`의 경로만 null을 받는다. 픽스처는 빠짐없이 등록된다 | `:core:network` |
| `ApiErrorCallAdapterTest` | 거절 응답이 `ApiException`이 되고(계약 밖 본문은 HTTP 코드로), 204는 null이 된다 | `:core:network` |
| `TokenAuthenticatorTest` | 401 → refresh → 1회 재시도, 재발급 거절이면 미인증, 그 밖의 실패(통신·429)는 토큰 유지 | `:core:network` |
| `HttpLogRedactionTest` | 로그 줄에서 시크릿 필드 값만 가리고 나머지는 그대로 둔다 | `:core:network` |
| `<Resource>MappersTest` | DTO→도메인 변환 | `:core:data` |
| `ApiResultTest` | 예외→`Result`, 취소는 전파 | `:core:data` |
| `ServedTest` | 미인증이면 Fake, 인증이면 서버, 관찰 중 인증이 바뀌면 갈아탄다 | `:core:data` |
| `HomeAssemblyTest` | 홈 조립의 부분 실패 규칙 | `:core:data` |
| `ModelConventionTest` | `AccountState`가 원천이 정한 매매 상태 다섯과 같다 | `:konsist` |
| `CoreConventionTest` | 이 문서의 규칙 | `:konsist` |

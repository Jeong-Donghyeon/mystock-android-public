# 서버 계약

코드가 계약과 어긋나면 코드를 고친다.

HTTP API는 [`openapi.yaml`](openapi.yaml)이 정의한다. 이 파일과 응답 픽스처는 서버가 정한 계약의 복사본이라 여기서 고치지 않는다 — 계약이 바뀌면 서버에서 고치고 복사해 온다.

## 응답 픽스처

`core/network/src/test/resources/contract/*.json`은 응답의 실제 형태이자, DTO가 계약보다 느슨하지 않은지 검증하는 입력이다.

- 픽스처에서 값이 있는 필드는 항상 값이고, null인 필드는 항상 null이다.
- 응답이 갈리면 픽스처를 나눈다 — 일시정지 계좌는 `account-paused.json`, 대기 계좌는 `account-waiting.json`, 소수점 수량은 `*-fractional.json`이다.
- 한 상태 안에서도 비어 있을 수 있는 필드만 `nullable.json`에 픽스처별 경로로 적는다(배열 원소는 `[*]`). 거기 없는 필드는 그 상태에서 항상 값이 온다.

## 푸시

서버는 `notification`의 `title`·`body`와 `data.link`를 보낸다. `notification`이 없으면 `data.title`·`data.body`를 대신 읽고, `data.link`는 탭했을 때 열 딥링크다. 탭으로 앱이 열리면 그 값이 인텐트 extras `link`로 오고, 아래 딥링크 표와 같은 규칙으로 목적지를 정한다.

보내는 조건은 `openapi.yaml`의 `/accounts/{id}/order-alert`와 `/devices`에 있다.

## 딥링크

앱이 정하고 서버가 따른다 — 이 절만 계약의 원천이 앱이다. 링크를 더하거나 바꾸면 여기를 먼저 고치고 서버에 알린다.

스킴은 `mystock`이고 host가 목적지다.

| 링크 | 목적지 |
|---|---|
| `mystock://accounts` | 홈 |

서버가 보낸 링크가 이 표에 없으면 스킴이 다르든 host가 다르든 홈으로 연다 — 앱이 모르는 링크를 서버가 보낼 수 있고, 그때 알림이 아무 데도 닿지 않으면 안 된다.

링크가 아예 없는 인텐트는 딥링크가 아니다. 런처 아이콘으로 돌아온 것이므로 보던 화면을 그대로 둔다.

이 절은 `:app`의 `MyStockDeepLinksTest`가 지킨다.

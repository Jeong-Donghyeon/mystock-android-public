---
name: screen
description: 디자인 시스템 원천(Design System 아티팩트 "MyStock Design")의 화면 시안을 Compose + MVI로 구현한다. 화면을 새로 만들거나 기존 화면을 시안에 맞춰 고칠 때 사용.
argument-hint: <화면 이름 (예: 홈, 전략 상세)>
---

**화면의 원천은 디자인 시스템뿐이다.** 레이아웃·구성·표현은 시안을 따르고, 다른 데서 짐작하지 않는다.

## 원천 확인

- `docs/DESIGN.md`에서 구현 대상 원천 버전을 확인한다.
- 시안은 Artifact 도구로 읽는다. 원천 URL은 `docs/DESIGN.md`에 있다. `project/README.md`(브랜드 북·화면 규칙)를 먼저 읽고, `project/components/Screens/preview.html`(화면)과 같은 폴더의 `README.md`(화면 결정)를 본다. 시안에 접근할 수 없으면 사용자에게 시안(스크린샷·설명)을 요청하고, 임의로 짐작해서 그리지 않는다.

## 순서

기존 화면을 고치는 작업이면 이 절을 건너뛰고, 시안과 달라진 부분에 해당하는 파일만 고친다.

모듈은 `README.md` 구조 표를 따른다. 새 화면이면 표를 먼저 갱신한다.

순서는 Contract(샘플 상태 포함) → 테스트 둘 → ViewModel·Screen·Route다. 스크린샷 테스트를 쓰기 전에 모듈에 `mystock.screenshot` 플러그인이 없으면 붙인다.

## 확인

완성된 프리뷰를 시안과 비교한다.

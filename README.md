# Problem Details

`Problem`은 API 발생하는 모든 종류의 오류(4xx, 5xx)를 설명하는 표준화(RFC 7807, 이후 RFC 9457로 업데이트됨)된 형식입니다.(정상 응답에서도 사용할
수 있지만, 오류 응답에 적합)   
이 표준은 HTTP API에서 오류 상황을 일관되고 구조화된 방식으로 클라이언트에게 전달하기 위한 규격을 제공합니다.
`Problem`은 JSON 또는 XML로 표현될 수 있지만, JSON에 대해서만 다룰 것입니다.

## 등장 배경

### 표준화 이전 문제점

- **일관성 문제:** 매번 바뀌는 오류 형식
    - **일관성 부재**: 각 API마다 다른 오류 형식을 사용하면 클라이언트 개발자가 각 API에 맞춰 다른 오류 처리 로직을 구현해야 함
    - **문서화 어려움:** 일관된 형식이 없어 API 문서화 과정이 복잡해지고, 개발자들이 오류 처리 방법을 이해하기 어려움
- **정보 과잉, 부족 문제**
    - **불충분한 정보:** 많은 오류 응답이 HTTP 상태 코드와 간단한 메시지만 제공하여, 문제 해결에 필요한 충분한 정보를 제공하지 못함
    - **보안 취약점:** 표준화되지 않은 오류 메시지는 때로 내부 시스템 정보나 스택 트레이스 같은 민감한 정보를 노출할 위험이 있음

`Problem Details 표준`은 이러한 문제를 해결하기 위해 일관되고 예측 가능한 오류 응답 형식을 제공합니다.

### Problem Details은 표준일 뿐

`Problem Details`은 표준이지만 모든 상황에 강제되는 것은 아닙니다

- 이미 잘 설계된 오류 응답 시스템이 있다면 반드시 변경할 필요는 없습니다.
    - 프로젝트의 특정 요구사항에 더 잘 맞는 커스텀 오류 형식이 있을 수 있습니다.
- `Problem Details`은 확장 가능하므로, 기존 오류 형식의 장점을 유지하면서 표준을 따르도록 조정할 수도 있습니다.
    - `Problem Details`로 시작하여 프로젝트에 맞게 확장, 개선해 나갈 수 있습니다.

## Problem JSON HTTP 응답 예시

```http
HTTP/1.1 403 Forbidden
Content-Type: application/problem+json
Content-Language: en
{
  "type": "https://example.com/probs/out-of-credit",
  "title": "You do not have enough credit.",
  "status": 403,
  "detail": "Your current balance is 30, but that costs 50.",
  "instance": "/account/12345/msgs/abc",
  "balance": 30,
  "accounts": ["/account/12345", "/account/67890"],
  "timestamp": "2023-04-05T12:34:56.789Z"
}
```

- HTTP 상태 코드는 403이며, 이는 JSON 본문의 `status` 필드와 일치합니다.
- `Content-Type` 헤더는 `application/problem+json`으로 설정되어 있어, 이것이 Problem JSON 응답임을 나타냅니다.
- 기본 필드(type, title, status, detail, instance)가 문제를 설명합니다.
- 추가 필드(balance, accounts, timestamp)는 문제에 대한 더 구체적인 정보를 제공합니다.

## Problem Details 구조

### 기본 필드

| 필드         | 타입  | 설명                      | 필수 여부 |
|:-----------|:----|:------------------------|:------|
| `type`     | URI | 문제 유형을 식별하는 URI 참조      | 권장    |
| `title`    | 문자열 | 문제 유형을 식별하는 URI 참조      | 권장    |
| `status`   | 숫자  | HTTP 상태 코드              | 권장    |
| `detail`   | 문자열 | 이 특정 문제 발생에 대한 더 자세한 설명 | 선택    |
| `instance` | URI | 특정 문제 발생을 식별하는 URI 참조   | 선택    |

### 각 필드 상세 설명 및 활용 방안

**`type` 필드**

- **설명:** 문제 유형을 식별하는 URI 참조입니다.
- **형식:** 절대 URI 형태(예: https://example.com/problems/out-of-credit)를 권장
- **목적:**
    - 클라이언트가 이 유형의 오류를 프로그래밍 방식으로 식별하고 처리할 수 있게 합니다.
    - 문제 유형에 대한 문서나 상세 정보를 제공하는 페이지로 연결될 수 있습니다.

- **주의점:**
    - URI는 반드시 실제로 접근 가능한 URL일 필요는 없습니다. 고유 식별자 역할만 해도 됩니다. 그러나 가능하면 실제 문서 페이지를 가리키는 것이 좋습니다.
    - 타입 URI는 계층적으로 구성하여 오류 유형 간의 관계를 표현할 수 있습니다.

- **활용 방안:**
    - 클라이언트 코드에서는 이 URI를 기준으로 오류 유형별 처리 로직을 구현할 수 있습니다.
        - 예: `if (problem.type === "https://example.com/probs/validation/required-field")`
    - 클라이언트가 처리하지 못하는 새로운 오류 유형이 나타나면, 해당 URI의 문서를 통해 대처 방법을 찾을 수 있습니다.

**`title` 필드**

- **설명:** 사람이 읽을 수 있는 짧은 문제 요약입니다.
- **형식:** 간결한 문장으로, 일반적으로 한 문장 이내로 구성합니다.
- **목적:**
    - 사용자나 개발자에게 문제가 무엇인지 빠르게 알려줍니다.
    - 로그 메시지나 오류 보고서에 포함될 수 있는 간결한 설명을 제공합니다.

- **주의점:**
    - 특정 인스턴스에 대한 세부 정보를 포함하지 않습니다.(`detail` 필드의 역할)
    - 고정된 문자열이어야 하며, 동적으로 생성된 정보는 포함하지 않습니다.
    - 기술적인 용어보다는 일반 사용자도 이해할 수 있는 표현을 사용합니다.

- **활용 방안:**
    - UI에 표시할 오류 메시지의 제목으로 활용할 수 있습니다.
    - 로깅 시스템에서 오류를 분류하는 데 사용할 수 있습니다.(읽기 쉬운 형태이므로)

**`status` 필드**

- **설명:** HTTP 상태 코드를 나타내는 숫자입니다.
- **형식:** 3자리 HTTP 상태 코드(예: 400, 404, 500 등)
- **목적:**
    - JSON 필드로만 상태를 확인할 수 있습니다.(처리 용이)

- **주의점:**
    - HTTP 헤더의 상태 코드와 반드시 일치해야 합니다. 불일치는 혼란을 야기합니다.
    - `HTTP status`와 중복됩니다. 데이터의 길이가 중요한 곳에서는 사용하지 말아야 합니다.

- **활용 방안:**
    - 클라이언트는 상태 코드에 따라 기본적인 오류 처리 전략을 결정할 수 있습니다.
        - 예: `if (problem.status >= 500) { /* 서버 오류 처리 */ } else { /* 클라이언트 오류 처리 */ }`

**`detail` 필드**

- **설명:** 특정 문제 발생에 대한 더 자세한 설명입니다.
- **형식:** 사람이 읽을 수 있는 문장 형태의 설명으로, 여러 문장으로 구성될 수 있습니다.
- **목적:**
    - `title`보다 더 구체적인 정보를 제공합니다.
    - 현재 발생한 문제 인스턴스에 대한 특정 정보를 포함할 수 있습니다.

- **주의점:**
    - 민감한 정보(내부 시스템 세부 정보, 스택 트레이스 등)는 포함하지 않아야 합니다.
    - 사용자가 취할 수 있는 조치에 대한 정보를 포함하면 유용합니다.
    - 너무 기술적인 설명은 일반 사용자에게 도움이 되지 않을 수 있습니다.

- **활용 방안:**
    - UI의 오류 메시지 본문으로 사용할 수 있습니다.
    - 로그 및 모니터링 시스템에서 더 상세한 오류 정보로 활용할 수 있습니다.

**`instance` 필드**

- **설명:** 특정 문제 발생을 식별하는 URI 참조입니다.(문제 발생 로깅 주소, 요청 URI, 문제 식별 URI 등)
- **형식:** 일반적으로 요청 URI 또는 고유 식별자를 포함하는 URI 경로입니다.
- **목적:**
    - 발생한 특정 오류 인스턴스를 고유하게 식별합니다.
    - 로그나 모니터링 시스템에서 이 특정 오류를 추적하는 데 사용할 수 있습니다.

- **주의점:**
    - 요청 URI를 사용하는 경우, 쿼리 파라미터에 민감한 정보가 포함되지 않도록 주의해야 합니다.
    - 내부 추적 ID나 로그 참조를 포함하면 디버깅에 유용할 수 있습니다.

- **활용 방안:**
    - 사용자 지원 시 특정 오류 인스턴스를 참조하는 식별자로 활용할 수 있습니다.
    - 로그 시스템이나 모니터링 도구에서 이 인스턴스를 검색하는 키로 사용할 수 있습니다.

**확장 필드**
기본 필드 외에 문제 유형에 맞는 추가 필드를 정의할 수 있도록 허용합니다.

- **특정 도메인 정보:** 비즈니스 규칙이나 응용 프로그램별 제약 조건에 관한 정보
    - 예: `"accountStatus": "suspended"`, `"remainingAttempts": 2`
- **디버깅 정보:** 개발자가 문제를 진단하는 데 도움이 되는 기술적 세부 정보
    - 예: `"requestId": "a7c21f"`, `"logReference": "error-2023-04-05-123"`
- **문제 해결 지침:** 사용자가 문제를 해결하기 위해 취할 수 있는 조치
    - 예: `"suggestedAction": "Please verify your account before continuing"`
- **메타데이터:** 타임스탬프, 버전 정보, 환경 등
    - 예: `"timestamp": "2023-04-05T12:34:56.789Z"`, `"apiVersion": "v2"`

- **주의점:**
    - 확장 필드는 프로젝트 내에서 일관되게 사용해야 합니다.
    - 필드 이름은 직관적이고 자체 설명적이어야 합니다.
    - 민감한 정보는 노출하지 않도록 주의해야 합니다.
    - 너무 많은 필드를 추가하면 복잡성이 증가할 수 있습니다.

**미디어 타입**

Problem JSON은 `application/problem+json` 미디어 타입을 사용해야 합니다. Content-Type 헤더에 설정되어 클라이언트가 응답이 Problem
형식임을 인식할 수 있게 합니다.   
이를 활용하여 클라이언트는 오류 응답을 일반 JSON 데이터와 구분하여 특별히 처리할 수 있게 해줍니다.

## Problem Details를 사용할 경우의 이점

- **표준화된 오류 처리**
    - 일관된 오류 응답 형식을 통해 API 전반에 걸쳐 예측 가능한 오류 처리가 가능합니다.
- **향상된 개발자 경험**
    - 클라이언트 개발자는 오류 응답 형식을 이해하기 위해 각 API의 문서를 참조할 필요가 줄어듭니다.
- **보안 향상**
    - 구조화된 형식은 민감한 정보 유출 위험을 줄이는 데 도움이 됩니다.
- **확장성과 유연성**
    - 기본 필드 외에 추가 필드를 지원하여 다양한 오류 상황에 맞게 확장할 수 있습니다.
- **로깅 용이**
    - 표준화된 형식은 오류 모니터링, 로깅, 분석 도구의 통합을 용이하게 합니다.

## 클라이언트 측 처리 지침

1. **미디어 타입 확인:** 응답의 `Content-Type`이 `application/problem+json`인지 확인합니다.
    2. **기본 필드 활용:**
        - `type:`
            - 문제 유형별로 다른 처리 로직을 구현할 수 있습니다.
            - `type`이 계층 구조인 경우, 계층 구조를 이용할 수도 있습니다.
        - `status:` HTTP 상태 코드 대신 `status` 필드를 통해 상태를 판별할 수 있습니다.
        - `title` & `detail:` 사용자에게 표시할 오류 메시지로 활용할 수 있습니다.

### `확장 필드 처리` & `중첩 구조` 처리

특정 오류 유형에 대해 맞춤형 처리 로직을 적용할 수 있습니다.

**예시: 회원가입**

```json
{
  "type": "https://example.com/probs/registration-validation",
  "title": "등록 필드 유효성 검사 실패",
  "status": 400,
  "detail": "등록 양식에는 유효성 검사 오류가 포함되어 있습니다.",
  "instance": "/api/register",
  "errors": {
    "email": {
      "message": "잘못된 이메일 형식",
      "code": "pattern",
      "suggestion": "@ 및 도메인 이름을 포함하십시오"
    },
    "password": {
      "message": "비밀번호가 너무 짧습니다",
      "code": "minLength",
      "minLength": 8,
      "currentLength": 4,
      "requirements": [
        "최소 8 자",
        "대문자를 포함합니다"
      ]
    }
  }
}
```

**`message` 필드:**

- **목적:** 사용자에게 표시할 수 있는 인간 친화적인 오류 메시지
- **활용 예시:**
    - UI에 직접 표시하여 사용자에게 오류 내용 전달

**`code` 필드**

- **목적:** 프로그래밍 방식으로 오류를 식별하는 표준화된 코드
- **활용 예시:**
    - 오류 유형에 따른 조건부 처리 로직 구현
    - 로깅 및 분석에서 오류 유형 분류

**suggestion 필드**

- **목적:** 오류 해결을 위한 구체적인 제안이나 힌트
- **활용 예시:**
    - 오류 메시지 아래에 추가 도움말로 표시

## Spring Boot 3에서의 Problem Details 구현

Spring Boot 3는 RFC 9457 "Problem Details for HTTP APIs" 표준을 기본적으로 지원합니다.

### 주요 컴포넌트

1. **ProblemDetail 클래스**
- RFC 9457 표준에 맞는 오류 응답을 생성하는 기본 구현
- 기본 필드(`type`, `title`, `status`, `detail`, `instance`) 및 확장 필드 지원
- `setProperty()` 메서드를 통한 확장 필드 추가

2. **ResponseEntityExceptionHandler 클래스**
- Spring MVC 예외를 처리하는 편리한 기본 클래스
- `@ControllerAdvice`를 통해 전역 예외 처리 설정
- 모든 Spring MVC 예외와 ErrorResponseException을 처리


### Spring Boot 회원가입 API 예제 구현

이 예제는 Spring Boot 3를 사용하여 회원가입 API를 구현하고, Problem Details 표준을 따르는 오류 응답을 제공합니다.

- **비즈니스 예외 처리 (RegistrationExceptionHandler)**
  - 회원가입 관련 비즈니스 규칙 위반 예외 처리 (이메일 중복 등)
  - 높은 우선순위로 설정하여 먼저 처리됨 (`@Order(Ordered.HIGHEST_PRECEDENCE)`)
  - 특정 비즈니스 규칙에 맞는 상세한 오류 정보 제공

- **전역 예외 처리 (GlobalExceptionHandler)**
  - Spring MVC 표준 예외 처리 (유효성 검사, 요청 형식 오류 등)
  - ResponseEntityExceptionHandler 상속을 통한 표준 예외 자동 처리
  - 모든 컨트롤러에 공통으로 적용되는 예외 처리
  - 낮은 우선순위로 설정 (`@Order(Ordered.LOWEST_PRECEDENCE)`, Default로 설정되어 있음)

### 테스트 방법

예제를 구현한 후 IntelliJ IDEA 환경에서 HTTP 클라이언트를 사용하여 쉽게 테스트할 수 있습니다.   
1. IntelliJ IDEA를 실행
2. user-registration.http 파일을 열어, 요청 실행

**테스트 시나리오**:
- **유효성 검사 오류**: 유효하지 않은 이메일 형식, 짧은 비밀번호, 필수 필드 누락 등 테스트
- **도메인 규칙 위반**: 이메일 중복 등의 비즈니스 규칙 검증
- **복합 오류**: 여러 유효성 검사 오류가 동시에 발생하는 경우


## 참조

- [Stop returning custom error responses from your API. Do this instead. - YouTube](https://www.youtube.com/watch?v=4NfflZilTvk)
- [Understanding Problem JSON - Medium](https://lakitna.medium.com/understanding-problem-json-adf68e5cf1f8)
- [RFC 9457](https://datatracker.ietf.org/doc/html/rfc9457#name-detail)
- [The Power of Problem Details for HTTP APIs - zuplo](https://zuplo.com/blog/2023/04/11/the-power-of-problem-details)

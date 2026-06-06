# Rankit - AI 기반 상품 키워드 분석 및 스마트스토어 노출 순위 예측 플랫폼 API 서버

> 네이버 스마트스토어 판매자가 상품명, 카테고리, 키워드 정보를 입력하면 예상 노출 순위를 예측하고, 상품명·키워드 전략을 개선할 수 있도록 지원하는 AI 기반 판매자 분석 플랫폼

<br>

## 1. 프로젝트 소개

Rankit은 네이버 스마트스토어 판매자를 대상으로 상품명, 카테고리, 키워드 정보를 분석해 예상 노출 순위를 예측하는 플랫폼임.

온라인 유통 환경에서 상품 노출 순위는 판매 성과에 직접적인 영향을 주지만, 기존 서비스는 키워드 통계 제공에 머무르는 경우가 많았음.
Rankit은 실제 상품을 등록하기 전에도 예상 노출 순위를 확인하고, 판매자가 상품명과 키워드 전략을 조정할 수 있도록 지원하는 것을 목표로 함.

백엔드 서버는 사용자 인증, AI 예측 요청, 사용자 예측 이력 저장, 외부 API 기반 데이터 수집, 관리자 배치 상태 조회, 배포 및 운영 모니터링 구조를 담당함.

<br>

## 2. 담당 역할

**백엔드 및 DevOps 담당**

* Spring Boot 기반 백엔드 API 설계 및 구현
* 사용자 인증, AI 예측 요청, 사용자 예측 이력 조회 기능 구현
* 네이버 쇼핑 인사이트 API, 쇼핑 검색 API, 검색광고 API 연동 구조 설계
* 외부 API 수집 로직을 운영형 배치 구조로 개선
* MySQL 기반 사용자 이력 조회 구조 개선 및 Keyset Pagination 적용
* Refresh Token Rotation, 해시 저장, HttpOnly/Secure 쿠키 기반 인증 구조 개선
* Docker, GitHub Actions, ECR, ECS Fargate 기반 백엔드 배포 자동화
* S3, CloudFront 기반 프론트엔드 정적 배포와 CloudFront 경로 라우팅 구성
* CloudWatch Dashboard, Alarm, SNS, Smoke Test, ECS Circuit Breaker 기반 운영 검증 구조 구성
* k6 부하 테스트를 통한 ECS Auto Scaling 동작 검증

<br>

## 3. 핵심 성과 요약

| 구분              | 개선 내용                                                            |
| --------------- | ---------------------------------------------------------------- |
| 이력 조회 성능 개선     | `Pageable` 기반 조회를 `createdAt + id` 커서 기반 Keyset Pagination으로 전환  |
| 인증 보안 강화        | Refresh Token Rotation, HMAC-SHA256 해시 저장, HttpOnly/Secure 쿠키 적용 |
| 외부 API 수집 구조 개선 | Swagger 수동 호출 구조를 스케줄러 기반 운영형 배치 구조로 전환                          |
| 부분 실패 대응        | 배치 실행 이력, 성공/실패 건수, 실패 대상, 에러 메시지 저장 구조 도입                       |
| 프론트/백엔드 분리 배포   | S3/CloudFront 정적 배포와 ECS/ALB 백엔드 배포 구조 분리                        |
| 단일 진입점 라우팅      | CloudFront 경로 기반으로 정적 파일, API, OAuth 요청을 Origin별 분기              |
| 배포 검증 자동화       | GitHub Actions 배포 후 CloudFront `/api/health` Smoke Test 추가       |
| 운영 관측성 개선       | CloudWatch Dashboard, Alarm, SNS 이메일 알림 구성                       |
| 장애 복구 기반 마련     | ECS Deployment Circuit Breaker와 Rollback 활성화                     |
| 확장성 검증          | k6 부하 테스트로 ECS Auto Scaling 동작 확인                                |

<br>

## 4. 주요 기능

* 회원가입, 로그인, JWT 인증
* Google, Naver, Kakao OAuth2 로그인
* Access Token / Refresh Token 기반 인증
* AI 노출 순위 예측 요청
* 사용자 예측 이력 조회
* 예측 이력 검색 및 커서 기반 페이지네이션
* 네이버 외부 API 기반 키워드·카테고리·트렌드 데이터 수집
* 배치 실행 이력 저장 및 관리자 조회
* 프론트엔드 정적 배포 및 백엔드 API 분리 운영
* 배포 후 헬스 체크 및 운영 지표 모니터링

<br>

## 5. 기술 스택

### Language & Framework

<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=Spring%20Security&logoColor=white"> <img src="https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white">

### Database & Storage

<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/mongodb-47A248?style=for-the-badge&logo=mongodb&logoColor=white"> <img src="https://img.shields.io/badge/amazon%20s3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">

### Infrastructure & DevOps

<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/github%20actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"> <img src="https://img.shields.io/badge/amazon%20ecs-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white"> <img src="https://img.shields.io/badge/amazon%20ecr-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/cloudfront-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/alb-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white">

### Monitoring & Test

<img src="https://img.shields.io/badge/cloudwatch-FF4F8B?style=for-the-badge&logo=amazoncloudwatch&logoColor=white"> <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white"> <img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black">

### External API

<img src="https://img.shields.io/badge/Naver%20Search%20Ad-03C75A?style=for-the-badge&logo=naver&logoColor=white"> <img src="https://img.shields.io/badge/Naver%20Shopping%20API-03C75A?style=for-the-badge&logo=naver&logoColor=white"> <img src="https://img.shields.io/badge/Naver%20Shopping%20Insight-03C75A?style=for-the-badge&logo=naver&logoColor=white">

<br>

## 6. 시스템 구조

아키텍처 이미지는 추후 추가 예정

현재 구조는 프론트엔드, 백엔드, AI 예측 서버를 분리하고, CloudFront를 단일 진입점으로 두는 방식으로 구성함.

```text
Client
  |
  v
CloudFront
  |-- Default(*) --------------> S3 정적 프론트엔드
  |-- /api/* ------------------> ALB -> ECS Spring Boot API Server
  |-- /oauth2/*, /login/oauth2/* -> ALB -> ECS Spring Boot API Server
  |-- /signup, /logout --------> ALB -> ECS Spring Boot API Server
  |
Spring Boot API Server
  |-- MySQL: 사용자, 인증, 예측 이력
  |-- MongoDB: 외부 API 수집 데이터
  |-- FastAPI: AI 노출 순위 예측 요청
  |-- Naver API: 키워드, 쇼핑 검색, 쇼핑 인사이트 데이터 수집
```

<br>

## 7. 핵심 기술 결정 및 트러블슈팅

사용자 이력 조회 성능, 인증 보안, 외부 API 수집 안정성, 프론트/백엔드 분리 배포, 운영 관측성, Auto Scaling 검증을 중심으로 구조를 개선함.

<br>

### 7.1. 사용자 이력 조회를 Keyset Pagination으로 전환

| 개선 항목        | 문제                                          | 해결                                               | 결과                                 |
| ------------ | ------------------------------------------- | ------------------------------------------------ | ---------------------------------- |
| 예측 이력 조회 최적화 | `Pageable` 기반 조회로 count 쿼리와 offset 탐색 비용 발생 | `createdAt + id` 기반 Keyset Pagination 적용         | count 쿼리 제거, 무한 스크롤에 적합한 조회 구조로 개선 |
| 검색 이력 조회 최적화 | 검색 조건과 최신순 정렬을 함께 처리해야 함                    | `user_id, query, createdAt DESC, id DESC` 인덱스 적용 | 검색 조건에서도 안정적인 커서 기반 조회 가능          |

#### 문제 상황

사용자 예측 이력을 최신순으로 조회하는 기능은 기존에 `Pageable` 기반으로 구현되어 있었음.
이 구조는 데이터 조회 쿼리 외에도 전체 개수를 세는 count 쿼리가 함께 발생하고, 뒤 페이지로 갈수록 offset 탐색 비용이 커지는 문제가 있었음.

예측 이력은 계속 누적되는 데이터이고, UI도 “다음 묶음”을 이어서 불러오는 방식이 자연스럽다고 판단해 페이지 번호 기반 조회보다 커서 기반 조회가 더 적합하다고 판단함.

#### 해결 방법

전체 이력 조회와 검색 이력 조회를 `Page` 기반에서 커서 기반 Keyset Pagination으로 변경함.

* 커서 기준: `createdAt + id`
* 첫 요청: 커서 없이 최신순 첫 묶음 조회
* 다음 요청: 마지막으로 조회한 `createdAt`, `id` 이후 데이터 조회
* `size + 1`개 조회 후 `hasNext` 계산
* count 쿼리 없이 다음 데이터 존재 여부 판단

조회 조건과 정렬 기준에 맞춰 다음 인덱스를 적용함.

```text
user_id, createdAt DESC, id DESC
user_id, query, createdAt DESC, id DESC
```

#### 결과

전체 이력 조회와 검색 이력 조회를 count 쿼리 없는 구조로 전환함.
Swagger로 첫 요청, 다음 요청, 검색 요청, 잘못된 커서 입력까지 검증했고, 실행 계획 확인 결과 full table scan 없이 인덱스를 사용하는 것을 확인함.

<br>

### 7.2. Refresh Token 보안 구조 고도화

| 개선 항목               | 문제                      | 해결                        | 결과                        |
| ------------------- | ----------------------- | ------------------------- | ------------------------- |
| Refresh Token 저장 방식 | DB에 refresh token 원문 저장 | HMAC-SHA256 기반 해시 저장      | DB 유출 시 원문 토큰 노출 위험 완화    |
| 토큰 재발급 구조           | 탈취 토큰 재사용 가능성 존재        | Refresh Token Rotation 적용 | 재발급 시 refresh token 교체    |
| 브라우저 쿠키 보안          | 쿠키 보안 속성 미흡             | HttpOnly, Secure 쿠키 적용    | JavaScript 접근 및 비보안 전송 제한 |

#### 문제 상황

기존 인증 구조는 Access Token과 Refresh Token을 사용하고 있었지만, Refresh Token을 DB에 원문으로 저장하고 있었음.
Refresh Token은 Access Token보다 수명이 길기 때문에 DB 유출 시 영향이 더 크다고 판단함.

또한 브라우저 기반 인증 흐름에서 쿠키에 `HttpOnly`, `Secure` 설정이 충분히 반영되지 않으면 XSS나 비보안 전송 측면에서 취약할 수 있다고 봄.

#### 해결 방법

Refresh Token 구조를 다음 방식으로 개선함.

* Refresh Token Rotation 적용
* Refresh Token 원문 저장 제거
* HMAC-SHA256 기반 해시값 저장
* 재발급 시 새 Refresh Token 발급 및 DB 해시값 교체
* Refresh Token은 HttpOnly/Secure 쿠키로 전달
* Access Token/Refresh Token 재발급 흐름 정리

#### 결과

기존 단순 토큰 발급 구조를 유출과 재사용 위험까지 고려한 구조로 개선함.
Refresh Token 원문 저장을 제거하고, RTR 기반 재발급 흐름과 브라우저 쿠키 보안 설정을 적용함.

<br>

### 7.3. 외부 API 수집 구조를 운영형 배치 구조로 개선

| 개선 항목        | 문제                        | 해결                          | 결과                   |
| ------------ | ------------------------- | --------------------------- | -------------------- |
| 외부 API 호출 구조 | Swagger 공개 POST로 수동 수집 실행 | 스케줄러 기반 내부 배치 구조로 변경        | 사용자 기능과 내부 수집 책임 분리  |
| 실패 추적        | 실패 시 로그 확인에 의존            | 배치 실행 이력 저장                 | 성공/부분 실패/실패 상태 확인 가능 |
| 부분 실패 처리     | 일부 수집 실패 시 전체 흐름 설명 어려움   | 개별 항목 실패를 기록하고 나머지 수집 계속 진행 | 운영 관점의 수집 안정성 개선     |

#### 문제 상황

Rankit은 네이버 쇼핑 인사이트 API, 쇼핑 검색 API, 검색광고 API를 사용해 데이터를 수집함.
초기에는 Swagger에서 직접 POST로 외부 API 수집을 실행할 수 있도록 열어두고 있었음.

이 구조는 빠른 개발에는 편했지만, 사용자 기능과 내부 수집 기능의 경계가 모호했고, 실패 시 최근 수집 상태를 로그로만 확인해야 하는 한계가 있었음.

#### 해결 방법

외부 수집용 공개 POST 컨트롤러를 제거하고, 저장된 결과를 조회하는 GET API만 남김.
외부 HTTP 호출은 client 계층으로 분리함.

* `NaverShoppingInsightClient`
* `NaverShopSearchClient`
* `NaverSearchAdKeywordClient`

수집 작업은 `RankcatJobs`에서 수행하도록 정리하고, 개별 항목 실패는 내부에서 처리해 나머지 수집이 계속 진행되도록 변경함.

배치 종료 시 실행 이력을 저장함.

* 잡 이름
* 시작/종료 시각
* 성공 건수
* 실패 대상
* 에러 코드
* 에러 메시지

또한 관리자 읽기 전용 API를 추가해 최근 수집 상태를 조회할 수 있게 함.

#### 검증 내용

`RankcatJobs` 테스트를 작성해 다음 시나리오를 검증함.

* 일부 항목에서 `BusinessException` 발생 시, 실패 대상과 성공 건수가 실행 이력으로 기록되는지 확인
* 카테고리 파일 로딩 단계에서 `IOException` 발생 시, 배치 시작 자체가 실패해도 `finally` 블록에서 실행 이력이 저장되는지 확인

#### 결과

외부 API 연동을 단순 호출 코드에서 운영형 수집 구조로 개선함.
부분 실패 허용, 실패 대상 기록, 실행 이력 저장, 관리자 조회 API를 통해 배치 상태를 설명할 수 있는 구조로 전환함.

<br>

### 7.4. CloudFront 단일 진입점 기반 요청 라우팅 구조 설계

| 개선 항목          | 문제                                 | 해결                                        | 결과                |
| -------------- | ---------------------------------- | ----------------------------------------- | ----------------- |
| 프론트/백엔드 분리     | Spring Boot가 화면과 API를 함께 담당        | S3/CloudFront 정적 배포와 ECS API 서버 분리        | 프론트와 백엔드 변경 영향 분리 |
| API 라우팅        | API 요청이 S3 Origin으로 전달되어 403 발생 가능 | CloudFront Behavior 기반 Origin 분기          | 요청 경로별 라우팅 정상화    |
| OAuth Redirect | HTTPS 접속 환경에서 HTTP Redirect URI 생성 | 운영 Redirect URI를 CloudFront HTTPS 기준으로 명시 | OAuth 인증 흐름 정상화   |

#### 문제 상황

초기에는 Spring Boot가 화면과 API를 함께 담당하는 구조였음.
이후 프론트엔드와 백엔드를 분리하면서 정적 화면 요청, 사용자 API 요청, OAuth 로그인 요청, AI 예측 요청을 각각 올바른 Origin으로 전달해야 했음.

CloudFront 기본 경로가 S3로 연결되어 있기 때문에, API 성격의 요청이 S3로 전달되면 403이 발생할 수 있었음.
실제로 회원가입 요청이 S3 Origin으로 전달되어 `POST /signup` 요청이 실패하는 문제가 있었고, OAuth 로그인에서는 Redirect URI가 HTTP로 생성되어 `redirect_uri_mismatch` 문제가 발생함.

#### 해결 방법

CloudFront를 서비스의 단일 진입점으로 두고 요청 경로별 Origin을 분리함.

* `Default(*)` → S3 정적 프론트엔드
* `/api/*` → ALB → ECS Spring Boot
* `/signup`, `/logout` → ALB → ECS Spring Boot
* `/oauth2/*`, `/login/oauth2/*` → ALB → ECS Spring Boot
* `/api/ai/*` 또는 `/ai/*` → Spring Boot → FastAPI AI 서버로 전달 가능한 구조 설계

API 계열 Behavior는 캐싱을 비활성화하고, Cookie, Authorization Header, Query String이 백엔드까지 전달되도록 설정함.
또한 운영 환경의 OAuth Redirect URI를 CloudFront HTTPS 도메인 기준으로 명시하고, 운영 도메인을 CORS 허용 Origin에 추가함.

#### 결과

프론트엔드와 백엔드를 분리하면서도 사용자는 하나의 CloudFront 도메인으로 서비스를 이용할 수 있게 함.
회원가입, 로그인, OAuth 인증 흐름을 CloudFront 운영 도메인 기준으로 정상화했고, Spring Boot는 REST API 서버 역할에 집중하는 구조로 개선함.

<br>

### 7.5. 배포 자동화 이후 운영 관측성과 배포 검증 구조 개선

| 개선 항목    | 문제                                  | 해결                                            | 결과                  |
| -------- | ----------------------------------- | --------------------------------------------- | ------------------- |
| 배포 후 검증  | ECS 배포 성공만으로 실제 사용자 경로 정상 여부 확인 어려움 | CloudFront `/api/health` Smoke Test 추가        | 배포 후 실제 요청 경로 자동 검증 |
| 운영 지표 확인 | ALB, ECS, RDS 상태를 콘솔에서 개별 확인        | CloudWatch Dashboard 구성                       | 주요 지표를 한 화면에서 확인    |
| 장애 인지    | 5XX, Target 부재, 리소스 증가를 수동 확인       | CloudWatch Alarm + SNS 이메일 연동                 | 주요 장애 상황 알림 수신      |
| 실패 배포 대응 | 신규 Task 실패 시 수동 복구 필요               | ECS Deployment Circuit Breaker + Rollback 활성화 | 실패 배포 자동 롤백 기반 마련   |

#### 문제 상황

GitHub Actions로 Docker 이미지 빌드, ECR Push, ECS Rolling Update까지 자동화했지만, 배포 자동화만으로 운영 안정성이 보장되는 것은 아니라고 판단함.

ECS Task가 Running 상태여도 ALB Health Check를 통과하지 못하면 사용자는 API를 호출할 수 없음.
또한 배포가 완료되더라도 `CloudFront → ALB → ECS → Spring Boot` 전체 경로가 정상인지 별도로 검증할 필요가 있었음.

#### 해결 방법

CloudWatch Dashboard를 구성해 주요 운영 지표를 한 화면에서 확인하도록 개선함.

* ALB 요청 수
* ALB 5XX 오류
* ALB 응답 시간
* Healthy / UnHealthy Target 수
* ECS CPU / Memory 사용률
* RDS 연결 수 / 저장 공간

CloudWatch Alarm과 SNS 이메일 알림도 연동함.

* ALB Target 5XX 오류 감지
* Healthy Target 부재 감지
* ECS CPU / Memory 사용률 증가 감지

또한 인증이 필요 없는 `/api/health` 엔드포인트를 추가하고, GitHub Actions 배포 마지막 단계에서 CloudFront 도메인의 `/api/health`를 호출하는 Smoke Test를 추가함.
ECS Deployment Circuit Breaker와 Rollback 옵션도 활성화함.

#### 결과

단순 배포 자동화에서 운영 관측성, 장애 인지, 배포 후 검증까지 고려한 구조로 개선함.
배포 후 실제 사용자 요청 경로를 자동 검증하고, 실패 배포 시 이전 성공 배포로 복구할 수 있는 기반을 마련함.

<br>

### 7.6. 부하 테스트 기반 ECS Auto Scaling 검증

| 개선 항목          | 문제                                   | 해결                                 | 결과                            |
| -------------- | ------------------------------------ | ---------------------------------- | ----------------------------- |
| 확장성 검증         | 단일 Task 정상 동작만으로 트래픽 증가 대응 여부 확인 어려움 | ECS Auto Scaling 설정 후 k6 부하 테스트 수행 | Desired Count `1 → 2` 확장 확인   |
| 사용자 API 안정성 확인 | 실제 사용자 기능에 가까운 API 부하 검증 필요          | 인증 및 RDS 조회가 포함된 이력 조회 API 테스트     | 요청 실패율 `0.00%`, 체크 성공률 `100%` |
| 응답 성능 확인       | 부하 상황에서 응답 시간 확인 필요                  | 100 VU 기준 테스트 수행                   | p95 응답 시간 `193.48ms` 확인       |

#### 문제 상황

ECS Fargate 기반 배포 이후 단일 Task 환경에서만 정상 동작하는 것이 아니라, 트래픽 증가 시 다중 Task로 확장 가능한지 검증할 필요가 있었음.

#### 해결 방법

ECS Service Auto Scaling을 설정함.

* 최소 Task: 1개
* 최대 Task: 2개
* CPU 사용률 30% 기준 Target Tracking 정책 구성

이후 k6를 사용해 운영 CloudFront 도메인의 사용자 이력 조회 API에 부하 테스트를 수행함.

```text
GET /api/ai/histories?size=100
```

해당 API는 JWT 인증, 사용자별 히스토리 조회, RDS 접근이 포함된 실제 사용자 기능에 가까운 조회 API였음.

#### 검증 결과

* 총 요청 수: 34,047건
* 요청 실패율: 0.00%
* 체크 성공률: 100%
* p95 응답 시간: 193.48ms
* CloudWatch에서 ALB 요청 수, 응답 시간, ECS CPU 사용률 증가 확인
* ECS Auto Scaling 활동에서 Desired Count `1 → 2` 변경 확인
* ECS Task 화면에서 Running Task 2개 실행 확인

#### 결과

부하 상황에서도 5XX 오류 없이 사용자 히스토리 조회 API가 안정적으로 응답함을 확인함.
또한 CloudWatch 지표 기반으로 ECS Auto Scaling이 트리거되고, ECS Fargate Task가 자동 확장되는 구조를 검증함.

<br>

## 8. API 명세

- [OpenAPI JSON 명세](./docs/openapi.json)
- [Postman Collection](./docs/rankit.postman_collection.json)
- Swagger UI: 로컬 실행 후 `http://localhost:8080/swagger-ui/index.html`에서 확인 가능

<br>

## 9. 팀원 및 역할

| 이름      | 역할               | 담당 내용                                                                                                                                                     |
| ------- | ---------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **김대기** | 기획               | 서비스 기획, 시장 및 사용자 요구사항 분석, 기능 정의, 발표 자료 구성                                                                                                                 |
| **김지원** | Backend & DevOps | Spring Boot 기반 API 설계 및 구현, 사용자 인증, 예측 이력 조회, 외부 API 수집 구조 개선, Keyset Pagination 적용, Refresh Token 보안 고도화, GitHub Actions/ECS/CloudFront 배포 및 운영 검증 구조 구성 |
| **정하윤** | Frontend & ML    | 프론트엔드 UI/UX 구현, 사용자 입력 및 예측 결과 화면 구현, 노출 순위 예측 모델 개발 및 FastAPI 기반 예측 API 구현                                                                               |

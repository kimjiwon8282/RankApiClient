# 🏆 Rankit – SmartStore Product Ranking Prediction Platform (Backend)

Java & Spring Boot 기반으로  
**SmartStore 상품 데이터를 수집·분석하고, 랭킹 예측을 지원하는 백엔드 시스템**입니다.

🔗 **Service URL**  
http://rankit-env.eba-qahh7i2w.ap-northeast-2.elasticbeanstalk.com/home

---

## 1. Problem & Motivation

SmartStore 판매자는 다음과 같은 문제를 겪습니다.

- 어떤 키워드가 실제로 효과적인지 알기 어렵다
- 내 상품이 어느 정도 순위에 노출될지 예측하기 힘들다
- 네이버 외부 API는 호출 비용이 발생하고 응답 속도가 느리다
- 동일 데이터를 반복 호출하게 되어 비용과 시간이 낭비된다

👉 **이 프로젝트는**
- 외부 API 호출을 최소화하고
- 데이터를 축적·분석하며
- AI 기반 예측을 가능하게 하는  
**확장 가능한 백엔드 시스템**을 목표로 설계되었습니다.

---

## 2. System Overview

- Java & Spring Boot 기반 REST API
- 네이버 외부 API 연동
- MongoDB 기반 데이터 저장 및 캐싱
- AI 모델 서버와 분리된 구조
- AWS 환경 배포

```text
Client
  ↓
Spring Boot Backend
  ├─ Authentication (JWT / OAuth2)
  ├─ Naver API Integration
  ├─ Data Cache (MongoDB)
  ├─ Scheduler (Spring @Scheduled)
  └─ Distributed Lock (ShedLock)
        ↓
AI Model Server (Separate Repository)
```

---

## 3. Key Design Decisions

### 🔐 Stateless Authentication (JWT + OAuth2)
- 세션 기반 인증의 확장성 한계를 고려하여 JWT 기반 인증 구조 채택
- Access Token / Refresh Token 분리
- Refresh Token은 HttpOnly Cookie + DB 저장 방식으로 보안 강화

---

### 📡 External API Optimization
- RestTemplate + Interceptor 패턴으로 API 인증 로직 중앙화
- MongoDB 캐싱을 통해 중복 API 호출 최소화

---

### ⏰ Scheduled Data Collection
- Spring `@Scheduled` 기반 자동 데이터 수집
- 다중 서버 환경을 고려해 ShedLock 적용
- MongoDB를 Lock Provider로 사용

---

## 4. Tech Stack

**Backend**  
- Java 17, Spring Boot, Spring Security  
- JWT, OAuth2  

**Database**  
- MongoDB, RDBMS  

**Cloud & DevOps**  
- AWS Elastic Beanstalk, EC2, RDS  
- GitHub Actions  

**External API**  
- Naver Search Ad / Shopping Insight API  

---

## 5. Related Repositories

- Backend API  
  https://github.com/kimjiwon8282/RankApiClient

- AI Model Server  
  https://github.com/kimjiwon8282/RankitAI

---

## 6. Documentation

- Technical Blog (KR)  
  https://blog.naver.com/iwill0324

---

## 7. Notes

- 본 프로젝트는 졸업작품으로 진행되었습니다.
- 실제 서비스 환경을 가정하여 설계되었습니다.



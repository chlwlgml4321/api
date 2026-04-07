# MPS API (MPSBAT)

**Hecto Financial** 선불전자지급수단(머니/포인트) 및 상품권 관리를 위한 REST API 서버

---

## 목차

- [개요](#개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [주요 기능](#주요-기능)
- [API 엔드포인트](#api-엔드포인트)
- [데이터베이스](#데이터베이스)
- [보안](#보안)
- [빌드 및 실행](#빌드-및-실행)
- [설정](#설정)
- [모니터링](#모니터링)

---

## 개요

MPS API는 디지털 월렛(선불머니/포인트) 기반의 금융 거래 플랫폼입니다. 잔액 관리, 자동충전, 상품권 발행/사용, 출금, 펌뱅킹 등 다양한 금융 서비스를 제공합니다.

### 핵심 도메인

| 도메인 | 설명 |
|--------|------|
| **월렛** | 머니/포인트 잔액 관리, 사용, 이체 |
| **충전** | 수동/자동 충전, 부족금액 자동충전, 임계값 자동충전 |
| **출금** | 머니 출금, 대기 출금, 관리자 출금 |
| **상품권** | 단건/대량 상품권 발행, 사용, 재발행, 양도 |
| **카드** | 카드 사용 승인, 취소 |
| **펌뱅킹** | 입금 통보, 정산 |
| **현금영수증** | 현금영수증 발급 |

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3.2 |
| **Build Tool** | Gradle 8.8 |
| **Database** | Oracle (ojdbc11) |
| **ORM** | Spring Data JPA / Hibernate |
| **Scheduling** | Spring Quartz |
| **Batch** | Spring Batch |
| **HTTP Client** | Spring Cloud OpenFeign |
| **Monitoring** | Spring Actuator, MTMS |
| **암호화** | Jasypt (PBEWithMD5AndTripleDES), AES |
| **기타** | Lombok, Commons Lang3 |

---

## 프로젝트 구조

```
src/main/java/kr/co/hectofinancial/mps/
├── api/v1/                              # API v1 구현
│   ├── authentication/                  # 인증 및 빌키 관리
│   ├── card/                            # 카드 사용/취소
│   ├── common/                          # 공통 유틸, 기반 클래스, 알림
│   ├── cpn/                             # 쿠폰 관리
│   ├── csrc/                            # 현금영수증
│   ├── customer/                        # 고객 정보 관리
│   ├── deposit/                         # 입금 통보
│   ├── firm/                            # 펌뱅킹
│   ├── giftcard/                        # 상품권 (단건/대량)
│   ├── health/                          # 헬스 체크
│   ├── market/                          # 가맹점 충전 매핑
│   ├── trade/                           # 핵심 거래 처리
│   └── notification/                    # 알림 서비스
├── global/                              # 글로벌 설정 및 유틸리티
│   ├── annotation/                      # 커스텀 어노테이션
│   ├── aop/                             # AOP (로깅, 트랜잭션 등)
│   ├── config/                          # Spring 설정 (CORS, JPA, Async 등)
│   ├── constant/                        # 상수 정의
│   ├── error/                           # 에러 핸들링 및 에러코드
│   ├── extern/whitelabel/               # 화이트라벨 연동
│   ├── filter/                          # 요청/응답 필터
│   ├── interceptor/                     # 요청 인터셉터 (인증, Rate Limit)
│   ├── mtms/                            # MTMS 모니터링 에이전트
│   ├── util/                            # 유틸리티 (암호화, 날짜 등)
│   └── validator/                       # 입력값 검증
└── test/                                # 테스트/부하 테스트
```

### 레이어 구조

```
Controller → Service → Repository → Entity
```

각 기능 모듈은 도메인 기반 패키지로 구성되며, `controller`, `service`, `repository`, `domain(entity/dto)` 레이어를 따릅니다.

---

## 주요 기능

### 1. 월렛 잔액 관리
- 머니/포인트 잔액 조회 (상세, 원장별)
- 잔액 사용 (통합/분리 사용)
- 고객 간 이체
- 출금 가능 금액 조회

### 2. 자동충전 시스템
- **부족금액 자동충전**: 잔액 부족 시 자동 충전
- **임계값 자동충전**: 설정된 한도 기준 자동 충전
- 가맹점별 자동충전 가용 여부 확인
- 고객별 충전 한도 관리

### 3. 결제 처리
- 카드 사용 승인 및 취소
- 머니/포인트 우선순위 기반 잔액 차감
- 충전 승인 및 취소
- 동시 거래 처리

### 4. 상품권 관리
- **단건 상품권**: 발행, 사용, 재발행
- **대량 상품권**: 발행, 사용, 양도, 유통잔액 충전/취소
- PIN 기반 상품권 관리
- 구매 이력 조회

### 5. 금융 거래
- 머니 출금 (즉시/대기/재시도)
- 포인트 만료 처리 및 소멸
- 빌키 관리 (정기 결제)
- 현금영수증 발급

### 6. 펌뱅킹
- 입금 통보 수신
- 펌 계좌 정산
- 거래 요약 처리

### 7. 관리자 수기 거래
관리자가 대량의 머니/포인트 지급, 출금, 포인트 회수를 일괄 처리할 수 있는 기능입니다.

- **비동기 처리**: `@Async`를 통한 대량 거래 비동기 실행
- **지원 거래 유형**:
  - `MP` (머니 지급) / `PP` (포인트 지급) - 충전 API 호출
  - `MW` (머니 출금) / `WW` (대기머니 출금) - 출금 API 호출
  - `PR` (포인트 회수) - 포인트 소멸 API 호출
- **중복 검증**: 고객명 + 휴대폰번호 기반 중복 요청 검증
- **트랜잭션 격리**: `IsolatedService`를 통해 건별 독립 트랜잭션 (`REQUIRES_NEW`) 처리
- **결과 추적**: 건별 처리 결과(성공/실패) 및 처리 전후 잔액 기록
- **처리 상태 관리**:
  - `W` (승인요청) → `A` (승인완료) → `S` (성공) / `F` (실패) / `P` (부분성공)

#### 관련 테이블

| 테이블 | 설명 |
|--------|------|
| `TB_MPS_ADMIN_TRD_REQ` | 관리자 거래 요청 (요청번호, 거래구분, 요청건수/금액, 처리상태, 성공/실패 집계) |
| `TB_MPS_ADMIN_TRD_DTL` | 관리자 거래 상세 (건별 고객정보, 거래금액, 처리결과, 전후잔액) |

#### 처리 흐름

```
1. AdminController: 관리자 수기 거래 요청 수신 (POST /v1/admin/manual/trade)
2. ApprovalService: 요청 검증 (chkAdminCharge)
3. AsyncService: 비동기 거래 처리 시작
   ├── 거래 요청 조회 및 상태 업데이트
   ├── 대상 리스트 중복 검증 (고객명 + 휴대폰번호)
   ├── 건별 처리:
   │   ├── 회원 조회 (고객명 + 휴대폰번호 → 회원번호)
   │   ├── 잔액 조회 (처리 전 잔액 기록)
   │   ├── pktHash 생성 (SHA256)
   │   └── IsolatedService: 독립 트랜잭션으로 거래 실행
   │       ├── MP/PP → 충전 승인
   │       ├── MW/WW → 관리자 출금
   │       └── PR → 포인트 회수
   └── 요청 결과 집계 (성공/실패 건수 및 금액)
```

---

## API 엔드포인트

### 헬스 체크
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/v1/health/check` | 서버 상태 확인 |
| GET | `/v1/health/check/mtms` | MTMS 모니터링 헬스 체크 |

### 인증/빌키
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/trade/getBillKey` | 고객 빌키 조회 |

### 고객
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/customer/info` | 고객 정보 조회 |

### 카드/월렛 사용
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/card/use` | 카드 사용 승인 |
| POST | `/v1/card/use/param` | 카드 사용 승인 (파라미터) |
| POST | `/v1/card/use/cancel` | 카드 사용 취소 |
| POST | `/v1/card/use/cancel/param` | 카드 사용 취소 (파라미터) |

### 월렛
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/wallet/use` | 월렛 사용 |
| POST | `/v1/wallet/use/cancel` | 월렛 사용 취소 |
| POST | `/v1/wallet/use/each` | 월렛 분리 사용 (머니/포인트) |
| POST | `/v1/wallet/balance` | 월렛 잔액 조회 |
| POST | `/v1/wallet/balance/withdrawal` | 출금 가능 금액 조회 |
| POST | `/v1/wallet/balance/detail` | 월렛 잔액 상세 조회 |
| POST | `/v1/wallet/option/balance` | 자동충전용 잔액 조회 |
| POST | `/v1/wallet/transfer` | 타 고객 이체 |
| POST | `/v1/wallet/balance/ledger` | 원장별 잔액 조회 |

### 머니
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/money/withdrawal` | 머니 출금 |
| POST | `/v1/money/wait/withdrawal` | 대기 머니 출금 |
| POST | `/v1/money/gift` | 머니 선물 |
| POST | `/v1/money/willMnyWdYn` | 출금 가능 여부 확인 |
| POST | `/v1/money/withdrawal/retry` | 출금 재시도 |
| POST | `/v1/money/admin/withdrawal` | 관리자 출금 |

### 포인트
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/get/customers/point/expiring` | 고객 만료 예정 포인트 조회 |
| POST | `/v1/point/revoke` | 포인트 소멸 |

### 충전/승인
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/approval/charge` | 충전 승인 |
| POST | `/v1/approval/charge/cancel` | 충전 승인 취소 |

### 관리자 수기 거래
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/admin/manual/trade` | 관리자 수기 대량 거래 (비동기) - 머니지급/포인트지급/머니출금/대기머니출금/포인트회수 |
| POST | `/v1/money/admin/withdrawal` | 관리자 머니 출금 (단건) |

### 거래 조회
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/trade/list` | 거래 목록 조회 |
| POST | `/v1/trade/detail` | 거래 상세 조회 |
| POST | `/v1/trade/detail/market` | 가맹점 거래번호 기반 상세 조회 |
| POST | `/v1/trade/use/summary` | 거래 사용 요약 조회 |

### 단건 상품권
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/giftcard/issue` | 상품권 발행 |
| POST | `/v1/giftcard/use` | 상품권 사용 |
| POST | `/v1/giftcard/search` | 상품권 조회 |
| POST | `/v1/giftcard/reissue` | 상품권 재발행 |

### 대량 상품권
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/giftcard/bundle/issue` | 대량 상품권 발행 |
| POST | `/v1/giftcard/bundle/use` | 대량 상품권 사용 |
| POST | `/v1/giftcard/bundle/transfer` | 대량 상품권 양도 |
| POST | `/v1/giftcard/bundle/charge/etc` | 유통잔액 충전 |
| POST | `/v1/giftcard/bundle/charge/cancel` | 유통잔액 충전 취소 |
| POST | `/v1/giftcard/bundle/balance` | 대량 상품권 잔액 조회 |
| POST | `/v1/giftcard/bundle/list` | 대량 상품권 목록 조회 |
| POST | `/v1/giftcard/bundle/info` | 대량 상품권 정보 조회 |
| POST | `/v1/giftcard/bundle/balance/use` | 유통잔액 사용 |
| POST | `/v1/giftcard/bundle/balance/use/cancel` | 유통잔액 사용 취소 |

### 현금영수증
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/csrc/resist` | 현금영수증 발급 |

### 가맹점
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/market/charge/list` | 가맹점 충전 매핑 목록 조회 |

### 펌뱅킹
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/v1/firm/deposite/notice` | 입금 통보 |

---

## 데이터베이스

### DBMS
- **Oracle Database** (ojdbc11 드라이버)

### 주요 테이블

| 엔티티 | 테이블명 | 설명 |
|--------|----------|------|
| Customer | `TB_MPS_CUST` | 고객 계정 정보 |
| Trade | `PM_MPS_TRD` | 거래 내역 |
| CustWallet | - | 고객 월렛 잔액 |
| PayMoney | - | 머니 결제 내역 |
| PayPoint | - | 포인트 결제 내역 |
| CustChrgMean | - | 고객 충전 수단 |
| GiftCardTrade | `PM_MPS_GC_TRD` | 단건 상품권 거래 |
| GiftCardBundleTrade | `PM_MPS_GC_DSTB_TRD` | 대량 상품권 거래 |
| AdminTrdReq | `TB_MPS_ADMIN_TRD_REQ` | 관리자 거래 요청 (요청번호, 거래구분, 건수/금액, 처리상태) |
| AdminTrdDtl | `TB_MPS_ADMIN_TRD_DTL` | 관리자 거래 상세 (건별 고객정보, 결과, 전후잔액) |
| DpmnRcv | - | 입금 수신 내역 |
| EzpCsrcIss | - | 현금영수증 발급 내역 |
| NotiInfo / NotiSend | - | API 알림 내역 |
| Holiday | - | 영업일/휴일 관리 |

### 데이터 암호화
민감 데이터는 AES 암호화 컨버터를 통해 DB 저장 시 자동 암호화됩니다:
- 고객명 (`CUST_NM`)
- CI (`CI_ENC`)
- 휴대폰번호 (`CPHONE_NO_ENC`)
- 충전수단 (빌키)

---

## 보안

### 요청 인증
- 요청 단위 인증 인터셉터 적용
- **Rate Limiting**: 초당 200건 제한 (초과 시 `TOO_MANY_REQUESTS` 응답)
- 제외 경로: `/v1/health/**`, `/error`

### 속성 암호화
- **Jasypt** 기반 `application.properties` 내 민감 속성 암호화
- 알고리즘: `PBEWithMD5AndTripleDES`
- Base64 인코딩

### CORS 설정
- 허용 Origin: `*` (전체)
- 허용 Method: `GET`, `POST`
- Max Age: `3600`초

### 요청 필터링
- `CachingRequestBodyFilter`: 요청 본문 캐싱 (다중 읽기 지원)
- MDC 기반 세션 ID 로깅
- `X-FORWARDED-FOR` 헤더 지원 (프록시 환경)

---

## 빌드 및 실행

### 요구사항
- **JDK**: 17 이상
- **Gradle**: 8.8 (Wrapper 포함)
- **Oracle Database**: 접속 정보 필요

### 빌드

```bash
# 클린 빌드
./gradlew clean build

# 테스트 제외 빌드
./gradlew clean build -x test
```

### 실행

#### 로컬 실행 (JAR)
```bash
java -jar build/libs/MPSBAT-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=local \
  --jasypt.encryptor.password=<암호화키>
```

#### Gradle 직접 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### WAS 배포 (WAR)
`build/libs/MPSBAT-0.0.1-SNAPSHOT.war` 파일을 Tomcat 등 WAS에 배포합니다.

---

## 설정

### 필수 속성

| 속성 | 설명 |
|------|------|
| `spring.profiles.active` | 실행 프로파일 (`local`, `dev`, `staging`, `prod`) |
| `jasypt.encryptor.password` | Jasypt 암호화 키 |
| `spring.datasource.url` | Oracle JDBC 접속 URL |
| `spring.datasource.username` | DB 사용자명 |
| `spring.datasource.password` | DB 비밀번호 |

### 주요 설정 클래스

| 클래스 | 역할 |
|--------|------|
| `WebConfig` | CORS, 인터셉터, Argument Resolver 설정 |
| `JasyptConfig` | 속성 암호화 설정 |
| `AsynConfig` | 비동기 처리/스레드 풀 설정 |
| `RestTemplateConfig` | HTTP 클라이언트 설정 |
| `ServerInfoConfig` | 서버 호스트/IP 정보 |
| `JpaConfig` | JPA 및 Hibernate 설정 |
| `FeignClientConfig` | Feign 클라이언트 설정 |

---

## 에러 처리

### 구조
- `GlobalExceptionHandler`: 전역 예외 처리
- `ErrorResponse`: 통일된 에러 응답 형식
- `ErrorCode` Enum: 100개 이상의 사전 정의된 에러코드

### 에러코드 분류

| 접두사 | 분류 | 예시 |
|--------|------|------|
| `A` | 공통 | 파라미터 검증, 암호화, 영업일 |
| `M` | 가맹점 | 가맹점 미존재, 상태 오류, 충전수단 |
| `C` | 고객 | 미존재, 상태 오류, KYC, CI 불일치 |
| `W` | 월렛 | 잔액 오류, 출금 오류 |
| `T` | 거래 | 금액 오류, 한도 초과, 취소 |
| `P` | 포인트 | 유효기간, 포인트 미존재 |
| `G` | 상품권 | PIN 불일치, 유효성 |
| `S` | 시스템 | 시스템 오류, 타임아웃 |

---

## 모니터링

| 항목 | 설명 |
|------|------|
| **Actuator** | `/actuator/*` 엔드포인트로 애플리케이션 상태 모니터링 |
| **MTMS** | `MonitAgent` 기반 커스텀 헬스 체크 |
| **요청 로깅** | MDC 세션 ID 기반 요청 추적 |
| **Rate Limiting** | 초당 200건 요청 제한 |
| **에러 추적** | 체계적 에러코드 시스템 |

---

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 부하 테스트 (local/test 프로파일에서만 활성화)
GET /load/test
```

---

## 프로젝트 통계

| 항목 | 수량 |
|------|------|
| Controller | 16개 |
| Service | 54개 |
| Repository | 40개+ |
| Entity | 50개+ |
| DTO | 100개+ |
| 에러코드 | 100개+ |

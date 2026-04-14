## 🛠️ 브랜치 전략

### 브랜치 종류

- main: 배포용 브랜치 (최종 코드)
- dev: 개발 통합 브랜치
- feature/*: 기능 개발 브랜치
- bug/*: 버그 수정 브랜치

---

### 브랜치 네이밍 규칙

- feature/기능명
    - 예: feature/email-verification
    - 예: feature/jwt-login

- bug/버그명
    - 예: bug/login-error
    - 예: bug/token-expired

---

### 브랜치 생성

```bash
git checkout dev
git pull origin dev
git checkout -b feature/email-verification
```

---

### 작업 흐름
0. dev 브랜치 원격 최신 반영
1. 이슈 생성
2. dev 브랜치에서 feature 브랜치 생성
3. 기능 개발 및 commit
4. 작업 중간 dev 최신 내용 가져오기
5. feature 브랜치 작업 완료 후 dev로 merge
6. dev에서 테스트 완료 후 main 브랜치로 merge
   🚨 main으로 merge는 가급적 하지 않는다. dev에서 모든 기능 구현 완료 후 main으로 merge 힌디. 🚨


---

# ✅ 📌 커밋 규칙

## 커밋 컨벤션
### 타입 종류
- init: 초기 설정
- feat: 기능 추가
- fix: 버그 수정
- refactor: 코드 리팩토링
- chore: 기타 작업 (설정, 문서 등)

---

###  작성 규칙
- 타입: 작업 내용 (#이슈번호)
- 예시
```bash
git commit -m "init: 프로젝트 초기 세팅"

git commit -m "feat: 이메일 인증 API 구현 (#12)"

git commit -m "fix: 로그인 시 토큰 오류 수정 (#15)"

git commit -m "refactor: UserService 코드 구조 개선"

git commit -m "chore: issue template 추가"
```

---

# ✅ 📌 이슈 규칙


## 🧩 이슈 작성 규칙

### 이슈 생성 타이밍

- 기능 개발 시작 전
- 버그 수정 전

---

### 이슈 네이밍 규칙

- [FEATURE] 기능 설명
    - 예: [FEATURE] 이메일 인증 기능 구현

- [BUG] 버그 설명
    - 예: [BUG] 로그인 시 토큰 오류

---

### 작성 내용

- 기능 설명
- 작업 내용 (체크리스트)
- 필요 시 흐름 및 참고사항

---

### 작업 흐름

1. 이슈 생성
2. 해당 이슈 기준으로 브랜치 생성
3. 기능 개발
4. 커밋 시 이슈 번호 포함

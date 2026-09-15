# 월간 재무관리 Android APK — Local DB Edition

기존 Google Sheets + Apps Script 월간 재무 웹앱의 핵심 구조를 Android 네이티브 앱으로 옮긴 버전입니다.

## 데이터 저장
- Google Sheets 사용 안 함
- Apps Script 사용 안 함
- Room(SQLite) 로컬 DB
- 인터넷 없이 기본 기능 사용 가능

## 포함 기능
- 재무 대시보드
  - 총 자산
  - 총 부채
  - 순자산
  - 가용자산
  - 전월 대비 변화
  - 계좌별 현황
  - 월별 순자산 추이
- 월간 업데이트
  - 전월 잔액
  - 월 적립/상환
  - 조정금액
  - 현재 잔액
  - 메모
- 계좌 관리
  - 자산/부채 구분
  - 계좌 유형
  - 기관
  - 가용자산 포함 여부
  - 수정/삭제
- 계획
  - 계획 대비 실제
  - 수입/지출/저축/투자/상환
  - 재무 목표
  - 보수적/기본/낙관적 시나리오
  - 1년/3년 단순 순자산 시뮬레이션

## GitHub에서 APK 빌드
1. 새 Private Repository 생성
2. 이 프로젝트의 내용 전체 업로드
3. `.github/workflows/build-apk.yml`이 있는지 확인
4. Actions → `Build Finance Tracker APK`
5. Run workflow
6. 성공 후 Artifacts → `FinanceTracker-debug-apk`
7. 다운로드 후 `app-debug.apk` 설치

## 중요한 점
앱 삭제 시 로컬 DB가 사라질 수 있으므로, 다음 버전에서 JSON/Drive 백업 기능 추가를 권장합니다.

기존 Google Sheets의 과거 재무 데이터를 자동으로 가져오지는 않습니다.
기존 데이터를 옮기려면 CSV/JSON Import 기능을 추가할 수 있습니다.

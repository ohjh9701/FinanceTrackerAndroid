# 기존 Google Sheets → Android 로컬 DB 대응

기존 주요 시트:
- ACCOUNT_MASTER → accounts
- MONTHLY_BALANCE → monthly_balances
- MONTHLY_PLAN → monthly_plans
- GOALS → goals
- FINANCIAL_EVENTS → financial_events
- SCENARIOS → scenarios

대시보드 계산 원칙:
- 총 자산 = 자산 계좌의 현재 잔액 합계
- 총 부채 = 부채 계좌의 현재 잔액 합계
- 순자산 = 총 자산 - 총 부채
- 가용자산 = availableAsset=true인 자산 계좌의 현재 잔액 합계
- 월 저축 = 자산 계좌 monthlyContribution 합계
- 월 부채상환 = 부채 계좌 monthlyContribution 합계

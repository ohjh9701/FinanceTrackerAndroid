package com.finance.localtracker.data

class FinanceRepository(private val db: FinanceDatabase) {
    private val dao = db.dao()

    val accounts = dao.observeAccounts()
    val balances = dao.observeBalances()
    val plans = dao.observePlans()
    val goals = dao.observeGoals()
    val events = dao.observeEvents()
    val scenarios = dao.observeScenarios()

    suspend fun saveAccount(account: AccountEntity) {
        if (account.id == 0L) dao.insertAccount(account) else dao.updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) = dao.deleteAccount(account)

    suspend fun saveBalance(balance: MonthlyBalanceEntity) {
        dao.upsertBalance(balance.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun savePlan(plan: MonthlyPlanEntity) {
        if (plan.id == 0L) dao.insertPlan(plan) else dao.updatePlan(plan)
    }

    suspend fun deletePlan(plan: MonthlyPlanEntity) = dao.deletePlan(plan)

    suspend fun saveGoal(goal: GoalEntity) {
        if (goal.id == 0L) dao.insertGoal(goal) else dao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    suspend fun saveEvent(event: FinancialEventEntity) = dao.insertEvent(event)
    suspend fun deleteEvent(event: FinancialEventEntity) = dao.deleteEvent(event)
}

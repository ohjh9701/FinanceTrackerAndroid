package com.finance.localtracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM accounts ORDER BY balanceType, id")
    fun observeAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM monthly_balances ORDER BY monthKey, accountId")
    fun observeBalances(): Flow<List<MonthlyBalanceEntity>>

    @Query("SELECT * FROM monthly_plans ORDER BY monthKey, id")
    fun observePlans(): Flow<List<MonthlyPlanEntity>>

    @Query("SELECT * FROM goals ORDER BY active DESC, targetMonth, id")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM financial_events ORDER BY monthKey, id")
    fun observeEvents(): Flow<List<FinancialEventEntity>>

    @Query("SELECT * FROM scenarios ORDER BY code")
    fun observeScenarios(): Flow<List<ScenarioEntity>>

    @Insert
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalance(balance: MonthlyBalanceEntity): Long

    @Delete
    suspend fun deleteBalance(balance: MonthlyBalanceEntity)

    @Insert
    suspend fun insertPlan(plan: MonthlyPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: MonthlyPlanEntity)

    @Delete
    suspend fun deletePlan(plan: MonthlyPlanEntity)

    @Insert
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Insert
    suspend fun insertEvent(event: FinancialEventEntity): Long

    @Delete
    suspend fun deleteEvent(event: FinancialEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScenario(scenario: ScenarioEntity)
}

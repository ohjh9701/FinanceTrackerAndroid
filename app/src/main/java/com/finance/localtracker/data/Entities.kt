package com.finance.localtracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val accountType: String,
    val balanceType: String,      // 자산 / 부채
    val institution: String = "",
    val availableAsset: Boolean = false,
    val status: String = "사용",
    val memo: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "monthly_balances",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("accountId"),
        Index(value = ["monthKey", "accountId"], unique = true)
    ]
)
data class MonthlyBalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String,                // yyyy-MM
    val accountId: Long,
    val previousBalance: Long = 0,
    val monthlyContribution: Long = 0,
    val adjustmentAmount: Long = 0,
    val currentBalance: Long = 0,
    val memo: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "monthly_plans")
data class MonthlyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String,
    val category: String,
    val plannedAmount: Long,
    val actualAmount: Long = 0,
    val planType: String,                // 수입 / 지출 / 저축 / 투자 / 상환
    val memo: String = ""
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Long,
    val targetMonth: String,
    val goalType: String,                // 순자산 / 가용자산 / 부채감소 등
    val memo: String = "",
    val active: Boolean = true
)

@Entity(tableName = "financial_events")
data class FinancialEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String,
    val title: String,
    val amount: Long,
    val direction: String,               // 증가 / 감소
    val target: String,                  // 자산 / 부채 / 순자산
    val memo: String = ""
)

@Entity(tableName = "scenarios")
data class ScenarioEntity(
    @PrimaryKey val code: String,
    val name: String,
    val incomeGrowth: Double,
    val expenseGrowth: Double,
    val investmentReturn: Double,
    val inflation: Double,
    val memo: String = "",
    val active: Boolean = true
)

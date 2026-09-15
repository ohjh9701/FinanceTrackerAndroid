package com.finance.localtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        MonthlyBalanceEntity::class,
        MonthlyPlanEntity::class,
        GoalEntity::class,
        FinancialEventEntity::class,
        ScenarioEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun dao(): FinanceDao

    companion object {
        @Volatile private var INSTANCE: FinanceDatabase? = null

        fun getInstance(context: Context): FinanceDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_tracker.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).dao().apply {
                                upsertScenario(
                                    ScenarioEntity(
                                        code = "CONSERVATIVE",
                                        name = "보수적",
                                        incomeGrowth = 0.0,
                                        expenseGrowth = 0.03,
                                        investmentReturn = 0.02,
                                        inflation = 0.02,
                                        memo = "생활비는 높게, 투자수익률은 낮게 가정"
                                    )
                                )
                                upsertScenario(
                                    ScenarioEntity(
                                        code = "BASE",
                                        name = "기본",
                                        incomeGrowth = 0.02,
                                        expenseGrowth = 0.02,
                                        investmentReturn = 0.05,
                                        inflation = 0.025,
                                        memo = "현재 계획을 중심으로 계산"
                                    )
                                )
                                upsertScenario(
                                    ScenarioEntity(
                                        code = "OPTIMISTIC",
                                        name = "낙관적",
                                        incomeGrowth = 0.04,
                                        expenseGrowth = 0.015,
                                        investmentReturn = 0.08,
                                        inflation = 0.03,
                                        memo = "소득과 투자수익률을 비교적 높게 가정"
                                    )
                                )
                            }
                        }
                    }
                }).build().also { INSTANCE = it }
            }
    }
}

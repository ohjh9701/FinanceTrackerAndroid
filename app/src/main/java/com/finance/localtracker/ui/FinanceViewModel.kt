package com.finance.localtracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.finance.localtracker.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = FinanceRepository(FinanceDatabase.getInstance(application))

    val accounts = repo.accounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val balances = repo.balances.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val plans = repo.plans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val goals = repo.goals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val events = repo.events.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val scenarios = repo.scenarios.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveAccount(account: AccountEntity, cb: (Result<Unit>) -> Unit) = viewModelScope.launch {
        cb(runCatching { repo.saveAccount(account) })
    }
    fun deleteAccount(account: AccountEntity, cb: (Result<Unit>) -> Unit) = viewModelScope.launch {
        cb(runCatching { repo.deleteAccount(account) })
    }
    fun saveBalance(balance: MonthlyBalanceEntity, cb: (Result<Unit>) -> Unit) = viewModelScope.launch {
        cb(runCatching { repo.saveBalance(balance) })
    }
    fun savePlan(plan: MonthlyPlanEntity, cb: (Result<Unit>) -> Unit) = viewModelScope.launch {
        cb(runCatching { repo.savePlan(plan) })
    }
    fun deletePlan(plan: MonthlyPlanEntity, cb: (Result<Unit>) -> Unit) = viewModelScope.launch {
        cb(runCatching { repo.deletePlan(plan) })
    }
    fun saveGoal(goal: GoalEntity, cb: (Result<Unit>) -> Unit) = viewModelScope.launch {
        cb(runCatching { repo.saveGoal(goal) })
    }
    fun deleteGoal(goal: GoalEntity, cb: (Result<Unit>) -> Unit) = viewModelScope.launch {
        cb(runCatching { repo.deleteGoal(goal) })
    }
}

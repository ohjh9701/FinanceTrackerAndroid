package com.finance.localtracker.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finance.localtracker.data.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale
import kotlin.math.roundToLong

private enum class Screen(val label: String) {
    DASHBOARD("대시보드"),
    MONTHLY("월간"),
    ACCOUNTS("계좌"),
    PLAN("계획")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(viewModel: FinanceViewModel = viewModel()) {
    val accounts by viewModel.accounts.collectAsState()
    val balances by viewModel.balances.collectAsState()
    val plans by viewModel.plans.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val scenarios by viewModel.scenarios.collectAsState()

    var screen by rememberSaveable { mutableStateOf(Screen.DASHBOARD) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MY FINANCE", style = MaterialTheme.typography.labelSmall)
                        Text(screen.label, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { item ->
                    val icon = when(item) {
                        Screen.DASHBOARD -> Icons.Outlined.Home
                        Screen.MONTHLY -> Icons.Outlined.CalendarMonth
                        Screen.ACCOUNTS -> Icons.Outlined.AccountBalanceWallet
                        Screen.PLAN -> Icons.Outlined.Flag
                    }
                    NavigationBarItem(
                        selected = screen == item,
                        onClick = { screen = item },
                        icon = { Icon(icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when(screen) {
                Screen.DASHBOARD -> DashboardScreen(accounts, balances)
                Screen.MONTHLY -> MonthlyScreen(
                    accounts = accounts,
                    balances = balances,
                    onSave = { balance ->
                        viewModel.saveBalance(balance) { r ->
                            scope.launch {
                                snack.showSnackbar(r.fold({"저장했습니다."},{it.message ?: "저장 실패"}))
                            }
                        }
                    }
                )
                Screen.ACCOUNTS -> AccountsScreen(
                    accounts = accounts,
                    onSave = { account ->
                        viewModel.saveAccount(account) { r ->
                            scope.launch {
                                snack.showSnackbar(r.fold({"계좌를 저장했습니다."},{it.message ?: "저장 실패"}))
                            }
                        }
                    },
                    onDelete = { account ->
                        viewModel.deleteAccount(account) { r ->
                            scope.launch {
                                snack.showSnackbar(r.fold({"계좌를 삭제했습니다."},{it.message ?: "삭제 실패"}))
                            }
                        }
                    }
                )
                Screen.PLAN -> PlanningScreen(
                    plans = plans,
                    goals = goals,
                    balances = balances,
                    accounts = accounts,
                    scenarios = scenarios,
                    onSavePlan = { plan ->
                        viewModel.savePlan(plan) { r ->
                            scope.launch {
                                snack.showSnackbar(r.fold({"계획을 저장했습니다."},{it.message ?: "저장 실패"}))
                            }
                        }
                    },
                    onDeletePlan = { plan ->
                        viewModel.deletePlan(plan) { r ->
                            scope.launch {
                                snack.showSnackbar(r.fold({"계획을 삭제했습니다."},{it.message ?: "삭제 실패"}))
                            }
                        }
                    },
                    onSaveGoal = { goal ->
                        viewModel.saveGoal(goal) { r ->
                            scope.launch {
                                snack.showSnackbar(r.fold({"목표를 저장했습니다."},{it.message ?: "저장 실패"}))
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    accounts: List<AccountEntity>,
    balances: List<MonthlyBalanceEntity>
) {
    var month by rememberSaveable { mutableStateOf(latestMonthOrNow(balances)) }
    val current = balances.filter { it.monthKey == month }
    val rows = accounts.mapNotNull { a ->
        current.firstOrNull { it.accountId == a.id }?.let { a to it }
    }

    val assets = rows.filter { it.first.balanceType == "자산" }.sumOf { it.second.currentBalance }
    val liabilities = rows.filter { it.first.balanceType == "부채" }.sumOf { it.second.currentBalance }
    val netWorth = assets - liabilities
    val available = rows.filter { it.first.balanceType == "자산" && it.first.availableAsset }.sumOf { it.second.currentBalance }

    val allMonths = balances.map { it.monthKey }.distinct().sorted()
    val previousMonth = allMonths.lastOrNull { it < month }
    val prevRows = if (previousMonth == null) emptyList() else accounts.mapNotNull { a ->
        balances.firstOrNull { it.monthKey == previousMonth && it.accountId == a.id }?.let { a to it }
    }
    val prevAssets = prevRows.filter { it.first.balanceType == "자산" }.sumOf { it.second.currentBalance }
    val prevLiab = prevRows.filter { it.first.balanceType == "부채" }.sumOf { it.second.currentBalance }
    val prevNet = prevAssets - prevLiab

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { MonthNav(month, { month = shiftMonth(month, -1) }, { month = shiftMonth(month, 1) }) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("총 자산", won(assets), diffText(assets, prevAssets), Modifier.weight(1f))
                MetricCard("총 부채", won(liabilities), diffText(liabilities, prevLiab), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("순자산", won(netWorth), diffText(netWorth, prevNet), Modifier.weight(1f))
                MetricCard("가용자산", won(available), null, Modifier.weight(1f))
            }
        }

        item {
            SectionCard("계좌별 현황") {
                if (rows.isEmpty()) {
                    Text("이 월의 저장된 잔액이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    rows.sortedByDescending { kotlin.math.abs(it.second.currentBalance) }.forEach { (a,b) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(a.name, fontWeight = FontWeight.Bold)
                                Text("${a.institution} · ${a.accountType}", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(won(b.currentBalance), fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        item {
            SectionCard("월별 순자산 추이") {
                val months = balances.map { it.monthKey }.distinct().sorted().takeLast(13)
                if (months.isEmpty()) Text("데이터가 쌓이면 월별 추이가 표시됩니다.")
                else months.forEach { m ->
                    val monthRows = accounts.mapNotNull { a ->
                        balances.firstOrNull { it.monthKey == m && it.accountId == a.id }?.let { a to it }
                    }
                    val a = monthRows.filter { it.first.balanceType == "자산" }.sumOf { it.second.currentBalance }
                    val l = monthRows.filter { it.first.balanceType == "부채" }.sumOf { it.second.currentBalance }
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(m, style = MaterialTheme.typography.labelMedium)
                        Text(won(a-l), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyScreen(
    accounts: List<AccountEntity>,
    balances: List<MonthlyBalanceEntity>,
    onSave: (MonthlyBalanceEntity) -> Unit
) {
    var month by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val activeAccounts = accounts.filter { it.status == "사용" }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { MonthNav(month, { month = shiftMonth(month, -1) }, { month = shiftMonth(month, 1) }) }

        if (activeAccounts.isEmpty()) {
            item {
                SectionCard("월간 업데이트") {
                    Text("먼저 계좌 메뉴에서 자산/부채 계좌를 등록해주세요.")
                }
            }
        }

        items(activeAccounts, key = { it.id }) { account ->
            val existing = balances.firstOrNull { it.monthKey == month && it.accountId == account.id }
            BalanceInputCard(
                month = month,
                account = account,
                existing = existing,
                onSave = onSave
            )
        }
    }
}

@Composable
private fun BalanceInputCard(
    month: String,
    account: AccountEntity,
    existing: MonthlyBalanceEntity?,
    onSave: (MonthlyBalanceEntity) -> Unit
) {
    var previous by remember(existing?.id, month) { mutableStateOf((existing?.previousBalance ?: 0).toString()) }
    var contribution by remember(existing?.id, month) { mutableStateOf((existing?.monthlyContribution ?: 0).toString()) }
    var adjustment by remember(existing?.id, month) { mutableStateOf((existing?.adjustmentAmount ?: 0).toString()) }
    var current by remember(existing?.id, month) { mutableStateOf((existing?.currentBalance ?: 0).toString()) }
    var memo by remember(existing?.id, month) { mutableStateOf(existing?.memo ?: "") }

    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(account.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("${account.balanceType} · ${account.accountType} · ${account.institution}", style = MaterialTheme.typography.labelSmall)

            MoneyField("전월 잔액", previous) { previous = it }
            MoneyField(if (account.balanceType == "자산") "이번 달 적립/투입" else "이번 달 상환", contribution) { contribution = it }
            MoneyField("조정금액", adjustment) { adjustment = it }
            MoneyField("현재 잔액", current) { current = it }

            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                label = { Text("메모") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    onSave(
                        MonthlyBalanceEntity(
                            id = existing?.id ?: 0,
                            monthKey = month,
                            accountId = account.id,
                            previousBalance = previous.toLongOrNull() ?: 0,
                            monthlyContribution = contribution.toLongOrNull() ?: 0,
                            adjustmentAmount = adjustment.toLongOrNull() ?: 0,
                            currentBalance = current.toLongOrNull() ?: 0,
                            memo = memo
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("저장") }
        }
    }
}

@Composable
private fun AccountsScreen(
    accounts: List<AccountEntity>,
    onSave: (AccountEntity) -> Unit,
    onDelete: (AccountEntity) -> Unit
) {
    var editing by remember { mutableStateOf<AccountEntity?>(null) }
    var showForm by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(
                onClick = { editing = null; showForm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("계좌 추가")
            }
        }

        items(accounts, key = { it.id }) { a ->
            Card {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(a.name, fontWeight = FontWeight.Bold)
                        Text("${a.balanceType} · ${a.accountType} · ${a.institution}", style = MaterialTheme.typography.labelSmall)
                        Text(if (a.availableAsset) "가용자산 포함" else "가용자산 제외", style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { editing = a; showForm = true }) { Icon(Icons.Outlined.Edit, "수정") }
                    IconButton(onClick = { onDelete(a) }) { Icon(Icons.Outlined.Delete, "삭제") }
                }
            }
        }
    }

    if (showForm) {
        AccountDialog(
            account = editing,
            onDismiss = { showForm = false },
            onSave = { onSave(it); showForm = false }
        )
    }
}

@Composable
private fun AccountDialog(
    account: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var type by remember { mutableStateOf(account?.accountType ?: "예금") }
    var balanceType by remember { mutableStateOf(account?.balanceType ?: "자산") }
    var institution by remember { mutableStateOf(account?.institution ?: "") }
    var available by remember { mutableStateOf(account?.availableAsset ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "계좌 추가" else "계좌 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, {name=it}, label={Text("계좌명")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(institution, {institution=it}, label={Text("기관")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(type, {type=it}, label={Text("계좌 유형")}, modifier=Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected=balanceType=="자산", onClick={balanceType="자산"}, label={Text("자산")})
                    FilterChip(selected=balanceType=="부채", onClick={balanceType="부채"}, label={Text("부채")})
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked=available, onCheckedChange={available=it})
                    Text("가용자산에 포함")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        AccountEntity(
                            id = account?.id ?: 0,
                            name = name.trim(),
                            accountType = type.trim(),
                            balanceType = balanceType,
                            institution = institution.trim(),
                            availableAsset = available,
                            status = account?.status ?: "사용",
                            memo = account?.memo ?: "",
                            createdAt = account?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                }
            }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick=onDismiss){Text("취소")} }
    )
}

@Composable
private fun PlanningScreen(
    plans: List<MonthlyPlanEntity>,
    goals: List<GoalEntity>,
    balances: List<MonthlyBalanceEntity>,
    accounts: List<AccountEntity>,
    scenarios: List<ScenarioEntity>,
    onSavePlan: (MonthlyPlanEntity) -> Unit,
    onDeletePlan: (MonthlyPlanEntity) -> Unit,
    onSaveGoal: (GoalEntity) -> Unit
) {
    var month by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var showPlanForm by remember { mutableStateOf(false) }
    var showGoalForm by remember { mutableStateOf(false) }
    var selectedScenario by rememberSaveable { mutableStateOf("BASE") }

    val monthPlans = plans.filter { it.monthKey == month }
    val totalPlanned = monthPlans.sumOf { it.plannedAmount }
    val totalActual = monthPlans.sumOf { it.actualAmount }
    val currentNetWorth = latestNetWorth(accounts, balances)
    val scenario = scenarios.firstOrNull { it.code == selectedScenario }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { MonthNav(month, {month=shiftMonth(month,-1)}, {month=shiftMonth(month,1)}) }

        item {
            SectionCard("계획 대비 실제") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("계획 합계", won(totalPlanned), null, Modifier.weight(1f))
                    MetricCard("실제 합계", won(totalActual), diffText(totalActual,totalPlanned), Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick={showPlanForm=true}, modifier=Modifier.fillMaxWidth()){Text("계획 항목 추가")}
            }
        }

        items(monthPlans) { p ->
            Card {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.category, fontWeight=FontWeight.Bold)
                        Text("${p.planType} · 계획 ${won(p.plannedAmount)} · 실제 ${won(p.actualAmount)}", style=MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick={onDeletePlan(p)}) { Icon(Icons.Outlined.Delete, null) }
                }
            }
        }

        item {
            SectionCard("목표") {
                if (goals.isEmpty()) Text("등록된 목표가 없습니다.")
                goals.filter { it.active }.forEach { g ->
                    val progress = if (g.targetAmount > 0) {
                        (currentNetWorth.toFloat() / g.targetAmount.toFloat()).coerceIn(0f,1f)
                    } else 0f
                    Text(g.title, fontWeight=FontWeight.Bold)
                    Text("${won(currentNetWorth)} / ${won(g.targetAmount)} · ${g.targetMonth}", style=MaterialTheme.typography.labelSmall)
                    LinearProgressIndicator(progress={progress}, modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
                Button(onClick={showGoalForm=true}, modifier=Modifier.fillMaxWidth()){Text("목표 추가")}
            }
        }

        item {
            SectionCard("목표 시뮬레이션") {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    scenarios.filter { it.active }.forEach { s ->
                        FilterChip(
                            selected = selectedScenario == s.code,
                            onClick = { selectedScenario = s.code },
                            label = { Text(s.name) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                scenario?.let { s ->
                    Text("투자수익률 ${(s.investmentReturn*100).roundToLong()}% · 소득증가 ${(s.incomeGrowth*100).roundToLong()}% · 지출증가 ${(s.expenseGrowth*100).roundToLong()}%")
                    Spacer(Modifier.height(8.dp))
                    val monthlyNetAdd = monthPlans.filter { it.planType in setOf("저축","투자","상환") }.sumOf { it.plannedAmount }
                    val oneYear = ((currentNetWorth + monthlyNetAdd * 12) * (1.0 + s.investmentReturn)).roundToLong()
                    val threeYear = ((currentNetWorth + monthlyNetAdd * 36) * Math.pow(1.0 + s.investmentReturn, 3.0)).roundToLong()
                    Text("1년 후 예상 순자산: ${won(oneYear)}", fontWeight=FontWeight.Bold)
                    Text("3년 후 예상 순자산: ${won(threeYear)}", fontWeight=FontWeight.Bold)
                    Text("※ 단순 추정치이며 실제 수익률·현금흐름과 다를 수 있습니다.", style=MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    if (showPlanForm) {
        PlanDialog(month, onDismiss={showPlanForm=false}, onSave={onSavePlan(it);showPlanForm=false})
    }
    if (showGoalForm) {
        GoalDialog(onDismiss={showGoalForm=false}, onSave={onSaveGoal(it);showGoalForm=false})
    }
}

@Composable
private fun PlanDialog(month: String, onDismiss:()->Unit, onSave:(MonthlyPlanEntity)->Unit) {
    var category by remember { mutableStateOf("") }
    var planned by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("저축") }

    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("계획 항목 추가")},
        text={
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(category,{category=it},label={Text("항목명")},modifier=Modifier.fillMaxWidth())
                MoneyField("계획 금액", planned){planned=it}
                MoneyField("실제 금액", actual){actual=it}
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    listOf("수입","지출","저축","투자","상환").forEach {
                        FilterChip(selected=type==it,onClick={type=it},label={Text(it)})
                    }
                }
            }
        },
        confirmButton={TextButton(onClick={
            if(category.isNotBlank()) onSave(MonthlyPlanEntity(monthKey=month,category=category,plannedAmount=planned.toLongOrNull()?:0,actualAmount=actual.toLongOrNull()?:0,planType=type))
        }){Text("저장")}},
        dismissButton={TextButton(onClick=onDismiss){Text("취소")}}
    )
}

@Composable
private fun GoalDialog(onDismiss:()->Unit, onSave:(GoalEntity)->Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var month by remember { mutableStateOf(YearMonth.now().plusYears(1).toString()) }

    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("목표 추가")},
        text={
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title,{title=it},label={Text("목표명")},modifier=Modifier.fillMaxWidth())
                MoneyField("목표 금액", amount){amount=it}
                OutlinedTextField(month,{month=it},label={Text("목표 월 (yyyy-MM)")},modifier=Modifier.fillMaxWidth())
            }
        },
        confirmButton={TextButton(onClick={
            if(title.isNotBlank()) onSave(GoalEntity(title=title,targetAmount=amount.toLongOrNull()?:0,targetMonth=month,goalType="순자산"))
        }){Text("저장")}},
        dismissButton={TextButton(onClick=onDismiss){Text("취소")}}
    )
}

@Composable
private fun MetricCard(label:String, value:String, sub:String?, modifier:Modifier=Modifier) {
    Card(modifier=modifier, shape=RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style=MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, style=MaterialTheme.typography.titleLarge, fontWeight=FontWeight.Black)
            if(sub!=null) Text(sub, style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionCard(title:String, content:@Composable ()->Unit) {
    Card(shape=RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun MonthNav(month:String,onPrev:()->Unit,onNext:()->Unit) {
    val ym = YearMonth.parse(month)
    Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
        TextButton(onClick=onPrev){Text("‹")}
        Text("${ym.year}년 ${ym.monthValue}월", fontWeight=FontWeight.Bold)
        TextButton(onClick=onNext){Text("›")}
    }
}

@Composable
private fun MoneyField(label:String,value:String,onValue:(String)->Unit) {
    OutlinedTextField(
        value=value,
        onValueChange={onValue(it.filter { ch -> ch.isDigit() || ch=='-' })},
        label={Text(label)},
        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
        modifier=Modifier.fillMaxWidth(),
        singleLine=true
    )
}

private fun won(v:Long):String = NumberFormat.getCurrencyInstance(Locale.KOREA).format(v)
private fun diffText(v:Long,p:Long):String {
    val d=v-p
    return when {
        d>0 -> "전월 대비 +${won(d)}"
        d<0 -> "전월 대비 ${won(d)}"
        else -> "전월과 동일"
    }
}
private fun shiftMonth(m:String,d:Long):String = YearMonth.parse(m).plusMonths(d).toString()
private fun latestMonthOrNow(b:List<MonthlyBalanceEntity>):String = b.maxOfOrNull { it.monthKey } ?: YearMonth.now().toString()
private fun latestNetWorth(a:List<AccountEntity>, b:List<MonthlyBalanceEntity>):Long {
    val m=b.maxOfOrNull { it.monthKey } ?: return 0
    val rows=a.mapNotNull { ac -> b.firstOrNull { it.monthKey==m && it.accountId==ac.id }?.let { ac to it } }
    return rows.filter { it.first.balanceType=="자산" }.sumOf { it.second.currentBalance } -
        rows.filter { it.first.balanceType=="부채" }.sumOf { it.second.currentBalance }
}

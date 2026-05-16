package ru.cashflow.statement.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class Screen { STATEMENT, PROFILE, HISTORY, ADV202, FAST_TRACK, HELP }

@Composable
fun CashflowApp() {
    val vm: StatementViewModel = viewModel()
    var screen by remember { mutableStateOf(Screen.STATEMENT) }

    if (screen != Screen.STATEMENT) {
        BackHandler { screen = Screen.STATEMENT }
    }

    when (screen) {
        Screen.STATEMENT -> StatementScreen(
            vm = vm,
            onOpenProfile = { screen = Screen.PROFILE },
            onOpenHistory = { screen = Screen.HISTORY },
            onOpen202 = { screen = Screen.ADV202 },
            onOpenFastTrack = { screen = Screen.FAST_TRACK },
            onOpenHelp = { screen = Screen.HELP },
        )
        Screen.PROFILE -> ProfileScreen(vm) { screen = Screen.STATEMENT }
        Screen.HISTORY -> HistoryScreen(vm) { screen = Screen.STATEMENT }
        Screen.ADV202 -> Advanced202Screen(vm) { screen = Screen.STATEMENT }
        Screen.FAST_TRACK -> FastTrackScreen(vm) { screen = Screen.STATEMENT }
        Screen.HELP -> HelpScreen { screen = Screen.STATEMENT }
    }
}

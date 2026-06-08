package ru.cashflow.statement.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.cashflow.statement.ads.LocalAdController

private enum class Screen { STATEMENT, PROFILE, HISTORY, ADV202, FAST_TRACK, HELP }

@Composable
fun CashflowApp() {
    val vm: StatementViewModel = viewModel()
    val ad = LocalAdController.current
    val activity = LocalContext.current as? Activity
    var screen by remember { mutableStateOf(Screen.STATEMENT) }

    LaunchedEffect(vm.lastEvent?.id) {
        if (vm.lastEvent != null) {
            ad.onMoveCompleted(activity)
        }
    }

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

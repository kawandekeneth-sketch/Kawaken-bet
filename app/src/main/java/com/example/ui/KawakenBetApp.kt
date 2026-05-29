package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BetRecord
import com.example.data.UserAccount
import com.example.ui.theme.*
import com.example.viewmodel.KawakenBetViewModel
import com.example.viewmodel.SelectedSportBet
import com.example.viewmodel.SportMatch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Util to format currency shillings beautifully
fun formatShillings(amount: Double, currency: String): String {
    val formattedVal = String.format("%,.0f", amount)
    return "$formattedVal $currency"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KawakenBetApp(
    viewModel: KawakenBetViewModel,
    modifier: Modifier = Modifier
) {
    val accountState by viewModel.userAccount.collectAsStateWithLifecycle()
    val allBetsState by viewModel.allBets.collectAsStateWithLifecycle()
    val matchesState by viewModel.liveMatches.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("SPORTS") } // SPORTS, CASINO, MOBILE_PAY, MY_BETS

    val currentCurrency = accountState?.preferredCurrency ?: "UGX"
    val accountBalance = accountState?.balance ?: 100000.0
    val usernameDisplay = accountState?.username ?: "kawaken"
    val currentMobile = accountState?.mobileNumber ?: "+256 772 000 001"
    val currentProvider = accountState?.mobileProvider ?: "MTN MoMo"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.background(DarkGreenBg)) {
                TopAppBar(
                    title = {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🏆 KAWAKEN BET",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.testTag("app_logo_title")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = NeonSuccess.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(1.dp, NeonSuccess.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "bet.site.com",
                                        color = NeonSuccess,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Interactive Sportsbook & Casino Shillings Money v1.2",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    actions = {
                        // Wallet Pill
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(
                                    color = DarkGreenSurface,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Wallet",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatShillings(accountBalance, currentCurrency),
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkGreenBg,
                        titleContentColor = TextPrimary
                    )
                )
                HorizontalDivider(color = CardBorder, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = CardBorder, thickness = 1.dp)
                NavigationBar(
                    containerColor = DarkGreenBg,
                    tonalElevation = 6.dp
                ) {
                    val tabs = listOf(
                        Triple("SPORTS", "Sports ⚽", "sports_tab"),
                        Triple("CASINO", "Casino 🎰", "casino_tab"),
                        Triple("MOBILE_PAY", "Mobile Pay 📱", "mobile_pay_tab"),
                        Triple("MY_BETS", "Ledger 📝", "my_bets_tab")
                    )

                    tabs.forEach { (tabId, label, tag) ->
                        val isSelected = activeTab == tabId
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tabId },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GoldPrimary else TextSecondary,
                                    fontSize = 11.sp
                                )
                            },
                            icon = {
                                val iconText = when (tabId) {
                                    "SPORTS" -> "⚽"
                                    "CASINO" -> "🎰"
                                    "MOBILE_PAY" -> "📱"
                                    else -> "📝"
                                }
                                Text(text = iconText, fontSize = 18.sp)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoldPrimary,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = DarkGreenSurface,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }
            }
        },
        containerColor = DarkGreenBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (activeTab) {
                "SPORTS" -> SportsScreen(
                    matches = matchesState,
                    activeSlip = viewModel.activeBetSlip,
                    stakeInput = viewModel.betSlipStakeInput,
                    onOddsSelected = { match, selection, odds ->
                        viewModel.selectBetOption(match, selection, odds)
                    },
                    onStakeChanged = { viewModel.betSlipStakeInput = it },
                    onPlaceBet = { viewModel.placeSportsBet() },
                    onCloseSlip = { viewModel.closeBetSlip() },
                    userBalance = accountBalance,
                    currency = currentCurrency
                )
                "CASINO" -> CasinoScreen(viewModel = viewModel, currency = currentCurrency)
                "MOBILE_PAY" -> MobileMoneyScreen(
                    viewModel = viewModel,
                    username = usernameDisplay,
                    balance = accountBalance,
                    currency = currentCurrency,
                    currentMobile = currentMobile,
                    currentProvider = currentProvider
                )
                "MY_BETS" -> MyBetsScreen(bets = allBetsState, currency = currentCurrency, onClear = { viewModel.clearBetHistory() })
            }
        }
    }
}

// -------------------------------------------------------------
// 1. SPORTS SCREEN (Live Football Matches & Custom Slip)
// -------------------------------------------------------------
@Composable
fun SportsScreen(
    matches: List<SportMatch>,
    activeSlip: SelectedSportBet?,
    stakeInput: String,
    onOddsSelected: (SportMatch, String, Double) -> Unit,
    onStakeChanged: (String) -> Unit,
    onPlaceBet: () -> Unit,
    onCloseSlip: () -> Unit,
    userBalance: Double,
    currency: String
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "Champions League", "Premier League", "La Liga")

    val filteredMatches = if (selectedFilter == "ALL") {
        matches
    } else {
        matches.filter { it.league == selectedFilter }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Horizontal league filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGreenSurface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) GoldPrimary else CardBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) DarkGreenBg else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Promotional banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(DarkGreenSurface, CardBorder)
                        )
                    )
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "🔥 BOOSTER PACK ACTIVE",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Double winnings applied dynamically on all virtual crash aviator game rounds and sportsbook match multipliers!",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            // Matches Lazy List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                item {
                    Text(
                        text = "⚽ LIVE & UPCOMING matches",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (filteredMatches.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkGreenSurface)
                        ) {
                            Text(
                                text = "Matches in $selectedFilter league are scheduled soon. Select 'ALL' to view real-time live events!",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredMatches) { match ->
                        SportMatchItem(
                            match = match,
                            onOddsSelected = onOddsSelected,
                            currentSlipSelection = activeSlip
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Active Bet Slip sliding bottom panel
        AnimatedVisibility(
            visible = activeSlip != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (activeSlip != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            brush = Brush.verticalGradient(listOf(GoldPrimary, DarkGreenBg)),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📑 SPORTS BET SLIP",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = onCloseSlip, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Match Bet Details
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeSlip.match.homeTeam} vs ${activeSlip.match.awayTeam}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Your Pick: " + when(activeSlip.selection) {
                                        "HOME" -> "${activeSlip.match.homeTeam} Win"
                                        "AWAY" -> "${activeSlip.match.awayTeam} Win"
                                        else -> "Draw Match"
                                    },
                                    color = NeonSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "ODDS MULTIPLIER", color = TextSecondary, fontSize = 9.sp)
                                Text(
                                    text = "@ " + activeSlip.odds.toString(),
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 10.dp))

                        val stakeVal = stakeInput.toDoubleOrNull() ?: 0.0
                        val potentialPayout = stakeVal * activeSlip.odds
                        val isOverBalance = stakeVal > userBalance

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.5f)) {
                                Text(text = "STAKE AMOUNT ($currency)", color = TextSecondary, fontSize = 11.sp)
                                OutlinedTextField(
                                    value = stakeInput,
                                    onValueChange = onStakeChanged,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = CardBorder,
                                        cursorColor = GoldPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(50.dp)
                                        .testTag("bet_stake_input")
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(0.5f)) {
                                Text(text = "ESTIMATED PAYOUT", color = TextSecondary, fontSize = 11.sp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(50.dp)
                                        .background(CardBorder, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = formatShillings(potentialPayout, currency),
                                        color = NeonSuccess,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Presets of Stakes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val presets = listOf("1000", "5000", "10000", "50000")
                            presets.forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .background(CardBorder, RoundedCornerShape(4.dp))
                                        .clickable { onStakeChanged(preset) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "+" + preset, color = TextPrimary, fontSize = 11.sp)
                                }
                            }
                        }

                        // Place Button
                        Button(
                            onClick = onPlaceBet,
                            enabled = !isOverBalance && stakeVal > 0.0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = DarkGreenBg,
                                disabledContainerColor = CardBorder,
                                disabledContentColor = TextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("place_bet_button")
                        ) {
                            Text(
                                text = if (isOverBalance) "INSUFFICIENT SHILLINGS" else "CONFIRM MULTIPLIED PLACE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SportMatchItem(
    match: SportMatch,
    onOddsSelected: (SportMatch, String, Double) -> Unit,
    currentSlipSelection: SelectedSportBet?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // League and live minute ticker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.league,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (match.isLive) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "pulse"
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(NeonDanger.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE ${match.minute}'",
                            color = NeonDanger,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (match.isFinished) {
                    Text(
                        text = "FT COMPLETED",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "STARTS SOON",
                        color = AccentBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scoreboard UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.homeTeam,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .background(DarkGreenBg, RoundedCornerShape(4.dp))
                        .padding(vertical = 4.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.homeScore.toString(),
                        color = if (match.isLive) NeonSuccess else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " - ",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = match.awayScore.toString(),
                        color = if (match.isLive) NeonSuccess else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = match.awayTeam,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Betting Options Buttons or Results
            if (!match.isFinished) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("HOME", "DRAW", "AWAY").forEach { pick ->
                        val odd = when (pick) {
                            "HOME" -> match.homeOdds
                            "DRAW" -> match.drawOdds
                            else -> match.awayOdds
                        }

                        val isSelected = currentSlipSelection?.match?.id == match.id && currentSlipSelection?.selection == pick

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) GoldPrimary else DarkGreenBg,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) GoldPrimary else CardBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onOddsSelected(match, pick, odd) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when(pick) {
                                        "HOME" -> "Home Win (1)"
                                        "DRAW" -> "Draw Match (X)"
                                        else -> "Away Win (2)"
                                    },
                                    color = if (isSelected) DarkGreenBg else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = odd.toString(),
                                    color = if (isSelected) DarkGreenBg else GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            } else {
                val outcomeLabel = when {
                    match.homeScore > match.awayScore -> "${match.homeTeam} Won (1)"
                    match.awayScore > match.homeScore -> "${match.awayTeam} Won (2)"
                    else -> "Draw Match (X)"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔔 Ended: $outcomeLabel. Sportsbook tickets settled instantly.",
                        color = NeonSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. CASINO HUB Screen (VIRTUAL / CASUAL GAMES)
// -------------------------------------------------------------
@Composable
fun CasinoScreen(
    viewModel: KawakenBetViewModel,
    currency: String
) {
    var activeGame by remember { mutableStateOf("HUB") } // HUB, CRASH, SLOTS, WHEEL

    when (activeGame) {
        "HUB" -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                item {
                    Text(
                        text = "🎰 MULTIPLIER CASINO & VARIETY GAMES",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                item {
                    CasinoHubCard(
                        title = "✈️ AVIATOR CRASH GAME",
                        description = "Take off with the lucky jet plane! Watch your shillings rise. Click cashout before the plane flies off or crashes to win big multiplier payouts.",
                        accent = NeonDanger,
                        emoji = "✈️",
                        onPlay = { activeGame = "CRASH" }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    CasinoHubCard(
                        title = "🍒 GOLD REELS VERTICAL SLOT",
                        description = "Simulate real gaming machine action! Line up Cherries, Lemons, Bells, or triple Gold 7s to earn secure multipliers up to 50x.",
                        accent = GoldPrimary,
                        emoji = "🍒",
                        onPlay = { activeGame = "SLOTS" }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    CasinoHubCard(
                        title = "🎡 WHEEL OF SHILLING MULTIPLIERS",
                        description = "Spin the physical color wheel segment board. Earn guaranteed rewards from 1.5x up to 10.0x stakes instantaneously.",
                        accent = AccentBlue,
                        emoji = "🎡",
                        onPlay = { activeGame = "WHEEL" }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        "CRASH" -> CrashGameView(viewModel = viewModel, currency = currency, onBack = { activeGame = "HUB" })
        "SLOTS" -> SlotsGameView(viewModel = viewModel, currency = currency, onBack = { activeGame = "HUB" })
        "WHEEL" -> WheelGameView(viewModel = viewModel, currency = currency, onBack = { activeGame = "HUB" })
    }
}

@Composable
fun CasinoHubCard(
    title: String,
    description: String,
    accent: Color,
    emoji: String,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, accent, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "START PLAYING NOW",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "play",
                        tint = GoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// -- CASINO GAME: AVIATOR CRASH --
@Composable
fun CrashGameView(
    viewModel: KawakenBetViewModel,
    currency: String,
    onBack: () -> Unit
) {
    var stakeText by remember { mutableStateOf(viewModel.crashStakeAmount.toInt().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back", tint = TextPrimary)
            }
            Text(
                text = "✈️ AVIATOR CRASH FLIGHT",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Virtual Radar Flight visual screen representation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B09)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Drawing nice background lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val segments = 5
                    for (i in 1..segments) {
                        val x = (size.width / segments) * i
                        val y = (size.height / segments) * i
                        drawLine(color = CardBorder.copy(alpha = 0.5f), start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
                        drawLine(color = CardBorder.copy(alpha = 0.5f), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (viewModel.isCrashBusted) {
                        Text(
                            text = "💥 FLEW AWAY 💥",
                            color = NeonDanger,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bust Point: ${viewModel.currentCrashMultiplier}x",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        val colorProgress = if (viewModel.isCrashFlying) NeonSuccess else GoldPrimary
                        Text(
                            text = String.format("%.2f", viewModel.currentCrashMultiplier) + "x",
                            color = colorProgress,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        if (viewModel.isCrashFlying) {
                            Text(
                                text = "📈 MULTIPLIER GAIN IN PROGRESS...",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "READY FOR TAKE OFF",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Small flying jet graphic positioning
                if (viewModel.isCrashFlying) {
                    val angleOffset = (viewModel.currentCrashMultiplier - 1.0).coerceIn(0.0, 5.0) / 5.0
                    val floatOffsetMultiplierX = (angleOffset * 180).toFloat()
                    val floatOffsetMultiplierY = (angleOffset * 100).toFloat()

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 10.dp + floatOffsetMultiplierX.dp, y = (-10).dp - floatOffsetMultiplierY.dp)
                    ) {
                        Text(text = "✈️", fontSize = 28.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Crash controls log card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "CONSOLE LOGS:",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.crashLogMsg,
                    color = if (viewModel.isCrashBusted) NeonDanger else if (viewModel.hasCashedOutCrash) NeonSuccess else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interaction card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = stakeText,
                onValueChange = {
                    stakeText = it
                    it.toDoubleOrNull()?.let { d -> viewModel.crashStakeAmount = d }
                },
                label = { Text("Stake ($currency)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(0.5f)
                    .height(60.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = GoldPrimary
                ),
                enabled = !viewModel.isCrashFlying
            )

            Spacer(modifier = Modifier.width(12.dp))

            if (!viewModel.isCrashFlying) {
                Button(
                    onClick = { viewModel.placeAndStartCrashFly() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkGreenBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.5f)
                        .height(52.dp)
                ) {
                    Text(text = "🚀 FLY JET", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = { viewModel.cashOutCrash() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonSuccess, contentColor = DarkGreenBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.5f)
                        .height(52.dp),
                    enabled = !viewModel.hasCashedOutCrash
                ) {
                    val earnAmount = viewModel.crashStakeAmount * viewModel.currentCrashMultiplier
                    Text(
                        text = "💰 CASH OUT (Sh ${earnAmount.toInt()})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// -- CASINO GAME: SLOTS --
@Composable
fun SlotsGameView(
    viewModel: KawakenBetViewModel,
    currency: String,
    onBack: () -> Unit
) {
    var stakeText by remember { mutableStateOf(viewModel.slotsStakeAmount.toInt().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back", tint = TextPrimary)
            }
            Text(
                text = "🍒 GOLD REELS REVOLUTION",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Slots Arena Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldPrimary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⭐ THREE COLUMN MATCH ⭐",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    viewModel.currentSlotsReels.forEachIndexed { index, symbol ->
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkGreenBg)
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = symbol, fontSize = 36.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = viewModel.slotsWinResultMsg,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = stakeText,
                onValueChange = {
                    stakeText = it
                    it.toDoubleOrNull()?.let { d -> viewModel.slotsStakeAmount = d }
                },
                label = { Text("Stake ($currency)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = GoldPrimary
                ),
                enabled = !viewModel.isSlotsSpinning
            )

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { viewModel.spinSlots() },
                enabled = !viewModel.isSlotsSpinning,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkGreenBg),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (viewModel.isSlotsSpinning) "SPINNING..." else "🎰 SPIN SLOTS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// -- CASINO GAME: HIGH SPEED WHEEL --
@Composable
fun WheelGameView(
    viewModel: KawakenBetViewModel,
    currency: String,
    onBack: () -> Unit
) {
    var stakeText by remember { mutableStateOf(viewModel.wheelStakeAmount.toInt().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back", tint = TextPrimary)
            }
            Text(
                text = "🎡 WHEEL OF MULTIPLIERS",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📍 TICKER INDICATOR ARROW",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                // Pointer drawn
                Text(text = "▼", fontSize = 28.sp, color = GoldPrimary)

                Spacer(modifier = Modifier.height(8.dp))

                // Physical spinning disc representation
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(80.dp))
                        .background(Color.Black)
                        .rotate(viewModel.wheelDrawnAngle), // bound angle rotation to VM state
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val segmentAngle = 360f / 8f
                        for (i in 0..7) {
                            val colorIndex = i % viewModel.wheelColours.size
                            val color = Color(viewModel.wheelColours[colorIndex])
                            drawArc(
                                color = color,
                                startAngle = i * segmentAngle,
                                sweepAngle = segmentAngle,
                                useCenter = true
                            )
                        }
                    }

                    // Circle labels
                    for (i in 0..7) {
                        val targetRad = (i * 45 + 22.5) * (PI / 180.0)
                        val r = 50.0
                        val offsetX = (r * cos(targetRad)).toFloat()
                        val offsetY = (r * sin(targetRad)).toFloat()

                        Box(
                            modifier = Modifier.offset(x = offsetX.dp, y = offsetY.dp)
                        ) {
                            Text(
                                text = "${viewModel.wheelMultipliers[i]}x",
                                color = DarkGreenBg,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = viewModel.wheelResultMsg,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = stakeText,
                onValueChange = {
                    stakeText = it
                    it.toDoubleOrNull()?.let { d -> viewModel.wheelStakeAmount = d }
                },
                label = { Text("Stake ($currency)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = GoldPrimary
                ),
                enabled = !viewModel.isWheelSpinning
            )

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { viewModel.spinLuckyWheel() },
                enabled = !viewModel.isWheelSpinning,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkGreenBg),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (viewModel.isWheelSpinning) "SPINNING..." else "🎡 SPIN WHEEL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. MOBILE MONEY payout desk & account manager
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MobileMoneyScreen(
    viewModel: KawakenBetViewModel,
    username: String,
    balance: Double,
    currency: String,
    currentMobile: String,
    currentProvider: String
) {
    var mobileInput by remember { mutableStateOf(currentMobile) }
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var amountInput by remember { mutableStateOf("25000") }

    val providers = listOf("MTN MoMo", "Airtel Money", "Safaricom M-Pesa")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        item {
            Text(
                text = "📱 MOBILE MONEY INTEGRATION HANDLER",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Add and correct your payment details below to initiate instant winner shillings payouts.",
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Active Payment Destination Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(6.dp)
                        ) {
                            Text(text = "🛡️", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "REGISTERED OUTLET DESK",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Account Username:  kawaken",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mobile Pay Number: $currentMobile",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Network Operator:  $currentProvider",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Wallet Currency:    $currency (Shillings Money)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🔄 CORRECT / UPDATE PHONE ACCOUNT DETAILS",
                color = GoldPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Form to correct/update details
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Correct Mobile Account Number",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { mobileInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CardBorder,
                            cursorColor = GoldPrimary
                        ),
                        singleLine = true,
                        placeholder = { Text("+256 ...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Network Operator Partner",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Selector row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        providers.forEach { provider ->
                            val isSelected = selectedProvider == provider
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) GoldPrimary else DarkGreenBg,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GoldPrimary else CardBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedProvider = provider }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = provider,
                                    color = if (isSelected) DarkGreenBg else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.updateMobileMoneyAccount(mobileInput, selectedProvider)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = TextPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(text = "💾 SAVE CORRECTED NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "💸 TRANSACT FUNDS SIMULATOR",
                color = GoldPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Transaction simulation panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Enter Shillings amount",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CardBorder,
                            cursorColor = GoldPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                amountInput.toDoubleOrNull()?.let { amt ->
                                    viewModel.depositMobileMoney(amt)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonSuccess, contentColor = DarkGreenBg),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(6.dp),
                            enabled = !viewModel.isMoMoProcessing
                        ) {
                            Text(text = "📥 DEPOSIT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                amountInput.toDoubleOrNull()?.let { amt ->
                                    viewModel.withdrawMobileMoney(amt)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkGreenBg),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(6.dp),
                            enabled = !viewModel.isMoMoProcessing
                        ) {
                            Text(text = "📤 WITHDRAW", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.depositFreeShinnings() },
                        colors = ButtonDefaults.buttonColors(containerColor = CardBorder, contentColor = TextPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = "🎁 CLAIM FREE 50,000 Shs BOOST", fontSize = 11.sp)
                    }
                }
            }
        }

        // Network Activity Log Output
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "📡 TRANSACTION STATUS TERMINAL",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(6.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (viewModel.isMoMoProcessing) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    val txtColor = when (viewModel.isMoMoSuccess) {
                        true -> NeonSuccess
                        false -> NeonDanger
                        else -> TextPrimary
                    }

                    Text(
                        text = if (viewModel.moMomessage.isEmpty()) "IDLE: Ready to handle sports/casino mobile processing." else "LOG: " + viewModel.moMomessage,
                        color = txtColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. LEDGER SCREEN (Bets & Transctions History Logs)
// -------------------------------------------------------------
@Composable
fun MyBetsScreen(
    bets: List<BetRecord>,
    currency: String,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📝 TRANSPARENT WALLET AUDIT LEDGER",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "CLEAR ALL",
                color = NeonDanger,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .background(NeonDanger.copy(alpha = 0.1f))
                    .clickable { onClear() }
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (bets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No records logged! Transact with Mobile Money or place sports bets / casino rounds to populate.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(bets.reversed()) { record ->
                    BetRecordItem(record = record, currency = currency)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun BetRecordItem(record: BetRecord, currency: String) {
    val indicatorColor = when (record.outcome) {
        "WON" -> NeonSuccess
        "DEPOSIT" -> NeonSuccess
        "LOST" -> NeonDanger
        "WITHDRAW" -> GoldPrimary
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High visibility outcome block
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(indicatorColor.copy(alpha = 0.12f))
                    .border(1.dp, indicatorColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                val outcomeEmoji = when (record.outcome) {
                    "WON" -> "🏆"
                    "DEPOSIT" -> "📥"
                    "WITHDRAW" -> "📤"
                    "LOST" -> "❌"
                    else -> "⏳"
                }
                Text(text = outcomeEmoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Category: ${record.category} | ${record.selection}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cash info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = when (record.outcome) {
                        "DEPOSIT" -> "+" + formatShillings(record.stakeAmount, currency)
                        "WITHDRAW" -> "-" + formatShillings(record.stakeAmount, currency)
                        else -> "Stake: " + formatShillings(record.stakeAmount, currency)
                    },
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (record.outcome == "WON") {
                    Text(
                        text = "Pay: " + formatShillings(record.potentialPayout, currency),
                        color = NeonSuccess,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                } else if (record.outcome == "PENDING") {
                    Text(
                        text = "Est: " + formatShillings(record.potentialPayout, currency),
                        color = GoldPrimary,
                        fontSize = 11.sp
                    )
                } else if (record.outcome == "LOST") {
                    Text(
                        text = "Lost Stake",
                        color = NeonDanger,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "Settled",
                        color = AccentBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

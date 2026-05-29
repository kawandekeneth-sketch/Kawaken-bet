package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BetRecord
import com.example.data.BetRepository
import com.example.data.UserAccount
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

// In-Memory Sports Match Model
data class SportMatch(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val minute: Int = 0, // 0 = pre-match, 1..90 = live, 90 = completed
    val homeOdds: Double,
    val drawOdds: Double,
    val awayOdds: Double,
    val isLive: Boolean = false,
    val isFinished: Boolean = false,
    val league: String = "Champions League"
)

// Active bet selection on matches
data class SelectedSportBet(
    val match: SportMatch,
    val selection: String, // "HOME", "DRAW", "AWAY"
    val odds: Double
)

class KawakenBetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BetRepository
    
    // User info flow from Room
    val userAccount: StateFlow<UserAccount?>
    
    // Bets history flow from Room
    val allBets: StateFlow<List<BetRecord>>

    // Live matches in memory
    private val _liveMatches = MutableStateFlow<List<SportMatch>>(emptyList())
    val liveMatches: StateFlow<List<SportMatch>> = _liveMatches.asStateFlow()

    // Selected bet for placing slips
    var activeBetSlip by mutableStateOf<SelectedSportBet?>(null)
        private set

    // Bet slip stake state input
    var betSlipStakeInput by mutableStateOf("")

    // Match Engine ticking Job
    private var engineJob: Job? = null

    // --- CASINO: CRASH GAME STATES ---
    var isCrashFlying by mutableStateOf(false)
        private set
    var currentCrashMultiplier by mutableStateOf(1.0)
        private set
    var isCrashBusted by mutableStateOf(false)
        private set
    var hasCashedOutCrash by mutableStateOf(false)
        private set
    var crashStakeAmount by mutableStateOf(1000.0) // initial stake
    var crashLogMsg by mutableStateOf("Place your stake in Shillings and take off!")
        private set
    private var crashJob: Job? = null
    private var actualCrashPoint = 1.0

    // --- CASINO: SLOTS GAME STATES ---
    var isSlotsSpinning by mutableStateOf(false)
        private set
    val slotSymbols = listOf("🍒", "🍋", "🍇", "🔔", "💎", "7️⃣")
    var currentSlotsReels by mutableStateOf(listOf("7️⃣", "7️⃣", "7️⃣"))
        private set
    var slotsStakeAmount by mutableStateOf(1000.0)
    var slotsWinResultMsg by mutableStateOf("Spin the Gold Reels to win up to 50x!")
        private set

    // --- CASINO: LUCKY WHEEL STATES ---
    var isWheelSpinning by mutableStateOf(false)
        private set
    var wheelAngleTarget by mutableStateOf(0f)
        private set
    var wheelDrawnAngle by mutableStateOf(0f)
        private set
    val wheelMultipliers = listOf(0.0, 2.0, 0.5, 5.0, 0.0, 1.5, 10.0, 3.0)
    val wheelColours = listOf(0xFFE57373, 0xFF81C784, 0xFFFFB74D, 0xFF64B5F6, 0xFFE0E0E0, 0xFFBA68C8, 0xFFFFD54F, 0xFF4DB6AC)
    var wheelStakeAmount by mutableStateOf(1000.0)
    var wheelResultMsg by mutableStateOf("Spin the Lucky Circle multiplier wheel!")
        private set

    // --- MOBILE MONEY SIMULATION STATES ---
    var isMoMoProcessing by mutableStateOf(false)
    var moMomessage by mutableStateOf("")
    var isMoMoSuccess by mutableStateOf<Boolean?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BetRepository(database.betDao())
        
        userAccount = repository.getUserAccount("kawaken")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
            
        allBets = repository.allBets
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Bootstrap initial user account if database is empty
        viewModelScope.launch {
            val current = repository.getUserAccount("kawaken").first()
            if (current == null) {
                repository.updateAccount(UserAccount("kawaken", 100000.0, "UGX"))
            }
            // Generate starter matches
            generateStarterMatches()
            // Start the simulated live sports ticker
            startMatchEngine()
        }
    }

    // Generate simulated football matches
    private fun generateStarterMatches() {
        val list = listOf(
            SportMatch(1, "Arsenal", "Chelsea", 0, 0, 0, 1.85, 3.40, 4.10, isLive = true),
            SportMatch(2, "Real Madrid", "Barcelona", 0, 0, 0, 2.10, 3.20, 3.30, isLive = true),
            SportMatch(3, "Man City", "Liverpool", 0, 0, 0, 1.95, 3.50, 3.60, isLive = false, league = "Premier League"),
            SportMatch(4, "Bayern Munich", "Dortmund", 0, 0, 0, 1.55, 4.20, 5.50, isLive = false, league = "Bundesliga"),
            SportMatch(5, "PSG", "Marseille", 0, 0, 0, 1.40, 4.60, 7.00, isLive = false, league = "Ligue 1")
        )
        _liveMatches.value = list
    }

    private fun startMatchEngine() {
        engineJob?.cancel()
        engineJob = viewModelScope.launch {
            while (true) {
                delay(4000) // update games every 4 seconds
                val currentList = _liveMatches.value.map { match ->
                    if (match.isFinished) {
                        // Automatically replace finished matches with a new random pairing
                        generateRandomReplacementMatch(match.id)
                    } else if (match.isLive) {
                        val nextMinute = match.minute + Random.nextInt(8, 15)
                        if (nextMinute >= 90) {
                            // Finish the match and settle sports bets!
                            val finalMatch = match.copy(minute = 90, isLive = false, isFinished = true)
                            settleBetsForMatch(finalMatch)
                            finalMatch
                        } else {
                            // Simulated goals chance
                            var hScore = match.homeScore
                            var aScore = match.awayScore
                            if (Random.nextDouble() < 0.18) {
                                if (Random.nextBoolean()) hScore++ else aScore++
                            }
                            // Shift Odds floating in live mode
                            val homeShift = if (hScore > aScore) -0.3 else 0.2
                            val awayShift = if (aScore > hScore) -0.3 else 0.2
                            
                            val draftH = (match.homeOdds + homeShift).coerceIn(1.05, 20.0)
                            val draftA = (match.awayOdds + awayShift).coerceIn(1.05, 20.0)
                            val draftD = (match.drawOdds + (if (hScore == aScore) -0.1 else 0.1)).coerceIn(1.05, 10.0)

                            match.copy(
                                minute = nextMinute,
                                homeScore = hScore,
                                awayScore = aScore,
                                homeOdds = Math.round(draftH * 100.0) / 100.0,
                                awayOdds = Math.round(draftA * 100.0) / 100.0,
                                drawOdds = Math.round(draftD * 100.0) / 100.0
                            )
                        }
                    } else {
                        // Chance pre-match kicks off
                        if (Random.nextDouble() < 0.25) {
                            match.copy(isLive = true, minute = 1)
                        } else {
                            match
                        }
                    }
                }
                _liveMatches.value = currentList
            }
        }
    }

    private fun generateRandomReplacementMatch(id: Int): SportMatch {
        val teams = listOf(
            "Manchester United", "Newcastle", "Tottenham", "Aston Villa", "Ajax", "Juventus", 
            "Inter Milan", "AC Milan", "Atletico Madrid", "Napoli", "Roma", "Benfica"
        ).shuffled()
        val hName = teams[0]
        val aName = teams[1]
        return SportMatch(
            id = id,
            homeTeam = hName,
            awayTeam = aName,
            homeScore = 0,
            awayScore = 0,
            minute = 0,
            homeOdds = Math.round(Random.nextDouble(1.4, 4.0) * 100.0) / 100.0,
            drawOdds = Math.round(Random.nextDouble(2.8, 3.8) * 100.0) / 100.0,
            awayOdds = Math.round(Random.nextDouble(1.8, 5.0) * 100.0) / 100.0,
            isLive = true,
            isFinished = false,
            league = listOf("Champions League", "Premier League", "Serie A", "La Liga").random()
        )
    }

    // Room persistence resolver for completed match sports bets
    private suspend fun settleBetsForMatch(match: SportMatch) {
        val currentBets = allBets.value
        val actualOutcome = when {
            match.homeScore > match.awayScore -> "HOME"
            match.homeScore < match.awayScore -> "AWAY"
            else -> "DRAW"
        }
        
        currentBets.forEach { bet ->
            if (bet.category == "SPORTS" && !bet.isSettled && bet.title.startsWith("${match.homeTeam} vs ${match.awayTeam}")) {
                val userChoice = bet.selection.split(" ")[0].uppercase() // HOME, AWAY, DRAW
                val isWin = (userChoice == actualOutcome)
                val status = if (isWin) "WON" else "LOST"
                
                val updatedBet = bet.copy(
                    isSettled = true,
                    outcome = status,
                    title = "${match.homeTeam} vs ${match.awayTeam} (${match.homeScore}-${match.awayScore})"
                )
                repository.updateBet(updatedBet)

                if (isWin) {
                    val acc = userAccount.value
                    if (acc != null) {
                        repository.updateAccount(acc.copy(balance = acc.balance + bet.potentialPayout))
                    }
                }
            }
        }
    }

    // --- USER PROFILE OPERATIONS ---
    fun depositFreeShinnings() {
        viewModelScope.launch {
            val current = userAccount.value ?: UserAccount("kawaken", 100000.0, "UGX")
            repository.updateAccount(current.copy(balance = current.balance + 50000.0))
        }
    }

    fun updateMobileMoneyAccount(phone: String, provider: String) {
        viewModelScope.launch {
            val current = userAccount.value ?: return@launch
            repository.updateAccount(current.copy(mobileNumber = phone, mobileProvider = provider))
        }
    }

    fun depositMobileMoney(amount: Double) {
        if (isMoMoProcessing) return
        isMoMoProcessing = true
        isMoMoSuccess = null
        moMomessage = "Sending instant STK push request to mobile money wallet..."

        viewModelScope.launch {
            delay(1500)
            val current = userAccount.value ?: return@launch
            moMomessage = "Billing authorized by user via secure PIN entry. Funding wallet..."
            delay(1000)

            repository.updateAccount(current.copy(balance = current.balance + amount))
            isMoMoSuccess = true
            isMoMoProcessing = false
            moMomessage = "Success! Deposited Sh ${String.format("%,.0f", amount)} ${current.preferredCurrency} from mobile account ${current.mobileNumber}."

            // Insert to local database history for proof
            repository.insertBet(
                BetRecord(
                    title = "MoMo Deposit Completed",
                    category = "WALLET",
                    selection = "Reference: ${System.currentTimeMillis() % 1000000}",
                    stakeAmount = amount,
                    odd = 1.0,
                    potentialPayout = amount,
                    isSettled = true,
                    outcome = "DEPOSIT"
                )
            )
        }
    }

    fun withdrawMobileMoney(amount: Double) {
        if (isMoMoProcessing) return
        val current = userAccount.value ?: return
        if (current.balance < amount) {
            moMomessage = "Failed! Insufficient balance to process withdrawal."
            isMoMoSuccess = false
            return
        }

        isMoMoProcessing = true
        isMoMoSuccess = null
        moMomessage = "Initiating instant payout processing to cash account ${current.mobileNumber} via ${current.mobileProvider}..."

        viewModelScope.launch {
            delay(1800)
            val updatedBalance = current.balance - amount
            repository.updateAccount(current.copy(balance = updatedBalance))
            
            moMomessage = "Transferring Shillings: Network handshake cleared..."
            delay(1000)

            isMoMoSuccess = true
            isMoMoProcessing = false
            moMomessage = "Payout Approved! Securely sent Sh ${String.format("%,.0f", amount)} ${current.preferredCurrency} to your registered mobile wallet."

            // Insert to transaction history for logs
            repository.insertBet(
                BetRecord(
                    title = "MoMo Mobile Pay Out",
                    category = "WALLET",
                    selection = "Sent to ${current.mobileNumber} (${current.mobileProvider})",
                    stakeAmount = amount,
                    odd = 1.0,
                    potentialPayout = amount,
                    isSettled = true,
                    outcome = "WITHDRAW"
                )
            )
        }
    }

    fun changeCurrency(newCurrency: String) {
        viewModelScope.launch {
            val current = userAccount.value ?: return@launch
            repository.updateAccount(current.copy(preferredCurrency = newCurrency))
        }
    }

    fun clearBetHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- SPORTS BET SLIP ACTIONS ---
    fun selectBetOption(match: SportMatch, type: String, odds: Double) {
        activeBetSlip = SelectedSportBet(match, type, odds)
        betSlipStakeInput = "1000" // default stake Sh 1000
    }

    fun placeSportsBet() {
        val slip = activeBetSlip ?: return
        val stake = betSlipStakeInput.toDoubleOrNull() ?: return
        if (stake <= 0.0) return

        viewModelScope.launch {
            val account = userAccount.value ?: return@launch
            if (account.balance >= stake) {
                // Deduct Balance
                repository.updateAccount(account.copy(balance = account.balance - stake))
                
                // Save Bet Rec
                val title = "${slip.match.homeTeam} vs ${slip.match.awayTeam}"
                val choiceDisplay = when(slip.selection) {
                    "HOME" -> "${slip.match.homeTeam} Win"
                    "AWAY" -> "${slip.match.awayTeam} Win"
                    else -> "Draw Match"
                }

                val bet = BetRecord(
                    title = title,
                    category = "SPORTS",
                    selection = choiceDisplay,
                    stakeAmount = stake,
                    odd = slip.odds,
                    potentialPayout = stake * slip.odds,
                    isSettled = false,
                    outcome = "PENDING"
                )
                repository.insertBet(bet)
                
                // Reset slip input
                activeBetSlip = null
                betSlipStakeInput = ""
            }
        }
    }

    fun closeBetSlip() {
        activeBetSlip = null
    }

    // --- CASINO: CRASH GAME (AVIATOR STYLE) ACTION ---
    fun placeAndStartCrashFly() {
        if (isCrashFlying) return
        val currentAccount = userAccount.value ?: return
        if (currentAccount.balance < crashStakeAmount) {
            crashLogMsg = "Insufficient balance! Please claim free Shillings."
            return
        }

        hasCashedOutCrash = false
        isCrashBusted = false
        currentCrashMultiplier = 1.00
        isCrashFlying = true
        crashLogMsg = "Lucky Plane is airborn! Multiply your Shillings!"

        // Decide the crash event limit beforehand
        // 90% chance to crash dynamically; higher crash points are less likely
        val r = Random.nextDouble()
        actualCrashPoint = when {
            r < 0.10 -> 1.05 + Random.nextDouble(1.0)              // 10% instant crash early
            r < 0.60 -> 1.20 + Random.nextDouble(1.8)              // 50% crash between 1.2 and 3.0
            r < 0.90 -> 3.00 + Random.nextDouble(5.0)              // 30% crash between 3.0 and 8.0
            else -> 8.00 + Random.nextDouble(25.0)                 // Jackpot flight! up to 33x
        }
        // Round to 2 decimals
        actualCrashPoint = Math.round(actualCrashPoint * 100.0) / 100.0

        // Deduct Stake
        viewModelScope.launch {
            repository.updateAccount(currentAccount.copy(balance = currentAccount.balance - crashStakeAmount))
        }

        crashJob?.cancel()
        crashJob = viewModelScope.launch {
            var ticker = 1.0
            while (ticker < actualCrashPoint) {
                delay(120) // multiplier velocity
                // Multiplier grows faster as length of flight increases
                val rate = if (ticker < 2.0) 0.05 else if (ticker < 5.0) 0.12 else 0.30
                ticker += rate
                currentCrashMultiplier = Math.round(ticker * 100.0) / 100.0
            }

            // Crash occurred!
            currentCrashMultiplier = actualCrashPoint
            isCrashBusted = true
            isCrashFlying = false
            
            if (!hasCashedOutCrash) {
                crashLogMsg = "PLANE CRASHED at ${actualCrashPoint}x! Staked Sh ${crashStakeAmount} lost."
                // Record lost bet in Room database
                repository.insertBet(
                    BetRecord(
                        title = "Crash Aviator Flight",
                        category = "CRASH",
                        selection = "Lost - Crashed @ ${actualCrashPoint}x",
                        stakeAmount = crashStakeAmount,
                        odd = 0.0,
                        potentialPayout = 0.0,
                        isSettled = true,
                        outcome = "LOST"
                    )
                )
            }
        }
    }

    fun cashOutCrash() {
        if (!isCrashFlying || hasCashedOutCrash || isCrashBusted) return
        hasCashedOutCrash = true
        isCrashFlying = false
        val winMultiplier = currentCrashMultiplier
        val totalWin = crashStakeAmount * winMultiplier
        crashLogMsg = "CASHOUT SUCCESSFUL! Caught at ${winMultiplier}x. Won Sh ${Math.round(totalWin)}!"

        viewModelScope.launch {
            val currentAccount = userAccount.value ?: return@launch
            repository.updateAccount(currentAccount.copy(balance = currentAccount.balance + totalWin))
            
            repository.insertBet(
                BetRecord(
                    title = "Crash Aviator Flight",
                    category = "CRASH",
                    selection = "Casher Out @ ${winMultiplier}x",
                    stakeAmount = crashStakeAmount,
                    odd = winMultiplier,
                    potentialPayout = totalWin,
                    isSettled = true,
                    outcome = "WON"
                )
            )
        }
    }

    // --- CASINO: SLOTS GAME ACTION ---
    fun spinSlots() {
        if (isSlotsSpinning) return
        val currentAccount = userAccount.value ?: return
        if (currentAccount.balance < slotsStakeAmount) {
            slotsWinResultMsg = "Insufficient balance! Please claim free funds."
            return
        }

        isSlotsSpinning = true
        slotsWinResultMsg = "Shuffling the reels..."

        // Deduct stake
        viewModelScope.launch {
            repository.updateAccount(currentAccount.copy(balance = currentAccount.balance - slotsStakeAmount))
        }

        viewModelScope.launch {
            // Spin duration animation
            for (i in 1..10) {
                currentSlotsReels = listOf(slotSymbols.random(), slotSymbols.random(), slotSymbols.random())
                delay(120)
            }

            val final1 = slotSymbols.random()
            val final2 = slotSymbols.random()
            val final3 = slotSymbols.random()
            currentSlotsReels = listOf(final1, final2, final3)

            isSlotsSpinning = false

            // Check payout
            val payoutFactor: Double
            val outcome: String
            val selectionText: String
            
            if (final1 == final2 && final2 == final3) {
                // Triple Match!
                payoutFactor = if (final1 == "7️⃣") 50.0 else if (final1 == "💎") 20.0 else if (final1 == "🔔") 12.0 else 8.0
                outcome = "WON"
                selectionText = "TRIPLE MATCH $final1"
            } else if (final1 == final2 || final2 == final3 || final1 == final3) {
                // Pair Match!
                payoutFactor = 1.5
                outcome = "WON"
                selectionText = "PAIR MATCH $final1/$final2/$final3"
            } else {
                payoutFactor = 0.0
                outcome = "LOST"
                selectionText = "No Match: $final1|$final2|$final3"
            }

            val potentialPayout = slotsStakeAmount * payoutFactor
            if (payoutFactor > 0.0) {
                slotsWinResultMsg = "WINNER! You matched $selectionText and won Sh ${Math.round(potentialPayout)}!"
                val acc = repository.getUserAccount("kawaken").first()
                if (acc != null) {
                    repository.updateAccount(acc.copy(balance = acc.balance + potentialPayout))
                }
            } else {
                slotsWinResultMsg = "Try again! Better luck on the next spin."
            }

            // Record slots bet inside Room
            repository.insertBet(
                BetRecord(
                    title = "Gold Reels Slot",
                    category = "SLOTS",
                    selection = selectionText,
                    stakeAmount = slotsStakeAmount,
                    odd = payoutFactor,
                    potentialPayout = potentialPayout,
                    isSettled = true,
                    outcome = outcome
                )
            )
        }
    }

    // --- CASINO: WHEEL GAME ACTION ---
    fun spinLuckyWheel() {
        if (isWheelSpinning) return
        val currentAccount = userAccount.value ?: return
        if (currentAccount.balance < wheelStakeAmount) {
            wheelResultMsg = "Insufficient balance! Please claim free funds."
            return
        }

        isWheelSpinning = true
        wheelResultMsg = "Spinning the Wheel of Fortune..."

        // Deduct stake
        viewModelScope.launch {
            repository.updateAccount(currentAccount.copy(balance = currentAccount.balance - wheelStakeAmount))
        }

        // Selected index
        val targetIndex = Random.nextInt(8)
        val selectedMultiplier = wheelMultipliers[targetIndex]
        
        // Calculate angle target
        // Each slice has width of 45 degrees
        // Target index centers on slice: angle = index * 45 + offset
        val baseAngle = targetIndex * 45f + 22.5f
        // Add 5-7 full spins to look real
        val totalRotationAngle = 360f * 6 + (360f - baseAngle)
        wheelAngleTarget = totalRotationAngle

        viewModelScope.launch {
            // Spin rotation kinetic friction simulation
            var elapsed = 0
            val duration = 2000
            val startAng = wheelDrawnAngle
            var currentAng: Float
            while (elapsed < duration) {
                delay(30)
                elapsed += 30
                // Ease out cubic
                val t = elapsed.toFloat() / duration
                val easeOut = 1f - Math.pow((1f - t).toDouble(), 3.0).toFloat()
                currentAng = startAng + (wheelAngleTarget - startAng) * easeOut
                wheelDrawnAngle = currentAng % 360f
            }

            // Complete simulation
            wheelDrawnAngle = (360f - baseAngle) % 360f
            isWheelSpinning = false

            val winPayout = wheelStakeAmount * selectedMultiplier
            val resultStatus = if (selectedMultiplier > 0.0) "WON" else "LOST"
            
            if (selectedMultiplier > 0.0) {
                wheelResultMsg = "CONGRATULATIONS! Settled on Segment ${targetIndex + 1} (${selectedMultiplier}x). Won Sh ${Math.round(winPayout)}!"
                val acc = repository.getUserAccount("kawaken").first()
                if (acc != null) {
                    repository.updateAccount(acc.copy(balance = acc.balance + winPayout))
                }
            } else {
                wheelResultMsg = "Bust! Settled on a empty gray 0x multiplier segment."
            }

            // Record Roulette/Wheel bet inside database
            repository.insertBet(
                BetRecord(
                    title = "Lucky Wheel Spin",
                    category = "ROULETTE",
                    selection = "Multiplier ${selectedMultiplier}x (Slice ${targetIndex + 1})",
                    stakeAmount = wheelStakeAmount,
                    odd = selectedMultiplier,
                    potentialPayout = winPayout,
                    isSettled = true,
                    outcome = resultStatus
                )
            )
        }
    }
}

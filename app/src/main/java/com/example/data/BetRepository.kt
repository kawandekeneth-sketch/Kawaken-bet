package com.example.data

import kotlinx.coroutines.flow.Flow

class BetRepository(private val betDao: BetDao) {
    val allBets: Flow<List<BetRecord>> = betDao.getAllBets()
    
    fun getUserAccount(username: String = "kawaken"): Flow<UserAccount?> =
        betDao.getUserAccount(username)

    suspend fun updateAccount(account: UserAccount) {
        betDao.insertOrUpdateUserAccount(account)
    }

    suspend fun insertBet(bet: BetRecord): Long {
        return betDao.insertBet(bet)
    }

    suspend fun updateBet(bet: BetRecord) {
        betDao.updateBet(bet)
    }
    
    suspend fun clearHistory() {
        betDao.clearHistory()
    }
}

package com.example.data.repository

import com.example.data.db.CalculationHistory
import com.example.data.db.HistoryDao
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

    val allHistory: Flow<List<CalculationHistory>> = historyDao.getAllHistory()
    val favoriteHistory: Flow<List<CalculationHistory>> = historyDao.getFavoriteHistory()

    suspend fun insert(expression: String, result: String, mode: String = "Standard") {
        if (expression.isBlank() || result.isBlank() || result == "Error") return
        val history = CalculationHistory(
            expression = expression,
            result = result,
            mode = mode
        )
        historyDao.insertHistory(history)
    }

    suspend fun deleteById(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearAll() {
        historyDao.clearAllHistory()
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        historyDao.updateFavoriteStatus(id, !currentStatus)
    }
}

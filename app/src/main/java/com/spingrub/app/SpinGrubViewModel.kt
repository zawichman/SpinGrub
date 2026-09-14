package com.spingrub.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spingrub.app.data.Category
import com.spingrub.app.data.SpinGrubData
import com.spingrub.app.data.SpinGrubRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpinGrubViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SpinGrubRepository(app.applicationContext)

    val state: StateFlow<SpinGrubData> = repo.data.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpinGrubData()
    )

    fun addItem(category: Category, item: String) = viewModelScope.launch {
        repo.addItem(category, item)
    }

    fun removeItem(category: Category, item: String) = viewModelScope.launch {
        repo.removeItem(category, item)
    }

    fun saveFavorite(name: String, meat: String, method: String, sauce: String) =
        viewModelScope.launch { repo.addFavorite(name, meat, method, sauce) }

    fun removeFavorite(id: String) = viewModelScope.launch { repo.removeFavorite(id) }

    fun setSound(enabled: Boolean) = viewModelScope.launch { repo.setSound(enabled) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { repo.setHaptics(enabled) }
    fun setConfetti(enabled: Boolean) = viewModelScope.launch { repo.setConfetti(enabled) }
    fun resetToDefaults() = viewModelScope.launch { repo.resetToDefaults() }
}

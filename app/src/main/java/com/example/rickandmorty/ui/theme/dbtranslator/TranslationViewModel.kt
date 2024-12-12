package com.example.rickandmorty.ui.theme.dbtranslator

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranslationViewModel(private val dao: TranslationDao) : ViewModel() {

    private val _translations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val translations: StateFlow<List<TranslationEntity>> = _translations

    init {
        fetchTranslations()
    }

    private fun fetchTranslations() {
        viewModelScope.launch {
            _translations.value = dao.getAllTranslations()
        }
    }
    // Metodo para eliminate una traducer
    fun deleteTranslation(translation: TranslationEntity) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    translation.id?.let { dao.deleteTranslation(it) }
                }
                // Refrescar los datos después de eliminar
                fetchTranslations()
                Log.d("DELETE", "Translation with ID ${translation.id} deleted successfully.")
            } catch (e: Exception) {
                Log.e("DELETE_ERROR", "Error deleting translation: ${e.message}")
            }
        }
    }

    fun updateTranslationCategory(updatedTranslation: TranslationEntity) {
        viewModelScope.launch {
            dao.updateTranslation(updatedTranslation)
        }
    }
}
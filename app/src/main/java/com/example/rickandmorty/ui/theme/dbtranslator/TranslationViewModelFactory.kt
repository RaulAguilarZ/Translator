package com.example.rickandmorty.ui.theme.dbtranslator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TranslationViewModelFactory(private val dao: TranslationDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TranslationViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

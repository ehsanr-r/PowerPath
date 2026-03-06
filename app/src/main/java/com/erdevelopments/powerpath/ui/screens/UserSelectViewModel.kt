package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.UserEntity
import com.erdevelopments.powerpath.data.local.dao.UserDao
import com.erdevelopments.powerpath.data.prefs.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSelectViewModel @Inject constructor(
    private val userDao: UserDao,
    private val prefs: PrefsRepository
) : ViewModel() {

    val users = userDao.observeUsers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectUser(userId: Long) {
        viewModelScope.launch { prefs.setSelectedUser(userId) }
    }

    fun addUser(name: String, age: Int, weightKg: Float, heightCm: Int) {
        viewModelScope.launch {
            val id = userDao.insert(UserEntity(name = name, age = age, weightKg = weightKg, heightCm = heightCm))
            prefs.setSelectedUser(id)
        }
    }
}
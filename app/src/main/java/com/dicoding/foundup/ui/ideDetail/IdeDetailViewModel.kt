package com.dicoding.foundup.ui.ideDetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.dicoding.foundup.data.UserRepository
import com.dicoding.foundup.data.pref.UserPreference
import com.dicoding.foundup.data.remote.ApiConfig
import com.dicoding.foundup.data.remote.ApiService
import com.dicoding.foundup.data.response.DetaiIdeData
import kotlinx.coroutines.launch

class IdeDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository
    private val apiService: ApiService = ApiConfig.getApiService()

    private val _ideDetail = MutableLiveData<DetaiIdeData?>()
    val ideDetail: LiveData<DetaiIdeData?> get() = _ideDetail

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> get() = _userRole

    init {
        val context = getApplication<Application>().applicationContext
        val userPreference = UserPreference.getInstance(context)
        userRepository = UserRepository(apiService, userPreference)
    }

    fun fetchDetailIde(postId: Int) {
        _loading.value = true

        getUserToken { token ->
            if (!token.isNullOrEmpty()) {
                viewModelScope.launch {
                    try {
                        val response = apiService.getDetailIde("Bearer $token", postId)
                        if (response.error == false) {
                            _ideDetail.value = response.data
                        } else {
                            _error.value = response.message
                        }
                    } catch (e: Exception) {
                        _error.value = "An error occurred: ${e.localizedMessage}"
                    } finally {
                        _loading.value = false
                    }
                }
            } else {
                _error.value = "Token is null or invalid"
                _loading.value = false
            }
        }
    }

    fun fetchUserRole() {
        getUserToken { token ->
            if (!token.isNullOrEmpty()) {
                viewModelScope.launch {
                    try {
                        val response = userRepository.getUserRole("Bearer $token")
                        _userRole.value = response?.data?.role
                    } catch (e: Exception) {
                        Log.e("IdeDetailViewModel", "Error fetching user role: ${e.localizedMessage}")
                        _userRole.value = null
                    }
                }
            } else {
                _userRole.value = null
            }
        }
    }

    fun getUserToken(onTokenRetrieved: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val token = userRepository.getUserToken()
                onTokenRetrieved(token)
            } catch (e: Exception) {
                onTokenRetrieved(null)
            }
        }
    }
}

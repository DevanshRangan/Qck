package com.dr.qck.application

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dr.qck.datastore.DatastoreRepository
import com.dr.qck.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    application: Application, private val datastoreRepository: DatastoreRepository
) : AndroidViewModel(application) {


    private val _userPreferences: MutableLiveData<UserPreferences> = MutableLiveData()
    val userPreferences: LiveData<UserPreferences> = _userPreferences

    fun getUserPreferences() = viewModelScope.launch(Dispatchers.IO) {
        Log.d("Datastore", "Get Called,,,")
        datastoreRepository.userPreferencesFlow.collect { prefs ->
            _userPreferences.postValue(prefs)
        }
    }

    fun updateUserPreferences(key: Preferences.Key<*>, value: Any) =
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Datastore", "Update Called $value")
            datastoreRepository.updateKey(key, value)
        }
}
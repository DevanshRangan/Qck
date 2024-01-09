package com.dr.qck.activities.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dr.qck.database.ExceptionDao
import com.dr.qck.database.ExceptionMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExceptionViewModel @Inject constructor(
    application: Application, private val dao: ExceptionDao
) : AndroidViewModel(application) {

    private val _exceptionList: MutableLiveData<List<ExceptionMessage>> = MutableLiveData()
    val exceptionList: LiveData<List<ExceptionMessage>> = _exceptionList

    fun getExceptionList() = viewModelScope.launch(Dispatchers.IO) {
        val list = dao.getExceptionList()
        _exceptionList.postValue(list)
    }


    private val _deleteException: MutableLiveData<Int> = MutableLiveData()
    val deleteException: LiveData<Int> = _deleteException

    fun deleteItem(position: Int, message: ExceptionMessage) =
        viewModelScope.launch(Dispatchers.IO) {
            dao.removeException(message)
            _deleteException.postValue(position)
        }

}
package com.samy.ganna.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.samy.ganna.data.BookServices
import com.samy.ganna.pojo.Page
import com.samy.ganna.pojo.Title
import com.samy.ganna.utils.NetworkState
import com.samy.ganna.utils.Utils.myLog
import com.samy.ganna.utils.Utils.replaceArabicNumbers
import kotlinx.coroutines.launch

@HiltViewModel
class BookViewModel @Inject constructor(private val repository: BookRepository) : ViewModel() {

    private val _bookStateFlow = MutableStateFlow<NetworkState>(NetworkState.Idle)
    val bookStateFlow: MutableStateFlow<NetworkState> get() = _bookStateFlow


    private val _titleStateFlow = MutableStateFlow<NetworkState>(NetworkState.Idle)
    val titleStateFlow: MutableStateFlow<NetworkState> get() = _titleStateFlow


    fun getBook() {

        _bookStateFlow.value = NetworkState.Loading
//        myLog("getBook")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                repository.getBook()
            }.onSuccess {
//                it.arr.map {
//                        page ->
//                    Page(
//                        page.id,
//                        page.title.replaceArabicNumbers(),
//                        page.mainText.replaceArabicNumbers(),
//                        page.description.replaceArabicNumbers(),
//                        page.startRef,
//                        page.endRef
//                    )
//                }
                _bookStateFlow.value = NetworkState.Result(it)
            }
        }

    }

    fun getTitles() {

        _titleStateFlow.value = NetworkState.Loading
//        myLog("getTitles")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                repository.getBook().arr.map { page -> Title(page.id, page.title, false) }
            }.onSuccess {
                _titleStateFlow.value = NetworkState.Result(it)
            }.onFailure {
                _titleStateFlow.value = NetworkState.Error(1,it.message)
            }
        }

    }



}

class BookRepository @Inject constructor(private val services: BookServices) {

    suspend fun getBook() = services.getBook()


}